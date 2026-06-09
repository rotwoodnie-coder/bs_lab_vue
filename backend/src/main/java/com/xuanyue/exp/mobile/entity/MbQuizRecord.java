package com.xuanyue.exp.mobile.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "mb_quiz_record")
public class MbQuizRecord {
    @Id @Column(name = "record_id") private String recordId;
    @Column(name = "user_id") private String userId;
    @Column(name = "question_id") private String questionId;
    @Column(name = "question_ids_json") private String questionIdsJson;
    @Column(name = "quiz_date") private java.sql.Date quizDate;
    @Column(name = "score") private Integer score;
    @Column(name = "total") private Integer total;
    @Column(name = "points") private Integer points;
    @Column(name = "perfect") private String perfect;
    @Column(name = "answers_json") private String answersJson;
    @Column(name = "create_time") private Date createTime;

    public String getRecordId() { return recordId; }
    public void setRecordId(String recordId) { this.recordId = recordId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getQuestionId() { return questionId; }
    public void setQuestionId(String questionId) { this.questionId = questionId; }
    public String getQuestionIdsJson() { return questionIdsJson; }
    public void setQuestionIdsJson(String questionIdsJson) { this.questionIdsJson = questionIdsJson; }
    public java.sql.Date getQuizDate() { return quizDate; }
    public void setQuizDate(java.sql.Date quizDate) { this.quizDate = quizDate; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    public Integer getTotal() { return total; }
    public void setTotal(Integer total) { this.total = total; }
    public Integer getPoints() { return points; }
    public void setPoints(Integer points) { this.points = points; }
    public String getPerfect() { return perfect; }
    public void setPerfect(String perfect) { this.perfect = perfect; }
    public String getAnswersJson() { return answersJson; }
    public void setAnswersJson(String answersJson) { this.answersJson = answersJson; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}
