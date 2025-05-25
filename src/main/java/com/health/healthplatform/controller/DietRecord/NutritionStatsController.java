package com.health.healthplatform.controller.DietRecord;

import com.health.healthplatform.DTO.DietRecord.NutritionStatsDTO;
import com.health.healthplatform.DTO.DietRecord.DailyNutritionDTO;
import com.health.healthplatform.service.DietRecord.NutritionStatsService;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/diet")
@Slf4j
public class NutritionStatsController {
    private final NutritionStatsService nutritionStatsService;

    public NutritionStatsController(NutritionStatsService nutritionStatsService) {
        this.nutritionStatsService = nutritionStatsService;
    }

    @GetMapping("/{userId}/nutrition/stats")
    public ResponseEntity<NutritionStatsDTO> getNutritionStats(
            @PathVariable Integer userId,
            @RequestParam(defaultValue = "week") String range) {
        log.info("获取用户 {} 的营养统计数据，时间范围：{}", userId, range);
        NutritionStatsDTO stats = nutritionStatsService.getStats(userId, range);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/{userId}/nutrition/monthly")
    public ResponseEntity<List<DailyNutritionDTO>> getMonthlyNutrition(
            @PathVariable Integer userId,
            @RequestParam(required = true) Integer year,
            @RequestParam(required = true) Integer month) {
        log.info("获取用户 {} 的 {}-{} 月度营养数据", userId, year, month);
        List<DailyNutritionDTO> monthlyData = nutritionStatsService.getMonthlyNutrition(userId, year, month);
        return ResponseEntity.ok(monthlyData);
    }
}