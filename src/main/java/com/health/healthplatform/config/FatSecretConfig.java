package com.health.healthplatform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
@Configuration
@ConfigurationProperties(prefix = "fatsecret.api")
@Data
@Slf4j
public class FatSecretConfig {
    private String consumerKey;
    private String consumerSecret;
    private String clientId;
    private String clientSecret;
    private String baseUrl;
    private String baseUrlPath;
    private String scope = "basic premier";  // FatSecret API 要求的权限范围

    
    @PostConstruct
    public void init() {
        log.info("FatSecret配置已加载: baseUrl={}, consumerKey={}", baseUrl, consumerKey);
    }
}