package com.xuanyue.exp.data.service;

import java.util.List;
import java.util.Map;

public interface DataDictService {

    List<?> list(String type);

    Object get(String type, String id);

    void create(String type, Map<String, Object> payload);

    void update(String type, String id, Map<String, Object> payload);

    void delete(String type, String id);

    List<Map<String, Object>> page(String type, int pageNum, int pageSize, String keyword);

    long count(String type, String keyword);
}
