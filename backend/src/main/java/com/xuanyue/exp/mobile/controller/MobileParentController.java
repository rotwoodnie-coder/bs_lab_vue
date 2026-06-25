package com.xuanyue.exp.mobile.controller;

import com.xuanyue.exp.common.ApiResponse;
import com.xuanyue.exp.mobile.dto.*;
import com.xuanyue.exp.mobile.service.MobileParentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mobile/parent")
public class MobileParentController {

    private final MobileParentService parentService;

    public MobileParentController(MobileParentService parentService) {
        this.parentService = parentService;
    }

    @PostMapping("/bind")
    public ApiResponse<ParentBindResultDto> bindChild(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestBody ParentBindRequest request) {
        try {
            return ApiResponse.success(parentService.bindChild(userId, request));
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    @GetMapping("/bind-pending")
    public ApiResponse<List<ParentBindResultDto>> listPendingBinds(
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        return ApiResponse.success(parentService.listPendingBinds(userId));
    }

    @GetMapping("/bind-applications")
    public ApiResponse<ParentBindApplicationsDto> bindApplications(
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        return ApiResponse.success(parentService.listBindApplications(userId));
    }

    @GetMapping("/children")
    public ApiResponse<List<ParentChildListItemDto>> listChildren(
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        return ApiResponse.success(parentService.listChildren(userId));
    }

    @PutMapping("/default-child")
    public ApiResponse<Void> setDefaultChild(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestBody ParentDefaultChildRequest request) {
        return doSetDefaultChild(userId, request);
    }

    @PostMapping("/default-child/save")
    public ApiResponse<Void> setDefaultChildViaPost(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestBody ParentDefaultChildRequest request) {
        return doSetDefaultChild(userId, request);
    }

    private ApiResponse<Void> doSetDefaultChild(String userId, ParentDefaultChildRequest request) {
        try {
            if (request == null || request.getChildUserId() == null) {
                return ApiResponse.fail(400, "请选择孩子");
            }
            parentService.setDefaultChild(userId, request.getChildUserId());
            return ApiResponse.success(null);
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }
}
