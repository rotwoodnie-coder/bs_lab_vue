package com.xuanyue.exp.mobile.dto;

public class CreateCommentRequest {

    private String targetType;
    private String targetId;
    private String content;

    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }
    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
