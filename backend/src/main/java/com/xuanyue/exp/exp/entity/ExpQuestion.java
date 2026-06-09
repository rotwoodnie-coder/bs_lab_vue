package com.xuanyue.exp.exp.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;
import javax.persistence.Lob;

@Entity
@Table(name = "exp_question")
public class ExpQuestion {

    @Id
    @Column(name = "question_id")
    private String questionId;

    @Lob
    @Column(name = "question_content", columnDefinition = "MEDIUMTEXT")
    private String questionContent;

    @Column(name = "question_type_id")
    private String questionTypeId;

    @Column(name = "difficulty_type_id")
    private String difficultyTypeId;

    @Column(name = "question_capacity_id")
    private String questionCapacityId;

    @Column(name = "grade_id")
    private String gradeId;

    @Column(name = "subject_id")
    private String subjectId;

    @Column(name = "coursebook_id")
    private String coursebookId;

    @Column(name = "content_unit_id")
    private String contentUnitId;

    @Column(name = "content_chapter_id")
    private String contentChapterId;

    @Column(name = "content_section_id")
    private String contentSectionId;

    @Column(name = "knowledge_content")
    private String knowledgeContent;

    @Column(name = "status")
    private String status;

    @Column(name = "create_user_id")
    private String createUserId;

    @Column(name = "create_time")
    private Date createTime;

    @Column(name = "update_user_id")
    private String updateUserId;

    @Column(name = "update_time")
    private Date updateTime;

    public String getQuestionId() { return questionId; }
    public void setQuestionId(String questionId) { this.questionId = questionId; }
    public String getQuestionContent() { return questionContent; }
    public void setQuestionContent(String questionContent) { this.questionContent = questionContent; }
    public String getQuestionTypeId() { return questionTypeId; }
    public void setQuestionTypeId(String questionTypeId) { this.questionTypeId = questionTypeId; }
    public String getDifficultyTypeId() { return difficultyTypeId; }
    public void setDifficultyTypeId(String difficultyTypeId) { this.difficultyTypeId = difficultyTypeId; }
    public String getQuestionCapacityId() { return questionCapacityId; }
    public void setQuestionCapacityId(String questionCapacityId) { this.questionCapacityId = questionCapacityId; }
    public String getGradeId() { return gradeId; }
    public void setGradeId(String gradeId) { this.gradeId = gradeId; }
    public String getSubjectId() { return subjectId; }
    public void setSubjectId(String subjectId) { this.subjectId = subjectId; }
    public String getCoursebookId() { return coursebookId; }
    public void setCoursebookId(String coursebookId) { this.coursebookId = coursebookId; }
    public String getContentUnitId() { return contentUnitId; }
    public void setContentUnitId(String contentUnitId) { this.contentUnitId = contentUnitId; }
    public String getContentChapterId() { return contentChapterId; }
    public void setContentChapterId(String contentChapterId) { this.contentChapterId = contentChapterId; }
    public String getContentSectionId() { return contentSectionId; }
    public void setContentSectionId(String contentSectionId) { this.contentSectionId = contentSectionId; }
    public String getKnowledgeContent() { return knowledgeContent; }
    public void setKnowledgeContent(String knowledgeContent) { this.knowledgeContent = knowledgeContent; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCreateUserId() { return createUserId; }
    public void setCreateUserId(String createUserId) { this.createUserId = createUserId; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public String getUpdateUserId() { return updateUserId; }
    public void setUpdateUserId(String updateUserId) { this.updateUserId = updateUserId; }
    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
}
