package com.xuanyue.exp.mobile.service;

import com.xuanyue.exp.common.PageResult;
import com.xuanyue.exp.mobile.dto.CreateWorkFileItem;
import com.xuanyue.exp.mobile.dto.CreateWorkRequest;
import com.xuanyue.exp.mobile.dto.MobileWorkDetailDto;
import com.xuanyue.exp.mobile.dto.MobileWorkItemDto;
import com.xuanyue.exp.mobile.entity.MbTask;
import com.xuanyue.exp.mobile.entity.MbTaskSubmission;
import com.xuanyue.exp.mobile.entity.MbWork;
import com.xuanyue.exp.mobile.entity.MbWorkFile;
import com.xuanyue.exp.mobile.repository.MbTaskRepository;
import com.xuanyue.exp.mobile.repository.MbTaskSubmissionRepository;
import com.xuanyue.exp.mobile.repository.MbWorkFileRepository;
import com.xuanyue.exp.mobile.repository.MbWorkRepository;
import com.xuanyue.exp.mobile.support.MobileEntityMapper;
import com.xuanyue.exp.mobile.support.MobileIds;
import com.xuanyue.exp.mobile.support.MobileUserContext;
import com.xuanyue.exp.mobile.support.MobileWorkExpResolver;
import com.xuanyue.exp.mobile.support.MobileWorkReviewSupport;
import com.xuanyue.exp.mobile.support.MobileParentAccessService;
import com.xuanyue.exp.system.entity.SysUser;
import com.xuanyue.exp.system.entity.SysOrg;
import com.xuanyue.exp.system.repository.SysOrgRepository;
import com.xuanyue.exp.system.repository.SysUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.text.SimpleDateFormat;
import java.util.stream.Collectors;

@Service
public class MobileWorkService {

    private static final SimpleDateFormat TIME_FMT = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    private final MbWorkRepository workRepository;
    private final MbWorkFileRepository workFileRepository;
    private final MbTaskRepository taskRepository;
    private final MbTaskSubmissionRepository submissionRepository;
    private final SysUserRepository sysUserRepository;
    private final SysOrgRepository sysOrgRepository;
    private final MobileWorkExpResolver workExpResolver;
    private final MobilePointsService pointsService;
    private final MobileBadgeGrantService badgeGrantService;
    private final MobileGrowthEventService growthEventService;
    private final MobileParentAccessService parentAccessService;

    public MobileWorkService(MbWorkRepository workRepository,
                             MbWorkFileRepository workFileRepository,
                             MbTaskRepository taskRepository,
                             MbTaskSubmissionRepository submissionRepository,
                             SysUserRepository sysUserRepository,
                             SysOrgRepository sysOrgRepository,
                             MobileWorkExpResolver workExpResolver,
                             MobilePointsService pointsService,
                             MobileBadgeGrantService badgeGrantService,
                             MobileGrowthEventService growthEventService,
                             MobileParentAccessService parentAccessService) {
        this.workRepository = workRepository;
        this.workFileRepository = workFileRepository;
        this.taskRepository = taskRepository;
        this.submissionRepository = submissionRepository;
        this.sysUserRepository = sysUserRepository;
        this.sysOrgRepository = sysOrgRepository;
        this.workExpResolver = workExpResolver;
        this.pointsService = pointsService;
        this.badgeGrantService = badgeGrantService;
        this.growthEventService = growthEventService;
        this.parentAccessService = parentAccessService;
    }

    public PageResult<MobileWorkItemDto> list(String type, int page, int size) {
        return listPublic(type, page, size);
    }

    /** 公开展示：仅已通过审核的有效作品 */
    public PageResult<MobileWorkItemDto> listPublic(String type, int page, int size) {
        List<MbWork> works;
        if (StringUtils.hasText(type) && !"all".equalsIgnoreCase(type.trim())) {
            works = workRepository.findByStatusAndWorkTypeOrderByCreateTimeDesc("y", type.trim());
        } else {
            works = workRepository.findByStatusOrderByCreateTimeDesc("y");
        }
        List<MobileWorkItemDto> items = works.stream()
                .filter(w -> MobileWorkReviewSupport.isPubliclyVisible(w.getStatus(), w.getReviewStatus()))
                .map(this::toWorkItemDto)
                .collect(Collectors.toList());
        return paginate(items, page, size);
    }

    /** 个人作品墙：当前用户全部上传（含待审核、草稿） */
    public PageResult<MobileWorkItemDto> listMine(String userId, String type, String reviewStatus, int page, int size) {
        String studentId = requireStudentId(userId);
        List<MbWork> works;
        if (StringUtils.hasText(type) && !"all".equalsIgnoreCase(type.trim())) {
            works = workRepository.findByStudentUserIdAndWorkTypeOrderByCreateTimeDesc(studentId, type.trim());
        } else {
            works = workRepository.findByStudentUserIdOrderByCreateTimeDesc(studentId);
        }
        List<MobileWorkItemDto> items = works.stream()
                .filter(w -> !"n".equals(w.getStatus()))
                .filter(w -> MobileWorkReviewSupport.matchesReviewFilter(w.getStatus(), w.getReviewStatus(), reviewStatus))
                .map(this::toWorkItemDto)
                .collect(Collectors.toList());
        return paginate(items, page, size);
    }

    private MobileWorkItemDto toWorkItemDto(MbWork work) {
        MobileWorkItemDto dto = MobileEntityMapper.toWorkItem(
                work, resolveAuthorName(work.getStudentUserId()), resolveInitial(work.getStudentUserId()));
        dto.setSourceExpId(workExpResolver.resolve(work));
        enrichReviewFields(work, dto);
        return dto;
    }

    private void enrichReviewFields(MbWork work, MobileWorkItemDto dto) {
        dto.setReviewStatus(MobileWorkReviewSupport.resolveDisplayStatus(work.getStatus(), work.getReviewStatus()));
        dto.setReviewStatusLabel(MobileWorkReviewSupport.resolveLabel(work.getStatus(), work.getReviewStatus()));
        dto.setReviewBadgeClass(MobileWorkReviewSupport.resolveBadgeClass(work.getStatus(), work.getReviewStatus()));
        dto.setCanEdit(MobileWorkReviewSupport.isDraft(work.getStatus(), work.getReviewStatus()));
        if (work.getCreateTime() != null) {
            dto.setTimeLabel(TIME_FMT.format(work.getCreateTime()));
        }
    }

    private String resolveWorkStudentId(String userId, CreateWorkRequest request) {
        if (request != null && StringUtils.hasText(request.getStudentUserId())) {
            return parentAccessService.resolveStudentScope(userId, request.getStudentUserId());
        }
        return parentAccessService.resolveStudentScope(userId, null);
    }

    private String requireStudentId(String userId) {
        String studentId = MobileUserContext.resolveStudentId(userId);
        if (!StringUtils.hasText(studentId)) {
            throw new IllegalArgumentException("请先登录");
        }
        return studentId.trim();
    }

    public MobileWorkDetailDto getDetail(String workId) {
        Optional<MbWork> workOpt = workRepository.findById(workId);
        if (workOpt.isPresent()) {
            MbWork work = workOpt.get();
            MobileWorkDetailDto dto = MobileEntityMapper.toWorkDetail(work, resolveAuthorName(work.getStudentUserId()));
            dto.setSourceExpId(workExpResolver.resolve(work));
            return dto;
        }
        return null;
    }

    public String resolveExpIdForWork(MbWork work) {
        return workExpResolver.resolve(work);
    }

    public Optional<MbWork> findWork(String workId) {
        return workRepository.findById(workId);
    }

    public void persistSourceExpId(MbWork work, String expId) {
        if (work == null || !StringUtils.hasText(expId) || StringUtils.hasText(work.getSourceExpId())) {
            return;
        }
        work.setSourceExpId(expId.trim());
        workRepository.save(work);
    }

    private MbTaskSubmission createSubmission(String taskId, String studentId, Date now) {
        MbTaskSubmission submission = new MbTaskSubmission();
        submission.setSubmissionId(MobileIds.newId("sub"));
        submission.setTaskId(taskId);
        submission.setStudentUserId(studentId);
        submission.setState("pending");
        submission.setStateLabel("待完成");
        submission.setBadgeClass("badge-warning");
        submission.setCreateTime(now);
        submission.setUpdateTime(now);
        return submission;
    }

    public MobileWorkDetailDto createWork(String userId, CreateWorkRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("请求不能为空");
        }
        String studentId = resolveWorkStudentId(userId, request);
        String workType = StringUtils.hasText(request.getWorkType()) ? request.getWorkType().trim() : "homework";
        String title = resolveWorkTitle(request);
        String taskId = resolveTaskId(request);
        String sourceExpId = resolveSourceExpIdForCreate(request, taskId);

        MbWork work = new MbWork();
        work.setWorkId(MobileIds.newId("work"));
        work.setStudentUserId(studentId);
        work.setTitle(title);
        work.setDescription(StringUtils.hasText(request.getDescription()) ? request.getDescription().trim() : null);
        work.setWorkType(workType);
        work.setTaskId(taskId);
        work.setSourceExpId(sourceExpId);
        work.setReviewStatus("remix".equals(workType) || "creative".equals(workType) ? "published" : "pending");
        work.setLikeCount(0);
        work.setCommentCount(0);
        applyStudentOrgDefaults(work, studentId, request);
        work.setTint(defaultTint(workType));
        work.setStatus("y");
        work.setCreateTime(new Date());

        if (StringUtils.hasText(taskId)) {
            Date now = new Date();
            Optional<MbTaskSubmission> submissionOpt = submissionRepository.findByTaskIdAndStudentUserId(taskId, studentId);
            MbTaskSubmission submission = submissionOpt.orElseGet(() -> createSubmission(taskId, studentId, now));
            work.setSubmissionId(submission.getSubmissionId());
            if ("remix".equals(workType) || "creative".equals(workType)) {
                submission.setState("done");
                submission.setStateLabel("已完成");
                submission.setBadgeClass("badge-success");
            } else {
                submission.setState("submitted");
                submission.setStateLabel("已提交");
                submission.setBadgeClass("badge-info");
            }
            submission.setSubmitTime(now);
            submission.setUpdateTime(now);
            submissionRepository.save(submission);
        }

        workRepository.save(work);
        saveWorkFiles(work.getWorkId(), request.getFiles());

        int workPoints = pointsService.credit(studentId, MobilePointsService.WORK_SUBMIT_POINTS,
                "work", work.getWorkId(), "提交作品") ? MobilePointsService.WORK_SUBMIT_POINTS : 0;
        growthEventService.onWorkSubmitted(studentId, work, workPoints);
        badgeGrantService.onWorkSubmitted(studentId);

        if (StringUtils.hasText(work.getSubmissionId())) {
            MbTaskSubmission linked = submissionRepository.findById(work.getSubmissionId()).orElse(null);
            if (linked != null && isTaskCompleteState(linked.getState())) {
                int taskPoints = pointsService.credit(studentId, MobilePointsService.TASK_COMPLETE_POINTS,
                        "task", linked.getSubmissionId(), "完成任务") ? MobilePointsService.TASK_COMPLETE_POINTS : 0;
                badgeGrantService.onTaskCompleted(studentId);
                MbTask task = StringUtils.hasText(work.getTaskId())
                        ? taskRepository.findById(work.getTaskId()).orElse(null) : null;
                growthEventService.onTaskCompleted(studentId, linked, task, taskPoints);
            }
        }

        MobileWorkDetailDto dto = MobileEntityMapper.toWorkDetail(work, resolveAuthorName(studentId));
        dto.setSourceExpId(workExpResolver.resolve(work));
        return dto;
    }

    /** 教师批阅作品 */
    public void reviewWork(String teacherUserId, String workId, String rating, String comment, Boolean featured) {
        if (!StringUtils.hasText(workId)) {
            throw new IllegalArgumentException("作品 id 不能为空");
        }
        if (!StringUtils.hasText(rating)) {
            throw new IllegalArgumentException("请选择评级");
        }
        MbWork work = workRepository.findById(workId.trim())
                .orElseThrow(() -> new IllegalArgumentException("作品不存在"));
        if (!"y".equals(work.getStatus())) {
            throw new IllegalArgumentException("作品不可批阅");
        }
        String teacherName = resolveAuthorName(teacherUserId);
        work.setTeacherReviewName(teacherName);
        work.setTeacherReviewText(StringUtils.hasText(comment) ? comment.trim() : null);
        work.setGrade(rating.trim().toLowerCase());
        work.setTeacherReviewStars(ratingToStars(rating));

        boolean showFeatured = Boolean.TRUE.equals(featured) || "excellent".equalsIgnoreCase(rating.trim());
        if ("fail".equalsIgnoreCase(rating.trim())) {
            work.setReviewStatus("rejected");
        } else {
            work.setReviewStatus("reviewed");
        }
        workRepository.save(work);

        String studentId = work.getStudentUserId();
        if (!"fail".equalsIgnoreCase(rating.trim()) && StringUtils.hasText(studentId)) {
            growthEventService.onWorkReviewed(studentId, work);
        }

        if (StringUtils.hasText(work.getSubmissionId())) {
            submissionRepository.findById(work.getSubmissionId()).ifPresent(sub -> {
                sub.setGrade(rating.trim().toLowerCase());
                sub.setReviewComment(work.getTeacherReviewText());
                sub.setReviewerUserId(teacherUserId);
                sub.setReviewTime(new Date());
                sub.setState("reviewed");
                sub.setStateLabel("已批阅");
                sub.setBadgeClass("badge-success");
                sub.setUpdateTime(new Date());
                submissionRepository.save(sub);
            });
        }

        if (showFeatured && !"fail".equalsIgnoreCase(rating.trim())) {
            markWorkFeatured(workId, true);
        }
    }

    private int ratingToStars(String rating) {
        if (!StringUtils.hasText(rating)) {
            return 3;
        }
        switch (rating.trim().toLowerCase()) {
            case "excellent":
                return 5;
            case "good":
                return 4;
            case "pass":
                return 3;
            case "fail":
                return 1;
            default:
                return 3;
        }
    }

    /** 教师批阅时将作品标记为展示，触发 featured 积分与勋章 */
    public void markWorkFeatured(String workId, boolean featured) {
        if (!StringUtils.hasText(workId)) {
            throw new IllegalArgumentException("作品 id 不能为空");
        }
        MbWork work = workRepository.findById(workId.trim())
                .orElseThrow(() -> new IllegalArgumentException("作品不存在"));
        boolean wasFeatured = "y".equalsIgnoreCase(work.getIsFeatured());
        work.setIsFeatured(featured ? "y" : "n");
        workRepository.save(work);
        if (featured && !wasFeatured) {
            String studentId = work.getStudentUserId();
            int featuredPoints = pointsService.credit(studentId, MobilePointsService.WORK_FEATURED_POINTS,
                    "featured", work.getWorkId(), "作品被教师展示")
                    ? MobilePointsService.WORK_FEATURED_POINTS : 0;
            growthEventService.onWorkFeatured(studentId, work, featuredPoints);
            badgeGrantService.onWorkFeatured(studentId);
        }
    }

    private boolean isTaskCompleteState(String state) {
        if (!StringUtils.hasText(state)) {
            return false;
        }
        String s = state.trim().toLowerCase();
        return "done".equals(s) || "submitted".equals(s) || "reviewed".equals(s);
    }

    private String resolveSourceExpIdForCreate(CreateWorkRequest request, String taskId) {
        if (request != null && StringUtils.hasText(request.getSourceExpId())) {
            return request.getSourceExpId().trim();
        }
        String fromTask = workExpResolver.resolveFromTaskId(taskId);
        if (StringUtils.hasText(fromTask)) {
            return fromTask;
        }
        if (request != null && StringUtils.hasText(request.getTitle())) {
            return workExpResolver.matchExpIdByTitle(request.getTitle());
        }
        return null;
    }

    private String resolveWorkTitle(CreateWorkRequest request) {
        if (StringUtils.hasText(request.getTitle())) {
            return request.getTitle().trim();
        }
        if (StringUtils.hasText(request.getRelatedTaskTitle())) {
            return request.getRelatedTaskTitle().trim();
        }
        return "实验成果";
    }

    private String resolveTaskId(CreateWorkRequest request) {
        if (StringUtils.hasText(request.getTaskId())) {
            return request.getTaskId().trim();
        }
        if (StringUtils.hasText(request.getRelatedTaskTitle())) {
            Optional<MbTask> task = taskRepository.findFirstByTitleAndStatus(request.getRelatedTaskTitle().trim(), "y");
            if (task.isPresent()) {
                return task.get().getTaskId();
            }
        }
        return null;
    }

    private void applyStudentOrgDefaults(MbWork work, String studentId, CreateWorkRequest request) {
        if (StringUtils.hasText(request.getClassName())) {
            work.setClassName(request.getClassName().trim());
        }
        if (StringUtils.hasText(request.getSchoolName())) {
            work.setSchoolName(request.getSchoolName().trim());
        }
        sysUserRepository.findById(studentId).ifPresent(user -> {
            if (!StringUtils.hasText(work.getSchoolName()) && StringUtils.hasText(user.getRootOrgId())) {
                sysOrgRepository.findById(user.getRootOrgId())
                        .map(SysOrg::getOrgName)
                        .filter(StringUtils::hasText)
                        .ifPresent(work::setSchoolName);
            }
            if (!StringUtils.hasText(work.getClassName()) && StringUtils.hasText(user.getUserOrgId())) {
                sysOrgRepository.findById(user.getUserOrgId())
                        .map(SysOrg::getOrgName)
                        .filter(StringUtils::hasText)
                        .ifPresent(work::setClassName);
            }
        });
        if (!StringUtils.hasText(work.getClassName())) {
            work.setClassName("");
        }
        if (!StringUtils.hasText(work.getSchoolName())) {
            work.setSchoolName("");
        }
    }

    private String defaultTint(String workType) {
        if ("remix".equals(workType)) {
            return "tint-amber";
        }
        if ("creative".equals(workType)) {
            return "tint-cyan";
        }
        return "tint-violet";
    }

    private void saveWorkFiles(String workId, List<CreateWorkFileItem> files) {
        if (files == null || files.isEmpty()) {
            return;
        }
        Date now = new Date();
        for (int i = 0; i < files.size(); i++) {
            CreateWorkFileItem item = files.get(i);
            if (item == null) {
                continue;
            }
            MbWorkFile file = new MbWorkFile();
            file.setFileId(MobileIds.newId("wf"));
            file.setWorkId(workId);
            file.setFileType(StringUtils.hasText(item.getType()) ? item.getType().trim() : "image");
            file.setFileName(item.getName());
            file.setFileSize(item.getSize());
            file.setFileUrl(item.getUrl());
            file.setGradClass(item.getGrad());
            file.setIconEmoji(item.getIcon());
            file.setDuration(item.getDuration());
            file.setSortOrder(i);
            file.setCreateTime(now);
            workFileRepository.save(file);
        }
    }

    private String resolveAuthorName(String userId) {
        if (!StringUtils.hasText(userId)) {
            return "同学";
        }
        Optional<SysUser> user = sysUserRepository.findById(userId);
        if (user.isPresent()) {
            SysUser u = user.get();
            return u.getUserNickName() != null ? u.getUserNickName() : u.getUserName();
        }
        return "同学";
    }

    private String resolveInitial(String userId) {
        String name = resolveAuthorName(userId);
        return name.isEmpty() ? "同" : name.substring(0, 1);
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
