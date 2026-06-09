package com.xuanyue.exp.edu.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "data_coursebook")
public class DataCoursebook {

    @Id
    @Column(name = "coursebook_id")
    private String coursebookId;

    @Column(name = "coursebook_name")
    private String coursebookName;

    @Column(name = "edition_id")
    private String editionId;

    @Column(name = "subject_id")
    private String subjectId;

    @Column(name = "level_id")
    private String levelId;

    @Column(name = "grade_id")
    private String gradeId;

    @Column(name = "semester_id")
    private String semesterId;

    @Column(name = "file_url")
    private String fileUrl;

    @Column(name = "comments")
    private String comments;

    @Column(name = "status")
    private String status;

    @Transient
    private String previewUrl;

    public String getCoursebookId() {
        return coursebookId;
    }

    public void setCoursebookId(String coursebookId) {
        this.coursebookId = coursebookId;
    }

    public String getCoursebookName() {
        return coursebookName;
    }

    public void setCoursebookName(String coursebookName) {
        this.coursebookName = coursebookName;
    }

    public String getEditionId() {
        return editionId;
    }

    public void setEditionId(String editionId) {
        this.editionId = editionId;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public String getLevelId() {
        return levelId;
    }

    public void setLevelId(String levelId) {
        this.levelId = levelId;
    }

    public String getGradeId() {
        return gradeId;
    }

    public void setGradeId(String gradeId) {
        this.gradeId = gradeId;
    }

    public String getSemesterId() {
        return semesterId;
    }

    public void setSemesterId(String semesterId) {
        this.semesterId = semesterId;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    public String getPreviewUrl() {
        return previewUrl;
    }

    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
    }
}
