package com.xuanyue.exp.exp.controller;

import com.xuanyue.exp.common.ApiResponse;
import com.xuanyue.exp.exp.service.ExpLogService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/exp/logs")
public class ExpLogController {

    private final ExpLogService expLogService;

    public ExpLogController(ExpLogService expLogService) {
        this.expLogService = expLogService;
    }

    @GetMapping
    public ApiResponse<?> list(@RequestParam(value = "keyword", required = false) String keyword,
                               @RequestParam(value = "expId", required = false) String expId,
                               @RequestParam(value = "logType", required = false) String logType,
                               @RequestParam(value = "logUserId", required = false) String logUserId,
                               @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                               @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {
        return ApiResponse.success(expLogService.list(keyword, expId, logType, logUserId, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResponse<?> get(@PathVariable String id) {
        return ApiResponse.success(expLogService.get(id));
    }

    @PostMapping
    public ApiResponse<Void> create(@RequestBody Map<String, Object> payload,
                                    @RequestHeader(value = "X-User-Id", required = false) String currentUserId) {
        expLogService.create(payload, currentUserId);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable String id,
                                    @RequestBody Map<String, Object> payload,
                                    @RequestHeader(value = "X-User-Id", required = false) String currentUserId) {
        expLogService.update(id, payload, currentUserId);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        expLogService.delete(id);
        return ApiResponse.success(null);
    }
}
