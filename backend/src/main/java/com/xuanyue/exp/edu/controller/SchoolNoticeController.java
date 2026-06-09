package com.xuanyue.exp.edu.controller;

import com.xuanyue.exp.common.ApiResponse;
import com.xuanyue.exp.edu.service.SchoolNoticeService;
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
@RequestMapping("/api/edu/school-notices")
public class SchoolNoticeController {

    private final SchoolNoticeService schoolNoticeService;

    public SchoolNoticeController(SchoolNoticeService schoolNoticeService) {
        this.schoolNoticeService = schoolNoticeService;
    }

    @GetMapping
    public ApiResponse<?> list(@RequestParam(value = "keyword", required = false) String keyword,
                               @RequestParam(value = "status", required = false) String status,
                               @RequestParam(value = "noticeOrgId", required = false) String noticeOrgId,
                               @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                               @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        return ApiResponse.success(schoolNoticeService.list(keyword, status, noticeOrgId, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResponse<?> get(@PathVariable String id) {
        return ApiResponse.success(schoolNoticeService.get(id));
    }

    @PostMapping
    public ApiResponse<Void> create(@RequestBody Map<String, Object> payload,
                                    @RequestHeader(value = "X-User-Id", required = false) String currentUserId) {
        schoolNoticeService.create(payload, currentUserId);
        return ApiResponse.success(null);
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable String id,
                                    @RequestBody Map<String, Object> payload,
                                    @RequestHeader(value = "X-User-Id", required = false) String currentUserId) {
        schoolNoticeService.update(id, payload, currentUserId);
        return ApiResponse.success(null);
    }

    @PutMapping("/{id}/confirm")
    public ApiResponse<Void> confirm(@PathVariable String id,
                                     @RequestBody Map<String, Object> payload,
                                     @RequestHeader(value = "X-User-Id", required = false) String currentUserId) {
        schoolNoticeService.confirm(id, payload, currentUserId);
        return ApiResponse.success(null);
    }

    @PutMapping("/{id}/publish")
    public ApiResponse<Void> publish(@PathVariable String id,
                                     @RequestBody Map<String, Object> payload,
                                     @RequestHeader(value = "X-User-Id", required = false) String currentUserId) {
        schoolNoticeService.publish(id, payload, currentUserId);
        return ApiResponse.success(null);
    }

    @PutMapping("/{id}/void")
    public ApiResponse<Void> voidNotice(@PathVariable String id,
                                        @RequestBody Map<String, Object> payload,
                                        @RequestHeader(value = "X-User-Id", required = false) String currentUserId) {
        schoolNoticeService.voidNotice(id, payload, currentUserId);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        schoolNoticeService.delete(id);
        return ApiResponse.success(null);
    }
}
