package com.xuanyue.exp.exp.dto;

public class ExpSimulatorSaveRequest {

    private String simulatorName;
    private String subjectId;
    private String coverImageUrl;
    private String simulatorUrl;
    private String comments;
    private String status;

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
}
