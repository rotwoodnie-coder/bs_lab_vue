package com.xuanyue.exp.exp.service;

import java.util.List;
import java.util.Map;

public interface ExpQuestionService {

    List<Map<String, Object>> list(String keyword, String status, String questionTypeId, String gradeId, String subjectId);

    List<Map<String, Object>> page(int pageNum, int pageSize, String keyword, String status, String questionTypeId, String gradeId, String subjectId);

    long count(String keyword, String status, String questionTypeId, String gradeId, String subjectId);

    Map<String, Object> get(String id);

    String create(Map<String, Object> payload);

    void update(String id, Map<String, Object> payload);

    void delete(String id);
}
