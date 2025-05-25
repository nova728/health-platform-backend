package com.health.healthplatform.service.health_data;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.health.healthplatform.DTO.HeartRateDTO;
import com.health.healthplatform.entity.healthdata.HealthData;
import com.health.healthplatform.entity.healthdata.HeartRateHistory;
import com.health.healthplatform.mapper.health_data.HealthDataMapper;
import com.health.healthplatform.mapper.health_data.HeartRateHistoryMapper;
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
public class HeartRateService {
    private final HeartRateHistoryMapper heartRateHistoryMapper;

    @Resource
    HealthDataMapper healthDataMapper;

    public HeartRateService(HeartRateHistoryMapper heartRateHistoryMapper) {
        this.heartRateHistoryMapper = heartRateHistoryMapper;
    }

    public List<HeartRateDTO> getUserHeartRateHistory(Integer userId, String period) {
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime;

        // 根据period参数确定时间范围
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
                // 默认返回24小时的数据
                startTime = endTime.minus(24, ChronoUnit.HOURS);
                log.warn("Invalid period: {}, using default 24 hours", period);
        }

        log.info("Fetching heart rate data for user {} from {} to {}", userId, startTime, endTime);

        List<HeartRateHistory> heartRateHistories = heartRateHistoryMapper.findByTimeRange(
                userId, startTime, endTime
        );

        return heartRateHistories.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private HeartRateDTO convertToDTO(HeartRateHistory entity) {
        HeartRateDTO dto = new HeartRateDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    public HeartRateDTO recordHeartRate(Integer userId, Double heartRate) {
        log.info("Recording heart rate {} for user {}", heartRate, userId);

        // 参数验证
        if (heartRate < 0 || heartRate > 250) {
            log.error("Invalid heart rate value: {}", heartRate);
            throw new IllegalArgumentException("心率数据必须在0到250之间");
        }

        // 创建新的心率记录
        HeartRateHistory heartRateHistory = new HeartRateHistory();
        heartRateHistory.setUserId(userId);
        heartRateHistory.setHeartRate(heartRate);
        heartRateHistory.setMeasurementTime(LocalDateTime.now());
        heartRateHistory.setCreateTime(LocalDateTime.now());
        heartRateHistory.setCreateTime(LocalDateTime.now());
        heartRateHistory.setUpdateTime(LocalDateTime.now());

        // 保存记录
        heartRateHistoryMapper.insert(heartRateHistory);
        log.info("Successfully recorded heart rate with id: {}", heartRateHistory.getId());

        try {
            HealthData healthData = healthDataMapper.findByUserId(userId);

            if (healthData == null) {
                // TODO:如果当天没有记录，创建新记录
                log.info("Created new health data record with heart rate for user: {}", userId);
            } else {
                UpdateWrapper<HealthData> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("user_id", userId)
                        .set("heart_rate", heartRate.intValue())
                        .set("update_time", LocalDateTime.now());

                healthDataMapper.update(null, updateWrapper);
                log.info("Updated heart rate in health data for user: {}", userId);
            }
        } catch (Exception e) {
            log.error("Error updating health data heart rate: {}", e.getMessage());
        }

        return convertToDTO(heartRateHistory);
    }
}