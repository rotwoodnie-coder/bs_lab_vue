package com.xuanyue.exp.mobile.dto;

public class TeacherReviewRequest {

    /** excellent / good / pass / fail */
    private String rating;
    private String comment;
    private Boolean featured;

    public String getRating() { return rating; }
    public void setRating(String rating) { this.rating = rating; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public Boolean getFeatured() { return featured; }
    public void setFeatured(Boolean featured) { this.featured = featured; }
}
