package com.xuanyue.exp.mobile.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import com.xuanyue.exp.mobile.config.MobileWebProperties;

import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.*;

/**
 * AI 聊天代理服务
 * 将移动端的聊天请求转发到 agents_service (FastAPI + LangGraph)
 */
@Service
public class MobileChatProxyService {

    private static final Logger log = LoggerFactory.getLogger(MobileChatProxyService.class);
    /** agents_service 支持的 AI 角色 */
    private static final Set<String> SUPPORTED_AGENT_ROLES = Collections.unmodifiableSet(
        new HashSet<>(Arrays.asList("free_chat", "pre_experiment", "plan_design", "post_experiment", "student"))
    );

    private final RestTemplate restTemplate;
    private final WebClient webClient;
    private final String agentsBaseUrl;

    public MobileChatProxyService(MobileWebProperties mobileWebProperties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10_000);
        factory.setReadTimeout(120_000);
        this.restTemplate = new RestTemplate(factory);
        this.agentsBaseUrl = normalizeBaseUrl(mobileWebProperties.getAgentsBaseUrl());
        this.webClient = WebClient.builder()
                .baseUrl(agentsBaseUrl)
                .build();
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
     * 发送聊天消息到 AI Agent 服务（同步/阻塞模式，仅 plan_design 使用）
     *
     * @param message          用户消息
     * @param threadId         会话ID（可选，新会话传空）
     * @param userId           用户ID
     * @param userName         用户名
     * @param gradeLevel       年级段（低段/中段/高段，可选）
     * @param role             AI 角色
     * @param experimentTitle  实验标题（可选）
     * @param imageBase64      图片 Base64（可选）
     * @return { reply, thread_id }
     */
    public Map<String, Object> sendChat(String message, String threadId,
                                         String userId, String userName,
                                         String gradeLevel,
                                         String role,
                                         String experimentTitle,
                                         String imageBase64) {
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
        if (experimentTitle != null && !experimentTitle.trim().isEmpty()) {
            body.put("experiment_title", experimentTitle.trim());
        }
        if (imageBase64 != null && !imageBase64.trim().isEmpty()) {
            body.put("image_base64", imageBase64.trim());
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
     * 流式聊天消息到 AI Agent 服务（SSE 非阻塞模式）
     *
     * @param message    用户消息
     * @param threadId   会话ID（可选，新会话传空）
     * @param userId     用户ID
     * @param userName   用户名
     * @param gradeLevel 年级段（低段/中段/高段，可选）
     * @param role       AI 角色
     * @return Flux&lt;ServerSentEvent&lt;String&gt;&gt; SSE 事件流
     */
    public Flux<ServerSentEvent<String>> streamChat(String message, String threadId,
                                                     String userId, String userName,
                                                     String gradeLevel,
                                                     String role,
                                                     String imageBase64) {
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
        if (imageBase64 != null && !imageBase64.trim().isEmpty()) {
            body.put("image_base64", imageBase64.trim());
        }

        log.info("转发流式聊天请求到 Agent 服务, message={}, threadId={}, gradeLevel={}, hasImage={}",
                message, threadId, gradeLevel, imageBase64 != null && !imageBase64.trim().isEmpty());

        return webClient.post()
                .uri("/v1/chat")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(new ParameterizedTypeReference<ServerSentEvent<String>>() {})
                .map(event -> {
                    String data = event.data();
                    if (data == null) data = "";
                    return ServerSentEvent.<String>builder(data).build();
                })
                .timeout(Duration.ofSeconds(120))
                .doOnError(e -> log.error("流式请求失败: {}", e.getMessage()))
                .onErrorResume(e -> Flux.just(
                        ServerSentEvent.builder("{\"error\":\"服务暂时不可用\"}").build()
                ));
    }

    /**
     * 获取聊天历史
     */
    public Object getChatHistory(String threadId) {
        try {
            String url = agentsBaseUrl + "/v1/chat/history/" + threadId;
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
            restTemplate.delete(agentsBaseUrl + "/v1/chat/clear/" + threadId);
            log.info("已清除聊天会话: {}", threadId);
        } catch (Exception e) {
            log.error("清除聊天会话失败: {}", e.getMessage());
        }
    }
}
