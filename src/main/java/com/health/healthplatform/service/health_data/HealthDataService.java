package com.health.healthplatform.service.health_data;

import com.health.healthplatform.DTO.HealthDataDTO;
import com.health.healthplatform.entity.healthdata.HealthData;
import com.health.healthplatform.mapper.health_data.HealthDataMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class HealthDataService {
    private final HealthDataMapper healthDataMapper;

    public HealthDataService(HealthDataMapper healthDataMapper) {
        this.healthDataMapper = healthDataMapper;
    }

    public List<HealthDataDTO> getHealthHistory(Integer userId) {
        List<HealthData> healthDataList = healthDataMapper.findLatestDataByUserId(userId);
        log.info("Retrieved {} records from database for user {}", healthDataList.size(), userId);
        return healthDataList.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private HealthDataDTO convertToDTO(HealthData healthData) {
        HealthDataDTO dto = new HealthDataDTO();
        BeanUtils.copyProperties(healthData, dto);
        return dto;
    }
}