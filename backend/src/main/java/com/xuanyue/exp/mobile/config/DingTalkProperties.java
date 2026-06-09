package com.xuanyue.exp.mobile.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.URI;

@Component
@ConfigurationProperties(prefix = "app.dingtalk")
public class DingTalkProperties {

    private boolean enabled = false;
    private String clientId = "";
    private String clientSecret = "";
    /** 默认移动端 H5 根地址，如 http://10.0.0.1:5174 或 http://10.0.0.1:8011/m */
    private String frontendOrigin = "http://localhost:5174";

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    public String getClientSecret() { return clientSecret; }
    public void setClientSecret(String clientSecret) { this.clientSecret = clientSecret; }
    public String getFrontendOrigin() { return frontendOrigin; }
    public void setFrontendOrigin(String frontendOrigin) { this.frontendOrigin = frontendOrigin; }

    public boolean isConfigured() {
        return enabled
                && clientId != null && !clientId.trim().isEmpty()
                && clientSecret != null && !clientSecret.trim().isEmpty();
    }

    /** OAuth 回调完整地址，须与钉钉开放平台登记一致 */
    public String redirectUri() {
        return redirectUri(null);
    }

    public String redirectUri(String redirectBase) {
        String base = normalizeBase(pickRedirectBase(redirectBase));
        return base + "/#/settings/dingtalk/callback";
    }

    private String pickRedirectBase(String redirectBase) {
        if (isAllowedRedirectBase(redirectBase)) {
            return redirectBase.trim();
        }
        return frontendOrigin == null ? "" : frontendOrigin.trim();
    }

    private boolean isAllowedRedirectBase(String redirectBase) {
        if (!StringUtils.hasText(redirectBase)) {
            return false;
        }
        try {
            URI uri = URI.create(redirectBase.trim());
            if (!"http".equalsIgnoreCase(uri.getScheme()) && !"https".equalsIgnoreCase(uri.getScheme())) {
                return false;
            }
            String host = uri.getHost();
            if (!StringUtils.hasText(host)) {
                return false;
            }
            if ("localhost".equalsIgnoreCase(host) || "127.0.0.1".equals(host)) {
                return true;
            }
            if (host.startsWith("10.") || host.startsWith("192.168.")) {
                return true;
            }
            if (host.startsWith("172.")) {
                String[] parts = host.split("\\.");
                if (parts.length >= 2) {
                    try {
                        int second = Integer.parseInt(parts[1]);
                        if (second >= 16 && second <= 31) {
                            return true;
                        }
                    } catch (NumberFormatException ignored) {
                        return false;
                    }
                }
            }
            String configuredHost = extractHost(frontendOrigin);
            return StringUtils.hasText(configuredHost) && configuredHost.equalsIgnoreCase(host);
        } catch (Exception e) {
            return false;
        }
    }

    private static String extractHost(String url) {
        if (!StringUtils.hasText(url)) {
            return null;
        }
        try {
            return URI.create(url.trim()).getHost();
        } catch (Exception e) {
            return null;
        }
    }

    private static String normalizeBase(String base) {
        String value = base == null ? "" : base.trim();
        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }
}
