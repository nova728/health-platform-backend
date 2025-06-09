package com.health.healthplatform.service.HealthReport;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.health.healthplatform.entity.healthdata.HealthData;
import com.health.healthplatform.entity.HealthReport.HealthReport;
import com.health.healthplatform.mapper.health_data.HealthDataMapper;
import com.health.healthplatform.mapper.HealthReport.HealthReportMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 健康数据自动同步服务
 * 将health_data表中的数据自动同步到health_reports表
 */
@Slf4j
@Service
public class HealthDataSyncService {

    @Autowired
    private HealthDataMapper healthDataMapper;
    
    @Autowired
    private HealthReportMapper healthReportMapper;
    
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 手动触发数据同步
     * @param userId 用户ID
     * @param reportDate 报告日期
     */
    @Transactional
    public void syncHealthDataToReport(Integer userId, LocalDate reportDate) {
        try {
            log.info("开始同步用户{}在{}的健康数据", userId, reportDate);              // 查询指定用户和日期的健康数据
            List<HealthData> healthDataList = healthDataMapper.findByUserIdAndDate(userId, reportDate);
            
            if (healthDataList.isEmpty()) {
                log.warn("用户{}在{}没有健康数据记录", userId, reportDate);
                return;
            }
            
            // 检查是否已存在该日期的健康报告
            QueryWrapper<HealthReport> reportQuery = new QueryWrapper<>();
            reportQuery.eq("user_id", userId)
                       .between("report_time", 
                               reportDate.atStartOfDay(), 
                               reportDate.atTime(23, 59, 59));
            
            HealthReport existingReport = healthReportMapper.selectOne(reportQuery);
            
            if (existingReport != null) {
                log.info("用户{}在{}已存在健康报告，将进行更新", userId, reportDate);
                updateHealthReportFromData(existingReport, healthDataList);
                healthReportMapper.updateById(existingReport);
            } else {
                log.info("为用户{}在{}创建新的健康报告", userId, reportDate);
                HealthReport newReport = createHealthReportFromData(userId, reportDate, healthDataList);
                healthReportMapper.insert(newReport);
            }
            
            log.info("用户{}在{}的健康数据同步完成", userId, reportDate);
            
        } catch (Exception e) {
            log.error("同步健康数据失败: userId={}, reportDate={}", userId, reportDate, e);
            throw new RuntimeException("健康数据同步失败", e);
        }
    }

    /**
     * 定时任务：每日凌晨2点自动同步前一天的数据
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void autoSyncHealthData() {
        try {
            LocalDate yesterday = LocalDate.now().minusDays(1);
            log.info("开始自动同步{}的健康数据", yesterday);
            
            // 获取昨天有健康数据记录的所有用户
            QueryWrapper<HealthData> query = new QueryWrapper<>();
            query.eq("record_date", yesterday)
                 .select("DISTINCT user_id");
            
            List<HealthData> userList = healthDataMapper.selectList(query);
              for (HealthData data : userList) {
                try {
                    syncHealthDataToReport(data.getUserId().intValue(), yesterday);
                } catch (Exception e) {
                    log.error("自动同步用户{}的健康数据失败", data.getUserId(), e);
                }
            }
            
            log.info("自动同步{}的健康数据完成，共处理{}个用户", yesterday, userList.size());
            
        } catch (Exception e) {
            log.error("自动同步健康数据失败", e);
        }
    }

    /**
     * 同步指定用户的健康数据（默认同步最近7天的数据）
     * @param userId 用户ID
     */
    @Transactional
    public void syncUserData(Integer userId) {
        try {
            log.info("开始同步用户{}的健康数据", userId);
            
            // 同步最近7天的数据
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(7);
            
            for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                syncHealthDataToReport(userId, date);
            }
            
            log.info("用户{}的健康数据同步完成", userId);
            
        } catch (Exception e) {
            log.error("同步用户{}的健康数据失败", userId, e);
            throw new RuntimeException("健康数据同步失败", e);
        }
    }

    /**
     * 从健康数据创建健康报告
     */
    private HealthReport createHealthReportFromData(Integer userId, LocalDate reportDate, List<HealthData> healthDataList) {
        HealthReport report = new HealthReport();
        
        // 基本信息
        report.setUserId(userId);
        report.setReportTime(reportDate.atTime(23, 59, 59));
        report.setCreateTime(LocalDateTime.now());
        report.setUpdateTime(LocalDateTime.now());
        report.setDeleted(0);
        
        // 聚合健康数据
        updateHealthReportFromData(report, healthDataList);
        
        return report;
    }

    /**
     * 从健康数据更新健康报告
     */
    private void updateHealthReportFromData(HealthReport report, List<HealthData> healthDataList) {
        List<String> abnormalIndicators = new ArrayList<>();
        
        // 获取最新的数据记录（按时间排序）
        HealthData latestData = healthDataList.get(healthDataList.size() - 1);
        
        // 更新基础健康指标
        if (latestData.getBmi() != null) {
            report.setBmi(latestData.getBmi().doubleValue());
            report.setBmiStatus(getBmiStatus(latestData.getBmi().doubleValue()));
            
            if (!"正常".equals(report.getBmiStatus())) {
                abnormalIndicators.add("BMI: " + report.getBmiStatus());
            }
        }
        
        if (latestData.getWeight() != null) {
            report.setWeight(latestData.getWeight().doubleValue());
        }
        
        if (latestData.getHeight() != null) {
            report.setHeight(latestData.getHeight());
        }
        
        // 更新血压数据
        if (latestData.getBloodPressureSystolic() != null && latestData.getBloodPressureDiastolic() != null) {
            report.setSystolic(latestData.getBloodPressureSystolic().doubleValue());
            report.setDiastolic(latestData.getBloodPressureDiastolic().doubleValue());
            
            String bpStatus = getBloodPressureStatus(
                latestData.getBloodPressureSystolic().doubleValue(), 
                latestData.getBloodPressureDiastolic().doubleValue()
            );
            report.setBloodPressureStatus(bpStatus);
            
            if (!"正常".equals(bpStatus)) {
                abnormalIndicators.add("血压: " + bpStatus);
            }
        }
        
        // 更新心率数据
        if (latestData.getHeartRate() != null) {
            report.setHeartRate(latestData.getHeartRate().doubleValue());
            
            String hrStatus = getHeartRateStatus(latestData.getHeartRate().doubleValue());
            report.setHeartRateStatus(hrStatus);
            
            if (!"正常".equals(hrStatus)) {
                abnormalIndicators.add("心率: " + hrStatus);
            }
        }
        
        // 更新步数数据（取当天最大值）
        int maxSteps = healthDataList.stream()
            .filter(data -> data.getSteps() != null)
            .mapToInt(HealthData::getSteps)
            .max()
            .orElse(0);
        
        if (maxSteps > 0) {
            report.setDailySteps(maxSteps);
            
            // 计算距离（假设每步0.7米）
            double distance = maxSteps * 0.0007; // 转换为公里
            report.setDailyDistance(distance);
            
            // 判断是否达到步数目标（一般建议10000步）
            report.setStepsGoalAchieved(maxSteps >= 10000);
        }
        
        // 更新睡眠数据（取平均值）
        if (latestData.getSleepDuration() != null) {
            report.setAverageSleepDuration(latestData.getSleepDuration().doubleValue());
            
            if (latestData.getSleepQuality() != null) {
                report.setSleepQuality(latestData.getSleepQuality());
                
                // 简化的睡眠质量评估
                if ("差".equals(latestData.getSleepQuality()) || 
                    latestData.getSleepDuration().doubleValue() < 6) {
                    abnormalIndicators.add("睡眠质量: " + latestData.getSleepQuality());
                }
            }
        }
        
        // 计算整体健康评分
        int overallScore = calculateOverallScore(report);
        report.setOverallScore(overallScore);
        
        // 生成健康建议
        List<String> suggestions = generateHealthSuggestions(report, abnormalIndicators);
        
        try {
            report.setHealthSuggestions(objectMapper.writeValueAsString(suggestions));
            report.setAbnormalIndicators(objectMapper.writeValueAsString(abnormalIndicators));
        } catch (Exception e) {
            log.error("序列化健康建议或异常指标失败", e);
            report.setHealthSuggestions("[]");
            report.setAbnormalIndicators("[]");
        }
        
        report.setUpdateTime(LocalDateTime.now());
    }

    /**
     * 获取BMI状态
     */    private String getBmiStatus(double bmi) {
        if (bmi <= 18.4) {
            return "偏瘦";
        } else if (bmi < 24.0) {
            return "正常";
        } else if (bmi >= 24.0 && bmi <= 27.9) {
            return "超重";
        } else if (bmi >= 28.0) {
            return "肥胖";
        }
        return "正常";
    }

    /**
     * 获取血压状态
     */    private String getBloodPressureStatus(double systolic, double diastolic) {
        if (systolic < 90 || diastolic < 60) {
            return "血压偏低";
        } else if (systolic >= 140) {
            return "血压偏高";
        } else {
            return "正常";
        }
    }

    /**
     * 获取心率状态
     */
    private String getHeartRateStatus(double heartRate) {
        if (heartRate < 60) {
            return "偏低";
        } else if (heartRate <= 100) {
            return "正常";
        } else {
            return "偏高";
        }
    }

    /**
     * 计算整体健康评分
     */
    private int calculateOverallScore(HealthReport report) {
        int score = 100;
        
        // BMI评分
        if (report.getBmi() != null) {
            if (!"正常".equals(report.getBmiStatus())) {
                score -= 15;
            }
        }
        
        // 血压评分
        if (!"正常".equals(report.getBloodPressureStatus())) {
            score -= 20;
        }
        
        // 心率评分
        if (!"正常".equals(report.getHeartRateStatus())) {
            score -= 15;
        }
        
        // 步数评分
        if (report.getDailySteps() != null && report.getDailySteps() < 8000) {
            score -= 15;
        }
        
        // 睡眠评分
        if (report.getAverageSleepDuration() != null && report.getAverageSleepDuration() < 7) {
            score -= 10;
        }
        
        return Math.max(score, 0);
    }

    /**
     * 生成健康建议
     */
    private List<String> generateHealthSuggestions(HealthReport report, List<String> abnormalIndicators) {
        List<String> suggestions = new ArrayList<>();
        
        if (report.getBmi() != null) {
            if ("偏瘦".equals(report.getBmiStatus())) {
                suggestions.add("适当增加营养摄入，建议咨询营养师制定增重计划");
            } else if ("超重".equals(report.getBmiStatus()) || "肥胖".equals(report.getBmiStatus())) {
                suggestions.add("建议控制饮食，增加运动量，必要时咨询专业医生");
            }
        }
        
        if (!"正常".equals(report.getBloodPressureStatus())) {
            suggestions.add("建议监测血压变化，保持良好作息，减少盐分摄入");
        }
        
        if (!"正常".equals(report.getHeartRateStatus())) {
            suggestions.add("建议适度运动，保持心情舒畅，如有不适及时就医");
        }
        
        if (report.getDailySteps() != null && report.getDailySteps() < 8000) {
            suggestions.add("建议增加日常活动量，每天至少步行8000步");
        }
        
        if (report.getAverageSleepDuration() != null && report.getAverageSleepDuration() < 7) {
            suggestions.add("建议保证充足睡眠，每晚至少7-8小时");
        }
        
        if (suggestions.isEmpty()) {
            suggestions.add("您的健康状况良好，请继续保持良好的生活习惯");
        }
        
        return suggestions;
    }
}
