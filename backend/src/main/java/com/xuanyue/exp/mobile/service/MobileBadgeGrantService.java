package com.xuanyue.exp.mobile.service;

import com.xuanyue.exp.mobile.entity.MbBadgeDef;
import com.xuanyue.exp.mobile.entity.MbBadgeProgress;
import com.xuanyue.exp.mobile.entity.MbQuizRecord;
import com.xuanyue.exp.mobile.repository.MbBadgeDefRepository;
import com.xuanyue.exp.mobile.repository.MbBadgeProgressRepository;
import com.xuanyue.exp.mobile.repository.MbQuizRecordRepository;
import com.xuanyue.exp.mobile.repository.MbTaskSubmissionRepository;
import com.xuanyue.exp.mobile.repository.MbWorkRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class MobileBadgeGrantService {

    private static final List<String> TASK_DONE_STATES = Arrays.asList("done", "submitted", "reviewed");
    private static final Set<String> QUIZ_CRITERIA = new HashSet<String>(Arrays.asList(
            "quiz_first", "quiz_correct", "quiz_streak"));
    private static final Set<String> WORK_CRITERIA = new HashSet<String>(Arrays.asList(
            "work_first", "work_submit_count"));
    private static final Set<String> TASK_CRITERIA = new HashSet<String>(Arrays.asList(
            "exp_task_done"));
    private static final Set<String> FEATURED_CRITERIA = new HashSet<String>(Arrays.asList(
            "work_featured"));

    private final MbBadgeDefRepository badgeDefRepository;
    private final MbBadgeProgressRepository badgeProgressRepository;
    private final MbQuizRecordRepository quizRecordRepository;
    private final MbWorkRepository workRepository;
    private final MbTaskSubmissionRepository taskSubmissionRepository;
    private final MobilePointsService pointsService;
    private final MobileGrowthEventService growthEventService;

    public MobileBadgeGrantService(MbBadgeDefRepository badgeDefRepository,
                                   MbBadgeProgressRepository badgeProgressRepository,
                                   MbQuizRecordRepository quizRecordRepository,
                                   MbWorkRepository workRepository,
                                   MbTaskSubmissionRepository taskSubmissionRepository,
                                   MobilePointsService pointsService,
                                   MobileGrowthEventService growthEventService) {
        this.badgeDefRepository = badgeDefRepository;
        this.badgeProgressRepository = badgeProgressRepository;
        this.quizRecordRepository = quizRecordRepository;
        this.workRepository = workRepository;
        this.taskSubmissionRepository = taskSubmissionRepository;
        this.pointsService = pointsService;
        this.growthEventService = growthEventService;
    }

    @Transactional
    public void onQuizSubmitted(String userId, MbQuizRecord record) {
        evaluate(userId, QUIZ_CRITERIA, record);
    }

    @Transactional
    public void onWorkSubmitted(String userId) {
        evaluate(userId, WORK_CRITERIA, null);
    }

    @Transactional
    public void onTaskCompleted(String userId) {
        evaluate(userId, TASK_CRITERIA, null);
    }

    @Transactional
    public void onWorkFeatured(String userId) {
        evaluate(userId, FEATURED_CRITERIA, null);
    }

    @Transactional
    public void manualGrant(String userId, String badgeId, boolean earned) {
        if (!StringUtils.hasText(userId) || !StringUtils.hasText(badgeId)) {
            throw new IllegalArgumentException("用户与勋章不能为空");
        }
        MbBadgeDef def = badgeDefRepository.findById(badgeId.trim())
                .orElseThrow(() -> new IllegalArgumentException("勋章不存在"));
        int target = def.getCriteriaValue() != null && def.getCriteriaValue() > 0
                ? def.getCriteriaValue() : 1;
        MbBadgeProgress progress = badgeProgressRepository.findByUserIdAndBadgeId(userId.trim(), badgeId.trim())
                .orElseGet(MbBadgeProgress::new);
        if (progress.getId() == null) {
            progress.setUserId(userId.trim());
            progress.setBadgeId(badgeId.trim());
        }
        progress.setProgressTarget(target);
        boolean wasEarned = "y".equalsIgnoreCase(progress.getEarned());
        if (earned) {
            progress.setEarned("y");
            progress.setProgressCurrent(Math.max(
                    progress.getProgressCurrent() != null ? progress.getProgressCurrent() : 0, target));
            if (progress.getEarnedTime() == null) {
                progress.setEarnedTime(new java.util.Date());
            }
            badgeProgressRepository.save(progress);
            if (!wasEarned) {
                onBadgeNewlyEarned(userId.trim(), def);
            }
        } else {
            progress.setEarned("n");
            progress.setEarnedTime(null);
            badgeProgressRepository.save(progress);
        }
    }

    private void evaluate(String userId, Set<String> criteriaFilter, MbQuizRecord latestRecord) {
        if (!StringUtils.hasText(userId)) {
            return;
        }
        List<MbBadgeDef> defs = badgeDefRepository.findByStatusOrderBySortOrderAsc("y");
        for (MbBadgeDef def : defs) {
            String type = normalizeCriteriaType(def.getCriteriaType());
            if (!criteriaFilter.contains(type)) {
                continue;
            }
            int target = def.getCriteriaValue() != null && def.getCriteriaValue() > 0
                    ? def.getCriteriaValue() : 1;
            int current = resolveMetric(userId, type, latestRecord);
            upsertProgress(userId, def, current, target);
        }
    }

    private void upsertProgress(String userId, MbBadgeDef def, int current, int target) {
        Optional<MbBadgeProgress> existingOpt = badgeProgressRepository.findByUserIdAndBadgeId(userId, def.getBadgeId());
        MbBadgeProgress progress = existingOpt.orElseGet(MbBadgeProgress::new);
        if (progress.getId() == null) {
            progress.setUserId(userId);
            progress.setBadgeId(def.getBadgeId());
        }
        boolean wasEarned = "y".equalsIgnoreCase(progress.getEarned());
        progress.setProgressCurrent(Math.min(current, target));
        progress.setProgressTarget(target);
        if (current >= target && !wasEarned) {
            progress.setEarned("y");
            progress.setProgressCurrent(target);
            progress.setEarnedTime(new java.util.Date());
        }
        badgeProgressRepository.save(progress);
        if (current >= target && !wasEarned) {
            onBadgeNewlyEarned(userId, def);
        }
    }

    private void onBadgeNewlyEarned(String userId, MbBadgeDef def) {
        growthEventService.onBadgeEarned(userId, def);
        int reward = def.getRewardPoints() != null ? def.getRewardPoints() : 0;
        if (reward > 0) {
            pointsService.credit(userId, reward, "badge", def.getBadgeId(),
                    "获得勋章：" + def.getTitle());
        }
    }

    private int resolveMetric(String userId, String type, MbQuizRecord latestRecord) {
        switch (type) {
            case "work_first":
            case "work_submit_count":
                return (int) workRepository.countByStudentUserIdAndStatus(userId, "y");
            case "exp_task_done":
                return (int) taskSubmissionRepository.countByStudentUserIdAndStateIn(userId, TASK_DONE_STATES);
            case "quiz_first":
                return (int) quizRecordRepository.countByUserId(userId);
            case "quiz_correct":
                return (int) quizRecordRepository.sumScoreByUserId(userId);
            case "quiz_streak":
                return resolveQuizStreak(userId, latestRecord);
            case "work_featured":
                return (int) workRepository.countByStudentUserIdAndStatusAndIsFeatured(userId, "y", "y");
            default:
                return 0;
        }
    }

    private int resolveQuizStreak(String userId, MbQuizRecord latestRecord) {
        if (latestRecord == null || latestRecord.getQuizDate() == null) {
            return 0;
        }
        if (!"y".equalsIgnoreCase(latestRecord.getPerfect())) {
            return 0;
        }
        LocalDate today = latestRecord.getQuizDate().toLocalDate();
        int streak = 1;
        LocalDate cursor = today.minusDays(1);
        while (true) {
            Optional<MbQuizRecord> recordOpt = quizRecordRepository.findByUserIdAndQuizDate(
                    userId, java.sql.Date.valueOf(cursor));
            if (!recordOpt.isPresent() || !"y".equalsIgnoreCase(recordOpt.get().getPerfect())) {
                break;
            }
            streak++;
            cursor = cursor.minusDays(1);
        }
        return streak;
    }

    private String normalizeCriteriaType(String criteriaType) {
        if (!StringUtils.hasText(criteriaType)) {
            return "";
        }
        String type = criteriaType.trim().toLowerCase();
        if ("upload_count".equals(type)) {
            return "work_submit_count";
        }
        if ("exp_count".equals(type)) {
            return "exp_task_done";
        }
        return type;
    }

    /** 勋章墙展示：实时统计进度（无 progress 行时也能显示 0/N） */
    public int resolveCurrentCount(String userId, MbBadgeDef def) {
        if (!StringUtils.hasText(userId) || def == null) {
            return 0;
        }
        return resolveMetric(userId, normalizeCriteriaType(def.getCriteriaType()), null);
    }

    public int resolveTarget(MbBadgeDef def) {
        if (def == null || def.getCriteriaValue() == null || def.getCriteriaValue() <= 0) {
            return 1;
        }
        return def.getCriteriaValue();
    }

    public String buildProgressLabel(String userId, MbBadgeDef def, MbBadgeProgress prog, boolean earned) {
        if (earned) {
            return null;
        }
        int target = resolveTarget(def);
        int current = prog != null && prog.getProgressCurrent() != null
                ? prog.getProgressCurrent()
                : resolveCurrentCount(userId, def);
        current = Math.min(Math.max(current, 0), target);
        return current + " / " + target;
    }

    public String buildProgressHint(String userId, List<MbBadgeDef> defs, Map<String, MbBadgeProgress> progressMap) {
        MbBadgeDef closest = null;
        int minRemaining = Integer.MAX_VALUE;
        for (MbBadgeDef def : defs) {
            MbBadgeProgress prog = progressMap.get(def.getBadgeId());
            boolean earned = prog != null && "y".equalsIgnoreCase(prog.getEarned());
            if (earned) {
                continue;
            }
            int target = resolveTarget(def);
            int current = prog != null && prog.getProgressCurrent() != null
                    ? prog.getProgressCurrent()
                    : resolveCurrentCount(userId, def);
            int remaining = target - Math.min(current, target);
            if (remaining > 0 && remaining < minRemaining) {
                minRemaining = remaining;
                closest = def;
            }
        }
        if (closest != null && minRemaining < Integer.MAX_VALUE) {
            return "还差 " + minRemaining + " " + unitLabel(closest.getCriteriaType())
                    + "即可解锁「" + closest.getTitle() + "」";
        }
        return "继续完成任务、上传作品与每日答题，解锁更多勋章";
    }

    private String unitLabel(String criteriaType) {
        String type = normalizeCriteriaType(criteriaType);
        if ("quiz_correct".equals(type)) {
            return "题 ";
        }
        if ("quiz_streak".equals(type)) {
            return "天 ";
        }
        if ("work_submit_count".equals(type)) {
            return "次上传 ";
        }
        if ("exp_task_done".equals(type)) {
            return "个任务 ";
        }
        return "步 ";
    }
}
