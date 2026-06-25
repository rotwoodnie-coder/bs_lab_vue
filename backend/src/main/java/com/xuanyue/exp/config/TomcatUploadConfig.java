package com.xuanyue.exp.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 放开 Tomcat 表单 POST 体积限制（默认 2MB）。
 * 超过后 Tomcat 直接断开连接，Nginx 侧表现为 502。
 */
@Configuration
public class TomcatUploadConfig {

    private static final Logger log = LoggerFactory.getLogger(TomcatUploadConfig.class);

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> tomcatUploadCustomizer() {
        return factory -> factory.addConnectorCustomizers(connector -> {
            connector.setMaxPostSize(-1);
            log.info("Tomcat upload limit: maxPostSize=unlimited");
        });
    }
}
