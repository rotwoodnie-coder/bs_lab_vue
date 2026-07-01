package com.xuanyue.exp.mobile.controller;

import com.xuanyue.exp.mobile.service.MobileChatProxyService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;

import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

/**
 * 移动端 AI 聊天代理 Controller
 * 将移动端的聊天请求代理到 agents_service (FastAPI)
 * 路径: /api/mobile/chat/*
 */
@RestController
@RequestMapping("/api/mobile/chat")
public class MobileChatProxyController {

    private final MobileChatProxyService chatProxyService;

    public MobileChatProxyController(MobileChatProxyService chatProxyService) {
        this.chatProxyService = chatProxyService;
    }

    /**
     * 发送聊天消息（非流式）
     * POST /api/mobile/chat/send
     *
     * @param request { message, thread_id?, user_id?, user_name?, grade_level?, experiment_title?, image_base64?, role? }
     * @return { reply, thread_id }
     */
    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> sendChat(
            @RequestBody Map<String, Object> request,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Name", required = false) String userName,
            @RequestHeader(value = "X-User-Role-Id", required = false) String userRoleId) {

        String message = (String) request.get("message");
        String threadId = (String) request.get("thread_id");
        String reqUserId = (String) request.get("user_id");
        String reqUserName = (String) request.get("user_name");
        String reqGradeLevel = (String) request.get("grade_level");
        String reqRole = (String) request.get("role");
        String experimentTitle = (String) request.get("experiment_title");
        String imageBase64 = (String) request.get("image_base64");

        if (message == null || message.trim().isEmpty()) {
            Map<String, Object> body = new HashMap<>();
            body.put("code", 400);
            body.put("message", "消息不能为空");
            return ResponseEntity.badRequest().body(body);
        }

        // 优先使用请求体中的值，其次使用 header
        String finalUserId = reqUserId != null ? reqUserId : userId;
        String finalUserName = reqUserName != null ? reqUserName : userName;
        String finalRole = reqRole != null ? reqRole : userRoleId;

        Map<String, Object> result = chatProxyService.sendChat(
                message.trim(), threadId, finalUserId, finalUserName,
                reqGradeLevel, finalRole, experimentTitle, imageBase64);

        Map<String, Object> okBody = new HashMap<>();
        okBody.put("code", 200);
        okBody.put("data", result);
        return ResponseEntity.ok(okBody);
    }

    /**
     * 发送聊天消息同步（供方案设计等长耗时任务使用）
     * POST /api/mobile/chat/sync
     */
    @PostMapping("/sync")
    public ResponseEntity<Map<String, Object>> syncChat(@RequestBody Map<String, Object> request) {
        String message = (String) request.get("message");
        String threadId = (String) request.get("thread_id");
        String role = (String) request.get("role");
        String userId = (String) request.get("user_id");
        String userName = (String) request.get("user_name");
        String gradeLevel = (String) request.get("grade_level");
        String experimentTitle = (String) request.get("experiment_title");

        if ((message == null || message.trim().isEmpty())
                && (experimentTitle == null || experimentTitle.trim().isEmpty())) {
            Map<String, Object> body = new HashMap<>();
            body.put("code", 400);
            body.put("message", "message 或 experiment_title 不能为空");
            return ResponseEntity.badRequest().body(body);
        }

        if (message == null || message.trim().isEmpty()) {
            message = "请为「" + experimentTitle.trim() + "」设计实验方案";
        }
        if (role == null || role.trim().isEmpty()) {
            role = "plan_design";
        }

        Map<String, Object> result = chatProxyService.sendChat(
                message.trim(), threadId, userId, userName,
                gradeLevel, role, experimentTitle, null);

        Map<String, Object> okBody = new HashMap<>();
        okBody.put("code", 200);
        okBody.put("data", result);
        return ResponseEntity.ok(okBody);
    }

    /**
     * 流式聊天消息（SSE）
     * POST /api/mobile/chat/stream
     *
     * @param request { message, thread_id?, user_id?, user_name?, grade_level?, role? }
     * @return Flux&lt;ServerSentEvent&lt;String&gt;&gt;
     */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> streamChat(@RequestBody Map<String, Object> request) {
        String message = (String) request.get("message");
        String role = (String) request.get("role");
        String threadId = (String) request.get("thread_id");
        String userId = (String) request.get("user_id");
        String userName = (String) request.get("user_name");
        String gradeLevel = (String) request.get("grade_level");
        String imageBase64 = (String) request.get("image_base64");

        return chatProxyService.streamChat(message, threadId, userId, userName, gradeLevel, role, imageBase64);
    }

    /**
     * 获取聊天历史
     * GET /api/mobile/chat/history/{threadId}
     */
    @GetMapping("/history/{threadId}")
    public ResponseEntity<Map<String, Object>> getHistory(@PathVariable String threadId) {
        Object history = chatProxyService.getChatHistory(threadId);
        Map<String, Object> body = new HashMap<>();
        body.put("code", 200);
        body.put("data", history);
        return ResponseEntity.ok(body);
    }

    /**
     * 清除聊天会话
     * DELETE /api/mobile/chat/clear/{threadId}
     */
    @DeleteMapping("/clear/{threadId}")
    public ResponseEntity<Map<String, Object>> clearSession(@PathVariable String threadId) {
        chatProxyService.clearChatSession(threadId);
        Map<String, Object> body = new HashMap<>();
        body.put("code", 200);
        body.put("message", "会话已清除");
        return ResponseEntity.ok(body);
    }
}
