package com.xuanyue.exp.exp.service;

import java.util.List;
import java.util.Map;

public interface ExpMaterialService {
    List<?> listByExpId(String expId);
    void saveBatch(String expId, List<Map<String, Object>> materials);
    String saveOne(String expId, Map<String, Object> material);
    void updateOne(String expMaterialId, Map<String, Object> material);
    List<?> listPicsByExpMaterialId(String expMaterialId);
    void delete(String expMaterialId);
}
