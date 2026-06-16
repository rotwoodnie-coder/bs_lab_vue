package com.xuanyue.exp.mobile.service;

import com.xuanyue.exp.common.PageResult;
import com.xuanyue.exp.common.storage.minio.MinioStorageService;
import com.xuanyue.exp.exp.entity.ExpMsg;
import com.xuanyue.exp.exp.entity.ExpVideo;
import com.xuanyue.exp.exp.repository.ExpMsgRepository;
import com.xuanyue.exp.exp.repository.ExpVideoRepository;
import com.xuanyue.exp.mobile.dto.*;
import com.xuanyue.exp.mobile.entity.MobileExpHomeworkStudent;
import com.xuanyue.exp.mobile.repository.MobileExpHomeworkStudentRepository;
import com.xuanyue.exp.mobile.support.MobileMediaUrlSupport;
import com.xuanyue.exp.mobile.support.MobileAvatarSupport;
import com.xuanyue.exp.mobile.support.MobileHomeCache;
import com.xuanyue.exp.mobile.support.MobileParentAccessService;
import com.xuanyue.exp.mobile.support.MobileUserContext;
import com.xuanyue.exp.system.repository.SysUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * MobileWorkService — 重构版
 *
 * 数据源改用 exp_msg (exp_type='student') + exp_video + exp_homework_student，
 * 方法签名与返回 DTO 不变。
 */
@Service
public class MobileWorkService {

    private static final SimpleDateFormat TIME_FMT = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    private final ExpMsgRepository expMsgRepository;
    private final ExpVideoRepository expVideoRepository;
    private final MobileExpHomeworkStudentRepository homeworkStudentRepository;
    private final SysUserRepository sysUserRepository;
    private final MobileStudentWorkService studentWorkService;
    private final MobilePointsService pointsService;
    private final MobileBadgeGrantService badgeGrantService;
    private final MobileParentAccessService parentAccessService;
    private final MinioStorageService minioStorageService;
    private final MobileHomeCache homeCache;

    public MobileWorkService(ExpMsgRepository expMsgRepository,
                             ExpVideoRepository expVideoRepository,
                             MobileExpHomeworkStudentRepository homeworkStudentRepository,
                             SysUserRepository sysUserRepository,
                             MobileStudentWorkService studentWorkService,
                             MobilePointsService pointsService,
                             MobileBadgeGrantService badgeGrantService,
                             MobileParentAccessService parentAccessService,
                             MinioStorageService minioStorageService,
                             MobileHomeCache homeCache) {
        this.expMsgRepository = expMsgRepository;
        this.expVideoRepository = expVideoRepository;
        this.homeworkStudentRepository = homeworkStudentRepository;
        this.sysUserRepository = sysUserRepository;
        this.studentWorkService = studentWorkService;
        this.pointsService = pointsService;
        this.badgeGrantService = badgeGrantService;
        this.parentAccessService = parentAccessService;
        this.minioStorageService = minioStorageService;
        this.homeCache = homeCache;
    }

    /* ════════════════════════════════════════
       公开 / 个人作品列表
       ════════════════════════════════════════ */

    public PageResult<MobileWorkItemDto> list(String type, int page, int size) {
        return listPublic(type, page, size);
    }

    public PageResult<MobileWorkItemDto> listPublic(String type, int page, int size) {
        return studentWorkService.listPublicWorks(type, page, size);
    }

    public PageResult<MobileWorkItemDto> listMine(String userId, String type, String reviewStatus, int page, int size) {
        String studentId = requireStudentId(userId);
        List<ExpMsg> msgs = studentWorkService.loadUserMsgs(studentId, type);

        List<MobileWorkItemDto> items = msgs.stream()
                .filter(m -> !"draft".equals(m.getStatus())
                        || "pending".equalsIgnoreCase(reviewStatus)
                        || !StringUtils.hasText(reviewStatus))
                .map(this::toWorkItemDto)
                .collect(Collectors.toList());
        enrichWorkItemFiles(items);
        return paginate(items, page, size);
    }

    /* ════════════════════════════════════════
       详情
       ════════════════════════════════════════ */

    public MobileWorkDetailDto getDetail(String workId) {
        return getDetail(workId, null);
    }

    public MobileWorkDetailDto getDetail(String workId, String viewerUserId) {
        return studentWorkService.getWorkDetail(workId, viewerUserId);
    }

    /**
     * 教师查看作品详情（批阅用）
     * 参数改为 ExpMsg
     */
    public MobileWorkDetailDto getDetailForTeacher(ExpMsg msg) {
        if (msg == null) return null;
        return studentWorkService.getWorkDetail(msg.getExpId(), null);
    }

    /* ════════════════════════════════════════
       创建作品
       ════════════════════════════════════════ */

    @Transactional
    public MobileWorkDetailDto createWork(String userId, CreateWorkRequest request) {
        return studentWorkService.submitWork(userId, request);
    }

    /* ════════════════════════════════════════
       教师批阅
       ════════════════════════════════════════ */

    public void reviewWork(String teacherUserId, String msgId, String rating, String comment, Boolean featured) {
        studentWorkService.reviewWork(teacherUserId, msgId, rating, comment);
    }

    public void markWorkFeatured(String workId, boolean featured) {
        // 不实现，批阅结果通过 markResult 反映
    }

    /* ════════════════════════════════════════
       旧服务兼容方法
       ════════════════════════════════════════ */

    @Deprecated
    public String resolveExpIdForWork(ExpMsg msg) {
        return msg != null ? msg.getLinkExpId() : null;
    }

    @Deprecated
    public Optional<ExpMsg> findWork(String workId) {
        return StringUtils.hasText(workId) ? expMsgRepository.findById(workId.trim()) : Optional.empty();
    }

    @Deprecated
    public void persistSourceExpId(ExpMsg msg, String expId) {
        // Already set during submitWork
    }

    /* ════════════════════════════════════════
       内部方法
       ════════════════════════════════════════ */

    private MobileWorkItemDto toWorkItemDto(ExpMsg msg) {
        MobileWorkItemDto item = new MobileWorkItemDto();
        item.setId(msg.getExpId());
        item.setTitle(msg.getExpName());
        item.setAuthor(resolveUserName(msg.getCreateUserId()));
        item.setAuthorInitial(initialOf(item.getAuthor()));
        item.setAuthorAvatarUrl(resolveAvatarUrl(msg.getCreateUserId()));
        item.setSourceExpId(msg.getLinkExpId());

        String et = msg.getExpTaskType();
        if ("tk".equals(et)) { item.setType("remix"); item.setTint("tint-amber"); }
        else if ("self".equals(et)) { item.setType("creative"); item.setTint("tint-cyan"); }
        else { item.setType("homework"); item.setTint("tint-violet"); }

        enrichReviewFields(msg, item);
        if (msg.getCreateTime() != null) item.setTimeLabel(TIME_FMT.format(msg.getCreateTime()));
        return item;
    }

    private void enrichReviewFields(ExpMsg msg, MobileWorkItemDto item) {
        List<MobileExpHomeworkStudent> records = homeworkStudentRepository.findByStudentExpId(msg.getExpId());
        if (!records.isEmpty() && records.get(0).getMarkTime() != null) {
            item.setReviewStatus("reviewed");
            item.setReviewStatusLabel("已批阅");
            item.setReviewBadgeClass("badge-success");
        } else if ("draft".equals(msg.getStatus())) {
            item.setReviewStatus("draft");
            item.setReviewStatusLabel("草稿");
            item.setReviewBadgeClass("badge-warning");
            item.setCanEdit(true);
        } else {
            item.setReviewStatus("pending");
            item.setReviewStatusLabel("待批阅");
            item.setReviewBadgeClass("badge-info");
        }
    }

    private void enrichWorkItemFiles(List<MobileWorkItemDto> items) {
        if (items == null || items.isEmpty()) return;
        List<String> expIds = items.stream()
                .map(MobileWorkItemDto::getId)
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());
        List<ExpVideo> all = expVideoRepository.findByExpIdIn(expIds);
        Map<String, List<ExpVideo>> byExp = all.stream()
                .collect(Collectors.groupingBy(ExpVideo::getExpId));
        for (MobileWorkItemDto item : items) {
            List<ExpVideo> videos = byExp.get(item.getId());
            if (videos == null || videos.isEmpty()) continue;
            videos.sort(Comparator.comparing(v -> v.getSortOrder() == null ? 0 : v.getSortOrder()));
            ExpVideo first = videos.get(0);
            item.setFileCount(videos.size());
            if (StringUtils.hasText(first.getVideoUrl())) {
                item.setCoverUrl(resolveAccessibleUrl(first.getVideoUrl()));
                item.setCoverType(isVideoUrl(first.getVideoUrl()) ? "video" : "image");
            }
        }
    }

    private boolean isVideoUrl(String url) {
        return StringUtils.hasText(url) && (url.toLowerCase().contains(".mp4")
                || url.toLowerCase().contains(".webm")
                || url.toLowerCase().contains(".mov"));
    }

    private String resolveAccessibleUrl(String fileUrl) {
        return MobileMediaUrlSupport.resolve(minioStorageService, fileUrl);
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

    private String resolveAvatarUrl(String userId) {
        if (!StringUtils.hasText(userId)) return null;
        return sysUserRepository.findById(userId)
                .map(u -> MobileAvatarSupport.resolveUserAvatarUrl(minioStorageService, u))
                .orElse(null);
    }

    private String requireStudentId(String userId) {
        String studentId = MobileUserContext.resolveStudentId(userId);
        if (!StringUtils.hasText(studentId)) throw new IllegalArgumentException("请先登录");
        return studentId.trim();
    }

    private <T> PageResult<T> paginate(List<T> all, int page, int size) {
        int safeSize = Math.max(size, 1);
        int from = Math.max(page - 1, 0) * safeSize;
        if (from >= all.size()) return new PageResult<>(all.size(), new ArrayList<>());
        int to = Math.min(from + safeSize, all.size());
        return new PageResult<>(all.size(), all.subList(from, to));
    }
}
