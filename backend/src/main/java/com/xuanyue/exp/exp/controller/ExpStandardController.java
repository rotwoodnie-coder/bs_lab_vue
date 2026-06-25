package com.xuanyue.exp.exp.controller;

import com.xuanyue.exp.common.ApiResponse;
import com.xuanyue.exp.common.PageResult;
import com.xuanyue.exp.exp.service.ExpStandardService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/exp/standards")
public class ExpStandardController {

    private final ExpStandardService service;

    public ExpStandardController(ExpStandardService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<PageResult<Map<String, Object>>> page(@org.springframework.web.bind.annotation.RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                                             @org.springframework.web.bind.annotation.RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
                                                             @org.springframework.web.bind.annotation.RequestHeader(value = "X-User-Id", required = false) String currentUserId,
                                                             @org.springframework.web.bind.annotation.RequestParam(value = "keyword", required = false) String keyword,
                                                             @org.springframework.web.bind.annotation.RequestParam(value = "status", required = false) String status,
                                                             @org.springframework.web.bind.annotation.RequestParam(value = "notstatus", required = false) String notstatus,
                                                             @org.springframework.web.bind.annotation.RequestParam(value = "subjectId", required = false) String subjectId,
                                                             @org.springframework.web.bind.annotation.RequestParam(value = "schoolLevelId", required = false) String schoolLevelId,
                                                             @org.springframework.web.bind.annotation.RequestParam(value = "gradeId", required = false) String gradeId,
                                                             @org.springframework.web.bind.annotation.RequestParam(value = "chooseType", required = false) String chooseType,
                                                             @org.springframework.web.bind.annotation.RequestParam(value = "expType", required = false) String expType) {
        List<Map<String, Object>> records = service.pageStandard(pageNum, pageSize, keyword, status, subjectId, schoolLevelId, gradeId, chooseType, expType,null,notstatus);
        long total = service.count(keyword, status, subjectId, schoolLevelId, gradeId, chooseType, expType,null,notstatus);
        return ApiResponse.success(new PageResult<Map<String, Object>>(total, records));
    }

    @GetMapping("/my")
    public ApiResponse<PageResult<Map<String, Object>>> pageMy(@org.springframework.web.bind.annotation.RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                                             @org.springframework.web.bind.annotation.RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
                                                             @org.springframework.web.bind.annotation.RequestHeader(value = "X-User-Id", required = false) String currentUserId,
                                                             @org.springframework.web.bind.annotation.RequestParam(value = "keyword", required = false) String keyword,
                                                             @org.springframework.web.bind.annotation.RequestParam(value = "status", required = false) String status,
                                                             @org.springframework.web.bind.annotation.RequestParam(value = "subjectId", required = false) String subjectId,
                                                             @org.springframework.web.bind.annotation.RequestParam(value = "schoolLevelId", required = false) String schoolLevelId,
                                                             @org.springframework.web.bind.annotation.RequestParam(value = "gradeId", required = false) String gradeId,
                                                             @org.springframework.web.bind.annotation.RequestParam(value = "chooseType", required = false) String chooseType,
                                                             @org.springframework.web.bind.annotation.RequestParam(value = "expType", required = false) String expType) {
        List<Map<String, Object>> records = service.pageStandard(pageNum, pageSize, keyword, status, subjectId, schoolLevelId, gradeId, chooseType, expType,currentUserId,null);
        long total = service.count(keyword, status, subjectId, schoolLevelId, gradeId, chooseType, expType,currentUserId,null);
        return ApiResponse.success(new PageResult<Map<String, Object>>(total, records));
    }

    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> get(@PathVariable String id) {
        return ApiResponse.success(service.get(id));
    }

    @GetMapping("/detail/{id}")
    public ApiResponse<Map<String, Object>> getDetail(@PathVariable String id) {
        return ApiResponse.success(service.getDetailView(id));
    }

    @GetMapping("/draft/latest")
    public ApiResponse<Map<String, Object>> latestDraft() {
        return ApiResponse.success(service.findLatestDraftByCurrentUser("standard"));
    }

    @PostMapping
    public ApiResponse<String> create(@RequestBody Map<String, Object> payload) {
        return ApiResponse.success(service.create(payload));
    }

    @PostMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable String id, @RequestBody Map<String, Object> payload) {
        service.update(id, payload);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/audit")
    public ApiResponse<Void> updateAudit(@PathVariable String id, @RequestBody Map<String, Object> payload) {
        service.updateAudit(id, payload);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ApiResponse.success(null);
    }
}
