package com.xuanyue.exp.mobile.service;

import com.xuanyue.exp.common.PageResult;
import com.xuanyue.exp.exp.entity.ExpMsg;
import com.xuanyue.exp.exp.repository.ExpMsgRepository;
import com.xuanyue.exp.exp.repository.ExpSimulatorRepository;
import com.xuanyue.exp.mobile.dto.*;
import com.xuanyue.exp.mobile.entity.MobileExpHomework;
import com.xuanyue.exp.mobile.entity.MbQuizRecord;
import com.xuanyue.exp.mobile.entity.MbTask;
import com.xuanyue.exp.mobile.entity.MbTaskSubmission;
import com.xuanyue.exp.mobile.repository.MbQuizRecordRepository;
import com.xuanyue.exp.mobile.repository.MbTaskRepository;
import com.xuanyue.exp.mobile.repository.MbTaskSubmissionRepository;
import com.xuanyue.exp.mobile.repository.MobileExpHomeworkRepository;
import com.xuanyue.exp.mobile.support.MobileEntityMapper;
import com.xuanyue.exp.mobile.support.MobileParentAccessService;
import com.xuanyue.exp.mobile.support.MobileUserContext;
import com.xuanyue.exp.system.entity.SysUser;
import com.xuanyue.exp.system.repository.SysUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * MobileTaskService — 兼容版
 *
 * 仍使用 MbTask 读取旧数据，新数据（作业）从 exp_homework + exp_msg 获取。
 * createTask → 委托 MobileStudentWorkService
 */
@Service
public class MobileTaskService {

    private static final Set<String> EXPERIMENT_TYPES = new HashSet<>(Arrays.asList("homework", "simulator", "experiment"));
    private static final List<String> DONE_STATES = Arrays.asList("done", "submitted", "reviewed");

    private final MbTaskRepository taskRepository;
    private final MbTaskSubmissionRepository submissionRepository;
    private final MbQuizRecordRepository quizRecordRepository;
    private final MobileQuizConfigService quizConfigService;
    private final MobileQuizQuestionAllocator quizQuestionAllocator;
    private final MobileParentAccessService parentAccessService;
    private final MobileNotificationService notificationService;
    private final SysUserRepository sysUserRepository;
    private final ExpSimulatorRepository simulatorRepository;
    private final ExpMsgRepository expMsgRepository;
    private final MobileExpHomeworkRepository homeworkRepository;
    private final MobileStudentWorkService studentWorkService;

    public MobileTaskService(MbTaskRepository taskRepository,
                             MbTaskSubmissionRepository submissionRepository,
                             MbQuizRecordRepository quizRecordRepository,
                             MobileQuizConfigService quizConfigService,
                             MobileQuizQuestionAllocator quizQuestionAllocator,
                             MobileParentAccessService parentAccessService,
                             MobileNotificationService notificationService,
                             SysUserRepository sysUserRepository,
                             ExpSimulatorRepository simulatorRepository,
                             ExpMsgRepository expMsgRepository,
                             MobileExpHomeworkRepository homeworkRepository,
                             MobileStudentWorkService studentWorkService) {
        this.taskRepository = taskRepository;
        this.submissionRepository = submissionRepository;
        this.quizRecordRepository = quizRecordRepository;
        this.quizConfigService = quizConfigService;
        this.quizQuestionAllocator = quizQuestionAllocator;
        this.parentAccessService = parentAccessService;
        this.notificationService = notificationService;
        this.sysUserRepository = sysUserRepository;
        this.simulatorRepository = simulatorRepository;
        this.expMsgRepository = expMsgRepository;
        this.homeworkRepository = homeworkRepository;
        this.studentWorkService = studentWorkService;
    }

    /* ════════════════════════════════════════
       列表
       ════════════════════════════════════════ */

    public PageResult<MobileTaskListItemDto> listItems(String userId, String childUserId, String category, String status, int page, int size) {
        String studentId;
        try { studentId = parentAccessService.resolveStudentScope(userId, childUserId); }
        catch (IllegalArgumentException e) { throw e; }
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

    public List<MobileTaskListItemDto> listMergedStudentItems(String userId, String childUserId, String status) {
        String studentId = parentAccessService.resolveStudentScope(userId, childUserId);
        List<MobileTaskListItemDto> items = new ArrayList<>();
        items.addAll(listExperimentItems(studentId, status));
        items.addAll(listRemixItems(studentId, status));
        items.addAll(listCreativeItems(studentId, status));
        if (!parentAccessService.isParentUser(userId)) {
            items.addAll(listQuizItems(studentId, status));
        }
        items.sort(Comparator.comparingLong(MobileTaskListItemDto::getSortTime).reversed());
        return items;
    }

    private List<MobileTaskListItemDto> listExperimentItems(String studentId, String status) {
        List<MobileTaskListItemDto> items = new ArrayList<>();

        // 从 exp_homework 读取新作业
        List<MobileExpHomework> hws = homeworkRepository.findByCreateTimeIsNotNullOrderByCreateTimeDesc();
        for (MobileExpHomework hw : hws) {
            if (!StringUtils.hasText(hw.getClassId())) continue;
            SysUser student = sysUserRepository.findById(studentId).orElse(null);
            if (student == null) continue;
            if (!hw.getClassId().equals(student.getUserOrgId())) continue;

            MobileTaskListItemDto item = new MobileTaskListItemDto();
            item.setId(hw.getHomeworkId());
            item.setCategory("experiment");
            item.setKind("task");
            item.setTitle(resolveExpName(hw.getTeacherExpId()));
            item.setDesc("作品 · " + (StringUtils.hasText(hw.getRequireDate()) ? "截止 " + hw.getRequireDate() : ""));
            item.setSubType("standard");
            item.setLink("/tasks/" + hw.getHomeworkId());
            item.setUploadLink("/upload?taskId=" + hw.getHomeworkId());
            item.setDeadline(hw.getRequireDate());

            // 检查是否已提交
            boolean submitted = studentWorkService.hasSubmittedHomework(hw.getHomeworkId(), studentId);
            item.setState(submitted ? "done" : "pending");
            item.setStateLabel(submitted ? "已提交" : "待完成");
            item.setBadgeClass(submitted ? "badge-success" : "badge-warning");

            if (hw.getCreateTime() != null) item.setSortTime(hw.getCreateTime().getTime());

            if (matchExperimentStatus(item, status)) items.add(item);
        }

        // 从 MbTask 读取旧作业（历史数据）
        String studentClassOrgId = resolveStudentClassOrgId(studentId);
        List<MbTask> tasks = new ArrayList<>(taskRepository.findByStatusOrderByCreateTimeDesc("y"));
        Set<String> seenTaskIds = tasks.stream().map(MbTask::getTaskId).collect(Collectors.toCollection(HashSet::new));
        for (MbTaskSubmission sub : submissionRepository.findByStudentUserIdOrderByUpdateTimeDesc(studentId)) {
            if (sub == null || !StringUtils.hasText(sub.getTaskId()) || seenTaskIds.contains(sub.getTaskId())) continue;
            taskRepository.findById(sub.getTaskId().trim()).ifPresent(task -> {
                if (isExperimentTaskType(task.getTaskType())) { tasks.add(task); seenTaskIds.add(task.getTaskId()); }
            });
        }
        for (MbTask task : tasks) {
            if (!isExperimentTaskType(task.getTaskType())) continue;
            if (!isExperimentTaskVisible(task, studentId, studentClassOrgId)) continue;
            Optional<MbTaskSubmission> sub = submissionRepository.findByTaskIdAndStudentUserId(task.getTaskId(), studentId);
            MobileTaskListItemDto item = toTaskListItem(task, sub.orElse(null));
            if (matchExperimentStatus(item, status)) items.add(item);
        }
        return items;
    }

    private boolean matchExperimentStatus(MobileTaskListItemDto item, String status) {
        if (!StringUtils.hasText(status)) return true;
        boolean done = isDoneState(item.getState()) || "cancelled".equalsIgnoreCase(safe(item.getState()));
        if ("done".equals(status)) return done;
        if ("pending".equals(status)) return !done;
        return true;
    }

    private boolean isExperimentTaskType(String taskType) {
        return StringUtils.hasText(taskType) && EXPERIMENT_TYPES.contains(taskType.trim().toLowerCase());
    }

    private String resolveStudentClassOrgId(String studentId) {
        if (!StringUtils.hasText(studentId)) return "";
        return sysUserRepository.findById(studentId.trim())
                .map(u -> u.getUserOrgId()).filter(StringUtils::hasText).map(String::trim).orElse("");
    }

    private boolean isExperimentTaskVisible(MbTask task, String studentId, String studentClassOrgId) {
        if (task == null) return false;
        if (!isTaskActive(task)) return submissionRepository.findByTaskIdAndStudentUserId(task.getTaskId(), studentId).isPresent();
        if (!StringUtils.hasText(task.getClassOrgId())) return true;
        if (!StringUtils.hasText(studentClassOrgId)) return true;
        if (task.getClassOrgId().trim().equals(studentClassOrgId)) return true;
        return submissionRepository.findByTaskIdAndStudentUserId(task.getTaskId(), studentId).isPresent();
    }

    private boolean isTaskActive(MbTask task) {
        return task != null && "y".equalsIgnoreCase(safe(task.getStatus()));
    }

    private List<MobileTaskListItemDto> listRemixItems(String studentId, String status) {
        List<MobileTaskListItemDto> items = new ArrayList<>();

        // 从 ExpMsg 读取新拍同款
        List<ExpMsg> remixMsgs = studentWorkService.loadUserMsgs(studentId, "remix");
        for (ExpMsg msg : remixMsgs) {
            MobileTaskListItemDto item = studentWorkService.buildStartCard("remix");
            item.setId(msg.getExpId());
            item.setTitle(msg.getExpName());
            item.setDesc("拍同款");
            boolean isDraft = "draft".equals(msg.getStatus());
            item.setState(isDraft ? "pending" : "done");
            item.setStateLabel(isDraft ? "待完成" : "已完成");
            item.setBadgeClass(isDraft ? "badge-warning" : "badge-success");
            if (!isDraft) item.setUploadLink(null);
            if (msg.getCreateTime() != null) item.setSortTime(msg.getCreateTime().getTime());
            if (matchStatus(item, status)) items.add(item);
        }

        // 从 MbTask 读取旧拍同款（历史数据）
        Set<String> studentTaskIds = submissionRepository.findByStudentUserIdOrderByUpdateTimeDesc(studentId)
                .stream().map(MbTaskSubmission::getTaskId).collect(Collectors.toCollection(HashSet::new));
        for (MbTask task : taskRepository.findByStatusOrderByCreateTimeDesc("y")) {
            if (!"remix".equals(task.getTaskType()) || !studentTaskIds.contains(task.getTaskId())) continue;
            Optional<MbTaskSubmission> sub = submissionRepository.findByTaskIdAndStudentUserId(task.getTaskId(), studentId);
            MobileTaskListItemDto item = toTaskListItem(task, sub.orElse(null));
            if (matchStatus(item, status)) items.add(item);
        }
        return items;
    }

    private List<MobileTaskListItemDto> listCreativeItems(String studentId, String status) {
        List<MobileTaskListItemDto> items = new ArrayList<>();

        // 从 ExpMsg 读取新创意实验
        List<ExpMsg> creativeMsgs = studentWorkService.loadUserMsgs(studentId, "creative");
        for (ExpMsg msg : creativeMsgs) {
            MobileTaskListItemDto item = studentWorkService.buildStartCard("creative");
            item.setId(msg.getExpId());
            item.setTitle(msg.getExpName());
            item.setDesc("创意实验");
            boolean isDraft = "draft".equals(msg.getStatus());
            item.setState(isDraft ? "pending" : "done");
            item.setStateLabel(isDraft ? "待完成" : "已完成");
            item.setBadgeClass(isDraft ? "badge-warning" : "badge-success");
            if (!isDraft) item.setUploadLink(null);
            if (msg.getCreateTime() != null) item.setSortTime(msg.getCreateTime().getTime());
            if (matchStatus(item, status)) items.add(item);
        }

        // 从 MbTask 读取旧创意实验
        Set<String> studentTaskIds = submissionRepository.findByStudentUserIdOrderByUpdateTimeDesc(studentId)
                .stream().map(MbTaskSubmission::getTaskId).collect(Collectors.toCollection(HashSet::new));
        for (MbTask task : taskRepository.findByStatusOrderByCreateTimeDesc("y")) {
            if (!"creative".equals(task.getTaskType()) || !studentTaskIds.contains(task.getTaskId())) continue;
            Optional<MbTaskSubmission> sub = submissionRepository.findByTaskIdAndStudentUserId(task.getTaskId(), studentId);
            MobileTaskListItemDto item = toTaskListItem(task, sub.orElse(null));
            if (matchStatus(item, status)) items.add(item);
        }
        if ("pending".equals(status) && items.isEmpty()) {
            items.add(studentWorkService.buildStartCard("creative"));
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
            if (!todayRecord.isPresent()) items.add(buildTodayQuizPending());
            return items;
        }
        if ("done".equals(status)) {
            if (todayRecord.isPresent()) items.add(buildTodayQuizDone(todayRecord.get()));
            for (MbQuizRecord record : quizRecordRepository.findByUserIdAndQuizDateGreaterThanEqualOrderByQuizDateDesc(studentId, fromDate)) {
                if (record.getQuizDate() != null && record.getQuizDate().equals(todaySql)) continue;
                items.add(toQuizHistoryItem(record));
            }
            return items;
        }
        if (!todayRecord.isPresent()) items.add(buildTodayQuizPending());
        else items.add(buildTodayQuizDone(todayRecord.get()));
        for (MbQuizRecord record : quizRecordRepository.findByUserIdAndQuizDateGreaterThanEqualOrderByQuizDateDesc(studentId, fromDate)) {
            if (record.getQuizDate() != null && record.getQuizDate().equals(todaySql)) continue;
            items.add(toQuizHistoryItem(record));
        }
        return items;
    }

    /* ════════════════════════════════════════
       详情
       ════════════════════════════════════════ */

    @Transactional(readOnly = true)
    public MobileTaskDto get(String userId, String childUserId, String taskId) {
        String studentId;
        try {
            studentId = parentAccessService.resolveStudentScope(userId, childUserId);
        } catch (IllegalArgumentException e) {
            throw e;
        }

        // 先尝试从 exp_homework 查找
        Optional<MobileExpHomework> hwOpt = homeworkRepository.findById(taskId);
        if (hwOpt.isPresent()) {
            MobileExpHomework hw = hwOpt.get();
            MobileTaskDto dto = new MobileTaskDto();
            dto.setId(hw.getHomeworkId());
            dto.setType("homework");
            dto.setTitle(resolveExpName(hw.getTeacherExpId()));
            dto.setTaskTypeLabel("老师发布");
            dto.setDeadline(hw.getRequireDate());
            dto.setTeacherHint("老师发布 · 实验任务");
            dto.setTeacherHintClass("tint-orange");
            dto.setVideoId(hw.getTeacherExpId());
            dto.setUploadQuery("taskId=" + hw.getHomeworkId());

            // 检查提交状态
            boolean submitted = studentWorkService.hasSubmittedHomework(taskId, studentId);
            if (submitted) {
                dto.setState("submitted");
                dto.setStateLabel("已提交");
                dto.setBadgeClass("badge-success");
            } else {
                dto.setState("pending");
                dto.setStateLabel("待完成");
            }
            return dto;
        }
        // 再尝试从 exp_msg 查找（拍同款/创意实验草稿）
        Optional<ExpMsg> msgOpt = expMsgRepository.findById(taskId);
        if (msgOpt.isPresent()) {
            return studentWorkService.toTaskDto(msgOpt.get());
        }
        // 最后从 MbTask 查找（历史数据）
        Optional<MbTask> taskOpt = taskRepository.findById(taskId);
        if (taskOpt.isPresent()) {
            MbTask task = taskOpt.get();
            Optional<MbTaskSubmission> sub = submissionRepository.findByTaskIdAndStudentUserId(taskId, studentId);
            MobileTaskDto dto = MobileEntityMapper.toTaskDto(task, sub.orElse(null));
            applyEffectiveTaskType(task, dto);
            return dto;
        }
        return null;
    }

    /* ════════════════════════════════════════
       创建任务（教师布置）
       委托 MobileStudentWorkService 写入 exp_homework
       ════════════════════════════════════════ */

    public MobileTaskDto createTask(String userId, CreateTaskRequest request) {
        if (request == null || !StringUtils.hasText(request.getExperimentTitle())) {
            throw new IllegalArgumentException("请选择关联实验");
        }
        String teacherId = MobileUserContext.resolveTeacherId(userId);

        AssignHomeworkRequest assignReq = new AssignHomeworkRequest();
        assignReq.setExperimentId(request.getExperimentId());
        assignReq.setClassId(request.getClassOrgId());
        assignReq.setRequireDate(request.getDeadline());

        studentWorkService.assignHomework(userId, assignReq);

        // 查找刚创建的作业返回 DTO
        List<MobileExpHomework> hws = homeworkRepository
                .findByTearcherUserIdAndCreateTimeIsNotNullOrderByCreateTimeDesc(teacherId);
        if (!hws.isEmpty()) {
            MobileExpHomework hw = hws.get(0);
            MobileTaskDto dto = new MobileTaskDto();
            dto.setId(hw.getHomeworkId());
            dto.setType("homework");
            dto.setTitle(request.getExperimentTitle().trim());
            dto.setState("pending");
            dto.setStateLabel("待完成");
            dto.setTaskTypeLabel("老师发布");
            dto.setDeadline(hw.getRequireDate());
            dto.setTeacherHint("老师发布 · 实验任务");
            dto.setTeacherHintClass("tint-orange");
            dto.setVideoId(hw.getTeacherExpId());
            return dto;
        }
        MobileTaskDto dto = new MobileTaskDto();
        dto.setId("");
        dto.setType("homework");
        dto.setTitle(request.getExperimentTitle().trim());
        dto.setState("pending");
        return dto;
    }

    /* ════════════════════════════════════════
       内部方法
       ════════════════════════════════════════ */

    private boolean isSimulatorTask(MbTask task) {
        if (task == null) return false;
        if ("simulator".equalsIgnoreCase(safe(task.getTaskType()))) return true;
        return StringUtils.hasText(task.getVideoId()) && simulatorRepository.existsById(task.getVideoId().trim());
    }

    private void applyEffectiveTaskType(MbTask task, MobileTaskDto dto) {
        if (dto == null || task == null) return;
        if (isSimulatorTask(task)) dto.setType("simulator");
    }

    private boolean isDoneState(String state) {
        return StringUtils.hasText(state) && DONE_STATES.contains(state.trim().toLowerCase());
    }

    private boolean matchStatus(MobileTaskListItemDto item, String status) {
        if (!StringUtils.hasText(status)) return true;
        boolean done = isDoneState(item.getState());
        if ("done".equals(status)) return done;
        if ("pending".equals(status)) return !done;
        return true;
    }

    private String resolveCategory(String category) {
        if (!StringUtils.hasText(category)) return "experiment";
        String value = category.trim();
        if ("homework".equals(value) || "all".equals(value)) return "experiment";
        return value;
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
        if (!quizConfigService.isEnabled()) return 0;
        int configured = quizConfigService.getQuestionsPerDay();
        int eligible = quizQuestionAllocator.countEligibleQuestions();
        return eligible <= 0 ? configured : Math.min(configured, eligible);
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
        long sortTime = record.getCreateTime() != null ? record.getCreateTime().getTime() : record.getQuizDate().getTime();
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
        long sortTime = record.getCreateTime() != null ? record.getCreateTime().getTime() : record.getQuizDate().getTime();
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
        if ("remix".equals(taskType)) item.setCategory("remix");
        else if ("creative".equals(taskType)) item.setCategory("creative");
        else { item.setCategory("experiment"); item.setSubType(isSimulatorTask(task) ? "simulator" : "standard"); }
        if (!"done".equals(item.getState())) {
            item.setUploadLink(buildUploadLink(task));
        }
        return item;
    }

    private String buildUploadLink(MbTask task) {
        if (task == null) return "/upload";
        if (StringUtils.hasText(task.getUploadQuery())) return "/upload?" + task.getUploadQuery().trim();
        String taskId = task.getTaskId();
        if (!StringUtils.hasText(taskId)) return "/upload";
        String taskType = task.getTaskType();
        if ("remix".equals(taskType)) return "/upload?type=remix&taskId=" + taskId.trim();
        if ("creative".equals(taskType)) return "/upload?type=creative&taskId=" + taskId.trim();
        return "/upload?taskId=" + taskId.trim();
    }

    private long resolveSortTime(MbTask task, MbTaskSubmission submission) {
        if (submission != null && submission.getUpdateTime() != null) return submission.getUpdateTime().getTime();
        if (submission != null && submission.getCreateTime() != null) return submission.getCreateTime().getTime();
        if (task.getCreateTime() != null) return task.getCreateTime().getTime();
        return 0L;
    }

    private String resolveExpName(String expId) {
        if (!StringUtils.hasText(expId)) return "实验任务";
        return expMsgRepository.findById(expId).map(ExpMsg::getExpName).filter(StringUtils::hasText).orElse("实验任务");
    }

    private String safe(String value) {
        return value != null ? value.trim() : "";
    }

    private <T> PageResult<T> paginate(List<T> all, int page, int size) {
        int safeSize = Math.max(size, 1);
        int from = Math.max(page - 1, 0) * safeSize;
        if (from >= all.size()) return new PageResult<>(all.size(), new ArrayList<>());
        int to = Math.min(from + safeSize, all.size());
        return new PageResult<>(all.size(), all.subList(from, to));
    }
}
