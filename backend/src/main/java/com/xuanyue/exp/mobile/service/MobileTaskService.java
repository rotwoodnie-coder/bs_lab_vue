package com.xuanyue.exp.mobile.service;

import com.xuanyue.exp.common.PageResult;
import com.xuanyue.exp.mobile.dto.CreateTaskRequest;
import com.xuanyue.exp.mobile.dto.MobileTaskDto;
import com.xuanyue.exp.mobile.dto.MobileTaskExpBriefDto;
import com.xuanyue.exp.mobile.dto.MobileTaskListItemDto;
import com.xuanyue.exp.mobile.entity.MbQuizRecord;
import com.xuanyue.exp.mobile.entity.MbTask;
import com.xuanyue.exp.mobile.entity.MbTaskSubmission;
import com.xuanyue.exp.mobile.repository.MbQuizRecordRepository;
import com.xuanyue.exp.mobile.repository.MbTaskRepository;
import com.xuanyue.exp.mobile.repository.MbTaskSubmissionRepository;
import com.xuanyue.exp.mobile.support.MobileEntityMapper;
import com.xuanyue.exp.mobile.support.MobileIds;
import com.xuanyue.exp.mobile.support.MobileJsonUtils;
import com.xuanyue.exp.mobile.support.MobileParentAccessService;
import com.xuanyue.exp.mobile.support.MobileUserContext;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MobileTaskService {

    private static final Set<String> EXPERIMENT_TYPES = new HashSet<>(Arrays.asList("homework", "simulator"));

    private final MbTaskRepository taskRepository;
    private final MbTaskSubmissionRepository submissionRepository;
    private final MbQuizRecordRepository quizRecordRepository;
    private final MobileQuizConfigService quizConfigService;
    private final MobileQuizQuestionAllocator quizQuestionAllocator;
    private final MobileTaskEnricher taskEnricher;
    private final MobileCreativeService creativeService;
    private final MobileParentAccessService parentAccessService;
    private final MobileNotificationService notificationService;

    public MobileTaskService(MbTaskRepository taskRepository,
                             MbTaskSubmissionRepository submissionRepository,
                             MbQuizRecordRepository quizRecordRepository,
                             MobileQuizConfigService quizConfigService,
                             MobileQuizQuestionAllocator quizQuestionAllocator,
                             MobileTaskEnricher taskEnricher,
                             MobileCreativeService creativeService,
                             MobileParentAccessService parentAccessService,
                             MobileNotificationService notificationService) {
        this.taskRepository = taskRepository;
        this.submissionRepository = submissionRepository;
        this.quizRecordRepository = quizRecordRepository;
        this.quizConfigService = quizConfigService;
        this.quizQuestionAllocator = quizQuestionAllocator;
        this.taskEnricher = taskEnricher;
        this.creativeService = creativeService;
        this.parentAccessService = parentAccessService;
        this.notificationService = notificationService;
    }

    public PageResult<MobileTaskListItemDto> listItems(String userId, String childUserId, String category, String status, int page, int size) {
        String studentId;
        try {
            studentId = parentAccessService.resolveStudentScope(userId, childUserId);
        } catch (IllegalArgumentException e) {
            throw e;
        }
        String resolvedCategory = resolveCategory(category);
        List<MobileTaskListItemDto> items;
        switch (resolvedCategory) {
            case "remix":
                items = listRemixItems(studentId, status);
                break;
            case "creative":
                items = listCreativeItems(studentId, status);
                break;
            case "quiz":
                items = listQuizItems(studentId, status);
                break;
            case "experiment":
            default:
                items = listExperimentItems(studentId, status);
                break;
        }
        items.sort(Comparator.comparingLong(MobileTaskListItemDto::getSortTime).reversed());
        return paginate(items, page, size);
    }

    private List<MobileTaskListItemDto> listExperimentItems(String studentId, String status) {
        List<MbTask> tasks = taskRepository.findByStatusOrderByCreateTimeDesc("y");
        List<MobileTaskListItemDto> items = new ArrayList<>();
        for (MbTask task : tasks) {
            if (!EXPERIMENT_TYPES.contains(task.getTaskType())) {
                continue;
            }
            Optional<MbTaskSubmission> sub = submissionRepository.findByTaskIdAndStudentUserId(task.getTaskId(), studentId);
            MobileTaskListItemDto item = toTaskListItem(task, sub.orElse(null));
            if (matchStatus(item, status)) {
                items.add(item);
            }
        }
        return items;
    }

    private List<MobileTaskListItemDto> listRemixItems(String studentId, String status) {
        Set<String> studentTaskIds = submissionRepository.findByStudentUserIdOrderByUpdateTimeDesc(studentId)
                .stream()
                .map(MbTaskSubmission::getTaskId)
                .collect(Collectors.toCollection(HashSet::new));
        List<MbTask> tasks = taskRepository.findByStatusOrderByCreateTimeDesc("y");
        List<MobileTaskListItemDto> items = new ArrayList<>();
        for (MbTask task : tasks) {
            if (!"remix".equals(task.getTaskType()) || !studentTaskIds.contains(task.getTaskId())) {
                continue;
            }
            Optional<MbTaskSubmission> sub = submissionRepository.findByTaskIdAndStudentUserId(task.getTaskId(), studentId);
            MobileTaskListItemDto item = toTaskListItem(task, sub.orElse(null));
            if (matchStatus(item, status)) {
                items.add(item);
            }
        }
        return items;
    }

    private List<MobileTaskListItemDto> listCreativeItems(String studentId, String status) {
        Set<String> studentTaskIds = submissionRepository.findByStudentUserIdOrderByUpdateTimeDesc(studentId)
                .stream()
                .map(MbTaskSubmission::getTaskId)
                .collect(Collectors.toCollection(HashSet::new));
        List<MbTask> tasks = taskRepository.findByStatusOrderByCreateTimeDesc("y");
        List<MobileTaskListItemDto> items = new ArrayList<>();
        for (MbTask task : tasks) {
            if (!"creative".equals(task.getTaskType()) || !studentTaskIds.contains(task.getTaskId())) {
                continue;
            }
            Optional<MbTaskSubmission> sub = submissionRepository.findByTaskIdAndStudentUserId(task.getTaskId(), studentId);
            MobileTaskListItemDto item = toTaskListItem(task, sub.orElse(null));
            if (matchStatus(item, status)) {
                items.add(item);
            }
        }
        if ("pending".equals(status) && items.isEmpty() && !creativeService.findInProgressCreative(studentId).isPresent()) {
            items.add(creativeService.buildStartCard());
        }
        return items;
    }

    private List<MobileTaskListItemDto> listQuizItems(String studentId, String status) {
        LocalDate today = LocalDate.now();
        java.sql.Date fromDate = java.sql.Date.valueOf(today.minusDays(6));
        java.sql.Date todaySql = java.sql.Date.valueOf(today);
        Optional<MbQuizRecord> todayRecord = quizRecordRepository.findByUserIdAndQuizDate(studentId, todaySql);

        List<MobileTaskListItemDto> items = new ArrayList<>();
        if ("pending".equals(status)) {
            if (!todayRecord.isPresent()) {
                items.add(buildTodayQuizPending());
            }
            return items;
        }

        if ("done".equals(status)) {
            if (todayRecord.isPresent()) {
                items.add(buildTodayQuizDone(todayRecord.get()));
            }
            List<MbQuizRecord> records = quizRecordRepository
                    .findByUserIdAndQuizDateGreaterThanEqualOrderByQuizDateDesc(studentId, fromDate);
            for (MbQuizRecord record : records) {
                if (record.getQuizDate() != null && record.getQuizDate().equals(todaySql)) {
                    continue;
                }
                items.add(toQuizHistoryItem(record));
            }
            return items;
        }

        if (!todayRecord.isPresent()) {
            items.add(buildTodayQuizPending());
        } else {
            items.add(buildTodayQuizDone(todayRecord.get()));
        }
        List<MbQuizRecord> records = quizRecordRepository
                .findByUserIdAndQuizDateGreaterThanEqualOrderByQuizDateDesc(studentId, fromDate);
        for (MbQuizRecord record : records) {
            if (record.getQuizDate() != null && record.getQuizDate().equals(todaySql)) {
                continue;
            }
            items.add(toQuizHistoryItem(record));
        }
        return items;
    }

    private MobileTaskListItemDto buildTodayQuizPending() {
        int count = resolveTodayQuizCount();
        MobileTaskListItemDto item = new MobileTaskListItemDto();
        item.setId("quiz-today");
        item.setCategory("quiz");
        item.setKind("quiz-today");
        item.setTitle("今日答题");
        item.setDesc(count + " 题 · 今日还可正式提交 1 次");
        item.setState("pending");
        item.setStateLabel("待完成");
        item.setBadgeClass("badge-warning");
        item.setLink("/quiz");
        item.setSortTime(System.currentTimeMillis());
        return item;
    }

    private int resolveTodayQuizCount() {
        if (!quizConfigService.isEnabled()) {
            return 0;
        }
        int configured = quizConfigService.getQuestionsPerDay();
        int eligible = quizQuestionAllocator.countEligibleQuestions();
        if (eligible <= 0) {
            return configured;
        }
        return Math.min(configured, eligible);
    }

    private MobileTaskListItemDto buildTodayQuizDone(MbQuizRecord record) {
        MobileTaskListItemDto item = new MobileTaskListItemDto();
        item.setId("quiz-today");
        item.setCategory("quiz");
        item.setKind("quiz-today");
        item.setTitle("今日答题");
        int score = record.getScore() != null ? record.getScore() : 0;
        int total = record.getTotal() != null ? record.getTotal() : 0;
        int points = record.getPoints() != null ? record.getPoints() : 0;
        item.setQuizScore(score + "/" + total + " · +" + points + " 分");
        item.setDesc("今日已完成 · 点击查看结果");
        item.setState("done");
        item.setStateLabel("已完成");
        item.setBadgeClass("badge-success");
        item.setLink("/quiz/completed");
        long sortTime = record.getCreateTime() != null
                ? record.getCreateTime().getTime()
                : record.getQuizDate().getTime();
        item.setSortTime(sortTime);
        return item;
    }

    private MobileTaskListItemDto toQuizHistoryItem(MbQuizRecord record) {
        MobileTaskListItemDto item = toQuizListItem(record, true);
        item.setKind("quiz-history");
        return item;
    }

    private MobileTaskListItemDto toQuizListItem(MbQuizRecord record, boolean done) {
        MobileTaskListItemDto item = new MobileTaskListItemDto();
        item.setId(record.getRecordId());
        item.setCategory("quiz");
        item.setKind("quiz");
        item.setTitle("每日答题");
        int score = record.getScore() != null ? record.getScore() : 0;
        int total = record.getTotal() != null ? record.getTotal() : 0;
        int points = record.getPoints() != null ? record.getPoints() : 0;
        item.setQuizScore(score + "/" + total + " · +" + points + " 分");
        item.setDesc(record.getQuizDate() + " · 已完成");
        item.setState(done ? "done" : "pending");
        item.setStateLabel(done ? "已完成" : "待完成");
        item.setBadgeClass(done ? "badge-success" : "badge-warning");
        String resultType = "y".equalsIgnoreCase(record.getPerfect()) ? "perfect" : "low";
        item.setLink("/quiz/result/" + resultType + "?date=" + record.getQuizDate());
        long sortTime = record.getCreateTime() != null
                ? record.getCreateTime().getTime()
                : record.getQuizDate().getTime();
        item.setSortTime(sortTime);
        return item;
    }

    private MobileTaskListItemDto toTaskListItem(MbTask task, MbTaskSubmission submission) {
        MobileTaskDto dto = MobileEntityMapper.toTaskDto(task, submission);
        MobileTaskListItemDto item = new MobileTaskListItemDto();
        item.setId(task.getTaskId());
        item.setKind("task");
        item.setTitle(dto.getTitle());
        item.setDesc(dto.getDesc());
        item.setState(dto.getState());
        item.setStateLabel(dto.getStateLabel());
        item.setBadgeClass(dto.getBadgeClass());
        item.setDeadline(dto.getDeadline());
        item.setLink("/tasks/" + task.getTaskId());
        item.setSortTime(resolveSortTime(task, submission));

        String taskType = task.getTaskType();
        if ("remix".equals(taskType)) {
            item.setCategory("remix");
        } else if ("creative".equals(taskType)) {
            item.setCategory("creative");
        } else {
            item.setCategory("experiment");
            item.setSubType("simulator".equals(taskType) ? "simulator" : "standard");
        }
        if (!"done".equals(item.getState())) {
            item.setUploadLink(buildUploadLink(task));
            if (StringUtils.hasText(task.getVideoId())) {
                MobileTaskExpBriefDto brief = taskEnricher.buildExpBrief(task.getVideoId().trim());
                if (brief != null && StringUtils.hasText(brief.getMaterialsLine())) {
                    item.setMaterialsLine(brief.getMaterialsLine());
                }
            }
        }
        return item;
    }

    private String buildUploadLink(MbTask task) {
        if (task == null) {
            return "/upload";
        }
        if (StringUtils.hasText(task.getUploadQuery())) {
            return "/upload?" + task.getUploadQuery().trim();
        }
        String taskId = task.getTaskId();
        if (!StringUtils.hasText(taskId)) {
            return "/upload";
        }
        String taskType = task.getTaskType();
        if ("remix".equals(taskType)) {
            return "/upload?type=remix&taskId=" + taskId.trim();
        }
        if ("creative".equals(taskType)) {
            return "/upload?type=creative&taskId=" + taskId.trim();
        }
        return "/upload?taskId=" + taskId.trim();
    }

    private long resolveSortTime(MbTask task, MbTaskSubmission submission) {
        if (submission != null && submission.getUpdateTime() != null) {
            return submission.getUpdateTime().getTime();
        }
        if (submission != null && submission.getCreateTime() != null) {
            return submission.getCreateTime().getTime();
        }
        if (task.getCreateTime() != null) {
            return task.getCreateTime().getTime();
        }
        return 0L;
    }

    private String resolveCategory(String category) {
        if (!StringUtils.hasText(category)) {
            return "experiment";
        }
        String value = category.trim();
        if ("homework".equals(value) || "all".equals(value)) {
            return "experiment";
        }
        return value;
    }

    public MobileTaskDto get(String userId, String taskId) {
        Optional<MbTask> taskOpt = taskRepository.findById(taskId);
        if (taskOpt.isPresent()) {
            MbTask task = taskOpt.get();
            String studentId = MobileUserContext.resolveStudentId(userId);
            Optional<MbTaskSubmission> sub = submissionRepository.findByTaskIdAndStudentUserId(taskId, studentId);
            MobileTaskDto dto = MobileEntityMapper.toTaskDto(task, sub.orElse(null));
            taskEnricher.enrich(dto, task);
            return dto;
        }
        return null;
    }

    public MobileTaskDto createTask(String userId, CreateTaskRequest request) {
        if (request == null || !StringUtils.hasText(request.getExperimentTitle())) {
            throw new IllegalArgumentException("请选择关联实验");
        }
        String title = request.getExperimentTitle().trim();
        String teacherId = MobileUserContext.resolveTeacherId(userId);
        String taskType = StringUtils.hasText(request.getTaskType()) ? request.getTaskType().trim() : "homework";
        if ("experiment".equals(taskType)) {
            taskType = "homework";
        }

        MbTask task = new MbTask();
        task.setTaskId(MobileIds.newId("task"));
        task.setTitle(title);
        task.setDescription(StringUtils.hasText(request.getRequirements())
                ? request.getRequirements().trim()
                : "完成实验并上传成果照片或短视频。");
        task.setTaskType(taskType);
        task.setTeacherUserId(teacherId);
        if (StringUtils.hasText(request.getClassOrgId())) {
            task.setClassOrgId(request.getClassOrgId().trim());
        } else if (StringUtils.hasText(request.getClassName())) {
            task.setClassOrgId(request.getClassName().trim());
        }
        task.setDeadline(parseDeadline(request.getDeadline()));
        task.setVideoTitle(title);
        if (StringUtils.hasText(request.getSimulatorId())) {
            task.setVideoId(request.getSimulatorId().trim());
        } else if (StringUtils.hasText(request.getExperimentId())) {
            task.setVideoId(request.getExperimentId().trim());
        }
        if ("simulator".equals(taskType)) {
            task.setTeacherHint("模拟实验 · 老师布置");
            task.setTeacherHintClass("tint-violet");
        } else {
            task.setTeacherHint("指导老师 · 老师布置");
            task.setTeacherHintClass("tint-orange");
        }
        task.setTaskTypeLabel("老师布置");
        if (StringUtils.hasText(request.getRequirements())) {
            task.setRequirementsJson(MobileJsonUtils.toJson(Collections.singletonList(request.getRequirements().trim())));
        } else {
            task.setRequirementsJson(MobileJsonUtils.toJson(Collections.singletonList("提交实验照片与心得")));
        }
        task.setStepsJson(MobileJsonUtils.toJson(Collections.singletonList("完成实验后上传成果")));
        task.setUploadQuery("");
        task.setStatus("y");
        task.setCreateTime(new Date());
        taskRepository.save(task);
        notificationService.sendTaskAssigned(teacherId, task);
        return MobileEntityMapper.toTaskDto(task, null);
    }

    private Date parseDeadline(String raw) {
        if (!StringUtils.hasText(raw)) {
            return null;
        }
        String value = raw.trim();
        String[] patterns = {"yyyy-MM-dd'T'HH:mm", "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd HH:mm:ss"};
        for (String pattern : patterns) {
            try {
                return new SimpleDateFormat(pattern).parse(value);
            } catch (ParseException ignored) {
            }
        }
        return null;
    }

    private boolean matchStatus(MobileTaskListItemDto item, String status) {
        if (!StringUtils.hasText(status)) {
            return true;
        }
        if ("done".equals(status)) {
            return "done".equals(item.getState());
        }
        if ("pending".equals(status)) {
            return !"done".equals(item.getState());
        }
        return true;
    }

    private <T> PageResult<T> paginate(List<T> all, int page, int size) {
        int safeSize = Math.max(size, 1);
        int from = Math.max(page - 1, 0) * safeSize;
        if (from >= all.size()) {
            return new PageResult<>(all.size(), new ArrayList<T>());
        }
        int to = Math.min(from + safeSize, all.size());
        return new PageResult<>(all.size(), all.subList(from, to));
    }
}
