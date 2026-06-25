package com.xuanyue.exp.mobile.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.xuanyue.exp.mobile.config.MobileWebProperties;

import java.util.*;

/**
 * AI 聊天代理服务
 * 将移动端的聊天请求转发到 agents_service (FastAPI + LangGraph)
 */
@Service
public class MobileChatProxyService {

    private static final Logger log = LoggerFactory.getLogger(MobileChatProxyService.class);
    /** agents_service 当前仅注册 student（石头老师） */
    private static final Set<String> SUPPORTED_AGENT_ROLES = Collections.singleton("student");

    private final RestTemplate restTemplate;
    private final String agentsBaseUrl;

    public MobileChatProxyService(MobileWebProperties mobileWebProperties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10_000);
        factory.setReadTimeout(120_000);
        this.restTemplate = new RestTemplate(factory);
        this.agentsBaseUrl = normalizeBaseUrl(mobileWebProperties.getAgentsBaseUrl());
        log.info("Mobile AI chat proxy target: {}", agentsBaseUrl);
    }

    static String normalizeBaseUrl(String raw) {
        if (!StringUtils.hasText(raw)) {
            return "http://127.0.0.1:5001";
        }
        return raw.trim().replaceAll("/+$", "");
    }

    static String normalizeAgentRole(String role) {
        String normalized = role == null ? "" : role.trim().toLowerCase();
        if (SUPPORTED_AGENT_ROLES.contains(normalized)) {
            return normalized;
        }
        if (StringUtils.hasText(normalized)) {
            log.info("Agent 角色 {} 尚未上线，回退为 student", normalized);
        }
        return "student";
    }

    /**
     * 发送聊天消息到 AI Agent 服务
     *
     * @param message   用户消息
     * @param threadId  会话ID（可选，新会话传空）
     * @param userId    用户ID
     * @param userName  用户名
     * @param gradeLevel 年级段（低段/中段/高段，可选）
     * @return { reply, thread_id }
     */
    public Map<String, Object> sendChat(String message, String threadId,
                                         String userId, String userName,
                                         String gradeLevel,
                                         String role) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", message);
        body.put("role", normalizeAgentRole(role));
        if (threadId != null && !threadId.isEmpty()) {
            body.put("thread_id", threadId);
        }
        if (userId != null) body.put("user_id", userId);
        if (userName != null) body.put("user_name", userName);
        if (gradeLevel != null && !gradeLevel.trim().isEmpty()) {
            body.put("grade_level", gradeLevel.trim());
        }

        log.info("转发聊天请求到 Agent 服务, message={}, threadId={}, gradeLevel={}",
                message, threadId, gradeLevel);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    agentsBaseUrl + "/v1/chat/sync",
                    HttpMethod.POST,
                    request,
                    Map.class
            );
            Map<String, Object> respBody = response.getBody();
            if (respBody == null) {
                respBody = new HashMap<>();
                respBody.put("reply", "抱歉，石头老师暂时无法回复，请稍后再试。");
            }
            log.info("Agent 服务响应成功, threadId={}", respBody.get("thread_id"));
            return respBody;
        } catch (Exception e) {
            log.error("Agent 服务请求失败: {}", e.getMessage());
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("reply", "抱歉，石头老师现在有点忙，请稍后再试。");
            fallback.put("thread_id", threadId);
            return fallback;
        }
    }

    /**
     * 获取聊天历史
     */
    public Object getChatHistory(String threadId) {
        try {
            String url = agentsBaseUrl + "/v1/student/chat/history/" + threadId;
            ResponseEntity<Object> response = restTemplate.getForEntity(url, Object.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("获取聊天历史失败: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 清除聊天会话
     */
    public void clearChatSession(String threadId) {
        try {
            restTemplate.delete(agentsBaseUrl + "/v1/student/chat/clear/" + threadId);
            log.info("已清除聊天会话: {}", threadId);
        } catch (Exception e) {
            log.error("清除聊天会话失败: {}", e.getMessage());
        }
    }
}
