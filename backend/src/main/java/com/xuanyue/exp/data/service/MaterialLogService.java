package com.xuanyue.exp.data.service;

import com.xuanyue.exp.common.PageResult;

import java.util.Map;

public interface MaterialLogService {

    PageResult<?> list(String keyword, String materialId, String logType, String logUserId, int pageNum, int pageSize);

    Object get(String id);

    void create(Map<String, Object> payload, String currentUserId);

    void update(String id, Map<String, Object> payload, String currentUserId);

    void delete(String id);
}
