package com.xuanyue.exp.mobile.admin.dto;

public class MobileBadgeDefDto {
    private String badgeId;
    private String icon;
    private String title;
    private String description;
    private String criteriaType;
    private Integer criteriaValue;
    private Integer rewardPoints;
    private String actionRoute;
    private Integer sortOrder;
    private String status;

    public String getBadgeId() { return badgeId; }
    public void setBadgeId(String badgeId) { this.badgeId = badgeId; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCriteriaType() { return criteriaType; }
    public void setCriteriaType(String criteriaType) { this.criteriaType = criteriaType; }
    public Integer getCriteriaValue() { return criteriaValue; }
    public void setCriteriaValue(Integer criteriaValue) { this.criteriaValue = criteriaValue; }
    public Integer getRewardPoints() { return rewardPoints; }
    public void setRewardPoints(Integer rewardPoints) { this.rewardPoints = rewardPoints; }
    public String getActionRoute() { return actionRoute; }
    public void setActionRoute(String actionRoute) { this.actionRoute = actionRoute; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
