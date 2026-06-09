package com.xuanyue.exp.exp.controller;

import com.xuanyue.exp.common.ApiResponse;
import com.xuanyue.exp.exp.service.ExpSimulatorLogService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/exp/simulators/logs")
public class ExpSimulatorLogController {

    private final ExpSimulatorLogService service;

    public ExpSimulatorLogController(ExpSimulatorLogService service) {
        this.service = service;
    }

    @PostMapping
    public ApiResponse<Void> record(@RequestParam("simulatorId") String simulatorId,
                                    @RequestHeader(value = "X-User-Id", required = false) String userId) {
        service.record(simulatorId, userId);
        return ApiResponse.success(null);
    }
}
