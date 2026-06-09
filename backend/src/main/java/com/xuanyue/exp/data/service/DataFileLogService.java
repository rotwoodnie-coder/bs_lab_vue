package com.xuanyue.exp.data.service;

import com.xuanyue.exp.common.PageResult;

import java.util.Map;

public interface DataFileLogService {

    PageResult<?> list(String keyword, String fileId, String logType, String logUserId, int pageNum, int pageSize);

    Object get(String id);

    void create(Map<String, Object> payload, String currentUserId);

    void update(String id, Map<String, Object> payload, String currentUserId);

    void delete(String id);
}
