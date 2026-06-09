package com.xuanyue.exp.mobile.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "mb_task")
public class MbTask {

    @Id
    @Column(name = "task_id")
    private String taskId;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "task_type")
    private String taskType;

    @Column(name = "teacher_user_id")
    private String teacherUserId;

    @Column(name = "class_org_id")
    private String classOrgId;

    @Column(name = "deadline")
    private Date deadline;

    @Column(name = "video_id")
    private String videoId;

    @Column(name = "video_title")
    private String videoTitle;

    @Column(name = "video_duration")
    private String videoDuration;

    @Column(name = "video_meta")
    private String videoMeta;

    @Column(name = "teacher_hint")
    private String teacherHint;

    @Column(name = "teacher_hint_class")
    private String teacherHintClass;

    @Column(name = "task_type_label")
    private String taskTypeLabel;

    @Column(name = "requirements_json")
    private String requirementsJson;

    @Column(name = "steps_json")
    private String stepsJson;

    @Column(name = "upload_query")
    private String uploadQuery;

    @Column(name = "status")
    private String status;

    @Column(name = "create_time")
    private Date createTime;

    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getTaskType() { return taskType; }
    public void setTaskType(String taskType) { this.taskType = taskType; }
    public String getTeacherUserId() { return teacherUserId; }
    public void setTeacherUserId(String teacherUserId) { this.teacherUserId = teacherUserId; }
    public String getClassOrgId() { return classOrgId; }
    public void setClassOrgId(String classOrgId) { this.classOrgId = classOrgId; }
    public Date getDeadline() { return deadline; }
    public void setDeadline(Date deadline) { this.deadline = deadline; }
    public String getVideoId() { return videoId; }
    public void setVideoId(String videoId) { this.videoId = videoId; }
    public String getVideoTitle() { return videoTitle; }
    public void setVideoTitle(String videoTitle) { this.videoTitle = videoTitle; }
    public String getVideoDuration() { return videoDuration; }
    public void setVideoDuration(String videoDuration) { this.videoDuration = videoDuration; }
    public String getVideoMeta() { return videoMeta; }
    public void setVideoMeta(String videoMeta) { this.videoMeta = videoMeta; }
    public String getTeacherHint() { return teacherHint; }
    public void setTeacherHint(String teacherHint) { this.teacherHint = teacherHint; }
    public String getTeacherHintClass() { return teacherHintClass; }
    public void setTeacherHintClass(String teacherHintClass) { this.teacherHintClass = teacherHintClass; }
    public String getTaskTypeLabel() { return taskTypeLabel; }
    public void setTaskTypeLabel(String taskTypeLabel) { this.taskTypeLabel = taskTypeLabel; }
    public String getRequirementsJson() { return requirementsJson; }
    public void setRequirementsJson(String requirementsJson) { this.requirementsJson = requirementsJson; }
    public String getStepsJson() { return stepsJson; }
    public void setStepsJson(String stepsJson) { this.stepsJson = stepsJson; }
    public String getUploadQuery() { return uploadQuery; }
    public void setUploadQuery(String uploadQuery) { this.uploadQuery = uploadQuery; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}
