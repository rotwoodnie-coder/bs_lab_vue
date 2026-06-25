package com.xuanyue.exp.mobile.admin.controller;

import com.xuanyue.exp.common.ApiResponse;
import com.xuanyue.exp.common.PageResult;
import com.xuanyue.exp.mobile.admin.dto.AdminWorkReviewRequest;
import com.xuanyue.exp.mobile.admin.support.MobileAdminAuthSupport;
import com.xuanyue.exp.mobile.dto.MobileWorkReviewItemDto;
import com.xuanyue.exp.mobile.service.MobileStudentWorkService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员端学生作品审核：系统管理员全局、校管理员限本校。
 */
@RestController
@RequestMapping("/api/mobile/admin/work-reviews")
public class MobileAdminWorkReviewController {

    private final MobileStudentWorkService studentWorkService;
    private final MobileAdminAuthSupport adminAuthSupport;

    public MobileAdminWorkReviewController(MobileStudentWorkService studentWorkService,
                                           MobileAdminAuthSupport adminAuthSupport) {
        this.studentWorkService = studentWorkService;
        this.adminAuthSupport = adminAuthSupport;
    }

    @GetMapping
    public ApiResponse<PageResult<MobileWorkReviewItemDto>> list(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        String userId = adminAuthSupport.requireAdminUser().getUserId();
        return ApiResponse.success(studentWorkService.listPendingWorkReviewsForAdmin(userId, page, size));
    }

    @PostMapping("/{workId}")
    public ApiResponse<Void> review(@PathVariable String workId,
                                    @RequestBody AdminWorkReviewRequest request) {
        String userId = adminAuthSupport.requireAdminUser().getUserId();
        String result = request != null ? request.getResult() : null;
        String comment = request != null ? request.getComment() : null;
        try {
            studentWorkService.reviewWork(userId, workId, result, comment);
            return ApiResponse.success(null);
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }
}
