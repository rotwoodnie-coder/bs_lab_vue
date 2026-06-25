package com.xuanyue.exp.mobile.service;

import com.xuanyue.exp.mobile.entity.MbBadgeDef;
import com.xuanyue.exp.mobile.entity.MbGrowthEvent;
import com.xuanyue.exp.mobile.entity.MbQuizRecord;
import com.xuanyue.exp.mobile.entity.MbTask;
import com.xuanyue.exp.mobile.entity.MbTaskSubmission;
import com.xuanyue.exp.mobile.entity.MbWork;
import com.xuanyue.exp.mobile.repository.MbGrowthEventRepository;
import com.xuanyue.exp.mobile.support.MobileIds;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Service
public class MobileGrowthEventService {

    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");

    private final MbGrowthEventRepository growthEventRepository;

    public MobileGrowthEventService(MbGrowthEventRepository growthEventRepository) {
        this.growthEventRepository = growthEventRepository;
    }

    @Transactional
    public void onQuizSubmitted(String userId, MbQuizRecord record, int points) {
        if (!StringUtils.hasText(userId) || record == null || !StringUtils.hasText(record.getRecordId())) {
            return;
        }
        if (growthEventRepository.findByUserIdAndSourceTypeAndSourceId(userId, "quiz", record.getRecordId()).isPresent()) {
            return;
        }
        Date now = record.getCreateTime() != null ? record.getCreateTime() : new Date();
        int score = record.getScore() != null ? record.getScore() : 0;
        int total = record.getTotal() != null ? record.getTotal() : 0;
        MbGrowthEvent event = baseEvent(userId, "quiz", record.getRecordId(), now);
        event.setEmoji("🧠");
        event.setTitle("完成每日答题（" + score + "/" + total + "）");
        if ("y".equalsIgnoreCase(record.getPerfect())) {
            event.setHint("全部答对，继续保持！");
            event.setDotClass("green");
            event.setBadgeClass("badge-success");
        } else {
            event.setHint("再接再厉，明天继续挑战");
            event.setDotClass("amber");
        }
        if (points > 0) {
            event.setPointsLabel("+" + points + "分");
        }
        growthEventRepository.save(event);
    }

    @Transactional
    public void onWorkSubmitted(String userId, MbWork work, int points) {
        if (!StringUtils.hasText(userId) || work == null || !StringUtils.hasText(work.getWorkId())) {
            return;
        }
        if (growthEventRepository.findByUserIdAndSourceTypeAndSourceId(userId, "work", work.getWorkId()).isPresent()) {
            return;
        }
        Date now = work.getCreateTime() != null ? work.getCreateTime() : new Date();
        MbGrowthEvent event = baseEvent(userId, "work", work.getWorkId(), now);
        event.setEmoji("🎨");
        event.setTitle("提交作品「" + safeTitle(work.getTitle()) + "」");
        event.setDotClass("violet");
        if (points > 0) {
            event.setPointsLabel("+" + points + "分");
        }
        growthEventRepository.save(event);
    }

    @Transactional
    public void onWorkFeatured(String userId, MbWork work, int points) {
        if (!StringUtils.hasText(userId) || work == null || !StringUtils.hasText(work.getWorkId())) {
            return;
        }
        String sourceId = work.getWorkId() + ":featured";
        if (growthEventRepository.findByUserIdAndSourceTypeAndSourceId(userId, "work", sourceId).isPresent()) {
            return;
        }
        Date now = new Date();
        MbGrowthEvent event = baseEvent(userId, "work", sourceId, now);
        event.setEmoji("🌟");
        event.setTitle("作品「" + safeTitle(work.getTitle()) + "」被教师展示");
        event.setHint("继续加油，创意无限");
        event.setDotClass("amber");
        event.setBadgeClass("badge-success");
        if (points > 0) {
            event.setPointsLabel("+" + points + "分");
        }
        growthEventRepository.save(event);
    }

    @Transactional
    public void onTaskCompleted(String userId, MbTaskSubmission submission, MbTask task, int points) {
        if (!StringUtils.hasText(userId) || submission == null || !StringUtils.hasText(submission.getSubmissionId())) {
            return;
        }
        if (growthEventRepository.findByUserIdAndSourceTypeAndSourceId(
                userId, "task", submission.getSubmissionId()).isPresent()) {
            return;
        }
        Date now = submission.getSubmitTime() != null ? submission.getSubmitTime() : new Date();
        MbGrowthEvent event = baseEvent(userId, "task", submission.getSubmissionId(), now);
        event.setEmoji("🧪");
        String taskTitle = task != null && StringUtils.hasText(task.getTitle()) ? task.getTitle().trim() : "实验任务";
        event.setTitle("完成实验任务「" + safeTitle(taskTitle) + "」");
        event.setHint("任务已提交，等待老师评价");
        event.setDotClass("blue");
        if (points > 0) {
            event.setPointsLabel("+" + points + "分");
        }
        growthEventRepository.save(event);
    }

    @Transactional
    public void onWorkReviewed(String userId, MbWork work) {
        if (!StringUtils.hasText(userId) || work == null || !StringUtils.hasText(work.getWorkId())) {
            return;
        }
        String sourceId = work.getWorkId() + ":reviewed";
        if (growthEventRepository.findByUserIdAndSourceTypeAndSourceId(userId, "work", sourceId).isPresent()) {
            return;
        }
        Date now = new Date();
        MbGrowthEvent event = baseEvent(userId, "work", sourceId, now);
        event.setEmoji("✅");
        event.setTitle("作品「" + safeTitle(work.getTitle()) + "」已评价");
        if (StringUtils.hasText(work.getGrade())) {
            event.setHint("评级：" + work.getGrade().trim().toUpperCase());
        } else if (StringUtils.hasText(work.getTeacherReviewText())) {
            event.setHint(work.getTeacherReviewText().trim());
        }
        event.setDotClass("blue");
        event.setBadgeClass("badge-info");
        growthEventRepository.save(event);
    }

    @Transactional
    public void onBadgeEarned(String userId, MbBadgeDef badge) {
        if (!StringUtils.hasText(userId) || badge == null || !StringUtils.hasText(badge.getBadgeId())) {
            return;
        }
        if (growthEventRepository.findByUserIdAndSourceTypeAndSourceId(userId, "badge", badge.getBadgeId()).isPresent()) {
            return;
        }
        Date now = new Date();
        MbGrowthEvent event = baseEvent(userId, "badge", badge.getBadgeId(), now);
        event.setEmoji(StringUtils.hasText(badge.getIcon()) ? badge.getIcon() : "🏅");
        event.setTitle("获得「" + safeTitle(badge.getTitle()) + "」勋章");
        event.setHint(badge.getDescription());
        event.setDotClass("green");
        event.setBadgeClass("badge-success");
        int reward = badge.getRewardPoints() != null ? badge.getRewardPoints() : 0;
        if (reward > 0) {
            event.setPointsLabel("+" + reward + "分");
        }
        growthEventRepository.save(event);
    }

    private MbGrowthEvent baseEvent(String userId, String sourceType, String sourceId, Date sortTime) {
        MbGrowthEvent event = new MbGrowthEvent();
        event.setEventId(MobileIds.newId("ge"));
        event.setUserId(userId);
        event.setSortTime(sortTime);
        event.setEventTimeLabel(formatTimeLabel(sortTime));
        event.setSourceType(sourceType);
        event.setSourceId(sourceId);
        event.setCreateTime(new Date());
        return event;
    }

    private String safeTitle(String title) {
        if (!StringUtils.hasText(title)) {
            return "未命名";
        }
        String trimmed = title.trim();
        return trimmed.length() > 40 ? trimmed.substring(0, 40) + "…" : trimmed;
    }

    static String formatTimeLabel(Date date) {
        if (date == null) {
            return "刚刚";
        }
        LocalDate day = date.toInstant().atZone(ZONE).toLocalDate();
        LocalDate today = LocalDate.now(ZONE);
        if (day.equals(today)) {
            return "今天 " + DateTimeFormatter.ofPattern("HH:mm").format(date.toInstant().atZone(ZONE));
        }
        if (day.equals(today.minusDays(1))) {
            return "昨天 " + DateTimeFormatter.ofPattern("HH:mm").format(date.toInstant().atZone(ZONE));
        }
        if (day.getYear() == today.getYear()) {
            return day.format(DateTimeFormatter.ofPattern("M月d日"));
        }
        return day.format(DateTimeFormatter.ofPattern("yyyy年M月d日"));
    }
}
