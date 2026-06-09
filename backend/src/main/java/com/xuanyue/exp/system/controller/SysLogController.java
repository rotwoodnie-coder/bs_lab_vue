package com.xuanyue.exp.system.controller;

import com.xuanyue.exp.common.ApiResponse;
import com.xuanyue.exp.system.entity.SysLog;
import com.xuanyue.exp.system.service.SysLogService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sys/logs")
public class SysLogController {

    private final SysLogService service;

    public SysLogController(SysLogService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<?> page(@RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                               @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
                               @RequestParam(value = "startTime", required = false) String startTime,
                               @RequestParam(value = "endTime", required = false) String endTime,
                               @RequestParam(value = "logType", required = false) String logType) {
        return ApiResponse.success(service.page(pageNum, pageSize, startTime, endTime, logType));
    }

    @GetMapping("/{logId}")
    public ApiResponse<?> get(@PathVariable String logId) {
        return ApiResponse.success(service.get(logId));
    }

    @DeleteMapping("/{logId}")
    public ApiResponse<Void> delete(@PathVariable String logId) {
        service.delete(logId);
        return ApiResponse.success(null);
    }

    public ApiResponse<Void> saveLoginLog(@RequestParam String userId) {
        SysLog log = new SysLog();
        log.setUserId(userId);
        log.setLogType("登录");
        log.setLogDataType("Login");
        log.setLogDataContent("用户登录成功");
        service.save(log);
        return ApiResponse.success(null);
    }
}
