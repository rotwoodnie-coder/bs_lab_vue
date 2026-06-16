package com.xuanyue.exp.mobile.controller;

import com.xuanyue.exp.common.ApiResponse;
import com.xuanyue.exp.common.PageResult;
import com.xuanyue.exp.mobile.dto.CreateWorkRequest;
import com.xuanyue.exp.mobile.dto.MobileWorkDetailDto;
import com.xuanyue.exp.mobile.dto.MobileWorkItemDto;
import com.xuanyue.exp.mobile.service.MobileWorkService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mobile/works")
public class MobileWorkController {

    private final MobileWorkService workService;

    public MobileWorkController(MobileWorkService workService) {
        this.workService = workService;
    }

    @GetMapping
    public ApiResponse<PageResult<MobileWorkItemDto>> list(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestParam(value = "scope", required = false) String scope,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "reviewStatus", required = false) String reviewStatus,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        try {
            if ("mine".equalsIgnoreCase(scope)) {
                return ApiResponse.success(workService.listMine(userId, type, reviewStatus, page, size));
            }
            return ApiResponse.success(workService.listPublic(type, page, size));
        } catch (IllegalArgumentException e) {
            int code = "请先登录".equals(e.getMessage()) ? 401 : 400;
            return ApiResponse.fail(code, e.getMessage());
        }
    }

    @GetMapping("/{workId}")
    public ApiResponse<MobileWorkDetailDto> get(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @PathVariable String workId) {
        MobileWorkDetailDto detail = workService.getDetail(workId, userId);
        if (detail == null) {
            return ApiResponse.fail(404, "作品不存在");
        }
        return ApiResponse.success(detail);
    }

    @PostMapping
    public ApiResponse<MobileWorkDetailDto> create(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestBody CreateWorkRequest request) {
        try {
            return ApiResponse.success(workService.createWork(userId, request));
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(400, e.getMessage());
        } catch (DataIntegrityViolationException e) {
            return ApiResponse.fail(400, "提交失败：数据约束冲突，请稍后重试或联系管理员");
        }
    }
}
