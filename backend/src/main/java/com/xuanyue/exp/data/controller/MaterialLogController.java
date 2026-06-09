package com.xuanyue.exp.data.controller;

import com.xuanyue.exp.common.ApiResponse;
import com.xuanyue.exp.data.service.MaterialLogService;
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
@RequestMapping("/api/data/material-logs")
public class MaterialLogController {

    private final MaterialLogService materialLogService;

    public MaterialLogController(MaterialLogService materialLogService) {
        this.materialLogService = materialLogService;
    }

    @GetMapping
    public ApiResponse<?> list(@RequestParam(value = "keyword", required = false) String keyword,
                               @RequestParam(value = "materialId", required = false) String materialId,
                               @RequestParam(value = "logType", required = false) String logType,
                               @RequestParam(value = "logUserId", required = false) String logUserId,
                               @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                               @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {
        return ApiResponse.success(materialLogService.list(keyword, materialId, logType, logUserId, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResponse<?> get(@PathVariable String id) {
        return ApiResponse.success(materialLogService.get(id));
    }

    @PostMapping
    public ApiResponse<Void> create(@RequestBody Map<String, Object> payload,
                                    @RequestHeader(value = "X-User-Id", required = false) String currentUserId) {
        materialLogService.create(payload, currentUserId);
        return ApiResponse.success(null);
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable String id,
                                    @RequestBody Map<String, Object> payload,
                                    @RequestHeader(value = "X-User-Id", required = false) String currentUserId) {
        materialLogService.update(id, payload, currentUserId);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        materialLogService.delete(id);
        return ApiResponse.success(null);
    }
}
