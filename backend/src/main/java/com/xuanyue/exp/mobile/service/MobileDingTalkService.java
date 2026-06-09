package com.xuanyue.exp.mobile.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xuanyue.exp.mobile.config.DingTalkProperties;
import com.xuanyue.exp.mobile.dto.DingTalkAuthorizeDto;
import com.xuanyue.exp.mobile.dto.DingTalkBindStatusDto;
import com.xuanyue.exp.mobile.entity.MbDingtalkBind;
import com.xuanyue.exp.mobile.repository.MbDingtalkBindRepository;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MobileDingTalkService {

    private static final String AUTH_URL = "https://login.dingtalk.com/oauth2/auth";
    private static final String TOKEN_URL = "https://api.dingtalk.com/v1.0/oauth2/userAccessToken";
    private static final String USER_ME_URL = "https://api.dingtalk.com/v1.0/contact/users/me";
    private static final long STATE_TTL_MS = 10 * 60 * 1000L;

    private final DingTalkProperties properties;
    private final MbDingtalkBindRepository bindRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<String, StateEntry> pendingStates = new ConcurrentHashMap<>();

    public MobileDingTalkService(DingTalkProperties properties,
                                 MbDingtalkBindRepository bindRepository,
                                 ObjectMapper objectMapper) {
        this.properties = properties;
        this.bindRepository = bindRepository;
        this.objectMapper = objectMapper;
    }

    public DingTalkAuthorizeDto buildAuthorizeUrl(String userId, String redirectBase) {
        purgeExpiredStates();
        DingTalkAuthorizeDto dto = new DingTalkAuthorizeDto();
        dto.setConfigured(properties.isConfigured());
        dto.setRedirectUri(properties.redirectUri(redirectBase));

        if (!properties.isConfigured()) {
            dto.setMessage("钉钉绑定未配置，请联系管理员设置 app.dingtalk");
            return dto;
        }

        String state = UUID.randomUUID().toString().replace("-", "");
        pendingStates.put(state, new StateEntry(userId, System.currentTimeMillis()));

        String redirectUri = properties.redirectUri(redirectBase);
        String authorizeUrl = UriComponentsBuilder.fromHttpUrl(AUTH_URL)
                .queryParam("client_id", properties.getClientId())
                .queryParam("redirect_uri", redirectUri)
                .queryParam("response_type", "code")
                .queryParam("scope", "openid")
                .queryParam("state", state)
                .queryParam("prompt", "consent")
                .build()
                .encode(StandardCharsets.UTF_8)
                .toUriString();

        dto.setState(state);
        dto.setAuthorizeUrl(authorizeUrl);
        return dto;
    }

    public DingTalkBindStatusDto getBindStatus(String userId) {
        DingTalkBindStatusDto dto = new DingTalkBindStatusDto();
        dto.setConfigured(properties.isConfigured());

        Optional<MbDingtalkBind> bind = bindRepository.findById(userId);
        if (bind.isPresent()) {
            MbDingtalkBind row = bind.get();
            dto.setBound(true);
            dto.setDingNick(row.getDingNick());
            dto.setBindTime(row.getBindTime());
            dto.setLabel(formatLabel(row.getDingNick()));
        } else {
            dto.setBound(false);
            dto.setLabel(dto.isConfigured() ? "未绑定" : "待开通");
        }
        return dto;
    }

    @Transactional
    public DingTalkBindStatusDto completeBind(String userId, String code, String state) {
        if (!properties.isConfigured()) {
            throw new IllegalStateException("钉钉绑定未配置");
        }
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("缺少授权码");
        }
        validateState(userId, state);

        String accessToken = exchangeCodeForToken(code.trim());
        JsonNode profile = fetchUserProfile(accessToken);

        String unionId = textOrNull(profile, "unionId");
        if (unionId == null || unionId.isEmpty()) {
            unionId = textOrNull(profile, "openId");
        }
        if (unionId == null || unionId.isEmpty()) {
            throw new IllegalStateException("未能获取钉钉用户标识");
        }

        Optional<MbDingtalkBind> existing = bindRepository.findByDingUnionId(unionId);
        if (existing.isPresent() && !existing.get().getUserId().equals(userId)) {
            throw new IllegalStateException("该钉钉账号已绑定其他用户");
        }

        MbDingtalkBind row = bindRepository.findById(userId).orElse(new MbDingtalkBind());
        row.setUserId(userId);
        row.setDingUnionId(unionId);
        row.setDingOpenId(textOrNull(profile, "openId"));
        row.setDingNick(firstNonEmpty(
                textOrNull(profile, "nick"),
                textOrNull(profile, "name"),
                "钉钉用户"));
        row.setBindTime(new Date());
        bindRepository.save(row);

        return getBindStatus(userId);
    }

    @Transactional
    public void unbind(String userId) {
        bindRepository.deleteById(userId);
    }

    public String formatLabel(String nick) {
        if (nick != null && !nick.trim().isEmpty()) {
            return "已绑定 · " + nick.trim();
        }
        return "已绑定";
    }

    private void validateState(String userId, String state) {
        if (state == null || state.trim().isEmpty()) {
            throw new IllegalArgumentException("缺少 state 参数");
        }
        StateEntry entry = pendingStates.remove(state.trim());
        if (entry == null) {
            throw new IllegalArgumentException("授权已过期，请重新发起绑定");
        }
        if (System.currentTimeMillis() - entry.createdAt > STATE_TTL_MS) {
            throw new IllegalArgumentException("授权已过期，请重新发起绑定");
        }
        if (!entry.userId.equals(userId)) {
            throw new IllegalArgumentException("授权用户不匹配");
        }
    }

    private String exchangeCodeForToken(String code) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> body = new java.util.HashMap<String, String>();
            body.put("clientId", properties.getClientId());
            body.put("clientSecret", properties.getClientSecret());
            body.put("code", code);
            body.put("grantType", "authorization_code");

            HttpEntity<Map<String, String>> entity = new HttpEntity<Map<String, String>>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(TOKEN_URL, entity, String.class);
            JsonNode json = objectMapper.readTree(response.getBody());

            String token = textOrNull(json, "accessToken");
            if (token == null) {
                token = textOrNull(json, "access_token");
            }
            if (token == null || token.isEmpty()) {
                throw new IllegalStateException("钉钉授权失败：" + extractError(json));
            }
            return token;
        } catch (HttpStatusCodeException ex) {
            throw new IllegalStateException("钉钉授权失败：" + parseHttpError(ex), ex);
        } catch (IllegalStateException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("钉钉授权失败：" + ex.getMessage(), ex);
        }
    }

    private JsonNode fetchUserProfile(String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-acs-dingtalk-access-token", accessToken);
            HttpEntity<Void> entity = new HttpEntity<Void>(headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    USER_ME_URL, HttpMethod.GET, entity, String.class);
            return objectMapper.readTree(response.getBody());
        } catch (HttpStatusCodeException ex) {
            throw new IllegalStateException("获取钉钉用户信息失败：" + parseHttpError(ex), ex);
        } catch (Exception ex) {
            throw new IllegalStateException("获取钉钉用户信息失败：" + ex.getMessage(), ex);
        }
    }

    private static String textOrNull(JsonNode node, String field) {
        if (node == null || !node.has(field) || node.get(field).isNull()) {
            return null;
        }
        return node.get(field).asText();
    }

    private static String firstNonEmpty(String... values) {
        for (String v : values) {
            if (v != null && !v.trim().isEmpty()) {
                return v.trim();
            }
        }
        return null;
    }

    private static String extractError(JsonNode json) {
        if (json == null) {
            return "未知错误";
        }
        String msg = textOrNull(json, "message");
        if (msg == null) {
            msg = textOrNull(json, "errmsg");
        }
        return msg != null ? msg : json.toString();
    }

    private String parseHttpError(HttpStatusCodeException ex) {
        try {
            JsonNode json = objectMapper.readTree(ex.getResponseBodyAsString());
            return extractError(json);
        } catch (Exception ignored) {
            return ex.getResponseBodyAsString();
        }
    }

    private void purgeExpiredStates() {
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<String, StateEntry>> it = pendingStates.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, StateEntry> e = it.next();
            if (now - e.getValue().createdAt > STATE_TTL_MS) {
                it.remove();
            }
        }
    }

    private static final class StateEntry {
        final String userId;
        final long createdAt;

        StateEntry(String userId, long createdAt) {
            this.userId = userId;
            this.createdAt = createdAt;
        }
    }
}
