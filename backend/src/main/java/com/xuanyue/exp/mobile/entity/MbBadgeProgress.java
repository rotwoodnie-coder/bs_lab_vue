package com.xuanyue.exp.mobile.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "mb_badge_progress")
public class MbBadgeProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id") private String userId;
    @Column(name = "badge_id") private String badgeId;
    @Column(name = "earned") private String earned;
    @Column(name = "progress_current") private Integer progressCurrent;
    @Column(name = "progress_target") private Integer progressTarget;
    @Column(name = "earned_time") private Date earnedTime;

    public Long getId() { return id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getBadgeId() { return badgeId; }
    public void setBadgeId(String badgeId) { this.badgeId = badgeId; }
    public String getEarned() { return earned; }
    public void setEarned(String earned) { this.earned = earned; }
    public Integer getProgressCurrent() { return progressCurrent; }
    public void setProgressCurrent(Integer progressCurrent) { this.progressCurrent = progressCurrent; }
    public Integer getProgressTarget() { return progressTarget; }
    public void setProgressTarget(Integer progressTarget) { this.progressTarget = progressTarget; }
    public Date getEarnedTime() { return earnedTime; }
    public void setEarnedTime(Date earnedTime) { this.earnedTime = earnedTime; }
}
