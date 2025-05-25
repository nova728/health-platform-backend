package com.health.healthplatform.service.health_data;

import com.health.healthplatform.DTO.StepsDTO;
import com.health.healthplatform.entity.healthdata.StepsHistory;
import com.health.healthplatform.mapper.health_data.StepsHistoryMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class StepsService {
    private final StepsHistoryMapper stepsHistoryMapper;

    public StepsService(StepsHistoryMapper stepsHistoryMapper) {
        this.stepsHistoryMapper = stepsHistoryMapper;
    }

    public List<StepsDTO> getUserStepsHistory(Integer userId, String period) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate;

        switch (period) {
            case "day":
                startDate = endDate;
                break;
            case "week":
                startDate = endDate.minus(7, ChronoUnit.DAYS);
                break;
            case "month":
                startDate = endDate.minus(30, ChronoUnit.DAYS);
                break;
            default:
                startDate = endDate;
                log.warn("Invalid period: {}, using default day", period);
        }

        log.info("Fetching steps data for user {} from {} to {}", userId, startDate, endDate);

        List<StepsHistory> stepsHistories = stepsHistoryMapper.findByDateRange(
                userId, startDate, endDate
        );

        return stepsHistories.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public StepsDTO recordSteps(Integer userId, Integer steps) {
        log.info("Recording steps {} for user {}", steps, userId);

        if (steps < 0 || steps > 100000) {
            log.error("Invalid steps value: {}", steps);
            throw new IllegalArgumentException("步数必须在0到100000之间");
        }

        LocalDate today = LocalDate.now();

        // 计算消耗的卡路里（假设每1000步消耗40卡路里）
        double calories = steps * 0.04;
        // 计算距离（假设每步0.7米）
        double distance = steps * 0.7;

        StepsHistory stepsHistory = new StepsHistory();
        stepsHistory.setUserId(userId);
        stepsHistory.setSteps(steps);
        stepsHistory.setCalories(calories);
        stepsHistory.setDistance(distance);
        stepsHistory.setCreateTime(LocalDateTime.now());
        stepsHistory.setUpdateTime(LocalDateTime.now());
        stepsHistory.setRecordDate(today);
        stepsHistory.setTarget(12000); // 默认目标步数

        stepsHistoryMapper.insert(stepsHistory);
        log.info("Successfully recorded steps with id: {}", stepsHistory.getId());

        return convertToDTO(stepsHistory);
    }

    private StepsDTO convertToDTO(StepsHistory entity) {
        StepsDTO dto = new StepsDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
}