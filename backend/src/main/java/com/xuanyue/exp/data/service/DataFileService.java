package com.xuanyue.exp.data.service;

import com.xuanyue.exp.common.PageResult;

import java.util.Map;

public interface DataFileService {

    PageResult<?> list(String keyword, String status, String isPublic, String fileTypeId, String currentUserId, boolean publicMode, int pageNum, int pageSize);
    
    PageResult<?> listAll(String keyword, String status, String isPublic, String fileTypeId, String ownerUserId, int pageNum, int pageSize);
 
    PageResult<?> listVideos(String keyword, String fileTypeId, String currentUserId, int pageNum, int pageSize);

    Object get(String id);

    void create(Map<String, Object> payload, String currentUserId);

    void update(String id, Map<String, Object> payload, String currentUserId);

    void updatePublic(String id, Map<String, Object> payload, String currentUserId);

    void delete(String id);
}
