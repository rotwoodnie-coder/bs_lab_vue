package com.xuanyue.exp.exp.dto;

public class ExpSimulatorListItem {

    private String simulatorId;
    private String simulatorName;
    private String subjectId;
    private String coverImageUrl;
    private String simulatorUrl;
    private String comments;
    private String status;
    private String createUserName;

    public ExpSimulatorListItem() {
    }

    public ExpSimulatorListItem(String simulatorId, String simulatorName, String subjectId, String coverImageUrl, String simulatorUrl, String comments, String status, String createUserName) {
        this.simulatorId = simulatorId;
        this.simulatorName = simulatorName;
        this.subjectId = subjectId;
        this.coverImageUrl = coverImageUrl;
        this.simulatorUrl = simulatorUrl;
        this.comments = comments;
        this.status = status;
        this.createUserName = createUserName;
    }

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
    public String getCreateUserName() { return createUserName; }
    public void setCreateUserName(String createUserName) { this.createUserName = createUserName; }
}
