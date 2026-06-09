package com.xuanyue.exp.mobile.admin.dto;

public class MobileBadgeProgressItemDto {
    private String badgeId;
    private String title;
    private String icon;
    private String earned;
    private Integer progressCurrent;
    private Integer progressTarget;
    private String earnedTime;

    public String getBadgeId() { return badgeId; }
    public void setBadgeId(String badgeId) { this.badgeId = badgeId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public String getEarned() { return earned; }
    public void setEarned(String earned) { this.earned = earned; }
    public Integer getProgressCurrent() { return progressCurrent; }
    public void setProgressCurrent(Integer progressCurrent) { this.progressCurrent = progressCurrent; }
    public Integer getProgressTarget() { return progressTarget; }
    public void setProgressTarget(Integer progressTarget) { this.progressTarget = progressTarget; }
    public String getEarnedTime() { return earnedTime; }
    public void setEarnedTime(String earnedTime) { this.earnedTime = earnedTime; }
}
