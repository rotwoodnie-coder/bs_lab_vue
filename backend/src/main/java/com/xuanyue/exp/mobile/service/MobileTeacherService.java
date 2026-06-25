package com.xuanyue.exp.mobile.service;

import com.xuanyue.exp.common.PageResult;
import com.xuanyue.exp.common.storage.minio.MinioStorageService;
import com.xuanyue.exp.exp.entity.ExpMsg;
import com.xuanyue.exp.exp.repository.ExpMsgRepository;
import com.xuanyue.exp.mobile.dto.*;
import com.xuanyue.exp.mobile.entity.MobileExpHomework;
import com.xuanyue.exp.mobile.entity.MobileExpHomeworkStudent;
import com.xuanyue.exp.mobile.repository.MobileExpHomeworkRepository;
import com.xuanyue.exp.mobile.repository.MobileExpHomeworkStudentRepository;
import com.xuanyue.exp.mobile.support.MobileAvatarSupport;
import com.xuanyue.exp.mobile.support.MobileTeacherClassScope;
import com.xuanyue.exp.mobile.support.MobileUserContext;
import com.xuanyue.exp.mobile.support.MobileWorkAuditStatus;
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

/**
 * MobileTeacherService — 重构版
 *
 * 数据源改用 exp_homework / exp_homework_student / exp_msg，
 * 外部方法签名与返回 DTO 不变。
 */
@Service
public class MobileTeacherService {

    private static final SimpleDateFormat TIME_FMT = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private static final String STUDENT_ROLE = "Student";
    private static final String CLASS_ORG_TYPE = "Org_School_Class";

    private final MobileExpHomeworkRepository homeworkRepository;
    private final MobileExpHomeworkStudentRepository homeworkStudentRepository;
    private final ExpMsgRepository expMsgRepository;
    private final SysUserRepository sysUserRepository;
    private final SysOrgRepository sysOrgRepository;
    private final MobileWorkService workService;
    private final MobileNotificationService notificationService;
    private final MobileTeacherParentBindService parentBindService;
    private final MinioStorageService minioStorageService;

    public MobileTeacherService(MobileExpHomeworkRepository homeworkRepository,
                                MobileExpHomeworkStudentRepository homeworkStudentRepository,
                                ExpMsgRepository expMsgRepository,
                                SysUserRepository sysUserRepository,
                                SysOrgRepository sysOrgRepository,
                                MobileWorkService workService,
                                MobileNotificationService notificationService,
                                MobileTeacherParentBindService parentBindService,
                                MinioStorageService minioStorageService) {
        this.homeworkRepository = homeworkRepository;
        this.homeworkStudentRepository = homeworkStudentRepository;
        this.expMsgRepository = expMsgRepository;
        this.sysUserRepository = sysUserRepository;
        this.sysOrgRepository = sysOrgRepository;
        this.workService = workService;
        this.notificationService = notificationService;
        this.parentBindService = parentBindService;
        this.minioStorageService = minioStorageService;
    }

    @Transactional(readOnly = true)
    public int countPendingReviews(String userId) {
        String teacherId = MobileUserContext.resolveTeacherId(userId);
        List<MobileExpHomework> hws = homeworkRepository
                .findByTearcherUserIdAndCreateTimeIsNotNullOrderByCreateTimeDesc(teacherId);
        Set<String> hwIds = hws.stream().map(MobileExpHomework::getHomeworkId).collect(Collectors.toSet());
        return (int) homeworkStudentRepository.findAll().stream()
                .filter(hs -> hwIds.contains(hs.getHomeworkId()))
                .filter(hs -> hs.getMarkTime() == null)
                .filter(hs -> StringUtils.hasText(hs.getStudentSubmitDate()))
                .count();
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

        List<MobileExpHomework> hws = homeworkRepository
                .findByTearcherUserIdAndCreateTimeIsNotNullOrderByCreateTimeDesc(teacherId);
        dto.setAssigned(hws.size());

        Set<String> hwIds = hws.stream().map(MobileExpHomework::getHomeworkId).collect(Collectors.toSet());
        List<MobileExpHomeworkStudent> allRecords = homeworkStudentRepository.findAll();

        long submitted = allRecords.stream()
                .filter(hs -> hwIds.contains(hs.getHomeworkId()))
                .filter(hs -> StringUtils.hasText(hs.getStudentSubmitDate()))
                .count();

        long pending = allRecords.stream()
                .filter(hs -> hwIds.contains(hs.getHomeworkId()))
                .filter(hs -> hs.getMarkTime() == null && StringUtils.hasText(hs.getStudentSubmitDate()))
                .count();

        dto.setPendingReview((int) pending);
        dto.setSubmitted((int) submitted);

        MobileExpHomework firstHw = hws.stream()
                .filter(hw -> StringUtils.hasText(hw.getClassId()))
                .findFirst().orElse(null);
        String classOrgId = firstHw != null ? firstHw.getClassId() : null;
        if (classOrgId == null && teacher != null) classOrgId = teacher.getUserOrgId();
        List<SysUser> classStudents = listStudentsInClass(classOrgId);
        dto.setStudents(classStudents.size());

        int totalSlots = Math.max(classStudents.size(), 1) * Math.max(hws.size(), 1);
        dto.setSubmitRate((int) (submitted * 100 / totalSlots));
        dto.setUnsubmitted(Math.max(0, totalSlots - (int) submitted));

        if (!hws.isEmpty()) {
            dto.setLatestTaskId(hws.get(0).getHomeworkId());
            dto.setLatestTaskTitle(resolveExpName(hws.get(0).getTeacherExpId()));
        }

        fillWeeklyTrend(dto, hws);
        dto.setPendingParentBinds(parentBindService.countPending(userId));
        return dto;
    }

    @Transactional(readOnly = true)
    public PageResult<TeacherReviewQueueItemDto> listReviewQueue(String userId, int page, int size) {
        String teacherId = MobileUserContext.resolveTeacherId(userId);
        List<MobileExpHomework> hws = homeworkRepository
                .findByTearcherUserIdAndCreateTimeIsNotNullOrderByCreateTimeDesc(teacherId);
        Set<String> hwIds = hws.stream().map(MobileExpHomework::getHomeworkId).collect(Collectors.toSet());

        List<MobileExpHomeworkStudent> pendingRecords = homeworkStudentRepository.findAll().stream()
                .filter(hs -> hwIds.contains(hs.getHomeworkId()))
                .filter(hs -> hs.getMarkTime() == null)
                .filter(hs -> StringUtils.hasText(hs.getStudentSubmitDate()))
                .sorted((a, b) -> {
                    // 给 StudentSubmitDate 不为空的优先排前
                    return 0;
                })
                .collect(Collectors.toList());

        List<TeacherReviewQueueItemDto> items = pendingRecords.stream()
                .map(this::toReviewItem)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 追加：无作业关联的创意/拍同款作品（status=t），按教师可管理班级范围筛选
        Set<String> existingIds = items.stream()
                .map(TeacherReviewQueueItemDto::getId)
                .collect(Collectors.toSet());
        for (ExpMsg msg : findPendingCreativeWorksInScope(teacherId)) {
            if (existingIds.contains(msg.getExpId())) continue;
            TeacherReviewQueueItemDto item = toReviewItemFromMsg(msg);
            if (item != null) {
                items.add(item);
                existingIds.add(msg.getExpId());
            }
        }
        return paginate(items, page, size);
    }

    /** 教师可审核范围内、待审核(t)且无作业关联的创意/拍同款作品 */
    private List<ExpMsg> findPendingCreativeWorksInScope(String teacherId) {
        SysUser teacher = StringUtils.hasText(teacherId)
                ? sysUserRepository.findById(teacherId.trim()).orElse(null) : null;
        if (teacher == null) return Collections.emptyList();
        Set<String> classOrgIds = MobileTeacherClassScope.resolveClassOrgIds(teacher, null, sysOrgRepository);
        if (classOrgIds.isEmpty()) return Collections.emptyList();

        Map<String, String> studentClass = sysUserRepository.findAll().stream()
                .filter(u -> STUDENT_ROLE.equals(u.getUserRoleId()))
                .filter(u -> u.getUserOrgId() != null && classOrgIds.contains(u.getUserOrgId().trim()))
                .collect(Collectors.toMap(SysUser::getUserId, u -> u.getUserOrgId().trim(), (a, b) -> a));

        return expMsgRepository.findAll().stream()
                .filter(m -> "student".equals(m.getExpType()))
                .filter(m -> MobileWorkAuditStatus.isPending(m.getStatus()))
                .filter(m -> "tk".equals(m.getExpTaskType()) || "self".equals(m.getExpTaskType()))
                .filter(m -> m.getCreateUserId() != null && studentClass.containsKey(m.getCreateUserId().trim()))
                .sorted((a, b) -> cmpTimeDesc(a.getCreateTime(), b.getCreateTime()))
                .collect(Collectors.toList());
    }

    private int cmpTimeDesc(Date a, Date b) {
        if (a == null && b == null) return 0;
        if (a == null) return 1;
        if (b == null) return -1;
        return b.compareTo(a);
    }

    private TeacherReviewQueueItemDto toReviewItemFromMsg(ExpMsg msg) {
        if (msg == null || !StringUtils.hasText(msg.getExpId())) return null;
        TeacherReviewQueueItemDto item = new TeacherReviewQueueItemDto();
        item.setId(msg.getExpId());
        String studentName = resolveStudentName(msg.getCreateUserId());
        item.setStudent(studentName);
        item.setStudentInitial(initialOf(studentName));
        String avatar = resolveStudentAvatar(msg.getCreateUserId());
        if (StringUtils.hasText(avatar)) item.setStudentAvatarUrl(avatar);
        item.setTitle(msg.getExpName());
        item.setTime(msg.getCreateTime() != null ? TIME_FMT.format(msg.getCreateTime()) : "");
        item.setAvatarClass("avatar-grad-warm");
        if ("tk".equals(msg.getExpTaskType())) {
            item.setWorkType("remix");
            item.setWorkTypeLabel("拍同款");
        } else {
            item.setWorkType("creative");
            item.setWorkTypeLabel("创意实验");
        }
        return item;
    }

    @Transactional(readOnly = true)
    public List<TeacherTaskSummaryDto> listTeacherTasks(String userId) {
        return listTeacherTasks(userId, "active");
    }

    @Transactional(readOnly = true)
    public List<TeacherTaskSummaryDto> listTeacherTasks(String userId, String scope) {
        String teacherId = MobileUserContext.resolveTeacherId(userId);
        List<MobileExpHomework> hws = homeworkRepository
                .findByTearcherUserIdAndCreateTimeIsNotNullOrderByCreateTimeDesc(teacherId);
        return hws.stream()
                .map(this::buildTaskSummary)
                .collect(Collectors.toList());
    }

    @Transactional
    public TeacherRemindResultDto remindUnsubmitted(String userId, String homeworkId) {
        if (!StringUtils.hasText(homeworkId)) throw new IllegalArgumentException("请选择实验任务");
        MobileExpHomework hw = homeworkRepository.findById(homeworkId.trim())
                .orElseThrow(() -> new IllegalArgumentException("实验任务不存在"));

        TeacherTaskBoardDto board = getTaskBoard(userId, homeworkId);
        String teacherId = MobileUserContext.resolveTeacherId(userId);

        List<String> unsubmittedIds = board.getStudents().stream()
                .filter(row -> row != null && !row.isDone())
                .map(TeacherTaskBoardDto.StudentRow::getUserId)
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());

        if (unsubmittedIds.isEmpty()) return new TeacherRemindResultDto(0, "当前无未提交学生");

        int notified = notificationService.sendTaskReminders(teacherId, hw, unsubmittedIds);
        if (notified <= 0) return new TeacherRemindResultDto(0, "未提交学生均已关闭任务通知，未发送提醒");
        return new TeacherRemindResultDto(notified, "已向 " + notified + " 名未提交学生发送提醒");
    }

    @Transactional
    public TeacherTaskCancelResultDto cancelTask(String userId, String taskId, TeacherTaskCancelRequest request) {
        // exp_homework 表无 status 列，暂不支持取消
        throw new IllegalArgumentException("当前版本不支持取消实验任务");
    }

    @Transactional
    public void submitReview(String teacherUserId, String workId, TeacherReviewRequest request) {
        if (request == null) throw new IllegalArgumentException("请求不能为空");
        ExpMsg msg = expMsgRepository.findById(workId.trim())
                .orElseThrow(() -> new IllegalArgumentException("作品不存在"));

        String rating = request.getRating();
        String comment = request.getComment();

        // 统一走审核逻辑：权限校验 + 评分落 exp_homework_student + 审核结论落 exp_msg.status
        // （作业类有关联记录；创意/拍同款无关联记录也可审核）
        workService.reviewWork(teacherUserId, msg.getExpId(), rating, comment, null);

        notificationService.sendWorkReviewedToStudent(teacherUserId, msg, rating, comment);
    }

    @Transactional(readOnly = true)
    public TeacherTaskBoardDto getTaskBoard(String userId, String homeworkId) {
        if (!StringUtils.hasText(homeworkId)) throw new IllegalArgumentException("请选择实验任务");
        MobileExpHomework hw = homeworkRepository.findById(homeworkId.trim())
                .orElseThrow(() -> new IllegalArgumentException("实验任务不存在"));
        String teacherId = MobileUserContext.resolveTeacherId(userId);
        if (!teacherId.equals(safe(hw.getTearcherUserId()))) {
            throw new IllegalArgumentException("无权查看该实验任务看板");
        }

        String classOrgId = hw.getClassId();
        String className = resolveOrgName(classOrgId);
        List<SysUser> students = listStudentsInClass(classOrgId);
        List<MobileExpHomeworkStudent> records = homeworkStudentRepository.findByHomeworkId(hw.getHomeworkId());

        // 通过 student_exp_id → exp_msg → createUserId 建立学生映射
        Map<String, MobileExpHomeworkStudent> subByStudent = new HashMap<>();
        for (MobileExpHomeworkStudent hs : records) {
            if (!StringUtils.hasText(hs.getStudentExpId())) continue;
            expMsgRepository.findById(hs.getStudentExpId()).ifPresent(msg -> {
                String uid = msg.getCreateUserId();
                if (StringUtils.hasText(uid)) subByStudent.put(uid, hs);
            });
        }

        List<TeacherTaskBoardDto.StudentRow> rows = new ArrayList<>();
        int submittedCount = 0;
        int pendingReview = 0;
        for (SysUser student : students) {
            TeacherTaskBoardDto.StudentRow row = new TeacherTaskBoardDto.StudentRow();
            row.setUserId(student.getUserId());
            row.setName(displayName(student));
            row.setInitial(initialOf(row.getName()));
            row.setAvatarUrl(MobileAvatarSupport.resolveUserAvatarUrl(minioStorageService, student));

            MobileExpHomeworkStudent hs = subByStudent.get(student.getUserId());
            boolean done = hs != null && StringUtils.hasText(hs.getStudentSubmitDate());
            row.setDone(done);
            if (done) {
                submittedCount++;
                row.setWorkId(hs.getStudentExpId());
                if (hs.getMarkTime() != null) {
                    row.setReviewStatus("reviewed");
                    row.setReviewStatusLabel("已评价");
                    row.setGrade(hs.getMarkResult());
                } else {
                    row.setReviewStatus("pending");
                    row.setReviewStatusLabel("待评价");
                    pendingReview++;
                }
            } else {
                row.setReviewStatus("not_submitted");
                row.setReviewStatusLabel("未提交");
            }
            rows.add(row);
        }

        TeacherTaskBoardDto dto = new TeacherTaskBoardDto();
        dto.setTaskId(hw.getHomeworkId());
        dto.setTaskTitle(resolveExpName(hw.getTeacherExpId()));
        dto.setClassName(StringUtils.hasText(className) ? className : "未指定班级");
        dto.setSubmitted(submittedCount);
        dto.setUnsubmitted(Math.max(0, students.size() - submittedCount));
        dto.setSubmitRate(students.isEmpty() ? 0 : (submittedCount * 100 / students.size()));
        dto.setPendingReview(pendingReview);
        dto.setStudents(rows);
        dto.setCancelled(false);
        return dto;
    }

    @Transactional(readOnly = true)
    public MobileWorkDetailDto getWorkForReview(String userId, String workId) {
        if (!StringUtils.hasText(workId)) throw new IllegalArgumentException("作品 id 不能为空");
        ExpMsg msg = expMsgRepository.findById(workId.trim())
                .orElseThrow(() -> new IllegalArgumentException("作品不存在"));
        return workService.getDetailForTeacher(msg);
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

    /* ════════════════════════════════════════
       内部方法
       ════════════════════════════════════════ */

    private TeacherReviewQueueItemDto toReviewItem(MobileExpHomeworkStudent hs) {
        if (!StringUtils.hasText(hs.getStudentExpId())) return null;
        Optional<ExpMsg> msgOpt = expMsgRepository.findById(hs.getStudentExpId());
        if (!msgOpt.isPresent()) return null;
        ExpMsg msg = msgOpt.get();

        TeacherReviewQueueItemDto item = new TeacherReviewQueueItemDto();
        item.setId(msg.getExpId());
        String studentName = resolveStudentName(msg.getCreateUserId());
        item.setStudent(studentName);
        item.setStudentInitial(initialOf(studentName));
        String avatar = resolveStudentAvatar(msg.getCreateUserId());
        if (StringUtils.hasText(avatar)) item.setStudentAvatarUrl(avatar);
        item.setTitle(msg.getExpName());
        item.setTime(msg.getCreateTime() != null ? TIME_FMT.format(msg.getCreateTime()) : "");
        item.setAvatarClass("avatar-grad-warm");

        String et = msg.getExpTaskType();
        if ("tk".equals(et)) {
            item.setWorkType("remix");
            item.setWorkTypeLabel("拍同款");
        } else if ("self".equals(et)) {
            item.setWorkType("creative");
            item.setWorkTypeLabel("创意实验");
        } else {
            item.setWorkType("homework");
            item.setWorkTypeLabel("作品");
        }
        return item;
    }

    private TeacherTaskSummaryDto buildTaskSummary(MobileExpHomework hw) {
        TeacherTaskSummaryDto item = new TeacherTaskSummaryDto();
        item.setTaskId(hw.getHomeworkId());
        item.setTitle(resolveExpName(hw.getTeacherExpId()));
        item.setClassName(resolveOrgName(hw.getClassId()));
        List<SysUser> students = listStudentsInClass(hw.getClassId());
        item.setTotalStudents(students.size());

        List<MobileExpHomeworkStudent> records = homeworkStudentRepository.findByHomeworkId(hw.getHomeworkId());
        int submitted = (int) records.stream().filter(r -> StringUtils.hasText(r.getStudentSubmitDate())).count();
        int reviewed = (int) records.stream().filter(r -> r.getMarkTime() != null).count();
        item.setSubmitted(submitted);
        item.setPendingReview(submitted - reviewed);
        item.setSubmitRate(students.isEmpty() ? 0 : (submitted * 100 / students.size()));
        item.setCancelled(false);
        if (hw.getCreateTime() != null) item.setSortTime(hw.getCreateTime().getTime());
        return item;
    }

    private String resolveExpName(String expId) {
        if (!StringUtils.hasText(expId)) return "实验任务";
        return expMsgRepository.findById(expId)
                .map(ExpMsg::getExpName)
                .filter(StringUtils::hasText)
                .orElse("实验任务");
    }

    private void fillWeeklyTrend(TeacherDashboardDto dto, List<MobileExpHomework> hws) {
        int[] buckets = new int[5];
        Set<String> hwIds = hws.stream().map(MobileExpHomework::getHomeworkId).collect(Collectors.toSet());

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

        for (MobileExpHomeworkStudent hs : homeworkStudentRepository.findAll()) {
            if (!hwIds.contains(hs.getHomeworkId()) || !StringUtils.hasText(hs.getStudentSubmitDate())) continue;
            Date submitDate = parseSubmitDate(hs.getStudentSubmitDate());
            if (submitDate == null) continue;
            Calendar cal = Calendar.getInstance();
            cal.setTime(submitDate);
            if (!cal.before(weekStart) && cal.before(nextWeek(weekStart))) {
                int idx = weekdayIndex(cal);
                if (idx >= 0 && idx < 5) buckets[idx]++;
            }
            if (!cal.before(prevWeekStart) && cal.before(weekStart)) prevTotal++;
        }

        List<Integer> trend = new ArrayList<>();
        int total = 0;
        for (int count : buckets) { trend.add(count); total += count; }
        dto.setWeeklyTrend(trend);
        dto.setTrendTotal(total);
        if (prevTotal > 0) dto.setTrendDeltaPercent((total - prevTotal) * 100 / prevTotal);
        else if (total > 0) dto.setTrendDeltaPercent(100);
        else dto.setTrendDeltaPercent(0);
    }

    private Date parseSubmitDate(String dateStr) {
        if (!StringUtils.hasText(dateStr)) return null;
        try { return TIME_FMT.parse(dateStr.trim()); }
        catch (Exception e) { return null; }
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

    private List<TeacherAssignOptionsDto.OptionItem> listClassesForTeacher(SysUser teacher) {
        if (teacher == null) return Collections.emptyList();
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
        if (StringUtils.hasText(rootId)) collectClassOptions(rootId, byParent, result, 0);
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
        if (depth > 8 || !StringUtils.hasText(orgId)) return;
        for (SysOrg child : byParent.getOrDefault(orgId, Collections.emptyList())) {
            if (CLASS_ORG_TYPE.equals(child.getOrgTypeId())) {
                int count = listStudentsInClass(child.getOrgId()).size();
                result.add(new TeacherAssignOptionsDto.OptionItem(child.getOrgId(), child.getOrgName(), count));
            }
            collectClassOptions(child.getOrgId(), byParent, result, depth + 1);
        }
    }

    private static final Set<String> ASSIGNABLE_EXP_TYPES = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList("standard", "teach", "teacher", "teaching")));

    private List<TeacherAssignOptionsDto.OptionItem> listExperimentOptions() {
        List<ExpMsg> exps = expMsgRepository.findAll().stream()
                .filter(this::isAssignableExperiment)
                .sorted(Comparator.comparing(ExpMsg::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(80)
                .collect(Collectors.toList());
        List<TeacherAssignOptionsDto.OptionItem> items = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (ExpMsg e : exps) {
            if (e == null || !StringUtils.hasText(e.getExpName()) || !seen.add(e.getExpId())) continue;
            TeacherAssignOptionsDto.OptionItem option = new TeacherAssignOptionsDto.OptionItem();
            option.setId(e.getExpId());
            option.setLabel(e.getExpName());
            option.setSubtitle(assignableExpSubtitle(e));
            if (StringUtils.hasText(e.getSimulatorId())) {
                option.setSourceType("simulator");
                option.setSimulatorId(e.getSimulatorId());
            } else {
                option.setSourceType("experiment");
            }
            items.add(option);
        }
        return items;
    }

    /** 与首页 feed 一致：已发布标准/教学实验及含模拟器的实验，排除学生作品 */
    private boolean isAssignableExperiment(ExpMsg e) {
        if (e == null || !"y".equalsIgnoreCase(e.getStatus()) || !StringUtils.hasText(e.getExpName())) {
            return false;
        }
        if ("student".equalsIgnoreCase(e.getExpType())) {
            return false;
        }
        if (StringUtils.hasText(e.getSimulatorId()) || StringUtils.hasText(e.getSimulatorUrl())) {
            return true;
        }
        String type = e.getExpType() != null ? e.getExpType().trim().toLowerCase(Locale.ROOT) : "";
        if (!StringUtils.hasText(type)) {
            return true;
        }
        return ASSIGNABLE_EXP_TYPES.contains(type);
    }

    private String assignableExpSubtitle(ExpMsg e) {
        if (StringUtils.hasText(e.getSimulatorId()) || StringUtils.hasText(e.getSimulatorUrl())) {
            return "模拟实验";
        }
        String type = e.getExpType() != null ? e.getExpType().trim().toLowerCase(Locale.ROOT) : "";
        if ("teach".equals(type) || "teaching".equals(type) || "teacher".equals(type)) {
            return "教学实验";
        }
        if ("standard".equals(type)) {
            return "标准实验";
        }
        return "实验库任务";
    }

    private String resolveStudentName(String userId) {
        if (!StringUtils.hasText(userId)) return "同学";
        return sysUserRepository.findById(userId).map(this::displayName).orElse("同学");
    }

    private String resolveStudentAvatar(String userId) {
        if (!StringUtils.hasText(userId)) return null;
        return sysUserRepository.findById(userId)
                .map(u -> MobileAvatarSupport.resolveUserAvatarUrl(minioStorageService, u))
                .orElse(null);
    }

    private String displayName(SysUser user) {
        if (StringUtils.hasText(user.getUserNickName())) return user.getUserNickName();
        return user.getUserName() != null ? user.getUserName() : "用户";
    }

    private String initialOf(String name) {
        return StringUtils.hasText(name) ? name.substring(0, 1) : "同";
    }

    private List<SysUser> listStudentsInClass(String classOrgId) {
        if (!StringUtils.hasText(classOrgId)) return Collections.emptyList();
        return sysUserRepository.findAll().stream()
                .filter(u -> STUDENT_ROLE.equals(u.getUserRoleId()))
                .filter(u -> "y".equalsIgnoreCase(safe(u.getStatus())))
                .filter(u -> classOrgId.equals(u.getUserOrgId()))
                .sorted(Comparator.comparing(this::displayName, Comparator.nullsLast(String::compareTo)))
                .collect(Collectors.toList());
    }

    private String resolveOrgName(String orgId) {
        if (!StringUtils.hasText(orgId)) return null;
        return sysOrgRepository.findById(orgId).map(SysOrg::getOrgName).orElse(orgId);
    }

    private static String safe(String value) {
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
