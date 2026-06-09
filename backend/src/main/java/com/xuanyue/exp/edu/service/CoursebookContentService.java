package com.xuanyue.exp.edu.service;

import java.util.List;
import java.util.Map;
import com.xuanyue.exp.edu.entity.DataCoursebookContent;

public interface CoursebookContentService {

    List<Map<String, Object>> listByCoursebook(String coursebookId);

    List<Map<String, Object>> listByCoursebookWithTeachCount(String coursebookId, String currentUserId);

    void create(Map<String, Object> payload);

    void update(String id, Map<String, Object> payload);

    void delete(String id);

    void reorder(String coursebookId, List<Map<String, Object>> tree);

    DataCoursebookContent findById(String id);
}
