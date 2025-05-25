package com.health.healthplatform.service.HealthReport;

import com.health.healthplatform.service.health_data.*;
import org.springframework.stereotype.Service;

import com.health.healthplatform.DTO.BmiDTO;
import com.health.healthplatform.DTO.BloodPressureDTO;
import com.health.healthplatform.DTO.HeartRateDTO;
import com.health.healthplatform.DTO.StepsDTO;
import com.health.healthplatform.DTO.SleepDTO;
import com.health.healthplatform.entity.HealthReport.HealthMetrics;
import com.health.healthplatform.entity.healthdata.HealthData;
import com.health.healthplatform.mapper.health_data.HealthDataMapper;

import lombok.extern.slf4j.Slf4j;
import jakarta.annotation.Resource;

import java.util.DoubleSummaryStatistics;
import java.util.List;

@Slf4j
@Service
public class HealthDataCollectorService {
    @Resource
    private BloodPressureService bloodPressureService;
    @Resource
    private BmiService bmiService;
    @Resource
    private HeartRateService heartRateService;
    @Resource
    private ExerciseRecordService exerciseRecordService;
    @Resource
    private StepsService stepsService;
    @Resource
    private SleepService sleepService;
    @Resource
    private WeightService weightService;
    @Resource
    private HealthDataMapper healthDataMapper;

    private void collectBasicHealthData(Integer userId, HealthMetrics metrics) {
        // 获取最新的基础健康数据
        HealthData latestData = healthDataMapper.findLatestByUserId(userId);
        if (latestData != null) {
            // 设置基础健康指标
            metrics.setWeight(latestData.getWeight());
            metrics.setHeight(latestData.getHeight());
            // 计算BMI
            if (latestData.getWeight() != null && latestData.getHeight() != null) {
                double heightInMeters = latestData.getHeight() / 100.0;
                metrics.setBmi(latestData.getWeight() / (heightInMeters * heightInMeters));
            }
            
            // 设置血压数据
            metrics.setSystolic(latestData.getBloodPressureSystolic() != null ? 
                latestData.getBloodPressureSystolic().doubleValue() : null);
            metrics.setDiastolic(latestData.getBloodPressureDiastolic() != null ? 
                latestData.getBloodPressureDiastolic().doubleValue() : null);
            
            // 设置心率数据
            metrics.setHeartRate(latestData.getHeartRate() != null ? 
                latestData.getHeartRate().doubleValue() : null);
            
            log.info("收集到基础健康数据: 体重{}kg, 身高{}cm, 收缩压{}, 舒张压{}, 心率{}", 
            latestData.getWeight(), latestData.getHeight(), 
            latestData.getBloodPressureSystolic(), latestData.getBloodPressureDiastolic(),
            latestData.getHeartRate());
        } else {
            log.warn("未找到用户{}的基础健康数据", userId);
        }
    }

    public HealthMetrics collectHealthData(Integer userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("无效的用户ID");
        }


        log.info("开始收集用户{}的健康数据", userId);
        HealthMetrics metrics = new HealthMetrics();

        try {
            // 收集各项数据
             // 收集基础健康数据
            collectBasicHealthData(userId, metrics);
            
            // 收集运动数据
            collectExerciseData(userId, metrics);
            
            // 收集步数数据
            collectStepsData(userId, metrics);
            
            // 收集睡眠数据
            collectSleepData(userId, metrics);

            return metrics;
        } catch (Exception e) {
            log.error("收集健康数据时发生错误", e);
            throw new RuntimeException("健康数据收集失败: " + e.getMessage(), e);
        }
    }


    
    private void collectBmiData(Integer userId, HealthMetrics metrics) {
        List<BmiDTO> bmiHistory = bmiService.getUserBmiHistory(userId, "day");
        if (!bmiHistory.isEmpty()) {
            BmiDTO latest = bmiHistory.get(0);
            metrics.setBmi(latest.getBmi());
            metrics.setWeight(latest.getWeight());
            metrics.setHeight(latest.getHeight());
            log.info("收集到BMI数据: BMI={}, 体重={}kg, 身高={}cm", 
                latest.getBmi(), latest.getWeight(), latest.getHeight());
        }
    }
    private void collectBloodPressureData(Integer userId, HealthMetrics metrics) {
        List<BloodPressureDTO> bpHistory = bloodPressureService.getUserBloodPressureHistory(userId, "all");
        if (!bpHistory.isEmpty()) {
            BloodPressureDTO latest = bpHistory.get(0);
            metrics.setSystolic(latest.getSystolic());
            metrics.setDiastolic(latest.getDiastolic());
            log.info("收集到血压数据: {}/{}", latest.getSystolic(), latest.getDiastolic());
        } else {
            log.warn("未找到用户{}的血压数据", userId);
        }
    }

    private void collectHeartRateData(Integer userId, HealthMetrics metrics) {
        List<HeartRateDTO> hrHistory = heartRateService.getUserHeartRateHistory(userId, "all");
        if (!hrHistory.isEmpty()) {
            HeartRateDTO latest = hrHistory.get(0);
            metrics.setHeartRate(latest.getHeartRate());
            log.info("收集到心率数据: {}", latest.getHeartRate());
        } else {
            log.warn("未找到用户{}的心率数据", userId);
        }
    }

    private void collectExerciseData(Integer userId, HealthMetrics metrics) {
        var weeklyStats = exerciseRecordService.getWeeklyStats(userId);
        metrics.setWeeklyExerciseDuration(weeklyStats.getTotalDuration());
        metrics.setWeeklyExerciseCount(weeklyStats.getExerciseCount());
        metrics.setWeeklyCaloriesBurned(weeklyStats.getTotalCalories().doubleValue());
        log.info("收集到运动数据: 时长{}分钟, 次数{}, 消耗{}卡路里", 
            weeklyStats.getTotalDuration(), weeklyStats.getExerciseCount(), weeklyStats.getTotalCalories());
    }

    private void collectStepsData(Integer userId, HealthMetrics metrics) {
        List<StepsDTO> stepsHistory = stepsService.getUserStepsHistory(userId, "all");
        if (!stepsHistory.isEmpty()) {
            StepsDTO latest = stepsHistory.get(0);
            metrics.setDailySteps(latest.getSteps());
            metrics.setDailyDistance(latest.getDistance());
            log.info("收集到步数数据: {}步, {}公里", latest.getSteps(), latest.getDistance());
        } else {
            log.warn("未找到用户{}的步数数据", userId);
        }
    }

    private void collectSleepData(Integer userId, HealthMetrics metrics) {
        List<SleepDTO> sleepHistory = sleepService.getUserSleepHistory(userId, "week");
        if (!sleepHistory.isEmpty()) {
            // 计算一周的平均睡眠数据
            DoubleSummaryStatistics durationStats = sleepHistory.stream()
                .mapToDouble(SleepDTO::getDuration)
                .summaryStatistics();
            
            DoubleSummaryStatistics deepSleepStats = sleepHistory.stream()
                .mapToDouble(SleepDTO::getDeepSleepPercentage)
                .summaryStatistics();
                
            DoubleSummaryStatistics lightSleepStats = sleepHistory.stream()
                .mapToDouble(SleepDTO::getLightSleepPercentage)
                .summaryStatistics();
                
            DoubleSummaryStatistics remSleepStats = sleepHistory.stream()
                .mapToDouble(SleepDTO::getRemSleepPercentage)
                .summaryStatistics();

            metrics.setAverageSleepDuration(durationStats.getAverage());
            metrics.setDeepSleepPercentage(deepSleepStats.getAverage());
            metrics.setLightSleepPercentage(lightSleepStats.getAverage());
            metrics.setRemSleepPercentage(remSleepStats.getAverage());
            
            log.info("收集到睡眠数据: 平均时长{}小时, 深睡{}%, 浅睡{}%, REM睡眠{}%", 
                durationStats.getAverage(),
                deepSleepStats.getAverage(),
                lightSleepStats.getAverage(),
                remSleepStats.getAverage());
        } else {
            log.warn("未找到用户{}的睡眠数据", userId);
        }
    }

    private void validateMetrics(HealthMetrics metrics) {
        if (metrics.getBmi() != null && (metrics.getBmi() < 10 || metrics.getBmi() > 50)) {
            log.warn("异常的BMI值: {}", metrics.getBmi());
        }
        
        if (metrics.getSystolic() != null && (metrics.getSystolic() < 70 || metrics.getSystolic() > 200)) {
            log.warn("异常的收缩压值: {}", metrics.getSystolic());
        }
        
        if (metrics.getDiastolic() != null && (metrics.getDiastolic() < 40 || metrics.getDiastolic() > 130)) {
            log.warn("异常的舒张压值: {}", metrics.getDiastolic());
        }
        
        if (metrics.getHeartRate() != null && (metrics.getHeartRate() < 40 || metrics.getHeartRate() > 200)) {
            log.warn("异常的心率值: {}", metrics.getHeartRate());
        }
    }
}