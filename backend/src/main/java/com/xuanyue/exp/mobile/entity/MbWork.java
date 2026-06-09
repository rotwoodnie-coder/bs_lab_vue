package com.xuanyue.exp.mobile.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "mb_work")
public class MbWork {

    @Id
    @Column(name = "work_id")
    private String workId;

    @Column(name = "student_user_id")
    private String studentUserId;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "work_type")
    private String workType;

    @Column(name = "task_id")
    private String taskId;

    @Column(name = "source_exp_id")
    private String sourceExpId;

    @Column(name = "submission_id")
    private String submissionId;

    @Column(name = "grade")
    private String grade;

    @Column(name = "review_status")
    private String reviewStatus;

    @Column(name = "is_featured")
    private String isFeatured;

    @Column(name = "teacher_review_name")
    private String teacherReviewName;

    @Column(name = "teacher_review_text")
    private String teacherReviewText;

    @Column(name = "teacher_review_stars")
    private Integer teacherReviewStars;

    @Column(name = "like_count")
    private Integer likeCount;

    @Column(name = "comment_count")
    private Integer commentCount;

    @Column(name = "class_name")
    private String className;

    @Column(name = "school_name")
    private String schoolName;

    @Column(name = "tint")
    private String tint;

    @Column(name = "status")
    private String status;

    @Column(name = "create_time")
    private Date createTime;

    public String getWorkId() { return workId; }
    public void setWorkId(String workId) { this.workId = workId; }
    public String getStudentUserId() { return studentUserId; }
    public void setStudentUserId(String studentUserId) { this.studentUserId = studentUserId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getWorkType() { return workType; }
    public void setWorkType(String workType) { this.workType = workType; }
    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }
    public String getSourceExpId() { return sourceExpId; }
    public void setSourceExpId(String sourceExpId) { this.sourceExpId = sourceExpId; }
    public String getSubmissionId() { return submissionId; }
    public void setSubmissionId(String submissionId) { this.submissionId = submissionId; }
    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
    public String getReviewStatus() { return reviewStatus; }
    public void setReviewStatus(String reviewStatus) { this.reviewStatus = reviewStatus; }
    public String getIsFeatured() { return isFeatured; }
    public void setIsFeatured(String isFeatured) { this.isFeatured = isFeatured; }
    public String getTeacherReviewName() { return teacherReviewName; }
    public void setTeacherReviewName(String teacherReviewName) { this.teacherReviewName = teacherReviewName; }
    public String getTeacherReviewText() { return teacherReviewText; }
    public void setTeacherReviewText(String teacherReviewText) { this.teacherReviewText = teacherReviewText; }
    public Integer getTeacherReviewStars() { return teacherReviewStars; }
    public void setTeacherReviewStars(Integer teacherReviewStars) { this.teacherReviewStars = teacherReviewStars; }
    public Integer getLikeCount() { return likeCount; }
    public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }
    public Integer getCommentCount() { return commentCount; }
    public void setCommentCount(Integer commentCount) { this.commentCount = commentCount; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public String getSchoolName() { return schoolName; }
    public void setSchoolName(String schoolName) { this.schoolName = schoolName; }
    public String getTint() { return tint; }
    public void setTint(String tint) { this.tint = tint; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}
