package com.xuanyue.exp.mobile.dto;

import java.util.ArrayList;
import java.util.List;

public class MobileWorkDetailDto {

    private String id;
    private String title;
    private String grade;
    private String author;
    private String className;
    private String time;
    private String desc;
    private int likes;
    private int comments;
    private String sourceExpId;
    private String workType;
    private String taskId;
    private String reviewStatus;
    private String reviewStatusLabel;
    private String coverUrl;
    private String coverType;
    private int fileCount;
    private List<MobileWorkFileDto> files = new ArrayList<>();
    private TeacherReview teacherReview;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    public String getDesc() { return desc; }
    public void setDesc(String desc) { this.desc = desc; }
    public int getLikes() { return likes; }
    public void setLikes(int likes) { this.likes = likes; }
    public int getComments() { return comments; }
    public void setComments(int comments) { this.comments = comments; }
    public String getSourceExpId() { return sourceExpId; }
    public void setSourceExpId(String sourceExpId) { this.sourceExpId = sourceExpId; }
    public String getWorkType() { return workType; }
    public void setWorkType(String workType) { this.workType = workType; }
    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }
    public String getReviewStatus() { return reviewStatus; }
    public void setReviewStatus(String reviewStatus) { this.reviewStatus = reviewStatus; }
    public String getReviewStatusLabel() { return reviewStatusLabel; }
    public void setReviewStatusLabel(String reviewStatusLabel) { this.reviewStatusLabel = reviewStatusLabel; }
    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    public String getCoverType() { return coverType; }
    public void setCoverType(String coverType) { this.coverType = coverType; }
    public int getFileCount() { return fileCount; }
    public void setFileCount(int fileCount) { this.fileCount = fileCount; }
    public List<MobileWorkFileDto> getFiles() { return files; }
    public void setFiles(List<MobileWorkFileDto> files) { this.files = files != null ? files : new ArrayList<>(); }
    public TeacherReview getTeacherReview() { return teacherReview; }
    public void setTeacherReview(TeacherReview teacherReview) { this.teacherReview = teacherReview; }

    public static class TeacherReview {
        private String name;
        private String text;
        private int stars;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
        public int getStars() { return stars; }
        public void setStars(int stars) { this.stars = stars; }
    }
}
