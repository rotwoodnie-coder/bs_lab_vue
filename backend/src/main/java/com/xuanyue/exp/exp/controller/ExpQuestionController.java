package com.xuanyue.exp.exp.controller;

import com.xuanyue.exp.common.ApiResponse;
import com.xuanyue.exp.exp.service.ExpQuestionService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/exp/questions")
public class ExpQuestionController {

    private final ExpQuestionService service;

    public ExpQuestionController(ExpQuestionService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<?> list(@RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                               @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
                               @RequestParam(value = "keyword", required = false) String keyword,
                               @RequestParam(value = "status", required = false) String status,
                               @RequestParam(value = "questionTypeId", required = false) String questionTypeId,
                               @RequestParam(value = "gradeId", required = false) String gradeId,
                               @RequestParam(value = "subjectId", required = false) String subjectId,
                               @RequestParam(value = "paged", defaultValue = "true") boolean paged) {
        if (paged) {
            Map<String, Object> result = new HashMap<String, Object>();
            result.put("records", service.page(pageNum, pageSize, keyword, status, questionTypeId, gradeId, subjectId));
            result.put("total", service.count(keyword, status, questionTypeId, gradeId, subjectId));
            result.put("pageNum", pageNum);
            result.put("pageSize", pageSize);
            return ApiResponse.success(result);
        }
        return ApiResponse.success(service.list(keyword, status, questionTypeId, gradeId, subjectId));
    }

    @GetMapping("/{id}")
    public ApiResponse<?> get(@PathVariable String id) {
        return ApiResponse.success(service.get(id));
    }

    @PostMapping
    public ApiResponse<?> create(@RequestBody Map<String, Object> payload) {
        String questionId = service.create(payload);
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("questionId", questionId);
        return ApiResponse.success(result);
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable String id, @RequestBody Map<String, Object> payload) {
        service.update(id, payload);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ApiResponse.success(null);
    }
}
