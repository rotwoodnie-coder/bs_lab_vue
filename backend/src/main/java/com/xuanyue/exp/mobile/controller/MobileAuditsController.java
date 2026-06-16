package com.xuanyue.exp.mobile.controller;

import com.xuanyue.exp.common.ApiResponse;
import com.xuanyue.exp.mobile.dto.MobileAuditSummaryDto;
import com.xuanyue.exp.mobile.service.MobileAuditsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mobile/audits")
public class MobileAuditsController {

    private final MobileAuditsService auditsService;

    public MobileAuditsController(MobileAuditsService auditsService) {
        this.auditsService = auditsService;
    }

    @GetMapping("/summary")
    public ApiResponse<MobileAuditSummaryDto> summary(
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            return ApiResponse.success(auditsService.getSummary(userId));
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }
}
