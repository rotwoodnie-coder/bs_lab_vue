package com.xuanyue.exp.data.controller;

import com.xuanyue.exp.common.ApiResponse;
import com.xuanyue.exp.data.service.MaterialMsgService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.xuanyue.exp.data.entity.MaterialMsg;

import java.util.Map;

@RestController
@RequestMapping("/api/data/material-msgs")
public class MaterialMsgController {

    private final MaterialMsgService materialMsgService;

    public MaterialMsgController(MaterialMsgService materialMsgService) {
        this.materialMsgService = materialMsgService;
    }

    @GetMapping
    public ApiResponse<?> list(@RequestParam(value = "keyword", required = false) String keyword,
                               @RequestParam(value = "status", required = false) String status,
                               @RequestParam(value = "isPublic", required = false) String isPublic,
                               @RequestParam(value = "publicMode", required = false, defaultValue = "false") boolean publicMode,
                               @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                               @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
                               @RequestHeader(value = "X-User-Id", required = false) String currentUserId) {
        return ApiResponse.success(materialMsgService.list(keyword, status, isPublic, publicMode, currentUserId, pageNum, pageSize));
    }

    @GetMapping("/my")
    public ApiResponse<?> listMy(@RequestParam(value = "keyword", required = false) String keyword,
                               @RequestParam(value = "status", required = false) String status,
                               @RequestParam(value = "isPublic", required = false) String isPublic,
                               @RequestParam(value = "publicMode", required = false, defaultValue = "false") boolean publicMode,
                               @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                               @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
                               @RequestHeader(value = "X-User-Id", required = false) String currentUserId) {
        return ApiResponse.success(materialMsgService.listAll(keyword, status, isPublic, currentUserId, pageNum, pageSize));
    }

     @GetMapping("/public")
    public ApiResponse<?> listPublic(@RequestParam(value = "keyword", required = false) String keyword,
                               @RequestParam(value = "status", required = false) String status,
                               @RequestParam(value = "isPublic", required = false) String isPublic,
                               @RequestParam(value = "publicMode", required = false, defaultValue = "false") boolean publicMode,
                               @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                               @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
                               @RequestHeader(value = "X-User-Id", required = false) String currentUserId) {
        return ApiResponse.success(materialMsgService.listAll(keyword, "y",  "y", null, pageNum, pageSize));
    }

    @GetMapping("/forPublic")
    public ApiResponse<?> listForPublic(@RequestParam(value = "keyword", required = false) String keyword,
                               @RequestParam(value = "status", required = false) String status,
                               @RequestParam(value = "isPublic", required = false) String isPublic,
                               @RequestParam(value = "publicMode", required = false, defaultValue = "false") boolean publicMode,
                               @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                               @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
                               @RequestHeader(value = "X-User-Id", required = false) String currentUserId) {
        return ApiResponse.success(materialMsgService.listAll(keyword, "y", isPublic, null, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResponse<?> get(@PathVariable String id) {
        return ApiResponse.success(materialMsgService.get(id));
    }

    @PostMapping
    public ApiResponse<?> create(@RequestBody Map<String, Object> payload,
                                    @RequestHeader(value = "X-User-Id", required = false) String currentUserId) {
        MaterialMsg msgMaterial = materialMsgService.create(payload, currentUserId);
        return ApiResponse.success(msgMaterial);
    }

    @PostMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable String id,
                                    @RequestBody Map<String, Object> payload,
                                    @RequestHeader(value = "X-User-Id", required = false) String currentUserId) {
        materialMsgService.update(id, payload, currentUserId);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/public")
    public ApiResponse<Void> updatePublic(@PathVariable String id,
                                    @RequestBody Map<String, Object> payload,
                                    @RequestHeader(value = "X-User-Id", required = false) String currentUserId) {
        materialMsgService.updatePublic(id, payload, currentUserId);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        materialMsgService.delete(id);
        return ApiResponse.success(null);
    }
}
