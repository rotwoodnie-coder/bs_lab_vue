package com.xuanyue.exp.mobile.service;

import com.xuanyue.exp.mobile.entity.MbBadgeDef;
import com.xuanyue.exp.mobile.entity.MbBadgeProgress;
import com.xuanyue.exp.mobile.entity.MbQuizRecord;
import com.xuanyue.exp.mobile.entity.MbTask;
import com.xuanyue.exp.mobile.entity.MbTaskSubmission;
import com.xuanyue.exp.mobile.entity.MbWork;
import com.xuanyue.exp.mobile.repository.MbBadgeDefRepository;
import com.xuanyue.exp.mobile.repository.MbBadgeProgressRepository;
import com.xuanyue.exp.mobile.repository.MbGrowthEventRepository;
import com.xuanyue.exp.mobile.repository.MbQuizRecordRepository;
import com.xuanyue.exp.mobile.repository.MbTaskRepository;
import com.xuanyue.exp.mobile.repository.MbTaskSubmissionRepository;
import com.xuanyue.exp.mobile.repository.MbWorkRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

/**
 * 从业务源表补写缺失的成长轨迹事件（幂等）。
 */
@Service
public class MobileGrowthBackfillService {

    private static final List<String> TASK_STATES = Arrays.asList("submitted", "reviewed", "done");

    private final MbQuizRecordRepository quizRecordRepository;
    private final MbWorkRepository workRepository;
    private final MbTaskSubmissionRepository taskSubmissionRepository;
    private final MbTaskRepository taskRepository;
    private final MbBadgeProgressRepository badgeProgressRepository;
    private final MbBadgeDefRepository badgeDefRepository;
    private final MbGrowthEventRepository growthEventRepository;
    private final MobileGrowthEventService growthEventService;

    public MobileGrowthBackfillService(MbQuizRecordRepository quizRecordRepository,
                                       MbWorkRepository workRepository,
                                       MbTaskSubmissionRepository taskSubmissionRepository,
                                       MbTaskRepository taskRepository,
                                       MbBadgeProgressRepository badgeProgressRepository,
                                       MbBadgeDefRepository badgeDefRepository,
                                       MbGrowthEventRepository growthEventRepository,
                                       MobileGrowthEventService growthEventService) {
        this.quizRecordRepository = quizRecordRepository;
        this.workRepository = workRepository;
        this.taskSubmissionRepository = taskSubmissionRepository;
        this.taskRepository = taskRepository;
        this.badgeProgressRepository = badgeProgressRepository;
        this.badgeDefRepository = badgeDefRepository;
        this.growthEventRepository = growthEventRepository;
        this.growthEventService = growthEventService;
    }

    @Transactional
    public void syncMissingEvents(String userId) {
        if (!StringUtils.hasText(userId)) {
            return;
        }
        String uid = userId.trim();
        backfillQuiz(uid);
        backfillWorks(uid);
        backfillTasks(uid);
        backfillBadges(uid);
    }

    private void backfillQuiz(String userId) {
        for (MbQuizRecord record : quizRecordRepository.findByUserIdOrderByQuizDateDesc(userId)) {
            if (record == null || !StringUtils.hasText(record.getRecordId())) {
                continue;
            }
            if (growthEventRepository.findByUserIdAndSourceTypeAndSourceId(userId, "quiz", record.getRecordId()).isPresent()) {
                continue;
            }
            int points = record.getPoints() != null ? record.getPoints() : 0;
            growthEventService.onQuizSubmitted(userId, record, points);
        }
    }

    private void backfillWorks(String userId) {
        for (MbWork work : workRepository.findByStudentUserIdOrderByCreateTimeDesc(userId)) {
            if (work == null || !StringUtils.hasText(work.getWorkId()) || !"y".equalsIgnoreCase(safe(work.getStatus()))) {
                continue;
            }
            if (!growthEventRepository.findByUserIdAndSourceTypeAndSourceId(userId, "work", work.getWorkId()).isPresent()) {
                growthEventService.onWorkSubmitted(userId, work, 0);
            }
            if ("reviewed".equalsIgnoreCase(safe(work.getReviewStatus()))
                    && !growthEventRepository.findByUserIdAndSourceTypeAndSourceId(
                            userId, "work", work.getWorkId() + ":reviewed").isPresent()) {
                growthEventService.onWorkReviewed(userId, work);
            }
            if ("y".equalsIgnoreCase(safe(work.getIsFeatured()))
                    && !growthEventRepository.findByUserIdAndSourceTypeAndSourceId(
                            userId, "work", work.getWorkId() + ":featured").isPresent()) {
                growthEventService.onWorkFeatured(userId, work, 0);
            }
        }
    }

    private void backfillTasks(String userId) {
        for (MbTaskSubmission sub : taskSubmissionRepository.findByStudentUserIdOrderByUpdateTimeDesc(userId)) {
            if (sub == null || !StringUtils.hasText(sub.getSubmissionId()) || !StringUtils.hasText(sub.getState())) {
                continue;
            }
            if (!TASK_STATES.contains(sub.getState().trim().toLowerCase())) {
                continue;
            }
            if (growthEventRepository.findByUserIdAndSourceTypeAndSourceId(
                    userId, "task", sub.getSubmissionId()).isPresent()) {
                continue;
            }
            MbTask task = StringUtils.hasText(sub.getTaskId())
                    ? taskRepository.findById(sub.getTaskId()).orElse(null)
                    : null;
            growthEventService.onTaskCompleted(userId, sub, task, 0);
        }
    }

    private void backfillBadges(String userId) {
        for (MbBadgeProgress progress : badgeProgressRepository.findByUserId(userId)) {
            if (progress == null || !"y".equalsIgnoreCase(safe(progress.getEarned()))
                    || !StringUtils.hasText(progress.getBadgeId())) {
                continue;
            }
            if (growthEventRepository.findByUserIdAndSourceTypeAndSourceId(
                    userId, "badge", progress.getBadgeId()).isPresent()) {
                continue;
            }
            MbBadgeDef def = badgeDefRepository.findById(progress.getBadgeId()).orElse(null);
            if (def != null) {
                growthEventService.onBadgeEarned(userId, def);
            }
        }
    }

    private String safe(String value) {
        return value != null ? value.trim() : "";
    }
}
