package com.xuanyue.exp.mobile.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MobileSocialSummaryDto {

    private int likeCount;
    private int collectCount;
    private int commentCount;
    private boolean liked;
    private boolean collected;

    public int getLikeCount() { return likeCount; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }
    public int getCollectCount() { return collectCount; }
    public void setCollectCount(int collectCount) { this.collectCount = collectCount; }
    public int getCommentCount() { return commentCount; }
    public void setCommentCount(int commentCount) { this.commentCount = commentCount; }
    @JsonProperty("liked")
    public boolean isLiked() { return liked; }
    public void setLiked(boolean liked) { this.liked = liked; }
    @JsonProperty("collected")
    public boolean isCollected() { return collected; }
    public void setCollected(boolean collected) { this.collected = collected; }
}
