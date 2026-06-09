package com.xuanyue.exp.edu.controller;

import com.xuanyue.exp.common.ApiResponse;
import com.xuanyue.exp.edu.service.SubjectGroupService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/edu/subject-groups")
public class SubjectGroupController {

    private final SubjectGroupService service;

    public SubjectGroupController(SubjectGroupService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<?> page(@RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                               @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
                               @RequestParam(value = "keyword", required = false) String keyword,
                               @RequestHeader(value = "X-User-Root-Org-Id", required = false) String currentRootOrgId) {
        return ApiResponse.success(service.page(pageNum, pageSize, keyword, currentRootOrgId));
    }

    @GetMapping("/{groupId}")
    public ApiResponse<?> get(@PathVariable String groupId) {
        return ApiResponse.success(service.get(groupId));
    }

    @PostMapping
    public ApiResponse<Void> create(@RequestBody Map<String, Object> payload,
                                    @RequestHeader(value = "X-User-Id", required = false) String currentUserId,
                                    @RequestHeader(value = "X-User-Root-Org-Id", required = false) String currentRootOrgId) {
        service.create(payload, currentUserId, currentRootOrgId);
        return ApiResponse.success(null);
    }

    @PutMapping("/{groupId}")
    public ApiResponse<Void> update(@PathVariable String groupId,
                                    @RequestBody Map<String, Object> payload,
                                    @RequestHeader(value = "X-User-Id", required = false) String currentUserId,
                                    @RequestHeader(value = "X-User-Root-Org-Id", required = false) String currentRootOrgId) {
        service.update(groupId, payload, currentUserId, currentRootOrgId);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{groupId}")
    public ApiResponse<Void> delete(@PathVariable String groupId,
                                    @RequestHeader(value = "X-User-Root-Org-Id", required = false) String currentRootOrgId) {
        service.delete(groupId, currentRootOrgId);
        return ApiResponse.success(null);
    }
}
