package com.xuanyue.exp.mobile.dto;

public class ParentRegisterResultDto {

    private String userId;
    private String message;

    public ParentRegisterResultDto() {
    }

    public ParentRegisterResultDto(String userId, String message) {
        this.userId = userId;
        this.message = message;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    private String childName;
    private String schoolName;
    private String gradeName;
    private String className;
    private String bindStatus;
    private String bindId;

    public String getChildName() { return childName; }
    public void setChildName(String childName) { this.childName = childName; }
    public String getSchoolName() { return schoolName; }
    public void setSchoolName(String schoolName) { this.schoolName = schoolName; }
    public String getGradeName() { return gradeName; }
    public void setGradeName(String gradeName) { this.gradeName = gradeName; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public String getBindStatus() { return bindStatus; }
    public void setBindStatus(String bindStatus) { this.bindStatus = bindStatus; }
    public String getBindId() { return bindId; }
    public void setBindId(String bindId) { this.bindId = bindId; }
}
