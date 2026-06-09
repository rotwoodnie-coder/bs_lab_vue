package com.xuanyue.exp.mobile.dto;

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
