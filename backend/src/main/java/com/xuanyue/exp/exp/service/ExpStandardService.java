package com.xuanyue.exp.exp.service;

import java.util.List;
import java.util.Map;

public interface ExpStandardService {

    List<Map<String, Object>> list(String keyword, String status, String subjectId, String schoolLevelId, String gradeId, String chooseType, String expType,String currentUserId,String notstatus);

    List<Map<String, Object>> pageStandard(int pageNum, int pageSize, String keyword, String status, String subjectId, String schoolLevelId, String gradeId, String chooseType, String expType,String currentUserId,String notstatus);

    List<Map<String, Object>> pageTeach(int pageNum, int pageSize, String keyword, String status, String subjectId, String schoolLevelId, String gradeId, String chooseType, String expType,String currentUserId,String notstatus);

    long count(String keyword, String status, String subjectId, String schoolLevelId, String gradeId, String chooseType, String expType,String currentUserId,String notstatus);

    Map<String, Object> get(String id);

    Map<String, Object> getDetailView(String id);

    String create(Map<String, Object> payload);

    void update(String id, Map<String, Object> payload);

    void updateAudit(String id, Map<String, Object> payload);

    void delete(String id);

    Map<String, Object> findLatestDraftByCurrentUser(String expType);
}
