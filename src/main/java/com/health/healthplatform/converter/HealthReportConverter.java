package com.health.healthplatform.converter;

import com.health.healthplatform.DTO.HealthReport.HealthReportDTO;
import com.health.healthplatform.entity.HealthReport.HealthReport;
import org.springframework.beans.BeanUtils;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class HealthReportConverter {

    /**
     * 将实体类转换为DTO
     */
    public static HealthReportDTO toDTO(HealthReport entity) {
        if (entity == null) {
            return null;
        }

        HealthReportDTO dto = HealthReportDTO.builder()
                .userId(entity.getUserId())
                .reportTime(entity.getReportTime())
                .bmi(entity.getBmi())
                .bmiStatus(entity.getBmiStatus())
                .weight(entity.getWeight())
                .height(entity.getHeight())
                .systolic(entity.getSystolic())
                .diastolic(entity.getDiastolic())
                .bloodPressureStatus(entity.getBloodPressureStatus())
                .heartRate(entity.getHeartRate())
                .heartRateStatus(entity.getHeartRateStatus())
                .weeklyExerciseDuration(entity.getWeeklyExerciseDuration())
                .weeklyExerciseCount(entity.getWeeklyExerciseCount())
                .weeklyCaloriesBurned(entity.getWeeklyCaloriesBurned())
                .exerciseGoalAchieved(entity.getExerciseGoalAchieved())
                .dailySteps(entity.getDailySteps())
                .dailyDistance(entity.getDailyDistance())
                .stepsGoalAchieved(entity.getStepsGoalAchieved())
                .averageSleepDuration(entity.getAverageSleepDuration())
                .deepSleepPercentage(entity.getDeepSleepPercentage())
                .lightSleepPercentage(entity.getLightSleepPercentage())
                .remSleepPercentage(entity.getRemSleepPercentage())
                .sleepQuality(entity.getSleepQuality())
                .overallScore(entity.getOverallScore())
                .build();

        // 转换健康建议字符串为List
        dto.setHealthSuggestions(convertStringToList(entity.getHealthSuggestions()));
        // 转换异常指标字符串为List
        dto.setAbnormalIndicators(convertStringToList(entity.getAbnormalIndicators()));

        return dto;
    }

    /**
     * 将DTO转换为实体类
     */
    public static HealthReport toEntity(HealthReportDTO dto) {
        if (dto == null) {
            return null;
        }

        HealthReport entity = new HealthReport();
        BeanUtils.copyProperties(dto, entity);

        // 转换List为字符串存储
        entity.setHealthSuggestions(convertListToString(dto.getHealthSuggestions()));
        entity.setAbnormalIndicators(convertListToString(dto.getAbnormalIndicators()));

        // 设置创建和更新时间
        LocalDateTime now = LocalDateTime.now();
        entity.setCreateTime(now);
        entity.setUpdateTime(now);

        return entity;
    }

    /**
     * 将逗号分隔的字符串转换为List
     */
    private static List<String> convertStringToList(String str) {
        if (str == null || str.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(str.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * 将List转换为逗号分隔的字符串
     */
    private static String convertListToString(List<String> list) {
        if (list == null || list.isEmpty()) {
            return "";
        }
        return list.stream()
                .filter(s -> s != null && !s.trim().isEmpty())
                .collect(Collectors.joining(","));
    }

    /**
     * 更新实体类
     */
    public static void updateEntity(HealthReport entity, HealthReportDTO dto) {
        if (entity == null || dto == null) {
            return;
        }

        BeanUtils.copyProperties(dto, entity, "id", "createTime", "updateTime");
        
        // 更新字符串类型的字段
        entity.setHealthSuggestions(convertListToString(dto.getHealthSuggestions()));
        entity.setAbnormalIndicators(convertListToString(dto.getAbnormalIndicators()));
        
        // 更新更新时间
        entity.setUpdateTime(LocalDateTime.now());
    }
}