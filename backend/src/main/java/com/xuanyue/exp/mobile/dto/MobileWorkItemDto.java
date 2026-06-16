package com.xuanyue.exp.mobile.dto;

public class MobileWorkItemDto {

    private String id;
    private String type;
    private String title;
    private String author;
    private String authorInitial;
    /** 作者头像可访问 URL */
    private String authorAvatarUrl;
    private String className;
    private String school;
    private String tint;
    private String sourceExpId;
    private String reviewStatus;
    private String reviewStatusLabel;
    private String reviewBadgeClass;
    private String timeLabel;
    private boolean canEdit;
    private String coverUrl;
    private String coverType;
    private int fileCount;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getAuthorInitial() { return authorInitial; }
    public void setAuthorInitial(String authorInitial) { this.authorInitial = authorInitial; }
    public String getAuthorAvatarUrl() { return authorAvatarUrl; }
    public void setAuthorAvatarUrl(String authorAvatarUrl) { this.authorAvatarUrl = authorAvatarUrl; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public String getSchool() { return school; }
    public void setSchool(String school) { this.school = school; }
    public String getTint() { return tint; }
    public void setTint(String tint) { this.tint = tint; }
    public String getSourceExpId() { return sourceExpId; }
    public void setSourceExpId(String sourceExpId) { this.sourceExpId = sourceExpId; }
    public String getReviewStatus() { return reviewStatus; }
    public void setReviewStatus(String reviewStatus) { this.reviewStatus = reviewStatus; }
    public String getReviewStatusLabel() { return reviewStatusLabel; }
    public void setReviewStatusLabel(String reviewStatusLabel) { this.reviewStatusLabel = reviewStatusLabel; }
    public String getReviewBadgeClass() { return reviewBadgeClass; }
    public void setReviewBadgeClass(String reviewBadgeClass) { this.reviewBadgeClass = reviewBadgeClass; }
    public String getTimeLabel() { return timeLabel; }
    public void setTimeLabel(String timeLabel) { this.timeLabel = timeLabel; }
    public boolean isCanEdit() { return canEdit; }
    public void setCanEdit(boolean canEdit) { this.canEdit = canEdit; }
    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    public String getCoverType() { return coverType; }
    public void setCoverType(String coverType) { this.coverType = coverType; }
    public int getFileCount() { return fileCount; }
    public void setFileCount(int fileCount) { this.fileCount = fileCount; }
}
