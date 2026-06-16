package com.xuanyue.exp.mobile.dto;

import java.util.List;

public class TeacherTaskBoardDto {

    private String taskId;
    private String taskTitle;
    private String className;
    private int submitted;
    private int unsubmitted;
    private int submitRate;
    private int pendingReview;
    /** 任务是否已取消（status=n） */
    private boolean cancelled;
    private List<StudentRow> students;

    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }
    public String getTaskTitle() { return taskTitle; }
    public void setTaskTitle(String taskTitle) { this.taskTitle = taskTitle; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public int getSubmitted() { return submitted; }
    public void setSubmitted(int submitted) { this.submitted = submitted; }
    public int getUnsubmitted() { return unsubmitted; }
    public void setUnsubmitted(int unsubmitted) { this.unsubmitted = unsubmitted; }
    public int getSubmitRate() { return submitRate; }
    public void setSubmitRate(int submitRate) { this.submitRate = submitRate; }
    public int getPendingReview() { return pendingReview; }
    public void setPendingReview(int pendingReview) { this.pendingReview = pendingReview; }
    public boolean isCancelled() { return cancelled; }
    public void setCancelled(boolean cancelled) { this.cancelled = cancelled; }
    public List<StudentRow> getStudents() { return students; }
    public void setStudents(List<StudentRow> students) { this.students = students; }

    public static class StudentRow {
        private String userId;
        private String name;
        private String initial;
        /** 学生头像可访问 URL */
        private String avatarUrl;
        private boolean done;
        private String workId;
        private String reviewStatus;
        private String reviewStatusLabel;
        private String grade;

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getInitial() { return initial; }
        public void setInitial(String initial) { this.initial = initial; }
        public String getAvatarUrl() { return avatarUrl; }
        public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
        public boolean isDone() { return done; }
        public void setDone(boolean done) { this.done = done; }
        public String getWorkId() { return workId; }
        public void setWorkId(String workId) { this.workId = workId; }
        public String getReviewStatus() { return reviewStatus; }
        public void setReviewStatus(String reviewStatus) { this.reviewStatus = reviewStatus; }
        public String getReviewStatusLabel() { return reviewStatusLabel; }
        public void setReviewStatusLabel(String reviewStatusLabel) { this.reviewStatusLabel = reviewStatusLabel; }
        public String getGrade() { return grade; }
        public void setGrade(String grade) { this.grade = grade; }
    }
}
