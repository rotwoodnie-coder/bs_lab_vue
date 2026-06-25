package com.xuanyue.exp.mobile.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xuanyue.exp.common.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Component
public class MobileJwtAuthFilter extends OncePerRequestFilter {

    private static final List<String> MOBILE_PUBLIC_PATHS = Arrays.asList(
            "/api/mobile/auth/login",
            "/api/mobile/auth/refresh",
            "/api/mobile/auth/login-name/available",
            "/api/mobile/auth/parent/register",
            "/api/mobile/auth/logout",
            // <img>/<video> 无法携带 JWT，媒体预览须匿名可读
            "/api/mobile/files/preview"
    );

    private static final String ORG_PREFIX = "/api/mobile/org/";

    private final MobileAuthTokenService tokenService;
    private final ObjectMapper objectMapper;

    public MobileJwtAuthFilter(MobileAuthTokenService tokenService, ObjectMapper objectMapper) {
        this.tokenService = tokenService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        String bearer = extractBearerToken(request);

        if (path.startsWith("/api/mobile/")) {
            if (isMobilePublicPath(path)) {
                filterChain.doFilter(request, response);
                return;
            }
            MobileAuthUser user = bearer == null ? null : tokenService.parseAccessToken(bearer);
            if (user == null) {
                writeUnauthorized(response, bearer == null ? "请先登录" : "登录已过期，请重新登录");
                return;
            }
            filterChain.doFilter(new MobileAuthRequestWrapper(request, user), response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isMobilePublicPath(String path) {
        for (String publicPath : MOBILE_PUBLIC_PATHS) {
            if (path.equals(publicPath)) {
                return true;
            }
        }
        return path.startsWith(ORG_PREFIX);
    }

    private String extractBearerToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (!StringUtils.hasText(header) || !header.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return null;
        }
        String token = header.substring(7).trim();
        return token.isEmpty() ? null : token;
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiResponse<Void> body = ApiResponse.fail(401, message);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
