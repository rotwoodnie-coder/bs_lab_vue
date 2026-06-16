package com.xuanyue.exp.mobile.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * 移动端只读实体 —— 映射 exp_homework（作业布置表）
 * 与管理端 ExpHomework 实体同表不同包，互不冲突。
 */
@Entity
@Table(name = "exp_homework")
public class MobileExpHomework {

    @Id
    @Column(name = "homework_id")
    private String homeworkId;

    @Column(name = "teacher_exp_id")
    private String teacherExpId;

    @Column(name = "tearcher_user_id")
    private String tearcherUserId;

    @Column(name = "class_id")
    private String classId;

    @Column(name = "require_date")
    private String requireDate;

    @Column(name = "create_time")
    private Date createTime;

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
}
