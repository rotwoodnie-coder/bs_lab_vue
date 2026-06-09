package com.xuanyue.exp.data.service;

import com.xuanyue.exp.common.PageResult;
import com.xuanyue.exp.data.entity.MaterialMsg;

import java.util.List;
import java.util.Map;

public interface MaterialMsgService {

    PageResult<?> list(String keyword, String status, String isPublic, boolean publicMode, String currentUserId, int pageNum, int pageSize);
    
    PageResult<?> listAll(String keyword, String status, String isPublic, String currentUserId, int pageNum, int pageSize);

    Object get(String id);

    MaterialMsg create(Map<String, Object> payload, String currentUserId);

    void update(String id, Map<String, Object> payload, String currentUserId);

    void updatePublic(String id, Map<String, Object> payload, String currentUserId);

    List<Map<String, Object>> listPics(String materialId);

    void delete(String id);
}
