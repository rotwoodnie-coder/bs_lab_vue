package com.xuanyue.exp.mobile.dto;

public class MobileTaskListItemDto {

    private String id;
    /** experiment | remix | creative | quiz */
    private String category;
    /** task | quiz | creative-start */
    private String kind;
    /** standard | simulator（实验类子类型） */
    private String subType;
    private String title;
    private String desc;
    private String state;
    private String stateLabel;
    private String badgeClass;
    private String link;
    private String deadline;
    private String quizScore;
    private long sortTime;
    /** 实验材料摘要（待完成任务，家长/学生列表） */
    private String materialsLine;
    /** 协助上传路径，如 /upload?taskId=xxx */
    private String uploadLink;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getKind() { return kind; }
    public void setKind(String kind) { this.kind = kind; }
    public String getSubType() { return subType; }
    public void setSubType(String subType) { this.subType = subType; }
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
    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }
    public String getDeadline() { return deadline; }
    public void setDeadline(String deadline) { this.deadline = deadline; }
    public String getQuizScore() { return quizScore; }
    public void setQuizScore(String quizScore) { this.quizScore = quizScore; }
    public long getSortTime() { return sortTime; }
    public void setSortTime(long sortTime) { this.sortTime = sortTime; }
    public String getMaterialsLine() { return materialsLine; }
    public void setMaterialsLine(String materialsLine) { this.materialsLine = materialsLine; }
    public String getUploadLink() { return uploadLink; }
    public void setUploadLink(String uploadLink) { this.uploadLink = uploadLink; }
}
