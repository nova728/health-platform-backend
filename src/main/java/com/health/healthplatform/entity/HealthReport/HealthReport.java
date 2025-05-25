package com.health.healthplatform.entity.HealthReport;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("health_reports")
public class HealthReport {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Integer userId;
    private LocalDateTime reportTime;
    
    // 基础健康指标
    private Double bmi;
    private String bmiStatus;  // 偏瘦、正常、超重、肥胖
    private Double weight;
    private Double height;
    
    // 血压指标
    private Double systolic;
    private Double diastolic;
    private String bloodPressureStatus;  // 低血压、正常、高血压
    
    // 心率指标
    private Double heartRate;
    private String heartRateStatus;  // 偏低、正常、偏高
    
    // 运动指标
    private Double weeklyExerciseDuration;  // 每周运动时长(分钟)
    private Integer weeklyExerciseCount;    // 每周运动次数
    private Double weeklyCaloriesBurned;    // 每周消耗卡路里
    private Boolean exerciseGoalAchieved;   // 是否达到运动目标
    
    // 步数指标
    private Integer dailySteps;
    private Double dailyDistance;
    private Boolean stepsGoalAchieved;
    
    // 睡眠指标
    private Double averageSleepDuration;
    private Double deepSleepPercentage;
    private Double lightSleepPercentage;
    private Double remSleepPercentage;
    private String sleepQuality;  // 优、良、差
    
    // 健康评分和建议
    private Integer overallScore;           // 总分(0-100)
    private String healthSuggestions;       // 健康建议(JSON字符串)
    private String abnormalIndicators;      // 异常指标(JSON字符串)
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
}