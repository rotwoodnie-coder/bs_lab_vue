package com.xuanyue.exp.mobile.controller;

import com.xuanyue.exp.common.ApiResponse;
import com.xuanyue.exp.common.PageResult;
import com.xuanyue.exp.mobile.dto.*;
import com.xuanyue.exp.mobile.service.MobileTeacherParentBindService;
import com.xuanyue.exp.mobile.service.MobileTeacherService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mobile/teacher")
public class MobileTeacherController {

    private final MobileTeacherService teacherService;
    private final MobileTeacherParentBindService parentBindService;

    public MobileTeacherController(MobileTeacherService teacherService,
                                   MobileTeacherParentBindService parentBindService) {
        this.teacherService = teacherService;
        this.parentBindService = parentBindService;
    }

    @GetMapping("/dashboard")
    public ApiResponse<TeacherDashboardDto> dashboard(
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            return ApiResponse.success(teacherService.getDashboard(userId));
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    @GetMapping("/review-queue")
    public ApiResponse<PageResult<TeacherReviewQueueItemDto>> reviewQueue(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "50") int size) {
        return ApiResponse.success(teacherService.listReviewQueue(userId, page, size));
    }

    @PostMapping("/works/{workId}/review")
    public ApiResponse<Void> review(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @PathVariable String workId,
            @RequestBody TeacherReviewRequest request) {
        try {
            teacherService.submitReview(userId, workId, request);
            return ApiResponse.success(null);
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    @GetMapping("/task-board")
    public ApiResponse<TeacherTaskBoardDto> taskBoard(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestParam String taskId) {
        try {
            return ApiResponse.success(teacherService.getTaskBoard(userId, taskId));
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    @GetMapping("/assign-options")
    public ApiResponse<TeacherAssignOptionsDto> assignOptions(
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        return ApiResponse.success(teacherService.getAssignOptions(userId));
    }

    @GetMapping("/tasks")
    public ApiResponse<List<TeacherTaskSummaryDto>> teacherTasks(
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        return ApiResponse.success(teacherService.listTeacherTasks(userId));
    }

    @PostMapping("/remind")
    public ApiResponse<TeacherRemindResultDto> remind(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestParam String taskId) {
        try {
            return ApiResponse.success(teacherService.remindUnsubmitted(userId, taskId));
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    @GetMapping("/parent-binds")
    public ApiResponse<PageResult<TeacherParentBindItemDto>> parentBinds(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestParam(value = "status", defaultValue = "pending") String status,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        try {
            return ApiResponse.success(parentBindService.list(userId, status, page, size));
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    @GetMapping("/parent-binds/pending-count")
    public ApiResponse<Integer> parentBindPendingCount(
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        return ApiResponse.success(parentBindService.countPending(userId));
    }

    @PatchMapping("/parent-binds/{bindId}")
    public ApiResponse<TeacherParentBindItemDto> auditParentBind(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @PathVariable String bindId,
            @RequestBody TeacherParentBindAuditRequest request) {
        try {
            return ApiResponse.success(parentBindService.audit(userId, bindId, request));
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }
}
