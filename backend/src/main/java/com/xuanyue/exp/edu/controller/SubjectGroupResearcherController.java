package com.xuanyue.exp.edu.controller;

import com.xuanyue.exp.common.ApiResponse;
import com.xuanyue.exp.edu.service.SubjectGroupResearcherService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/edu/subject-groups")
public class SubjectGroupResearcherController {

    private final SubjectGroupResearcherService service;

    public SubjectGroupResearcherController(SubjectGroupResearcherService service) {
        this.service = service;
    }

    @GetMapping("/{groupId}/researchers")
    public ApiResponse<?> get(@PathVariable String groupId) {
        return ApiResponse.success(service.get(groupId));
    }

    @PutMapping("/{groupId}/researchers")
    public ApiResponse<Void> save(@PathVariable String groupId,
                                  @RequestBody Map<String, Object> payload,
                                  @RequestHeader(value = "X-User-Id", required = false) String currentUserId,
                                  @RequestHeader(value = "X-User-Root-Org-Id", required = false) String currentRootOrgId) {
        service.save(groupId, payload, currentUserId, currentRootOrgId);
        return ApiResponse.success(null);
    }
}
