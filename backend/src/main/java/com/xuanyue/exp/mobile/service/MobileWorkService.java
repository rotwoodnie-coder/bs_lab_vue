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
import com.xuanyue.exp.mobile.support.MobileStudentOrgSupport;
import com.xuanyue.exp.mobile.support.MobileUserContext;
import com.xuanyue.exp.mobile.support.MobileWorkAuditStatus;
import com.xuanyue.exp.system.repository.SysOrgRepository;
import com.xuanyue.exp.system.repository.SysUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * MobileWorkService — 统一使用 exp_msg 数据源
 */
@Service
public class MobileWorkService {

    private static final SimpleDateFormat TIME_FMT = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private static final int RECOMMEND_LIMIT = 8;
    private static final Set<String> ASSIGNABLE_EXP_TYPES = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList("standard", "teach", "teacher", "teaching")));

    private final ExpMsgRepository expMsgRepository;
    private final ExpVideoRepository expVideoRepository;
    private final MobileExpHomeworkStudentRepository homeworkStudentRepository;
    private final SysUserRepository sysUserRepository;
    private final SysOrgRepository sysOrgRepository;
    private final MobileStudentWorkService studentWorkService;
    private final MobilePointsService pointsService;
    private final MobileBadgeGrantService badgeGrantService;
    private final MobileParentAccessService parentAccessService;
    private final MinioStorageService minioStorageService;
    private final MobileHomeCache homeCache;
    private final MobileStudentOrgSupport studentOrgSupport;
    private final MobileHomeService mobileHomeService;

    public MobileWorkService(ExpMsgRepository expMsgRepository,
                             ExpVideoRepository expVideoRepository,
                             MobileExpHomeworkStudentRepository homeworkStudentRepository,
                             SysUserRepository sysUserRepository,
                             SysOrgRepository sysOrgRepository,
                             MobileStudentWorkService studentWorkService,
                             MobilePointsService pointsService,
                             MobileBadgeGrantService badgeGrantService,
                             MobileParentAccessService parentAccessService,
                             MinioStorageService minioStorageService,
                             MobileHomeCache homeCache,
                             MobileStudentOrgSupport studentOrgSupport,
                             MobileHomeService mobileHomeService) {
        this.expMsgRepository = expMsgRepository;
        this.expVideoRepository = expVideoRepository;
        this.homeworkStudentRepository = homeworkStudentRepository;
        this.sysUserRepository = sysUserRepository;
        this.sysOrgRepository = sysOrgRepository;
        this.studentWorkService = studentWorkService;
        this.pointsService = pointsService;
        this.badgeGrantService = badgeGrantService;
        this.parentAccessService = parentAccessService;
        this.minioStorageService = minioStorageService;
        this.homeCache = homeCache;
        this.studentOrgSupport = studentOrgSupport;
        this.mobileHomeService = mobileHomeService;
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
                .map(this::toWorkItemDto)
                .filter(item -> filterByReviewStatus(item, reviewStatus))
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

    public MobileWorkDetailDto getDetailForTeacher(ExpMsg msg) {
        if (msg == null) return null;
        // 审核场景：允许查看待审核/未通过作品
        return studentWorkService.getWorkDetail(msg.getExpId(), null, true);
    }

    @Transactional(readOnly = true)
    public List<HomeFeedItem> listRecommendations(String workId) {
        final String excludeExpId;
        if (StringUtils.hasText(workId)) {
            excludeExpId = expMsgRepository.findById(workId.trim())
                    .map(ExpMsg::getLinkExpId)
                    .filter(StringUtils::hasText)
                    .map(String::trim)
                    .orElse(null);
        } else {
            excludeExpId = null;
        }
        List<String> expIds = expMsgRepository.findAll().stream()
                .filter(this::isRecommendableExperiment)
                .filter(e -> excludeExpId == null || !excludeExpId.equals(e.getExpId()))
                .sorted(Comparator.comparing(ExpMsg::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(RECOMMEND_LIMIT)
                .map(ExpMsg::getExpId)
                .collect(Collectors.toList());
        return mobileHomeService.buildExperimentFeedItems(expIds);
    }

    /* ════════════════════════════════════════
       创建作品
       ════════════════════════════════════════ */

    @Transactional
    public MobileWorkDetailDto createWork(String userId, CreateWorkRequest request) {
        return studentWorkService.submitWork(userId, request);
    }

    @Transactional
    public MobileWorkDetailDto updateWork(String userId, String workId, UpdateWorkRequest request) {
        return studentWorkService.updateWork(userId, workId, request);
    }

    /* ════════════════════════════════════════
       教师批阅
       ════════════════════════════════════════ */

    public void reviewWork(String teacherUserId, String msgId, String rating, String comment, Boolean featured) {
        studentWorkService.reviewWork(teacherUserId, msgId, rating, comment);
    }

    public void markWorkFeatured(String workId, boolean featured) {
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

        item.setClassName(resolveClassName(msg.getCreateUserId()));
        enrichWorkItemOrg(item, msg.getCreateUserId());
        enrichReviewFields(msg, item);
        if (msg.getCreateTime() != null) item.setTimeLabel(TIME_FMT.format(msg.getCreateTime()));
        return item;
    }

    private void enrichReviewFields(ExpMsg msg, MobileWorkItemDto item) {
        String status = msg.getStatus();
        item.setReviewStatus(MobileWorkAuditStatus.reviewStatusCode(status));
        item.setReviewStatusLabel(MobileWorkAuditStatus.reviewStatusLabel(status));
        if (MobileWorkAuditStatus.isApproved(status)) {
            item.setReviewBadgeClass("badge-success");
        } else if (MobileWorkAuditStatus.isRejected(status)) {
            item.setReviewBadgeClass("badge-danger");
            item.setCanEdit(true);
        } else if (MobileWorkAuditStatus.isDraft(status)) {
            item.setReviewStatusLabel("草稿");
            item.setReviewBadgeClass("badge-warning");
            item.setCanEdit(true);
        } else {
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

    private void enrichWorkItemOrg(MobileWorkItemDto item, String userId) {
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
    }

    private boolean isRecommendableExperiment(ExpMsg e) {
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

    private String resolveClassName(String userId) {
        if (!StringUtils.hasText(userId)) return null;
        return sysUserRepository.findById(userId.trim())
                .map(u -> {
                    if (!StringUtils.hasText(u.getUserOrgId())) return null;
                    return sysOrgRepository.findById(u.getUserOrgId())
                            .map(o -> o.getOrgName())
                            .orElse(null);
                })
                .orElse(null);
    }

    private String requireStudentId(String userId) {
        String studentId = MobileUserContext.resolveStudentId(userId);
        if (!StringUtils.hasText(studentId)) throw new IllegalArgumentException("请先登录");
        return studentId.trim();
    }

    private boolean filterByReviewStatus(MobileWorkItemDto item, String reviewStatus) {
        if (!StringUtils.hasText(reviewStatus) || "all".equalsIgnoreCase(reviewStatus.trim())) {
            return true;
        }
        String rs = reviewStatus.trim().toLowerCase();
        String dtoStatus = item.getReviewStatus();
        if ("approved".equals(rs) || "reviewed".equals(rs)) return "reviewed".equals(dtoStatus);
        if ("pending".equals(rs)) return "pending".equals(dtoStatus);
        if ("rejected".equals(rs)) return "rejected".equals(dtoStatus);
        if ("draft".equals(rs)) return "draft".equals(dtoStatus);
        return true;
    }

    private <T> PageResult<T> paginate(List<T> all, int page, int size) {
        int safeSize = Math.max(size, 1);
        int from = Math.max(page - 1, 0) * safeSize;
        if (from >= all.size()) return new PageResult<>(all.size(), new ArrayList<>());
        int to = Math.min(from + safeSize, all.size());
        return new PageResult<>(all.size(), all.subList(from, to));
    }
}
