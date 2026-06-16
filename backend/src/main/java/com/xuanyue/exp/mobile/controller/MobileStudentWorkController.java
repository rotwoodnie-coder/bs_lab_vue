package com.xuanyue.exp.mobile.controller;

import com.xuanyue.exp.common.ApiResponse;
import com.xuanyue.exp.common.PageResult;
import com.xuanyue.exp.mobile.dto.AssignHomeworkRequest;
import com.xuanyue.exp.mobile.dto.CreateWorkRequest;
import com.xuanyue.exp.mobile.dto.MobileTaskDto;
import com.xuanyue.exp.mobile.dto.MobileWorkDetailDto;
import com.xuanyue.exp.mobile.dto.MobileWorkItemDto;
import com.xuanyue.exp.mobile.dto.TeacherReviewRequest;
import com.xuanyue.exp.mobile.dto.TeacherTaskSummaryDto;
import com.xuanyue.exp.mobile.service.MobileStudentWorkService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 移动端学生作品 API（v2 版，使用 exp_msg + exp_homework）
 *
 * 所有路径以 /api/mobile/v2/ 为前缀，与管理端代码完全隔离。
 */
@RestController
@RequestMapping("/api/mobile/v2/works")
public class MobileStudentWorkController {

    private final MobileStudentWorkService studentWorkService;

    public MobileStudentWorkController(MobileStudentWorkService studentWorkService) {
        this.studentWorkService = studentWorkService;
    }

    /* ───── 教师布置作业 ───── */

    @PostMapping("/assign")
    public ApiResponse<Void> assignHomework(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody AssignHomeworkRequest request) {
        try {
            studentWorkService.assignHomework(userId, request);
            return ApiResponse.success(null);
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    /* ───── 学生提交作品 ───── */

    @PostMapping("/submit")
    public ApiResponse<MobileWorkDetailDto> submitWork(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody CreateWorkRequest request) {
        try {
            return ApiResponse.success(studentWorkService.submitWork(userId, request));
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    /* ───── 教师批阅 ───── */

    @PostMapping("/{msgId}/review")
    public ApiResponse<Void> reviewWork(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String msgId,
            @RequestBody TeacherReviewRequest request) {
        try {
            studentWorkService.reviewWork(userId, msgId,
                    request != null ? request.getRating() : "pass",
                    request != null ? request.getComment() : null);
            return ApiResponse.success(null);
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    /* ───── 拍同款 / 创意实验开始 ───── */

    @PostMapping("/start")
    public ApiResponse<MobileTaskDto> startStudentTask(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam("type") String type,
            @RequestParam(value = "refExpId", required = false) String refExpId) {
        try {
            return ApiResponse.success(studentWorkService.startStudentTask(userId, type, refExpId));
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    /* ───── 作品列表 ───── */

    @GetMapping
    public ApiResponse<PageResult<MobileWorkItemDto>> listWorks(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestParam(value = "scope", required = false) String scope,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        try {
            if ("mine".equalsIgnoreCase(scope) && userId != null) {
                return ApiResponse.success(studentWorkService.listMyWorks(userId, type, page, size));
            }
            return ApiResponse.success(studentWorkService.listPublicWorks(type, page, size));
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    @GetMapping("/{msgId}")
    public ApiResponse<MobileWorkDetailDto> getWork(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @PathVariable String msgId) {
        MobileWorkDetailDto detail = studentWorkService.getWorkDetail(msgId, userId);
        if (detail == null) {
            return ApiResponse.fail(404, "作品不存在");
        }
        return ApiResponse.success(detail);
    }

    /* ───── 教师看板 ───── */

    @GetMapping("/teacher/homeworks")
    public ApiResponse<List<TeacherTaskSummaryDto>> listTeacherHomeworks(
            @RequestHeader("X-User-Id") String userId) {
        return ApiResponse.success(studentWorkService.listTeacherHomeworks(userId));
    }
}
