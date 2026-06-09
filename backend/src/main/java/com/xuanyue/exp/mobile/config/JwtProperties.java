package com.xuanyue.exp.mobile.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    private String secret = "bs-lab-mobile-dev-secret-change-before-production-2026";
    private long accessTokenSeconds = 7200L;
    private long refreshTokenSeconds = 2592000L;
    private String issuer = "bs-lab-mobile";

    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }
    public long getAccessTokenSeconds() { return accessTokenSeconds; }
    public void setAccessTokenSeconds(long accessTokenSeconds) { this.accessTokenSeconds = accessTokenSeconds; }
    public long getRefreshTokenSeconds() { return refreshTokenSeconds; }
    public void setRefreshTokenSeconds(long refreshTokenSeconds) { this.refreshTokenSeconds = refreshTokenSeconds; }
    public String getIssuer() { return issuer; }
    public void setIssuer(String issuer) { this.issuer = issuer; }
}
