package com.xuanyue.exp.exp.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "exp_simulator")
public class ExpSimulator {

    @Id
    @Column(name = "simulator_id")
    private String simulatorId;

    @Column(name = "simulator_name")
    private String simulatorName;

    @Column(name = "subject_id")
    private String subjectId;

    @Column(name = "cover_image_url")
    private String coverImageUrl;

    @Column(name = "simulator_url")
    private String simulatorUrl;

    @Column(name = "comments")
    private String comments;

    @Column(name = "status")
    private String status;

    @Column(name = "create_user_id")
    private String createUserId;

    @Column(name = "create_time")
    private java.util.Date createTime;

    @Column(name = "update_user_id")
    private String updateUserId;

    @Column(name = "update_time")
    private java.util.Date updateTime;

    public String getSimulatorId() { return simulatorId; }
    public void setSimulatorId(String simulatorId) { this.simulatorId = simulatorId; }
    public String getSimulatorName() { return simulatorName; }
    public void setSimulatorName(String simulatorName) { this.simulatorName = simulatorName; }
    public String getSubjectId() { return subjectId; }
    public void setSubjectId(String subjectId) { this.subjectId = subjectId; }
    public String getCoverImageUrl() { return coverImageUrl; }
    public void setCoverImageUrl(String coverImageUrl) { this.coverImageUrl = coverImageUrl; }
    public String getSimulatorUrl() { return simulatorUrl; }
    public void setSimulatorUrl(String simulatorUrl) { this.simulatorUrl = simulatorUrl; }
    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCreateUserId() { return createUserId; }
    public void setCreateUserId(String createUserId) { this.createUserId = createUserId; }
    public java.util.Date getCreateTime() { return createTime; }
    public void setCreateTime(java.util.Date createTime) { this.createTime = createTime; }
    public String getUpdateUserId() { return updateUserId; }
    public void setUpdateUserId(String updateUserId) { this.updateUserId = updateUserId; }
    public java.util.Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(java.util.Date updateTime) { this.updateTime = updateTime; }
}
