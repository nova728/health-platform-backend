package com.health.healthplatform.service;

import com.health.healthplatform.entity.healthdata.*;
import com.health.healthplatform.entity.HealthReport.HealthReport;
import com.health.healthplatform.mapper.health_data.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class IntelligentAnalysisService {

    @Autowired
    private DeepSeekService deepSeekService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private HealthDataMapper healthDataMapper;

    @Autowired
    private WeightHistoryMapper weightHistoryMapper;

    @Autowired
    private HeightHistoryMapper heightHistoryMapper;

    @Autowired
    private HeartRateHistoryMapper heartRateHistoryMapper;

    @Autowired
    private BloodPressureHistoryMapper bloodPressureHistoryMapper;

    @Autowired
    private BmiHistoryMapper bmiHistoryMapper;

    @Autowired
    private SleepHistoryMapper sleepHistoryMapper;

    @Autowired
    private StepsHistoryMapper stepsHistoryMapper;

    @Autowired
    private ExerciseRecordMapper exerciseRecordMapper;

    @Autowired
    private ExerciseGoalMapper exerciseGoalMapper;

    /**
     * 智能分析用户的综合健康数据
     * @param userId 用户ID
     * @return 分析结果
     */
    public String analyzeUserHealthData(Integer userId) {
        try {
            // 获取最近30天的数据进行分析
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(30);
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

            // 构建综合健康数据
            Map<String, Object> healthAnalysisData = new HashMap<>();

            // 基础健康数据
            List<HealthData> healthDataList = healthDataMapper.findByUserIdAndDateRange(userId, startDate, endDate);
            if (!healthDataList.isEmpty()) {
                HealthData latestData = healthDataList.get(healthDataList.size() - 1);
                healthAnalysisData.put("latestHealthData", buildHealthDataMap(latestData));
                healthAnalysisData.put("healthDataTrend", analyzeHealthDataTrend(healthDataList));
            }

            // 体重历史分析
            List<WeightHistory> weightHistory = weightHistoryMapper.findByTimeRange(userId, startDateTime, endDateTime);
            healthAnalysisData.put("weightAnalysis", analyzeWeightTrend(weightHistory));

            // 血压历史分析
            List<BloodPressureHistory> bpHistory = bloodPressureHistoryMapper.findByTimeRange(userId, startDateTime, endDateTime);
            healthAnalysisData.put("bloodPressureAnalysis", analyzeBloodPressureTrend(bpHistory));

            // 心率历史分析
            List<HeartRateHistory> hrHistory = heartRateHistoryMapper.findByTimeRange(userId, startDateTime, endDateTime);
            healthAnalysisData.put("heartRateAnalysis", analyzeHeartRateTrend(hrHistory));

            // 睡眠历史分析
            List<SleepHistory> sleepHistory = sleepHistoryMapper.findByTimeRange(userId, startDateTime, endDateTime);
            healthAnalysisData.put("sleepAnalysis", analyzeSleepTrend(sleepHistory));

            // 步数历史分析
            List<StepsHistory> stepsHistory = stepsHistoryMapper.findByDateRange(userId, startDate, endDate);
            healthAnalysisData.put("activityAnalysis", analyzeActivityTrend(stepsHistory));

            // 运动记录分析
            List<ExerciseRecord> exerciseRecords = exerciseRecordMapper.findByDateRange(userId, startDate, endDate);
            ExerciseGoal exerciseGoal = exerciseGoalMapper.findByUserId(userId);
            healthAnalysisData.put("exerciseAnalysis", analyzeExerciseTrend(exerciseRecords, exerciseGoal));

            String healthDataJson = objectMapper.writeValueAsString(healthAnalysisData);

            return deepSeekService.analyzeHealthData(healthDataJson);

        } catch (Exception e) {
            e.printStackTrace();
            return "健康数据分析失败，请稍后再试。错误信息：" + e.getMessage();
        }
    }

    /**
     * 智能分析单项健康数据
     * @param healthData 健康数据
     * @return 分析结果
     */
    public String analyzeHealthData(HealthData healthData) {
        try {
            Map<String, Object> dataMap = buildHealthDataMap(healthData);
            String healthDataJson = objectMapper.writeValueAsString(dataMap);

            return deepSeekService.analyzeHealthData(healthDataJson);

        } catch (Exception e) {
            e.printStackTrace();
            return "数据分析失败，请稍后再试。";
        }
    }

    /**
     * 基于健康报告生成个性化建议
     * @param healthReport 健康报告
     * @return 个性化建议
     */
    public String generatePersonalizedAdvice(HealthReport healthReport) {
        try {
            Map<String, Object> reportData = buildHealthReportMap(healthReport);
            String reportJson = objectMapper.writeValueAsString(reportData);

            return deepSeekService.analyzeHealthData(reportJson);

        } catch (Exception e) {
            e.printStackTrace();
            return "建议生成失败，请稍后再试。";
        }
    }

    /**
     * 生成个性化运动计划
     * @param userId 用户ID
     * @param goal 健身目标
     * @return 运动计划
     */
    public String generateExercisePlan(Integer userId, String goal) {
        try {
            // 获取用户最新健康数据
            HealthData latestData = healthDataMapper.findLatestByUserId(userId);
            if (latestData == null) {
                return "未找到健康数据，请先记录基础健康信息。";
            }

            // 获取用户运动历史
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(30);
            List<ExerciseRecord> exerciseHistory = exerciseRecordMapper.findByDateRange(userId, startDate, endDate);
            ExerciseGoal exerciseGoal = exerciseGoalMapper.findByUserId(userId);

            // 构建用户档案
            Map<String, Object> userProfile = new HashMap<>();
            userProfile.put("userId", userId);
            userProfile.put("basicInfo", buildHealthDataMap(latestData));
            userProfile.put("exerciseHistory", analyzeExerciseTrend(exerciseHistory, exerciseGoal));
            userProfile.put("fitnessGoal", goal);

            String userProfileJson = objectMapper.writeValueAsString(userProfile);

            return deepSeekService.generateExercisePlan(userProfileJson, goal);

        } catch (Exception e) {
            e.printStackTrace();
            return "运动计划生成失败，请稍后再试。";
        }
    }

    /**
     * 分析睡眠质量
     * @param userId 用户ID
     * @return 睡眠分析结果
     */
    public String analyzeSleepQuality(Integer userId) {
        try {
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = endTime.minusDays(30);

            List<SleepHistory> sleepHistory = sleepHistoryMapper.findByTimeRange(userId, startTime, endTime);

            if (sleepHistory.isEmpty()) {
                return "未找到睡眠数据，请先记录睡眠信息。";
            }

            Map<String, Object> sleepAnalysis = analyzeSleepTrend(sleepHistory);
            String sleepDataJson = objectMapper.writeValueAsString(sleepAnalysis);

            return deepSeekService.analyzeSleepQuality(sleepDataJson);

        } catch (Exception e) {
            e.printStackTrace();
            return "睡眠分析失败，请稍后再试。";
        }
    }

    /**
     * 生成营养建议
     * @param userId 用户ID
     * @param dietaryGoal 饮食目标
     * @return 营养建议
     */
    public String generateNutritionAdvice(Integer userId, String dietaryGoal) {
        try {
            HealthData latestData = healthDataMapper.findLatestByUserId(userId);
            if (latestData == null) {
                return "未找到健康数据，请先记录基础健康信息。";
            }

            // 获取运动数据以评估能量消耗
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(7);
            List<ExerciseRecord> exerciseRecords = exerciseRecordMapper.findByDateRange(userId, startDate, endDate);
            List<StepsHistory> stepsHistory = stepsHistoryMapper.findByDateRange(userId, startDate, endDate);

            Map<String, Object> nutritionData = new HashMap<>();
            nutritionData.put("basicHealthData", buildHealthDataMap(latestData));
            nutritionData.put("activityLevel", analyzeActivityLevel(exerciseRecords, stepsHistory));
            nutritionData.put("dietaryGoal", dietaryGoal);

            String nutritionDataJson = objectMapper.writeValueAsString(nutritionData);

            return deepSeekService.generateNutritionAdvice(nutritionDataJson, dietaryGoal);

        } catch (Exception e) {
            e.printStackTrace();
            return "营养建议生成失败，请稍后再试。";
        }
    }

    // 辅助方法：构建健康数据映射
    private Map<String, Object> buildHealthDataMap(HealthData healthData) {
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("userId", healthData.getUserId());
        dataMap.put("heartRate", healthData.getHeartRate());
        dataMap.put("bloodPressure", Map.of(
                "systolic", healthData.getBloodPressureSystolic(),
                "diastolic", healthData.getBloodPressureDiastolic()
        ));
        dataMap.put("weight", healthData.getWeight());
        dataMap.put("height", healthData.getHeight());
        dataMap.put("bmi", healthData.getBmi());
        dataMap.put("steps", healthData.getSteps());
        dataMap.put("sleepDuration", healthData.getSleepDuration());
        dataMap.put("sleepQuality", healthData.getSleepQuality());
        dataMap.put("recordDate", healthData.getRecordDate());
        return dataMap;
    }

    // 辅助方法：构建健康报告映射
    private Map<String, Object> buildHealthReportMap(HealthReport healthReport) {
        Map<String, Object> reportData = new HashMap<>();
        reportData.put("overallScore", healthReport.getOverallScore());
        reportData.put("bmi", Map.of(
                "value", healthReport.getBmi(),
                "status", healthReport.getBmiStatus()
        ));
        reportData.put("bloodPressure", Map.of(
                "systolic", healthReport.getSystolic(),
                "diastolic", healthReport.getDiastolic(),
                "status", healthReport.getBloodPressureStatus()
        ));
        reportData.put("heartRate", Map.of(
                "value", healthReport.getHeartRate(),
                "status", healthReport.getHeartRateStatus()
        ));
        reportData.put("sleep", Map.of(
                "duration", healthReport.getAverageSleepDuration(),
                "quality", healthReport.getSleepQuality(),
                "deepSleepPercentage", healthReport.getDeepSleepPercentage()
        ));
        reportData.put("exercise", Map.of(
                "weeklyDuration", healthReport.getWeeklyExerciseDuration(),
                "dailySteps", healthReport.getDailySteps(),
                "goalAchieved", healthReport.getExerciseGoalAchieved() // 修改这里
        ));
        reportData.put("abnormalIndicators", healthReport.getAbnormalIndicators());
        return reportData;
    }

    // 辅助方法：分析健康数据趋势
    private Map<String, Object> analyzeHealthDataTrend(List<HealthData> healthDataList) {
        if (healthDataList.isEmpty()) return new HashMap<>();

        Map<String, Object> trend = new HashMap<>();

        // 计算平均值和趋势
        double avgHeartRate = healthDataList.stream().mapToInt(HealthData::getHeartRate).average().orElse(0);
        double avgWeight = healthDataList.stream().mapToDouble(HealthData::getWeight).average().orElse(0);
        double avgSteps = healthDataList.stream().mapToInt(HealthData::getSteps).average().orElse(0);
        double avgSleepDuration = healthDataList.stream().mapToDouble(HealthData::getSleepDuration).average().orElse(0);

        trend.put("averages", Map.of(
                "heartRate", avgHeartRate,
                "weight", avgWeight,
                "steps", avgSteps,
                "sleepDuration", avgSleepDuration
        ));

        // 分析趋势方向
        if (healthDataList.size() >= 2) {
            HealthData first = healthDataList.get(0);
            HealthData last = healthDataList.get(healthDataList.size() - 1);

            trend.put("trends", Map.of(
                    "weightChange", last.getWeight() - first.getWeight(),
                    "stepsChange", last.getSteps() - first.getSteps(),
                    "heartRateChange", last.getHeartRate() - first.getHeartRate()
            ));
        }

        return trend;
    }

    // 辅助方法：分析体重趋势
    private Map<String, Object> analyzeWeightTrend(List<WeightHistory> weightHistory) {
        Map<String, Object> analysis = new HashMap<>();

        if (weightHistory.isEmpty()) {
            analysis.put("status", "无体重数据");
            return analysis;
        }

        double avgWeight = weightHistory.stream().mapToDouble(WeightHistory::getWeight).average().orElse(0);
        analysis.put("averageWeight", avgWeight);
        analysis.put("dataPoints", weightHistory.size());

        if (weightHistory.size() >= 2) {
            double weightChange = weightHistory.get(weightHistory.size() - 1).getWeight() -
                    weightHistory.get(0).getWeight();
            analysis.put("weightChange", weightChange);
            analysis.put("trend", weightChange > 0 ? "上升" : weightChange < 0 ? "下降" : "稳定");
        }

        return analysis;
    }

    // 辅助方法：分析血压趋势
    private Map<String, Object> analyzeBloodPressureTrend(List<BloodPressureHistory> bpHistory) {
        Map<String, Object> analysis = new HashMap<>();

        if (bpHistory.isEmpty()) {
            analysis.put("status", "无血压数据");
            return analysis;
        }

        double avgSystolic = bpHistory.stream().mapToDouble(BloodPressureHistory::getSystolic).average().orElse(0);
        double avgDiastolic = bpHistory.stream().mapToDouble(BloodPressureHistory::getDiastolic).average().orElse(0);

        analysis.put("averages", Map.of(
                "systolic", avgSystolic,
                "diastolic", avgDiastolic
        ));
        analysis.put("dataPoints", bpHistory.size());

        return analysis;
    }

    // 辅助方法：分析心率趋势
    private Map<String, Object> analyzeHeartRateTrend(List<HeartRateHistory> hrHistory) {
        Map<String, Object> analysis = new HashMap<>();

        if (hrHistory.isEmpty()) {
            analysis.put("status", "无心率数据");
            return analysis;
        }

        double avgHeartRate = hrHistory.stream().mapToDouble(HeartRateHistory::getHeartRate).average().orElse(0);
        analysis.put("averageHeartRate", avgHeartRate);
        analysis.put("dataPoints", hrHistory.size());

        return analysis;
    }

    // 辅助方法：分析睡眠趋势
    private Map<String, Object> analyzeSleepTrend(List<SleepHistory> sleepHistory) {
        Map<String, Object> analysis = new HashMap<>();

        if (sleepHistory.isEmpty()) {
            analysis.put("status", "无睡眠数据");
            return analysis;
        }

        double avgDuration = sleepHistory.stream().mapToDouble(SleepHistory::getSleepDuration).average().orElse(0);
        double avgDeepSleep = sleepHistory.stream().mapToDouble(SleepHistory::getDeepSleep).average().orElse(0);
        double avgLightSleep = sleepHistory.stream().mapToDouble(SleepHistory::getLightSleep).average().orElse(0);
        double avgRemSleep = sleepHistory.stream().mapToDouble(SleepHistory::getRemSleep).average().orElse(0);

        analysis.put("averages", Map.of(
                "duration", avgDuration,
                "deepSleep", avgDeepSleep,
                "lightSleep", avgLightSleep,
                "remSleep", avgRemSleep
        ));

        // 计算睡眠质量分布
        Map<String, Long> qualityDistribution = sleepHistory.stream()
                .collect(Collectors.groupingBy(SleepHistory::getSleepQuality, Collectors.counting()));
        analysis.put("qualityDistribution", qualityDistribution);

        analysis.put("dataPoints", sleepHistory.size());

        return analysis;
    }

    // 辅助方法：分析活动趋势
    private Map<String, Object> analyzeActivityTrend(List<StepsHistory> stepsHistory) {
        Map<String, Object> analysis = new HashMap<>();

        if (stepsHistory.isEmpty()) {
            analysis.put("status", "无步数数据");
            return analysis;
        }

        double avgSteps = stepsHistory.stream().mapToInt(StepsHistory::getSteps).average().orElse(0);
        double avgDistance = stepsHistory.stream().mapToDouble(StepsHistory::getDistance).average().orElse(0);
        double avgCalories = stepsHistory.stream().mapToDouble(StepsHistory::getCalories).average().orElse(0);

        analysis.put("averages", Map.of(
                "steps", avgSteps,
                "distance", avgDistance,
                "calories", avgCalories
        ));

        // 计算目标达成率
        long goalAchievedDays = stepsHistory.stream()
                .mapToLong(sh -> sh.getSteps() >= sh.getTarget() ? 1L : 0L)
                .sum();
        double achievementRate = (double) goalAchievedDays / stepsHistory.size() * 100;
        analysis.put("goalAchievementRate", achievementRate);

        analysis.put("dataPoints", stepsHistory.size());

        return analysis;
    }

    // 辅助方法：分析运动趋势
    private Map<String, Object> analyzeExerciseTrend(List<ExerciseRecord> exerciseRecords, ExerciseGoal exerciseGoal) {
        Map<String, Object> analysis = new HashMap<>();

        if (exerciseRecords.isEmpty()) {
            analysis.put("status", "无运动数据");
            return analysis;
        }

        int totalDuration = exerciseRecords.stream().mapToInt(ExerciseRecord::getDuration).sum();
        int totalCalories = exerciseRecords.stream().mapToInt(ExerciseRecord::getCalories).sum();

        analysis.put("totals", Map.of(
                "duration", totalDuration,
                "calories", totalCalories,
                "exerciseCount", exerciseRecords.size()
        ));

        // 运动类型分布
        Map<String, Long> exerciseTypeDistribution = exerciseRecords.stream()
                .collect(Collectors.groupingBy(ExerciseRecord::getExerciseType, Collectors.counting()));
        analysis.put("exerciseTypeDistribution", exerciseTypeDistribution);

        // 目标达成情况
        if (exerciseGoal != null) {
            analysis.put("goalComparison", Map.of(
                    "weeklyDurationGoal", exerciseGoal.getWeeklyDurationGoal(),
                    "actualDuration", totalDuration,
                    "weeklyCaloriesGoal", exerciseGoal.getWeeklyCaloriesGoal(),
                    "actualCalories", totalCalories,
                    "weeklyCountGoal", exerciseGoal.getWeeklyCountGoal(),
                    "actualCount", exerciseRecords.size()
            ));
        }

        return analysis;
    }

    // 辅助方法：分析活动水平
    private Map<String, Object> analyzeActivityLevel(List<ExerciseRecord> exerciseRecords, List<StepsHistory> stepsHistory) {
        Map<String, Object> activityLevel = new HashMap<>();

        // 运动活动水平
        int totalExerciseDuration = exerciseRecords.stream().mapToInt(ExerciseRecord::getDuration).sum();
        activityLevel.put("weeklyExerciseDuration", totalExerciseDuration);

        // 日常活动水平
        double avgDailySteps = stepsHistory.stream().mapToDouble(StepsHistory::getSteps).average().orElse(0);
        activityLevel.put("averageDailySteps", avgDailySteps);

        // 活动水平评估
        String level;
        if (avgDailySteps >= 10000 && totalExerciseDuration >= 150) {
            level = "高";
        } else if (avgDailySteps >= 6000 && totalExerciseDuration >= 75) {
            level = "中";
        } else {
            level = "低";
        }
        activityLevel.put("activityLevel", level);

        return activityLevel;
    }
}