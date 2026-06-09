package com.xuanyue.exp.mobile.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "mb_growth_event")
public class MbGrowthEvent {
    @Id @Column(name = "event_id") private String eventId;
    @Column(name = "user_id") private String userId;
    @Column(name = "event_time_label") private String eventTimeLabel;
    @Column(name = "sort_time") private Date sortTime;
    @Column(name = "emoji") private String emoji;
    @Column(name = "title") private String title;
    @Column(name = "hint") private String hint;
    @Column(name = "badges_json") private String badgesJson;
    @Column(name = "points_label") private String pointsLabel;
    @Column(name = "dot_class") private String dotClass;
    @Column(name = "badge_class") private String badgeClass;
    @Column(name = "source_type") private String sourceType;
    @Column(name = "source_id") private String sourceId;
    @Column(name = "create_time") private Date createTime;

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getEventTimeLabel() { return eventTimeLabel; }
    public void setEventTimeLabel(String eventTimeLabel) { this.eventTimeLabel = eventTimeLabel; }
    public Date getSortTime() { return sortTime; }
    public void setSortTime(Date sortTime) { this.sortTime = sortTime; }
    public String getEmoji() { return emoji; }
    public void setEmoji(String emoji) { this.emoji = emoji; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getHint() { return hint; }
    public void setHint(String hint) { this.hint = hint; }
    public String getBadgesJson() { return badgesJson; }
    public void setBadgesJson(String badgesJson) { this.badgesJson = badgesJson; }
    public String getPointsLabel() { return pointsLabel; }
    public void setPointsLabel(String pointsLabel) { this.pointsLabel = pointsLabel; }
    public String getDotClass() { return dotClass; }
    public void setDotClass(String dotClass) { this.dotClass = dotClass; }
    public String getBadgeClass() { return badgeClass; }
    public void setBadgeClass(String badgeClass) { this.badgeClass = badgeClass; }
    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
    public String getSourceId() { return sourceId; }
    public void setSourceId(String sourceId) { this.sourceId = sourceId; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}
