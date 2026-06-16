package com.xuanyue.exp.mobile.service;

import com.xuanyue.exp.common.PageResult;
import com.xuanyue.exp.common.storage.minio.MinioStorageService;
import com.xuanyue.exp.mobile.support.MobileMediaUrlSupport;
import com.xuanyue.exp.data.entity.DataSchoolGrade;
import com.xuanyue.exp.data.entity.DataSchoolSemester;
import com.xuanyue.exp.data.entity.DataSchoolSubject;
import com.xuanyue.exp.data.repository.DataSchoolGradeRepository;
import com.xuanyue.exp.data.repository.DataSchoolSemesterRepository;
import com.xuanyue.exp.data.repository.DataSchoolSubjectRepository;
import com.xuanyue.exp.data.service.DataDictService;
import com.xuanyue.exp.edu.repository.SchoolNoticeRepository;
import com.xuanyue.exp.data.entity.DataFile;
import com.xuanyue.exp.data.repository.DataFileRepository;
import com.xuanyue.exp.exp.entity.ExpGrade;
import com.xuanyue.exp.exp.entity.ExpMaterial;
import com.xuanyue.exp.exp.entity.ExpSimulator;
import com.xuanyue.exp.exp.entity.ExpVideo;
import com.xuanyue.exp.exp.repository.ExpGradeRepository;
import com.xuanyue.exp.exp.repository.ExpMaterialRepository;
import com.xuanyue.exp.exp.repository.ExpSimulatorRepository;
import com.xuanyue.exp.exp.repository.ExpVideoRepository;
import com.xuanyue.exp.exp.service.ExpGradeService;
import com.xuanyue.exp.mobile.dto.HomeBootstrapDto;
import com.xuanyue.exp.mobile.dto.HomeFeedItem;
import com.xuanyue.exp.mobile.dto.NoticeDto;
import com.xuanyue.exp.mobile.dto.ParentDashboardDto;
import com.xuanyue.exp.mobile.dto.SubjectItem;
import com.xuanyue.exp.mobile.entity.MobileExpMsg;
import com.xuanyue.exp.mobile.entity.MbParentChild;
import com.xuanyue.exp.mobile.entity.MbTask;
import com.xuanyue.exp.mobile.entity.MbWork;
import com.xuanyue.exp.mobile.entity.MbWorkFile;
import com.xuanyue.exp.mobile.repository.MobileFeedRef;
import com.xuanyue.exp.mobile.repository.MobileHomeRepository;
import com.xuanyue.exp.mobile.repository.MbParentChildRepository;
import com.xuanyue.exp.mobile.repository.MbTaskRepository;
import com.xuanyue.exp.mobile.repository.MbTaskSubmissionRepository;
import com.xuanyue.exp.mobile.repository.MbWorkFileRepository;
import com.xuanyue.exp.mobile.repository.MbWorkRepository;
import com.xuanyue.exp.mobile.support.MobileAvatarSupport;
import com.xuanyue.exp.mobile.support.MobileFeedGradeSupport;
import com.xuanyue.exp.mobile.support.MobileFeedRankSupport;
import com.xuanyue.exp.mobile.support.MobileFeedRankSupport.FeedRankMeta;
import com.xuanyue.exp.mobile.support.MobileFeedRankSupport.RankedFeedEntry;
import com.xuanyue.exp.mobile.support.MobileFeedViewerProfile;
import com.xuanyue.exp.mobile.support.MobileHomeCache;
import com.xuanyue.exp.mobile.support.MobileStudentOrgSupport;
import com.xuanyue.exp.mobile.support.MobileTeacherClassScope;
import com.xuanyue.exp.mobile.support.MobileUserContext;
import com.xuanyue.exp.system.service.SysMsgService;
import com.xuanyue.exp.system.entity.SysOrg;
import com.xuanyue.exp.system.entity.SysUser;
import com.xuanyue.exp.system.repository.SysOrgRepository;
import com.xuanyue.exp.edu.entity.SchoolNotice;
import com.xuanyue.exp.system.repository.SysUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 移动端首页业务服务
 */
@Service
public class MobileHomeService {

    private static final Logger log = LoggerFactory.getLogger(MobileHomeService.class);

    private final MobileHomeRepository homeRepository;
    private final DataSchoolSubjectRepository subjectRepository;
    private final DataSchoolGradeRepository gradeRepository;
    private final DataSchoolSemesterRepository semesterRepository;
    private final SysUserRepository sysUserRepository;
    private final SysOrgRepository sysOrgRepository;
    private final DataDictService dataDictService;
    private final SchoolNoticeRepository schoolNoticeRepository;
    private final ExpGradeService expGradeService;
    private final ExpVideoRepository expVideoRepository;
    private final ExpGradeRepository expGradeRepository;
    private final ExpMaterialRepository expMaterialRepository;
    private final DataFileRepository dataFileRepository;
    private final ExpSimulatorRepository expSimulatorRepository;
    private final MbParentChildRepository parentChildRepository;
    private final MbTaskSubmissionRepository taskSubmissionRepository;
    private final MbWorkRepository workRepository;
    private final MbWorkFileRepository workFileRepository;
    private final MbTaskRepository taskRepository;
    private final MobileParentActivityService parentActivityService;
    private final MobileSettingsService settingsService;
    private final MobileHomeCache homeCache;
    private final MobileStudentOrgSupport studentOrgSupport;
    private final MobileFeedGradeSupport feedGradeSupport;
    private final SysMsgService sysMsgService;
    private final MinioStorageService minioStorageService;

    /** 学科 key -> gradient 映射（与前端一致） */
    private static final Map<String, String> SUBJECT_GRADIENT = new LinkedHashMap<>();
    static {
        SUBJECT_GRADIENT.put("科学", "card-media-grad-forest");
        SUBJECT_GRADIENT.put("物理", "card-media-grad-cool");
        SUBJECT_GRADIENT.put("化学", "card-media-grad-amber-rose");
        SUBJECT_GRADIENT.put("生物", "card-media-grad-forest");
    }

    /** 默认渐变色 */
    private static final String DEFAULT_GRADIENT = "card-media-grad-warm";

    public MobileHomeService(MobileHomeRepository homeRepository,
                             DataSchoolSubjectRepository subjectRepository,
                             DataSchoolGradeRepository gradeRepository,
                             DataSchoolSemesterRepository semesterRepository,
                             SysUserRepository sysUserRepository,
                             SysOrgRepository sysOrgRepository,
                             DataDictService dataDictService,
                             SchoolNoticeRepository schoolNoticeRepository,
                             ExpGradeService expGradeService,
                             ExpVideoRepository expVideoRepository,
                             ExpGradeRepository expGradeRepository,
                             ExpMaterialRepository expMaterialRepository,
                             DataFileRepository dataFileRepository,
                             ExpSimulatorRepository expSimulatorRepository,
                             MbParentChildRepository parentChildRepository,
                             MbTaskSubmissionRepository taskSubmissionRepository,
                             MbWorkRepository workRepository,
                             MbWorkFileRepository workFileRepository,
                             MbTaskRepository taskRepository,
                             MobileParentActivityService parentActivityService,
                             MobileSettingsService settingsService,
                             MobileHomeCache homeCache,
                             MobileStudentOrgSupport studentOrgSupport,
                             MobileFeedGradeSupport feedGradeSupport,
                             SysMsgService sysMsgService,
                             MinioStorageService minioStorageService) {
        this.homeRepository = homeRepository;
        this.subjectRepository = subjectRepository;
        this.gradeRepository = gradeRepository;
        this.semesterRepository = semesterRepository;
        this.sysUserRepository = sysUserRepository;
        this.sysOrgRepository = sysOrgRepository;
        this.dataDictService = dataDictService;
        this.schoolNoticeRepository = schoolNoticeRepository;
        this.expGradeService = expGradeService;
        this.expVideoRepository = expVideoRepository;
        this.expGradeRepository = expGradeRepository;
        this.expMaterialRepository = expMaterialRepository;
        this.dataFileRepository = dataFileRepository;
        this.expSimulatorRepository = expSimulatorRepository;
        this.parentChildRepository = parentChildRepository;
        this.taskSubmissionRepository = taskSubmissionRepository;
        this.workRepository = workRepository;
        this.workFileRepository = workFileRepository;
        this.taskRepository = taskRepository;
        this.parentActivityService = parentActivityService;
        this.settingsService = settingsService;
        this.homeCache = homeCache;
        this.studentOrgSupport = studentOrgSupport;
        this.feedGradeSupport = feedGradeSupport;
        this.sysMsgService = sysMsgService;
        this.minioStorageService = minioStorageService;
    }

    /**
     * 首页首屏聚合：feed + 最新公告 + 未读消息数。
     */
    @Transactional(readOnly = true)
    public HomeBootstrapDto getBootstrap(String userId, String gradeKey, int size) {
        HomeBootstrapDto dto = new HomeBootstrapDto();
        dto.setFeed(getFeed(userId, null, gradeKey, 1, size));
        dto.setNotice(getLatestNotice(userId));
        dto.setUnreadCount(sysMsgService.unreadCount(userId));
        return dto;
    }

    /**
     * 获取首页信息流（分页）
     * @param gradeKey 年级 Chip：all 展示全部；g12/g34/g56 按年级段过滤
     */
    @Transactional(readOnly = true)
    public PageResult<HomeFeedItem> getFeed(String userId, String childUserId, String gradeKey, int page, int size) {
        int safePage = Math.max(1, page);
        int safeSize = Math.max(1, Math.min(size, 50));
        String viewerKey = StringUtils.hasText(userId) ? userId.trim() : "guest";
        if (StringUtils.hasText(childUserId)) {
            viewerKey = viewerKey + ":child:" + childUserId.trim();
        }
        String cacheKey = "feed:" + viewerKey + ":" + normalizeGradeKey(gradeKey) + ":" + safePage + ":" + safeSize;
        return homeCache.getFeed(cacheKey, () -> loadFeedPage(userId, childUserId, gradeKey, safePage, safeSize));
    }

    @Transactional(readOnly = true)
    public PageResult<HomeFeedItem> getFeed(String gradeKey, int page, int size) {
        return getFeed(null, null, gradeKey, page, size);
    }

    /** 参与排序的最大条目数（三类 eligible 资源合并后） */
    private static final int FEED_SORT_CAP = 1000;

    /**
     * 首页瀑布流：按年级 Chip 过滤 eligible 资源 → 个性化排序 → 分页。
     */
    private PageResult<HomeFeedItem> loadFeedPage(String userId, String childUserId, String gradeKey, int page, int size) {
        List<String> gradeIds = feedGradeSupport.resolveGradeIds(gradeKey);
        boolean filterByGrade = gradeIds != null && !gradeIds.isEmpty();

        long totalEligible = filterByGrade
                ? homeRepository.countFeedByGrades(gradeIds) + countSupplementWorksByGrade(gradeIds)
                : homeRepository.countFeedAll();
        if (totalEligible <= 0) {
            return new PageResult<>(0, Collections.emptyList());
        }

        int fetchLimit = (int) Math.min(totalEligible, Math.max(FEED_SORT_CAP, (long) page * size));
        List<Object[]> refRows = filterByGrade
                ? homeRepository.findFeedCandidateRefsByGrades(gradeIds, fetchLimit)
                : homeRepository.findFeedCandidateRefsAll(fetchLimit);
        if (filterByGrade) {
            refRows = supplementGradeFilteredWorkRefs(refRows, gradeIds, fetchLimit);
        }

        List<RankedFeedEntry> entries = buildRankedFeedEntries(mapFeedRefs(refRows));
        if (entries.isEmpty()) {
            return new PageResult<>(0, Collections.emptyList());
        }

        MobileFeedViewerProfile viewer = resolveViewerProfile(userId, childUserId);
        List<HomeFeedItem> sorted = MobileFeedRankSupport.sort(entries, viewer, null);

        int offset = Math.max(0, (page - 1) * size);
        int end = Math.min(offset + size, sorted.size());
        List<HomeFeedItem> pageItems = offset >= sorted.size()
                ? Collections.emptyList()
                : new ArrayList<>(sorted.subList(offset, end));

        applyCoverUrls(pageItems);
        return new PageResult<>(totalEligible, pageItems);
    }

    private long countSupplementWorksByGrade(List<String> gradeIds) {
        if (gradeIds == null || gradeIds.isEmpty()) {
            return 0;
        }
        long count = 0;
        for (MbWork work : workRepository.findByStatusOrderByCreateTimeDesc("y")) {
            if (!isReviewedWork(work)) {
                continue;
            }
            if (StringUtils.hasText(work.getSchoolGradeId())) {
                continue;
            }
            String resolved = resolveWorkSchoolGradeId(work);
            if (StringUtils.hasText(resolved) && gradeIds.contains(resolved.trim())) {
                count++;
            }
        }
        return count;
    }

    private List<Object[]> supplementGradeFilteredWorkRefs(List<Object[]> refRows,
                                                           List<String> gradeIds,
                                                           int maxTotal) {
        Set<String> existingWorkIds = new HashSet<>();
        if (refRows != null) {
            for (Object[] row : refRows) {
                if (row != null && row.length >= 2 && row[0] != null
                        && "work".equals(String.valueOf(row[1]))) {
                    existingWorkIds.add(String.valueOf(row[0]));
                }
            }
        }
        List<Object[]> merged = refRows != null ? new ArrayList<>(refRows) : new ArrayList<>();
        for (MbWork work : workRepository.findByStatusOrderByCreateTimeDesc("y")) {
            if (merged.size() >= maxTotal) {
                break;
            }
            if (!isReviewedWork(work) || existingWorkIds.contains(work.getWorkId())) {
                continue;
            }
            if (StringUtils.hasText(work.getSchoolGradeId())) {
                continue;
            }
            String resolved = resolveWorkSchoolGradeId(work);
            if (StringUtils.hasText(resolved) && gradeIds.contains(resolved.trim())) {
                merged.add(new Object[]{work.getWorkId(), "work"});
                existingWorkIds.add(work.getWorkId());
            }
        }
        return merged;
    }

    private boolean isReviewedWork(MbWork work) {
        if (work == null || !"y".equalsIgnoreCase(safe(work.getStatus()))) {
            return false;
        }
        String reviewStatus = safe(work.getReviewStatus());
        return "reviewed".equalsIgnoreCase(reviewStatus) || "approved".equalsIgnoreCase(reviewStatus);
    }

    private String resolveWorkSchoolGradeId(MbWork work) {
        if (work == null) {
            return null;
        }
        if (StringUtils.hasText(work.getSchoolGradeId())) {
            return work.getSchoolGradeId().trim();
        }
        if (!StringUtils.hasText(work.getStudentUserId())) {
            return null;
        }
        return studentOrgSupport.resolve(work.getStudentUserId()).getSchoolGradeId();
    }

    private static String safe(String value) {
        return value != null ? value.trim() : "";
    }

    private MobileFeedViewerProfile resolveViewerProfile(String userId, String childUserId) {
        if (!StringUtils.hasText(userId)) {
            return MobileFeedViewerProfile.guest();
        }
        SysUser user = sysUserRepository.findById(userId.trim()).orElse(null);
        if (user == null || !StringUtils.hasText(user.getUserRoleId())) {
            return MobileFeedViewerProfile.guest();
        }
        String role = user.getUserRoleId().trim();
        if ("Parent".equalsIgnoreCase(role)) {
            String targetChild = resolveParentChildUserId(userId, childUserId);
            if (StringUtils.hasText(targetChild)) {
                MobileStudentOrgSupport.StudentOrgContext ctx = studentOrgSupport.resolve(targetChild);
                return new MobileFeedViewerProfile("parent", ctx.getSchoolGradeId(),
                        ctx.getClassOrgId(), ctx.getRootOrgId(), Collections.emptySet());
            }
            return new MobileFeedViewerProfile("parent", null, null, user.getRootOrgId(), Collections.emptySet());
        }
        if ("Teacher".equalsIgnoreCase(role)) {
            List<MbTask> tasks = taskRepository.findByTeacherUserIdAndStatusOrderByCreateTimeDesc(userId.trim(), "y");
            Set<String> classOrgIds = MobileTeacherClassScope.resolveClassOrgIds(user, tasks, sysOrgRepository);
            MobileStudentOrgSupport.StudentOrgContext ctx = studentOrgSupport.resolve(userId.trim());
            return new MobileFeedViewerProfile("teacher", ctx.getSchoolGradeId(),
                    user.getUserOrgId(), user.getRootOrgId(), classOrgIds);
        }
        if ("Student".equalsIgnoreCase(role)) {
            MobileStudentOrgSupport.StudentOrgContext ctx = studentOrgSupport.resolve(userId.trim());
            return new MobileFeedViewerProfile("student", ctx.getSchoolGradeId(),
                    ctx.getClassOrgId(), ctx.getRootOrgId(), Collections.emptySet());
        }
        return new MobileFeedViewerProfile(role.toLowerCase(), null, null, user.getRootOrgId(), Collections.emptySet());
    }

    private String resolveParentChildUserId(String parentUserId, String childUserId) {
        if (StringUtils.hasText(childUserId)) {
            return parentChildRepository.findByParentUserIdAndChildUserId(parentUserId.trim(), childUserId.trim())
                    .filter(b -> "approved".equalsIgnoreCase(safeBindStatus(b.getBindStatus())))
                    .map(MbParentChild::getChildUserId)
                    .orElse(null);
        }
        List<MbParentChild> binds = parentChildRepository
                .findByParentUserIdAndBindStatusOrderByIsDefaultDesc(parentUserId.trim(), "approved");
        if (binds.isEmpty()) {
            return null;
        }
        return binds.get(0).getChildUserId();
    }

    private static String safeBindStatus(String value) {
        return value != null ? value.trim() : "";
    }

    private String normalizeGradeKey(String gradeKey) {
        return StringUtils.hasText(gradeKey) ? gradeKey.trim() : "all";
    }

    /**
     * 搜索已发布实验（与首页 feed 相同的数据结构，含封面与作者）
     */
    @Transactional(readOnly = true)
    public PageResult<HomeFeedItem> searchFeed(String keyword, int page, int size) {
        if (!StringUtils.hasText(keyword)) {
            return new PageResult<>(0, Collections.emptyList());
        }
        String kw = keyword.trim();
        int safeSize = Math.max(1, Math.min(size, 50));
        int offset = Math.max(0, (page - 1)) * safeSize;
        List<Object[]> refs = homeRepository.findFeedRefsPageByKeyword(kw, offset, safeSize);
        List<HomeFeedItem> items = buildFeedItems(mapFeedRefs(refs));
        applyCoverUrls(items);
        long total = estimateFeedTotal(offset, safeSize, refs.size());
        return new PageResult<>(total, items);
    }

    /** 避免 UNION COUNT；前端以本页条数是否满页判断 hasMore */
    private long estimateFeedTotal(int offset, int pageSize, int rowCount) {
        if (rowCount < pageSize) {
            return offset + rowCount;
        }
        return offset + pageSize + 1L;
    }

    /**
     * 搜索热词：最近发布的实验名称
     */
    @Transactional(readOnly = true)
    public List<String> getHotKeywords(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 20));
        List<String> names = homeRepository.findRecentFeedNames(safeLimit);
        if (names == null || names.isEmpty()) {
            return Collections.emptyList();
        }
        LinkedHashSet<String> distinct = new LinkedHashSet<>();
        for (String name : names) {
            if (StringUtils.hasText(name)) {
                distinct.add(name.trim());
            }
        }
        return new ArrayList<>(distinct);
    }

    /**
     * 移动端统计（实验/模拟实验数量等）
     */
    @Transactional(readOnly = true)
    public Map<String, Long> getBrowseStats() {
        return homeCache.getBrowseStats(() -> {
            Map<String, Long> stats = new LinkedHashMap<>();
            stats.put("publishedExpCount", homeRepository.countFeedAll());
            stats.put("simulatorCount", homeRepository.countStandaloneSimulators());
            return stats;
        });
    }

    /**
     * 获取年级筛选列表：全部、1-2年级、3-4年级、5-6年级
     */
    @Transactional(readOnly = true)
    public List<SubjectItem> getGradeFilters() {
        List<SubjectItem> filters = new ArrayList<>();
        filters.add(new SubjectItem("all", "全部"));
        filters.add(new SubjectItem("g12", "1-2年级"));
        filters.add(new SubjectItem("g34", "3-4年级"));
        filters.add(new SubjectItem("g56", "5-6年级"));
        return filters;
    }

    /**
     * 获取最新公告通知
     */
    @Transactional(readOnly = true)
    public NoticeDto getLatestNotice(String userId) {
        List<SchoolNotice> notices = schoolNoticeRepository.findTopByStatusOrderByReleaseTimeDesc("y");
        if (notices == null || notices.isEmpty()) {
            return null;
        }
        SchoolNotice notice = notices.get(0);

        NoticeDto dto = new NoticeDto();
        dto.setId(notice.getNoticeId());
        dto.setTitle(notice.getNoticeName());
        dto.setBody(notice.getNoticeContent());
        dto.setDate(notice.getReleaseTime() != null
                ? new SimpleDateFormat("yyyy-MM-dd").format(notice.getReleaseTime())
                : "");
        dto.setType("system");
        dto.setBadge("通知公告");
        boolean unread = userId == null || !settingsService.isNoticeRead(userId, notice.getNoticeId());
        dto.setHasUnread(unread);
        return dto;
    }

    public List<String> listReadNoticeIds(String userId) {
        return settingsService.listReadNoticeIds(userId);
    }

    public void markNoticeRead(String userId, String noticeId) {
        settingsService.markNoticeRead(userId, noticeId);
    }

    @Transactional(readOnly = true)
    public ParentDashboardDto getParentDashboard(String userId) {
        String parentId = MobileUserContext.resolveParentId(userId);
        List<MbParentChild> binds = parentChildRepository.findByParentUserIdAndBindStatusOrderByIsDefaultDesc(parentId, "approved");
        if (binds.isEmpty()) {
            return new ParentDashboardDto();
        }
        ParentDashboardDto dto = new ParentDashboardDto();
        List<ParentDashboardDto.ChildInfo> children = new ArrayList<>();
        Map<String, List<ParentDashboardDto.ActivityItem>> activitiesByChild = new LinkedHashMap<>();
        Map<String, ParentDashboardDto.QuizTodayStatus> quizTodayByChild = new LinkedHashMap<>();
        for (MbParentChild bind : binds) {
            ParentDashboardDto.ChildInfo child = new ParentDashboardDto.ChildInfo();
            child.setId(bind.getChildUserId());
            SysUser childUser = sysUserRepository.findById(bind.getChildUserId()).orElse(null);
            String childName = resolveChildDisplayName(bind.getChildUserId());
            child.setName(childName);
            child.setAvatar(MobileAvatarSupport.initialOf(childName, "孩"));
            child.setAvatarUrl(MobileAvatarSupport.resolveUserAvatarUrl(minioStorageService, childUser));
            child.setCurrent("y".equalsIgnoreCase(bind.getIsDefault()));
            long pending = taskSubmissionRepository.countByStudentUserIdAndStateNot(bind.getChildUserId(), "done");
            long completed = taskSubmissionRepository.countByStudentUserIdAndState(bind.getChildUserId(), "done");
            child.setPending((int) pending);
            child.setCompleted((int) completed);
            children.add(child);
            MobileParentActivityService.ParentActivitySnapshot snapshot =
                    parentActivityService.resolve(bind.getChildUserId());
            activitiesByChild.put(bind.getChildUserId(), snapshot.getActivities());
            if (snapshot.getQuizToday() != null) {
                quizTodayByChild.put(bind.getChildUserId(), snapshot.getQuizToday());
            }
        }
        dto.setChildren(children);
        if (children.isEmpty()) {
            return dto;
        }
        ParentDashboardDto.ChildInfo first = children.get(0);
        ParentDashboardDto.TodayProgress progress = new ParentDashboardDto.TodayProgress();
        progress.setPending(first.getPending());
        progress.setCompleted(first.getCompleted());
        int total = first.getPending() + first.getCompleted();
        progress.setCompletionRate(total > 0 ? (first.getCompleted() * 100 / total) : 0);
        dto.setTodayProgress(progress);
        dto.setActivitiesByChild(activitiesByChild);
        dto.setQuizTodayByChild(quizTodayByChild);
        if (activitiesByChild.containsKey(first.getId())) {
            dto.setRecentActivities(activitiesByChild.get(first.getId()));
        }
        return dto;
    }

    private String resolveChildDisplayName(String childUserId) {
        if (childUserId == null) return "孩子";
        try {
            return sysUserRepository.findById(childUserId)
                    .map(SysUser::getUserName)
                    .orElse("孩子");
        } catch (Exception e) {
            return "孩子";
        }
    }

    /**
     * 解析年级筛选 key 为 grade_id 列表，null 表示全部
     */
    private List<String> resolveGradeIds(String gradeKey) {
        return feedGradeSupport.resolveGradeIds(gradeKey);
    }

    private List<MobileFeedRef> mapFeedRefs(List<Object[]> rows) {
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyList();
        }
        List<MobileFeedRef> refs = new ArrayList<>(rows.size());
        for (Object[] row : rows) {
            if (row == null || row.length < 2 || row[0] == null) {
                continue;
            }
            refs.add(new MobileFeedRef(String.valueOf(row[0]), String.valueOf(row[1])));
        }
        return refs;
    }

    private List<HomeFeedItem> buildFeedItems(List<MobileFeedRef> refs) {
        List<RankedFeedEntry> entries = buildRankedFeedEntries(refs);
        List<HomeFeedItem> items = new ArrayList<>(entries.size());
        for (RankedFeedEntry entry : entries) {
            items.add(entry.getItem());
        }
        return items;
    }

    private List<RankedFeedEntry> buildRankedFeedEntries(List<MobileFeedRef> refs) {
        if (refs.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> expIds = refs.stream()
                .filter(r -> "exp_msg".equals(r.getItemSource()))
                .map(MobileFeedRef::getItemId)
                .collect(Collectors.toList());
        List<String> simIds = refs.stream()
                .filter(r -> "simulator".equals(r.getItemSource()))
                .map(MobileFeedRef::getItemId)
                .collect(Collectors.toList());
        List<String> workIds = refs.stream()
                .filter(r -> "work".equals(r.getItemSource()))
                .map(MobileFeedRef::getItemId)
                .collect(Collectors.toList());

        Map<String, MobileExpMsg> expMap = expIds.isEmpty()
                ? Collections.emptyMap()
                : homeRepository.findAllById(expIds).stream()
                .collect(Collectors.toMap(MobileExpMsg::getExpId, e -> e, (a, b) -> a, LinkedHashMap::new));
        Map<String, ExpSimulator> simMap = simIds.isEmpty()
                ? Collections.emptyMap()
                : expSimulatorRepository.findAllById(simIds).stream()
                .collect(Collectors.toMap(ExpSimulator::getSimulatorId, s -> s, (a, b) -> a, LinkedHashMap::new));
        Map<String, MbWork> workMap = workIds.isEmpty()
                ? Collections.emptyMap()
                : workRepository.findAllById(workIds).stream()
                .collect(Collectors.toMap(MbWork::getWorkId, w -> w, (a, b) -> a, LinkedHashMap::new));

        FeedBuildContext ctx = buildFeedContext(expMap, simMap, workMap);
        Map<String, MbWorkFile> workCoverMap = loadWorkCoverFiles(workIds);

        List<RankedFeedEntry> items = new ArrayList<>(refs.size());
        for (MobileFeedRef ref : refs) {
            if ("exp_msg".equals(ref.getItemSource())) {
                MobileExpMsg entity = expMap.get(ref.getItemId());
                if (entity != null) {
                    items.add(toExpRankedEntry(entity, ctx));
                }
            } else if ("simulator".equals(ref.getItemSource())) {
                ExpSimulator sim = simMap.get(ref.getItemId());
                if (sim != null) {
                    items.add(toSimulatorRankedEntry(sim, ctx));
                }
            } else if ("work".equals(ref.getItemSource())) {
                MbWork work = workMap.get(ref.getItemId());
                if (work != null) {
                    items.add(toWorkRankedEntry(work, ctx, workCoverMap.get(work.getWorkId())));
                }
            }
        }
        return items;
    }

    private Map<String, MbWorkFile> loadWorkCoverFiles(List<String> workIds) {
        if (workIds == null || workIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, MbWorkFile> coverMap = new HashMap<>();
        for (MbWorkFile file : workFileRepository.findByWorkIdInOrderByWorkIdAscSortOrderAsc(workIds)) {
            coverMap.putIfAbsent(file.getWorkId(), file);
        }
        return coverMap;
    }

    private RankedFeedEntry toExpRankedEntry(MobileExpMsg entity, FeedBuildContext ctx) {
        HomeFeedItem item = toFeedItem(entity, ctx);
        FeedRankMeta meta = new FeedRankMeta();
        meta.setSource("exp_msg");
        meta.setSchoolGradeIds(ctx.gradeIdsByExp.getOrDefault(entity.getExpId(), Collections.emptyList()));
        meta.setPublishTime(entity.getCreateTime());
        meta.setEngagementScore(item.getPlayCount() != null ? item.getPlayCount() : 0);
        return new RankedFeedEntry(item, meta);
    }

    private RankedFeedEntry toSimulatorRankedEntry(ExpSimulator sim, FeedBuildContext ctx) {
        HomeFeedItem item = toSimulatorFeedItem(sim, ctx);
        FeedRankMeta meta = new FeedRankMeta();
        meta.setSource("simulator");
        meta.setSchoolGradeIds(ctx.gradeIdsBySim.getOrDefault(sim.getSimulatorId(), Collections.emptyList()));
        meta.setPublishTime(sim.getCreateTime());
        meta.setEngagementScore(0);
        return new RankedFeedEntry(item, meta);
    }

    private RankedFeedEntry toWorkRankedEntry(MbWork work, FeedBuildContext ctx, MbWorkFile coverFile) {
        HomeFeedItem item = toWorkFeedItem(work, ctx, coverFile);
        FeedRankMeta meta = new FeedRankMeta();
        meta.setSource("work");
        String schoolGradeId = work.getSchoolGradeId();
        if (!StringUtils.hasText(schoolGradeId) && StringUtils.hasText(work.getStudentUserId())) {
            schoolGradeId = studentOrgSupport.resolve(work.getStudentUserId()).getSchoolGradeId();
        }
        meta.setSchoolGradeId(schoolGradeId);
        meta.setClassOrgId(work.getClassOrgId());
        SysUser student = ctx.users.get(work.getStudentUserId());
        if (student != null && StringUtils.hasText(student.getRootOrgId())) {
            meta.setRootOrgId(student.getRootOrgId());
        }
        Date publishTime = work.getReviewTime() != null ? work.getReviewTime() : work.getCreateTime();
        meta.setPublishTime(publishTime);
        meta.setFeatured("y".equalsIgnoreCase(work.getIsFeatured()));
        int engagement = (work.getLikeCount() != null ? work.getLikeCount() : 0)
                + (work.getCommentCount() != null ? work.getCommentCount() : 0);
        if (work.getTeacherReviewStars() != null) {
            engagement += work.getTeacherReviewStars() * 2;
        }
        meta.setEngagementScore(engagement);
        return new RankedFeedEntry(item, meta);
    }

    private HomeFeedItem toWorkFeedItem(MbWork work, FeedBuildContext ctx, MbWorkFile coverFile) {
        HomeFeedItem item = new HomeFeedItem();
        item.setId(work.getWorkId());
        item.setTitle(work.getTitle());
        item.setType("work");
        item.setWorkType(work.getWorkType());
        item.setAuthorRole("student");
        item.setClassLabel(work.getClassName());
        item.setPlayCount(work.getLikeCount() != null ? work.getLikeCount() : 0);
        item.setGradientClass(StringUtils.hasText(work.getTint()) ? work.getTint() : DEFAULT_GRADIENT);

        String workType = work.getWorkType();
        if ("remix".equals(workType)) {
            item.setTagLabel("拍同款");
            item.setTagType("work-remix");
        } else if ("creative".equals(workType)) {
            item.setTagLabel("创意");
            item.setTagType("work-creative");
        } else {
            item.setTagLabel("作品");
            item.setTagType("work-homework");
        }

        if (StringUtils.hasText(work.getSchoolGradeId())) {
            for (DataSchoolGrade grade : gradeRepository.findByGradeIdIn(
                    Collections.singletonList(work.getSchoolGradeId()))) {
                item.setGrade(grade.getGradeName());
            }
        }

        SysUser student = ctx.users.get(work.getStudentUserId());
        if (student != null) {
            item.setAuthor(StringUtils.hasText(student.getUserNickName())
                    ? student.getUserNickName()
                    : (StringUtils.hasText(student.getUserName()) ? student.getUserName() : "同学"));
            item.setAuthorAvatarUrl(MobileAvatarSupport.resolveUserAvatarUrl(minioStorageService, student));
            if (StringUtils.hasText(student.getRootOrgId())) {
                item.setAuthorSchool(ctx.orgNames.get(student.getRootOrgId()));
            }
        }

        if (coverFile != null && StringUtils.hasText(coverFile.getFileUrl())) {
            String url = resolveMediaUrl(coverFile.getFileUrl());
            if ("video".equalsIgnoreCase(coverFile.getFileType())) {
                item.setVideoUrl(url);
            } else {
                item.setCoverUrl(url);
            }
        }
        return item;
    }

    private static final class FeedBuildContext {
        final Map<String, String> subjectNames = new HashMap<>();
        final Map<String, List<String>> gradeNamesByExp = new HashMap<>();
        final Map<String, List<String>> gradeIdsByExp = new HashMap<>();
        final Map<String, List<String>> gradeIdsBySim = new HashMap<>();
        final Map<String, String> semesterNames = new HashMap<>();
        final Map<String, SysUser> users = new HashMap<>();
        final Map<String, String> orgNames = new HashMap<>();
    }

    private FeedBuildContext buildFeedContext(Map<String, MobileExpMsg> expMap,
                                              Map<String, ExpSimulator> simMap,
                                              Map<String, MbWork> workMap) {
        FeedBuildContext ctx = new FeedBuildContext();
        Set<String> subjectIds = new HashSet<>();
        Set<String> userIds = new HashSet<>();
        Set<String> semesterIds = new HashSet<>();
        Set<String> fallbackGradeIds = new HashSet<>();
        List<String> expIds = new ArrayList<>(expMap.keySet());

        for (MobileExpMsg entity : expMap.values()) {
            if (StringUtils.hasText(entity.getSubjectId())) {
                subjectIds.add(entity.getSubjectId());
            }
            if (StringUtils.hasText(entity.getCreateUserId())) {
                userIds.add(entity.getCreateUserId());
            }
            if (StringUtils.hasText(entity.getSemesterId())) {
                semesterIds.add(entity.getSemesterId());
            }
            if (StringUtils.hasText(entity.getGradeId())) {
                fallbackGradeIds.add(entity.getGradeId());
            }
        }
        for (ExpSimulator sim : simMap.values()) {
            if (StringUtils.hasText(sim.getSubjectId())) {
                subjectIds.add(sim.getSubjectId());
            }
            if (StringUtils.hasText(sim.getCreateUserId())) {
                userIds.add(sim.getCreateUserId());
            }
        }
        for (MbWork work : workMap.values()) {
            if (StringUtils.hasText(work.getStudentUserId())) {
                userIds.add(work.getStudentUserId());
            }
        }

        if (!subjectIds.isEmpty()) {
            for (DataSchoolSubject subject : subjectRepository.findAllById(subjectIds)) {
                ctx.subjectNames.put(subject.getSubjectId(), subject.getSubjectName());
            }
        }

        if (!expIds.isEmpty()) {
            Map<String, List<ExpGrade>> gradesByExp = new HashMap<>();
            Set<String> gradeIds = new LinkedHashSet<>(fallbackGradeIds);
            for (ExpGrade grade : expGradeRepository.findByExpIdIn(expIds)) {
                gradesByExp.computeIfAbsent(grade.getExpId(), k -> new ArrayList<>()).add(grade);
                if (StringUtils.hasText(grade.getGradeId())) {
                    gradeIds.add(grade.getGradeId());
                }
            }
            Map<String, String> gradeIdToName = new HashMap<>();
            if (!gradeIds.isEmpty()) {
                for (DataSchoolGrade grade : gradeRepository.findByGradeIdIn(gradeIds)) {
                    gradeIdToName.put(grade.getGradeId(), grade.getGradeName());
                }
            }
            for (String expId : expIds) {
                List<String> names = new ArrayList<>();
                List<String> ids = new ArrayList<>();
                List<ExpGrade> grades = gradesByExp.getOrDefault(expId, Collections.emptyList());
                for (ExpGrade grade : grades) {
                    if (StringUtils.hasText(grade.getGradeId())) {
                        ids.add(grade.getGradeId().trim());
                    }
                    String name = gradeIdToName.get(grade.getGradeId());
                    if (StringUtils.hasText(name)) {
                        names.add(name);
                    }
                }
                if (names.isEmpty()) {
                    MobileExpMsg entity = expMap.get(expId);
                    if (entity != null && StringUtils.hasText(entity.getGradeId())) {
                        ids.add(entity.getGradeId().trim());
                        String name = gradeIdToName.get(entity.getGradeId());
                        if (StringUtils.hasText(name)) {
                            names.add(name);
                        }
                    }
                }
                ctx.gradeNamesByExp.put(expId, names);
                ctx.gradeIdsByExp.put(expId, ids);
            }
        }

        List<String> simIds = new ArrayList<>(simMap.keySet());
        if (!simIds.isEmpty()) {
            for (Object[] row : homeRepository.findGradeIdsBySimulatorIds(simIds)) {
                if (row == null || row.length < 2 || row[0] == null || row[1] == null) {
                    continue;
                }
                String simId = String.valueOf(row[0]).trim();
                String gradeId = String.valueOf(row[1]).trim();
                if (!StringUtils.hasText(simId) || !StringUtils.hasText(gradeId)) {
                    continue;
                }
                ctx.gradeIdsBySim.computeIfAbsent(simId, k -> new ArrayList<>());
                List<String> ids = ctx.gradeIdsBySim.get(simId);
                if (!ids.contains(gradeId)) {
                    ids.add(gradeId);
                }
            }
        }

        if (!semesterIds.isEmpty()) {
            for (DataSchoolSemester semester : semesterRepository.findAllById(semesterIds)) {
                ctx.semesterNames.put(semester.getSemesterId(), semester.getSemesterName());
            }
        }

        if (!userIds.isEmpty()) {
            for (SysUser user : sysUserRepository.findAllById(userIds)) {
                ctx.users.put(user.getUserId(), user);
            }
            Set<String> orgIds = new HashSet<>();
            for (SysUser user : ctx.users.values()) {
                if (StringUtils.hasText(user.getRootOrgId())) {
                    orgIds.add(user.getRootOrgId());
                }
                if (StringUtils.hasText(user.getUserOrgId())) {
                    orgIds.add(user.getUserOrgId());
                }
            }
            if (!orgIds.isEmpty()) {
                for (SysOrg org : sysOrgRepository.findAllById(orgIds)) {
                    ctx.orgNames.put(org.getOrgId(), org.getOrgName());
                }
            }
        }

        return ctx;
    }

    /**
     * 将实体转为首页信息流 DTO
     */
    private HomeFeedItem toFeedItem(MobileExpMsg entity, FeedBuildContext ctx) {
        HomeFeedItem item = new HomeFeedItem();
        item.setId(entity.getExpId());
        item.setTitle(entity.getExpName());

        String expType = entity.getExpType();
        if (StringUtils.hasText(entity.getSimulatorUrl()) || StringUtils.hasText(entity.getSimulatorId())) {
            item.setType("simulation");
            item.setTagLabel("模拟");
            item.setTagType("sim");
        } else if ("standard".equals(expType)) {
            item.setType("experiment");
            item.setTagLabel("标准");
            item.setTagType("exp");
        } else if ("teacher".equals(expType) || "teaching".equals(expType)) {
            item.setType("experiment");
            item.setTagLabel("教学");
            item.setTagType("exp");
        } else if ("student".equals(expType)) {
            item.setType("experiment");
            item.setTagLabel("学生");
            item.setTagType("exp");
        } else {
            item.setType("experiment");
            item.setTagLabel("实验");
            item.setTagType("exp");
        }

        String subjectName = ctx.subjectNames.get(entity.getSubjectId());
        item.setSubject(subjectName != null ? subjectName : "");
        item.setGrade(formatGradeNames(entity, ctx));

        Integer likeNum = entity.getLikeNum();
        item.setPlayCount(likeNum != null ? likeNum : 0);

        applyAuthor(entity.getCreateUserId(), item, ctx);

        item.setGradientClass(SUBJECT_GRADIENT.getOrDefault(subjectName, DEFAULT_GRADIENT));
        item.setSimulatorId(entity.getSimulatorId());

        return item;
    }

    private HomeFeedItem toSimulatorFeedItem(ExpSimulator sim, FeedBuildContext ctx) {
        HomeFeedItem item = new HomeFeedItem();
        item.setId(sim.getSimulatorId());
        item.setSimulatorId(sim.getSimulatorId());
        item.setTitle(sim.getSimulatorName());
        item.setType("simulation");
        item.setTagLabel("模拟");
        item.setTagType("sim");

        String subjectName = ctx.subjectNames.get(sim.getSubjectId());
        item.setSubject(subjectName != null ? subjectName : "");
        item.setGrade("");
        item.setPlayCount(0);
        applyAuthor(sim.getCreateUserId(), item, ctx);
        item.setGradientClass(SUBJECT_GRADIENT.getOrDefault(subjectName, DEFAULT_GRADIENT));
        if (StringUtils.hasText(sim.getCoverImageUrl())) {
            item.setCoverUrl(resolveMediaUrl(sim.getCoverImageUrl()));
        }
        return item;
    }

    /**
     * 批量解析封面与视频：封面仅使用图片；视频 URL 单独返回供前端 video 预览（与实验详情一致）
     */
    private void applyCoverUrls(List<HomeFeedItem> items) {
        if (items == null || items.isEmpty()) {
            return;
        }

        List<String> expIdsForMedia = items.stream()
                .filter(i -> "experiment".equals(i.getType())
                        || ("simulation".equals(i.getType())
                        && StringUtils.hasText(i.getSimulatorId())
                        && !i.getId().equals(i.getSimulatorId())))
                .map(HomeFeedItem::getId)
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.toList());

        Map<String, String> simCoverMap = new HashMap<>();
        List<String> simIds = items.stream()
                .filter(i -> "simulation".equals(i.getType()) && StringUtils.hasText(i.getSimulatorId()))
                .map(i -> StringUtils.hasText(i.getSimulatorId()) ? i.getSimulatorId() : i.getId())
                .distinct()
                .collect(Collectors.toList());
        if (!simIds.isEmpty()) {
            for (ExpSimulator sim : expSimulatorRepository.findAllById(simIds)) {
                if (StringUtils.hasText(sim.getCoverImageUrl())) {
                    simCoverMap.put(sim.getSimulatorId(), sim.getCoverImageUrl());
                }
            }
        }

        Map<String, String> imageCoverMap = new HashMap<>();
        Map<String, String> videoUrlMap = new HashMap<>();

        if (!expIdsForMedia.isEmpty()) {
            List<ExpVideo> allVideos = expVideoRepository.findByExpIdIn(expIdsForMedia);
            Map<String, List<ExpVideo>> videosByExp = allVideos.stream()
                    .collect(Collectors.groupingBy(ExpVideo::getExpId));

            Set<String> fileIds = new HashSet<>();
            Map<String, ExpVideo> firstVideoMap = new HashMap<>();
            Map<String, List<ExpVideo>> videosByExpOrdered = new HashMap<>();
            for (String expId : expIdsForMedia) {
                List<ExpVideo> videos = videosByExp.get(expId);
                if (videos == null || videos.isEmpty()) {
                    continue;
                }
                videos.sort(Comparator.comparing(v -> v.getSortOrder() == null ? 0 : v.getSortOrder()));
                videosByExpOrdered.put(expId, videos);
                ExpVideo first = videos.get(0);
                firstVideoMap.put(expId, first);
                if (StringUtils.hasText(first.getFileId())) {
                    fileIds.add(first.getFileId());
                }
                for (ExpVideo video : videos) {
                    if (StringUtils.hasText(video.getFileId())) {
                        fileIds.add(video.getFileId());
                    }
                }
            }

            Map<String, DataFile> fileMap = new HashMap<>();
            if (!fileIds.isEmpty()) {
                for (DataFile file : dataFileRepository.findAllById(fileIds)) {
                    fileMap.put(file.getFileId(), file);
                }
            }

            for (String expId : expIdsForMedia) {
                List<ExpVideo> videos = videosByExpOrdered.get(expId);
                if (videos == null || videos.isEmpty()) {
                    continue;
                }
                for (ExpVideo video : videos) {
                    DataFile file = StringUtils.hasText(video.getFileId()) ? fileMap.get(video.getFileId()) : null;
                    collectExpThumbnail(expId, video, file, imageCoverMap);
                }
                for (ExpVideo video : videos) {
                    DataFile file = StringUtils.hasText(video.getFileId()) ? fileMap.get(video.getFileId()) : null;
                    if (assignExpVideo(expId, video, file, videoUrlMap)) {
                        break;
                    }
                }
            }

            Map<String, String> materialCoverMap = new HashMap<>();
            for (ExpMaterial material : expMaterialRepository.findByExpIdIn(expIdsForMedia)) {
                if (!StringUtils.hasText(material.getMainPicUrl())) {
                    continue;
                }
                materialCoverMap.putIfAbsent(material.getExpId(), material.getMainPicUrl());
            }
            for (String expId : expIdsForMedia) {
                if (!imageCoverMap.containsKey(expId) && materialCoverMap.containsKey(expId)) {
                    imageCoverMap.put(expId, materialCoverMap.get(expId));
                }
            }
        }

        for (HomeFeedItem item : items) {
            if ("work".equals(item.getType())) {
                continue;
            }
            boolean standaloneSim = "simulation".equals(item.getType())
                    && StringUtils.hasText(item.getSimulatorId())
                    && item.getId().equals(item.getSimulatorId());

            if (standaloneSim) {
                String simCover = simCoverMap.get(item.getId());
                if (StringUtils.hasText(simCover)) {
                    item.setCoverUrl(resolveMediaUrl(simCover));
                }
                continue;
            }

            String videoUrl = videoUrlMap.get(item.getId());
            if (StringUtils.hasText(videoUrl)) {
                item.setVideoUrl(resolveMediaUrl(videoUrl));
            }
            String imageCover = imageCoverMap.get(item.getId());
            if (StringUtils.hasText(imageCover)) {
                item.setCoverUrl(resolveMediaUrl(imageCover));
            }

            if (!StringUtils.hasText(item.getCoverUrl())
                    && !StringUtils.hasText(item.getVideoUrl())
                    && StringUtils.hasText(item.getSimulatorId())) {
                String simCover = simCoverMap.get(item.getSimulatorId());
                if (StringUtils.hasText(simCover)) {
                    item.setCoverUrl(resolveMediaUrl(simCover));
                }
            }
        }
    }

    private String resolveMediaUrl(String fileUrl) {
        return MobileMediaUrlSupport.resolve(minioStorageService, fileUrl);
    }

    /** 收集实验缩略图（coverUrl 用）：先找独立图片，无图时用视频 URL 兜底 */
    private void collectExpThumbnail(String expId, ExpVideo video, DataFile file,
                                     Map<String, String> imageCoverMap) {
        if (imageCoverMap.containsKey(expId)) {
            return;
        }
        String fileCoverUrl = file != null ? file.getCoverImageUrl() : null;
        String videoFieldUrl = video.getVideoUrl();
        String fileMediaUrl = file != null ? file.getFileUrl() : null;

        // 优先 data_file.cover_image_url 图片
        String thumb = MobileMediaUrlSupport.pickBestMediaUrl(fileCoverUrl);
        // 其次图片型 video_url 或 file_url
        if (!StringUtils.hasText(thumb) && isLikelyImageUrl(videoFieldUrl)) {
            thumb = MobileMediaUrlSupport.pickBestMediaUrl(videoFieldUrl);
        }
        if (!StringUtils.hasText(thumb) && isLikelyImageUrl(fileMediaUrl)) {
            thumb = MobileMediaUrlSupport.pickBestMediaUrl(fileMediaUrl);
        }
        // 纯图片直接放 cover
        if (StringUtils.hasText(thumb) && !isVideoMedia(thumb, file != null ? file.getFileExt() : null)) {
            imageCoverMap.put(expId, thumb);
            return;
        }
        // 纯视频实验：把视频 URL 也放 coverUrl，前端会走 posterVideoSrc 展示首帧
        if (!StringUtils.hasText(thumb)) {
            thumb = MobileMediaUrlSupport.pickBestMediaUrl(videoFieldUrl, fileMediaUrl);
        }
        if (StringUtils.hasText(thumb)) {
            imageCoverMap.put(expId, thumb);
        }
    }

    /** 解析实验视频 URL（详情页播放用，首页卡片不直接使用） */
    private boolean assignExpVideo(String expId, ExpVideo video, DataFile file,
                                   Map<String, String> videoUrlMap) {
        if (videoUrlMap.containsKey(expId)) {
            return true;
        }
        String fileMediaUrl = file != null ? file.getFileUrl() : null;
        String videoFieldUrl = video.getVideoUrl();
        String videoCandidate = MobileMediaUrlSupport.pickBestMediaUrl(videoFieldUrl, fileMediaUrl);

        if (!StringUtils.hasText(videoCandidate)) {
            return false;
        }
        if (isVideoMedia(videoCandidate, file != null ? file.getFileExt() : null)) {
            videoUrlMap.put(expId, videoCandidate);
            return true;
        }
        return false;
    }

    private boolean isVideoMedia(String url, String fileExt) {
        if (StringUtils.hasText(fileExt)) {
            String ext = fileExt.trim().toLowerCase();
            if (isVideoExtension(ext)) {
                return true;
            }
            if (isImageExtension(ext)) {
                return false;
            }
        }
        if (isLikelyVideoUrl(url)) {
            return true;
        }
        if (isLikelyImageUrl(url)) {
            return false;
        }
        return StringUtils.hasText(url);
    }

    private static boolean isVideoExtension(String ext) {
        return "mp4".equals(ext) || "webm".equals(ext) || "mov".equals(ext)
                || "m4v".equals(ext) || "avi".equals(ext) || "ogg".equals(ext) || "mkv".equals(ext);
    }

    private static boolean isImageExtension(String ext) {
        return "jpg".equals(ext) || "jpeg".equals(ext) || "png".equals(ext)
                || "webp".equals(ext) || "gif".equals(ext) || "bmp".equals(ext) || "svg".equals(ext);
    }

    private static boolean isLikelyVideoUrl(String url) {
        if (!StringUtils.hasText(url)) {
            return false;
        }
        String lower = url.toLowerCase();
        return lower.contains(".mp4") || lower.contains(".webm") || lower.contains(".mov")
                || lower.contains(".m4v") || lower.contains(".ogg");
    }

    private static boolean isLikelyImageUrl(String url) {
        if (!StringUtils.hasText(url)) {
            return false;
        }
        String lower = url.toLowerCase();
        return lower.contains(".jpg") || lower.contains(".jpeg") || lower.contains(".png")
                || lower.contains(".webp") || lower.contains(".gif") || lower.contains(".bmp");
    }

    private String formatGradeNames(MobileExpMsg entity, FeedBuildContext ctx) {
        List<String> gradeNames = ctx.gradeNamesByExp.getOrDefault(entity.getExpId(), Collections.emptyList());
        StringBuilder sb = new StringBuilder();
        if (gradeNames != null && !gradeNames.isEmpty()) {
            sb.append(String.join("、", gradeNames));
        }
        if (StringUtils.hasText(entity.getSemesterId())) {
            String semesterName = ctx.semesterNames.get(entity.getSemesterId());
            if (StringUtils.hasText(semesterName) && sb.length() > 0) {
                sb.append("（").append(semesterName).append("）");
            }
        }
        return sb.toString();
    }

    private void applyAuthor(String userId, HomeFeedItem item, FeedBuildContext ctx) {
        if (!StringUtils.hasText(userId)) {
            return;
        }
        SysUser user = ctx.users.get(userId);
        if (user == null) {
            return;
        }
        item.setAuthor(StringUtils.hasText(user.getUserName())
                ? user.getUserName() : user.getLoginName());
        item.setAuthorSchool(resolveSchoolFullName(user, ctx.orgNames));
    }

    private String resolveSchoolFullName(SysUser user, Map<String, String> orgNames) {
        if (StringUtils.hasText(user.getRootOrgId())) {
            String name = orgNames.get(user.getRootOrgId());
            if (StringUtils.hasText(name)) {
                return name;
            }
        }
        if (StringUtils.hasText(user.getUserOrgId())) {
            String name = orgNames.get(user.getUserOrgId());
            if (StringUtils.hasText(name)) {
                return name;
            }
        }
        return null;
    }
}
