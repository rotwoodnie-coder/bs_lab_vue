package com.xuanyue.exp.mobile.admin.controller;

import com.xuanyue.exp.common.ApiResponse;
import com.xuanyue.exp.common.PageResult;
import com.xuanyue.exp.mobile.admin.dto.ParentRegistrationAuditRequest;
import com.xuanyue.exp.mobile.admin.dto.ParentRegistrationListItem;
import com.xuanyue.exp.mobile.admin.service.MobileParentRegistrationAdminService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mobile/admin/parent-registrations")
public class MobileParentRegistrationAdminController {

    private final MobileParentRegistrationAdminService service;

    public MobileParentRegistrationAdminController(MobileParentRegistrationAdminService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<PageResult<ParentRegistrationListItem>> page(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        return ApiResponse.success(service.page(keyword, status, pageNum, pageSize));
    }

    @PatchMapping("/{userId}")
    public ApiResponse<Void> audit(@PathVariable String userId,
                                   @RequestBody ParentRegistrationAuditRequest request) {
        service.audit(userId, request);
        return ApiResponse.success(null);
    }
}
