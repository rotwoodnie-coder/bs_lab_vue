package com.xuanyue.exp.mobile.service;

import com.xuanyue.exp.common.PageResult;
import com.xuanyue.exp.mobile.dto.*;
import com.xuanyue.exp.mobile.entity.MbTask;
import com.xuanyue.exp.mobile.entity.MbTaskSubmission;
import com.xuanyue.exp.mobile.entity.MbWork;
import com.xuanyue.exp.mobile.repository.MbTaskRepository;
import com.xuanyue.exp.mobile.repository.MbTaskSubmissionRepository;
import com.xuanyue.exp.mobile.repository.MbWorkRepository;
import com.xuanyue.exp.mobile.support.MobileUserContext;
import com.xuanyue.exp.system.entity.SysOrg;
import com.xuanyue.exp.system.entity.SysUser;
import com.xuanyue.exp.system.repository.SysOrgRepository;
import com.xuanyue.exp.system.repository.SysUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MobileTeacherService {

    private static final SimpleDateFormat TIME_FMT = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private static final String STUDENT_ROLE = "Student";
    private static final String CLASS_ORG_TYPE = "Org_School_Class";
    private static final List<String> DONE_STATES = Arrays.asList("done", "submitted", "reviewed");

    private final MbWorkRepository workRepository;
    private final MbTaskRepository taskRepository;
    private final MbTaskSubmissionRepository submissionRepository;
    private final SysUserRepository sysUserRepository;
    private final SysOrgRepository sysOrgRepository;
    private final MobileWorkService workService;
    private final MobileHomeService homeService;
    private final MobileNotificationService notificationService;
    private final MobileTeacherParentBindService parentBindService;

    public MobileTeacherService(MbWorkRepository workRepository,
                                MbTaskRepository taskRepository,
                                MbTaskSubmissionRepository submissionRepository,
                                SysUserRepository sysUserRepository,
                                SysOrgRepository sysOrgRepository,
                                MobileWorkService workService,
                                MobileHomeService homeService,
                                MobileNotificationService notificationService,
                                MobileTeacherParentBindService parentBindService) {
        this.workRepository = workRepository;
        this.taskRepository = taskRepository;
        this.submissionRepository = submissionRepository;
        this.sysUserRepository = sysUserRepository;
        this.sysOrgRepository = sysOrgRepository;
        this.workService = workService;
        this.homeService = homeService;
        this.notificationService = notificationService;
        this.parentBindService = parentBindService;
    }

    @Transactional(readOnly = true)
    public TeacherDashboardDto getDashboard(String userId) {
        String teacherId = MobileUserContext.resolveTeacherId(userId);
        TeacherDashboardDto dto = new TeacherDashboardDto();

        SysUser teacher = sysUserRepository.findById(teacherId).orElse(null);
        if (teacher != null) {
            dto.setTeacherName(displayName(teacher));
            if (StringUtils.hasText(teacher.getUserOrgId())) {
                sysOrgRepository.findById(teacher.getUserOrgId())
                        .map(SysOrg::getOrgName)
                        .ifPresent(dto::setClassLabel);
            }
        }

        List<MbTask> teacherTasks = taskRepository.findByTeacherUserIdAndStatusOrderByCreateTimeDesc(teacherId, "y");
        dto.setAssigned(teacherTasks.size());

        Set<String> teacherClassOrgIds = resolveTeacherClassOrgIds(teacher, teacherTasks);
        Map<String, MbTask> taskById = teacherTasks.stream()
                .collect(Collectors.toMap(MbTask::getTaskId, t -> t, (a, b) -> a));

        List<MbWork> pendingWorks = workRepository.findByStatusOrderByCreateTimeDesc("y").stream()
                .filter(w -> "pending".equalsIgnoreCase(safe(w.getReviewStatus())))
                .filter(w -> "homework".equalsIgnoreCase(safe(w.getWorkType())) || !StringUtils.hasText(w.getWorkType()))
                .filter(w -> isWorkForTeacher(teacherId, w, teacherClassOrgIds, taskById))
                .collect(Collectors.toList());
        dto.setPendingReview(pendingWorks.size());

        if (!teacherTasks.isEmpty()) {
            MbTask latest = teacherTasks.get(0);
            dto.setLatestTaskId(latest.getTaskId());
            dto.setLatestTaskTitle(latest.getTitle());
        }

        int submitted = 0;
        int totalSlots = 0;
        String classOrgId = resolvePrimaryClassOrgId(teacher, teacherTasks);
        List<SysUser> classStudents = listStudentsInClass(classOrgId);
        dto.setStudents(classStudents.size());

        for (MbTask task : teacherTasks) {
            List<MbTaskSubmission> subs = submissionRepository.findByTaskId(task.getTaskId());
            for (MbTaskSubmission sub : subs) {
                totalSlots++;
                if (isDoneState(sub.getState())) {
                    submitted++;
                }
            }
        }
        if (totalSlots == 0 && !classStudents.isEmpty() && !teacherTasks.isEmpty()) {
            totalSlots = classStudents.size() * teacherTasks.size();
            for (MbTask task : teacherTasks) {
                submitted += (int) submissionRepository.findByTaskId(task.getTaskId()).stream()
                        .filter(s -> isDoneState(s.getState())).count();
            }
        }
        dto.setSubmitted(submitted);
        dto.setSubmitRate(totalSlots > 0 ? (submitted * 100 / totalSlots) : 0);
        dto.setUnsubmitted(Math.max(0, totalSlots - submitted));
        fillWeeklyTrend(dto, teacherTasks);
        dto.setPendingParentBinds(parentBindService.countPending(userId));
        return dto;
    }

    @Transactional(readOnly = true)
    public PageResult<TeacherReviewQueueItemDto> listReviewQueue(String userId, int page, int size) {
        String teacherId = MobileUserContext.resolveTeacherId(userId);
        SysUser teacher = sysUserRepository.findById(teacherId).orElse(null);
        List<MbTask> teacherTasks = taskRepository.findByTeacherUserIdAndStatusOrderByCreateTimeDesc(teacherId, "y");
        Set<String> teacherClassOrgIds = resolveTeacherClassOrgIds(teacher, teacherTasks);
        Map<String, MbTask> taskById = teacherTasks.stream()
                .collect(Collectors.toMap(MbTask::getTaskId, t -> t, (a, b) -> a));

        List<TeacherReviewQueueItemDto> items = workRepository.findByStatusOrderByCreateTimeDesc("y").stream()
                .filter(w -> "pending".equalsIgnoreCase(safe(w.getReviewStatus())))
                .filter(w -> "homework".equalsIgnoreCase(safe(w.getWorkType())) || !StringUtils.hasText(w.getWorkType()))
                .filter(w -> isWorkForTeacher(teacherId, w, teacherClassOrgIds, taskById))
                .map(this::toReviewItem)
                .collect(Collectors.toList());
        return paginate(items, page, size);
    }

    @Transactional(readOnly = true)
    public List<TeacherTaskSummaryDto> listTeacherTasks(String userId) {
        String teacherId = MobileUserContext.resolveTeacherId(userId);
        return taskRepository.findByTeacherUserIdAndStatusOrderByCreateTimeDesc(teacherId, "y").stream()
                .map(task -> {
                    TeacherTaskSummaryDto item = new TeacherTaskSummaryDto();
                    item.setTaskId(task.getTaskId());
                    item.setTitle(task.getTitle());
                    item.setClassName(resolveOrgName(task.getClassOrgId()));
                    return item;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public TeacherRemindResultDto remindUnsubmitted(String userId, String taskId) {
        if (!StringUtils.hasText(taskId)) {
            throw new IllegalArgumentException("请选择任务");
        }
        MbTask task = taskRepository.findById(taskId.trim())
                .orElseThrow(() -> new IllegalArgumentException("任务不存在"));
        TeacherTaskBoardDto board = getTaskBoard(userId, taskId);
        String teacherId = MobileUserContext.resolveTeacherId(userId);

        List<String> unsubmittedIds = board.getStudents().stream()
                .filter(row -> row != null && !row.isDone())
                .map(TeacherTaskBoardDto.StudentRow::getUserId)
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());

        if (unsubmittedIds.isEmpty()) {
            return new TeacherRemindResultDto(0, "当前无未提交学生");
        }

        int notified = notificationService.sendTaskReminders(teacherId, task, unsubmittedIds);
        if (notified <= 0) {
            return new TeacherRemindResultDto(0, "未提交学生均已关闭任务通知，未发送提醒");
        }
        return new TeacherRemindResultDto(notified, "已向 " + notified + " 名未提交学生发送提醒");
    }

    @Transactional
    public void submitReview(String teacherUserId, String workId, TeacherReviewRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("请求不能为空");
        }
        workService.reviewWork(teacherUserId, workId, request.getRating(), request.getComment(), request.getFeatured());
    }

    @Transactional(readOnly = true)
    public TeacherTaskBoardDto getTaskBoard(String userId, String taskId) {
        if (!StringUtils.hasText(taskId)) {
            throw new IllegalArgumentException("请选择任务");
        }
        MbTask task = taskRepository.findById(taskId.trim())
                .orElseThrow(() -> new IllegalArgumentException("任务不存在"));
        String teacherId = MobileUserContext.resolveTeacherId(userId);
        if (!teacherId.equals(safe(task.getTeacherUserId()))) {
            throw new IllegalArgumentException("无权查看该任务看板");
        }

        String classOrgId = task.getClassOrgId();
        String className = resolveOrgName(classOrgId);
        List<SysUser> students = listStudentsInClass(classOrgId);
        Map<String, MbTaskSubmission> subByStudent = submissionRepository.findByTaskId(task.getTaskId()).stream()
                .collect(Collectors.toMap(MbTaskSubmission::getStudentUserId, s -> s, (a, b) -> a));
        Map<String, String> workByStudent = workRepository.findByTaskId(task.getTaskId()).stream()
                .filter(w -> StringUtils.hasText(w.getStudentUserId()))
                .collect(Collectors.toMap(MbWork::getStudentUserId, MbWork::getWorkId, (a, b) -> a));

        List<TeacherTaskBoardDto.StudentRow> rows = new ArrayList<>();
        int submitted = 0;
        for (SysUser student : students) {
            TeacherTaskBoardDto.StudentRow row = new TeacherTaskBoardDto.StudentRow();
            row.setUserId(student.getUserId());
            row.setName(displayName(student));
            row.setInitial(initialOf(row.getName()));
            MbTaskSubmission sub = subByStudent.get(student.getUserId());
            boolean done = sub != null && isDoneState(sub.getState());
            row.setDone(done);
            if (done) {
                submitted++;
                row.setWorkId(workByStudent.get(student.getUserId()));
            }
            rows.add(row);
        }

        TeacherTaskBoardDto dto = new TeacherTaskBoardDto();
        dto.setTaskId(task.getTaskId());
        dto.setTaskTitle(task.getTitle());
        dto.setClassName(StringUtils.hasText(className) ? className : "未指定班级");
        dto.setSubmitted(submitted);
        dto.setUnsubmitted(Math.max(0, students.size() - submitted));
        dto.setSubmitRate(students.isEmpty() ? 0 : (submitted * 100 / students.size()));
        dto.setStudents(rows);
        return dto;
    }

    @Transactional(readOnly = true)
    public TeacherAssignOptionsDto getAssignOptions(String userId) {
        String teacherId = MobileUserContext.resolveTeacherId(userId);
        SysUser teacher = sysUserRepository.findById(teacherId).orElse(null);

        TeacherAssignOptionsDto dto = new TeacherAssignOptionsDto();
        dto.setClasses(listClassesForTeacher(teacher));
        dto.setExperiments(listExperimentOptions());
        return dto;
    }

    private List<TeacherAssignOptionsDto.OptionItem> listClassesForTeacher(SysUser teacher) {
        if (teacher == null) {
            return Collections.emptyList();
        }
        List<SysOrg> all = sysOrgRepository.findAll().stream()
                .filter(o -> "y".equalsIgnoreCase(safe(o.getStatus())))
                .collect(Collectors.toList());
        Map<String, List<SysOrg>> byParent = new HashMap<>();
        for (SysOrg org : all) {
            String parent = org.getParentOrgId() != null ? org.getParentOrgId() : "";
            byParent.computeIfAbsent(parent, k -> new ArrayList<>()).add(org);
        }

        List<TeacherAssignOptionsDto.OptionItem> result = new ArrayList<>();
        String rootId = teacher.getRootOrgId();
        if (StringUtils.hasText(rootId)) {
            collectClassOptions(rootId, byParent, result, 0);
        }
        if (result.isEmpty() && StringUtils.hasText(teacher.getUserOrgId())) {
            sysOrgRepository.findById(teacher.getUserOrgId()).ifPresent(org -> {
                if (CLASS_ORG_TYPE.equals(org.getOrgTypeId())) {
                    int count = listStudentsInClass(org.getOrgId()).size();
                    result.add(new TeacherAssignOptionsDto.OptionItem(org.getOrgId(), org.getOrgName(), count));
                }
            });
        }
        result.sort(Comparator.comparing(TeacherAssignOptionsDto.OptionItem::getLabel, Comparator.nullsLast(String::compareTo)));
        return result;
    }

    private void collectClassOptions(String orgId, Map<String, List<SysOrg>> byParent,
                                     List<TeacherAssignOptionsDto.OptionItem> result, int depth) {
        if (depth > 8 || !StringUtils.hasText(orgId)) {
            return;
        }
        List<SysOrg> children = byParent.getOrDefault(orgId, Collections.emptyList());
        for (SysOrg child : children) {
            if (CLASS_ORG_TYPE.equals(child.getOrgTypeId())) {
                int count = listStudentsInClass(child.getOrgId()).size();
                result.add(new TeacherAssignOptionsDto.OptionItem(child.getOrgId(), child.getOrgName(), count));
            }
            collectClassOptions(child.getOrgId(), byParent, result, depth + 1);
        }
    }

    private List<TeacherAssignOptionsDto.OptionItem> listExperimentOptions() {
        try {
            PageResult<HomeFeedItem> feed = homeService.getFeed(null, 1, 50);
            if (feed == null || feed.getRecords() == null) {
                return Collections.emptyList();
            }
            List<TeacherAssignOptionsDto.OptionItem> items = new ArrayList<>();
            Set<String> seen = new HashSet<>();
            for (HomeFeedItem item : feed.getRecords()) {
                if (item == null || !StringUtils.hasText(item.getTitle())) {
                    continue;
                }
                String id = StringUtils.hasText(item.getId()) ? item.getId() : item.getTitle();
                if (seen.add(id)) {
                    items.add(new TeacherAssignOptionsDto.OptionItem(id, item.getTitle()));
                }
            }
            return items;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private TeacherReviewQueueItemDto toReviewItem(MbWork work) {
        TeacherReviewQueueItemDto item = new TeacherReviewQueueItemDto();
        item.setId(work.getWorkId());
        String studentName = resolveStudentName(work.getStudentUserId());
        item.setStudent(studentName);
        item.setStudentInitial(initialOf(studentName));
        item.setTitle(work.getTitle());
        item.setTime(work.getCreateTime() != null ? TIME_FMT.format(work.getCreateTime()) : "");
        item.setAvatarClass("avatar-grad-warm");
        return item;
    }

    private String resolveStudentName(String userId) {
        if (!StringUtils.hasText(userId)) {
            return "同学";
        }
        return sysUserRepository.findById(userId)
                .map(this::displayName)
                .orElse("同学");
    }

    private String displayName(SysUser user) {
        if (StringUtils.hasText(user.getUserNickName())) {
            return user.getUserNickName();
        }
        return user.getUserName() != null ? user.getUserName() : "用户";
    }

    private String initialOf(String name) {
        return StringUtils.hasText(name) ? name.substring(0, 1) : "同";
    }

    private List<SysUser> listStudentsInClass(String classOrgId) {
        if (!StringUtils.hasText(classOrgId)) {
            return Collections.emptyList();
        }
        List<SysUser> result = new ArrayList<>();
        for (SysUser user : sysUserRepository.findAll()) {
            if (!STUDENT_ROLE.equals(user.getUserRoleId())) {
                continue;
            }
            if (!"y".equalsIgnoreCase(safe(user.getStatus()))) {
                continue;
            }
            if (classOrgId.equals(user.getUserOrgId())) {
                result.add(user);
            }
        }
        result.sort(Comparator.comparing(this::displayName, Comparator.nullsLast(String::compareTo)));
        return result;
    }

    private String resolvePrimaryClassOrgId(SysUser teacher, List<MbTask> tasks) {
        for (MbTask task : tasks) {
            if (StringUtils.hasText(task.getClassOrgId())) {
                Optional<SysOrg> org = sysOrgRepository.findById(task.getClassOrgId());
                if (org.isPresent() && CLASS_ORG_TYPE.equals(org.get().getOrgTypeId())) {
                    return task.getClassOrgId();
                }
            }
        }
        if (teacher != null && StringUtils.hasText(teacher.getUserOrgId())) {
            return teacher.getUserOrgId();
        }
        return null;
    }

    private String resolveOrgName(String orgId) {
        if (!StringUtils.hasText(orgId)) {
            return null;
        }
        return sysOrgRepository.findById(orgId).map(SysOrg::getOrgName).orElse(orgId);
    }

    private boolean isDoneState(String state) {
        return DONE_STATES.contains(safe(state).toLowerCase());
    }

    private String safe(String value) {
        return value != null ? value.trim() : "";
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

    private Set<String> resolveTeacherClassOrgIds(SysUser teacher, List<MbTask> teacherTasks) {
        Set<String> ids = new LinkedHashSet<>();
        if (teacher != null && StringUtils.hasText(teacher.getUserOrgId())) {
            ids.add(teacher.getUserOrgId().trim());
        }
        if (teacherTasks != null) {
            for (MbTask task : teacherTasks) {
                if (StringUtils.hasText(task.getClassOrgId())) {
                    ids.add(task.getClassOrgId().trim());
                }
            }
        }
        return ids;
    }

    private boolean isWorkForTeacher(String teacherId, MbWork work, Set<String> classOrgIds,
                                     Map<String, MbTask> taskById) {
        if (work == null) {
            return false;
        }
        if (StringUtils.hasText(work.getTaskId()) && taskById.containsKey(work.getTaskId())) {
            return true;
        }
        if (StringUtils.hasText(work.getStudentUserId())) {
            return sysUserRepository.findById(work.getStudentUserId())
                    .map(u -> classOrgIds.contains(safe(u.getUserOrgId())))
                    .orElse(false);
        }
        return false;
    }

    private void fillWeeklyTrend(TeacherDashboardDto dto, List<MbTask> teacherTasks) {
        int[] buckets = new int[5];
        Set<String> taskIds = teacherTasks.stream().map(MbTask::getTaskId).collect(Collectors.toSet());
        Calendar weekStart = Calendar.getInstance();
        weekStart.set(Calendar.HOUR_OF_DAY, 0);
        weekStart.set(Calendar.MINUTE, 0);
        weekStart.set(Calendar.SECOND, 0);
        weekStart.set(Calendar.MILLISECOND, 0);
        int dow = weekStart.get(Calendar.DAY_OF_WEEK);
        int offset = (dow + 5) % 7;
        weekStart.add(Calendar.DAY_OF_MONTH, -offset);

        Calendar prevWeekStart = (Calendar) weekStart.clone();
        prevWeekStart.add(Calendar.DAY_OF_MONTH, -7);
        int prevTotal = 0;

        for (MbTaskSubmission sub : submissionRepository.findAll()) {
            if (!taskIds.contains(sub.getTaskId()) || !isDoneState(sub.getState())) {
                continue;
            }
            Date time = sub.getSubmitTime() != null ? sub.getSubmitTime()
                    : (sub.getUpdateTime() != null ? sub.getUpdateTime() : sub.getCreateTime());
            if (time == null) {
                continue;
            }
            Calendar cal = Calendar.getInstance();
            cal.setTime(time);
            if (!cal.before(weekStart) && cal.before(nextWeek(weekStart))) {
                int idx = weekdayIndex(cal);
                if (idx >= 0 && idx < 5) {
                    buckets[idx]++;
                }
            }
            if (!cal.before(prevWeekStart) && cal.before(weekStart)) {
                prevTotal++;
            }
        }

        List<Integer> trend = new ArrayList<>();
        int total = 0;
        for (int count : buckets) {
            trend.add(count);
            total += count;
        }
        dto.setWeeklyTrend(trend);
        dto.setTrendTotal(total);
        if (prevTotal > 0) {
            dto.setTrendDeltaPercent((total - prevTotal) * 100 / prevTotal);
        } else if (total > 0) {
            dto.setTrendDeltaPercent(100);
        } else {
            dto.setTrendDeltaPercent(0);
        }
    }

    private Calendar nextWeek(Calendar weekStart) {
        Calendar next = (Calendar) weekStart.clone();
        next.add(Calendar.DAY_OF_MONTH, 7);
        return next;
    }

    private int weekdayIndex(Calendar cal) {
        switch (cal.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.MONDAY: return 0;
            case Calendar.TUESDAY: return 1;
            case Calendar.WEDNESDAY: return 2;
            case Calendar.THURSDAY: return 3;
            case Calendar.FRIDAY: return 4;
            default: return -1;
        }
    }
}
