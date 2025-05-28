package com.health.healthplatform.service.HealthReport;

import com.health.healthplatform.DTO.ExerciseGoalDTO;
import com.health.healthplatform.entity.HealthReport.HealthMetrics;
import com.health.healthplatform.entity.HealthReport.HealthReport;
import com.health.healthplatform.entity.healthdata.ExerciseGoal;
import com.health.healthplatform.mapper.HealthReport.HealthReportMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class HealthReportAnalysisService {
    private final ObjectMapper objectMapper;
    
    @Autowired
    private HealthReportMapper healthReportMapper;
    
    @Autowired
    private HealthDataCollectorService healthDataCollectorService;

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
    }    private void analyzeBloodPressure(HealthMetrics metrics, HealthReport report) {
        List<String> abnormalIndicators = new ArrayList<>();
        if (metrics.getSystolic() != null && metrics.getDiastolic() != null) {
            report.setSystolic(metrics.getSystolic());
            report.setDiastolic(metrics.getDiastolic());
            
            // 改进血压状态判断
            String bloodPressureStatus = getBloodPressureStatus(metrics.getSystolic(), metrics.getDiastolic());
            report.setBloodPressureStatus(bloodPressureStatus);
            
            if (!"正常".equals(bloodPressureStatus)) {
                abnormalIndicators.add("血压: " + bloodPressureStatus);
            }
            
            try {
                report.setAbnormalIndicators(objectMapper.writeValueAsString(abnormalIndicators));
            } catch (Exception e) {
                log.error("转换异常指标列表失败", e);
                report.setAbnormalIndicators("[]");
            }
        }
    }

    /**
     * 获取血压状态
     */
    private String getBloodPressureStatus(double systolic, double diastolic) {
        if (systolic < 90 || diastolic < 60) {
            return "低血压";
        } else if (systolic <= 120 && diastolic <= 80) {
            return "正常";
        } else if (systolic <= 140 && diastolic <= 90) {
            return "偏高";
        } else {
            return "高血压";
        }
    }    private void analyzeHeartRate(HealthMetrics metrics, HealthReport report) {
        List<String> abnormalIndicators = new ArrayList<>();
        
        if (metrics.getHeartRate() != null) {
            report.setHeartRate(metrics.getHeartRate());
            
            // 改进心率状态判断
            String heartRateStatus = getHeartRateStatus(metrics.getHeartRate());
            report.setHeartRateStatus(heartRateStatus);
            
            if (!"正常".equals(heartRateStatus)) {
                abnormalIndicators.add("心率: " + heartRateStatus);
            }
            
            try {
                report.setAbnormalIndicators(objectMapper.writeValueAsString(abnormalIndicators));
            } catch (Exception e) {
                log.error("转换异常指标列表失败", e);
                report.setAbnormalIndicators("[]");
            }
        }
    }

    /**
     * 获取心率状态
     */
    private String getHeartRateStatus(double heartRate) {
        if (heartRate < 60) {
            return "偏低";
        } else if (heartRate <= 100) {
            return "正常";
        } else {
            return "偏高";
        }
    }private void analyzeExercise(HealthMetrics metrics, HealthReport report, ExerciseGoalDTO goal) {        // 设置运动数据
        report.setWeeklyExerciseDuration(metrics.getWeeklyExerciseDuration() != null ? metrics.getWeeklyExerciseDuration().doubleValue() : 0.0);
        report.setWeeklyExerciseCount(metrics.getWeeklyExerciseCount() != null ? metrics.getWeeklyExerciseCount() : 0);
        report.setWeeklyCaloriesBurned(metrics.getWeeklyCaloriesBurned() != null ? metrics.getWeeklyCaloriesBurned() : 0.0);
        
        // 设置步数和距离数据
        report.setDailySteps(metrics.getDailySteps() != null ? metrics.getDailySteps() : 0);
        report.setDailyDistance(metrics.getDailyDistance() != null ? metrics.getDailyDistance() : 0.0);
        
        // 判断运动目标达成情况
        boolean goalAchieved = true;
        
        if (goal != null) {
            // 如果有设定目标，按目标判断
            if (metrics.getWeeklyExerciseDuration() != null && metrics.getWeeklyExerciseDuration() < goal.getWeeklyDurationGoal()) {
                goalAchieved = false;
            }
            if (metrics.getWeeklyExerciseCount() != null && metrics.getWeeklyExerciseCount() < goal.getWeeklyCountGoal()) {
                goalAchieved = false;
            }
            if (metrics.getWeeklyCaloriesBurned() != null && metrics.getWeeklyCaloriesBurned() < goal.getWeeklyCaloriesGoal()) {
                goalAchieved = false;
            }
        } else {
            // 如果没有设定目标，使用WHO推荐标准（每周至少150分钟）
            if (metrics.getWeeklyExerciseDuration() == null || metrics.getWeeklyExerciseDuration() < 150) {
                goalAchieved = false;
            }
        }
        
        report.setExerciseGoalAchieved(goalAchieved);
    }private void analyzeSleep(HealthMetrics metrics, HealthReport report) {
        List<String> abnormalIndicators = new ArrayList<>();
        
        if (metrics.getAverageSleepDuration() != null) {
            report.setAverageSleepDuration(metrics.getAverageSleepDuration());
            report.setDeepSleepPercentage(metrics.getDeepSleepPercentage() != null ? metrics.getDeepSleepPercentage() : 0.0);
            report.setLightSleepPercentage(metrics.getLightSleepPercentage() != null ? metrics.getLightSleepPercentage() : 0.0);
            report.setRemSleepPercentage(metrics.getRemSleepPercentage() != null ? metrics.getRemSleepPercentage() : 0.0);
            
            // 改进睡眠质量评估逻辑
            String sleepQuality = evaluateSleepQuality(metrics.getAverageSleepDuration(), metrics.getDeepSleepPercentage());
            report.setSleepQuality(sleepQuality);
            
            if (metrics.getAverageSleepDuration() < 7) {
                abnormalIndicators.add("睡眠时间不足");
            }
            
            if ("差".equals(sleepQuality) || "中".equals(sleepQuality)) {
                abnormalIndicators.add("睡眠质量: " + sleepQuality);
            }
            
            try {
                report.setAbnormalIndicators(objectMapper.writeValueAsString(abnormalIndicators));
            } catch (Exception e) {
                log.error("转换异常指标列表失败", e);
                report.setAbnormalIndicators("[]");
            }
        } else {
            log.warn("睡眠数据缺失");
            report.setSleepQuality("无数据");
            report.setAverageSleepDuration(0.0);
            report.setDeepSleepPercentage(0.0);
            report.setLightSleepPercentage(0.0);
            report.setRemSleepPercentage(0.0);
        }
    }

    /**
     * 评估睡眠质量
     */
    private String evaluateSleepQuality(double avgDuration, Double deepSleepPercentage) {
        if (avgDuration >= 7 && deepSleepPercentage != null && deepSleepPercentage >= 25) {
            return "优";
        } else if (avgDuration >= 6.5 && deepSleepPercentage != null && deepSleepPercentage >= 20) {
            return "良";
        } else if (avgDuration >= 6) {
            return "中";
        } else {
            return "差";
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
    }    private void generateHealthSuggestions(HealthReport report) {
        List<String> suggestions = new ArrayList<>();

        // 根据BMI生成建议
        if ("偏瘦".equals(report.getBmiStatus())) {
            suggestions.add("您的BMI指数偏低，建议适当增加营养摄入，保持均衡饮食，增加适量蛋白质摄入。");
            suggestions.add("可以进行力量训练来增加肌肉量，提高身体素质。");
        } else if ("超重".equals(report.getBmiStatus())) {
            suggestions.add("您的BMI指数偏高，建议控制饮食热量摄入，增加有氧运动频率，保持每日适量活动。");
            suggestions.add("建议每天进行30分钟以上的有氧运动，如快走、游泳或骑自行车。");
        } else if ("肥胖".equals(report.getBmiStatus())) {
            suggestions.add("您的BMI指数表明肥胖，建议咨询医生或营养师制定减重计划，控制饮食并增加运动量。");
            suggestions.add("建议制定合理的减重目标，每周减重0.5-1公斤为宜。");
        }

        // 根据血压生成建议
        if ("高血压".equals(report.getBloodPressureStatus())) {
            suggestions.add("您的血压较高，建议及时就医检查，遵医嘱用药，同时控制饮食，规律作息，适量运动。");
            suggestions.add("建议限制盐分摄入，保持清淡饮食，避免剧烈运动。");
            suggestions.add("建议定期监测血压，保持情绪稳定，避免过度紧张。");
        } else if ("低血压".equals(report.getBloodPressureStatus())) {
            suggestions.add("您的血压偏低，建议增加盐分摄入，避免长时间站立，起床时动作要缓慢，必要时咨询医生。");
            suggestions.add("避免突然起立，注意循序渐进，可以适当增加液体摄入。");
        } else if ("偏高".equals(report.getBloodPressureStatus())) {
            suggestions.add("您的血压偏高，建议减少盐分摄入，保持良好作息，适当增加体育锻炼，避免过度劳累和紧张。");
        }

        // 根据心率生成建议
        if ("偏高".equals(report.getHeartRateStatus())) {
            suggestions.add("您的静息心率偏高，建议减少咖啡因摄入，保持充足睡眠，尝试放松训练如深呼吸，必要时咨询医生。");
            suggestions.add("建议避免剧烈运动和情绪波动，可以尝试冥想或深呼吸来调节心率。");
        } else if ("偏低".equals(report.getHeartRateStatus())) {
            suggestions.add("您的静息心率偏低，如无不适感可能是良好体能的表现，但如有头晕乏力等症状，请咨询医生。");
            suggestions.add("建议适当增加运动量，提高心肺功能。");
        }

        // 根据运动情况生成建议
        if (Boolean.FALSE.equals(report.getExerciseGoalAchieved())) {
            suggestions.add("您的周运动时间不足，建议每周至少进行150分钟中等强度有氧运动或75分钟高强度运动，并每周进行2-3次力量训练。");
            suggestions.add("建议制定合理的运动计划，循序渐进，可以尝试找到喜欢的运动方式，坚持锻炼。");
        }

        if (report.getDailySteps() != null && report.getDailySteps() < 6000) {
            suggestions.add("您的日均步数较少，建议增加日常活动量，尽量达到每天8000-10000步，可以通过步行上下班、使用楼梯等方式增加步数。");
        }

        // 根据睡眠情况生成建议
        if ("差".equals(report.getSleepQuality())) {
            suggestions.add("您的睡眠质量较差，建议固定作息时间，睡前避免使用电子设备，创造舒适睡眠环境，必要时咨询医生。");
            suggestions.add("睡前避免使用电子设备和剧烈运动，可以尝试睡前热水澡或冥想来改善睡眠。");
        } else if ("中".equals(report.getSleepQuality())) {
            suggestions.add("您的睡眠质量一般，建议睡前放松，避免剧烈运动和过度思考，睡前可喝杯温牛奶帮助入睡。");
        }

        // 如果各项指标都正常，给出保持健康的一般建议
        if (suggestions.isEmpty()) {
            suggestions.add("您的健康状况良好，建议继续保持健康的生活方式，均衡饮食，规律作息，适量运动。");
            suggestions.add("定期健康检查是预防疾病的重要手段，建议每年进行一次全面体检。");
        }

        try {
            report.setHealthSuggestions(objectMapper.writeValueAsString(suggestions));
        } catch (Exception e) {
            log.error("Error converting health suggestions to JSON", e);
            report.setHealthSuggestions("[]");
        }
    }

    /**
     * 为用户生成完整的健康报告（适配前端需求）
     * @param userId 用户ID
     * @return 生成的健康报告
     */
    @Transactional
    public HealthReport generateHealthReport(Integer userId) {
        log.info("开始为用户{}生成健康报告", userId);
        
        try {
            // 收集健康数据
            HealthMetrics metrics = healthDataCollectorService.collectHealthData(userId);
            
            // 分析健康数据并生成报告
            HealthReport report = analyzeHealthData(userId, metrics, null);
              // 设置报告时间
            report.setReportTime(LocalDateTime.now());
            
            // 设置步数和距离数据（前端需要）
            if (metrics.getDailySteps() != null) {
                report.setDailySteps(metrics.getDailySteps());
            }
            if (metrics.getDailyDistance() != null) {
                report.setDailyDistance(metrics.getDailyDistance());
            }
            
            // 保存报告到数据库
            healthReportMapper.insert(report);
            
            log.info("用户{}的健康报告生成完成，总体评分: {}", userId, report.getOverallScore());
            return report;
        } catch (Exception e) {
            log.error("生成健康报告失败", e);
            throw new RuntimeException("生成健康报告失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取用户最新的健康报告
     * @param userId 用户ID
     * @return 最新的健康报告
     */
    public HealthReport getLatestReport(Integer userId) {
        log.info("获取用户{}的最新健康报告", userId);
        return healthReportMapper.findLatestByUserId(userId);
    }

    /**
     * 获取用户的健康报告历史
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 报告列表
     */
    public List<HealthReport> getReportHistory(Integer userId, Date startDate, Date endDate) {
        log.info("获取用户{}的健康报告历史，时间范围: {} - {}", userId, startDate, endDate);
        return healthReportMapper.findByUserIdAndDateRange(userId, startDate, endDate);
    }
}