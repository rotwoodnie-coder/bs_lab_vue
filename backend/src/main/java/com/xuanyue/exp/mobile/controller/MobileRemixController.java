package com.xuanyue.exp.mobile.controller;

import com.xuanyue.exp.common.ApiResponse;
import com.xuanyue.exp.mobile.dto.MobileTaskDto;
import com.xuanyue.exp.mobile.dto.RemixStartRequest;
import com.xuanyue.exp.mobile.service.MobileRemixService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mobile/remix")
public class MobileRemixController {

    private final MobileRemixService remixService;

    public MobileRemixController(MobileRemixService remixService) {
        this.remixService = remixService;
    }

    @PostMapping("/start")
    public ApiResponse<MobileTaskDto> start(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestBody RemixStartRequest request) {
        try {
            String expId = request != null ? request.getExpId() : null;
            String workId = request != null ? request.getWorkId() : null;
            MobileRemixService.StartResult result = remixService.start(userId, expId, workId);
            if (result.isInProgress()) {
                return new ApiResponse<>(409, "该实验已有进行中的拍同款任务", result.getTask());
            }
            return ApiResponse.success(result.getTask());
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }
}
