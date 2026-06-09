package com.xuanyue.exp.system.service;

import java.util.List;
import java.util.Map;

public interface DataFileTypeService {

    List<?> list();

    Object get(String id);

    void create(Map<String, Object> payload);

    void update(String id, Map<String, Object> payload);

    void delete(String id);
}
