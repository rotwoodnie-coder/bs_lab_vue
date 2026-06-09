package com.xuanyue.exp.mobile.entity;



import javax.persistence.Column;

import javax.persistence.Entity;

import javax.persistence.Id;

import javax.persistence.Table;

import java.util.Date;



@Entity

@Table(name = "mb_parent_child")

public class MbParentChild {

    @Id @Column(name = "bind_id") private String bindId;

    @Column(name = "parent_user_id") private String parentUserId;

    @Column(name = "child_user_id") private String childUserId;

    @Column(name = "relation") private String relation;

    @Column(name = "is_default") private String isDefault;

    @Column(name = "school_name") private String schoolName;

    @Column(name = "grade_name") private String gradeName;

    @Column(name = "class_name") private String className;

    @Column(name = "class_org_id") private String classOrgId;

    @Column(name = "bind_status") private String bindStatus;

    @Column(name = "confirm_user_id") private String confirmUserId;

    @Column(name = "confirm_time") private Date confirmTime;

    @Column(name = "reject_reason") private String rejectReason;

    @Column(name = "create_time") private Date createTime;

    @Column(name = "update_time") private Date updateTime;



    public String getBindId() { return bindId; }

    public void setBindId(String bindId) { this.bindId = bindId; }

    public String getParentUserId() { return parentUserId; }

    public void setParentUserId(String parentUserId) { this.parentUserId = parentUserId; }

    public String getChildUserId() { return childUserId; }

    public void setChildUserId(String childUserId) { this.childUserId = childUserId; }

    public String getRelation() { return relation; }

    public void setRelation(String relation) { this.relation = relation; }

    public String getIsDefault() { return isDefault; }

    public void setIsDefault(String isDefault) { this.isDefault = isDefault; }

    public String getSchoolName() { return schoolName; }

    public void setSchoolName(String schoolName) { this.schoolName = schoolName; }

    public String getGradeName() { return gradeName; }

    public void setGradeName(String gradeName) { this.gradeName = gradeName; }

    public String getClassName() { return className; }

    public void setClassName(String className) { this.className = className; }

    public String getClassOrgId() { return classOrgId; }

    public void setClassOrgId(String classOrgId) { this.classOrgId = classOrgId; }

    public String getBindStatus() { return bindStatus; }

    public void setBindStatus(String bindStatus) { this.bindStatus = bindStatus; }

    public String getConfirmUserId() { return confirmUserId; }

    public void setConfirmUserId(String confirmUserId) { this.confirmUserId = confirmUserId; }

    public Date getConfirmTime() { return confirmTime; }

    public void setConfirmTime(Date confirmTime) { this.confirmTime = confirmTime; }

    public String getRejectReason() { return rejectReason; }

    public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }

    public Date getCreateTime() { return createTime; }

    public void setCreateTime(Date createTime) { this.createTime = createTime; }

    public Date getUpdateTime() { return updateTime; }

    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }

}


