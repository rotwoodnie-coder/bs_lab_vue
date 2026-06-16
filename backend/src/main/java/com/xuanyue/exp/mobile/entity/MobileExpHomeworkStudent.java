package com.xuanyue.exp.mobile.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * 移动端实体 —— 映射 exp_homework_student（作业提交批阅表）
 * 与管理端 ExpHomeworkStudent 实体同表不同包，互不冲突。
 */
@Entity
@Table(name = "exp_homework_student")
public class MobileExpHomeworkStudent {

    @Id
    @Column(name = "seq_id")
    private String seqId;

    @Column(name = "homework_id")
    private String homeworkId;

    @Column(name = "teacher_exp_id")
    private String teacherExpId;

    @Column(name = "teacher_user_id")
    private String teacherUserId;

    @Column(name = "require_date")
    private String requireDate;

    @Column(name = "student_exp_id")
    private String studentExpId;

    @Column(name = "student_submit_date")
    private String studentSubmitDate;

    @Column(name = "mark_user_id")
    private String markUserId;

    @Column(name = "mark_time")
    private Date markTime;

    @Column(name = "mark_comments")
    private String markComments;

    @Column(name = "mark_result")
    private String markResult;

    public String getSeqId() { return seqId; }
    public void setSeqId(String seqId) { this.seqId = seqId; }
    public String getHomeworkId() { return homeworkId; }
    public void setHomeworkId(String homeworkId) { this.homeworkId = homeworkId; }
    public String getTeacherExpId() { return teacherExpId; }
    public void setTeacherExpId(String teacherExpId) { this.teacherExpId = teacherExpId; }
    public String getTeacherUserId() { return teacherUserId; }
    public void setTeacherUserId(String teacherUserId) { this.teacherUserId = teacherUserId; }
    public String getRequireDate() { return requireDate; }
    public void setRequireDate(String requireDate) { this.requireDate = requireDate; }
    public String getStudentExpId() { return studentExpId; }
    public void setStudentExpId(String studentExpId) { this.studentExpId = studentExpId; }
    public String getStudentSubmitDate() { return studentSubmitDate; }
    public void setStudentSubmitDate(String studentSubmitDate) { this.studentSubmitDate = studentSubmitDate; }
    public String getMarkUserId() { return markUserId; }
    public void setMarkUserId(String markUserId) { this.markUserId = markUserId; }
    public Date getMarkTime() { return markTime; }
    public void setMarkTime(Date markTime) { this.markTime = markTime; }
    public String getMarkComments() { return markComments; }
    public void setMarkComments(String markComments) { this.markComments = markComments; }
    public String getMarkResult() { return markResult; }
    public void setMarkResult(String markResult) { this.markResult = markResult; }
}
