package com.xuanyue.exp.mobile.controller;

import com.xuanyue.exp.common.ApiResponse;
import com.xuanyue.exp.mobile.dto.CreateCommentRequest;
import com.xuanyue.exp.mobile.dto.MobileCommentDto;
import com.xuanyue.exp.mobile.dto.MobileSocialSummaryDto;
import com.xuanyue.exp.mobile.dto.ToggleReactionRequest;
import com.xuanyue.exp.mobile.service.MobileSocialService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mobile/social")
public class MobileSocialController {

    private final MobileSocialService socialService;

    public MobileSocialController(MobileSocialService socialService) {
        this.socialService = socialService;
    }

    @GetMapping("/comments")
    public ApiResponse<List<MobileCommentDto>> listComments(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestParam String targetType,
            @RequestParam String targetId) {
        return ApiResponse.success(socialService.listComments(userId, targetType, targetId));
    }

    @PostMapping("/comments")
    public ApiResponse<MobileCommentDto> addComment(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestBody CreateCommentRequest request) {
        try {
            return ApiResponse.success(socialService.addComment(userId, request));
        } catch (IllegalArgumentException e) {
            int code = "请先登录".equals(e.getMessage()) ? 401 : 400;
            return ApiResponse.fail(code, e.getMessage());
        }
    }

    @GetMapping("/summary")
    public ApiResponse<MobileSocialSummaryDto> getSummary(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestParam String targetType,
            @RequestParam String targetId) {
        return ApiResponse.success(socialService.getSummary(userId, targetType, targetId));
    }

    @PostMapping("/reactions/toggle")
    public ApiResponse<MobileSocialSummaryDto> toggleReaction(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestBody ToggleReactionRequest request) {
        try {
            return ApiResponse.success(socialService.toggleReaction(userId, request));
        } catch (IllegalArgumentException e) {
            int code = "请先登录".equals(e.getMessage()) ? 401 : 400;
            return ApiResponse.fail(code, e.getMessage());
        }
    }

    @PostMapping("/comments/{commentId}/like/toggle")
    public ApiResponse<MobileCommentDto> toggleCommentLike(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @PathVariable String commentId) {
        try {
            return ApiResponse.success(socialService.toggleCommentLike(userId, commentId));
        } catch (IllegalArgumentException e) {
            int code = "请先登录".equals(e.getMessage()) ? 401 : 400;
            return ApiResponse.fail(code, e.getMessage());
        }
    }
}
