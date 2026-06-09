package com.xuanyue.exp.mobile.service;

import com.xuanyue.exp.mobile.dto.MobileGrowthDto;
import com.xuanyue.exp.mobile.entity.MbBadgeProgress;
import com.xuanyue.exp.mobile.entity.MbQuizRecord;
import com.xuanyue.exp.mobile.entity.MbTaskSubmission;
import com.xuanyue.exp.mobile.entity.MbWork;
import com.xuanyue.exp.mobile.repository.MbBadgeProgressRepository;
import com.xuanyue.exp.mobile.repository.MbPointsLedgerRepository;
import com.xuanyue.exp.mobile.repository.MbQuizRecordRepository;
import com.xuanyue.exp.mobile.repository.MbTaskSubmissionRepository;
import com.xuanyue.exp.mobile.repository.MbWorkRepository;
import com.xuanyue.exp.mobile.support.MobileGrowthFilterSupport;
import com.xuanyue.exp.system.repository.SysUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class MobileGrowthStatsService {

    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");
    private static final List<String> EXPERIMENT_STATES = Arrays.asList("submitted", "reviewed");

    private final MbTaskSubmissionRepository taskSubmissionRepository;
    private final MbWorkRepository workRepository;
    private final MbQuizRecordRepository quizRecordRepository;
    private final MbBadgeProgressRepository badgeProgressRepository;
    private final MbPointsLedgerRepository pointsLedgerRepository;
    private final SysUserRepository sysUserRepository;

    public MobileGrowthStatsService(MbTaskSubmissionRepository taskSubmissionRepository,
                                    MbWorkRepository workRepository,
                                    MbQuizRecordRepository quizRecordRepository,
                                    MbBadgeProgressRepository badgeProgressRepository,
                                    MbPointsLedgerRepository pointsLedgerRepository,
                                    SysUserRepository sysUserRepository) {
        this.taskSubmissionRepository = taskSubmissionRepository;
        this.workRepository = workRepository;
        this.quizRecordRepository = quizRecordRepository;
        this.badgeProgressRepository = badgeProgressRepository;
        this.pointsLedgerRepository = pointsLedgerRepository;
        this.sysUserRepository = sysUserRepository;
    }

    public MobileGrowthDto.Stats computeStats(String userId, List<String> contentKeys, String rangeKey) {
        MobileGrowthDto.Stats stats = new MobileGrowthDto.Stats();
        Date rangeStart = MobileGrowthFilterSupport.resolveRangeStartDate(rangeKey);
        Set<String> allowed = MobileGrowthFilterSupport.resolveAllowedSourceTypes(contentKeys);

        int totalPoints = resolveTotalPoints(userId);
        stats.setTotalPoints(totalPoints);
        stats.setPoints(totalPoints);
        stats.setPeriodPoints(sumPeriodPoints(userId, rangeStart));

        stats.setExperiments(allowed.contains("task") ? countExperiments(userId, rangeStart) : 0);
        stats.setWorks(allowed.contains("work") ? countWorks(userId, rangeStart) : 0);
        stats.setQuizDays(allowed.contains("quiz") ? countQuizDays(userId, rangeStart) : 0);
        stats.setBadges(allowed.contains("badge") ? countBadges(userId, rangeStart) : 0);
        return stats;
    }

    public MobileGrowthDto.Stats emptyStats() {
        MobileGrowthDto.Stats stats = new MobileGrowthDto.Stats();
        stats.setExperiments(0);
        stats.setWorks(0);
        stats.setQuizDays(0);
        stats.setBadges(0);
        stats.setPoints(0);
        stats.setTotalPoints(0);
        stats.setPeriodPoints(0);
        return stats;
    }

    private int resolveTotalPoints(String userId) {
        return sysUserRepository.findById(userId)
                .map(u -> u.getPerScore() != null ? u.getPerScore() : 0)
                .orElse(0);
    }

    private int sumPeriodPoints(String userId, Date rangeStart) {
        if (rangeStart == null) {
            return (int) Math.min(Integer.MAX_VALUE, pointsLedgerRepository.sumDeltaByUserId(userId));
        }
        return (int) Math.min(Integer.MAX_VALUE, pointsLedgerRepository.sumDeltaByUserIdSince(userId, rangeStart));
    }

    private int countExperiments(String userId, Date rangeStart) {
        List<MbTaskSubmission> submissions = taskSubmissionRepository.findByStudentUserIdOrderByUpdateTimeDesc(userId);
        int count = 0;
        for (MbTaskSubmission sub : submissions) {
            if (sub == null || !StringUtils.hasText(sub.getState())) {
                continue;
            }
            if (!EXPERIMENT_STATES.contains(sub.getState().trim().toLowerCase())) {
                continue;
            }
            Date eventTime = sub.getSubmitTime() != null ? sub.getSubmitTime() : sub.getUpdateTime();
            if (isOnOrAfter(eventTime, rangeStart)) {
                count++;
            }
        }
        return count;
    }

    private int countWorks(String userId, Date rangeStart) {
        List<MbWork> works = workRepository.findByStudentUserIdOrderByCreateTimeDesc(userId);
        int count = 0;
        for (MbWork work : works) {
            if (work == null || !"y".equalsIgnoreCase(safe(work.getStatus()))) {
                continue;
            }
            if (isOnOrAfter(work.getCreateTime(), rangeStart)) {
                count++;
            }
        }
        return count;
    }

    private int countQuizDays(String userId, Date rangeStart) {
        List<MbQuizRecord> records = quizRecordRepository.findByUserIdOrderByQuizDateDesc(userId);
        Set<LocalDate> days = new HashSet<>();
        for (MbQuizRecord record : records) {
            if (record == null || record.getQuizDate() == null) {
                continue;
            }
            LocalDate day = record.getQuizDate().toLocalDate();
            if (rangeStart != null) {
                LocalDate start = rangeStart.toInstant().atZone(ZONE).toLocalDate();
                if (day.isBefore(start)) {
                    continue;
                }
            }
            days.add(day);
        }
        return days.size();
    }

    private int countBadges(String userId, Date rangeStart) {
        List<MbBadgeProgress> progressList = badgeProgressRepository.findByUserId(userId);
        int count = 0;
        for (MbBadgeProgress progress : progressList) {
            if (progress == null || !"y".equalsIgnoreCase(safe(progress.getEarned()))) {
                continue;
            }
            Date earnedTime = progress.getEarnedTime();
            if (isOnOrAfter(earnedTime, rangeStart)) {
                count++;
            }
        }
        return count;
    }

    private boolean isOnOrAfter(Date eventTime, Date rangeStart) {
        if (rangeStart == null) {
            return true;
        }
        if (eventTime == null) {
            return false;
        }
        return !eventTime.before(rangeStart);
    }

    private String safe(String value) {
        return value != null ? value.trim() : "";
    }
}
