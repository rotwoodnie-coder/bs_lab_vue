package com.xuanyue.exp.exp.controller;

import com.xuanyue.exp.common.ApiResponse;
import com.xuanyue.exp.exp.service.ExpMaterialService;
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
@RequestMapping("/api/exp/materials")
public class ExpMaterialController {

    private final ExpMaterialService service;

    public ExpMaterialController(ExpMaterialService service) {
        this.service = service;
    }

    @GetMapping("/{expId}")
    public ApiResponse<?> list(@PathVariable String expId) {
        return ApiResponse.success(service.listByExpId(expId));
    }

    @PostMapping("/{expId}")
    public ApiResponse<Void> save(@PathVariable String expId, @RequestBody List<Map<String, Object>> materials) {
        service.saveBatch(expId, materials);
        return ApiResponse.success(null);
    }

    @PostMapping("/{expId}/one")
    public ApiResponse<?> saveOne(@PathVariable String expId, @RequestBody Map<String, Object> material) {
        return ApiResponse.success(service.saveOne(expId, material));
    }

    @PostMapping("/one/{expMaterialId}")
    public ApiResponse<Void> updateOne(@PathVariable String expMaterialId, @RequestBody Map<String, Object> material) {
        service.updateOne(expMaterialId, material);
        return ApiResponse.success(null);
    }

    @GetMapping("/pics/{expMaterialId}")
    public ApiResponse<?> listPics(@PathVariable String expMaterialId) {
        return ApiResponse.success(service.listPicsByExpMaterialId(expMaterialId));
    }

    @DeleteMapping("/{expMaterialId}")
    public ApiResponse<Void> delete(@PathVariable String expMaterialId) {
        service.delete(expMaterialId);
        return ApiResponse.success(null);
    }
}
