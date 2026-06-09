package com.xuanyue.exp.mobile.controller;

import com.xuanyue.exp.common.ApiResponse;
import com.xuanyue.exp.mobile.dto.MobileTaskDto;
import com.xuanyue.exp.mobile.service.MobileCreativeService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mobile/creative")
public class MobileCreativeController {

    private final MobileCreativeService creativeService;

    public MobileCreativeController(MobileCreativeService creativeService) {
        this.creativeService = creativeService;
    }

    @PostMapping("/start")
    public ApiResponse<MobileTaskDto> start(
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        MobileCreativeService.StartResult result = creativeService.start(userId);
        if (result.isInProgress()) {
            return new ApiResponse<>(409, "已有进行中的创意实验任务", result.getTask());
        }
        return ApiResponse.success(result.getTask());
    }
}
