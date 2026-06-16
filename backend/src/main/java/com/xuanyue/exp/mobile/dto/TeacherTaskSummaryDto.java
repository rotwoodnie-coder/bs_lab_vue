package com.xuanyue.exp.mobile.dto;

public class TeacherTaskSummaryDto {

    private String taskId;
    private String title;
    private String className;
    private int totalStudents;
    private int submitted;
    private int pendingReview;
    private int submitRate;
    private long sortTime;
    private boolean cancelled;

    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public int getTotalStudents() { return totalStudents; }
    public void setTotalStudents(int totalStudents) { this.totalStudents = totalStudents; }
    public int getSubmitted() { return submitted; }
    public void setSubmitted(int submitted) { this.submitted = submitted; }
    public int getPendingReview() { return pendingReview; }
    public void setPendingReview(int pendingReview) { this.pendingReview = pendingReview; }
    public int getSubmitRate() { return submitRate; }
    public void setSubmitRate(int submitRate) { this.submitRate = submitRate; }
    public long getSortTime() { return sortTime; }
    public void setSortTime(long sortTime) { this.sortTime = sortTime; }
    public boolean isCancelled() { return cancelled; }
    public void setCancelled(boolean cancelled) { this.cancelled = cancelled; }
}
