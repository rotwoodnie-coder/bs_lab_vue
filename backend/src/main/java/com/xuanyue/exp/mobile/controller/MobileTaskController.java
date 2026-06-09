package com.xuanyue.exp.mobile.controller;

import com.xuanyue.exp.common.ApiResponse;
import com.xuanyue.exp.common.PageResult;
import com.xuanyue.exp.mobile.dto.CreateTaskRequest;
import com.xuanyue.exp.mobile.dto.MobileTaskListItemDto;
import com.xuanyue.exp.mobile.dto.MobileTaskDto;
import com.xuanyue.exp.mobile.service.MobileTaskService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mobile/tasks")
public class MobileTaskController {

    private final MobileTaskService taskService;

    public MobileTaskController(MobileTaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public ApiResponse<PageResult<MobileTaskListItemDto>> list(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestParam(value = "childUserId", required = false) String childUserId,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        String resolved = StringUtils.hasText(category) ? category : type;
        try {
            return ApiResponse.success(taskService.listItems(userId, childUserId, resolved, status, page, size));
        } catch (IllegalArgumentException e) {
            int code = "请先登录".equals(e.getMessage()) ? 401 : 400;
            return ApiResponse.fail(code, e.getMessage());
        }
    }

    @GetMapping("/{taskId}")
    public ApiResponse<MobileTaskDto> get(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @PathVariable String taskId) {
        MobileTaskDto task = taskService.get(userId, taskId);
        if (task == null) {
            return ApiResponse.fail(404, "任务不存在");
        }
        return ApiResponse.success(task);
    }

    @PostMapping
    public ApiResponse<MobileTaskDto> create(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestBody CreateTaskRequest request) {
        try {
            return ApiResponse.success(taskService.createTask(userId, request));
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }
}
