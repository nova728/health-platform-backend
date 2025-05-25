package com.health.healthplatform.service.health_data;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.health.healthplatform.DTO.BmiDTO;
import com.health.healthplatform.entity.healthdata.BmiHistory;
import com.health.healthplatform.entity.healthdata.HealthData;
import com.health.healthplatform.mapper.health_data.BmiHistoryMapper;
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
public class BmiService {
    private final BmiHistoryMapper bmiHistoryMapper;

    @Resource
    private HealthDataMapper healthDataMapper;

    public BmiService(BmiHistoryMapper bmiHistoryMapper) {
        this.bmiHistoryMapper = bmiHistoryMapper;
    }

    public List<BmiDTO> getUserBmiHistory(Integer userId, String period) {
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

        log.info("Fetching BMI data for user {} from {} to {}", userId, startTime, endTime);

        List<BmiHistory> bmiHistories = bmiHistoryMapper.findByTimeRange(
                userId, startTime, endTime
        );

        return bmiHistories.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private BmiDTO convertToDTO(BmiHistory entity) {
        BmiDTO dto = new BmiDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    public BmiDTO recordBmi(Integer userId, Double bmi, Double weight, Double height) {
        log.info("Recording BMI {} for user {}", bmi, userId);

        validateBmi(bmi);

        BmiHistory bmiHistory = new BmiHistory();
        bmiHistory.setUserId(userId);
        bmiHistory.setBmi(bmi);
        bmiHistory.setWeight(weight);
        bmiHistory.setHeight(height);
        bmiHistory.setMeasurementTime(LocalDateTime.now());
        bmiHistory.setCreateTime(LocalDateTime.now());
        bmiHistory.setUpdateTime(LocalDateTime.now());

        bmiHistoryMapper.insert(bmiHistory);
        log.info("Successfully recorded BMI with id: {}", bmiHistory.getId());

        // 更新健康数据总表
        updateHealthData(userId, bmi);

        return convertToDTO(bmiHistory);
    }

    private void updateHealthData(Integer userId, Double bmi) {
        try {
            UpdateWrapper<HealthData> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("user_id", userId)
                    .set("bmi", bmi)
                    .set("update_time", LocalDateTime.now());

            healthDataMapper.update(null, updateWrapper);
            log.info("Updated BMI in health data for user: {}", userId);
        } catch (Exception e) {
            log.error("Error updating health data BMI: {}", e.getMessage());
        }
    }

    private void validateBmi(Double bmi) {
        if (bmi == null || bmi < 10 || bmi > 50) {
            throw new IllegalArgumentException("BMI必须在10-50之间");
        }
    }
}