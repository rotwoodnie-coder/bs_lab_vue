package com.xuanyue.exp.data.controller;

import com.xuanyue.exp.common.ApiResponse;
import com.xuanyue.exp.data.service.DataFileService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/data/files")
public class DataFileController {

    private final DataFileService dataFileService;

    public DataFileController(DataFileService dataFileService) {
        this.dataFileService = dataFileService;
    }

    @GetMapping
    public ApiResponse<?> list(@org.springframework.web.bind.annotation.RequestParam(value = "keyword", required = false) String keyword,
                                @org.springframework.web.bind.annotation.RequestParam(value = "status", required = false) String status,
                                @org.springframework.web.bind.annotation.RequestParam(value = "isPublic", required = false) String isPublic,
                                @org.springframework.web.bind.annotation.RequestParam(value = "fileTypeId", required = false) String fileTypeId,
                                @org.springframework.web.bind.annotation.RequestHeader(value = "X-User-Id", required = false) String currentUserId,
                                @org.springframework.web.bind.annotation.RequestParam(value = "publicMode", required = false, defaultValue = "false") boolean publicMode,
                                @org.springframework.web.bind.annotation.RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                @org.springframework.web.bind.annotation.RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {
        return ApiResponse.success(dataFileService.list(keyword, status, isPublic, fileTypeId, currentUserId, publicMode, pageNum, pageSize));
    }

    @GetMapping("/my")
    public ApiResponse<?> listMine(@org.springframework.web.bind.annotation.RequestParam(value = "keyword", required = false) String keyword,
                                   @org.springframework.web.bind.annotation.RequestParam(value = "status", required = false) String status,
                                   @org.springframework.web.bind.annotation.RequestParam(value = "isPublic", required = false) String isPublic,
                                   @org.springframework.web.bind.annotation.RequestParam(value = "fileTypeId", required = false) String fileTypeId,
                                   @org.springframework.web.bind.annotation.RequestHeader(value = "X-User-Id", required = false) String currentUserId,
                                   @org.springframework.web.bind.annotation.RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                   @org.springframework.web.bind.annotation.RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {
        return ApiResponse.success(dataFileService.listAll(keyword, status, isPublic, fileTypeId, currentUserId, pageNum, pageSize));
    }

    @GetMapping("/public")
    public ApiResponse<?> listPublic(@org.springframework.web.bind.annotation.RequestParam(value = "keyword", required = false) String keyword,
                                   @org.springframework.web.bind.annotation.RequestParam(value = "status", required = false) String status,
                                   @org.springframework.web.bind.annotation.RequestParam(value = "isPublic", required = false) String isPublic,
                                   @org.springframework.web.bind.annotation.RequestParam(value = "fileTypeId", required = false) String fileTypeId,
                                   @org.springframework.web.bind.annotation.RequestHeader(value = "X-User-Id", required = false) String currentUserId,
                                   @org.springframework.web.bind.annotation.RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                   @org.springframework.web.bind.annotation.RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {
        return ApiResponse.success(dataFileService.listAll(keyword, "y","y",fileTypeId, null,pageNum, pageSize));
    }

    @GetMapping("/forPublic")
    public ApiResponse<?> listForPublic(@org.springframework.web.bind.annotation.RequestParam(value = "keyword", required = false) String keyword,
                                   @org.springframework.web.bind.annotation.RequestParam(value = "status", required = false) String status,
                                   @org.springframework.web.bind.annotation.RequestParam(value = "isPublic", required = false) String isPublic,
                                   @org.springframework.web.bind.annotation.RequestParam(value = "fileTypeId", required = false) String fileTypeId,
                                   @org.springframework.web.bind.annotation.RequestHeader(value = "X-User-Id", required = false) String currentUserId,
                                   @org.springframework.web.bind.annotation.RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                   @org.springframework.web.bind.annotation.RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {
        return ApiResponse.success(dataFileService.listAll(keyword, "y",isPublic,fileTypeId, null,pageNum, pageSize));
    }

    @GetMapping("/videos")
    public ApiResponse<?> listVideos(@org.springframework.web.bind.annotation.RequestParam(value = "keyword", required = false) String keyword,
                                     @org.springframework.web.bind.annotation.RequestParam(value = "fileTypeId", required = false) String fileTypeId,
                                     @org.springframework.web.bind.annotation.RequestHeader(value = "X-User-Id", required = false) String currentUserId,
                                     @org.springframework.web.bind.annotation.RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                     @org.springframework.web.bind.annotation.RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {
        return ApiResponse.success(dataFileService.listVideos(keyword, fileTypeId, currentUserId, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResponse<?> get(@PathVariable String id) {
        return ApiResponse.success(dataFileService.get(id));
    }

    @PostMapping
    public ApiResponse<Void> create(@RequestBody Map<String, Object> payload,
                                    @RequestHeader(value = "X-User-Id", required = false) String currentUserId) {
        dataFileService.create(payload, currentUserId);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable String id,
                                    @RequestBody Map<String, Object> payload,
                                    @RequestHeader(value = "X-User-Id", required = false) String currentUserId) {
        dataFileService.update(id, payload, currentUserId);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/public")
    public ApiResponse<Void> updatePublic(@PathVariable String id,
                                    @RequestBody Map<String, Object> payload,
                                    @RequestHeader(value = "X-User-Id", required = false) String currentUserId) {
        dataFileService.updatePublic(id, payload, currentUserId);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        dataFileService.delete(id);
        return ApiResponse.success(null);
    }
}
