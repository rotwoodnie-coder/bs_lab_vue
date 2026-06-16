package com.xuanyue.exp.mobile.controller;

import com.xuanyue.exp.common.ApiResponse;
import com.xuanyue.exp.common.PageResult;
import com.xuanyue.exp.mobile.dto.ResearcherExpAuditItemDto;
import com.xuanyue.exp.mobile.dto.ResearcherExpAuditRequest;
import com.xuanyue.exp.mobile.service.MobileResearcherExpAuditService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/mobile/researcher")
public class MobileResearcherController {

    private final MobileResearcherExpAuditService expAuditService;

    public MobileResearcherController(MobileResearcherExpAuditService expAuditService) {
        this.expAuditService = expAuditService;
    }

    @GetMapping("/exp-audits")
    public ApiResponse<PageResult<ResearcherExpAuditItemDto>> listExpAudits(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestParam(value = "expType", required = false) String expType,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        try {
            return ApiResponse.success(expAuditService.listPending(userId, expType, page, size));
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    @GetMapping("/exp-audits/pending-count")
    public ApiResponse<Integer> pendingCount(
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            return ApiResponse.success(expAuditService.countPending(userId));
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    @GetMapping("/exp-audits/{expId}")
    public ApiResponse<Map<String, Object>> expAuditDetail(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @PathVariable String expId) {
        try {
            return ApiResponse.success(expAuditService.getDetail(userId, expId));
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    @PatchMapping("/exp-audits/{expId}")
    public ApiResponse<Void> auditExp(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @PathVariable String expId,
            @RequestBody ResearcherExpAuditRequest request) {
        try {
            expAuditService.audit(userId, expId, request);
            return ApiResponse.success(null);
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }
}
