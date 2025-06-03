package com.health.healthplatform.service.DietRecord;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.health.healthplatform.DTO.DietRecord.FoodRecognitionDTO;
import com.health.healthplatform.DTO.DietRecord.FoodRecognitionResponseDTO;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FoodImageRecognitionService {
    @Value("${food.api.url}")
    private String API_URL;
    @Value("${food.api.key}")
    private String API_KEY;
    
    private final RestTemplate restTemplate;
    
    public FoodImageRecognitionService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }    public FoodRecognitionResponseDTO recognizeFood(String base64Image) {
        try {
            log.info("开始食物识别，图片数据长度: {}", base64Image != null ? base64Image.length() : 0);
            // 直接使用传入的base64图片数据调用API识别食物
            return callFoodRecognitionAPI(base64Image);
        } catch (Exception e) {
            log.error("食物图片处理失败", e);
            throw new RuntimeException("食物图片处理失败: " + e.getMessage());
        }
    }    private FoodRecognitionResponseDTO callFoodRecognitionAPI(String base64Image) {
        try {
            log.info("调用食物识别API，API_URL: {}, API_KEY是否配置: {}", API_URL, API_KEY != null && !API_KEY.isEmpty());
            
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            // 设置请求参数
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("key", API_KEY);
            params.add("img", base64Image);

            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, headers);

            log.info("发送请求到食物识别API...");
            // 发送请求
            ResponseEntity<String> response = restTemplate.exchange(
                    API_URL,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            log.info("API响应状态码: {}", response.getStatusCode());
            log.info("API响应内容: {}", response.getBody());

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("食物识别API请求失败: " + response.getStatusCode());
            }

        try {
            // 解析API响应
            ObjectMapper mapper = new ObjectMapper();
            TianApiResponse apiResponse = mapper.readValue(response.getBody(), TianApiResponse.class);

            if (apiResponse.getCode() != 200) {
                throw new RuntimeException("食物识别失败: " + apiResponse.getMsg());
            }            // 检查识别结果是否为有效食物
            List<TianApiResponse.Result.FoodItem> foodItems = apiResponse.getResult().getList();
            if (foodItems.isEmpty()) {
                throw new RuntimeException("未识别出任何食物，请确保图片清晰且包含食物");
            }
            
            // 检查是否识别为"非菜"或其他无效结果
            TianApiResponse.Result.FoodItem firstItem = foodItems.get(0);
            if ("非菜".equals(firstItem.getName()) || !firstItem.getHas_calorie()) {
                throw new RuntimeException("图片中未识别出有效的食物，请重新拍摄包含食物的图片");
            }

            // 转换为内部DTO
            FoodRecognitionResponseDTO result = new FoodRecognitionResponseDTO();            result.setFoods(foodItems.stream()
                    .filter(item -> !"非菜".equals(item.getName()) && item.getHas_calorie()) // 过滤掉无效结果
                    .map(item -> {
                        FoodRecognitionDTO dto = new FoodRecognitionDTO();
                        dto.setName(item.getName());
                        try {
                            dto.setCalorie(item.getCalorie() != null && !item.getCalorie().isEmpty() ?
                                    Double.parseDouble(item.getCalorie()) : 0.0);
                        } catch (NumberFormatException e) {
                            dto.setCalorie(0.0);
                        }
                        dto.setTrust(Double.parseDouble(item.getTrust()));
                        return dto;
                    })
                    .collect(Collectors.toList()));return result;

        } catch (Exception e) {
            log.error("食物识别结果解析失败", e);
            throw new RuntimeException("食物识别结果解析失败: " + e.getMessage());
        }
        } catch (Exception e) {
            log.error("食物识别API调用失败", e);
            throw new RuntimeException("食物识别API调用失败: " + e.getMessage());
        }
    }

    // 内部类用于解析API响应
    @Data
    private static class TianApiResponse {
        private Integer code;
        private String msg;
        private Result result;

        @Data
        static class Result {
            private List<FoodItem> list;

            @Data
            static class FoodItem {
                private String name;
                private String calorie;
                private Boolean has_calorie;
                private String trust;
            }
        }
    }
}