package com.xuanyue.exp.mobile.dto;

public class ToggleReactionRequest {

    private String targetType;
    private String targetId;
    /** like / collect */
    private String reactionType;

    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }
    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }
    public String getReactionType() { return reactionType; }
    public void setReactionType(String reactionType) { this.reactionType = reactionType; }
}
