package com.health.healthplatform.service.health_data;

import com.health.healthplatform.DTO.SleepDTO;
import com.health.healthplatform.entity.healthdata.SleepHistory;
import com.health.healthplatform.mapper.health_data.SleepHistoryMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SleepService {
    private final SleepHistoryMapper sleepHistoryMapper;

    public SleepService(SleepHistoryMapper sleepHistoryMapper) {
        this.sleepHistoryMapper = sleepHistoryMapper;
    }

    public List<SleepDTO> getUserSleepHistory(Integer userId, String period) {
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime;

        switch (period) {
            case "day" -> startTime = endTime.minus(24, ChronoUnit.HOURS);
            case "week" -> startTime = endTime.minus(7, ChronoUnit.DAYS);
            case "month" -> startTime = endTime.minus(30, ChronoUnit.DAYS);
            default -> {
                startTime = endTime.minus(24, ChronoUnit.HOURS);
                log.warn("Invalid period: {}, using default 24 hours", period);
            }
        }

        List<SleepHistory> sleepHistories = sleepHistoryMapper.findByTimeRange(
                userId, startTime, endTime
        );

        return sleepHistories.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public SleepDTO recordSleep(Integer userId, Double duration, String quality) {
        if (duration < 0 || duration > 24) {
            throw new IllegalArgumentException("睡眠时长必须在0到24小时之间");
        }

        SleepHistory sleepHistory = new SleepHistory();
        sleepHistory.setUserId(userId);
        sleepHistory.setSleepDuration(duration);
        sleepHistory.setSleepQuality(quality);
        sleepHistory.setCreateTime(LocalDateTime.now());
        sleepHistory.setUpdateTime(LocalDateTime.now());

        // 根据睡眠时长估算各阶段时长
        double deepSleep = duration * 0.2;
        double remSleep = duration * 0.25;
        double lightSleep = duration - deepSleep - remSleep;

        sleepHistory.setDeepSleep(deepSleep);
        sleepHistory.setLightSleep(lightSleep);
        sleepHistory.setRemSleep(remSleep);

        LocalDateTime now = LocalDateTime.now();
        sleepHistory.setSleepEnd(now);
        sleepHistory.setSleepStart(now.minusHours((long) Math.floor(duration)));
        sleepHistory.setCreateTime(now);
        sleepHistory.setUpdateTime(now);

        sleepHistoryMapper.insert(sleepHistory);
        log.info("Successfully recorded sleep with id: {}", sleepHistory.getId());

        return convertToDTO(sleepHistory);
    }

    private SleepDTO convertToDTO(SleepHistory entity) {
        SleepDTO dto = new SleepDTO();
        dto.setId(entity.getId());
        dto.setUserId(entity.getUserId());
        dto.setDuration(entity.getSleepDuration());
        dto.setQuality(entity.getSleepQuality());

        double totalSleep = entity.getSleepDuration();
        dto.setDeepSleepPercentage(Double.parseDouble(String.format("%.2f", entity.getDeepSleep() / totalSleep * 100)));
        dto.setLightSleepPercentage(Double.parseDouble(String.format("%.2f", entity.getLightSleep() / totalSleep * 100)));
        dto.setRemSleepPercentage(Double.parseDouble(String.format("%.2f", entity.getRemSleep() / totalSleep * 100)));

        dto.setDate(entity.getSleepStart());
        return dto;
    }
}