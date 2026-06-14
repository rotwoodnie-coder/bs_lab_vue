package com.xuanyue.exp.edu.controller;

import com.xuanyue.exp.common.ApiResponse;
import com.xuanyue.exp.edu.service.TeacherClassService;
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
@RequestMapping("/api/edu/teacher-classes")
public class TeacherClassController {

    private final TeacherClassService teacherClassService;

    public TeacherClassController(TeacherClassService teacherClassService) {
        this.teacherClassService = teacherClassService;
    }

    @GetMapping
    public ApiResponse<?> page(@RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                               @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
                               @RequestParam(value = "keyword", required = false) String keyword,
                               @RequestHeader(value = "X-User-Root-Org-Id", required = false) String currentRootOrgId) {
        return ApiResponse.success(teacherClassService.page(pageNum, pageSize, keyword, currentRootOrgId));
    }

    @GetMapping("/{teacherId}")
    public ApiResponse<?> get(@PathVariable String teacherId) {
        return ApiResponse.success(teacherClassService.get(teacherId));
    }

    @PostMapping("/{teacherId}")
    public ApiResponse<Void> save(@PathVariable String teacherId,
                                  @RequestBody Map<String, Object> payload,
                                  @RequestHeader(value = "X-User-Id", required = false) String currentUserId,
                                  @RequestHeader(value = "X-User-Root-Org-Id", required = false) String currentRootOrgId) {
        teacherClassService.save(teacherId, payload, currentUserId, currentRootOrgId);
        return ApiResponse.success(null);
    }

    @PutMapping("/{teacherId}")
    public ApiResponse<Void> update(@PathVariable String teacherId,
                                    @RequestBody Map<String, Object> payload,
                                    @RequestHeader(value = "X-User-Id", required = false) String currentUserId,
                                    @RequestHeader(value = "X-User-Root-Org-Id", required = false) String currentRootOrgId) {
        teacherClassService.save(teacherId, payload, currentUserId, currentRootOrgId);
        return ApiResponse.success(null);
    }
}
