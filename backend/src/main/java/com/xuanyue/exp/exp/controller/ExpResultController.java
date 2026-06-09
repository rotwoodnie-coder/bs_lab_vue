package com.xuanyue.exp.exp.controller;

import com.xuanyue.exp.common.ApiResponse;
import com.xuanyue.exp.exp.service.ExpResultService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/exp/results")
public class ExpResultController {

    private final ExpResultService service;

    public ExpResultController(ExpResultService service) {
        this.service = service;
    }

    @GetMapping("/{expId}")
    public ApiResponse<?> list(@PathVariable String expId) {
        return ApiResponse.success(service.listByExpId(expId));
    }

    @PostMapping("/{expId}")
    public ApiResponse<?> saveBatch(@PathVariable String expId, @RequestBody List<Map<String, Object>> results) {
        return ApiResponse.success(service.saveBatch(expId, results));
    }

    @PostMapping("/{expId}/one")
    public ApiResponse<?> saveOne(@PathVariable String expId, @RequestBody Map<String, Object> result) {
        return ApiResponse.success(service.saveOne(expId, result));
    }

    @DeleteMapping("/{resultId}")
    public ApiResponse<Void> delete(@PathVariable String resultId) {
        service.delete(resultId);
        return ApiResponse.success(null);
    }
}
