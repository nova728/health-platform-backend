package com.health.healthplatform.service.health_data;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.health.healthplatform.DTO.BloodPressureDTO;
import com.health.healthplatform.entity.healthdata.BloodPressureHistory;
import com.health.healthplatform.entity.healthdata.HealthData;
import com.health.healthplatform.mapper.health_data.BloodPressureHistoryMapper;
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
public class BloodPressureService {
    private final BloodPressureHistoryMapper bloodPressureHistoryMapper;

    @Resource
    HealthDataMapper healthDataMapper;

    public BloodPressureService(BloodPressureHistoryMapper bloodPressureHistoryMapper) {
        this.bloodPressureHistoryMapper = bloodPressureHistoryMapper;
    }

    public List<BloodPressureDTO> getUserBloodPressureHistory(Integer userId, String period) {
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

        log.info("Fetching blood pressure data for user {} from {} to {}", userId, startTime, endTime);

        List<BloodPressureHistory> bloodPressureHistories = bloodPressureHistoryMapper.findByTimeRange(
                userId, startTime, endTime
        );

        return bloodPressureHistories.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private BloodPressureDTO convertToDTO(BloodPressureHistory entity) {
        BloodPressureDTO dto = new BloodPressureDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    public BloodPressureDTO recordBloodPressure(Integer userId, Double systolic, Double diastolic) {
        log.info("Recording blood pressure - systolic: {}, diastolic: {} for user {}",
                systolic, diastolic, userId);

        // 验证血压数据
        validateBloodPressure(systolic, diastolic);

        // 创建新的血压记录
        BloodPressureHistory bloodPressureHistory = new BloodPressureHistory();
        bloodPressureHistory.setUserId(userId);
        bloodPressureHistory.setSystolic(systolic);
        bloodPressureHistory.setDiastolic(diastolic);
        bloodPressureHistory.setCreateTime(LocalDateTime.now());
        bloodPressureHistory.setUpdateTime(LocalDateTime.now());
        bloodPressureHistory.setMeasurementTime(LocalDateTime.now());

        // 保存记录
        bloodPressureHistoryMapper.insert(bloodPressureHistory);
        log.info("Successfully recorded blood pressure with id: {}", bloodPressureHistory.getId());

        // 更新健康数据总表
        try {
            HealthData healthData = healthDataMapper.findByUserId(userId);

            if (healthData == null) {
                log.info("No existing health data found for user: {}", userId);
            } else {
                UpdateWrapper<HealthData> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("user_id", userId)
                        .set("blood_pressure_systolic", systolic.intValue())
                        .set("blood_pressure_diastolic", diastolic.intValue())
                        .set("update_time", LocalDateTime.now());

                healthDataMapper.update(null, updateWrapper);
                log.info("Updated blood pressure in health data for user: {}", userId);
            }
        } catch (Exception e) {
            log.error("Error updating health data blood pressure: {}", e.getMessage());
        }

        return convertToDTO(bloodPressureHistory);
    }

    private void validateBloodPressure(Double systolic, Double diastolic) {
        if (systolic == null || systolic < 60 || systolic > 200) {
            throw new IllegalArgumentException("收缩压必须在60-200之间");
        }
        if (diastolic == null || diastolic < 40 || diastolic > 130) {
            throw new IllegalArgumentException("舒张压必须在40-130之间");
        }
        if (systolic <= diastolic) {
            throw new IllegalArgumentException("收缩压必须大于舒张压");
        }
    }
}