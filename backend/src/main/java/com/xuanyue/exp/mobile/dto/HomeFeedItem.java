package com.xuanyue.exp.mobile.dto;

/**
 * 首页信息流条目 DTO
 */
public class HomeFeedItem {

    private String id;
    /** 实验/视频标题 */
    private String title;
    /** 封面图URL */
    private String coverUrl;
    /** 首条实验视频 URL（与详情页播放器一致） */
    private String videoUrl;
    /** 类型：video-视频, experiment-实验, simulation-模拟 */
    private String type;
    /** 学科名 */
    private String subject;
    /** 年级 */
    private String grade;
    /** 播放量 */
    private Integer playCount;
    /** 作者姓名 */
    private String author;
    /** 作者职称 */
    private String authorTitle;
    /** 作者学校 */
    private String authorSchool;
    /** 时长 */
    private String duration;
    /** 标签文字 */
    private String tagLabel;
    /** 标签类型 */
    private String tagType;
    /** 渐变色CSS类名 */
    private String gradientClass;
    /** 关联模拟器 ID（type=simulation 时） */
    private String simulatorId;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
    public Integer getPlayCount() { return playCount; }
    public void setPlayCount(Integer playCount) { this.playCount = playCount; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getAuthorTitle() { return authorTitle; }
    public void setAuthorTitle(String authorTitle) { this.authorTitle = authorTitle; }
    public String getAuthorSchool() { return authorSchool; }
    public void setAuthorSchool(String authorSchool) { this.authorSchool = authorSchool; }
    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }
    public String getTagLabel() { return tagLabel; }
    public void setTagLabel(String tagLabel) { this.tagLabel = tagLabel; }
    public String getTagType() { return tagType; }
    public void setTagType(String tagType) { this.tagType = tagType; }
    public String getGradientClass() { return gradientClass; }
    public void setGradientClass(String gradientClass) { this.gradientClass = gradientClass; }
    public String getSimulatorId() { return simulatorId; }
    public void setSimulatorId(String simulatorId) { this.simulatorId = simulatorId; }
}
