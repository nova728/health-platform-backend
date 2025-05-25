package com.health.healthplatform.service;

import com.health.healthplatform.entity.healthdata.ExerciseRecord;
import com.health.healthplatform.mapper.health_data.ExerciseRecordMapper;
import com.health.healthplatform.DTO.AchievementDTO;
import com.health.healthplatform.DTO.UserRankDTO;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.*;
import java.time.LocalDate;

@Service
@Slf4j
public class ExerciseAchievementService {
    private final ExerciseRecordMapper exerciseRecordMapper;

    // 定义各个成就称号的时长阈值(分钟)
    private static final Map<String, Integer> ACHIEVEMENT_THRESHOLDS = Map.of(
            "跑步达人", 3000,  // 累计跑步50小时
            "游泳健将", 1800,  // 累计游泳30小时
            "骑行先锋", 3600,  // 累计骑行60小时
            "健身达人", 2400,  // 累计健身40小时
            "瑜伽大师", 1800,  // 累计瑜伽30小时
            "运动达人", 6000   // 累计运动100小时
    );

    public ExerciseAchievementService(ExerciseRecordMapper exerciseRecordMapper) {
        this.exerciseRecordMapper = exerciseRecordMapper;
    }

    // 获取用户排名列表
    public List<UserRankDTO> getUserRankings(String timeRange) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate;

        // 根据时间范围获取起始日期
        switch (timeRange) {
            case "daily" -> startDate = endDate;
            case "weekly" -> startDate = endDate.minusWeeks(1);
            case "monthly" -> startDate = endDate.minusMonths(1);
            default -> startDate = endDate;
        }

        List<UserRankDTO> rankings = exerciseRecordMapper.getUserRankings(startDate, endDate);

        // 计算排名
        for (int i = 0; i < rankings.size(); i++) {
            rankings.get(i).setRank(i + 1);
        }

        return rankings;
    }

    // 获取用户成就列表
    public List<AchievementDTO> getUserAchievements(Integer userId) {
        try {
            // 获取用户各项运动的累计时长
            List<Map<String, Object>> durationList = exerciseRecordMapper.getTotalDurationByType(userId);

            // 转换为 Map 格式，同时处理类型转换
            Map<String, Integer> totalDurations = new HashMap<>();
            for (Map<String, Object> record : durationList) {
                String type = (String) record.get("TYPE");
                // 处理 BigDecimal 转 Integer
                BigDecimal duration = (BigDecimal) record.get("DURATION");
                totalDurations.put(type, duration != null ? duration.intValue() : 0);
            }

            log.debug("用户运动时长统计: {}", totalDurations);

            List<AchievementDTO> achievements = new ArrayList<>();

            // 检查每个成就是否达成
            ACHIEVEMENT_THRESHOLDS.forEach((name, threshold) -> {
                boolean unlocked = false;
                String exerciseType = name.substring(0, 2); // 提取运动类型

                if (name.equals("运动达人")) {
                    // 运动达人需要计算所有运动时长之和
                    int totalDuration = totalDurations.values().stream()
                            .mapToInt(Integer::intValue)
                            .sum();
                    unlocked = totalDuration >= threshold;
                    log.debug("运动达人成就检查 - 总时长: {}, 阈值: {}", totalDuration, threshold);
                } else {
                    // 特定运动类型的成就
                    Integer duration = totalDurations.getOrDefault(exerciseType, 0);
                    unlocked = duration >= threshold;
                    log.debug("{}成就检查 - 时长: {}, 阈值: {}", name, duration, threshold);
                }

                achievements.add(new AchievementDTO(
                        name,
                        getAchievementDescription(name, threshold),
                        getAchievementIcon(name),
                        unlocked
                ));
            });

            return achievements;
        } catch (Exception e) {
            log.error("获取用户成就失败: ", e);
            throw e;
        }
    }

    // 获取成就描述
    private String getAchievementDescription(String name, int threshold) {
        int hours = threshold / 60;
        return switch (name) {
            case "运动达人" -> String.format("累计运动时长达到%d小时", hours);
            default -> String.format("累计%s时长达到%d小时", name.substring(0, 2), hours);
        };
    }

    // 获取成就图标
    private String getAchievementIcon(String name) {
        return switch (name) {
            case "跑步达人" -> "Timer";
            case "游泳健将" -> "Ship";
            case "骑行先锋" -> "Bicycle";
            case "健身达人" -> "User";
            case "瑜伽大师" -> "Magic";
            case "运动达人" -> "Trophy";
            default -> "Medal";
        };
    }
}