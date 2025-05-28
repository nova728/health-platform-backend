package com.health.healthplatform.mapper.HealthReport;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.health.healthplatform.entity.HealthReport.HealthReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Mapper
public interface HealthReportMapper extends BaseMapper<HealthReport> {
    
    @Insert("INSERT INTO health_reports (user_id, report_time, " +
            "bmi, bmi_status, weight, height, " +
            "systolic, diastolic, blood_pressure_status, " +
            "heart_rate, heart_rate_status, " +
            "weekly_exercise_duration, weekly_exercise_count, weekly_calories_burned, exercise_goal_achieved, " +
            "daily_steps, daily_distance, steps_goal_achieved, " +
            "average_sleep_duration, deep_sleep_percentage, light_sleep_percentage, rem_sleep_percentage, sleep_quality, " +
            "overall_score, health_suggestions, abnormal_indicators, " +
            "create_time, update_time) " +
            "VALUES (#{userId}, #{reportTime}, " +
            "#{bmi}, #{bmiStatus}, #{weight}, #{height}, " +
            "#{systolic}, #{diastolic}, #{bloodPressureStatus}, " +
            "#{heartRate}, #{heartRateStatus}, " +
            "#{weeklyExerciseDuration}, #{weeklyExerciseCount}, #{weeklyCaloriesBurned}, #{exerciseGoalAchieved}, " +
            "#{dailySteps}, #{dailyDistance}, #{stepsGoalAchieved}, " +
            "#{averageSleepDuration}, #{deepSleepPercentage}, #{lightSleepPercentage}, #{remSleepPercentage}, #{sleepQuality}, " +
            "#{overallScore}, #{healthSuggestions}, #{abnormalIndicators}, " +
            "NOW(), NOW())")
    int insert(HealthReport report);
    
    @Select("SELECT * FROM health_reports WHERE user_id = #{userId} " +
            "AND deleted = 0 " +
            "ORDER BY report_time DESC LIMIT 1")
    HealthReport findLatestByUserId(@Param("userId") Integer userId);
    
    @Select("SELECT * FROM health_reports WHERE user_id = #{userId} " +
            "AND report_time BETWEEN #{startTime} AND #{endTime} " +
            "AND deleted = 0 " +
            "ORDER BY report_time DESC")
    List<HealthReport> findByUserIdAndTimeRange(
        @Param("userId") Integer userId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    @Select("SELECT * FROM health_reports WHERE id = #{id} AND deleted = 0")
    HealthReport findById(@Param("id") Long id);
    
    @Select("SELECT COUNT(*) FROM health_reports " +
            "WHERE user_id = #{userId} AND deleted = 0")
    int countByUserId(@Param("userId") Integer userId);

    @Select("SELECT * FROM health_reports WHERE user_id = #{userId} " +
            "AND report_time BETWEEN #{startDate} AND #{endDate} " +
            "AND deleted = 0 " +
            "ORDER BY report_time DESC")
    List<HealthReport> findByUserIdAndDateRange(
        @Param("userId") Integer userId,
        @Param("startDate") Date startDate,
        @Param("endDate") Date endDate
    );
    
}