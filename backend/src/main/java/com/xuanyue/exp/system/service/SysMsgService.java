package com.xuanyue.exp.system.service;

import com.xuanyue.exp.common.PageResult;

public interface SysMsgService {
    PageResult<?> page(int pageNum, int pageSize, String msgTypeId, String readTag, String currentUserId);
    long unreadCount(String currentUserId);
    void markRead(String msgId, String currentUserId);
}
