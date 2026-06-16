package com.xuanyue.exp.mobile.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MobileCommentDto {

    private String id;
    private String userName;
    private String userInitial;
    /** 评论用户头像可访问 URL */
    private String userAvatarUrl;
    private String userRoleTag;
    private String content;
    private int likeCount;
    private boolean liked;
    private String timeLabel;
    private boolean mine;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getUserInitial() { return userInitial; }
    public void setUserInitial(String userInitial) { this.userInitial = userInitial; }
    public String getUserAvatarUrl() { return userAvatarUrl; }
    public void setUserAvatarUrl(String userAvatarUrl) { this.userAvatarUrl = userAvatarUrl; }
    public String getUserRoleTag() { return userRoleTag; }
    public void setUserRoleTag(String userRoleTag) { this.userRoleTag = userRoleTag; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public int getLikeCount() { return likeCount; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }
    @JsonProperty("liked")
    public boolean isLiked() { return liked; }
    public void setLiked(boolean liked) { this.liked = liked; }
    public String getTimeLabel() { return timeLabel; }
    public void setTimeLabel(String timeLabel) { this.timeLabel = timeLabel; }
    public boolean isMine() { return mine; }
    public void setMine(boolean mine) { this.mine = mine; }
}
