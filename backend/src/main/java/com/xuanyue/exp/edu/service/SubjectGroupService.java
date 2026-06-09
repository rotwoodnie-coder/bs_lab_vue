package com.xuanyue.exp.edu.service;

import com.xuanyue.exp.common.PageResult;

import java.util.Map;

public interface SubjectGroupService {
    PageResult<?> page(int pageNum, int pageSize, String keyword, String currentRootOrgId);
    Object get(String groupId);
    void create(Map<String, Object> payload, String currentUserId, String currentRootOrgId);
    void update(String groupId, Map<String, Object> payload, String currentUserId, String currentRootOrgId);
    void delete(String groupId, String currentRootOrgId);
}
