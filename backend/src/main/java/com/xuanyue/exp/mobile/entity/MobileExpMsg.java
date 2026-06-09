package com.xuanyue.exp.mobile.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import java.util.Date;

/**
 * 移动端只读实体 —— 映射 exp_msg（实验信息表）
 * 仅查询首页需要展示的字段，不做任何写入操作
 */
@Entity
@Table(name = "exp_msg")
public class MobileExpMsg {

    @Id
    @Column(name = "exp_id")
    private String expId;

    @Column(name = "exp_name")
    private String expName;

    @Column(name = "subject_id")
    private String subjectId;

    @Column(name = "school_level_id")
    private String schoolLevelId;

    @Column(name = "grade_id")
    private String gradeId;

    @Column(name = "semester_id")
    private String semesterId;

    @Column(name = "exp_type")
    private String expType;

    @Column(name = "exp_task_type")
    private String expTaskType;

    @Column(name = "simulator_url")
    private String simulatorUrl;

    @Column(name = "simulator_id")
    private String simulatorId;

    @Column(name = "like_num")
    private Integer likeNum;

    @Column(name = "collection_num")
    private Integer collectionNum;

    @Column(name = "evaluate_num")
    private Integer evaluateNum;

    /** 封面图URL（存于 exp_video 表，首页暂不读取） */
    @Column(name = "status")
    private String status;

    @Column(name = "create_user_id")
    private String createUserId;

    @Column(name = "create_time")
    private Date createTime;

    public String getExpId() { return expId; }
    public void setExpId(String expId) { this.expId = expId; }
    public String getExpName() { return expName; }
    public void setExpName(String expName) { this.expName = expName; }
    public String getSubjectId() { return subjectId; }
    public void setSubjectId(String subjectId) { this.subjectId = subjectId; }
    public String getSchoolLevelId() { return schoolLevelId; }
    public void setSchoolLevelId(String schoolLevelId) { this.schoolLevelId = schoolLevelId; }
    public String getGradeId() { return gradeId; }
    public void setGradeId(String gradeId) { this.gradeId = gradeId; }
    public String getSemesterId() { return semesterId; }
    public void setSemesterId(String semesterId) { this.semesterId = semesterId; }
    public String getExpType() { return expType; }
    public void setExpType(String expType) { this.expType = expType; }
    public String getExpTaskType() { return expTaskType; }
    public void setExpTaskType(String expTaskType) { this.expTaskType = expTaskType; }
    public String getSimulatorUrl() { return simulatorUrl; }
    public void setSimulatorUrl(String simulatorUrl) { this.simulatorUrl = simulatorUrl; }
    public String getSimulatorId() { return simulatorId; }
    public void setSimulatorId(String simulatorId) { this.simulatorId = simulatorId; }
    public Integer getLikeNum() { return likeNum; }
    public void setLikeNum(Integer likeNum) { this.likeNum = likeNum; }
    public Integer getCollectionNum() { return collectionNum; }
    public void setCollectionNum(Integer collectionNum) { this.collectionNum = collectionNum; }
    public Integer getEvaluateNum() { return evaluateNum; }
    public void setEvaluateNum(Integer evaluateNum) { this.evaluateNum = evaluateNum; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCreateUserId() { return createUserId; }
    public void setCreateUserId(String createUserId) { this.createUserId = createUserId; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}
