package com.health.healthplatform.entity.DietRecord;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("daily_nutrition_summary")
public class NutritionSummary {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Integer userId;
    private LocalDate recordDate;
    private Double totalCalories;
    private Double totalCarbs;
    private Double totalProtein;
    private Double totalFat;

    // 推荐值
    private Double recommendedCalories;
    private Double recommendedCarbs;
    private Double recommendedProtein;
    private Double recommendedFat;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}