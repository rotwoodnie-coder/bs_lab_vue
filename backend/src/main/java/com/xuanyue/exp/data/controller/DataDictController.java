package com.xuanyue.exp.data.controller;

import com.xuanyue.exp.common.ApiResponse;
import com.xuanyue.exp.data.service.DataDictService;
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
@RequestMapping("/api/data/dict")
public class DataDictController {

    private final DataDictService dataDictService;

    public DataDictController(DataDictService dataDictService) {
        this.dataDictService = dataDictService;
    }

    @GetMapping("/{type}")
    public ApiResponse<?> list(@PathVariable String type,
                               @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                               @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
                               @RequestParam(value = "keyword", required = false) String keyword,
                               @RequestParam(value = "paged", defaultValue = "false") boolean paged) {
        if (paged) {
            Map<String, Object> result = new HashMap<String, Object>();
            result.put("records", dataDictService.page(type, pageNum, pageSize, keyword));
            result.put("total", dataDictService.count(type, keyword));
            result.put("pageNum", pageNum);
            result.put("pageSize", pageSize);
            return ApiResponse.success(result);
        }
        return ApiResponse.success(dataDictService.list(type));
    }

    @GetMapping("/{type}/{id}")
    public ApiResponse<?> get(@PathVariable String type, @PathVariable String id) {
        return ApiResponse.success(dataDictService.get(type, id));
    }

    @PostMapping("/{type}")
    public ApiResponse<Void> create(@PathVariable String type, @RequestBody Map<String, Object> payload) {
        dataDictService.create(type, payload);
        return ApiResponse.success(null);
    }

    @PutMapping("/{type}/{id}")
    public ApiResponse<Void> update(@PathVariable String type, @PathVariable String id, @RequestBody Map<String, Object> payload) {
        dataDictService.update(type, id, payload);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{type}/{id}")
    public ApiResponse<Void> delete(@PathVariable String type, @PathVariable String id) {
        dataDictService.delete(type, id);
        return ApiResponse.success(null);
    }
}
