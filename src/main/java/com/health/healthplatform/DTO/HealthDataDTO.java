package com.health.healthplatform.DTO;

import lombok.Data;
import javax.validation.constraints.*;
import java.time.LocalDate;

@Data
public class HealthDataDTO {
    @NotNull(message = "心率不能为空")
    @Min(value = 40, message = "心率不能低于40")
    @Max(value = 200, message = "心率不能超过200")
    private Integer heartRate;

    @NotNull(message = "睡眠时长不能为空")
    @DecimalMin(value = "0.0", message = "睡眠时长不能为负")
    @DecimalMax(value = "24.0", message = "睡眠时长不能超过24小时")
    private Double sleepDuration;

    @NotNull(message = "睡眠质量不能为空")
    private String sleepQuality;

    @Min(value = 0, message = "步数不能为负数")
    private Integer steps;

    @Min(value = 60, message = "收缩压不能低于60")
    @Max(value = 200, message = "收缩压不能超过200")
    private Integer bloodPressureSystolic;

    @Min(value = 40, message = "舒张压不能低于40")
    @Max(value = 140, message = "舒张压不能超过140")
    private Integer bloodPressureDiastolic;

    @DecimalMin(value = "20.0", message = "体重不能低于20kg")
    @DecimalMax(value = "300.0", message = "体重不能超过300kg")
    private Double weight;

    private Double height;

    @DecimalMin(value = "10.0", message = "BMI不能低于10")
    @DecimalMax(value = "50.0", message = "BMI不能超过50")
    private Double bmi;

    @NotNull(message = "记录日期不能为空")
    private LocalDate recordDate;
}