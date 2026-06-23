package com.xuanyue.exp.mobile.dto;

/**
 * 管理员（系统/校管理员）待审核学生作品列表项。
 */
public class MobileWorkReviewItemDto {

    private String id;
    private String title;
    private String studentName;
    private String studentInitial;
    private String groupLabel;
    private String workType;
    private String workTypeLabel;
    private String time;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getStudentInitial() { return studentInitial; }
    public void setStudentInitial(String studentInitial) { this.studentInitial = studentInitial; }
    public String getGroupLabel() { return groupLabel; }
    public void setGroupLabel(String groupLabel) { this.groupLabel = groupLabel; }
    public String getWorkType() { return workType; }
    public void setWorkType(String workType) { this.workType = workType; }
    public String getWorkTypeLabel() { return workTypeLabel; }
    public void setWorkTypeLabel(String workTypeLabel) { this.workTypeLabel = workTypeLabel; }
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
}
