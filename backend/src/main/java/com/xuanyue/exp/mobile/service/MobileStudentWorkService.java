package com.xuanyue.exp.mobile.service;

import com.xuanyue.exp.common.PageResult;
import com.xuanyue.exp.common.storage.minio.MinioStorageService;
import com.xuanyue.exp.data.entity.DataFile;
import com.xuanyue.exp.data.repository.DataFileRepository;
import com.xuanyue.exp.exp.entity.ExpMsg;
import com.xuanyue.exp.exp.entity.ExpVideo;
import com.xuanyue.exp.exp.repository.ExpMsgRepository;
import com.xuanyue.exp.exp.repository.ExpVideoRepository;
import com.xuanyue.exp.mobile.dto.*;
import com.xuanyue.exp.mobile.entity.MobileExpHomework;
import com.xuanyue.exp.mobile.entity.MobileExpHomeworkStudent;
import com.xuanyue.exp.mobile.entity.MbTask;
import com.xuanyue.exp.mobile.entity.MbTaskSubmission;
import com.xuanyue.exp.mobile.repository.MbTaskRepository;
import com.xuanyue.exp.mobile.repository.MbTaskSubmissionRepository;
import com.xuanyue.exp.mobile.repository.MobileExpHomeworkRepository;
import com.xuanyue.exp.mobile.repository.MobileExpHomeworkStudentRepository;
import com.xuanyue.exp.mobile.support.MobileHomeCache;
import com.xuanyue.exp.mobile.support.MobileIds;
import com.xuanyue.exp.mobile.support.MobileMediaUrlSupport;
import com.xuanyue.exp.mobile.support.MobileMinioKeySupport;
import com.xuanyue.exp.mobile.support.MobileParentAccessService;
import com.xuanyue.exp.mobile.support.MobileStudentOrgSupport;
import com.xuanyue.exp.mobile.support.MobileAvatarSupport;
import com.xuanyue.exp.mobile.support.MobileTeacherClassScope;
import com.xuanyue.exp.mobile.support.MobileUserContext;
import com.xuanyue.exp.mobile.support.MobileWorkAuditStatus;
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
 * 学生作品核心服务（统一使用 exp_msg 数据源）
 *
 * 数据模型：
 * - exp_msg (exp_type='student') → 所有学生作品的统一容器
 * - exp_homework → 作业布置
 * - exp_homework_student → 作业提交与批阅流程
 * - data_file + exp_video → 媒体文件存储
 */
@Service
public class MobileStudentWorkService {

    private static final SimpleDateFormat TIME_FMT = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private static final int MAX_IMAGES = 9;
    private static final int MAX_VIDEOS = 3;

    /* ───── 依赖注入 ───── */
    private final ExpMsgRepository expMsgRepository;
    private final ExpVideoRepository expVideoRepository;
    private final DataFileRepository dataFileRepository;
    private final MobileExpHomeworkRepository homeworkRepository;
    private final MobileExpHomeworkStudentRepository homeworkStudentRepository;
    private final MbTaskRepository mbTaskRepository;
    private final MbTaskSubmissionRepository mbTaskSubmissionRepository;
    private final SysUserRepository sysUserRepository;
    private final SysOrgRepository sysOrgRepository;
    private final MobilePointsService pointsService;
    private final MobileBadgeGrantService badgeGrantService;
    private final MobileNotificationService notificationService;
    private final MobileParentAccessService parentAccessService;
    private final MobileHomeCache homeCache;
    private final MinioStorageService minioStorageService;
    private final MobileStudentOrgSupport studentOrgSupport;

    public MobileStudentWorkService(ExpMsgRepository expMsgRepository,
                                    ExpVideoRepository expVideoRepository,
                                    DataFileRepository dataFileRepository,
                                    MobileExpHomeworkRepository homeworkRepository,
                                    MobileExpHomeworkStudentRepository homeworkStudentRepository,
                                    MbTaskRepository mbTaskRepository,
                                    MbTaskSubmissionRepository mbTaskSubmissionRepository,
                                    SysUserRepository sysUserRepository,
                                    SysOrgRepository sysOrgRepository,
                                    MobilePointsService pointsService,
                                    MobileBadgeGrantService badgeGrantService,
                                    MobileNotificationService notificationService,
                                    MobileParentAccessService parentAccessService,
                                    MobileHomeCache homeCache,
                                    MinioStorageService minioStorageService,
                                    MobileStudentOrgSupport studentOrgSupport) {
        this.expMsgRepository = expMsgRepository;
        this.expVideoRepository = expVideoRepository;
        this.dataFileRepository = dataFileRepository;
        this.homeworkRepository = homeworkRepository;
        this.homeworkStudentRepository = homeworkStudentRepository;
        this.mbTaskRepository = mbTaskRepository;
        this.mbTaskSubmissionRepository = mbTaskSubmissionRepository;
        this.sysUserRepository = sysUserRepository;
        this.sysOrgRepository = sysOrgRepository;
        this.pointsService = pointsService;
        this.badgeGrantService = badgeGrantService;
        this.notificationService = notificationService;
        this.parentAccessService = parentAccessService;
        this.homeCache = homeCache;
        this.minioStorageService = minioStorageService;
        this.studentOrgSupport = studentOrgSupport;
    }

    /* ════════════════════════════════════════════
       1. 教师布置作业 → exp_homework
       ════════════════════════════════════════════ */

    @Transactional
    public void assignHomework(String userId, AssignHomeworkRequest request) {
        String teacherId = MobileUserContext.resolveTeacherId(userId);
        String teacherExpId = Objects.requireNonNull(request.getExperimentId(), "请选择关联实验").trim();
        String classId = Objects.requireNonNull(request.getClassId(), "请选择班级").trim();

        MobileExpHomework hw = new MobileExpHomework();
        hw.setHomeworkId(MobileIds.newId());
        hw.setTeacherExpId(teacherExpId);
        hw.setTearcherUserId(teacherId);
        hw.setClassId(classId);
        hw.setRequireDate(StringUtils.hasText(request.getRequireDate()) ? request.getRequireDate().trim() : null);
        hw.setCreateTime(new Date());
        homeworkRepository.save(hw);

        notificationService.sendTaskAssigned(teacherId, hw);
    }

    /* ════════════════════════════════════════════
       2. 学生提交作品 → exp_msg + exp_homework_student
       ════════════════════════════════════════════ */

    @Transactional
    public MobileWorkDetailDto submitWork(String userId, CreateWorkRequest request) {
        if (request == null) throw new IllegalArgumentException("请求不能为空");
        validateWorkFiles(request.getFiles());

        String studentId = resolveStudentId(userId, request);
        String workType = resolveWorkType(request);
        String title = resolveWorkTitle(request);
        String taskId = resolveTaskId(request);
        Date now = new Date();

        // 2a. 创建 exp_msg 作为作品容器
        ExpMsg msg = new ExpMsg();
        msg.setExpId(MobileIds.newId("exp"));
        msg.setExpName(title);
        msg.setExpType("student");
        msg.setExpTaskType(mapWorkType(workType));
        msg.setCreateUserId(studentId.trim());
        // 提交即进入待审核(t)，需老师/校管理员审核通过(y)后方可在首页/公开墙展示
        msg.setStatus(MobileWorkAuditStatus.PENDING);
        msg.setChooseType("");           // NOT NULL, 学生作品无此分类
        msg.setSubjectId("");            // NOT NULL, 学生作品无学科关联
        msg.setSchoolLevelId("");        // NOT NULL, 学生作品无学段关联
        msg.setLikeNum(0);
        msg.setNotlikeNum(0);
        msg.setCollectionNum(0);
        msg.setEvaluateNum(0);
        msg.setCreateTime(now);
        msg.setUpdateTime(now);
        if (StringUtils.hasText(request.getSourceExpId())) {
            msg.setLinkExpId(request.getSourceExpId().trim());
        } else {
            String linkExpId = resolveTeacherExpIdFromTask(taskId);
            if (StringUtils.hasText(linkExpId)) {
                msg.setLinkExpId(linkExpId);
            }
        }
        if (StringUtils.hasText(request.getDescription())) {
            msg.setConfirmComments(request.getDescription().trim());
        }
        expMsgRepository.save(msg);

        // 2b. 保存媒体文件到 data_file + exp_video
        saveWorkMedia(msg.getExpId(), request.getFiles(), now);

        // 2c. 如果是作业，写入 exp_homework_student
        if (StringUtils.hasText(taskId)) {
            upsertHomeworkStudent(taskId.trim(), studentId, msg.getExpId(), now);
        }

        // 2d. 积分
        pointsService.credit(studentId, MobilePointsService.WORK_SUBMIT_POINTS,
                "student_work", msg.getExpId(), "提交作品");
        badgeGrantService.onWorkSubmitted(studentId);

        homeCache.invalidateFeed();
        return buildDetailDto(msg, studentId, workType);
    }

    /* ════════════════════════════════════════════
       2e. 学生编辑已提交作品
       ════════════════════════════════════════════ */

    @Transactional
    public MobileWorkDetailDto updateWork(String userId, String workId, UpdateWorkRequest request) {
        if (request == null || !StringUtils.hasText(workId)) {
            throw new IllegalArgumentException("请求不能为空");
        }
        ExpMsg msg = expMsgRepository.findById(workId.trim())
                .orElseThrow(() -> new IllegalArgumentException("作品不存在"));

        String studentId = resolveStudentId(userId, null);
        if (!studentId.trim().equals(msg.getCreateUserId() != null ? msg.getCreateUserId().trim() : "")) {
            throw new IllegalArgumentException("只能编辑自己的作品");
        }
        // 学生本人的草稿/待审核/已通过/未通过作品均可编辑（管理端草稿 'c' 不在此列）
        String curStatus = msg.getStatus();
        if (!MobileWorkAuditStatus.DRAFT.equals(curStatus)
                && !MobileWorkAuditStatus.PENDING.equals(curStatus)
                && !MobileWorkAuditStatus.APPROVED.equals(curStatus)
                && !MobileWorkAuditStatus.REJECTED.equals(curStatus)) {
            throw new IllegalArgumentException("该作品状态不可编辑");
        }
        Date now = new Date();

        // 更新标题
        if (StringUtils.hasText(request.getTitle())) {
            msg.setExpName(request.getTitle().trim());
        }
        // 更新描述
        msg.setConfirmComments(StringUtils.hasText(request.getDescription())
                ? request.getDescription().trim() : null);
        // 已提交作品被编辑后内容已变更，需重新审核（草稿仍保持草稿）
        if (!MobileWorkAuditStatus.DRAFT.equals(curStatus)) {
            msg.setStatus(MobileWorkAuditStatus.PENDING);
            msg.setConfirmUserId(null);
            msg.setConfirmTime(null);
        }
        msg.setUpdateTime(now);
        expMsgRepository.save(msg);

        // 更新媒体文件
        updateWorkMedia(workId.trim(), request.getFiles(), now);

        // 如果有批阅记录，清空并重置为待批阅
        List<MobileExpHomeworkStudent> reviewRecords = homeworkStudentRepository
                .findByStudentExpId(workId.trim());
        for (MobileExpHomeworkStudent hs : reviewRecords) {
            if (hs.getMarkTime() != null) {
                hs.setMarkUserId(null);
                hs.setMarkTime(null);
                hs.setMarkComments(null);
                hs.setMarkResult(null);
                homeworkStudentRepository.save(hs);
            }
        }

        homeCache.invalidateFeed();
        String workType = reverseMapWorkType(msg.getExpTaskType());
        return buildDetailDto(msg, studentId, workType);
    }

    private void updateWorkMedia(String expId, List<CreateWorkFileItem> files, Date now) {
        List<ExpVideo> existing = expVideoRepository.findByExpIdOrderBySortOrderAsc(expId);
        Map<String, ExpVideo> existingBySeqId = new HashMap<>();
        for (ExpVideo ev : existing) {
            existingBySeqId.put(ev.getSeqId(), ev);
        }
        Set<String> keptIds = new HashSet<>();
        if (files != null) {
            for (CreateWorkFileItem item : files) {
                if (item != null && StringUtils.hasText(item.getId())) {
                    keptIds.add(item.getId().trim());
                }
            }
        }
        for (ExpVideo ev : existing) {
            if (!keptIds.contains(ev.getSeqId())) {
                if (StringUtils.hasText(ev.getFileId())) {
                    dataFileRepository.deleteById(ev.getFileId().trim());
                }
                expVideoRepository.delete(ev);
            }
        }
        if (files != null) {
            int sortOrder = 0;
            for (CreateWorkFileItem item : files) {
                if (item == null || !StringUtils.hasText(item.getUrl())) continue;
                if (StringUtils.hasText(item.getId())) {
                    ExpVideo existingEv = existingBySeqId.get(item.getId().trim());
                    if (existingEv != null && (existingEv.getSortOrder() == null || existingEv.getSortOrder() != sortOrder)) {
                        existingEv.setSortOrder(sortOrder);
                        expVideoRepository.save(existingEv);
                    }
                    sortOrder++;
                    continue;
                }
                DataFile df = new DataFile();
                df.setFileId(MobileIds.newId("df"));
                df.setFileName(truncate(item.getName(), 60));
                df.setFileUrl(truncate(MobileMinioKeySupport.normalizeForStorage(minioStorageService, item.getUrl()), 200));
                df.setFileTypeId("video".equalsIgnoreCase(item.getType()) ? "video" : "image");
                df.setStatus("y");
                df.setIsPublic("n");
                df.setCreateTime(now);
                df.setUpdateTime(now);
                dataFileRepository.save(df);

                ExpVideo video = new ExpVideo();
                video.setSeqId(MobileIds.newId("ev"));
                video.setExpId(expId);
                video.setFileId(df.getFileId());
                video.setVideoUrl(df.getFileUrl());
                video.setSortOrder(sortOrder);
                expVideoRepository.save(video);
                sortOrder++;
            }
        }
    }

    private void saveWorkMedia(String expId, List<CreateWorkFileItem> files, Date now) {
        if (files == null || files.isEmpty()) return;
        for (int i = 0; i < files.size(); i++) {
            CreateWorkFileItem item = files.get(i);
            if (item == null || !StringUtils.hasText(item.getUrl())) continue;

            DataFile df = new DataFile();
            df.setFileId(MobileIds.newId("df"));
            df.setFileName(truncate(item.getName(), 60));
            df.setFileUrl(truncate(MobileMinioKeySupport.normalizeForStorage(minioStorageService, item.getUrl()), 200));
            df.setFileTypeId("video".equalsIgnoreCase(item.getType()) ? "video" : "image");
            df.setStatus("y");
            df.setIsPublic("n");
            df.setCreateTime(now);
            df.setUpdateTime(now);
            dataFileRepository.save(df);

            ExpVideo video = new ExpVideo();
            video.setSeqId(MobileIds.newId("ev"));
            video.setExpId(expId);
            video.setFileId(df.getFileId());
            video.setVideoUrl(df.getFileUrl());
            video.setSortOrder(i);
            expVideoRepository.save(video);
        }
    }

    private void upsertHomeworkStudent(String homeworkId, String studentId, String expMsgId, Date now) {
        // 先尝试新系统 exp_homework
        Optional<MobileExpHomework> hwOpt = homeworkRepository.findById(homeworkId);
        if (hwOpt.isPresent()) {
            MobileExpHomework hw = hwOpt.get();
            List<MobileExpHomeworkStudent> existing = homeworkStudentRepository.findByHomeworkId(homeworkId);
            MobileExpHomeworkStudent hs = null;
            for (MobileExpHomeworkStudent e : existing) {
                if (StringUtils.hasText(e.getStudentExpId())) {
                    Optional<ExpMsg> m = expMsgRepository.findById(e.getStudentExpId());
                    if (m.isPresent() && studentId.equals(m.get().getCreateUserId())) {
                        hs = e;
                        break;
                    }
                }
            }
            if (hs == null) {
                hs = new MobileExpHomeworkStudent();
                hs.setSeqId(MobileIds.newId());
                hs.setHomeworkId(homeworkId);
                hs.setTeacherExpId(hw.getTeacherExpId());
                hs.setTeacherUserId(hw.getTearcherUserId());
                hs.setRequireDate(hw.getRequireDate());
            }
            hs.setStudentExpId(expMsgId);
            hs.setStudentSubmitDate(TIME_FMT.format(now));
            homeworkStudentRepository.save(hs);
            return;
        }

        // 再尝试旧系统 mb_task（模拟实验等历史任务）
        Optional<MbTask> mbTaskOpt = mbTaskRepository.findById(homeworkId);
        if (mbTaskOpt.isPresent()) {
            MbTask task = mbTaskOpt.get();
            Optional<MbTaskSubmission> subOpt = mbTaskSubmissionRepository
                    .findByTaskIdAndStudentUserId(homeworkId, studentId);
            MbTaskSubmission sub;
            if (subOpt.isPresent()) {
                sub = subOpt.get();
            } else {
                sub = new MbTaskSubmission();
                sub.setSubmissionId(MobileIds.newId());
                sub.setTaskId(homeworkId);
                sub.setStudentUserId(studentId);
                sub.setCreateTime(now);
            }
            sub.setState("submitted");
            sub.setStateLabel("已提交");
            sub.setBadgeClass("badge-success");
            sub.setUpdateTime(now);
            sub.setSubmitTime(now);
            mbTaskSubmissionRepository.save(sub);
            return;
        }

        throw new IllegalArgumentException("作业不存在");
    }

    /* ════════════════════════════════════════════
       3. 教师批阅 → exp_homework_student.mark_*
       ════════════════════════════════════════════ */

    /* ════════════════════════════════════════════
       管理员（系统/校管理员）学生作品审核队列（本校范围）
       ════════════════════════════════════════════ */

    @Transactional(readOnly = true)
    public PageResult<MobileWorkReviewItemDto> listPendingWorkReviewsForAdmin(String adminUserId, int page, int size) {
        SysUser admin = requireAdminUser(adminUserId);
        List<ExpMsg> candidates = collectPendingAdminWorks(admin);
        long total = candidates.size();
        int pageSize = Math.max(size, 1);
        int from = Math.max(0, (Math.max(page, 1) - 1) * pageSize);
        int to = Math.min(candidates.size(), from + pageSize);
        List<MobileWorkReviewItemDto> items = new ArrayList<>();
        if (from < candidates.size()) {
            for (ExpMsg msg : candidates.subList(from, to)) {
                items.add(toAdminReviewItem(msg));
            }
        }
        return new PageResult<>(total, items);
    }

    @Transactional(readOnly = true)
    public int countPendingWorkReviewsForAdmin(String adminUserId) {
        if (!StringUtils.hasText(adminUserId)) {
            return 0;
        }
        SysUser admin = sysUserRepository.findById(adminUserId.trim()).orElse(null);
        if (admin == null) {
            return 0;
        }
        String role = safe(admin.getUserRoleId());
        if (!"Sys_Admin".equals(role) && !"School_Admin".equals(role)) {
            return 0;
        }
        return collectPendingAdminWorks(admin).size();
    }

    private SysUser requireAdminUser(String adminUserId) {
        if (!StringUtils.hasText(adminUserId)) {
            throw new IllegalArgumentException("请先登录");
        }
        SysUser admin = sysUserRepository.findById(adminUserId.trim())
                .orElseThrow(() -> new IllegalArgumentException("登录用户不存在"));
        String role = safe(admin.getUserRoleId());
        if (!"Sys_Admin".equals(role) && !"School_Admin".equals(role)) {
            throw new IllegalArgumentException("无权访问学生作品审核");
        }
        return admin;
    }

    private List<ExpMsg> collectPendingAdminWorks(SysUser admin) {
        String role = safe(admin.getUserRoleId());
        boolean global = "Sys_Admin".equals(role);
        final Set<String> schoolUserIds;
        if (global) {
            schoolUserIds = Collections.emptySet();
        } else {
            String rootOrgId = safe(admin.getRootOrgId());
            if (rootOrgId.isEmpty()) {
                return Collections.emptyList();
            }
            schoolUserIds = sysUserRepository.findByRootOrgId(rootOrgId).stream()
                    .map(SysUser::getUserId)
                    .filter(StringUtils::hasText)
                    .map(String::trim)
                    .collect(Collectors.toSet());
        }
        return expMsgRepository.findAll().stream()
                .filter(m -> "student".equals(m.getExpType()))
                .filter(m -> MobileWorkAuditStatus.isPending(m.getStatus()))
                .filter(m -> "tk".equals(m.getExpTaskType()) || "self".equals(m.getExpTaskType()))
                .filter(m -> global
                        || (StringUtils.hasText(m.getCreateUserId())
                            && schoolUserIds.contains(m.getCreateUserId().trim())))
                .sorted((a, b) -> {
                    Date ta = a.getCreateTime();
                    Date tb = b.getCreateTime();
                    if (ta == null && tb == null) return 0;
                    if (ta == null) return 1;
                    if (tb == null) return -1;
                    return tb.compareTo(ta);
                })
                .collect(Collectors.toList());
    }

    private MobileWorkReviewItemDto toAdminReviewItem(ExpMsg msg) {
        MobileWorkReviewItemDto item = new MobileWorkReviewItemDto();
        item.setId(msg.getExpId());
        item.setTitle(StringUtils.hasText(msg.getExpName()) ? msg.getExpName() : "学生作品");
        SysUser student = StringUtils.hasText(msg.getCreateUserId())
                ? sysUserRepository.findById(msg.getCreateUserId().trim()).orElse(null) : null;
        String name = "学生";
        String group = "";
        if (student != null) {
            if (StringUtils.hasText(student.getUserName())) {
                name = student.getUserName().trim();
            } else if (StringUtils.hasText(student.getUserNickName())) {
                name = student.getUserNickName().trim();
            } else if (StringUtils.hasText(student.getLoginName())) {
                name = student.getLoginName().trim();
            }
            group = resolveOrgName(student.getUserOrgId());
            if (!StringUtils.hasText(group)) {
                group = resolveOrgName(student.getRootOrgId());
            }
        }
        item.setStudentName(name);
        item.setStudentInitial(name.isEmpty() ? "?" : name.substring(0, 1));
        item.setGroupLabel(group);
        item.setTime(msg.getCreateTime() != null ? TIME_FMT.format(msg.getCreateTime()) : "");
        if ("tk".equals(msg.getExpTaskType())) {
            item.setWorkType("remix");
            item.setWorkTypeLabel("拍同款");
        } else {
            item.setWorkType("creative");
            item.setWorkTypeLabel("创意实验");
        }
        return item;
    }

    private static String safe(String value) {
        return value != null ? value.trim() : "";
    }

    @Transactional
    public void reviewWork(String teacherUserId, String msgId, String result, String comment) {
        ExpMsg msg = expMsgRepository.findById(msgId)
                .orElseThrow(() -> new IllegalArgumentException("作品不存在"));
        ensureCanReviewStudentWork(teacherUserId, msg);

        Date now = new Date();
        // 1) 评分/评语落 exp_homework_student（作业类作品有关联记录；创意/拍同款可能无记录）
        List<MobileExpHomeworkStudent> records = homeworkStudentRepository.findByStudentExpId(msgId);
        for (MobileExpHomeworkStudent hs : records) {
            hs.setMarkUserId(teacherUserId);
            hs.setMarkTime(now);
            hs.setMarkComments(StringUtils.hasText(comment) ? comment.trim() : null);
            hs.setMarkResult(StringUtils.hasText(result) ? result.trim().toLowerCase() : "pass");
            homeworkStudentRepository.save(hs);
        }

        // 2) 审核结论落 exp_msg.status：合格→通过(y)上首页，不合格(fail)→驳回(n)不展示
        String decision = MobileWorkAuditStatus.fromRating(result);
        msg.setStatus(decision);
        msg.setConfirmUserId(teacherUserId);
        msg.setConfirmTime(now);
        msg.setUpdateTime(now);
        expMsgRepository.save(msg);

        String studentId = msg.getCreateUserId();
        if (StringUtils.hasText(studentId) && MobileWorkAuditStatus.isApproved(decision)) {
            pointsService.credit(studentId, MobilePointsService.WORK_FEATURED_POINTS,
                    "reviewed", msgId, "作品审核通过");
        }

        homeCache.invalidateFeed();
    }

    /* ════════════════════════════════════════════
       4. 拍同款 / 创意实验起始
       ════════════════════════════════════════════ */

    @Transactional
    public MobileTaskDto startStudentTask(String userId, String taskType, String refExpId) {
        String studentId = MobileUserContext.resolveStudentId(userId);
        String expTaskType = "remix".equals(taskType) ? "tk" : "self";
        Date now = new Date();

        Optional<ExpMsg> inProgress = expMsgRepository
                .findTopByCreateUserIdAndStatusAndExpTypeOrderByCreateTimeDesc(studentId, "draft", "student");
        if (inProgress.isPresent() && expTaskType.equals(inProgress.get().getExpTaskType())) {
            return toTaskDto(inProgress.get());
        }

        ExpMsg msg = new ExpMsg();
        msg.setExpId(MobileIds.newId("exp"));
        msg.setExpName("remix".equals(taskType) ? "拍同款" : "创意实验");
        msg.setExpType("student");
        msg.setExpTaskType(expTaskType);
        msg.setCreateUserId(studentId.trim());
        msg.setStatus("draft");
        msg.setChooseType("");           // NOT NULL
        msg.setSubjectId("");            // NOT NULL
        msg.setSchoolLevelId("");        // NOT NULL
        msg.setLikeNum(0);
        msg.setNotlikeNum(0);
        msg.setCollectionNum(0);
        msg.setEvaluateNum(0);
        msg.setCreateTime(now);
        msg.setUpdateTime(now);

        if (StringUtils.hasText(refExpId)) {
            msg.setLinkExpId(refExpId.trim());
            expMsgRepository.findById(refExpId.trim()).ifPresent(ref -> {
                if (StringUtils.hasText(ref.getExpName())) {
                    msg.setExpName("拍同款 · " + ref.getExpName().trim());
                }
            });
        }
        expMsgRepository.save(msg);
        return toTaskDto(msg);
    }

    public MobileTaskListItemDto buildStartCard(String taskType) {
        MobileTaskListItemDto item = new MobileTaskListItemDto();
        if ("remix".equals(taskType)) {
            item.setId("remix-start");
            item.setCategory("remix");
            item.setKind("remix-start");
            item.setTitle("开始拍同款");
            item.setDesc("拍摄与参考视频同主题的实验过程，提交后由老师审核");
            item.setLink("/upload?type=remix");
        } else {
            item.setId("creative-start");
            item.setCategory("creative");
            item.setKind("creative-start");
            item.setTitle("开始创意实验");
            item.setDesc("自由上传实验成果，不关联老师布置，提交后由老师审核再展示在作品墙");
            item.setLink("/upload?type=creative");
        }
        item.setState("pending");
        item.setStateLabel("待开始");
        item.setBadgeClass("badge-warning");
        item.setSortTime(System.currentTimeMillis());
        return item;
    }

    @Transactional(readOnly = true)
    public boolean hasInProgressStudentTask(String userId, String taskType) {
        String studentId = MobileUserContext.resolveStudentId(userId);
        Optional<ExpMsg> found = expMsgRepository
                .findTopByCreateUserIdAndStatusAndExpTypeOrderByCreateTimeDesc(studentId, "draft", "student");
        return found.isPresent();
    }

    /* ════════════════════════════════════════════
       5. 查询：作品墙、个人作品、详情
       ════════════════════════════════════════════ */

    @Transactional(readOnly = true)
    public PageResult<MobileWorkItemDto> listPublicWorks(String type, int page, int size) {
        List<ExpMsg> all = loadStudentMsgs(type);
        List<ExpMsg> visible = all.stream()
                .filter(m -> "y".equals(m.getStatus()))
                .collect(Collectors.toList());
        List<MobileWorkItemDto> items = visible.stream()
                .map(this::toWorkItemDto)
                .collect(Collectors.toList());
        enrichWorkItemFiles(items);

        items.sort((a, b) -> {
            if (a.getTimeLabel() == null && b.getTimeLabel() == null) return 0;
            if (a.getTimeLabel() == null) return 1;
            if (b.getTimeLabel() == null) return -1;
            return b.getTimeLabel().compareTo(a.getTimeLabel());
        });

        return paginate(items, page, size);
    }

    @Transactional(readOnly = true)
    public PageResult<MobileWorkItemDto> listMyWorks(String userId, String type, int page, int size) {
        String studentId = MobileUserContext.resolveStudentId(userId);
        List<ExpMsg> msgs = expMsgRepository.findAll().stream()
                .filter(m -> "student".equals(m.getExpType()))
                .filter(m -> isOwnVisibleStatus(m.getStatus()))
                .filter(m -> studentId.equals(m.getCreateUserId()))
                .filter(m -> matchExpTaskType(m.getExpTaskType(), type))
                .sorted((a, b) -> cmpTime(b.getCreateTime(), a.getCreateTime()))
                .collect(Collectors.toList());

        List<MobileWorkItemDto> items = msgs.stream()
                .map(this::toWorkItemDto)
                .collect(Collectors.toList());
        enrichWorkItemFiles(items);

        items.sort((a, b) -> {
            if (a.getTimeLabel() == null && b.getTimeLabel() == null) return 0;
            if (a.getTimeLabel() == null) return 1;
            if (b.getTimeLabel() == null) return -1;
            return b.getTimeLabel().compareTo(a.getTimeLabel());
        });

        return paginate(items, page, size);
    }

    @Transactional(readOnly = true)
    public MobileWorkDetailDto getWorkDetail(String msgId, String viewerUserId) {
        return getWorkDetail(msgId, viewerUserId, false);
    }

    /**
     * @param privileged true 表示审核场景（老师/校管理员）可查看待审核/未通过作品；
     *                   false 表示普通访问，仅通过(y)作品对外可见，待审核/未通过/草稿仅作者本人可见。
     */
    @Transactional(readOnly = true)
    public MobileWorkDetailDto getWorkDetail(String msgId, String viewerUserId, boolean privileged) {
        ExpMsg msg = expMsgRepository.findById(msgId).orElse(null);
        if (msg == null) return null;
        if (!privileged && MobileWorkAuditStatus.isAuthorOnlyVisible(msg.getStatus())
                && !isOwnerOrGuardian(viewerUserId, msg.getCreateUserId())) {
            return null;
        }

        MobileWorkDetailDto dto = new MobileWorkDetailDto();
        dto.setId(msg.getExpId());
        dto.setTitle(msg.getExpName());
        dto.setDesc(msg.getConfirmComments());
        dto.setLikes(msg.getLikeNum() != null ? msg.getLikeNum() : 0);
        dto.setComments(msg.getEvaluateNum() != null ? msg.getEvaluateNum() : 0);
        dto.setAuthor(resolveUserName(msg.getCreateUserId()));
        dto.setAuthorUserId(msg.getCreateUserId());
        enrichAuthorInfo(dto, msg.getCreateUserId());
        dto.setSourceExpId(resolveLinkExpId(msg));
        dto.setSourceExpName(resolveSourceExpName(dto.getSourceExpId()));
        dto.setReviewStatus(MobileWorkAuditStatus.reviewStatusCode(msg.getStatus()));
        dto.setReviewStatusLabel(MobileWorkAuditStatus.reviewStatusLabel(msg.getStatus()));
        if (msg.getCreateTime() != null) dto.setTime(TIME_FMT.format(msg.getCreateTime()));

        enrichTeacherReview(dto, msg.getExpId());

        List<ExpVideo> videos = expVideoRepository.findByExpIdOrderBySortOrderAsc(msg.getExpId());
        List<MobileWorkFileDto> files = videos.stream().map(this::toFileDto).collect(Collectors.toList());
        dto.setFiles(files);
        dto.setFileCount(files.size());
        if (!files.isEmpty()) {
            MobileWorkFileDto cover = files.get(0);
            dto.setCoverUrl(cover.getPreviewUrl());
            dto.setCoverType(cover.getType());
        }
        return dto;
    }

    /* ════════════════════════════════════════════
       6. 教师看板查询
       ════════════════════════════════════════════ */

    @Transactional(readOnly = true)
    public List<TeacherTaskSummaryDto> listTeacherHomeworks(String userId) {
        String teacherId = MobileUserContext.resolveTeacherId(userId);
        List<MobileExpHomework> homeworks = homeworkRepository
                .findByTearcherUserIdAndCreateTimeIsNotNullOrderByCreateTimeDesc(teacherId);

        return homeworks.stream().map(hw -> {
            TeacherTaskSummaryDto dto = new TeacherTaskSummaryDto();
            dto.setTaskId(hw.getHomeworkId());
            dto.setTitle(resolveExpName(hw.getTeacherExpId()));
            dto.setClassName(resolveOrgName(hw.getClassId()));

            List<MobileExpHomeworkStudent> records = homeworkStudentRepository.findByHomeworkId(hw.getHomeworkId());
            int submitted = (int) records.stream()
                    .filter(r -> StringUtils.hasText(r.getStudentSubmitDate()))
                    .count();
            int reviewed = (int) records.stream()
                    .filter(r -> r.getMarkTime() != null)
                    .count();
            dto.setSubmitted(submitted);
            dto.setPendingReview(submitted - reviewed);
            dto.setSubmitRate(0);
            if (hw.getCreateTime() != null) dto.setSortTime(hw.getCreateTime().getTime());
            return dto;
        }).collect(Collectors.toList());
    }

    /* ════════════════════════════════════════════
       7. 内部工具方法
       ════════════════════════════════════════════ */

    public List<ExpMsg> loadUserMsgs(String studentId, String typeFilter) {
        String safeStudentId = studentId != null ? studentId.trim() : "";
        return expMsgRepository.findAll().stream()
                .filter(m -> "student".equals(m.getExpType()))
                .filter(m -> isOwnVisibleStatus(m.getStatus()))
                .filter(m -> safeStudentId.equals(m.getCreateUserId() != null ? m.getCreateUserId().trim() : ""))
                .filter(m -> matchExpTaskType(m.getExpTaskType(), typeFilter))
                .sorted((a, b) -> cmpTime(b.getCreateTime(), a.getCreateTime()))
                .collect(Collectors.toList());
    }

    /** 学生本人可见的作品状态：草稿/待审核/通过/未通过（管理端草稿 'c' 除外） */
    private boolean isOwnVisibleStatus(String status) {
        return MobileWorkAuditStatus.isDraft(status)
                || MobileWorkAuditStatus.isPending(status)
                || MobileWorkAuditStatus.isApproved(status)
                || MobileWorkAuditStatus.isRejected(status);
    }

    /** 列表卡片审核状态徽标 */
    private void applyAuditBadge(MobileWorkItemDto item, String status) {
        item.setReviewStatus(MobileWorkAuditStatus.reviewStatusCode(status));
        item.setReviewStatusLabel(MobileWorkAuditStatus.reviewStatusLabel(status));
        if (MobileWorkAuditStatus.isApproved(status)) {
            item.setReviewBadgeClass("badge-success");
        } else if (MobileWorkAuditStatus.isRejected(status)) {
            item.setReviewBadgeClass("badge-danger");
            item.setCanEdit(true);
        } else if (MobileWorkAuditStatus.isDraft(status)) {
            item.setReviewStatusLabel("待完成");
            item.setReviewBadgeClass("badge-warning");
            item.setCanEdit(true);
        } else {
            item.setReviewBadgeClass("badge-info");
        }
    }

    /** 普通访问场景下，待审核/未通过/草稿作品仅作者本人（或其家长）可见 */
    private boolean isOwnerOrGuardian(String viewerUserId, String authorUserId) {
        if (!StringUtils.hasText(viewerUserId) || !StringUtils.hasText(authorUserId)) {
            return false;
        }
        String author = authorUserId.trim();
        if (author.equals(viewerUserId.trim())) {
            return true;
        }
        try {
            String scoped = parentAccessService.resolveStudentScope(viewerUserId, author);
            return author.equals(scoped);
        } catch (RuntimeException ignore) {
            return false;
        }
    }

    /** 审核权限校验：系统管理员全局；校管理员限本校；老师限其布置作业或所管理班级的学生 */
    private void ensureCanReviewStudentWork(String reviewerUserId, ExpMsg work) {
        if (!StringUtils.hasText(reviewerUserId)) {
            throw new IllegalArgumentException("请先登录");
        }
        SysUser reviewer = sysUserRepository.findById(reviewerUserId.trim())
                .orElseThrow(() -> new IllegalArgumentException("登录用户不存在"));
        String role = reviewer.getUserRoleId() != null ? reviewer.getUserRoleId().trim() : "";
        if ("Sys_Admin".equals(role)) {
            return;
        }
        String studentId = work.getCreateUserId();
        SysUser student = StringUtils.hasText(studentId)
                ? sysUserRepository.findById(studentId.trim()).orElse(null) : null;

        if ("School_Admin".equals(role)) {
            if (student != null && sameSchool(reviewer, student)) {
                return;
            }
            throw new IllegalArgumentException("无权审核该作品（非本校学生）");
        }
        if ("Teacher".equals(role)) {
            if (isHomeworkOwner(reviewerUserId, work.getExpId())) {
                return;
            }
            if (student != null && teacherManagesStudentClass(reviewer, student)) {
                return;
            }
            throw new IllegalArgumentException("无权审核该作品（非任课班级）");
        }
        throw new IllegalArgumentException("无权审核该作品");
    }

    private boolean sameSchool(SysUser a, SysUser b) {
        String ra = a.getRootOrgId() != null ? a.getRootOrgId().trim() : "";
        String rb = b.getRootOrgId() != null ? b.getRootOrgId().trim() : "";
        return !ra.isEmpty() && ra.equals(rb);
    }

    private boolean isHomeworkOwner(String reviewerUserId, String workExpId) {
        String teacherId = MobileUserContext.resolveTeacherId(reviewerUserId);
        if (!StringUtils.hasText(teacherId)) {
            return false;
        }
        List<MobileExpHomeworkStudent> records = homeworkStudentRepository.findByStudentExpId(workExpId);
        for (MobileExpHomeworkStudent hs : records) {
            if (!StringUtils.hasText(hs.getHomeworkId())) continue;
            boolean owned = homeworkRepository.findById(hs.getHomeworkId().trim())
                    .map(hw -> teacherId.equals(hw.getTearcherUserId() != null ? hw.getTearcherUserId().trim() : ""))
                    .orElse(false);
            if (owned) {
                return true;
            }
        }
        return false;
    }

    private boolean teacherManagesStudentClass(SysUser teacher, SysUser student) {
        String classOrgId = student.getUserOrgId() != null ? student.getUserOrgId().trim() : "";
        if (classOrgId.isEmpty()) {
            return false;
        }
        Set<String> classOrgIds = MobileTeacherClassScope.resolveClassOrgIds(teacher, null, sysOrgRepository);
        return classOrgIds.contains(classOrgId);
    }

    public boolean hasSubmittedHomework(String homeworkId, String studentId) {
        // 新系统 exp_homework_student
        List<MobileExpHomeworkStudent> records = homeworkStudentRepository.findByHomeworkId(homeworkId);
        for (MobileExpHomeworkStudent hs : records) {
            if (!StringUtils.hasText(hs.getStudentExpId())) continue;
            Optional<ExpMsg> msg = expMsgRepository.findById(hs.getStudentExpId());
            if (msg.isPresent() && studentId.equals(msg.get().getCreateUserId())) {
                return StringUtils.hasText(hs.getStudentSubmitDate());
            }
        }
        // 旧系统 mb_task_submission
        Optional<MbTaskSubmission> subOpt = mbTaskSubmissionRepository
                .findByTaskIdAndStudentUserId(homeworkId, studentId);
        if (subOpt.isPresent()) {
            MbTaskSubmission sub = subOpt.get();
            return "submitted".equals(sub.getState()) || "reviewed".equals(sub.getState()) || "done".equals(sub.getState());
        }
        return false;
    }

    private List<ExpMsg> loadStudentMsgs(String type) {
        return expMsgRepository.findAll().stream()
                .filter(m -> "student".equals(m.getExpType()))
                .filter(m -> matchExpTaskType(m.getExpTaskType(), type))
                .sorted((a, b) -> cmpTime(b.getCreateTime(), a.getCreateTime()))
                .collect(Collectors.toList());
    }

    private boolean matchExpTaskType(String et, String typeFilter) {
        if (!StringUtils.hasText(typeFilter) || "all".equalsIgnoreCase(typeFilter.trim())) return true;
        String t = typeFilter.trim().toLowerCase();
        if ("remix".equals(t)) return "tk".equals(et);
        if ("creative".equals(t)) return "self".equals(et);
        return "hw".equals(et) || !StringUtils.hasText(et);
    }

    private String mapWorkType(String workType) {
        if ("remix".equals(workType)) return "tk";
        if ("creative".equals(workType)) return "self";
        return "hw";
    }

    private String reverseMapWorkType(String expTaskType) {
        if ("tk".equals(expTaskType)) return "remix";
        if ("self".equals(expTaskType)) return "creative";
        return "homework";
    }

    private String resolveWorkType(CreateWorkRequest request) {
        String raw = StringUtils.hasText(request.getWorkType()) ? request.getWorkType().trim() : "homework";
        return "experiment".equals(raw) ? "homework" : raw;
    }

    private String resolveTaskId(CreateWorkRequest request) {
        if (request == null) return null;
        if (StringUtils.hasText(request.getTaskId())) return request.getTaskId().trim();
        return null;
    }

    private String resolveStudentId(String userId, CreateWorkRequest request) {
        if (request != null && StringUtils.hasText(request.getStudentUserId())) {
            return parentAccessService.resolveStudentScope(userId, request.getStudentUserId());
        }
        return parentAccessService.resolveStudentScope(userId, null);
    }

    private String resolveWorkTitle(CreateWorkRequest request) {
        if (StringUtils.hasText(request.getTitle())) return request.getTitle().trim();
        return "实验成果";
    }

    private void validateWorkFiles(List<CreateWorkFileItem> files) {
        if (files == null || files.isEmpty()) throw new IllegalArgumentException("请至少上传一张照片或视频");
        int images = 0, videos = 0;
        for (CreateWorkFileItem item : files) {
            if (item == null) continue;
            String type = StringUtils.hasText(item.getType()) ? item.getType().trim().toLowerCase() : "image";
            if ("video".equals(type)) videos++; else images++;
            if (!StringUtils.hasText(item.getUrl()) || item.getUrl().trim().startsWith("blob:")) {
                throw new IllegalArgumentException("存在未上传成功的文件，请重新选择");
            }
            try {
                MobileMinioKeySupport.requireStorageKey(minioStorageService, item.getUrl());
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("存在无效的文件地址，请重新上传：" + ex.getMessage());
            }
        }
        if (images > MAX_IMAGES) throw new IllegalArgumentException("最多上传 " + MAX_IMAGES + " 张照片");
        if (videos > MAX_VIDEOS) throw new IllegalArgumentException("最多上传 " + MAX_VIDEOS + " 段视频");
    }

    private void enrichTeacherReview(MobileWorkDetailDto dto, String msgId) {
        List<MobileExpHomeworkStudent> records = homeworkStudentRepository.findByStudentExpId(msgId);
        if (!records.isEmpty()) {
            MobileExpHomeworkStudent hs = records.get(0);
            if (hs.getMarkTime() != null) {
                MobileWorkDetailDto.TeacherReview review = new MobileWorkDetailDto.TeacherReview();
                review.setName(resolveUserName(hs.getMarkUserId()));
                review.setText(hs.getMarkComments());
                review.setStars(resultToStars(hs.getMarkResult()));
                dto.setTeacherReview(review);
                // reviewStatus/Label 由 exp_msg.status 统一决定（已通过/未通过），此处仅补充评语与评分
                dto.setGrade(mapGrade(hs.getMarkResult()));
            }
        }
    }

    private MobileWorkItemDto toWorkItemDto(ExpMsg msg) {
        MobileWorkItemDto item = new MobileWorkItemDto();
        item.setId(msg.getExpId());
        item.setTitle(msg.getExpName());
        item.setAuthor(resolveUserName(msg.getCreateUserId()));
        item.setAuthorInitial(initialOf(item.getAuthor()));
        item.setSourceExpId(resolveLinkExpId(msg));
        enrichWorkItemAuthor(item, msg.getCreateUserId());

        String et = msg.getExpTaskType();
        if ("tk".equals(et)) {
            item.setTint("tint-amber");
            item.setType("remix");
        } else if ("self".equals(et)) {
            item.setTint("tint-cyan");
            item.setType("creative");
        } else {
            item.setTint("tint-violet");
            item.setType("homework");
        }

        applyAuditBadge(item, msg.getStatus());
        if (msg.getCreateTime() != null) item.setTimeLabel(TIME_FMT.format(msg.getCreateTime()));
        return item;
    }

    private void enrichWorkItemFiles(List<MobileWorkItemDto> items) {
        if (items == null || items.isEmpty()) return;
        List<String> expIds = items.stream().map(MobileWorkItemDto::getId).filter(StringUtils::hasText).collect(Collectors.toList());
        List<ExpVideo> allVideos = expVideoRepository.findByExpIdIn(expIds);
        Map<String, List<ExpVideo>> byExp = allVideos.stream().collect(Collectors.groupingBy(ExpVideo::getExpId));
        for (MobileWorkItemDto item : items) {
            List<ExpVideo> videos = byExp.get(item.getId());
            if (videos == null || videos.isEmpty()) continue;
            videos.sort(Comparator.comparing(v -> v.getSortOrder() == null ? 0 : v.getSortOrder()));
            ExpVideo first = videos.get(0);
            item.setFileCount(videos.size());
            String url = first.getVideoUrl();
            if (StringUtils.hasText(url)) {
                item.setCoverUrl(resolveAccessibleUrl(url));
                if (url.toLowerCase().contains(".mp4") || url.toLowerCase().contains(".webm") || url.toLowerCase().contains(".mov")) {
                    item.setCoverType("video");
                }
            }
        }
    }

    private MobileWorkDetailDto buildDetailDto(ExpMsg msg, String studentId, String workType) {
        MobileWorkDetailDto dto = new MobileWorkDetailDto();
        dto.setId(msg.getExpId());
        dto.setTitle(msg.getExpName());
        dto.setDesc(msg.getConfirmComments());
        dto.setAuthor(resolveUserName(studentId));
        dto.setAuthorUserId(studentId);
        enrichAuthorInfo(dto, studentId);
        dto.setLikes(0);
        dto.setComments(0);
        dto.setSourceExpId(resolveLinkExpId(msg));
        dto.setSourceExpName(resolveSourceExpName(dto.getSourceExpId()));
        dto.setWorkType(workType);
        dto.setReviewStatus(MobileWorkAuditStatus.reviewStatusCode(msg.getStatus()));
        dto.setReviewStatusLabel(MobileWorkAuditStatus.reviewStatusLabel(msg.getStatus()));
        if (msg.getCreateTime() != null) dto.setTime(TIME_FMT.format(msg.getCreateTime()));

        List<ExpVideo> videos = expVideoRepository.findByExpIdOrderBySortOrderAsc(msg.getExpId());
        List<MobileWorkFileDto> files = videos.stream().map(this::toFileDto).collect(Collectors.toList());
        dto.setFiles(files);
        dto.setFileCount(files.size());
        if (!files.isEmpty()) {
            MobileWorkFileDto cover = files.get(0);
            dto.setCoverUrl(cover.getPreviewUrl());
            dto.setCoverType(cover.getType());
        }
        return dto;
    }

    private MobileWorkFileDto toFileDto(ExpVideo video) {
        MobileWorkFileDto dto = new MobileWorkFileDto();
        dto.setId(video.getSeqId());
        String url = video.getVideoUrl();
        boolean isVideo = StringUtils.hasText(url) && (url.toLowerCase().contains(".mp4")
                || url.toLowerCase().contains(".webm") || url.toLowerCase().contains(".mov"));
        dto.setType(isVideo ? "video" : "image");
        dto.setUrl(url);
        dto.setPreviewUrl(resolveAccessibleUrl(url));
        dto.setSortOrder(video.getSortOrder() != null ? video.getSortOrder() : 0);
        if (StringUtils.hasText(video.getFileId())) {
            dataFileRepository.findById(video.getFileId().trim())
                    .ifPresent(df -> dto.setName(df.getFileName()));
        }
        return dto;
    }

    public MobileTaskDto toTaskDto(ExpMsg msg) {
        MobileTaskDto dto = new MobileTaskDto();
        dto.setId(msg.getExpId());
        dto.setType("student");
        dto.setTitle(msg.getExpName());
        boolean draft = "draft".equals(msg.getStatus());
        dto.setState(draft ? "pending" : "done");
        dto.setStateLabel(draft ? "待完成" : "已完成");
        dto.setBadgeClass(draft ? "badge-warning" : "badge-success");

        String et = msg.getExpTaskType();
        if ("tk".equals(et)) {
            dto.setType("remix");
        } else if ("self".equals(et)) {
            dto.setType("creative");
        }
        dto.setUploadQuery("expId=" + msg.getExpId());
        return dto;
    }

    private void enrichAuthorInfo(MobileWorkDetailDto dto, String userId) {
        if (dto == null || !StringUtils.hasText(userId)) {
            return;
        }
        dto.setAuthorRole("student");
        MobileStudentOrgSupport.StudentOrgContext ctx = studentOrgSupport.resolve(userId.trim());
        if (StringUtils.hasText(ctx.getClassName())) {
            dto.setClassName(ctx.getClassName());
        } else {
            dto.setClassName(resolveOrgName(resolveUserOrgId(userId)));
        }
        if (StringUtils.hasText(ctx.getSchoolName())) {
            dto.setSchoolName(ctx.getSchoolName());
        }
        if (StringUtils.hasText(ctx.getGradeName())) {
            dto.setGrade(ctx.getGradeName());
        }
        sysUserRepository.findById(userId.trim())
                .map(u -> MobileAvatarSupport.resolveUserAvatarUrl(minioStorageService, u))
                .ifPresent(dto::setAuthorAvatarUrl);
    }

    private void enrichWorkItemAuthor(MobileWorkItemDto item, String userId) {
        if (item == null || !StringUtils.hasText(userId)) {
            return;
        }
        MobileStudentOrgSupport.StudentOrgContext ctx = studentOrgSupport.resolve(userId.trim());
        if (StringUtils.hasText(ctx.getClassName())) {
            item.setClassName(ctx.getClassName());
        }
        if (StringUtils.hasText(ctx.getSchoolName())) {
            item.setSchool(ctx.getSchoolName());
        }
        sysUserRepository.findById(userId.trim())
                .map(u -> MobileAvatarSupport.resolveUserAvatarUrl(minioStorageService, u))
                .ifPresent(item::setAuthorAvatarUrl);
    }

    private String resolveOrgName(String orgId) {
        if (!StringUtils.hasText(orgId)) return null;
        return sysOrgRepository.findById(orgId).map(o -> o.getOrgName()).orElse(orgId);
    }

    private String resolveUserOrgId(String userId) {
        if (!StringUtils.hasText(userId)) return null;
        return sysUserRepository.findById(userId.trim())
                .map(u -> u.getUserOrgId()).filter(StringUtils::hasText).orElse(null);
    }

    private String resolveExpName(String expId) {
        if (!StringUtils.hasText(expId)) return "实验作业";
        return expMsgRepository.findById(expId).map(ExpMsg::getExpName).filter(StringUtils::hasText).orElse("实验作业");
    }

    private String resolveUserName(String userId) {
        if (!StringUtils.hasText(userId)) return "同学";
        return sysUserRepository.findById(userId)
                .map(u -> StringUtils.hasText(u.getUserNickName()) ? u.getUserNickName() : u.getUserName())
                .orElse("同学");
    }

    private String initialOf(String name) {
        return StringUtils.hasText(name) ? name.substring(0, 1) : "同";
    }

    private String resolveAccessibleUrl(String fileUrl) {
        return MobileMediaUrlSupport.resolve(minioStorageService, fileUrl);
    }

    private static int resultToStars(String result) {
        if (!StringUtils.hasText(result)) return 3;
        switch (result.trim().toLowerCase()) {
            case "excellent": return 5;
            case "good": return 4;
            case "pass": return 3;
            case "fail": return 1;
            default: return 3;
        }
    }

    private static String mapGrade(String markResult) {
        if (!StringUtils.hasText(markResult)) return null;
        String r = markResult.trim().toLowerCase();
        if ("excellent".equals(r)) return "excellent";
        if ("good".equals(r)) return "good";
        if ("pass".equals(r)) return "pass";
        if ("fail".equals(r)) return "fail";
        return null;
    }

    private String truncate(String value, int maxLen) {
        if (!StringUtils.hasText(value)) return value;
        String t = value.trim();
        return t.length() <= maxLen ? t : t.substring(0, maxLen);
    }

    private static int cmpTime(Date a, Date b) {
        if (a == null && b == null) return 0;
        if (a == null) return -1;
        if (b == null) return 1;
        return a.compareTo(b);
    }

    private String resolveSourceExpName(String linkExpId) {
        if (!StringUtils.hasText(linkExpId)) return null;
        return expMsgRepository.findById(linkExpId.trim())
                .map(ExpMsg::getExpName)
                .orElse(null);
    }

    private String resolveLinkExpId(ExpMsg msg) {
        if (msg == null) {
            return null;
        }
        if (StringUtils.hasText(msg.getLinkExpId())) {
            return msg.getLinkExpId().trim();
        }
        List<MobileExpHomeworkStudent> records = homeworkStudentRepository.findByStudentExpId(msg.getExpId());
        for (MobileExpHomeworkStudent record : records) {
            if (StringUtils.hasText(record.getTeacherExpId())) {
                return record.getTeacherExpId().trim();
            }
        }
        return null;
    }

    private String resolveTeacherExpIdFromTask(String taskId) {
        if (!StringUtils.hasText(taskId)) {
            return null;
        }
        Optional<MobileExpHomework> homework = homeworkRepository.findById(taskId.trim());
        if (homework.isPresent() && StringUtils.hasText(homework.get().getTeacherExpId())) {
            return homework.get().getTeacherExpId().trim();
        }
        Optional<MbTask> task = mbTaskRepository.findById(taskId.trim());
        if (task.isPresent() && StringUtils.hasText(task.get().getVideoId())) {
            return task.get().getVideoId().trim();
        }
        return null;
    }

    private <T> PageResult<T> paginate(List<T> all, int page, int size) {
        int safeSize = Math.max(size, 1);
        int from = Math.max(page - 1, 0) * safeSize;
        if (from >= all.size()) return new PageResult<>(all.size(), new ArrayList<>());
        int to = Math.min(from + safeSize, all.size());
        return new PageResult<>(all.size(), all.subList(from, to));
    }
}
