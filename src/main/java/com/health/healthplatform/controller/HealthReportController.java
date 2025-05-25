package com.health.healthplatform.controller;

import com.health.healthplatform.DTO.*;
import com.health.healthplatform.result.Result;
import com.health.healthplatform.service.health_data.*;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/health/report")
@Slf4j
@CrossOrigin(origins = {"http://localhost:5173"}, allowCredentials = "true")
public class HealthReportController {
    @Resource
    private HeartRateService heartRateService;
    @Resource
    private SleepService sleepService;
    @Resource
    private StepsService stepsService;
    @Resource
    private WeightService weightService;
    @Resource
    private ExerciseRecordService exerciseRecordService;
    @Resource
    private BmiService bmiService;

    @GetMapping("/{userId}/score")
    public Result getHealthScore(@PathVariable Integer userId) {
        try {
            if (userId == null || userId <= 0) {
                return Result.failure(400, "无效的用户ID");
            }

            Map<String, Object> response = new HashMap<>();
            Map<String, Object> subScores = calculateSubScores(userId);

            // 计算总分
            double totalScore = ((Double) subScores.get("exerciseScore")
                    + (Double) subScores.get("sleepScore")
                    + (Double) subScores.get("physicalScore")) / 3;

            response.put("totalScore", Math.round(totalScore));
            response.put("subScores", subScores);

            return Result.success(response);
        } catch (Exception e) {
            log.error("Failed to get health score: ", e);
            return Result.failure(500, "获取健康评分失败：" + e.getMessage());
        }
    }

    @PostMapping("/{userId}/generate")
    public Result generateReport(
            @PathVariable Integer userId,
            @RequestBody GenerateReportRequest request) {
        try {
            Map<String, Object> report = new HashMap<>();

            // 获取各项指标数据
            if (request.getMetrics().contains("weight")) {
                List<WeightDTO> weightData = weightService.getUserWeightHistory(userId, request.getType());
                report.put("weightData", weightData);
            }
            if (request.getMetrics().contains("sleep")) {
                List<SleepDTO> sleepData = sleepService.getUserSleepHistory(userId, request.getType());
                report.put("sleepData", sleepData);
            }
            if (request.getMetrics().contains("exercise")) {
                List<ExerciseRecordDTO> exerciseData = exerciseRecordService.getUserExerciseRecords(userId, request.getType());
                report.put("exerciseData", exerciseData);
            }

            // 生成健康建议
            List<Map<String, String>> suggestions = generateHealthSuggestions(userId, report);
            report.put("suggestions", suggestions);

            return Result.success(report);
        } catch (Exception e) {
            log.error("Failed to generate health report: ", e);
            return Result.failure(500, "生成健康报告失败：" + e.getMessage());
        }
    }

    @GetMapping("/{userId}/trend/{metric}")
    public Result getTrendData(
            @PathVariable Integer userId,
            @PathVariable String metric,
            @RequestParam(defaultValue = "week") String period) {
        try {
            if (userId == null || userId <= 0) {
                return Result.failure(400, "无效的用户ID");
            }

            // 验证 period 参数
            if (!Arrays.asList("day", "week", "month").contains(period)) {
                return Result.failure(400, "无效的时间范围参数");
            }

            Object data = switch (metric) {
                case "weight" -> weightService.getUserWeightHistory(userId, period);
                case "sleep" -> sleepService.getUserSleepHistory(userId, period);
                case "steps" -> stepsService.getUserStepsHistory(userId, period);
                case "heartRate" -> heartRateService.getUserHeartRateHistory(userId, period);
                default -> {
                    log.warn("Invalid metric requested: {}", metric);
                    yield null;
                }
            };

            if (data == null) {
                return Result.failure(400, "无效的指标类型：" + metric);
            }

            return Result.success(data);
        } catch (Exception e) {
            log.error("Failed to get trend data: ", e);
            return Result.failure(500, "获取趋势数据失败：" + e.getMessage());
        }
    }

    private Map<String, Object> calculateSubScores(Integer userId) {
        Map<String, Object> scores = new HashMap<>();

        // 计算运动指数
        WeeklyStats exerciseStats = exerciseRecordService.getWeeklyStats(userId);
        double exerciseScore = calculateExerciseScore(exerciseStats);
        scores.put("exerciseScore", exerciseScore);

        // 计算睡眠质量
        List<SleepDTO> sleepData = sleepService.getUserSleepHistory(userId, "week");
        double sleepScore = calculateSleepScore(sleepData);
        scores.put("sleepScore", sleepScore);

        // 计算身体状况分数
        double physicalScore = calculatePhysicalScore(userId);
        scores.put("physicalScore", physicalScore);

        return scores;
    }

    private double calculateExerciseScore(WeeklyStats stats) {
        if (stats == null) return 60.0;

        double score = 60.0; // 基础分

        if (stats.getTotalDuration() >= 7.0) score += 15; // 每周运动7小时以上
        else if (stats.getTotalDuration() >= 5.0) score += 10;

        if (stats.getExerciseCount() >= 5) score += 15;  // 每周运动5次以上
        else if (stats.getExerciseCount() >= 3) score += 10;

        if (stats.getTotalCalories() >= 2000) score += 10; // 每周消耗2000千卡以上
        else if (stats.getTotalCalories() >= 1500) score += 5;

        return Math.min(score, 100.0);
    }

    private double calculateSleepScore(List<SleepDTO> sleepData) {
        if (sleepData == null || sleepData.isEmpty()) return 60.0;

        double score = 60.0; // 基础分
        double avgDuration = sleepData.stream()
                .mapToDouble(SleepDTO::getDuration)
                .average()
                .orElse(0.0);

        if (avgDuration >= 7.0 && avgDuration <= 9.0) score += 40;
        else if (avgDuration >= 6.0) score += 20;

        return Math.min(score, 100.0);
    }

    private double calculatePhysicalScore(Integer userId) {
        double score = 75.0; // 基础分

        try {
            // 获取最新的BMI数据
            List<BmiDTO> bmiData = bmiService.getUserBmiHistory(userId, "day");
            if (!bmiData.isEmpty()) {
                double bmi = bmiData.get(0).getBmi();
                if (bmi >= 18.5 && bmi < 24.0) score += 25;
                else if (bmi >= 17.0 || bmi < 28.0) score += 15;
            }
        } catch (Exception e) {
            log.error("Error calculating physical score: ", e);
        }

        return Math.min(score, 100.0);
    }

    private List<Map<String, String>> generateHealthSuggestions(Integer userId, Map<String, Object> reportData) {
        List<Map<String, String>> suggestions = new ArrayList<>();

        // 运动建议
        Map<String, String> exerciseSuggestion = new HashMap<>();
        WeeklyStats stats = exerciseRecordService.getWeeklyStats(userId);
        if (stats.getExerciseCount() < 3) {
            exerciseSuggestion.put("title", "运动建议");
            exerciseSuggestion.put("type", "warning");
            exerciseSuggestion.put("content", "您的运动频率偏低，建议每周至少进行3次中等强度运动，每次30-60分钟。");
        } else {
            exerciseSuggestion.put("title", "运动建议");
            exerciseSuggestion.put("type", "success");
            exerciseSuggestion.put("content", "您保持了良好的运动习惯，建议继续保持每周" + stats.getExerciseCount() + "次的运动频率。");
        }
        suggestions.add(exerciseSuggestion);

        // 睡眠建议
        Map<String, String> sleepSuggestion = new HashMap<>();
        List<SleepDTO> sleepData = sleepService.getUserSleepHistory(userId, "week");
        double avgSleep = sleepData.stream()
                .mapToDouble(SleepDTO::getDuration)
                .average()
                .orElse(0.0);
        if (avgSleep < 7.0) {
            sleepSuggestion.put("title", "睡眠建议");
            sleepSuggestion.put("type", "warning");
            sleepSuggestion.put("content", "您的平均睡眠时间不足，建议保证每天7-8小时的睡眠时间，改善睡眠质量。");
        } else {
            sleepSuggestion.put("title", "睡眠建议");
            sleepSuggestion.put("type", "success");
            sleepSuggestion.put("content", "您的睡眠时间达标，建议继续保持规律的作息时间。");
        }
        suggestions.add(sleepSuggestion);

        return suggestions;
    }
}

class GenerateReportRequest {
    private String type;
    private List<String> metrics;
    private List<LocalDate> dateRange;

    // Getters and setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public List<String> getMetrics() { return metrics; }
    public void setMetrics(List<String> metrics) { this.metrics = metrics; }
    public List<LocalDate> getDateRange() { return dateRange; }
    public void setDateRange(List<LocalDate> dateRange) { this.dateRange = dateRange; }
}