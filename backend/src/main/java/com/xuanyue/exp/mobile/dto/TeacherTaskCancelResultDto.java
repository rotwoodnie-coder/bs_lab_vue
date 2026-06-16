package com.xuanyue.exp.mobile.dto;

public class TeacherTaskCancelResultDto {

    private String taskId;
    private String message;
    private int notifiedStudents;
    private int submittedCount;

    public TeacherTaskCancelResultDto() {
    }

    public TeacherTaskCancelResultDto(String taskId, String message, int notifiedStudents, int submittedCount) {
        this.taskId = taskId;
        this.message = message;
        this.notifiedStudents = notifiedStudents;
        this.submittedCount = submittedCount;
    }

    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public int getNotifiedStudents() { return notifiedStudents; }
    public void setNotifiedStudents(int notifiedStudents) { this.notifiedStudents = notifiedStudents; }
    public int getSubmittedCount() { return submittedCount; }
    public void setSubmittedCount(int submittedCount) { this.submittedCount = submittedCount; }
}
