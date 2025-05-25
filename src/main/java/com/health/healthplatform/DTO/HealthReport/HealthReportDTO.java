package com.health.healthplatform.DTO.HealthReport;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthReportDTO {
    private Long id;
    private Integer userId;
    private LocalDateTime reportTime;
    
    // 基础健康指标
    private Double bmi;
    private String bmiStatus;
    private Double weight;
    private Double height;
    
    // 血压指标
    private Double systolic;
    private Double diastolic;
    private String bloodPressureStatus;
    
    // 心率指标
    private Double heartRate;
    private String heartRateStatus;
    
    // 运动指标
    private Double weeklyExerciseDuration;
    private Integer weeklyExerciseCount;
    private Double weeklyCaloriesBurned;
    private Boolean exerciseGoalAchieved;
    
    // 步数指标
    private Integer dailySteps;
    private Double dailyDistance;
    private Boolean stepsGoalAchieved;
    
    // 睡眠指标
    private Double averageSleepDuration;
    private Double deepSleepPercentage;
    private Double lightSleepPercentage;
    private Double remSleepPercentage;
    private String sleepQuality;
    
    // 健康评分和建议
    private Integer overallScore;
    private List<String> healthSuggestions;
    private List<String> abnormalIndicators;
}