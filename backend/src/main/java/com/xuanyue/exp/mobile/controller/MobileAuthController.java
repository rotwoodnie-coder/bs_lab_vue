package com.xuanyue.exp.mobile.controller;



import com.xuanyue.exp.common.ApiResponse;

import com.xuanyue.exp.mobile.dto.LoginNameAvailabilityDto;

import com.xuanyue.exp.mobile.dto.MobileLoginRequest;

import com.xuanyue.exp.mobile.dto.MobileRefreshTokenRequest;

import com.xuanyue.exp.mobile.dto.MobileSessionDto;

import com.xuanyue.exp.mobile.dto.ParentRegisterRequest;

import com.xuanyue.exp.mobile.dto.ParentRegisterResultDto;

import com.xuanyue.exp.mobile.service.MobileAuthLoginService;

import com.xuanyue.exp.mobile.service.MobileParentAuthService;

import com.xuanyue.exp.system.dto.LoginResponse;

import org.springframework.validation.annotation.Validated;

import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.RequestBody;

import org.springframework.web.bind.annotation.RequestHeader;

import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.bind.annotation.RestController;



@RestController

@RequestMapping("/api/mobile/auth")

public class MobileAuthController {



    private final MobileParentAuthService parentAuthService;

    private final MobileAuthLoginService mobileAuthLoginService;



    public MobileAuthController(MobileParentAuthService parentAuthService,

                              MobileAuthLoginService mobileAuthLoginService) {

        this.parentAuthService = parentAuthService;

        this.mobileAuthLoginService = mobileAuthLoginService;

    }



    @PostMapping("/login")

    public ApiResponse<LoginResponse> login(@Validated @RequestBody MobileLoginRequest request) {

        try {

            return ApiResponse.success(mobileAuthLoginService.login(request));

        } catch (RuntimeException e) {

            return ApiResponse.fail(400, e.getMessage());

        }

    }



    @PostMapping("/refresh")

    public ApiResponse<LoginResponse> refresh(@RequestBody MobileRefreshTokenRequest request) {

        try {

            return ApiResponse.success(mobileAuthLoginService.refresh(request));

        } catch (RuntimeException e) {

            return ApiResponse.fail(401, e.getMessage());

        }

    }



    @GetMapping("/session")

    public ApiResponse<MobileSessionDto> session(

            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        if (userId == null || userId.trim().isEmpty()) {

            return ApiResponse.fail(401, "请先登录");

        }

        try {

            return ApiResponse.success(mobileAuthLoginService.getSession(userId.trim()));

        } catch (RuntimeException e) {

            return ApiResponse.fail(400, e.getMessage());

        }

    }



    @PostMapping("/logout")

    public ApiResponse<Void> logout(@RequestBody(required = false) MobileRefreshTokenRequest request) {

        if (request != null) {

            mobileAuthLoginService.logout(request.getRefreshToken());

        }

        return ApiResponse.success(null);

    }



    @GetMapping("/login-name/available")

    public ApiResponse<LoginNameAvailabilityDto> checkLoginName(@RequestParam String loginName) {

        return ApiResponse.success(parentAuthService.checkLoginName(loginName));

    }



    @PostMapping("/parent/register")

    public ApiResponse<ParentRegisterResultDto> register(@RequestBody ParentRegisterRequest request) {

        try {

            return ApiResponse.success(parentAuthService.register(request));

        } catch (IllegalArgumentException e) {

            return ApiResponse.fail(400, e.getMessage());

        }

    }

}

