package com.health.healthplatform;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

@SpringBootApplication
@ComponentScan(basePackages = {
		"com.health.healthplatform.controller",
		"com.health.healthplatform.service",
		"com.health.healthplatform.config",
		"com.health.healthplatform.websocket"  // 添加这个包路径
})
@MapperScan("com.health.healthplatform.mapper")
public class HealthplatformApplication {

	public static void main(String[] args) {
		SpringApplication.run(HealthplatformApplication.class, args);
	}

	@Bean
	public Jackson2ObjectMapperBuilderCustomizer jacksonObjectMapperCustomization() {
		return jacksonObjectMapperBuilder ->
				jacksonObjectMapperBuilder.timeZone(TimeZone.getTimeZone("GMT+8"))
						.dateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));
	}
}
