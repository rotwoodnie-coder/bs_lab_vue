package com.xuanyue.exp.mobile.controller;

import com.xuanyue.exp.common.ApiResponse;
import com.xuanyue.exp.common.PageResult;
import com.xuanyue.exp.mobile.dto.MobilePointsLedgerItemDto;
import com.xuanyue.exp.mobile.service.MobilePointsService;
import org.springframework.web.bind.annotation.*;

/**
 * 移动端积分明细（流水）API
 */
@RestController
@RequestMapping("/api/mobile/points")
public class MobilePointsController {

    private final MobilePointsService pointsService;

    public MobilePointsController(MobilePointsService pointsService) {
        this.pointsService = pointsService;
    }

    @GetMapping("/ledger")
    public ApiResponse<PageResult<MobilePointsLedgerItemDto>> ledger(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        return ApiResponse.success(pointsService.listLedger(userId, page, size));
    }
}
