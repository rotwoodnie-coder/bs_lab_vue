package com.xuanyue.exp.exp.controller;

import com.xuanyue.exp.common.ApiResponse;
import com.xuanyue.exp.exp.service.ExpScientistService;
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
@RequestMapping("/api/exp/scientists")
public class ExpScientistController {

    private final ExpScientistService service;

    public ExpScientistController(ExpScientistService service) {
        this.service = service;
    }

    @GetMapping("/{expId}")
    public ApiResponse<?> list(@PathVariable String expId) {
        return ApiResponse.success(service.listByExpId(expId));
    }

    @PostMapping("/{expId}")
    public ApiResponse<?> saveBatch(@PathVariable String expId, @RequestBody List<Map<String, Object>> scientists) {
        return ApiResponse.success(service.saveBatch(expId, scientists));
    }

    @PostMapping("/{expId}/one")
    public ApiResponse<?> saveOne(@PathVariable String expId, @RequestBody Map<String, Object> scientist) {
        return ApiResponse.success(service.saveOne(expId, scientist));
    }

    @DeleteMapping("/{scientistId}")
    public ApiResponse<Void> delete(@PathVariable String scientistId) {
        service.delete(scientistId);
        return ApiResponse.success(null);
    }
}
