package com.xuanyue.exp.exp.controller;

import com.xuanyue.exp.common.ApiResponse;
import com.xuanyue.exp.exp.service.ExpStepService;
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
@RequestMapping("/api/exp/steps")
public class ExpStepController {

    private final ExpStepService service;

    public ExpStepController(ExpStepService service) {
        this.service = service;
    }

    @GetMapping("/{expId}")
    public ApiResponse<?> list(@PathVariable String expId) {
        return ApiResponse.success(service.listByExpId(expId));
    }

    @PostMapping("/{expId}")
    public ApiResponse<?> saveBatch(@PathVariable String expId, @RequestBody List<Map<String, Object>> steps) {
        return ApiResponse.success(service.saveBatch(expId, steps));
    }

    @PostMapping("/{expId}/one")
    public ApiResponse<?> saveOne(@PathVariable String expId, @RequestBody Map<String, Object> step) {
        return ApiResponse.success(service.saveOne(expId, step));
    }

    @DeleteMapping("/{stepId}")
    public ApiResponse<Void> delete(@PathVariable String stepId) {
        service.delete(stepId);
        return ApiResponse.success(null);
    }
}
