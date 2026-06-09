package com.xuanyue.exp.system.service;

import java.util.List;
import java.util.Map;

public interface DataDictionaryService {

    List<?> list(String type);

    Object get(String type, String id);

    void create(String type, Map<String, Object> payload);

    void update(String type, String id, Map<String, Object> payload);

    void delete(String type, String id);
}
