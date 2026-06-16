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
import com.xuanyue.exp.mobile.repository.MobileExpHomeworkRepository;
import com.xuanyue.exp.mobile.repository.MobileExpHomeworkStudentRepository;
import com.xuanyue.exp.mobile.support.MobileHomeCache;
import com.xuanyue.exp.mobile.support.MobileIds;
import com.xuanyue.exp.mobile.support.MobileMediaUrlSupport;
import com.xuanyue.exp.mobile.support.MobileMinioKeySupport;
import com.xuanyue.exp.mobile.support.MobileParentAccessService;
import com.xuanyue.exp.mobile.support.MobileUserContext;
import com.xuanyue.exp.system.repository.SysOrgRepository;
import com.xuanyue.exp.system.repository.SysUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 学生作品核心服务（v2 版）
 *
 * 数据模型：
 * - exp_msg (exp_type='student') → 所有学生作品的统一容器
 * - exp_homework → 作业布置
 * - exp_homework_student → 作业提交与批阅流程
 * - data_file + exp_video → 媒体文件存储
 *
 * 原则：零表变更，不改管理端代码，所有新增代码在 mobile/ 包内。
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
    private final SysUserRepository sysUserRepository;
    private final SysOrgRepository sysOrgRepository;
    private final MobilePointsService pointsService;
    private final MobileBadgeGrantService badgeGrantService;
    private final MobileNotificationService notificationService;
    private final MobileParentAccessService parentAccessService;
    private final MobileHomeCache homeCache;
    private final MinioStorageService minioStorageService;

    public MobileStudentWorkService(ExpMsgRepository expMsgRepository,
                                    ExpVideoRepository expVideoRepository,
                                    DataFileRepository dataFileRepository,
                                    MobileExpHomeworkRepository homeworkRepository,
                                    MobileExpHomeworkStudentRepository homeworkStudentRepository,
                                    SysUserRepository sysUserRepository,
                                    SysOrgRepository sysOrgRepository,
                                    MobilePointsService pointsService,
                                    MobileBadgeGrantService badgeGrantService,
                                    MobileNotificationService notificationService,
                                    MobileParentAccessService parentAccessService,
                                    MobileHomeCache homeCache,
                                    MinioStorageService minioStorageService) {
        this.expMsgRepository = expMsgRepository;
        this.expVideoRepository = expVideoRepository;
        this.dataFileRepository = dataFileRepository;
        this.homeworkRepository = homeworkRepository;
        this.homeworkStudentRepository = homeworkStudentRepository;
        this.sysUserRepository = sysUserRepository;
        this.sysOrgRepository = sysOrgRepository;
        this.pointsService = pointsService;
        this.badgeGrantService = badgeGrantService;
        this.notificationService = notificationService;
        this.parentAccessService = parentAccessService;
        this.homeCache = homeCache;
        this.minioStorageService = minioStorageService;
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

        // 发送布置作业通知给学生
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
        msg.setCreateUserId(studentId);
        msg.setStatus("y");
        msg.setLikeNum(0);
        msg.setCollectionNum(0);
        msg.setEvaluateNum(0);
        msg.setCreateTime(now);
        msg.setUpdateTime(now);
        if (StringUtils.hasText(request.getSourceExpId())) {
            msg.setLinkExpId(request.getSourceExpId().trim());
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

    private void saveWorkMedia(String expId, List<CreateWorkFileItem> files, Date now) {
        if (files == null || files.isEmpty()) return;
        for (int i = 0; i < files.size(); i++) {
            CreateWorkFileItem item = files.get(i);
            if (item == null || !StringUtils.hasText(item.getUrl())) continue;

            DataFile df = new DataFile();
            df.setFileId(MobileIds.newId("df"));
            df.setFileName(truncate(item.getName(), 200));
            df.setFileUrl(truncate(MobileMinioKeySupport.normalizeForStorage(minioStorageService, item.getUrl()), 500));
            df.setFileTypeId("video".equalsIgnoreCase(item.getType()) ? "video" : "image");
            df.setStatus("y");
            df.setCreateTime(now);
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
        MobileExpHomework hw = homeworkRepository.findById(homeworkId)
                .orElseThrow(() -> new IllegalArgumentException("作业不存在"));

        // 查找该学生是否已有此作业的记录（通过学⽣的 exp_msg 来判定）
        List<MobileExpHomeworkStudent> existing = homeworkStudentRepository.findByHomeworkId(homeworkId);
        // 因 exp_homework_student 无 student_user_id 列，通过 studentExpId 回溯 createUserId
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
    }

    /* ════════════════════════════════════════════
       3. 教师批阅 → exp_homework_student.mark_*
       ════════════════════════════════════════════ */

    @Transactional
    public void reviewWork(String teacherUserId, String msgId, String result, String comment) {
        ExpMsg msg = expMsgRepository.findById(msgId)
                .orElseThrow(() -> new IllegalArgumentException("作品不存在"));

        List<MobileExpHomeworkStudent> records = homeworkStudentRepository.findByStudentExpId(msgId);
        for (MobileExpHomeworkStudent hs : records) {
            hs.setMarkUserId(teacherUserId);
            hs.setMarkTime(new Date());
            hs.setMarkComments(StringUtils.hasText(comment) ? comment.trim() : null);
            hs.setMarkResult(StringUtils.hasText(result) ? result.trim().toLowerCase() : "pass");
            homeworkStudentRepository.save(hs);
        }

        String studentId = msg.getCreateUserId();
        if (StringUtils.hasText(studentId)) {
            pointsService.credit(studentId, MobilePointsService.WORK_FEATURED_POINTS,
                    "reviewed", msgId, "作品被批阅");
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

        // 检查进行中的草稿
        Optional<ExpMsg> inProgress = expMsgRepository
                .findTopByCreateUserIdAndStatusAndExpTypeOrderByCreateTimeDesc(studentId, "draft", "student");
        if (inProgress.isPresent() && expTaskType.equals(inProgress.get().getExpTaskType())) {
            return toTaskDto(inProgress.get());
        }

        // 创建草稿
        ExpMsg msg = new ExpMsg();
        msg.setExpId(MobileIds.newId("exp"));
        msg.setExpName("remix".equals(taskType) ? "拍同款" : "创意实验");
        msg.setExpType("student");
        msg.setExpTaskType(expTaskType);
        msg.setCreateUserId(studentId);
        msg.setStatus("draft");
        msg.setLikeNum(0);
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
        return paginate(items, page, size);
    }

    @Transactional(readOnly = true)
    public PageResult<MobileWorkItemDto> listMyWorks(String userId, String type, int page, int size) {
        String studentId = MobileUserContext.resolveStudentId(userId);
        List<ExpMsg> msgs = expMsgRepository.findAll().stream()
                .filter(m -> "student".equals(m.getExpType()))
                .filter(m -> "y".equals(m.getStatus()) || "draft".equals(m.getStatus()))
                .filter(m -> studentId.equals(m.getCreateUserId()))
                .filter(m -> matchExpTaskType(m.getExpTaskType(), type))
                .sorted((a, b) -> cmpTime(b.getCreateTime(), a.getCreateTime()))
                .collect(Collectors.toList());

        List<MobileWorkItemDto> items = msgs.stream()
                .map(this::toWorkItemDto)
                .collect(Collectors.toList());
        enrichWorkItemFiles(items);
        return paginate(items, page, size);
    }

    @Transactional(readOnly = true)
    public MobileWorkDetailDto getWorkDetail(String msgId, String viewerUserId) {
        ExpMsg msg = expMsgRepository.findById(msgId).orElse(null);
        if (msg == null || !"y".equals(msg.getStatus())) return null;

        MobileWorkDetailDto dto = new MobileWorkDetailDto();
        dto.setId(msg.getExpId());
        dto.setTitle(msg.getExpName());
        dto.setDesc(msg.getConfirmComments());
        dto.setLikes(msg.getLikeNum() != null ? msg.getLikeNum() : 0);
        dto.setComments(msg.getEvaluateNum() != null ? msg.getEvaluateNum() : 0);
        dto.setAuthor(resolveUserName(msg.getCreateUserId()));
        dto.setSourceExpId(msg.getLinkExpId());
        dto.setReviewStatus("pending");
        dto.setReviewStatusLabel("待批阅");
        if (msg.getCreateTime() != null) dto.setTime(TIME_FMT.format(msg.getCreateTime()));

        // 批阅信息（如果有批阅记录会覆盖 status 和 label）
        enrichTeacherReview(dto, msg.getExpId());

        // 媒体
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

    /**
     * 按类型加载学生作品 (exp_type='student')
     * 公开给 MobileWorkService 复用
     */
    public List<ExpMsg> loadUserMsgs(String studentId, String typeFilter) {
        return expMsgRepository.findAll().stream()
                .filter(m -> "student".equals(m.getExpType()))
                .filter(m -> "y".equals(m.getStatus()) || "draft".equals(m.getStatus()))
                .filter(m -> studentId.equals(m.getCreateUserId()))
                .filter(m -> matchExpTaskType(m.getExpTaskType(), typeFilter))
                .sorted((a, b) -> cmpTime(b.getCreateTime(), a.getCreateTime()))
                .collect(Collectors.toList());
    }

    /**
     * 检查学生是否已提交某个作业
     */
    public boolean hasSubmittedHomework(String homeworkId, String studentId) {
        List<MobileExpHomeworkStudent> records = homeworkStudentRepository.findByHomeworkId(homeworkId);
        for (MobileExpHomeworkStudent hs : records) {
            if (!StringUtils.hasText(hs.getStudentExpId())) continue;
            Optional<ExpMsg> msg = expMsgRepository.findById(hs.getStudentExpId());
            if (msg.isPresent() && studentId.equals(msg.get().getCreateUserId())) {
                return StringUtils.hasText(hs.getStudentSubmitDate());
            }
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
        return "hw".equals(et);
    }

    private String mapWorkType(String workType) {
        if ("remix".equals(workType)) return "tk";
        if ("creative".equals(workType)) return "self";
        return "hw";
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
                dto.setReviewStatus("reviewed");
                dto.setReviewStatusLabel("已批阅");
            }
        }
    }

    private MobileWorkItemDto toWorkItemDto(ExpMsg msg) {
        MobileWorkItemDto item = new MobileWorkItemDto();
        item.setId(msg.getExpId());
        item.setTitle(msg.getExpName());
        item.setAuthor(resolveUserName(msg.getCreateUserId()));
        item.setAuthorInitial(initialOf(item.getAuthor()));
        item.setSourceExpId(msg.getLinkExpId());

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

        // 查批阅状态
        List<MobileExpHomeworkStudent> records = homeworkStudentRepository.findByStudentExpId(msg.getExpId());
        if (!records.isEmpty() && records.get(0).getMarkTime() != null) {
            item.setReviewStatus("reviewed");
            item.setReviewStatusLabel("已批阅");
            item.setReviewBadgeClass("badge-success");
        } else if ("draft".equals(msg.getStatus())) {
            item.setReviewStatus("pending");
            item.setReviewStatusLabel("待完成");
            item.setReviewBadgeClass("badge-warning");
            item.setCanEdit(true);
        } else {
            item.setReviewStatus("pending");
            item.setReviewStatusLabel("待批阅");
            item.setReviewBadgeClass("badge-info");
        }
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
        dto.setLikes(0);
        dto.setComments(0);
        dto.setSourceExpId(msg.getLinkExpId());
        dto.setWorkType(workType);
        dto.setReviewStatus("pending");
        dto.setReviewStatusLabel("待批阅");
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

    private String resolveOrgName(String orgId) {
        if (!StringUtils.hasText(orgId)) return null;
        return sysOrgRepository.findById(orgId).map(o -> o.getOrgName()).orElse(orgId);
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

    private <T> PageResult<T> paginate(List<T> all, int page, int size) {
        int safeSize = Math.max(size, 1);
        int from = Math.max(page - 1, 0) * safeSize;
        if (from >= all.size()) return new PageResult<>(all.size(), new ArrayList<>());
        int to = Math.min(from + safeSize, all.size());
        return new PageResult<>(all.size(), all.subList(from, to));
    }
}
