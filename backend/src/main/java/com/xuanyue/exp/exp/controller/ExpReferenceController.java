package com.xuanyue.exp.exp.controller;

import com.xuanyue.exp.common.ApiResponse;
import com.xuanyue.exp.exp.service.ExpReferenceService;
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
@RequestMapping("/api/exp/references")
public class ExpReferenceController {

    private final ExpReferenceService service;

    public ExpReferenceController(ExpReferenceService service) {
        this.service = service;
    }

    @GetMapping("/{expId}")
    public ApiResponse<?> list(@PathVariable String expId) {
        return ApiResponse.success(service.listByExpId(expId));
    }

    @PostMapping("/{expId}")
    public ApiResponse<?> saveBatch(@PathVariable String expId, @RequestBody List<Map<String, Object>> references) {
        return ApiResponse.success(service.saveBatch(expId, references));
    }

    @PostMapping("/{expId}/one")
    public ApiResponse<?> saveOne(@PathVariable String expId, @RequestBody Map<String, Object> reference) {
        return ApiResponse.success(service.saveOne(expId, reference));
    }

    @DeleteMapping("/{referenceId}")
    public ApiResponse<Void> delete(@PathVariable String referenceId) {
        service.delete(referenceId);
        return ApiResponse.success(null);
    }
}
