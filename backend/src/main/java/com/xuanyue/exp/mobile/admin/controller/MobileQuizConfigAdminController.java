package com.xuanyue.exp.mobile.admin.controller;

import com.xuanyue.exp.common.ApiResponse;
import com.xuanyue.exp.mobile.admin.support.MobileAdminAuthSupport;
import com.xuanyue.exp.mobile.dto.MobileQuizConfigDto;
import com.xuanyue.exp.mobile.dto.QuizConfigSaveRequest;
import com.xuanyue.exp.mobile.service.MobileQuizConfigService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mobile/admin/quiz-config")
public class MobileQuizConfigAdminController {

    private final MobileQuizConfigService quizConfigService;
    private final MobileAdminAuthSupport adminAuthSupport;

    public MobileQuizConfigAdminController(MobileQuizConfigService quizConfigService,
                                           MobileAdminAuthSupport adminAuthSupport) {
        this.quizConfigService = quizConfigService;
        this.adminAuthSupport = adminAuthSupport;
    }

    @GetMapping
    public ApiResponse<MobileQuizConfigDto> getConfig() {
        adminAuthSupport.requireAdminUser();
        return ApiResponse.success(quizConfigService.getConfigView());
    }

    @PutMapping
    public ApiResponse<MobileQuizConfigDto> saveConfig(@RequestBody QuizConfigSaveRequest request) {
        return doSaveConfig(request);
    }

    @PostMapping("/save")
    public ApiResponse<MobileQuizConfigDto> saveConfigViaPost(@RequestBody QuizConfigSaveRequest request) {
        return doSaveConfig(request);
    }

    private ApiResponse<MobileQuizConfigDto> doSaveConfig(QuizConfigSaveRequest request) {
        String userId = adminAuthSupport.requireAdminUser().getUserId();
        try {
            return ApiResponse.success(quizConfigService.saveConfig(request, userId));
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }
}
