package com.xuanyue.exp.mobile.controller;

import com.xuanyue.exp.common.ApiResponse;
import com.xuanyue.exp.exp.service.ExpMaterialService;
import com.xuanyue.exp.exp.service.ExpReferenceService;
import com.xuanyue.exp.exp.service.ExpResultService;
import com.xuanyue.exp.exp.service.ExpScientistService;
import com.xuanyue.exp.exp.service.ExpStandardService;
import com.xuanyue.exp.exp.service.ExpStepService;
import com.xuanyue.exp.exp.service.ExpVideoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mobile/content/exp")
public class MobileContentController {

    private final ExpStandardService standardService;
    private final ExpVideoService videoService;
    private final ExpStepService stepService;
    private final ExpMaterialService materialService;
    private final ExpResultService resultService;
    private final ExpReferenceService referenceService;
    private final ExpScientistService scientistService;

    public MobileContentController(ExpStandardService standardService,
                                   ExpVideoService videoService,
                                   ExpStepService stepService,
                                   ExpMaterialService materialService,
                                   ExpResultService resultService,
                                   ExpReferenceService referenceService,
                                   ExpScientistService scientistService) {
        this.standardService = standardService;
        this.videoService = videoService;
        this.stepService = stepService;
        this.materialService = materialService;
        this.resultService = resultService;
        this.referenceService = referenceService;
        this.scientistService = scientistService;
    }

    @GetMapping("/{expId}/detail")
    public ApiResponse<?> detail(@PathVariable String expId) {
        return ApiResponse.success(standardService.getDetailView(expId));
    }

    @GetMapping("/{expId}/videos")
    public ApiResponse<?> videos(@PathVariable String expId) {
        return ApiResponse.success(videoService.listByExpId(expId));
    }

    @GetMapping("/{expId}/steps")
    public ApiResponse<?> steps(@PathVariable String expId) {
        return ApiResponse.success(stepService.listByExpId(expId));
    }

    @GetMapping("/{expId}/materials")
    public ApiResponse<?> materials(@PathVariable String expId) {
        return ApiResponse.success(materialService.listByExpId(expId));
    }

    @GetMapping("/{expId}/results")
    public ApiResponse<?> results(@PathVariable String expId) {
        return ApiResponse.success(resultService.listByExpId(expId));
    }

    @GetMapping("/{expId}/references")
    public ApiResponse<?> references(@PathVariable String expId) {
        return ApiResponse.success(referenceService.listByExpId(expId));
    }

    @GetMapping("/{expId}/scientists")
    public ApiResponse<?> scientists(@PathVariable String expId) {
        return ApiResponse.success(scientistService.listByExpId(expId));
    }
}
