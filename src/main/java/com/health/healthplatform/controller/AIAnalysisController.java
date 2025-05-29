package com.health.healthplatform.controller;

import com.health.healthplatform.service.IntelligentAnalysisService;
import com.health.healthplatform.service.DeepSeekService;
import com.health.healthplatform.entity.healthdata.HealthData;
import com.health.healthplatform.entity.healthdata.SleepHistory;
import com.health.healthplatform.mapper.health_data.HealthDataMapper;
import com.health.healthplatform.mapper.health_data.SleepHistoryMapper;
import com.health.healthplatform.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin
public class AIAnalysisController {

    @Autowired
    private IntelligentAnalysisService intelligentAnalysisService;

    @Autowired
    private DeepSeekService deepSeekService;

    @Autowired
    private HealthDataMapper healthDataMapper;

    @Autowired
    private SleepHistoryMapper sleepHistoryMapper;

    /**
     * 综合分析用户健康数据（包含多种历史数据的趋势分析）
     */
    @GetMapping("/comprehensive-analysis/{userId}")
    public Result comprehensiveHealthAnalysis(@PathVariable Integer userId) {
        try {
            String analysis = intelligentAnalysisService.analyzeUserHealthData(userId);
            return Result.success(analysis);
        } catch (Exception e) {
            return Result.failure(500, "综合健康分析失败：" + e.getMessage());
        }
    }

    /**
     * 分析用户最新健康数据
     */
    @GetMapping("/analyze/{userId}")
    public Result analyzeUserHealth(@PathVariable Integer userId) {
        try {
            HealthData latestData = healthDataMapper.findLatestByUserId(userId);
            if (latestData == null) {
                return Result.failure(404, "未找到健康数据，请先记录健康信息。");
            }
            String analysis = intelligentAnalysisService.analyzeHealthData(latestData);
            return Result.success(analysis);
        } catch (Exception e) {
            return Result.failure(500, "分析失败：" + e.getMessage());
        }
    }

    /**
     * 生成个性化运动计划
     */
    @PostMapping("/exercise-plan")
    public Result generateExercisePlan(
            @RequestParam Integer userId,
            @RequestParam(required = false) String goals,
            @RequestParam(required = false) String preferences) {
        try {
            String exercisePlan = deepSeekService.generateExercisePlan(goals, preferences);
            return Result.success(exercisePlan);
        } catch (Exception e) {
            return Result.failure(500, "运动计划生成失败：" + e.getMessage());
        }
    }

    /**
     * 获取营养建议
     */
    @PostMapping("/nutrition-advice")
    public Result generateNutritionAdvice(
            @RequestParam Integer userId,
            @RequestParam(required = false) String dietaryRestrictions,
            @RequestParam(required = false) String goals) {
        try {
            String nutritionAdvice = deepSeekService.generateNutritionAdvice(dietaryRestrictions, goals);
            return Result.success(nutritionAdvice);
        } catch (Exception e) {
            return Result.failure(500, "营养建议生成失败：" + e.getMessage());
        }
    }

    /**
     * 分析睡眠质量
     */
    @GetMapping("/sleep-analysis/{userId}")
    public Result analyzeSleepQuality(@PathVariable Integer userId) {
        try {
            String sleepAnalysis = intelligentAnalysisService.analyzeSleepQuality(userId);
            return Result.success(sleepAnalysis);
        } catch (Exception e) {
            return Result.failure(500, "睡眠分析失败：" + e.getMessage());
        }
    }

    /**
     * AI健康助手对话
     */
    @PostMapping("/chat")
    public Result chat(
            @RequestParam Integer userId,
            @RequestParam String message,
            @RequestParam(required = false) String context) {
        try {
            String response = deepSeekService.chat(message, context != null ? context : "");
            return Result.success(response);
        } catch (Exception e) {
            return Result.failure(500, "AI对话失败：" + e.getMessage());
        }
    }

    /**
     * 健康知识问答
     */
    @PostMapping("/health-qa")
    public Result healthQA(
            @RequestParam String question,
            @RequestParam(required = false) Integer userId) {
        try {
            // 构建专业的健康问答提示
            StringBuilder prompt = new StringBuilder();
            prompt.append("作为专业的健康顾问，请回答以下健康相关问题：\n");
            prompt.append("问题：").append(question).append("\n\n");
            
            // 如果提供了用户ID，可以结合用户的健康数据给出个性化建议
            if (userId != null) {
                HealthData healthData = healthDataMapper.findLatestByUserId(userId);
                if (healthData != null) {
                    prompt.append("用户健康状况参考：\n");
                    prompt.append("- 身高：").append(healthData.getHeight()).append("cm\n");
                    prompt.append("- 体重：").append(healthData.getWeight()).append("kg\n");
                    if (healthData.getHeartRate() != null) {
                        prompt.append("- 心率：").append(healthData.getHeartRate()).append("次/分\n");
                    }
                    if (healthData.getBloodPressureSystolic() != null && healthData.getBloodPressureDiastolic() != null) {
                        prompt.append("- 血压：").append(healthData.getBloodPressureSystolic())
                              .append("/").append(healthData.getBloodPressureDiastolic()).append("mmHg\n");
                    }
                    prompt.append("\n");
                }
            }
            
            prompt.append("请提供：\n");
            prompt.append("1. 准确、科学的回答\n");
            prompt.append("2. 实用的建议和注意事项\n");
            prompt.append("3. 如有必要，建议咨询专业医生\n");
            
            String response = deepSeekService.chat(prompt.toString(), "");
            return Result.success(response);
        } catch (Exception e) {
            return Result.failure(500, "健康问答失败：" + e.getMessage());
        }
    }

    /**
     * 获取用户健康评估
     */
    @GetMapping("/health-assessment/{userId}")
    public Result getHealthAssessment(@PathVariable Integer userId) {
        try {
            HealthData healthData = healthDataMapper.findLatestByUserId(userId);
            if (healthData == null) {
                return Result.failure(404, "未找到健康数据，请先记录健康信息。");
            }
            
            Map<String, Object> assessment = new HashMap<>();
            
            // 计算BMI
            if (healthData.getHeight() != null && healthData.getWeight() != null) {
                double heightM = healthData.getHeight() / 100.0;
                double bmi = healthData.getWeight() / (heightM * heightM);
                assessment.put("bmi", Math.round(bmi * 100.0) / 100.0);
                
                // BMI评估
                String bmiStatus;
                if (bmi < 18.5) bmiStatus = "偏瘦";
                else if (bmi < 24) bmiStatus = "正常";
                else if (bmi < 28) bmiStatus = "偏重";
                else bmiStatus = "肥胖";
                assessment.put("bmiStatus", bmiStatus);
            }
            
            // 血压评估
            if (healthData.getBloodPressureSystolic() != null && healthData.getBloodPressureDiastolic() != null) {
                String bpStatus;
                int systolic = healthData.getBloodPressureSystolic();
                int diastolic = healthData.getBloodPressureDiastolic();
                
                if (systolic < 120 && diastolic < 80) bpStatus = "正常";
                else if (systolic < 130 && diastolic < 85) bpStatus = "正常偏高";
                else if (systolic < 140 && diastolic < 90) bpStatus = "轻度高血压";
                else bpStatus = "高血压";
                
                assessment.put("bloodPressureStatus", bpStatus);
            }
            
            // 心率评估
            if (healthData.getHeartRate() != null) {
                String hrStatus;
                int hr = healthData.getHeartRate();
                if (hr < 60) hrStatus = "偏慢";
                else if (hr <= 100) hrStatus = "正常";
                else hrStatus = "偏快";
                assessment.put("heartRateStatus", hrStatus);
            }
            
            return Result.success(assessment);
        } catch (Exception e) {
            return Result.failure(500, "健康评估失败：" + e.getMessage());
        }
    }

    /**
     * 获取个性化健康建议
     */
    @GetMapping("/personalized-advice/{userId}")
    public Result getPersonalizedAdvice(@PathVariable Integer userId) {
        try {
            // 获取用户的综合健康数据
            String healthAnalysis = intelligentAnalysisService.analyzeUserHealthData(userId);
            
            // 基于分析结果生成个性化建议
            StringBuilder prompt = new StringBuilder();
            prompt.append("基于以下用户健康数据分析，请提供个性化的健康建议：\n\n");
            prompt.append(healthAnalysis).append("\n\n");
            prompt.append("请提供以下方面的具体建议：\n");
            prompt.append("1. 运动锻炼计划\n");
            prompt.append("2. 饮食营养建议\n");
            prompt.append("3. 生活方式改善\n");
            prompt.append("4. 健康监测重点\n");
            prompt.append("5. 预防保健措施\n");
            prompt.append("\n建议要具体、可操作、个性化。");
            
            String advice = deepSeekService.chat(prompt.toString(), "");
            
            Map<String, Object> result = new HashMap<>();
            result.put("advice", advice);
            result.put("analysisData", healthAnalysis);
            
            return Result.success(result);
        } catch (Exception e) {
            return Result.failure(500, "个性化建议生成失败：" + e.getMessage());
        }
    }

    /**
     * AI健康教练对话
     */
    @PostMapping("/health-coach/{userId}")
    public Result healthCoachChat(
            @PathVariable Integer userId,
            @RequestParam String message,
            @RequestParam(required = false) String context) {
        try {
            HealthData latestData = healthDataMapper.findLatestByUserId(userId);

            String systemPrompt = """
                你是一个专业的AI健康教练，具有丰富的健康管理经验。
                请根据用户的健康数据和对话历史，提供个性化的健康指导。
                
                指导原则：
                1. 基于科学的健康知识
                2. 考虑用户的具体健康状况
                3. 提供可操作的建议
                4. 保持积极正面的态度
                5. 必要时建议寻求专业医疗帮助
                
                回复要求：
                - 简洁明了，易于理解
                - 提供具体的行动建议
                - 鼓励用户保持健康的生活方式
                """;

            if (latestData != null) {
                systemPrompt += String.format(
                        "\n\n用户当前健康状况：\n" +
                                "- 心率：%d次/分\n" +
                                "- 血压：%d/%d mmHg\n" +
                                "- BMI：%.1f\n" +
                                "- 日步数：%d步\n" +
                                "- 睡眠时长：%.1f小时\n" +
                                "- 睡眠质量：%s\n",
                        latestData.getHeartRate(),
                        latestData.getBloodPressureSystolic(),
                        latestData.getBloodPressureDiastolic(),
                        latestData.getBmi(),
                        latestData.getSteps(),
                        latestData.getSleepDuration(),
                        latestData.getSleepQuality()
                );
            }

            if (context != null && !context.trim().isEmpty()) {
                systemPrompt += "\n\n对话上下文：" + context;
            }

            String response = deepSeekService.chat(systemPrompt, message);
            return Result.success(response);
        } catch (Exception e) {
            return Result.failure(500, "健康教练对话失败：" + e.getMessage());
        }
    }
}