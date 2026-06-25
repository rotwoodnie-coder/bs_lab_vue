package com.xuanyue.exp.system.controller;

import com.xuanyue.exp.common.ApiResponse;
import com.xuanyue.exp.system.service.DataFileTypeService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/sys/data-file-types")
public class DataFileTypeController {

    private final DataFileTypeService dataFileTypeService;

    public DataFileTypeController(DataFileTypeService dataFileTypeService) {
        this.dataFileTypeService = dataFileTypeService;
    }

    @GetMapping
    public ApiResponse<?> list() {
        return ApiResponse.success(dataFileTypeService.list());
    }

    @GetMapping("/{id}")
    public ApiResponse<?> get(@PathVariable String id) {
        return ApiResponse.success(dataFileTypeService.get(id));
    }

    @PostMapping
    public ApiResponse<Void> create(@RequestBody Map<String, Object> payload) {
        dataFileTypeService.create(payload);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable String id, @RequestBody Map<String, Object> payload) {
        dataFileTypeService.update(id, payload);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        dataFileTypeService.delete(id);
        return ApiResponse.success(null);
    }
}
