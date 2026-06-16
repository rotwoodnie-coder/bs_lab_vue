package com.xuanyue.exp.mobile.dto;



public class TeacherParentBindItemDto {



    private String bindId;

    private String bindStatus;

    private String submitTime;



    private String parentUserId;

    private String parentName;

    private String parentPhoneMasked;

    private String relation;



    private String childUserId;

    private String childName;

    /** 学生头像可访问 URL（来自 sys_user.user_logo） */
    private String childAvatarUrl;

    private String studentNo;

    private String schoolName;

    private String gradeName;

    private String className;

    private String rejectReason;



    public String getBindId() { return bindId; }

    public void setBindId(String bindId) { this.bindId = bindId; }

    public String getBindStatus() { return bindStatus; }

    public void setBindStatus(String bindStatus) { this.bindStatus = bindStatus; }

    public String getSubmitTime() { return submitTime; }

    public void setSubmitTime(String submitTime) { this.submitTime = submitTime; }

    public String getParentUserId() { return parentUserId; }

    public void setParentUserId(String parentUserId) { this.parentUserId = parentUserId; }

    public String getParentName() { return parentName; }

    public void setParentName(String parentName) { this.parentName = parentName; }

    public String getParentPhoneMasked() { return parentPhoneMasked; }

    public void setParentPhoneMasked(String parentPhoneMasked) { this.parentPhoneMasked = parentPhoneMasked; }

    public String getRelation() { return relation; }

    public void setRelation(String relation) { this.relation = relation; }

    public String getChildUserId() { return childUserId; }

    public void setChildUserId(String childUserId) { this.childUserId = childUserId; }

    public String getChildName() { return childName; }

    public void setChildName(String childName) { this.childName = childName; }

    public String getChildAvatarUrl() { return childAvatarUrl; }

    public void setChildAvatarUrl(String childAvatarUrl) { this.childAvatarUrl = childAvatarUrl; }

    public String getStudentNo() { return studentNo; }

    public void setStudentNo(String studentNo) { this.studentNo = studentNo; }

    public String getSchoolName() { return schoolName; }

    public void setSchoolName(String schoolName) { this.schoolName = schoolName; }

    public String getGradeName() { return gradeName; }

    public void setGradeName(String gradeName) { this.gradeName = gradeName; }

    public String getClassName() { return className; }

    public void setClassName(String className) { this.className = className; }

    public String getRejectReason() { return rejectReason; }

    public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }

}


