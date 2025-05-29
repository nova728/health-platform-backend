package com.health.healthplatform.service.HealthReport;

import com.health.healthplatform.DTO.ExerciseGoalDTO;
import com.health.healthplatform.DTO.HealthReport.HealthReportDTO;
import com.health.healthplatform.entity.HealthReport.HealthReport;
import com.health.healthplatform.entity.HealthReport.HealthMetrics;
import com.health.healthplatform.entity.healthdata.ExerciseGoal;
import com.health.healthplatform.mapper.HealthReport.HealthReportMapper;
import com.health.healthplatform.service.health_data.ExerciseRecordService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.annotation.Resource;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class HealthReportService {
    @Resource
    private HealthDataCollectorService dataCollector;
    
    @Resource
    private HealthReportAnalysisService analysisService;
    
    @Resource
    private HealthReportMapper healthReportMapper;
    
    @Resource
    private ExerciseRecordService exerciseRecordService;
    
    @Resource
    private ObjectMapper objectMapper;

    @Transactional(rollbackFor = Exception.class)
    public HealthReportDTO generateReport(Integer userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("无效的用户ID");
        }
        
        log.info("开始为用户{}生成健康报告", userId);
        
        try {
            // 1. 收集健康数据
            HealthMetrics metrics = dataCollector.collectHealthData(userId);
            
            // 2. 获取用户运动目标
            ExerciseGoalDTO goalDto = exerciseRecordService.getUserGoals(userId);
            ExerciseGoal exerciseGoal = null;
            if (goalDto != null) {
                exerciseGoal = new ExerciseGoal();
                BeanUtils.copyProperties(goalDto, exerciseGoal);
            }
            
            // 3. 分析数据并生成报告
            HealthReport report = analysisService.analyzeHealthData(userId, metrics, goalDto);
            report.setReportTime(LocalDateTime.now());
            
            // 4. 保存报告到数据库
            healthReportMapper.insert(report);
            log.info("健康报告已保存，报告ID: {}", report.getId());
            
            // 5. 转换为DTO并返回
            return convertToDTO(report);
            
        } catch (Exception e) {
            log.error("生成健康报告时发生错误: {}", e.getMessage(), e);
            throw new RuntimeException("生成健康报告失败: " + e.getMessage(), e);
        }
    }

    public HealthReportDTO getLatestReport(Integer userId) {
        log.info("获取用户{}的最新健康报告", userId);
        HealthReport report = healthReportMapper.findLatestByUserId(userId);
        if (report == null) {
            log.info("未找到用户{}的健康报告", userId);
            return null;
        }
        return convertToDTO(report);
    }

    public List<HealthReportDTO> getReportHistory(Integer userId, LocalDateTime startTime, LocalDateTime endTime) {
        log.info("获取用户{}从{}到{}的健康报告历史", userId, startTime, endTime);
        List<HealthReport> reports = healthReportMapper.findByUserIdAndTimeRange(userId, startTime, endTime);
        return reports.stream().map(this::convertToDTO).toList();
    }

    private HealthReportDTO convertToDTO(HealthReport report) {
        try {
            HealthReportDTO dto = new HealthReportDTO();
            dto.setId(report.getId());
            dto.setUserId(report.getUserId());
            dto.setReportTime(report.getReportTime());
            
            // 复制基础健康指标
            dto.setBmi(report.getBmi());
            dto.setBmiStatus(report.getBmiStatus());
            dto.setWeight(report.getWeight());
            dto.setHeight(report.getHeight());
              // 复制血压指标
            dto.setSystolic(report.getSystolic() != null ? report.getSystolic().doubleValue() : null);
            dto.setDiastolic(report.getDiastolic() != null ? report.getDiastolic().doubleValue() : null);
            dto.setBloodPressureStatus(report.getBloodPressureStatus());
            
            // 复制心率指标
            dto.setHeartRate(report.getHeartRate() != null ? report.getHeartRate().doubleValue() : null);
            dto.setHeartRateStatus(report.getHeartRateStatus());
            
            // 复制运动指标
            dto.setWeeklyExerciseDuration(report.getWeeklyExerciseDuration());
            dto.setWeeklyExerciseCount(report.getWeeklyExerciseCount());
            dto.setWeeklyCaloriesBurned(report.getWeeklyCaloriesBurned());
            dto.setExerciseGoalAchieved(report.getExerciseGoalAchieved());
            
            // 复制步数指标
            dto.setDailySteps(report.getDailySteps());
            dto.setDailyDistance(report.getDailyDistance());
            dto.setStepsGoalAchieved(report.getStepsGoalAchieved());
            
            // 复制睡眠指标
            dto.setAverageSleepDuration(report.getAverageSleepDuration());
            dto.setDeepSleepPercentage(report.getDeepSleepPercentage());
            dto.setLightSleepPercentage(report.getLightSleepPercentage());
            dto.setRemSleepPercentage(report.getRemSleepPercentage());
            dto.setSleepQuality(report.getSleepQuality());
            
            // 复制评分和建议
            dto.setOverallScore(report.getOverallScore());
              // 转换JSON字符串为List，处理格式错误的JSON
            dto.setHealthSuggestions(parseJsonStringList(report.getHealthSuggestions()));
            dto.setAbnormalIndicators(parseJsonStringList(report.getAbnormalIndicators()));
            
            return dto;
        } catch (Exception e) {
            log.error("转换健康报告DTO时发生错误", e);
            throw new RuntimeException("转换健康报告失败", e);
        }
    }

    /**
     * 解析JSON字符串为字符串列表，处理格式错误的JSON
     * @param jsonString JSON字符串
     * @return 字符串列表
     */
    private List<String> parseJsonStringList(String jsonString) {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        try {
            // 先尝试直接解析
            return objectMapper.readValue(jsonString, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.warn("JSON格式错误，尝试修复: {}", jsonString);
            
            // 修复常见的JSON格式错误
            String fixedJson = fixJsonFormat(jsonString);
            
            try {
                return objectMapper.readValue(fixedJson, new TypeReference<List<String>>() {});
            } catch (Exception ex) {
                log.error("修复JSON失败，返回空列表. 原始JSON: {}", jsonString, ex);
                return new ArrayList<>();
            }
        }
    }
    
    /**
     * 修复JSON格式错误
     * @param jsonString 原始JSON字符串
     * @return 修复后的JSON字符串
     */
    private String fixJsonFormat(String jsonString) {
        String trimmed = jsonString.trim();
        
        // 如果不是以[开头，添加[
        if (!trimmed.startsWith("[")) {
            trimmed = "[" + trimmed;
        }
        
        // 如果不是以]结尾，添加]
        if (!trimmed.endsWith("]")) {
            trimmed = trimmed + "]";
        }
        
        // 移除最后一个逗号后紧跟]的情况 (例如: ["a","b",])
        trimmed = trimmed.replaceAll(",\\s*]", "]");
        
        // 移除多余的逗号
        trimmed = trimmed.replaceAll(",{2,}", ",");
        
        log.info("修复后的JSON: {}", trimmed);
        return trimmed;
    }
}