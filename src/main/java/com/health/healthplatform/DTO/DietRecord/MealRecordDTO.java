package com.health.healthplatform.DTO.DietRecord;

import java.security.PrivateKey;

import com.health.healthplatform.enums.MealType;
import lombok.Data;


@Data
public class MealRecordDTO {
    private Long id; // 主键   
    private Integer userId; // 用户ID 
    private MealType mealType; // 餐次类型
    private String foodId; // 食物ID
    private String foodName; // 食物名称
    private Double servingAmount; // 份量
    private String servingUnit; // 份量单位
    private Double calories; // 热量
    private Double carbs; // 碳水
    private Double protein; // 蛋白质
    private Double fat; // 脂肪
}
