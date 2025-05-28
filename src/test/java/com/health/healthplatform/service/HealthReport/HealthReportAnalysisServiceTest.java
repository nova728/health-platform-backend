package com.health.healthplatform.service.HealthReport;

import com.health.healthplatform.DTO.ExerciseGoalDTO;
import com.health.healthplatform.entity.HealthReport.HealthMetrics;
import com.health.healthplatform.entity.HealthReport.HealthReport;
import com.health.healthplatform.mapper.HealthReport.HealthReportMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HealthReportAnalysisServiceTest {

    @Mock
    private HealthReportMapper healthReportMapper;

    @Mock
    private HealthDataCollectorService healthDataCollectorService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private HealthReportAnalysisService healthReportAnalysisService;

    private HealthMetrics testMetrics;
    private ExerciseGoalDTO testExerciseGoal;
    private Integer testUserId = 1;

    @BeforeEach
    void setUp() {
        // 准备测试数据
        testMetrics = new HealthMetrics();
        testMetrics.setHeight(175.0);
        testMetrics.setWeight(70.0);
        testMetrics.setSystolic(120.0);
        testMetrics.setDiastolic(80.0);
        testMetrics.setHeartRate(75.0);
        testMetrics.setWeeklyExerciseDuration(300.0);
        testMetrics.setWeeklyExerciseCount(5);
        testMetrics.setWeeklyCaloriesBurned(1500.0);
        testMetrics.setDailySteps(8000);
        testMetrics.setDailyDistance(6.4);
        testMetrics.setAverageSleepDuration(7.5);
        testMetrics.setDeepSleepPercentage(25.0);
        testMetrics.setLightSleepPercentage(65.0);
        testMetrics.setRemSleepPercentage(10.0);

        testExerciseGoal = new ExerciseGoalDTO();
        testExerciseGoal.setWeeklyDurationGoal(250.0);
        testExerciseGoal.setWeeklyCountGoal(3);
        testExerciseGoal.setDailyStepsGoal(10000);
    }

    @Test
    void testAnalyzeHealthData_Normal() {
        // 测试正常健康指标分析
        HealthReport result = healthReportAnalysisService.analyzeHealthData(testUserId, testMetrics, testExerciseGoal);
        
        assertNotNull(result);
        assertEquals(testUserId, result.getUserId());
        
        // 验证BMI计算和状态
        assertEquals(22.86, result.getBmi(), 0.01);
        assertEquals("正常", result.getBmiStatus());
        
        // 验证血压状态
        assertEquals("正常", result.getBloodPressureStatus());
        
        // 验证心率状态
        assertEquals("正常", result.getHeartRateStatus());
        
        // 验证睡眠质量
        assertEquals("良", result.getSleepQuality());
        
        // 验证运动目标达成
        assertTrue(result.getExerciseGoalAchieved());
        assertFalse(result.getStepsGoalAchieved()); // 8000 < 10000
        
        // 验证评分范围
        assertTrue(result.getOverallScore() >= 0 && result.getOverallScore() <= 100);
    }

    @Test
    void testAnalyzeHealthData_Abnormal() {
        // 测试异常健康指标
        testMetrics.setWeight(100.0); // 导致肥胖
        testMetrics.setSystolic(150.0); // 高血压
        testMetrics.setHeartRate(100.0); // 心率偏高
        testMetrics.setAverageSleepDuration(5.0); // 睡眠不足
        testMetrics.setWeeklyExerciseDuration(100.0); // 运动不足
        
        HealthReport result = healthReportAnalysisService.analyzeHealthData(testUserId, testMetrics, testExerciseGoal);
        
        assertNotNull(result);
        
        // 验证异常状态
        assertEquals("肥胖", result.getBmiStatus());
        assertEquals("高血压", result.getBloodPressureStatus());
        assertEquals("偏高", result.getHeartRateStatus());
        assertEquals("差", result.getSleepQuality());
        assertFalse(result.getExerciseGoalAchieved());
        
        // 异常情况下评分应该较低
        assertTrue(result.getOverallScore() < 70);
    }

    @Test
    void testAnalyzeHealthData_NullMetrics() {
        // 测试空指标数据
        assertThrows(IllegalArgumentException.class, () -> {
            healthReportAnalysisService.analyzeHealthData(testUserId, null, testExerciseGoal);
        });
    }

    @Test
    void testGenerateHealthReport_Success() throws Exception {
        // Mock数据收集服务
        when(healthDataCollectorService.collectHealthMetrics(testUserId)).thenReturn(testMetrics);
        when(healthDataCollectorService.getUserExerciseGoal(testUserId)).thenReturn(testExerciseGoal);
        when(healthReportMapper.insert(any(HealthReport.class))).thenReturn(1);
        
        HealthReport result = healthReportAnalysisService.generateHealthReport(testUserId);
        
        assertNotNull(result);
        assertEquals(testUserId, result.getUserId());
        assertNotNull(result.getReportTime());
        
        // 验证保存操作被调用
        verify(healthReportMapper, times(1)).insert(any(HealthReport.class));
        verify(healthDataCollectorService, times(1)).collectHealthMetrics(testUserId);
        verify(healthDataCollectorService, times(1)).getUserExerciseGoal(testUserId);
    }

    @Test
    void testGetLatestReport_Success() {
        // 准备Mock数据
        HealthReport mockReport = new HealthReport();
        mockReport.setUserId(testUserId);
        mockReport.setReportTime(LocalDateTime.now());
        mockReport.setOverallScore(85);
        
        when(healthReportMapper.findLatestByUserId(testUserId)).thenReturn(mockReport);
        
        HealthReport result = healthReportAnalysisService.getLatestReport(testUserId);
        
        assertNotNull(result);
        assertEquals(testUserId, result.getUserId());
        assertEquals(85, result.getOverallScore());
        
        verify(healthReportMapper, times(1)).findLatestByUserId(testUserId);
    }

    @Test
    void testGetReportHistory_Success() {
        // 准备Mock数据
        HealthReport report1 = new HealthReport();
        report1.setUserId(testUserId);
        report1.setReportTime(LocalDateTime.now().minusDays(1));
        report1.setOverallScore(80);
        
        HealthReport report2 = new HealthReport();
        report2.setUserId(testUserId);
        report2.setReportTime(LocalDateTime.now().minusDays(7));
        report2.setOverallScore(75);
        
        List<HealthReport> mockReports = Arrays.asList(report1, report2);
        
        when(healthReportMapper.findByUserIdAndTimeRange(eq(testUserId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(mockReports);
        
        List<HealthReport> result = healthReportAnalysisService.getReportHistory(testUserId, 30);
        
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(80, result.get(0).getOverallScore());
        assertEquals(75, result.get(1).getOverallScore());
        
        verify(healthReportMapper, times(1)).findByUserIdAndTimeRange(eq(testUserId), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void testBmiCalculation() {
        // 测试各种BMI分类
        
        // 偏瘦 (BMI < 18.5)
        testMetrics.setHeight(175.0);
        testMetrics.setWeight(55.0); // BMI = 17.96
        HealthReport result1 = healthReportAnalysisService.analyzeHealthData(testUserId, testMetrics, testExerciseGoal);
        assertEquals("偏瘦", result1.getBmiStatus());
        
        // 正常 (18.5 <= BMI < 24)
        testMetrics.setWeight(70.0); // BMI = 22.86
        HealthReport result2 = healthReportAnalysisService.analyzeHealthData(testUserId, testMetrics, testExerciseGoal);
        assertEquals("正常", result2.getBmiStatus());
        
        // 超重 (24 <= BMI < 28)
        testMetrics.setWeight(80.0); // BMI = 26.12
        HealthReport result3 = healthReportAnalysisService.analyzeHealthData(testUserId, testMetrics, testExerciseGoal);
        assertEquals("超重", result3.getBmiStatus());
        
        // 肥胖 (BMI >= 28)
        testMetrics.setWeight(90.0); // BMI = 29.39
        HealthReport result4 = healthReportAnalysisService.analyzeHealthData(testUserId, testMetrics, testExerciseGoal);
        assertEquals("肥胖", result4.getBmiStatus());
    }

    @Test
    void testBloodPressureAnalysis() {
        // 低血压
        testMetrics.setSystolic(90.0);
        testMetrics.setDiastolic(60.0);
        HealthReport result1 = healthReportAnalysisService.analyzeHealthData(testUserId, testMetrics, testExerciseGoal);
        assertEquals("低血压", result1.getBloodPressureStatus());
        
        // 正常血压
        testMetrics.setSystolic(120.0);
        testMetrics.setDiastolic(80.0);
        HealthReport result2 = healthReportAnalysisService.analyzeHealthData(testUserId, testMetrics, testExerciseGoal);
        assertEquals("正常", result2.getBloodPressureStatus());
        
        // 高血压
        testMetrics.setSystolic(150.0);
        testMetrics.setDiastolic(95.0);
        HealthReport result3 = healthReportAnalysisService.analyzeHealthData(testUserId, testMetrics, testExerciseGoal);
        assertEquals("高血压", result3.getBloodPressureStatus());
    }

    @Test
    void testSleepQualityAnalysis() {
        // 优质睡眠
        testMetrics.setAverageSleepDuration(8.0);
        testMetrics.setDeepSleepPercentage(30.0);
        HealthReport result1 = healthReportAnalysisService.analyzeHealthData(testUserId, testMetrics, testExerciseGoal);
        assertEquals("优", result1.getSleepQuality());
        
        // 良好睡眠
        testMetrics.setAverageSleepDuration(7.0);
        testMetrics.setDeepSleepPercentage(20.0);
        HealthReport result2 = healthReportAnalysisService.analyzeHealthData(testUserId, testMetrics, testExerciseGoal);
        assertEquals("良", result2.getSleepQuality());
        
        // 较差睡眠
        testMetrics.setAverageSleepDuration(5.0);
        testMetrics.setDeepSleepPercentage(10.0);
        HealthReport result3 = healthReportAnalysisService.analyzeHealthData(testUserId, testMetrics, testExerciseGoal);
        assertEquals("差", result3.getSleepQuality());
    }

    @Test
    void testOverallScoreCalculation() {
        // 测试综合评分计算
        HealthReport result = healthReportAnalysisService.analyzeHealthData(testUserId, testMetrics, testExerciseGoal);
        
        // 验证评分在合理范围内
        assertTrue(result.getOverallScore() >= 0);
        assertTrue(result.getOverallScore() <= 100);
        
        // 对于正常的测试数据，评分应该较高
        assertTrue(result.getOverallScore() >= 70);
    }
}
