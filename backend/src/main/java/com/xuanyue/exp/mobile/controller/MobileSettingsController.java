package com.xuanyue.exp.mobile.controller;

import com.xuanyue.exp.common.ApiResponse;
import com.xuanyue.exp.mobile.dto.DingTalkAuthorizeDto;
import com.xuanyue.exp.mobile.dto.DingTalkBindRequest;
import com.xuanyue.exp.mobile.dto.DingTalkBindStatusDto;
import com.xuanyue.exp.mobile.dto.MobileAccountSecurityDto;
import com.xuanyue.exp.mobile.dto.MobileUserPreferencesDto;
import com.xuanyue.exp.mobile.service.MobileDingTalkService;
import com.xuanyue.exp.mobile.service.MobileSettingsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mobile/settings")
public class MobileSettingsController {

    private final MobileSettingsService settingsService;
    private final MobileDingTalkService dingTalkService;

    public MobileSettingsController(MobileSettingsService settingsService,
                                    MobileDingTalkService dingTalkService) {
        this.settingsService = settingsService;
        this.dingTalkService = dingTalkService;
    }

    @GetMapping("/preferences")
    public ApiResponse<MobileUserPreferencesDto> getPreferences(
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        return ApiResponse.success(settingsService.getPreferences(userId));
    }

    @PutMapping("/preferences")
    public ApiResponse<MobileUserPreferencesDto> savePreferences(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestBody MobileUserPreferencesDto preferences) {
        return ApiResponse.success(settingsService.savePreferences(userId, preferences));
    }

    @GetMapping("/account")
    public ApiResponse<MobileAccountSecurityDto> getAccountSecurity(
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        return ApiResponse.success(settingsService.getAccountSecurity(userId));
    }

    @GetMapping("/dingtalk/authorize-url")
    public ApiResponse<DingTalkAuthorizeDto> getDingTalkAuthorizeUrl(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestParam(value = "redirectBase", required = false) String redirectBase) {
        if (userId == null || userId.trim().isEmpty()) {
            return ApiResponse.fail(401, "请先登录");
        }
        return ApiResponse.success(dingTalkService.buildAuthorizeUrl(userId.trim(), redirectBase));
    }

    @GetMapping("/dingtalk/status")
    public ApiResponse<DingTalkBindStatusDto> getDingTalkStatus(
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return ApiResponse.fail(401, "请先登录");
        }
        return ApiResponse.success(dingTalkService.getBindStatus(userId.trim()));
    }

    @PostMapping("/dingtalk/bind")
    public ApiResponse<DingTalkBindStatusDto> bindDingTalk(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestBody DingTalkBindRequest request) {
        if (userId == null || userId.trim().isEmpty()) {
            return ApiResponse.fail(401, "请先登录");
        }
        try {
            return ApiResponse.success(dingTalkService.completeBind(
                    userId.trim(),
                    request != null ? request.getCode() : null,
                    request != null ? request.getState() : null));
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(400, e.getMessage());
        } catch (IllegalStateException e) {
            return ApiResponse.fail(500, e.getMessage());
        }
    }

    @DeleteMapping("/dingtalk/bind")
    public ApiResponse<DingTalkBindStatusDto> unbindDingTalk(
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return ApiResponse.fail(401, "请先登录");
        }
        dingTalkService.unbind(userId.trim());
        return ApiResponse.success(dingTalkService.getBindStatus(userId.trim()));
    }
}
