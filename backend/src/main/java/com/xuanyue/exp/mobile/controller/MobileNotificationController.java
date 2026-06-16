package com.xuanyue.exp.mobile.controller;

import com.xuanyue.exp.common.ApiResponse;
import com.xuanyue.exp.system.service.SysMsgService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mobile/notifications")
public class MobileNotificationController {

    private final SysMsgService sysMsgService;

    public MobileNotificationController(SysMsgService sysMsgService) {
        this.sysMsgService = sysMsgService;
    }

    @GetMapping
    public ApiResponse<?> page(@RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                               @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
                               @RequestParam(value = "msgTypeId", required = false) String msgTypeId,
                               @RequestParam(value = "readTag", required = false) String readTag,
                               @RequestHeader(value = "X-User-Id", required = false) String currentUserId) {
        return ApiResponse.success(sysMsgService.page(pageNum, pageSize, msgTypeId, readTag, currentUserId));
    }

    @GetMapping("/unread-count")
    public ApiResponse<?> unreadCount(@RequestHeader(value = "X-User-Id", required = false) String currentUserId) {
        return ApiResponse.success(sysMsgService.unreadCount(currentUserId));
    }

    @PutMapping("/{msgId}/read")
    public ApiResponse<Void> markRead(@PathVariable String msgId,
                                      @RequestHeader(value = "X-User-Id", required = false) String currentUserId) {
        sysMsgService.markRead(msgId, currentUserId);
        return ApiResponse.success(null);
    }

    @PutMapping("/read-all")
    public ApiResponse<?> markAllRead(@RequestHeader(value = "X-User-Id", required = false) String currentUserId) {
        return ApiResponse.success(sysMsgService.markAllRead(currentUserId));
    }
}
