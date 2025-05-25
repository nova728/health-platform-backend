package com.health.healthplatform.service.HealthReport;

import com.health.healthplatform.DTO.ExerciseGoalDTO;
import com.health.healthplatform.entity.HealthReport.HealthMetrics;
import com.health.healthplatform.entity.HealthReport.HealthReport;
import com.health.healthplatform.entity.healthdata.ExerciseGoal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class HealthReportAnalysisService {
    private final ObjectMapper objectMapper;

    public HealthReportAnalysisService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public HealthReport analyzeHealthData(Integer userId, HealthMetrics metrics, ExerciseGoalDTO exerciseGoal) {
        if (metrics == null) {
            throw new IllegalArgumentException("健康指标数据不能为空");
        }
        
        HealthReport report = new HealthReport();
        report.setUserId(userId);
        report.setAbnormalIndicators("[]");

        try {
            // 分析BMI
            analyzeBmi(metrics, report);
            
            // 分析血压
            analyzeBloodPressure(metrics, report);
            
            // 分析心率
            analyzeHeartRate(metrics, report);
            
            // 分析运动情况
            analyzeExercise(metrics, report, exerciseGoal);
            
            // 分析睡眠情况
            analyzeSleep(metrics, report);
            
            // 计算总体健康评分
            calculateOverallScore(report);
            
            // 生成健康建议
            generateHealthSuggestions(report);
            
            return report;
        } catch (Exception e) {
            log.error("分析健康数据时发生错误", e);
            throw new RuntimeException("健康数据分析失败: " + e.getMessage(), e);
        }
    }

    private void analyzeBmi(HealthMetrics metrics, HealthReport report) {
        List<String> abnormalIndicators = new ArrayList<>();
        
        if (metrics.getBmi() != null) {
            report.setBmi(metrics.getBmi());
            report.setWeight(metrics.getWeight());
            report.setHeight(metrics.getHeight());
            
            if (metrics.getBmi() < 18.5) {
                report.setBmiStatus("偏瘦");
                abnormalIndicators.add("体重偏轻");
            } else if (metrics.getBmi() < 24) {
                report.setBmiStatus("正常");
            } else if (metrics.getBmi() < 28) {
                report.setBmiStatus("超重");
                abnormalIndicators.add("体重超重");
            } else {
                report.setBmiStatus("肥胖");
                abnormalIndicators.add("体重肥胖");
            }
            
            try {
                report.setAbnormalIndicators(objectMapper.writeValueAsString(abnormalIndicators));
            } catch (Exception e) {
                log.error("转换异常指标列表失败", e);
                report.setAbnormalIndicators("[]");
            }
        }
    }

    private void analyzeBloodPressure(HealthMetrics metrics, HealthReport report) {
        List<String> abnormalIndicators = new ArrayList<>();
        
        if (metrics.getSystolic() != null && metrics.getDiastolic() != null) {
            report.setSystolic(metrics.getSystolic());
            report.setDiastolic(metrics.getDiastolic());
            
            if (metrics.getSystolic() < 90 || metrics.getDiastolic() < 60) {
                report.setBloodPressureStatus("低血压");
                abnormalIndicators.add("血压偏低");
            } else if (metrics.getSystolic() > 140 || metrics.getDiastolic() > 90) {
                report.setBloodPressureStatus("高血压");
                abnormalIndicators.add("血压偏高");
            } else {
                report.setBloodPressureStatus("正常");
            }
            
            try {
                report.setAbnormalIndicators(objectMapper.writeValueAsString(abnormalIndicators));
            } catch (Exception e) {
                log.error("转换异常指标列表失败", e);
                report.setAbnormalIndicators("[]");
            }
        }
    }

    private void analyzeHeartRate(HealthMetrics metrics, HealthReport report) {
        List<String> abnormalIndicators = new ArrayList<>();
        
        if (metrics.getHeartRate() != null) {
            report.setHeartRate(metrics.getHeartRate());
            
            if (metrics.getHeartRate() < 60) {
                report.setHeartRateStatus("偏低");
                abnormalIndicators.add("心率偏慢");
            } else if (metrics.getHeartRate() > 100) {
                report.setHeartRateStatus("偏高");
                abnormalIndicators.add("心率偏快");
            } else {
                report.setHeartRateStatus("正常");
            }
            
            try {
                report.setAbnormalIndicators(objectMapper.writeValueAsString(abnormalIndicators));
            } catch (Exception e) {
                log.error("转换异常指标列表失败", e);
                report.setAbnormalIndicators("[]");
            }
        }
    }

    private void analyzeExercise(HealthMetrics metrics, HealthReport report, ExerciseGoalDTO goal) {
        if (metrics.getWeeklyExerciseDuration() != null) {
            report.setWeeklyExerciseDuration(metrics.getWeeklyExerciseDuration());
            report.setWeeklyExerciseCount(metrics.getWeeklyExerciseCount());
            report.setWeeklyCaloriesBurned(metrics.getWeeklyCaloriesBurned());
            
            boolean goalAchieved = true;
            if (goal != null) {
                if (metrics.getWeeklyExerciseDuration() < goal.getWeeklyDurationGoal()) goalAchieved = false;
                if (metrics.getWeeklyExerciseCount() < goal.getWeeklyCountGoal()) goalAchieved = false;
                if (metrics.getWeeklyCaloriesBurned() < goal.getWeeklyCaloriesGoal()) goalAchieved = false;
            }
            report.setExerciseGoalAchieved(goalAchieved);
        }
    }

    private void analyzeSleep(HealthMetrics metrics, HealthReport report) {
        List<String> abnormalIndicators = new ArrayList<>();
        
        if (metrics.getAverageSleepDuration() != null) {
            report.setAverageSleepDuration(metrics.getAverageSleepDuration());
            report.setDeepSleepPercentage(metrics.getDeepSleepPercentage());
            report.setLightSleepPercentage(metrics.getLightSleepPercentage());
            report.setRemSleepPercentage(metrics.getRemSleepPercentage());
            
            int sleepScore = 0;
            if (metrics.getAverageSleepDuration() >= 7) {
                sleepScore += 40;  // 睡眠时长达标
            }
            
            if (metrics.getDeepSleepPercentage() != null && metrics.getDeepSleepPercentage() >= 25) {
                sleepScore += 30;  // 深睡眠比例良好
            }
            
            if (metrics.getRemSleepPercentage() != null && metrics.getRemSleepPercentage() >= 20) {
                sleepScore += 30;  // REM睡眠比例良好
            }
            
            if (sleepScore >= 80) {
                report.setSleepQuality("优");
            } else if (sleepScore >= 60) {
                report.setSleepQuality("良");
            } else {
                report.setSleepQuality("差");
                abnormalIndicators.add("睡眠质量差");
            }
            
            if (metrics.getAverageSleepDuration() < 7) {
                abnormalIndicators.add("睡眠时间不足");
            }
            
            try {
                report.setAbnormalIndicators(objectMapper.writeValueAsString(abnormalIndicators));
            } catch (Exception e) {
                log.error("转换异常指标列表失败", e);
                report.setAbnormalIndicators("[]");
            }
        } else {
            log.warn("睡眠数据缺失");
            report.setSleepQuality("数据缺失");
        }
    }

    private void calculateOverallScore(HealthReport report) {
        int score = 100;
        List<String> abnormalIndicators = new ArrayList<>();

        // BMI评分（-20分）
        if ("偏瘦".equals(report.getBmiStatus())) {
            score -= 10;
            abnormalIndicators.add("体重偏低");
        } else if ("超重".equals(report.getBmiStatus())) {
            score -= 15;
            abnormalIndicators.add("体重超重");
        } else if ("肥胖".equals(report.getBmiStatus())) {
            score -= 20;
            abnormalIndicators.add("体重肥胖");
        }

        // 血压评分（-20分）
        if ("高血压".equals(report.getBloodPressureStatus())) {
            score -= 20;
            abnormalIndicators.add("血压偏高");
        } else if ("低血压".equals(report.getBloodPressureStatus())) {
            score -= 15;
            abnormalIndicators.add("血压偏低");
        }

        // 心率评分（-15分）
        if ("偏高".equals(report.getHeartRateStatus())) {
            score -= 15;
            abnormalIndicators.add("心率偏快");
        } else if ("偏低".equals(report.getHeartRateStatus())) {
            score -= 10;
            abnormalIndicators.add("心率偏慢");
        }

        // 运动评分（-15分）
        if (Boolean.FALSE.equals(report.getExerciseGoalAchieved())) {
            score -= 15;
            abnormalIndicators.add("运动量不足");
        }

        // 睡眠评分（-30分）
        if ("差".equals(report.getSleepQuality())) {
            score -= 30;
            abnormalIndicators.add("睡眠质量差");
        } else if ("良".equals(report.getSleepQuality())) {
            score -= 10;
        }

        if ("数据缺失".equals(report.getBmiStatus())) score -= 5;
        if ("数据缺失".equals(report.getBloodPressureStatus())) score -= 5;
        if ("数据缺失".equals(report.getHeartRateStatus())) score -= 5;
        if ("数据缺失".equals(report.getSleepQuality())) score -= 5;

        report.setOverallScore(Math.max(0, score));
        try {
            report.setAbnormalIndicators(objectMapper.writeValueAsString(abnormalIndicators));
        } catch (Exception e) {
            log.error("Error converting abnormal indicators to JSON", e);
        }
    }

    private void generateHealthSuggestions(HealthReport report) {
        List<String> suggestions = new ArrayList<>();

        // 根据BMI生成建议
        if ("偏瘦".equals(report.getBmiStatus())) {
            suggestions.add("建议适当增加饮食摄入，注意营养均衡");
            suggestions.add("可以进行力量训练来增加肌肉量");
        } else if ("超重".equals(report.getBmiStatus()) || "肥胖".equals(report.getBmiStatus())) {
            suggestions.add("建议控制饮食摄入，减少高热量食物");
            suggestions.add("建议每天进行30分钟以上的有氧运动");
        }

        // 根据血压生成建议
        if ("高血压".equals(report.getBloodPressureStatus())) {
            suggestions.add("建议限制盐分摄入，保持清淡饮食");
            suggestions.add("避免剧烈运动，建议进行适度的有氧运动");
            suggestions.add("建议定期监测血压，必要时就医");
        } else if ("低血压".equals(report.getBloodPressureStatus())) {
            suggestions.add("建议适当增加盐分摄入");
            suggestions.add("避免突然起立，注意循序渐进");
        }

        // 根据心率生成建议
        if ("偏高".equals(report.getHeartRateStatus())) {
            suggestions.add("建议避免剧烈运动和情绪波动");
            suggestions.add("可以尝试冥想或深呼吸来调节心率");
        } else if ("偏低".equals(report.getHeartRateStatus())) {
            suggestions.add("建议适当增加运动量，提高心肺功能");
        }

        // 根据运动情况生成建议
        if (Boolean.FALSE.equals(report.getExerciseGoalAchieved())) {
            suggestions.add("建议制定合理的运动计划，循序渐进");
            suggestions.add("可以尝试找到喜欢的运动方式，坚持锻炼");
        }

        // 根据睡眠情况生成建议
        if ("差".equals(report.getSleepQuality())) {
            suggestions.add("建议保持规律的作息时间");
            suggestions.add("睡前避免使用电子设备和剧烈运动");
            suggestions.add("可以尝试睡前热水澡或冥想来改善睡眠");
        }

        try {
            report.setHealthSuggestions(objectMapper.writeValueAsString(suggestions));
        } catch (Exception e) {
            log.error("Error converting health suggestions to JSON", e);
        }
    }
}