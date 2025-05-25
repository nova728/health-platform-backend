//用来存储收集到的健康数据
package com.health.healthplatform.entity.HealthReport;

import lombok.Data;

@Data
public class HealthMetrics {
    // 基础健康指标
    private Double bmi;
    private Double weight;
    private Double height;
    
    // 血压指标
    private Double systolic;
    private Double diastolic;
    
    // 心率指标
    private Double heartRate;
    
    // 运动指标
    private Double weeklyExerciseDuration;
    private Integer weeklyExerciseCount;
    private Double weeklyCaloriesBurned;
    
    // 步数指标
    private Integer dailySteps;
    private Double dailyDistance;
    
    // 睡眠指标
    private Double averageSleepDuration;
    private Double deepSleepPercentage;
    private Double lightSleepPercentage;
    private Double remSleepPercentage;
}