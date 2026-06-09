package com.xuanyue.exp.exp.service;

import java.util.List;
import java.util.Map;

public interface ExpResultService {
    List<?> listByExpId(String expId);
    List<?> saveBatch(String expId, List<Map<String, Object>> results);
    String saveOne(String expId, Map<String, Object> result);
    void delete(String resultId);
}
