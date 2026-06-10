package com.xuanyue.exp.homework.service;

import com.xuanyue.exp.common.PageResult;
import com.xuanyue.exp.homework.entity.ExpHomeworkStudent;

import java.util.Map;

public interface ExpHomeworkStudentService {
    PageResult<ExpHomeworkStudent> page(String keyword, String homeworkId, String teacherExpId, String teacherUserId, String studentExpId, int pageNum, int pageSize);
    ExpHomeworkStudent get(String seqId);
    void create(Map<String, Object> payload);
    void update(String seqId, Map<String, Object> payload);
    void delete(String seqId);
}
