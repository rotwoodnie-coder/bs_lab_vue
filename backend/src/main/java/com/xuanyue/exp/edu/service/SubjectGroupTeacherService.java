package com.xuanyue.exp.edu.service;

import java.util.List;
import java.util.Map;

public interface SubjectGroupTeacherService {
    Object get(String groupId);

    void save(String groupId, Object payload, String currentUserId, String currentRootOrgId);

    List<Map<String, Object>> teacherAssignOptions(String subjectId);
}
