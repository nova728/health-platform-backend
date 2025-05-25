// src/main/java/com/health/healthplatform/DTO/DietRecord/NutritionStatsDTO.java
package com.health.healthplatform.DTO.DietRecord;

import lombok.Data;
import java.util.List;

@Data
public class NutritionStatsDTO {
    private List<String> dates;
    private List<Double> calories;
    private List<Double> carbs;
    private List<Double> protein;
    private List<Double> fat;
    private List<Double> recommendedCalories;
    private List<Double> recommendedCarbs;
    private List<Double> recommendedProtein;
    private List<Double> recommendedFat;
}