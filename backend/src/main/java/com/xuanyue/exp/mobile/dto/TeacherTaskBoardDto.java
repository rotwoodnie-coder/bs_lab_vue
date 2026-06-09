package com.xuanyue.exp.mobile.dto;

import java.util.List;

public class TeacherTaskBoardDto {

    private String taskId;
    private String taskTitle;
    private String className;
    private int submitted;
    private int unsubmitted;
    private int submitRate;
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
    public List<StudentRow> getStudents() { return students; }
    public void setStudents(List<StudentRow> students) { this.students = students; }

    public static class StudentRow {
        private String userId;
        private String name;
        private String initial;
        private boolean done;
        private String workId;

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getInitial() { return initial; }
        public void setInitial(String initial) { this.initial = initial; }
        public boolean isDone() { return done; }
        public void setDone(boolean done) { this.done = done; }
        public String getWorkId() { return workId; }
        public void setWorkId(String workId) { this.workId = workId; }
    }
}
