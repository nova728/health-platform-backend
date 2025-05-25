package com.health.healthplatform.config;

import com.health.healthplatform.enums.MealType;

import org.apache.ibatis.type.EnumTypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyBatisPlusConfig {

    @Bean
    public ConfigurationCustomizer configurationCustomizer() {
        return configuration -> {
            TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
            // 注册基于字符串的枚举处理器
            typeHandlerRegistry.register(MealType.class, new EnumTypeHandler<>(MealType.class));
        };
    }
}