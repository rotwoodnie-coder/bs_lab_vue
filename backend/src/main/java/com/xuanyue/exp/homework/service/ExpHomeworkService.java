package com.xuanyue.exp.homework.service;

import com.xuanyue.exp.common.PageResult;
import com.xuanyue.exp.homework.entity.ExpHomework;

import java.util.Map;

public interface ExpHomeworkService {
    PageResult<Map<String, Object>> page(String keyword, String teacherExpId, String tearcherUserId, String classId, String requireDate, int pageNum, int pageSize);
    ExpHomework get(String homeworkId);
    void create(Map<String, Object> payload);
    void update(String homeworkId, Map<String, Object> payload);
    void assign(String homeworkId);
    void delete(String homeworkId);
}
