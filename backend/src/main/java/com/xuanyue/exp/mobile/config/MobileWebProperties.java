package com.xuanyue.exp.mobile.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.mobile")
public class MobileWebProperties {

    /** 是否启用移动端静态资源托管 */
    private boolean enabled = true;

    /** 移动端构建产物目录（相对 backend 工作目录） */
    private String staticDir = "../frontend/mobile/dist";

    /** 访问路径前缀，如 /m */
    private String urlPrefix = "/m";

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getStaticDir() { return staticDir; }
    public void setStaticDir(String staticDir) { this.staticDir = staticDir; }
    public String getUrlPrefix() { return urlPrefix; }
    public void setUrlPrefix(String urlPrefix) { this.urlPrefix = urlPrefix; }
}
