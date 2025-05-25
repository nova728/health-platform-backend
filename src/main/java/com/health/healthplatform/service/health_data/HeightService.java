package com.health.healthplatform.service.health_data;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.health.healthplatform.DTO.HeightDTO;
import com.health.healthplatform.entity.healthdata.HeightHistory;
import com.health.healthplatform.entity.healthdata.HealthData;
import com.health.healthplatform.mapper.health_data.HeightHistoryMapper;
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
public class HeightService {
    private final HeightHistoryMapper heightHistoryMapper;

    @Resource
    private HealthDataMapper healthDataMapper;

    @Resource
    private BmiService bmiService;

    public HeightService(HeightHistoryMapper heightHistoryMapper) {
        this.heightHistoryMapper = heightHistoryMapper;
    }

    public List<HeightDTO> getUserHeightHistory(Integer userId, String period) {
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

        log.info("Fetching height data for user {} from {} to {}", userId, startTime, endTime);

        List<HeightHistory> heightHistories = heightHistoryMapper.findByTimeRange(
                userId, startTime, endTime
        );

        return heightHistories.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private HeightDTO convertToDTO(HeightHistory entity) {
        HeightDTO dto = new HeightDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    public HeightDTO recordHeight(Integer userId, Double height) {
        log.info("Recording height {} for user {}", height, userId);

        validateHeight(height);

        HeightHistory heightHistory = new HeightHistory();
        heightHistory.setUserId(userId);
        heightHistory.setHeight(height);
        heightHistory.setMeasurementTime(LocalDateTime.now());
        heightHistory.setCreateTime(LocalDateTime.now());
        heightHistory.setUpdateTime(LocalDateTime.now());

        heightHistoryMapper.insert(heightHistory);
        log.info("Successfully recorded height with id: {}", heightHistory.getId());

        // 更新健康数据总表
        updateHealthDataAndBmi(userId, height);

        return convertToDTO(heightHistory);
    }

    private void updateHealthDataAndBmi(Integer userId, Double height) {
        try {
            HealthData healthData = healthDataMapper.findByUserId(userId);
            if (healthData != null && healthData.getWeight() != null) {
                // 有体重数据时自动计算BMI
                double heightM = height / 100.0;
                double bmi = Math.round((healthData.getWeight() / (heightM * heightM)) * 10) / 10.0;
                bmiService.recordBmi(userId, bmi, healthData.getWeight(), height);
            }
            // 更新身高数据
            UpdateWrapper<HealthData> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("user_id", userId)
                    .set("height", height)
                    .set("update_time", LocalDateTime.now());
            healthDataMapper.update(null, updateWrapper);
        } catch (Exception e) {
            log.error("Error updating health data and BMI: {}", e.getMessage());
        }
    }

    private void validateHeight(Double height) {
        if (height == null || height < 50 || height > 300) {
            throw new IllegalArgumentException("身高必须在50-300cm之间");
        }
    }
}