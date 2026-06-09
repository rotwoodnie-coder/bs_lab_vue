package com.xuanyue.exp.exp.service;

import java.util.List;
import java.util.Map;

public interface ExpReferenceService {
    List<?> listByExpId(String expId);
    List<?> saveBatch(String expId, List<Map<String, Object>> references);
    Object saveOne(String expId, Map<String, Object> reference);
    void delete(String referenceId);
}
