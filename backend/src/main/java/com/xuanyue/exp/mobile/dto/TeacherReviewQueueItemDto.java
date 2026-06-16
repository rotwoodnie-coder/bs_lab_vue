package com.xuanyue.exp.mobile.dto;

public class TeacherReviewQueueItemDto {

    private String id;
    private String student;
    private String studentInitial;
    /** 学生头像可访问 URL */
    private String studentAvatarUrl;
    private String title;
    private String time;
    private String avatarClass;
    /** 作品类型：homework / remix / creative */
    private String workType;
    private String workTypeLabel;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getStudent() { return student; }
    public void setStudent(String student) { this.student = student; }
    public String getStudentInitial() { return studentInitial; }
    public void setStudentInitial(String studentInitial) { this.studentInitial = studentInitial; }
    public String getStudentAvatarUrl() { return studentAvatarUrl; }
    public void setStudentAvatarUrl(String studentAvatarUrl) { this.studentAvatarUrl = studentAvatarUrl; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    public String getAvatarClass() { return avatarClass; }
    public void setAvatarClass(String avatarClass) { this.avatarClass = avatarClass; }
    public String getWorkType() { return workType; }
    public void setWorkType(String workType) { this.workType = workType; }
    public String getWorkTypeLabel() { return workTypeLabel; }
    public void setWorkTypeLabel(String workTypeLabel) { this.workTypeLabel = workTypeLabel; }
}
