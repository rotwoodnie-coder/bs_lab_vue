package com.xuanyue.exp.homework.controller;

import com.xuanyue.exp.common.ApiResponse;
import com.xuanyue.exp.common.PageResult;
import com.xuanyue.exp.homework.entity.ExpHomeworkStudent;
import com.xuanyue.exp.homework.service.ExpHomeworkStudentService;
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
@RequestMapping("/api/homework-students")
public class ExpHomeworkStudentController {

    private final ExpHomeworkStudentService service;

    public ExpHomeworkStudentController(ExpHomeworkStudentService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<PageResult<ExpHomeworkStudent>> page(@RequestParam(value = "keyword", required = false) String keyword,
                                                            @RequestParam(value = "homeworkId", required = false) String homeworkId,
                                                            @RequestParam(value = "teacherExpId", required = false) String teacherExpId,
                                                            @RequestParam(value = "teacherUserId", required = false) String teacherUserId,
                                                            @RequestParam(value = "studentExpId", required = false) String studentExpId,
                                                            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                                            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        return ApiResponse.success(service.page(keyword, homeworkId, teacherExpId, teacherUserId, studentExpId, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResponse<ExpHomeworkStudent> get(@PathVariable("id") String id) {
        return ApiResponse.success(service.get(id));
    }

    @PostMapping
    public ApiResponse<Void> create(@RequestBody Map<String, Object> payload) {
        service.create(payload);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable("id") String id, @RequestBody Map<String, Object> payload) {
        service.update(id, payload);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") String id) {
        service.delete(id);
        return ApiResponse.success(null);
    }
}
