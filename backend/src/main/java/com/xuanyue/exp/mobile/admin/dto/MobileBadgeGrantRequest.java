package com.xuanyue.exp.mobile.admin.dto;

public class MobileBadgeGrantRequest {
    private String userId;
    private String badgeId;
    private Boolean earned;

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getBadgeId() { return badgeId; }
    public void setBadgeId(String badgeId) { this.badgeId = badgeId; }
    public Boolean getEarned() { return earned; }
    public void setEarned(Boolean earned) { this.earned = earned; }
}
