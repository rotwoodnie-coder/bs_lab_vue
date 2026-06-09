package com.xuanyue.exp.mobile.service;

import com.xuanyue.exp.common.PageResult;
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
import com.xuanyue.exp.exp.entity.ExpMaterial;
import com.xuanyue.exp.exp.entity.ExpSimulator;
import com.xuanyue.exp.exp.entity.ExpVideo;
import com.xuanyue.exp.exp.repository.ExpMaterialRepository;
import com.xuanyue.exp.exp.repository.ExpSimulatorRepository;
import com.xuanyue.exp.exp.repository.ExpVideoRepository;
import com.xuanyue.exp.exp.service.ExpGradeService;
import com.xuanyue.exp.mobile.dto.HomeFeedItem;
import com.xuanyue.exp.mobile.dto.NoticeDto;
import com.xuanyue.exp.mobile.dto.ParentDashboardDto;
import com.xuanyue.exp.mobile.dto.SubjectItem;
import com.xuanyue.exp.mobile.entity.MobileExpMsg;
import com.xuanyue.exp.mobile.repository.MobileFeedRef;
import com.xuanyue.exp.mobile.repository.MobileHomeRepository;
import com.xuanyue.exp.mobile.entity.MbParentChild;
import com.xuanyue.exp.mobile.repository.MbParentChildRepository;
import com.xuanyue.exp.mobile.repository.MbTaskSubmissionRepository;
import com.xuanyue.exp.mobile.support.MobileUserContext;
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
    private final ExpMaterialRepository expMaterialRepository;
    private final DataFileRepository dataFileRepository;
    private final ExpSimulatorRepository expSimulatorRepository;
    private final MbParentChildRepository parentChildRepository;
    private final MbTaskSubmissionRepository taskSubmissionRepository;
    private final MobileParentActivityService parentActivityService;
    private final MobileSettingsService settingsService;

    /** 年级筛选 key -> grade_id 列表 */
    private static final Map<String, List<String>> GRADE_FILTER_MAP = new LinkedHashMap<>();
    static {
        GRADE_FILTER_MAP.put("g12", Arrays.asList("g1", "g2"));
        GRADE_FILTER_MAP.put("g34", Arrays.asList("g3", "g4"));
        GRADE_FILTER_MAP.put("g56", Arrays.asList("g5", "g6"));
    }

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
                             ExpMaterialRepository expMaterialRepository,
                             DataFileRepository dataFileRepository,
                             ExpSimulatorRepository expSimulatorRepository,
                             MbParentChildRepository parentChildRepository,
                             MbTaskSubmissionRepository taskSubmissionRepository,
                             MobileParentActivityService parentActivityService,
                             MobileSettingsService settingsService) {
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
        this.expMaterialRepository = expMaterialRepository;
        this.dataFileRepository = dataFileRepository;
        this.expSimulatorRepository = expSimulatorRepository;
        this.parentChildRepository = parentChildRepository;
        this.taskSubmissionRepository = taskSubmissionRepository;
        this.parentActivityService = parentActivityService;
        this.settingsService = settingsService;
    }

    /**
     * 获取首页信息流（分页）
     * @param gradeKey 年级筛选 key：all / g12 / g34 / g56
     */
    @Transactional(readOnly = true)
    public PageResult<HomeFeedItem> getFeed(String gradeKey, int page, int size) {
        int offset = Math.max(0, (page - 1)) * size;

        List<Object[]> refs;
        long total;

        List<String> gradeIds = resolveGradeIds(gradeKey);
        if (gradeIds == null) {
            refs = homeRepository.findFeedRefsPageAll(offset, size);
            total = homeRepository.countFeedAll();
        } else {
            refs = homeRepository.findFeedRefsPageByGrades(gradeIds, offset, size);
            total = homeRepository.countFeedByGrades(gradeIds);
        }

        List<HomeFeedItem> items = buildFeedItems(mapFeedRefs(refs));
        applyCoverUrls(items);

        return new PageResult<>(total, items);
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
        int offset = Math.max(0, (page - 1)) * size;
        List<Object[]> refs = homeRepository.findFeedRefsPageByKeyword(kw, offset, size);
        long total = homeRepository.countFeedByKeyword(kw);
        List<HomeFeedItem> items = buildFeedItems(mapFeedRefs(refs));
        applyCoverUrls(items);
        return new PageResult<>(total, items);
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
        Map<String, Long> stats = new LinkedHashMap<>();
        stats.put("publishedExpCount", homeRepository.countFeedAll());
        stats.put("simulatorCount", homeRepository.countStandaloneSimulators());
        return stats;
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
            child.setName(resolveChildDisplayName(bind.getChildUserId()));
            child.setAvatar(child.getName().substring(0, 1));
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
        if (!StringUtils.hasText(gradeKey) || "all".equals(gradeKey)) {
            return null;
        }
        return GRADE_FILTER_MAP.get(gradeKey);
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

        Map<String, MobileExpMsg> expMap = expIds.isEmpty()
                ? Collections.emptyMap()
                : homeRepository.findAllById(expIds).stream()
                .collect(Collectors.toMap(MobileExpMsg::getExpId, e -> e, (a, b) -> a, LinkedHashMap::new));
        Map<String, ExpSimulator> simMap = simIds.isEmpty()
                ? Collections.emptyMap()
                : expSimulatorRepository.findAllById(simIds).stream()
                .collect(Collectors.toMap(ExpSimulator::getSimulatorId, s -> s, (a, b) -> a, LinkedHashMap::new));

        List<HomeFeedItem> items = new ArrayList<>(refs.size());
        for (MobileFeedRef ref : refs) {
            if ("exp_msg".equals(ref.getItemSource())) {
                MobileExpMsg entity = expMap.get(ref.getItemId());
                if (entity != null) {
                    items.add(toFeedItem(entity));
                }
            } else if ("simulator".equals(ref.getItemSource())) {
                ExpSimulator sim = simMap.get(ref.getItemId());
                if (sim != null) {
                    items.add(toSimulatorFeedItem(sim));
                }
            }
        }
        return items;
    }

    /**
     * 将实体转为首页信息流 DTO
     */
    private HomeFeedItem toFeedItem(MobileExpMsg entity) {
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

        String subjectName = resolveSubjectName(entity.getSubjectId());
        item.setSubject(subjectName != null ? subjectName : "");

        item.setGrade(formatGradeNames(entity.getExpId(), entity.getGradeId(), entity.getSemesterId()));

        Integer likeNum = entity.getLikeNum();
        item.setPlayCount(likeNum != null ? likeNum : 0);

        resolveAuthor(entity.getCreateUserId(), item);

        item.setGradientClass(SUBJECT_GRADIENT.getOrDefault(subjectName, DEFAULT_GRADIENT));
        item.setSimulatorId(entity.getSimulatorId());

        return item;
    }

    private HomeFeedItem toSimulatorFeedItem(ExpSimulator sim) {
        HomeFeedItem item = new HomeFeedItem();
        item.setId(sim.getSimulatorId());
        item.setSimulatorId(sim.getSimulatorId());
        item.setTitle(sim.getSimulatorName());
        item.setType("simulation");
        item.setTagLabel("模拟");
        item.setTagType("sim");

        String subjectName = resolveSubjectName(sim.getSubjectId());
        item.setSubject(subjectName != null ? subjectName : "");
        item.setGrade("");
        item.setPlayCount(0);
        resolveAuthor(sim.getCreateUserId(), item);
        item.setGradientClass(SUBJECT_GRADIENT.getOrDefault(subjectName, DEFAULT_GRADIENT));
        if (StringUtils.hasText(sim.getCoverImageUrl())) {
            item.setCoverUrl(sim.getCoverImageUrl());
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
            for (String expId : expIdsForMedia) {
                List<ExpVideo> videos = videosByExp.get(expId);
                if (videos == null || videos.isEmpty()) {
                    continue;
                }
                videos.sort(Comparator.comparing(v -> v.getSortOrder() == null ? 0 : v.getSortOrder()));
                ExpVideo first = videos.get(0);
                firstVideoMap.put(expId, first);
                if (StringUtils.hasText(first.getFileId())) {
                    fileIds.add(first.getFileId());
                }
            }

            Map<String, DataFile> fileMap = new HashMap<>();
            if (!fileIds.isEmpty()) {
                for (DataFile file : dataFileRepository.findAllById(fileIds)) {
                    fileMap.put(file.getFileId(), file);
                }
            }

            for (Map.Entry<String, ExpVideo> entry : firstVideoMap.entrySet()) {
                String expId = entry.getKey();
                ExpVideo first = entry.getValue();
                DataFile file = StringUtils.hasText(first.getFileId()) ? fileMap.get(first.getFileId()) : null;

                String videoUrl = null;
                String cover = null;

                if (file != null) {
                    if (StringUtils.hasText(file.getFileUrl()) && isLikelyVideoUrl(file.getFileUrl())) {
                        videoUrl = file.getFileUrl();
                    } else if (StringUtils.hasText(file.getFileUrl()) && isLikelyImageUrl(file.getFileUrl())) {
                        cover = file.getFileUrl();
                    }
                    if (StringUtils.hasText(file.getCoverImageUrl())) {
                        cover = file.getCoverImageUrl();
                    }
                }

                if (!StringUtils.hasText(videoUrl) && StringUtils.hasText(first.getVideoUrl())) {
                    if (isLikelyVideoUrl(first.getVideoUrl())) {
                        videoUrl = first.getVideoUrl();
                    } else if (!StringUtils.hasText(cover) && isLikelyImageUrl(first.getVideoUrl())) {
                        cover = first.getVideoUrl();
                    }
                }

                if (StringUtils.hasText(videoUrl)) {
                    videoUrlMap.put(expId, videoUrl);
                }
                if (StringUtils.hasText(cover)) {
                    imageCoverMap.put(expId, cover);
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
            boolean standaloneSim = "simulation".equals(item.getType())
                    && StringUtils.hasText(item.getSimulatorId())
                    && item.getId().equals(item.getSimulatorId());

            if (standaloneSim) {
                String simCover = simCoverMap.get(item.getId());
                if (StringUtils.hasText(simCover)) {
                    item.setCoverUrl(simCover);
                }
                continue;
            }

            String videoUrl = videoUrlMap.get(item.getId());
            if (StringUtils.hasText(videoUrl)) {
                item.setVideoUrl(videoUrl);
            }
            String imageCover = imageCoverMap.get(item.getId());
            if (StringUtils.hasText(imageCover)) {
                item.setCoverUrl(imageCover);
            } else if (StringUtils.hasText(videoUrl)) {
                item.setCoverUrl(videoUrl);
            }
        }
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

    /**
     * 格式化年级显示：优先使用 exp_grade 关联的多个年级
     */
    private String formatGradeNames(String expId, String gradeId, String semesterId) {
        List<String> gradeNames = expGradeService.listGradeNames(expId);
        StringBuilder sb = new StringBuilder();
        if (gradeNames != null && !gradeNames.isEmpty()) {
            sb.append(String.join("、", gradeNames));
        } else if (StringUtils.hasText(gradeId)) {
            DataSchoolGrade grade = gradeRepository.findById(gradeId).orElse(null);
            if (grade != null) {
                sb.append(grade.getGradeName());
            }
        }
        if (StringUtils.hasText(semesterId)) {
            DataSchoolSemester semester = semesterRepository.findById(semesterId).orElse(null);
            if (semester != null && sb.length() > 0) {
                sb.append("（").append(semester.getSemesterName()).append("）");
            }
        }
        return sb.toString();
    }

    private void resolveAuthor(String userId, HomeFeedItem item) {
        if (!StringUtils.hasText(userId)) return;
        try {
            SysUser user = sysUserRepository.findById(userId).orElse(null);
            if (user != null) {
                item.setAuthor(StringUtils.hasText(user.getUserName())
                        ? user.getUserName() : user.getLoginName());
                item.setAuthorTitle(resolvePrefTitleName(user.getPrefTitleId()));
                item.setAuthorSchool(resolveSchoolFullName(user));
            }
        } catch (Exception e) {
            log.warn("解析作者信息失败 userId={}", userId, e);
        }
    }

    private String resolvePrefTitleName(String prefTitleId) {
        if (!StringUtils.hasText(prefTitleId)) return null;
        try {
            Object obj = dataDictService.get("data_pref_title", prefTitleId);
            if (obj instanceof Map) {
                Object name = ((Map<?, ?>) obj).get("title_name");
                return name != null ? name.toString() : null;
            }
        } catch (Exception e) {
            log.warn("查询职称失败 prefTitleId={}", prefTitleId, e);
        }
        return null;
    }

    /**
     * 解析老师所在学校全名（优先 rootOrgId）
     */
    private String resolveSchoolFullName(SysUser user) {
        if (StringUtils.hasText(user.getRootOrgId())) {
            SysOrg rootOrg = sysOrgRepository.findById(user.getRootOrgId()).orElse(null);
            if (rootOrg != null && StringUtils.hasText(rootOrg.getOrgName())) {
                return rootOrg.getOrgName();
            }
        }
        if (StringUtils.hasText(user.getUserOrgId())) {
            SysOrg org = sysOrgRepository.findById(user.getUserOrgId()).orElse(null);
            if (org != null && StringUtils.hasText(org.getOrgName())) {
                return org.getOrgName();
            }
        }
        return null;
    }

    private String resolveSubjectName(String subjectId) {
        if (!StringUtils.hasText(subjectId)) return null;
        try {
            return subjectRepository.findById(subjectId)
                    .map(DataSchoolSubject::getSubjectName)
                    .orElse(null);
        } catch (Exception e) {
            log.warn("查询学科名失败 subjectId={}", subjectId, e);
            return null;
        }
    }
}
