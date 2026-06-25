package com.xuanyue.exp.mobile.controller;

import com.xuanyue.exp.common.ApiResponse;
import com.xuanyue.exp.mobile.dto.GrowthPlanSaveRequest;
import com.xuanyue.exp.mobile.dto.MobileBadgeWallDto;
import com.xuanyue.exp.mobile.dto.MobileGrowthDto;
import com.xuanyue.exp.mobile.dto.MobileQuizDto;
import com.xuanyue.exp.mobile.dto.MobileQuizReviewDto;
import com.xuanyue.exp.mobile.dto.QuizSubmitRequest;
import com.xuanyue.exp.mobile.dto.QuizSubmitResultDto;
import com.xuanyue.exp.mobile.service.MobileLearningService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mobile")
public class MobileLearningController {

    private final MobileLearningService learningService;

    public MobileLearningController(MobileLearningService learningService) {
        this.learningService = learningService;
    }

    @GetMapping("/quiz/today")
    public ApiResponse<MobileQuizDto> getTodayQuiz(
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        return ApiResponse.success(learningService.getTodayQuiz(userId));
    }

    @GetMapping("/quiz/record")
    public ApiResponse<MobileQuizDto.TodayResult> getQuizRecord(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestParam(value = "date", required = false) String date) {
        java.time.LocalDate quizDate = parseQuizDate(date);
        if (quizDate == null) {
            return ApiResponse.fail(400, "日期格式无效");
        }
        return learningService.getRecordByDate(userId, quizDate)
                .map(ApiResponse::success)
                .orElseGet(() -> ApiResponse.fail(404, "答题记录不存在"));
    }

    @GetMapping("/quiz/review")
    public ApiResponse<java.util.List<MobileQuizReviewDto>> getQuizReview(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestParam(value = "date", required = false) String date) {
        java.time.LocalDate quizDate = parseQuizDate(date);
        if (quizDate == null) {
            return ApiResponse.fail(400, "日期格式无效");
        }
        return learningService.getReview(userId, quizDate)
                .map(ApiResponse::success)
                .orElseGet(() -> ApiResponse.fail(404, "暂无错题解析"));
    }

    @PostMapping("/quiz/submit")
    public ApiResponse<QuizSubmitResultDto> submitQuiz(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestBody QuizSubmitRequest request) {
        try {
            return ApiResponse.success(learningService.submitQuiz(userId, request));
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(400, e.getMessage());
        } catch (IllegalStateException e) {
            return ApiResponse.fail(409, e.getMessage());
        } catch (DataIntegrityViolationException e) {
            return ApiResponse.fail(409, "今日已提交过答卷，请勿重复提交");
        }
    }

    @GetMapping("/badges")
    public ApiResponse<MobileBadgeWallDto> getBadges(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestParam(value = "childUserId", required = false) String childUserId) {
        try {
            return ApiResponse.success(learningService.getBadges(userId, childUserId));
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    @GetMapping("/growth")
    public ApiResponse<MobileGrowthDto> getGrowth(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestParam(value = "childUserId", required = false) String childUserId) {
        try {
            return ApiResponse.success(learningService.getGrowth(userId, childUserId));
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    @PutMapping("/growth/plan")
    public ApiResponse<MobileGrowthDto.Plan> saveGrowthPlan(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestBody GrowthPlanSaveRequest request) {
        return doSaveGrowthPlan(userId, request);
    }

    @PostMapping("/growth/plan/save")
    public ApiResponse<MobileGrowthDto.Plan> saveGrowthPlanViaPost(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestBody GrowthPlanSaveRequest request) {
        return doSaveGrowthPlan(userId, request);
    }

    private ApiResponse<MobileGrowthDto.Plan> doSaveGrowthPlan(String userId, GrowthPlanSaveRequest request) {
        try {
            return ApiResponse.success(learningService.saveGrowthPlan(userId, request));
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    private static java.time.LocalDate parseQuizDate(String raw) {
        if (!StringUtils.hasText(raw)) {
            return java.time.LocalDate.now();
        }
        try {
            return java.time.LocalDate.parse(raw.trim());
        } catch (Exception e) {
            return null;
        }
    }
}
