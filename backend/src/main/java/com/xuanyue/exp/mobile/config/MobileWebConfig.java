package com.xuanyue.exp.mobile.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 将移动端构建产物托管在后端同一端口（8011），
 * 手机只需访问 http://&lt;电脑IP&gt;:8011/m/ 即可，避免 Vite 5174 端口问题。
 */
@Configuration
@ConditionalOnProperty(name = "app.mobile.enabled", havingValue = "true", matchIfMissing = true)
public class MobileWebConfig implements WebMvcConfigurer {

    private static final Logger log = LoggerFactory.getLogger(MobileWebConfig.class);

    private final MobileWebProperties properties;

    public MobileWebConfig(MobileWebProperties properties) {
        this.properties = properties;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path distPath = resolveDistPath();
        if (!Files.isDirectory(distPath)) {
            log.warn("移动端静态目录不存在: {}，请先执行 frontend/mobile 下 pnpm build:lan", distPath);
            return;
        }

        String prefix = normalizePrefix(properties.getUrlPrefix());
        String location = distPath.toAbsolutePath().normalize().toUri().toString();
        if (!location.endsWith("/")) {
            location += "/";
        }

        registry.addResourceHandler(prefix + "/**")
                .addResourceLocations(location)
                .setCachePeriod(0);

        log.info("移动端静态资源: {}/ -> {}", prefix, distPath.toAbsolutePath().normalize());
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        Path distPath = resolveDistPath();
        if (!Files.isDirectory(distPath)) {
            return;
        }
        String prefix = normalizePrefix(properties.getUrlPrefix());
        registry.addRedirectViewController(prefix, prefix + "/");
        registry.addViewController(prefix + "/").setViewName("forward:" + prefix + "/index.html");
    }

    private Path resolveDistPath() {
        String dir = properties.getStaticDir();
        if (!StringUtils.hasText(dir)) {
            dir = "../frontend/mobile/dist";
        }
        return Paths.get(dir).normalize();
    }

    private String normalizePrefix(String prefix) {
        if (!StringUtils.hasText(prefix)) {
            return "/m";
        }
        return prefix.startsWith("/") ? prefix : "/" + prefix;
    }
}
