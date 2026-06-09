package com.xuanyue.exp.edu.service;

import java.util.Map;

public interface SubjectGroupResearcherService {
    Object get(String groupId);
    void save(String groupId, Map<String, Object> payload, String currentUserId, String currentRootOrgId);
}
