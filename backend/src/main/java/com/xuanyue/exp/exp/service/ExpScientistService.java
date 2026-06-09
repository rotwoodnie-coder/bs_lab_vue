package com.xuanyue.exp.exp.service;

import java.util.List;
import java.util.Map;

public interface ExpScientistService {
    List<?> listByExpId(String expId);
    List<?> saveBatch(String expId, List<Map<String, Object>> scientists);
    Object saveOne(String expId, Map<String, Object> scientist);
    void delete(String scientistId);
}
