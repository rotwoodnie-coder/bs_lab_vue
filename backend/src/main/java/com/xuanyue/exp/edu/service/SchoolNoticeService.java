package com.xuanyue.exp.edu.service;

import com.xuanyue.exp.common.PageResult;

import java.util.Map;

public interface SchoolNoticeService {

    PageResult<?> list(String keyword, String status, String noticeOrgId, int pageNum, int pageSize);

    Object get(String id);

    void create(Map<String, Object> payload, String currentUserId);

    void update(String id, Map<String, Object> payload, String currentUserId);

    void confirm(String id, Map<String, Object> payload, String currentUserId);

    void publish(String id, Map<String, Object> payload, String currentUserId);

    void voidNotice(String id, Map<String, Object> payload, String currentUserId);

    void delete(String id);
}
