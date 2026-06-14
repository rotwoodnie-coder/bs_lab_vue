package com.xuanyue.exp.edu.service;

import com.xuanyue.exp.common.PageResult;

import java.util.Map;

public interface TeacherClassService {
    PageResult<?> page(int pageNum, int pageSize, String keyword, String currentRootOrgId);
    Object get(String teacherId);
    void save(String teacherId, Map<String, Object> payload, String currentUserId, String currentRootOrgId);
}
