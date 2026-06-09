package com.xuanyue.exp.exp.service;

import com.xuanyue.exp.common.PageResult;

import java.util.Map;

public interface ExpLogService {

    PageResult<?> list(String keyword, String expId, String logType, String logUserId, int pageNum, int pageSize);

    Object get(String id);

    void create(Map<String, Object> payload, String currentUserId);

    void update(String id, Map<String, Object> payload, String currentUserId);

    void delete(String id);
}
