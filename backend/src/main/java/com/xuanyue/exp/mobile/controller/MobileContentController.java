package com.xuanyue.exp.mobile.controller;

import com.xuanyue.exp.common.ApiResponse;
import com.xuanyue.exp.mobile.service.MobileContentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mobile/content/exp")
public class MobileContentController {

    private final MobileContentService contentService;

    public MobileContentController(MobileContentService contentService) {
        this.contentService = contentService;
    }

    @GetMapping("/{expId}/detail")
    public ApiResponse<?> detail(@PathVariable String expId) {
        return ApiResponse.success(contentService.getDetail(expId));
    }

    @GetMapping("/{expId}/videos")
    public ApiResponse<?> videos(@PathVariable String expId) {
        return ApiResponse.success(contentService.listVideos(expId));
    }

    @GetMapping("/{expId}/steps")
    public ApiResponse<?> steps(@PathVariable String expId) {
        return ApiResponse.success(contentService.listSteps(expId));
    }

    @GetMapping("/{expId}/materials")
    public ApiResponse<?> materials(@PathVariable String expId) {
        return ApiResponse.success(contentService.listMaterials(expId));
    }

    @GetMapping("/{expId}/results")
    public ApiResponse<?> results(@PathVariable String expId) {
        return ApiResponse.success(contentService.listResults(expId));
    }

    @GetMapping("/{expId}/references")
    public ApiResponse<?> references(@PathVariable String expId) {
        return ApiResponse.success(contentService.listReferences(expId));
    }

    @GetMapping("/{expId}/scientists")
    public ApiResponse<?> scientists(@PathVariable String expId) {
        return ApiResponse.success(contentService.listScientists(expId));
    }
}
