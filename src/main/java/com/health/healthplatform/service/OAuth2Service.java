package com.health.healthplatform.service;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.health.healthplatform.config.FatSecretConfig;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;
@Service
@Slf4j
public class OAuth2Service {
    private final String tokenUrl = "https://oauth.fatsecret.com/connect/token";
    private final FatSecretConfig config;
    private String accessToken;
    private LocalDateTime tokenExpiration;

    public OAuth2Service(FatSecretConfig config) {
        this.config = config;
    }

    public String getAccessToken() {
        if (accessToken == null || LocalDateTime.now().isAfter(tokenExpiration)) {
            refreshAccessToken();
        }
        return accessToken;
    }

    private void refreshAccessToken() {
        try {
            String credentials = Base64.getEncoder().encodeToString(
                (config.getClientId() + ":" + config.getClientSecret()).getBytes()
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("Authorization", "Basic " + credentials);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "client_credentials");
            body.add("scope", config.getScope());

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
            
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map> response = restTemplate.postForEntity(
                tokenUrl, 
                request, 
                Map.class
            );

            accessToken = (String) response.getBody().get("access_token");
            Integer expiresIn = (Integer) response.getBody().get("expires_in");
            tokenExpiration = LocalDateTime.now().plusSeconds(expiresIn - 60); // 提前60秒刷新
            
        } catch (Exception e) {
            log.error("获取访问令牌失败", e);
            throw new RuntimeException("获取访问令牌失败", e);
        }
    }
}
