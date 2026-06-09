package com.xuanyue.exp.exp.service;

import java.util.List;
import java.util.Map;

public interface ExpStepService {
    List<?> listByExpId(String expId);
    List<?> saveBatch(String expId, List<Map<String, Object>> steps);
    String saveOne(String expId, Map<String, Object> step);
    void delete(String stepId);
}
