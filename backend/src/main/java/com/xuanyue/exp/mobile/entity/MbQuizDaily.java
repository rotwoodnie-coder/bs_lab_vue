package com.xuanyue.exp.mobile.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "mb_quiz_daily")
public class MbQuizDaily {

    @Id
    @Column(name = "daily_id")
    private String dailyId;

    @Column(name = "quiz_date")
    private java.sql.Date quizDate;

    @Column(name = "question_id")
    private String questionId;

    @Column(name = "bonus_points")
    private Integer bonusPoints;

    @Column(name = "status")
    private String status;

    @Column(name = "create_time")
    private Date createTime;

    public String getDailyId() { return dailyId; }
    public void setDailyId(String dailyId) { this.dailyId = dailyId; }
    public java.sql.Date getQuizDate() { return quizDate; }
    public void setQuizDate(java.sql.Date quizDate) { this.quizDate = quizDate; }
    public String getQuestionId() { return questionId; }
    public void setQuestionId(String questionId) { this.questionId = questionId; }
    public Integer getBonusPoints() { return bonusPoints; }
    public void setBonusPoints(Integer bonusPoints) { this.bonusPoints = bonusPoints; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}
