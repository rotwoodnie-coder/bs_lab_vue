package com.xuanyue.exp.edu.controller;

import com.xuanyue.exp.common.ApiResponse;
import com.xuanyue.exp.edu.service.SubjectGroupTeacherService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/edu/subject-groups")
public class SubjectGroupTeacherController {

    private final SubjectGroupTeacherService service;

    public SubjectGroupTeacherController(SubjectGroupTeacherService service) {
        this.service = service;
    }

    @GetMapping("/{groupId}/teachers")
    public ApiResponse<?> get(@PathVariable String groupId) {
        return ApiResponse.success(service.get(groupId));
    }

    @PostMapping("/{groupId}/teachers")
    public ApiResponse<Void> save(@PathVariable String groupId,
                                  @RequestBody Map<String, Object> payload,
                                  @RequestHeader(value = "X-User-Id", required = false) String currentUserId,
                                  @RequestHeader(value = "X-User-Root-Org-Id", required = false) String currentRootOrgId) {
        service.save(groupId, payload, currentUserId, currentRootOrgId);
        return ApiResponse.success(null);
    }

    @GetMapping("/teacher-assign-options")
    public ApiResponse<?> teacherAssignOptions(@org.springframework.web.bind.annotation.RequestParam(value = "subjectId", required = false) String subjectId) {
        return ApiResponse.success(service.teacherAssignOptions(subjectId));
    }
}
