package com.xuanyue.exp.mobile.controller;

import com.xuanyue.exp.mobile.config.MobileWebProperties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * AI 视觉审查 Controller
 * 将移动端图片审查请求转发到 agents_service (FastAPI)
 * 路径: /api/mobile/vision/*
 */
@RestController
@RequestMapping("/api/mobile/vision")
public class MobileVisionReviewController {

    private static final Logger log = LoggerFactory.getLogger(MobileVisionReviewController.class);

    private final RestTemplate restTemplate;
    private final String agentsBaseUrl;

    public MobileVisionReviewController(MobileWebProperties mobileWebProperties) {
        this.restTemplate = new RestTemplate();
        this.agentsBaseUrl = mobileWebProperties.getAgentsBaseUrl().trim().replaceAll("/+$", "");
        log.info("Mobile vision review proxy target: {}", agentsBaseUrl);
    }

    /**
     * 提交图片进行 AI 视觉审查
     * POST /api/mobile/vision/review
     *
     * @param request { image_base64, grade_level?, role?, thread_id? }
     * @return { summary, overall_rating, is_safe, ... }
     */
    @PostMapping("/review")
    public ResponseEntity<Map<String, Object>> reviewImage(@RequestBody Map<String, Object> request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> httpRequest = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    agentsBaseUrl + "/v1/vision/review",
                    HttpMethod.POST, httpRequest, Map.class);
            Map<String, Object> respBody = response.getBody();
            if (respBody == null) {
                respBody = new HashMap<>();
            }
            return ResponseEntity.ok(respBody);
        } catch (Exception e) {
            log.error("视觉审查请求失败: {}", e.getMessage());
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("summary", "视觉分析服务暂时不可用");
            fallback.put("overall_rating", "未分析");
            fallback.put("is_safe", null);
            return ResponseEntity.ok(fallback);
        }
    }
}
