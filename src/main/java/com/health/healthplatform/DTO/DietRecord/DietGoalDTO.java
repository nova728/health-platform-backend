package com.health.healthplatform.DTO.DietRecord;

import lombok.Data;

/**
 * 饮食目标DTO
 */
@Data
public class DietGoalDTO {
    
    /**
     * 每日热量目标 (kcal)
     */
    private Double calories;
    
    /**
     * 每日碳水化合物目标 (g)
     */
    private Double carbs;
    
    /**
     * 每日蛋白质目标 (g)
     */
    private Double protein;
    
    /**
     * 每日脂肪目标 (g)
     */
    private Double fat;
    
    /**
     * 备注说明
     */
    private String note;
    
    /**
     * 目标类型 (lose-weight, maintain, gain-muscle, custom)
     */
    private String goalType;
}
