package com.health.healthplatform.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "ernie.bot")
public class ErnieBotConfig {
    private String apiKey;
    private String secretKey;
}
