package com.xuanyue.exp.mobile.controller;

import com.xuanyue.exp.common.ApiResponse;
import com.xuanyue.exp.mobile.dto.MobileProfileDto;
import com.xuanyue.exp.mobile.service.MobileProfileService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 移动端个人中心 Controller
 * 路径: /api/mobile/profile
 */
@RestController
@RequestMapping("/api/mobile/profile")
public class MobileProfileController {

    private final MobileProfileService profileService;

    public MobileProfileController(MobileProfileService profileService) {
        this.profileService = profileService;
    }

    /**
     * 获取当前用户完整信息
     * GET /api/mobile/profile
     */
    @GetMapping
    public ApiResponse<MobileProfileDto> getProfile(@RequestHeader("X-User-Id") String userId) {
        return ApiResponse.success(profileService.getProfile(userId));
    }

    /**
     * 更新个人资料
     * PUT /api/mobile/profile
     */
    @PutMapping
    public ApiResponse<Void> updateProfile(@RequestHeader("X-User-Id") String userId,
                                           @RequestBody Map<String, Object> payload) {
        return doUpdateProfile(userId, payload);
    }

    /**
     * 更新个人资料（POST，兼容外网仅放行 POST 的网关/WAF）
     * POST /api/mobile/profile
     */
    @PostMapping
    public ApiResponse<Void> saveProfile(@RequestHeader("X-User-Id") String userId,
                                         @RequestBody Map<String, Object> payload) {
        return doUpdateProfile(userId, payload);
    }

    /** POST /api/mobile/profile/save（兼容旧移动端包） */
    @PostMapping("/save")
    public ApiResponse<Void> saveProfileAlias(@RequestHeader("X-User-Id") String userId,
                                              @RequestBody Map<String, Object> payload) {
        return doUpdateProfile(userId, payload);
    }

    private ApiResponse<Void> doUpdateProfile(String userId, Map<String, Object> payload) {
        profileService.updateProfile(userId, payload);
        return ApiResponse.success(null);
    }

    /**
     * 修改密码
     * POST /api/mobile/profile/password
     */
    @PostMapping("/password")
    public ApiResponse<Void> changePassword(@RequestHeader("X-User-Id") String userId,
                                            @RequestBody Map<String, String> payload) {
        String oldPassword = payload.get("oldPassword");
        String newPassword = payload.get("newPassword");
        profileService.changePassword(userId, oldPassword, newPassword);
        return ApiResponse.success(null);
    }
}
