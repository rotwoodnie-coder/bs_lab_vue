package com.xuanyue.exp.homework.controller;

import com.xuanyue.exp.common.ApiResponse;
import com.xuanyue.exp.common.PageResult;
import com.xuanyue.exp.homework.entity.ExpHomework;
import com.xuanyue.exp.homework.service.ExpHomeworkService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/homework")
public class ExpHomeworkController {

    private final ExpHomeworkService service;

    public ExpHomeworkController(ExpHomeworkService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<PageResult<Map<String, Object>>> page(@RequestParam(value = "keyword", required = false) String keyword,
                                                     @RequestParam(value = "teacherExpId", required = false) String teacherExpId,
                                                     @RequestParam(value = "teacherUserId", required = false) String teacherUserId,
                                                     @RequestParam(value = "classId", required = false) String classId,
                                                     @RequestParam(value = "requireDate", required = false) String requireDate,
                                                     @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                                     @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        return ApiResponse.success(service.page(keyword, teacherExpId, teacherUserId, classId, requireDate, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResponse<ExpHomework> get(@PathVariable("id") String id) {
        return ApiResponse.success(service.get(id));
    }

    @PostMapping
    public ApiResponse<Void> create(@RequestBody Map<String, Object> payload) {
        service.create(payload);
        return ApiResponse.success(null);
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable("id") String id, @RequestBody Map<String, Object> payload) {
        service.update(id, payload);
        return ApiResponse.success(null);
    }

    @PutMapping("/{id}/assign")
    public ApiResponse<Void> assign(@PathVariable("id") String id) {
        service.assign(id);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") String id) {
        service.delete(id);
        return ApiResponse.success(null);
    }
}
