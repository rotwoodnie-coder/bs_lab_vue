package com.xuanyue.exp.mobile.admin.dto;

public class AdminWorkReviewRequest {

    /** pass/excellent/good -> 通过(y)；fail -> 驳回(n) */
    private String result;
    private String comment;

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
