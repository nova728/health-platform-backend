package com.health.healthplatform.controller.DietRecord;

import java.util.Collections;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.health.healthplatform.DTO.DietRecord.FoodRecognitionResponseDTO;
import com.health.healthplatform.DTO.DietRecord.FoodSearchResponseDTO;
import com.health.healthplatform.service.DietRecord.FoodImageRecognitionService;
import com.health.healthplatform.service.DietRecord.FoodSearchService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/foods")
@Slf4j
public class FoodController {
    private final FoodSearchService foodSearchService;
    private final FoodImageRecognitionService foodImageRecognitionService;

    public FoodController(FoodSearchService foodSearchService,
                          FoodImageRecognitionService foodImageRecognitionService) {
        this.foodSearchService = foodSearchService;
        this.foodImageRecognitionService = foodImageRecognitionService;
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchFood(
            @RequestParam String query,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        try {
            if (query == null || query.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Collections.singletonMap("error", "搜索关键词不能为空"));
            }

            if (page < 1 || size < 1) {
                return ResponseEntity.badRequest()
                        .body(Collections.singletonMap("error", "页码和大小必须大于0"));
            }

            FoodSearchResponseDTO result = foodSearchService.searchFood(query, page, size);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("搜索处理失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "搜索处理失败: " + e.getMessage()));
        }
    }

    // 新增图片识别端点
    @PostMapping("/recognize")
    public ResponseEntity<?> recognizeFoodFromImage(@RequestBody Map<String, String> request) {
        String base64Image = request.get("image");
        if (base64Image == null || base64Image.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Collections.singletonMap("error", "图片数据不能为空"));
        }

        try {
            FoodRecognitionResponseDTO result = foodImageRecognitionService.recognizeFood(base64Image);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("食物识别失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "食物识别失败: " + e.getMessage()));
        }
    }
}