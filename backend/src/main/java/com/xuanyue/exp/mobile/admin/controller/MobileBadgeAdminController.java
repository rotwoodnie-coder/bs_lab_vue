package com.xuanyue.exp.mobile.admin.controller;

import com.xuanyue.exp.common.ApiResponse;
import com.xuanyue.exp.mobile.admin.dto.MobileBadgeDefDto;
import com.xuanyue.exp.mobile.admin.dto.MobileBadgeGrantRequest;
import com.xuanyue.exp.mobile.admin.dto.MobileBadgeProgressItemDto;
import com.xuanyue.exp.mobile.admin.service.MobileBadgeAdminService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/mobile/admin/badges")
public class MobileBadgeAdminController {

    private final MobileBadgeAdminService service;

    public MobileBadgeAdminController(MobileBadgeAdminService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<MobileBadgeDefDto>> listDefinitions() {
        return ApiResponse.success(service.listDefinitions());
    }

    @PostMapping
    public ApiResponse<MobileBadgeDefDto> saveDefinition(@RequestBody MobileBadgeDefDto dto) {
        return ApiResponse.success(service.saveDefinition(dto));
    }

    @DeleteMapping("/{badgeId}")
    public ApiResponse<Void> deleteDefinition(@PathVariable String badgeId) {
        service.deleteDefinition(badgeId);
        return ApiResponse.success(null);
    }

    @GetMapping("/progress")
    public ApiResponse<List<MobileBadgeProgressItemDto>> listProgress(@RequestParam String userId) {
        return ApiResponse.success(service.listProgress(userId));
    }

    @PostMapping("/grant")
    public ApiResponse<Void> grant(@RequestBody MobileBadgeGrantRequest request) {
        service.grant(request);
        return ApiResponse.success(null);
    }
}
