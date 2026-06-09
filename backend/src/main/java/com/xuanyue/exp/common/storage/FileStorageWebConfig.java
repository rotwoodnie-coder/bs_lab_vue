package com.xuanyue.exp.common.storage;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class FileStorageWebConfig implements WebMvcConfigurer {

    private final FileStorageProperties properties;

    public FileStorageWebConfig(FileStorageProperties properties) {
        this.properties = properties;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping(normalizeUrl(properties.getUrlPrefix()) + "/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "HEAD", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Content-Disposition", "Content-Length")
                .allowCredentials(false);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadDir = Paths.get(properties.getUploadDir()).toAbsolutePath().normalize().toUri().toString();
        String urlPrefix = normalizeUrl(properties.getUrlPrefix()) + "/**";
        registry.addResourceHandler(urlPrefix).addResourceLocations(uploadDir);
    }

    private String normalizeUrl(String prefix) {
        if (prefix == null || prefix.trim().isEmpty()) {
            return "/uploads";
        }
        return prefix.startsWith("/") ? prefix : "/" + prefix;
    }
}
