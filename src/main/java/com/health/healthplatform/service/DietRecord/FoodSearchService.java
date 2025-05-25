package com.health.healthplatform.service.DietRecord;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.health.healthplatform.DTO.DietRecord.FoodSearchResponseDTO;
import com.health.healthplatform.service.OAuth2Service;

import lombok.extern.slf4j.Slf4j;
@Service
@Slf4j
public class FoodSearchService {
    private final OAuth2Service oAuth2Service;
    private final String apiUrl;

    public FoodSearchService(OAuth2Service oAuth2Service, 
                           @Value("${fatsecret.api.base-url}") String apiUrl) {
        this.oAuth2Service = oAuth2Service;
        this.apiUrl = apiUrl;
    }

    public FoodSearchResponseDTO searchFood(String query, int pageNumber, int maxResults) {
        try {
            // 创建请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(oAuth2Service.getAccessToken());
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            // 创建请求参数
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("method", "foods.search.v3");
            params.add("search_expression", query);
            params.add("page_number", String.valueOf(pageNumber));
            params.add("max_results", String.valueOf(maxResults));
            params.add("format", "json");


            // 创建请求实体
            HttpEntity<MultiValueMap<String, String>> requestEntity = 
                new HttpEntity<>(params, headers);

            // 创建 RestTemplate 并添加日志拦截器
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.setInterceptors(Collections.singletonList(
                (request, body, execution) -> {
                    log.info("发送请求: URL={}, Headers={}, Body={}", 
                            request.getURI(), request.getHeaders(), new String((byte[]) body));
                    ClientHttpResponse response = execution.execute(request, body);
                    log.info("收到响应: Status={}, Headers={}", 
                            response.getStatusCode(), response.getHeaders());
                    return response;
                }
            ));

            // 发送请求
            ResponseEntity<String> response = restTemplate.exchange(
                apiUrl,
                HttpMethod.POST,
                requestEntity,
                String.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("API 请求失败: " + response.getStatusCode());
            }

            log.info("API 返回的数据： {}", response.getBody());

            // 将响应体转换为 FoodSearchResponseDTO
            ObjectMapper objectMapper = new ObjectMapper();
            FoodSearchResponseDTO foodSearchResponse = objectMapper.readValue(response.getBody(), FoodSearchResponseDTO.class);

            return foodSearchResponse;

        } catch (Exception e) {
            log.error("搜索食物失败: {}", e.getMessage(), e);
            throw new RuntimeException("搜索食物失败: " + e.getMessage(), e);
        }
    }
}
