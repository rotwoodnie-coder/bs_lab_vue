package com.xuanyue.exp.exp.controller;

import com.xuanyue.exp.common.ApiResponse;
import com.xuanyue.exp.exp.service.ExpVideoService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/exp/videos")
public class ExpVideoController {

    private final ExpVideoService service;

    public ExpVideoController(ExpVideoService service) {
        this.service = service;
    }

    @GetMapping("/{expId}")
    public ApiResponse<?> list(@PathVariable String expId) {
        return ApiResponse.success(service.listByExpId(expId));
    }

    @PostMapping("/{expId}")
    public ApiResponse<Void> save(@PathVariable String expId, @RequestBody List<Map<String, Object>> videos) {
        service.saveBatch(expId, videos);
        return ApiResponse.success(null);
    }

    @PostMapping("/{expId}/one")
    public ApiResponse<Void> saveOne(@PathVariable String expId, @RequestBody Map<String, Object> video) {
        service.saveOne(expId, video);
        return ApiResponse.success(null);
    }
    
    /*
    @PostMapping("/{expId}")
    public ApiResponse<Void> update(@PathVariable String expId, @RequestBody List<Map<String, Object>> videos) {
        service.saveBatch(expId, videos);
        return ApiResponse.success(null);
    }*/

    @DeleteMapping("/{seqId}")
    public ApiResponse<Void> delete(@PathVariable String seqId) {
        service.delete(seqId);
        return ApiResponse.success(null);
    }
}
