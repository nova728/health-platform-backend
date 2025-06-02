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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
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
    }

    public FoodRecognitionResponseDTO recognizeFood(String imagePath) {
        try {
            // 1. 将图片文件转换为Base64编码字符串
            String base64Image = convertImageToBase64(imagePath);

            // 2. 调用API识别食物
            return callFoodRecognitionAPI(base64Image);
        } catch (Exception e) {
            log.error("食物图片处理失败", e);
            throw new RuntimeException("食物图片处理失败: " + e.getMessage());
        }
    }

    private String convertImageToBase64(String imagePath) throws IOException {
        File imageFile = new File(imagePath);
        if (!imageFile.exists()) {
            throw new IOException("图片文件不存在: " + imagePath);
        }

        // 读取文件内容
        byte[] fileContent = Files.readAllBytes(imageFile.toPath());

        // 编码为Base64
        return Base64.getEncoder().encodeToString(fileContent);
    }    private FoodRecognitionResponseDTO callFoodRecognitionAPI(String base64Image) {
        // 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // 设置请求参数
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("key", API_KEY);
        params.add("img", base64Image);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, headers);

        // 发送请求
        ResponseEntity<String> response = restTemplate.exchange(
                API_URL,
                HttpMethod.POST,
                requestEntity,
                String.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("食物识别API请求失败: " + response.getStatusCode());
        }

        try {
            // 解析API响应
            ObjectMapper mapper = new ObjectMapper();
            TianApiResponse apiResponse = mapper.readValue(response.getBody(), TianApiResponse.class);

            if (apiResponse.getCode() != 200) {
                throw new RuntimeException("食物识别失败: " + apiResponse.getMsg());
            }

            // 转换为内部DTO
            FoodRecognitionResponseDTO result = new FoodRecognitionResponseDTO();
            result.setFoods(apiResponse.getResult().getList().stream()
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
                    .collect(Collectors.toList()));

            return result;

        } catch (Exception e) {
            log.error("食物识别结果解析失败", e);
            throw new RuntimeException("食物识别结果解析失败: " + e.getMessage());
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