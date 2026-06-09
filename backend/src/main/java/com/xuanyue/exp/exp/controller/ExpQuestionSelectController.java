package com.xuanyue.exp.exp.controller;

import com.xuanyue.exp.common.ApiResponse;
import com.xuanyue.exp.exp.service.ExpQuestionSelectService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/exp")
public class ExpQuestionSelectController {

    private final ExpQuestionSelectService service;

    public ExpQuestionSelectController(ExpQuestionSelectService service) {
        this.service = service;
    }

    @GetMapping("/question-selects")
    public ApiResponse<?> list(@RequestParam String questionId) {
        return ApiResponse.success(service.list(questionId));
    }

    @GetMapping("/questions/{questionId}/selects")
    public ApiResponse<?> listByQuestionPath(@PathVariable String questionId) {
        return ApiResponse.success(service.list(questionId));
    }

    @PutMapping("/question-selects/{questionId}")
    public ApiResponse<Void> saveBatch(@PathVariable String questionId, @RequestBody List<Map<String, Object>> selects) {
        service.saveBatch(questionId, selects);
        return ApiResponse.success(null);
    }

    @PutMapping("/questions/{questionId}/selects")
    public ApiResponse<Void> saveBatchByQuestionPath(@PathVariable String questionId, @RequestBody List<Map<String, Object>> selects) {
        service.saveBatch(questionId, selects);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/question-selects/{selectId}")
    public ApiResponse<Void> delete(@PathVariable String selectId) {
        service.delete(selectId);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/questions/{questionId}/selects/{selectId}")
    public ApiResponse<Void> deleteByQuestionPath(@PathVariable String selectId) {
        service.delete(selectId);
        return ApiResponse.success(null);
    }
}
