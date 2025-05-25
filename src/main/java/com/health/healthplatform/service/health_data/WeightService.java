package com.health.healthplatform.service.health_data;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.health.healthplatform.DTO.WeightDTO;
import com.health.healthplatform.entity.healthdata.WeightHistory;
import com.health.healthplatform.entity.healthdata.HealthData;
import com.health.healthplatform.mapper.health_data.WeightHistoryMapper;
import com.health.healthplatform.mapper.health_data.HealthDataMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class WeightService {
    private final WeightHistoryMapper weightHistoryMapper;

    @Resource
    private HealthDataMapper healthDataMapper;

    @Resource
    private BmiService bmiService;

    public WeightService(WeightHistoryMapper weightHistoryMapper) {
        this.weightHistoryMapper = weightHistoryMapper;
    }

    public List<WeightDTO> getUserWeightHistory(Integer userId, String period) {
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime;

        switch (period) {
            case "day":
                startTime = endTime.minus(24, ChronoUnit.HOURS);
                break;
            case "week":
                startTime = endTime.minus(7, ChronoUnit.DAYS);
                break;
            case "month":
                startTime = endTime.minus(30, ChronoUnit.DAYS);
                break;
            default:
                startTime = endTime.minus(24, ChronoUnit.HOURS);
                log.warn("Invalid period: {}, using default 24 hours", period);
        }

        log.info("Fetching weight data for user {} from {} to {}", userId, startTime, endTime);

        List<WeightHistory> weightHistories = weightHistoryMapper.findByTimeRange(
                userId, startTime, endTime
        );

        return weightHistories.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private WeightDTO convertToDTO(WeightHistory entity) {
        WeightDTO dto = new WeightDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    public WeightDTO recordWeight(Integer userId, Double weight) {
        log.info("Recording weight {} for user {}", weight, userId);

        validateWeight(weight);

        WeightHistory weightHistory = new WeightHistory();
        weightHistory.setUserId(userId);
        weightHistory.setWeight(weight);
        weightHistory.setMeasurementTime(LocalDateTime.now());
        weightHistory.setCreateTime(LocalDateTime.now());
        weightHistory.setUpdateTime(LocalDateTime.now());

        weightHistoryMapper.insert(weightHistory);
        log.info("Successfully recorded weight with id: {}", weightHistory.getId());

        // 更新健康数据总表并计算BMI
        updateHealthDataAndBmi(userId, weight);

        return convertToDTO(weightHistory);
    }

    private void updateHealthDataAndBmi(Integer userId, Double weight) {
        try {
            HealthData healthData = healthDataMapper.findByUserId(userId);
            if (healthData != null && healthData.getHeight() != null) {
                // 有身高数据时自动计算BMI
                double heightM = healthData.getHeight() / 100.0;
                double bmi = Math.round((weight / (heightM * heightM)) * 10) / 10.0;
                bmiService.recordBmi(userId, bmi, weight, healthData.getHeight());
            }
            // 更新体重数据
            UpdateWrapper<HealthData> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("user_id", userId)
                    .set("weight", weight)
                    .set("update_time", LocalDateTime.now());
            healthDataMapper.update(null, updateWrapper);
        } catch (Exception e) {
            log.error("Error updating health data and BMI: {}", e.getMessage());
        }
    }

    private void validateWeight(Double weight) {
        if (weight == null || weight < 20 || weight > 300) {
            throw new IllegalArgumentException("体重必须在20-300kg之间");
        }
    }
}