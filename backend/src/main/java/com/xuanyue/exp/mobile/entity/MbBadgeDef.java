package com.xuanyue.exp.mobile.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "mb_badge_def")
public class MbBadgeDef {
    @Id @Column(name = "badge_id") private String badgeId;
    @Column(name = "icon") private String icon;
    @Column(name = "title") private String title;
    @Column(name = "description") private String description;
    @Column(name = "criteria_type") private String criteriaType;
    @Column(name = "criteria_value") private Integer criteriaValue;
    @Column(name = "reward_points") private Integer rewardPoints;
    @Column(name = "action_route") private String actionRoute;
    @Column(name = "sort_order") private Integer sortOrder;
    @Column(name = "status") private String status;

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
