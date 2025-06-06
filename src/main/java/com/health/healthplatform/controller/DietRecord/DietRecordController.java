package com.health.healthplatform.controller.DietRecord;

import com.health.healthplatform.DTO.DietRecord.MealRecordDTO;
import com.health.healthplatform.DTO.DietRecord.NutritionSummaryDTO;
import com.health.healthplatform.service.DietRecord.DietRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/diet")
@Slf4j
public class DietRecordController {
    private final DietRecordService dietRecordService;

    public DietRecordController(DietRecordService dietRecordService) {
        this.dietRecordService = dietRecordService;
    }

    @PostMapping("/{userId}/meals")
    public ResponseEntity<NutritionSummaryDTO> addMealRecord(
            @PathVariable Integer userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestBody MealRecordDTO mealRecord) {
        return ResponseEntity.ok(dietRecordService.addMealRecord(userId, date, mealRecord));
    }

    @GetMapping("/{userId}/nutrition/daily")
    public ResponseEntity<NutritionSummaryDTO> getDailyNutritionSummary(
            @PathVariable Integer userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(dietRecordService.getDailyNutritionSummary(userId, date));
    }

    @DeleteMapping("/{userId}/meals/{mealId}")
    public ResponseEntity<NutritionSummaryDTO> deleteMealRecord(
            @PathVariable Integer userId,
            @PathVariable Long mealId) {
        return ResponseEntity.ok(dietRecordService.deleteMealRecord(userId, mealId));
    }
}