package com.xuanyue.exp.mobile.dto;

import com.xuanyue.exp.common.PageResult;

/**
 * 首页首屏聚合：feed + 公告 + 未读数（P2 减少 HTTP 往返）。
 */
public class HomeBootstrapDto {

    private PageResult<HomeFeedItem> feed;
    private NoticeDto notice;
    private long unreadCount;

    public PageResult<HomeFeedItem> getFeed() {
        return feed;
    }

    public void setFeed(PageResult<HomeFeedItem> feed) {
        this.feed = feed;
    }

    public NoticeDto getNotice() {
        return notice;
    }

    public void setNotice(NoticeDto notice) {
        this.notice = notice;
    }

    public long getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(long unreadCount) {
        this.unreadCount = unreadCount;
    }
}
