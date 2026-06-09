package com.xuanyue.exp.exp.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "exp_msg")
public class ExpMsg {

    @Id
    @Column(name = "exp_id")
    private String expId;

    @Column(name = "exp_name")
    private String expName;

    @Column(name = "choose_type")
    private String chooseType;

    @Column(name = "subject_id")
    private String subjectId;

    @Column(name = "school_level_id")
    private String schoolLevelId;

    @Column(name = "grade_id")
    private String gradeId;

    @Column(name = "semester_id")
    private String semesterId;

    @Column(name = "difficulty_id")
    private String difficultyId;

    @Lob
    @Column(name = "exp_principle", columnDefinition = "MEDIUMTEXT")
    private String expPrinciple;

    @Column(name = "exp_caution")
    private String expCaution;

    @Column(name = "exp_danger")
    private String expDanger;

    @Column(name = "class_hour")
    private java.math.BigDecimal classHour;

    @Column(name = "simulator_url")
    private String simulatorUrl;

    @Column(name = "simulator_id")
    private String simulatorId;

    @Column(name = "coursebook_id")
    private String coursebookId;

    @Column(name = "unit_id")
    private String unitId;

    @Column(name = "chapter_id")
    private String chapterId;

    @Column(name = "section_id")
    private String sectionId;

    @Column(name = "link_exp_id")
    private String linkExpId;

    @Column(name = "exp_type")
    private String expType;

    @Column(name = "exp_task_type")
    private String expTaskType;

    @Column(name = "like_num")
    private Integer likeNum;

    @Column(name = "notlike_num")
    private Integer notlikeNum;

    @Column(name = "collection_num")
    private Integer collectionNum;

    @Column(name = "evaluate_num")
    private Integer evaluateNum;

    @Column(name = "status")
    private String status;

    @Column(name = "create_user_id")
    private String createUserId;

    @Column(name = "create_time")
    private Date createTime;

    @Column(name = "confirm_user_id")
    private String confirmUserId;

    @Column(name = "confirm_time")
    private Date confirmTime;

    @Column(name = "confirm_comments")
    private String confirmComments;

    @Column(name = "update_user_id")
    private String updateUserId;

    @Column(name = "update_time")
    private Date updateTime;

    public String getExpId() { return expId; }
    public void setExpId(String expId) { this.expId = expId; }
    public String getExpName() { return expName; }
    public void setExpName(String expName) { this.expName = expName; }
    public String getChooseType() { return chooseType; }
    public void setChooseType(String chooseType) { this.chooseType = chooseType; }
    public String getSubjectId() { return subjectId; }
    public void setSubjectId(String subjectId) { this.subjectId = subjectId; }
    public String getSchoolLevelId() { return schoolLevelId; }
    public void setSchoolLevelId(String schoolLevelId) { this.schoolLevelId = schoolLevelId; }
    public String getGradeId() { return gradeId; }
    public void setGradeId(String gradeId) { this.gradeId = gradeId; }
    public String getSemesterId() { return semesterId; }
    public void setSemesterId(String semesterId) { this.semesterId = semesterId; }
    public String getDifficultyId() { return difficultyId; }
    public void setDifficultyId(String difficultyId) { this.difficultyId = difficultyId; }
    public String getExpPrinciple() { return expPrinciple; }
    public void setExpPrinciple(String expPrinciple) { this.expPrinciple = expPrinciple; }
    public String getExpCaution() { return expCaution; }
    public void setExpCaution(String expCaution) { this.expCaution = expCaution; }
    public String getExpDanger() { return expDanger; }
    public void setExpDanger(String expDanger) { this.expDanger = expDanger; }
    public java.math.BigDecimal getClassHour() { return classHour; }
    public void setClassHour(java.math.BigDecimal classHour) { this.classHour = classHour; }
    public String getSimulatorUrl() { return simulatorUrl; }
    public void setSimulatorUrl(String simulatorUrl) { this.simulatorUrl = simulatorUrl; }
    public String getSimulatorId() { return simulatorId; }
    public void setSimulatorId(String simulatorId) { this.simulatorId = simulatorId; }
    public String getCoursebookId() { return coursebookId; }
    public void setCoursebookId(String coursebookId) { this.coursebookId = coursebookId; }
    public String getUnitId() { return unitId; }
    public void setUnitId(String unitId) { this.unitId = unitId; }
    public String getChapterId() { return chapterId; }
    public void setChapterId(String chapterId) { this.chapterId = chapterId; }
    public String getSectionId() { return sectionId; }
    public void setSectionId(String sectionId) { this.sectionId = sectionId; }
    public String getLinkExpId() { return linkExpId; }
    public void setLinkExpId(String linkExpId) { this.linkExpId = linkExpId; }
    public String getExpType() { return expType; }
    public void setExpType(String expType) { this.expType = expType; }
    public String getExpTaskType() { return expTaskType; }
    public void setExpTaskType(String expTaskType) { this.expTaskType = expTaskType; }
    public Integer getLikeNum() { return likeNum; }
    public void setLikeNum(Integer likeNum) { this.likeNum = likeNum; }
    public Integer getNotlikeNum() { return notlikeNum; }
    public void setNotlikeNum(Integer notlikeNum) { this.notlikeNum = notlikeNum; }
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
    public String getConfirmUserId() { return confirmUserId; }
    public void setConfirmUserId(String confirmUserId) { this.confirmUserId = confirmUserId; }
    public Date getConfirmTime() { return confirmTime; }
    public void setConfirmTime(Date confirmTime) { this.confirmTime = confirmTime; }
    public String getConfirmComments() { return confirmComments; }
    public void setConfirmComments(String confirmComments) { this.confirmComments = confirmComments; }
    public String getUpdateUserId() { return updateUserId; }
    public void setUpdateUserId(String updateUserId) { this.updateUserId = updateUserId; }
    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
}
