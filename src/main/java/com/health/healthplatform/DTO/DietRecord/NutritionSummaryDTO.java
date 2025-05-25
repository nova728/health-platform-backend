package com.health.healthplatform.DTO.DietRecord;

import java.util.List;
import com.health.healthplatform.DTO.DietRecord.MealRecordDTO;

import lombok.Data;

@Data
public class NutritionSummaryDTO {
    private Double totalCalories; // 总热量
    private Double totalCarbs; // 总碳水
    private Double totalProtein; // 总蛋白质
    private Double totalFat; // 总脂肪

    private List<MealRecordDTO> meals;
    
    // 推荐值
    private Double recommendedCalories = 2000.0;
    private Double recommendedCarbs = 250.0;
    private Double recommendedProtein = 60.0;
    private Double recommendedFat = 70.0;
    
    // 完成百分比
    private Long caloriesPercentage;
    private Long carbsPercentage;
    private Long proteinPercentage; 
    private Long fatPercentage;

    // 计算百分比
    public void calculatePercentages() {
        if (totalCalories != null && recommendedCalories != null && recommendedCalories != 0) {
            this.caloriesPercentage = Math.round(totalCalories / recommendedCalories * 100.0);
        } else {
            this.caloriesPercentage = 0L;
        }
    
        if (totalCarbs != null && recommendedCarbs != null && recommendedCarbs != 0) {
            this.carbsPercentage = Math.round(totalCarbs / recommendedCarbs * 100.0);
        } else {    
            this.carbsPercentage = 0L;
        }
    
        if (totalProtein != null && recommendedProtein != null && recommendedProtein != 0) {
            this.proteinPercentage = Math.round(totalProtein / recommendedProtein * 100.0);
        } else {
            this.proteinPercentage = 0L;
        }
    
        if (totalFat != null && recommendedFat != null && recommendedFat != 0) {
            this.fatPercentage = Math.round(totalFat / recommendedFat * 100.0);
        } else {
            this.fatPercentage = 0L;
        }
    }

    public void setMeals(List<MealRecordDTO> meals) {
        this.meals = meals;
    }


}
