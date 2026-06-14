package com.xuanyue.exp.homework.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;
import javax.persistence.Transient;

@Entity
@Table(name = "exp_homework")
public class ExpHomework {

    @Id
    @Column(name = "homework_id")
    private String homeworkId;

    @Column(name = "teacher_exp_id")
    private String teacherExpId;

    @Column(name = "tearcher_user_id")
    private String tearcherUserId;

    @Column(name = "class_id")
    private String classId;

    @Column(name = "status")
    private String status;

    @Column(name = "require_date")
    private String requireDate;

    @Column(name = "create_time")
    private Date createTime;

    @Transient
    private String teacherExpName;
    @Transient
    private String teacherUserName;
    @Transient
    private String className;

    public String getHomeworkId() { return homeworkId; }
    public void setHomeworkId(String homeworkId) { this.homeworkId = homeworkId; }
    public String getTeacherExpId() { return teacherExpId; }
    public void setTeacherExpId(String teacherExpId) { this.teacherExpId = teacherExpId; }
    public String getTearcherUserId() { return tearcherUserId; }
    public void setTearcherUserId(String tearcherUserId) { this.tearcherUserId = tearcherUserId; }
    public String getClassId() { return classId; }
    public void setClassId(String classId) { this.classId = classId; }
    public String getRequireDate() { return requireDate; }
    public void setRequireDate(String requireDate) { this.requireDate = requireDate; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getTeacherExpName() { return teacherExpName; }
    public void setTeacherExpName(String teacherExpName) { this.teacherExpName = teacherExpName; }
    public String getTeacherUserName() { return teacherUserName; }
    public void setTeacherUserName(String teacherUserName) { this.teacherUserName = teacherUserName; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
}
