package com.health.healthplatform.service;

import com.health.healthplatform.DTO.deepseek.DeepSeekRequest;
import com.health.healthplatform.DTO.deepseek.DeepSeekResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Service
public class DeepSeekService {

    @Autowired
    private WebClient deepSeekWebClient;

    @Value("${deepseek.api.model:deepseek-chat}")
    private String model;

    /**
     * 调用DeepSeek API进行对话
     * @param systemPrompt 系统提示词
     * @param userMessage 用户消息
     * @return AI回复内容
     */
    public String chat(String systemPrompt, String userMessage) {
        try {
            List<DeepSeekRequest.Message> messages = Arrays.asList(
                    new DeepSeekRequest.Message("system", systemPrompt),
                    new DeepSeekRequest.Message("user", userMessage)
            );

            DeepSeekRequest request = new DeepSeekRequest(
                    model,
                    messages,
                    2000,
                    0.7
            );

            Mono<DeepSeekResponse> responseMono = deepSeekWebClient.post()
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(DeepSeekResponse.class);

            DeepSeekResponse response = responseMono.block();

            if (response != null &&
                    response.getChoices() != null &&
                    !response.getChoices().isEmpty()) {
                return response.getChoices().get(0).getMessage().getContent();
            }

            return "抱歉，AI服务暂时不可用，请稍后再试。";

        } catch (Exception e) {
            e.printStackTrace();
            return "抱歉，AI服务出现错误，请稍后再试。";
        }
    }

    /**
     * 分析健康数据
     * @param healthData 健康数据JSON字符串
     * @return 分析结果
     */
    public String analyzeHealthData(String healthData) {
        String systemPrompt = """
            你是一个专业的健康数据分析师。请根据用户提供的健康数据，提供专业的健康分析和建议。
            
            分析要求：
            1. 对各项健康指标进行专业评估
            2. 指出可能存在的健康风险
            3. 提供具体的改善建议
            4. 建议是否需要就医
            5. 回复要专业、准确、易懂
            6. 回复格式要结构化，便于阅读
            
            注意：你的建议仅供参考，不能替代医生的专业诊断。
            """;

        String userMessage = "请分析以下健康数据：\n" + healthData;

        return chat(systemPrompt, userMessage);
    }

    /**
     * 生成个性化运动建议
     * @param userProfile 用户档案
     * @param fitnessGoal 健身目标
     * @return 运动建议
     */
    public String generateExercisePlan(String userProfile, String fitnessGoal) {
        String systemPrompt = """
            你是一个专业的健身教练和运动处方师。请根据用户的基本信息和健身目标，
            制定个性化的运动计划。
            
            要求：
            1. 考虑用户的年龄、体重、身高、健康状况
            2. 根据目标制定合适的运动强度和频率
            3. 推荐具体的运动项目和时长
            4. 提供运动注意事项和安全提醒
            5. 给出循序渐进的计划安排
            
            运动计划要科学、安全、可操作性强。
            """;

        String userMessage = String.format(
                "用户信息：%s\n健身目标：%s\n请制定详细的运动计划。",
                userProfile, fitnessGoal
        );

        return chat(systemPrompt, userMessage);
    }

    /**
     * 生成营养建议
     * @param healthData 健康数据
     * @param dietaryGoal 饮食目标
     * @return 营养建议
     */
    public String generateNutritionAdvice(String healthData, String dietaryGoal) {
        String systemPrompt = """
            你是一个专业的营养师。请根据用户的健康数据和饮食目标，
            提供个性化的营养建议。
            
            要求：
            1. 分析用户当前的营养状况
            2. 推荐合适的饮食结构和食物选择
            3. 提供具体的营养摄入建议
            4. 给出饮食注意事项
            5. 推荐健康的饮食习惯
            
            建议要科学、实用、易于执行。
            """;

        String userMessage = String.format(
                "健康数据：%s\n饮食目标：%s\n请提供营养建议。",
                healthData, dietaryGoal
        );

        return chat(systemPrompt, userMessage);
    }

    /**
     * 睡眠质量分析
     * @param sleepData 睡眠数据
     * @return 睡眠分析结果
     */
    public String analyzeSleepQuality(String sleepData) {
        String systemPrompt = """
            你是一个专业的睡眠医学专家。请根据用户的睡眠数据，
            分析睡眠质量并提供改善建议。
            
            要求：
            1. 评估睡眠时长、深睡眠比例、睡眠效率等指标
            2. 分析可能的睡眠问题
            3. 提供具体的睡眠改善建议
            4. 推荐良好的睡眠习惯
            5. 必要时建议寻求专业医疗帮助
            
            分析要专业、全面、实用。
            """;

        String userMessage = "请分析以下睡眠数据：\n" + sleepData;

        return chat(systemPrompt, userMessage);
    }
}