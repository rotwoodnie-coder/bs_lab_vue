package com.xuanyue.exp.edu.controller;

import com.xuanyue.exp.common.ApiResponse;
import com.xuanyue.exp.edu.service.CoursebookContentService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/edu/coursebook-content")
public class CoursebookContentController {

    private final CoursebookContentService service;

    public CoursebookContentController(CoursebookContentService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<?> list(@RequestParam String coursebookId,
                               @RequestHeader(value = "X-User-Id", required = false) String currentUserId) {
        return ApiResponse.success(service.listByCoursebookWithTeachCount(coursebookId, currentUserId));
    }

    @PostMapping
    public ApiResponse<Void> create(@RequestBody Map<String, Object> payload) {
        service.create(payload);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable String id, @RequestBody Map<String, Object> payload) {
        service.update(id, payload);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/{coursebookId}/reorder")
    public ApiResponse<Void> reorder(@PathVariable String coursebookId, @RequestBody List<Map<String, Object>> tree) {
        service.reorder(coursebookId, tree);
        return ApiResponse.success(null);
    }
}
