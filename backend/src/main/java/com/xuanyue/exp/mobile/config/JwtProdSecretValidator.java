package com.xuanyue.exp.mobile.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 生产环境启动校验：JWT 密钥不得使用开发默认值。
 */
@Component
@Profile("prod")
public class JwtProdSecretValidator {

    static final String DEV_SECRET = "bs-lab-mobile-dev-secret-change-before-production-2026";

    private final JwtProperties jwtProperties;

    public JwtProdSecretValidator(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void validateOnStartup() {
        String secret = jwtProperties.getSecret();
        if (!StringUtils.hasText(secret)) {
            throw new IllegalStateException(
                    "生产环境必须设置 JWT_SECRET 环境变量（app.jwt.secret）");
        }
        if (DEV_SECRET.equals(secret) || secret.length() < 32) {
            throw new IllegalStateException(
                    "生产环境 JWT_SECRET 不得使用开发默认值，且长度不少于 32 字符");
        }
    }
}
