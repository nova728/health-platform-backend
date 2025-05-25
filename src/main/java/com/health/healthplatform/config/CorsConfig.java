package com.health.healthplatform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // 允许的前端源
        config.addAllowedOrigin("http://localhost:5173"); // Vue 开发服务器默认端口

        // 允许凭证
        config.setAllowCredentials(true);

        // 允许的请求头
        config.addAllowedHeader("*");

        // 允许的HTTP方法
        config.addAllowedMethod("*");

        // 暴露的响应头
        config.addExposedHeader("*");

        // 预检请求的有效期，单位为秒
        config.setMaxAge(3600L);

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}