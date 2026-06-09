package com.xuanyue.exp.mobile.dto;

import java.util.List;

public class CreateWorkRequest {

    private String title;
    private String description;
    private String workType;
    private String taskId;
    private String sourceExpId;
    private String relatedTaskTitle;
    private String className;
    private String schoolName;
    /** 家长代传时指定学生 userId */
    private String studentUserId;
    private List<CreateWorkFileItem> files;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getWorkType() { return workType; }
    public void setWorkType(String workType) { this.workType = workType; }
    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }
    public String getSourceExpId() { return sourceExpId; }
    public void setSourceExpId(String sourceExpId) { this.sourceExpId = sourceExpId; }
    public String getRelatedTaskTitle() { return relatedTaskTitle; }
    public void setRelatedTaskTitle(String relatedTaskTitle) { this.relatedTaskTitle = relatedTaskTitle; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public String getSchoolName() { return schoolName; }
    public void setSchoolName(String schoolName) { this.schoolName = schoolName; }
    public String getStudentUserId() { return studentUserId; }
    public void setStudentUserId(String studentUserId) { this.studentUserId = studentUserId; }
    public List<CreateWorkFileItem> getFiles() { return files; }
    public void setFiles(List<CreateWorkFileItem> files) { this.files = files; }
}
