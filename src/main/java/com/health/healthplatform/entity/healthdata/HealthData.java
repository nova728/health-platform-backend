package com.health.healthplatform.entity.healthdata;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("health_data")
public class HealthData {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Integer heartRate;

    private Double sleepDuration;

    private String sleepQuality;

    private Integer steps;

    private Integer bloodPressureSystolic;

    private Integer bloodPressureDiastolic;

    private Double weight;

    private Double bmi;

    private Double height;

    private LocalDate recordDate;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}