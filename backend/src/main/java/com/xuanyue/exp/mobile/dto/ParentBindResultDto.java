package com.xuanyue.exp.mobile.dto;

public class ParentBindResultDto {

    private String bindId;
    private String childUserId;
    private String childName;
    /** 学生头像可访问 URL */
    private String childAvatarUrl;
    private String bindStatus;
    private String schoolName;
    private String gradeName;
    private String className;
    private String relation;
    private String message;
    private String rejectReason;
    private String submitTime;

    public String getBindId() { return bindId; }
    public void setBindId(String bindId) { this.bindId = bindId; }
    public String getChildUserId() { return childUserId; }
    public void setChildUserId(String childUserId) { this.childUserId = childUserId; }
    public String getChildName() { return childName; }
    public void setChildName(String childName) { this.childName = childName; }
    public String getChildAvatarUrl() { return childAvatarUrl; }
    public void setChildAvatarUrl(String childAvatarUrl) { this.childAvatarUrl = childAvatarUrl; }
    public String getBindStatus() { return bindStatus; }
    public void setBindStatus(String bindStatus) { this.bindStatus = bindStatus; }
    public String getSchoolName() { return schoolName; }
    public void setSchoolName(String schoolName) { this.schoolName = schoolName; }
    public String getGradeName() { return gradeName; }
    public void setGradeName(String gradeName) { this.gradeName = gradeName; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public String getRelation() { return relation; }
    public void setRelation(String relation) { this.relation = relation; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getRejectReason() { return rejectReason; }
    public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }
    public String getSubmitTime() { return submitTime; }
    public void setSubmitTime(String submitTime) { this.submitTime = submitTime; }
}
