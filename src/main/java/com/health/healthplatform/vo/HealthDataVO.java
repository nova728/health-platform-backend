package com.health.healthplatform.vo;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class HealthDataVO {
    private Long id;
    private Integer heartRate;
    private Double sleepDuration;
    private String sleepQuality;
    private Integer steps;
    private Integer bloodPressureSystolic;
    private Integer bloodPressureDiastolic;
    private Double weight;
    private Double bmi;
    private LocalDate recordDate;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}