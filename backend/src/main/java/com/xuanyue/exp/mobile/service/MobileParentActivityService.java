package com.xuanyue.exp.mobile.service;

import com.xuanyue.exp.mobile.dto.MobileGrowthDto;
import com.xuanyue.exp.mobile.dto.ParentDashboardDto;
import com.xuanyue.exp.mobile.entity.MbBadgeDef;
import com.xuanyue.exp.mobile.entity.MbBadgeProgress;
import com.xuanyue.exp.mobile.entity.MbQuizRecord;
import com.xuanyue.exp.mobile.entity.MbTask;
import com.xuanyue.exp.mobile.entity.MbTaskSubmission;
import com.xuanyue.exp.mobile.entity.MbWork;
import com.xuanyue.exp.mobile.repository.MbBadgeDefRepository;
import com.xuanyue.exp.mobile.repository.MbBadgeProgressRepository;
import com.xuanyue.exp.mobile.repository.MbQuizRecordRepository;
import com.xuanyue.exp.mobile.repository.MbTaskRepository;
import com.xuanyue.exp.mobile.repository.MbTaskSubmissionRepository;
import com.xuanyue.exp.mobile.repository.MbWorkRepository;
import com.xuanyue.exp.mobile.support.MobileGrowthAccessSupport;
import com.xuanyue.exp.mobile.support.MobileGrowthFilterSupport;
import com.xuanyue.exp.mobile.support.MobileGrowthPlanSupport;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 家长端最近动态：直接从业务表聚合，不依赖 mb_growth_event。
 */
@Service
public class MobileParentActivityService {

    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");
    private static final int ACTIVITY_LIMIT = 5;
    private static final List<String> TASK_ACTIVITY_STATES = Arrays.asList("submitted", "reviewed", "done");

    private final MbTaskSubmissionRepository taskSubmissionRepository;
    private final MbTaskRepository taskRepository;
    private final MbWorkRepository workRepository;
    private final MbBadgeProgressRepository badgeProgressRepository;
    private final MbBadgeDefRepository badgeDefRepository;
    private final MbQuizRecordRepository quizRecordRepository;
    private final MobileGrowthPlanSupport growthPlanSupport;
    private final MobileQuizConfigService quizConfigService;

    public MobileParentActivityService(MbTaskSubmissionRepository taskSubmissionRepository,
                                       MbTaskRepository taskRepository,
                                       MbWorkRepository workRepository,
                                       MbBadgeProgressRepository badgeProgressRepository,
                                       MbBadgeDefRepository badgeDefRepository,
                                       MbQuizRecordRepository quizRecordRepository,
                                       MobileGrowthPlanSupport growthPlanSupport,
                                       MobileQuizConfigService quizConfigService) {
        this.taskSubmissionRepository = taskSubmissionRepository;
        this.taskRepository = taskRepository;
        this.workRepository = workRepository;
        this.badgeProgressRepository = badgeProgressRepository;
        this.badgeDefRepository = badgeDefRepository;
        this.quizRecordRepository = quizRecordRepository;
        this.growthPlanSupport = growthPlanSupport;
        this.quizConfigService = quizConfigService;
    }

    @Transactional(readOnly = true)
    public ParentActivitySnapshot resolve(String childUserId) {
        ParentActivitySnapshot snapshot = new ParentActivitySnapshot();
        if (!StringUtils.hasText(childUserId)) {
            snapshot.setActivities(Collections.emptyList());
            snapshot.setQuizToday(buildQuizDisabled());
            return snapshot;
        }
        String uid = childUserId.trim();
        MobileGrowthDto.Plan plan = growthPlanSupport.resolvePlan(uid);
        if (MobileGrowthAccessSupport.isSelfOnly(plan.getVisibilityKey())) {
            snapshot.setRestricted(true);
            snapshot.setActivities(Collections.emptyList());
            snapshot.setQuizToday(null);
            return snapshot;
        }

        LocalDate rangeStart = MobileGrowthFilterSupport.resolveRangeStart(plan.getRangeKey(), LocalDate.now(ZONE));
        Set<String> allowed = resolveParentContentKeys(plan.getContentKeys());

        List<ActivityCandidate> candidates = new ArrayList<ActivityCandidate>();
        if (allowed.contains("task")) {
            candidates.addAll(collectTaskActivities(uid, rangeStart));
        }
        if (allowed.contains("work")) {
            candidates.addAll(collectWorkActivities(uid, rangeStart));
        }
        if (allowed.contains("badge")) {
            candidates.addAll(collectBadgeActivities(uid, rangeStart));
        }

        candidates.sort(Comparator.comparing(ActivityCandidate::getSortTime, Comparator.nullsLast(Comparator.reverseOrder())));
        List<ParentDashboardDto.ActivityItem> items = new ArrayList<ParentDashboardDto.ActivityItem>();
        int limit = Math.min(ACTIVITY_LIMIT, candidates.size());
        for (int i = 0; i < limit; i++) {
            items.add(toActivityItem(candidates.get(i)));
        }
        snapshot.setActivities(items);
        snapshot.setQuizToday(allowed.contains("quiz") ? resolveTodayQuiz(uid) : null);
        return snapshot;
    }

    private Set<String> resolveParentContentKeys(List<String> contentKeys) {
        Set<String> mapped = MobileGrowthFilterSupport.resolveAllowedSourceTypes(contentKeys);
        Set<String> allowed = new HashSet<String>();
        if (mapped.contains("task")) {
            allowed.add("task");
        }
        if (mapped.contains("work")) {
            allowed.add("work");
        }
        if (mapped.contains("badge")) {
            allowed.add("badge");
        }
        if (mapped.contains("quiz")) {
            allowed.add("quiz");
        }
        return allowed;
    }

    private List<ActivityCandidate> collectTaskActivities(String userId, LocalDate rangeStart) {
        List<ActivityCandidate> list = new ArrayList<ActivityCandidate>();
        for (MbTaskSubmission sub : taskSubmissionRepository.findByStudentUserIdOrderByUpdateTimeDesc(userId)) {
            if (sub == null || !StringUtils.hasText(sub.getState())) {
                continue;
            }
            if (!TASK_ACTIVITY_STATES.contains(sub.getState().trim().toLowerCase())) {
                continue;
            }
            Date sortTime = sub.getSubmitTime() != null ? sub.getSubmitTime()
                    : (sub.getUpdateTime() != null ? sub.getUpdateTime() : sub.getCreateTime());
            if (!isWithinRange(sortTime, rangeStart)) {
                continue;
            }
            MbTask task = StringUtils.hasText(sub.getTaskId())
                    ? taskRepository.findById(sub.getTaskId()).orElse(null)
                    : null;
            String taskTitle = task != null && StringUtils.hasText(task.getTitle())
                    ? task.getTitle().trim() : "实验任务";
            ActivityCandidate candidate = new ActivityCandidate();
            candidate.setSortTime(sortTime);
            candidate.setType("completed");
            candidate.setContent("完成实验任务「" + safeTitle(taskTitle) + "」");
            if ("submitted".equalsIgnoreCase(sub.getState())) {
                candidate.setHint("任务已提交，等待老师批阅");
            } else if (StringUtils.hasText(sub.getGrade())) {
                candidate.setHint("评级：" + sub.getGrade().trim().toUpperCase());
            }
            list.add(candidate);
        }
        return list;
    }

    private List<ActivityCandidate> collectWorkActivities(String userId, LocalDate rangeStart) {
        List<ActivityCandidate> list = new ArrayList<ActivityCandidate>();
        for (MbWork work : workRepository.findByStudentUserIdOrderByCreateTimeDesc(userId)) {
            if (work == null || !"y".equalsIgnoreCase(safe(work.getStatus()))) {
                continue;
            }
            Date sortTime = work.getCreateTime();
            if (!isWithinRange(sortTime, rangeStart)) {
                continue;
            }
            ActivityCandidate candidate = new ActivityCandidate();
            candidate.setSortTime(sortTime);
            if ("reviewed".equalsIgnoreCase(safe(work.getReviewStatus()))) {
                candidate.setType("completed");
                candidate.setContent("作品「" + safeTitle(work.getTitle()) + "」已批阅");
                if (StringUtils.hasText(work.getGrade())) {
                    candidate.setHint("评级：" + work.getGrade().trim().toUpperCase());
                } else if (StringUtils.hasText(work.getTeacherReviewText())) {
                    candidate.setHint(work.getTeacherReviewText().trim());
                }
            } else {
                candidate.setType("submitted");
                candidate.setContent("提交作品「" + safeTitle(work.getTitle()) + "」");
                candidate.setHint("等待老师批阅");
            }
            list.add(candidate);
        }
        return list;
    }

    private List<ActivityCandidate> collectBadgeActivities(String userId, LocalDate rangeStart) {
        List<ActivityCandidate> list = new ArrayList<ActivityCandidate>();
        for (MbBadgeProgress progress : badgeProgressRepository.findByUserId(userId)) {
            if (progress == null || !"y".equalsIgnoreCase(safe(progress.getEarned()))
                    || !StringUtils.hasText(progress.getBadgeId())) {
                continue;
            }
            Date sortTime = progress.getEarnedTime() != null ? progress.getEarnedTime() : new Date();
            if (!isWithinRange(sortTime, rangeStart)) {
                continue;
            }
            MbBadgeDef def = badgeDefRepository.findById(progress.getBadgeId()).orElse(null);
            String badgeTitle = def != null && StringUtils.hasText(def.getTitle()) ? def.getTitle().trim() : "勋章";
            ActivityCandidate candidate = new ActivityCandidate();
            candidate.setSortTime(sortTime);
            candidate.setType("completed");
            candidate.setContent("获得「" + safeTitle(badgeTitle) + "」勋章");
            if (def != null && StringUtils.hasText(def.getDescription())) {
                candidate.setHint(def.getDescription().trim());
            }
            list.add(candidate);
        }
        return list;
    }

    private ParentDashboardDto.QuizTodayStatus resolveTodayQuiz(String userId) {
        if (!quizConfigService.isEnabled()) {
            return buildQuizDisabled();
        }
        java.sql.Date todaySql = java.sql.Date.valueOf(LocalDate.now(ZONE));
        Optional<MbQuizRecord> recordOpt = quizRecordRepository.findByUserIdAndQuizDate(userId, todaySql);
        ParentDashboardDto.QuizTodayStatus status = new ParentDashboardDto.QuizTodayStatus();
        if (recordOpt.isPresent()) {
            MbQuizRecord record = recordOpt.get();
            int score = record.getScore() != null ? record.getScore() : 0;
            int total = record.getTotal() != null ? record.getTotal() : 0;
            status.setState("done");
            status.setLabel("今日答题：已完成 " + score + "/" + total);
        } else {
            status.setState("pending");
            int count = quizConfigService.getQuestionsPerDay();
            status.setLabel("今日答题：待完成（" + count + " 题）");
        }
        return status;
    }

    private ParentDashboardDto.QuizTodayStatus buildQuizDisabled() {
        ParentDashboardDto.QuizTodayStatus status = new ParentDashboardDto.QuizTodayStatus();
        status.setState("disabled");
        status.setLabel("每日答题功能已暂停");
        return status;
    }

    private ParentDashboardDto.ActivityItem toActivityItem(ActivityCandidate candidate) {
        ParentDashboardDto.ActivityItem item = new ParentDashboardDto.ActivityItem();
        item.setType(candidate.getType());
        String content = candidate.getContent();
        if (StringUtils.hasText(candidate.getHint())) {
            content = content + " · " + candidate.getHint();
        }
        item.setContent(content);
        item.setTime(MobileGrowthEventService.formatTimeLabel(candidate.getSortTime()));
        return item;
    }

    private boolean isWithinRange(Date sortTime, LocalDate rangeStart) {
        if (rangeStart == null || sortTime == null) {
            return true;
        }
        LocalDate day = sortTime.toInstant().atZone(ZONE).toLocalDate();
        return !day.isBefore(rangeStart);
    }

    private String safeTitle(String title) {
        if (!StringUtils.hasText(title)) {
            return "未命名";
        }
        String trimmed = title.trim();
        return trimmed.length() > 40 ? trimmed.substring(0, 40) + "…" : trimmed;
    }

    private String safe(String value) {
        return value != null ? value.trim() : "";
    }

    public static class ParentActivitySnapshot {
        private boolean restricted;
        private List<ParentDashboardDto.ActivityItem> activities = Collections.emptyList();
        private ParentDashboardDto.QuizTodayStatus quizToday;

        public boolean isRestricted() { return restricted; }
        public void setRestricted(boolean restricted) { this.restricted = restricted; }
        public List<ParentDashboardDto.ActivityItem> getActivities() { return activities; }
        public void setActivities(List<ParentDashboardDto.ActivityItem> activities) { this.activities = activities; }
        public ParentDashboardDto.QuizTodayStatus getQuizToday() { return quizToday; }
        public void setQuizToday(ParentDashboardDto.QuizTodayStatus quizToday) { this.quizToday = quizToday; }
    }

    private static class ActivityCandidate {
        private Date sortTime;
        private String type;
        private String content;
        private String hint;

        public Date getSortTime() { return sortTime; }
        public void setSortTime(Date sortTime) { this.sortTime = sortTime; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getHint() { return hint; }
        public void setHint(String hint) { this.hint = hint; }
    }
}
