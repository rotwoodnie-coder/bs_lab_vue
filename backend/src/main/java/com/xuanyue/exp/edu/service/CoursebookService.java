package com.xuanyue.exp.edu.service;

import java.util.List;
import java.util.Map;

import com.xuanyue.exp.edu.entity.DataCoursebook;

public interface CoursebookService {

    List<Map<String, Object>> list(String keyword, String editionId, String subjectId, String gradeId);

    Object get(String id);

    DataCoursebook findById(String id);

    void create(Map<String, Object> payload);

    void update(String id, Map<String, Object> payload);

    void delete(String id);

    String getFileUrl(String id);

    List<Map<String, Object>> page(int pageNum, int pageSize, String keyword, String editionId, String subjectId, String gradeId);

    long count(String keyword, String editionId, String subjectId, String gradeId);
}
