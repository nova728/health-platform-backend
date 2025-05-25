package com.health.healthplatform.entity.DietRecord;

import com.baomidou.mybatisplus.annotation.*;
import com.health.healthplatform.enums.MealType;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("meal_records")
public class MealRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long nutritionSummaryId; // 每日饮食记录ID，用于关联每日饮食记录
    @EnumValue
    private MealType mealType; // 餐次类型
    private Integer userId; // 用户ID
    private String foodId; // 食物ID
    private String foodName; // 食物名称
    private Double servingAmount; // 份量
    private String servingUnit; // 份量单位
    private Double calories; // 热量
    private Double carbs; // 碳水
    private Double protein; // 蛋白质
    private Double fat; // 脂肪
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
