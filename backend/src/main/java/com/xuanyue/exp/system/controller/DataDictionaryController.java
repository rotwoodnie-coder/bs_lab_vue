package com.xuanyue.exp.system.controller;

import com.xuanyue.exp.common.ApiResponse;
import com.xuanyue.exp.system.service.DataDictionaryService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/sys/data-dictionaries")
public class DataDictionaryController {

    private final DataDictionaryService dataDictionaryService;

    public DataDictionaryController(DataDictionaryService dataDictionaryService) {
        this.dataDictionaryService = dataDictionaryService;
    }

    @GetMapping("/{type}")
    public ApiResponse<?> list(@PathVariable String type) {
        return ApiResponse.success(dataDictionaryService.list(type));
    }

    @GetMapping("/{type}/{id}")
    public ApiResponse<?> get(@PathVariable String type, @PathVariable String id) {
        return ApiResponse.success(dataDictionaryService.get(type, id));
    }

    @PostMapping("/{type}")
    public ApiResponse<Void> create(@PathVariable String type, @RequestBody Map<String, Object> payload) {
        dataDictionaryService.create(type, payload);
        return ApiResponse.success(null);
    }

    @PostMapping("/{type}/{id}")
    public ApiResponse<Void> update(@PathVariable String type, @PathVariable String id, @RequestBody Map<String, Object> payload) {
        dataDictionaryService.update(type, id, payload);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{type}/{id}")
    public ApiResponse<Void> delete(@PathVariable String type, @PathVariable String id) {
        dataDictionaryService.delete(type, id);
        return ApiResponse.success(null);
    }
}
