package com.xuanyue.exp.mobile.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "mb_growth_plan")
public class MbGrowthPlan {

    @Id
    @Column(name = "user_id")
    private String userId;

    @Column(name = "content_keys_json")
    private String contentKeysJson;

    @Column(name = "visibility")
    private String visibility;

    @Column(name = "range")
    private String range;

    @Column(name = "update_time")
    private Date updateTime;

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getContentKeysJson() { return contentKeysJson; }
    public void setContentKeysJson(String contentKeysJson) { this.contentKeysJson = contentKeysJson; }
    public String getVisibility() { return visibility; }
    public void setVisibility(String visibility) { this.visibility = visibility; }
    public String getRange() { return range; }
    public void setRange(String range) { this.range = range; }
    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
}
