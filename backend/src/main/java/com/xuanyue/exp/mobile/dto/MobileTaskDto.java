package com.xuanyue.exp.mobile.dto;

import java.util.List;

public class MobileTaskDto {

    private String id;
    private String type;
    private String title;
    private String desc;
    private String state;
    private String stateLabel;
    private String badgeClass;
    private String deadline;
    private String teacherHint;
    private String teacherHintClass;
    private String videoTitle;
    private String videoDuration;
    private String videoMeta;
    private String videoId;
    private String taskTypeLabel;
    private List<String> requirements;
    private List<String> steps;
    private String uploadQuery;
    private MobileTaskExpBriefDto expBrief;
    private List<String> completionGuide;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDesc() { return desc; }
    public void setDesc(String desc) { this.desc = desc; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getStateLabel() { return stateLabel; }
    public void setStateLabel(String stateLabel) { this.stateLabel = stateLabel; }
    public String getBadgeClass() { return badgeClass; }
    public void setBadgeClass(String badgeClass) { this.badgeClass = badgeClass; }
    public String getDeadline() { return deadline; }
    public void setDeadline(String deadline) { this.deadline = deadline; }
    public String getTeacherHint() { return teacherHint; }
    public void setTeacherHint(String teacherHint) { this.teacherHint = teacherHint; }
    public String getTeacherHintClass() { return teacherHintClass; }
    public void setTeacherHintClass(String teacherHintClass) { this.teacherHintClass = teacherHintClass; }
    public String getVideoTitle() { return videoTitle; }
    public void setVideoTitle(String videoTitle) { this.videoTitle = videoTitle; }
    public String getVideoDuration() { return videoDuration; }
    public void setVideoDuration(String videoDuration) { this.videoDuration = videoDuration; }
    public String getVideoMeta() { return videoMeta; }
    public void setVideoMeta(String videoMeta) { this.videoMeta = videoMeta; }
    public String getVideoId() { return videoId; }
    public void setVideoId(String videoId) { this.videoId = videoId; }
    public String getTaskTypeLabel() { return taskTypeLabel; }
    public void setTaskTypeLabel(String taskTypeLabel) { this.taskTypeLabel = taskTypeLabel; }
    public List<String> getRequirements() { return requirements; }
    public void setRequirements(List<String> requirements) { this.requirements = requirements; }
    public List<String> getSteps() { return steps; }
    public void setSteps(List<String> steps) { this.steps = steps; }
    public String getUploadQuery() { return uploadQuery; }
    public void setUploadQuery(String uploadQuery) { this.uploadQuery = uploadQuery; }
    public MobileTaskExpBriefDto getExpBrief() { return expBrief; }
    public void setExpBrief(MobileTaskExpBriefDto expBrief) { this.expBrief = expBrief; }
    public List<String> getCompletionGuide() { return completionGuide; }
    public void setCompletionGuide(List<String> completionGuide) { this.completionGuide = completionGuide; }
}
