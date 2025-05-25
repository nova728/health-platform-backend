package com.health.healthplatform.DTO.DietRecord;

import lombok.Data;
import java.time.LocalDate;

@Data
public class DailyNutritionDTO {
    private LocalDate date;
    private Double totalCalories;
    private Double totalCarbs;
    private Double totalProtein;
    private Double totalFat;
    private Double recommendedCalories;
    private Double recommendedCarbs;
    private Double recommendedProtein;
    private Double recommendedFat;
    private Double caloriesPercentage;
    private Double carbsPercentage;
    private Double proteinPercentage;
    private Double fatPercentage;
}
