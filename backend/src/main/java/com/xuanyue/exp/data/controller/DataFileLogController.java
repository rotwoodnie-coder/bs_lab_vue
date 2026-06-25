package com.xuanyue.exp.data.controller;

import com.xuanyue.exp.common.ApiResponse;
import com.xuanyue.exp.data.service.DataFileLogService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/data/file-logs")
public class DataFileLogController {

    private final DataFileLogService service;

    public DataFileLogController(DataFileLogService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<?> list(@RequestParam(value = "keyword", required = false) String keyword,
                               @RequestParam(value = "fileId", required = false) String fileId,
                               @RequestParam(value = "logType", required = false) String logType,
                               @RequestParam(value = "logUserId", required = false) String logUserId,
                               @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                               @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        return ApiResponse.success(service.list(keyword, fileId, logType, logUserId, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResponse<?> get(@PathVariable String id) {
        return ApiResponse.success(service.get(id));
    }

    @PostMapping
    public ApiResponse<Void> create(@RequestBody Map<String, Object> payload,
                                    @RequestHeader(value = "X-User-Id", required = false) String currentUserId) {
        service.create(payload, currentUserId);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable String id,
                                    @RequestBody Map<String, Object> payload,
                                    @RequestHeader(value = "X-User-Id", required = false) String currentUserId) {
        service.update(id, payload, currentUserId);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ApiResponse.success(null);
    }
}
