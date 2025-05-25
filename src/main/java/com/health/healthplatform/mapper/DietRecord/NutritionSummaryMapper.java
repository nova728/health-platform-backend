package com.health.healthplatform.mapper.DietRecord;

import java.time.LocalDate;
import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.health.healthplatform.entity.DietRecord.NutritionSummary;

@Mapper
public interface NutritionSummaryMapper extends BaseMapper<NutritionSummary> {
    @Select("SELECT * FROM daily_nutrition_summary WHERE user_id = #{userId} AND record_date = #{date}")
    NutritionSummary findByUserIdAndDate(@Param("userId") Integer userId, @Param("date") LocalDate date);
    
    @Select("SELECT * FROM daily_nutrition_summary WHERE user_id = #{userId} ORDER BY record_date DESC LIMIT 1")
    NutritionSummary findLatestByUserId(@Param("userId") Integer userId);

    @Insert("""
        INSERT INTO daily_nutrition_summary
            (user_id, record_date, total_calories, total_carbs, total_protein, total_fat,
             recommended_calories, recommended_carbs, recommended_protein, recommended_fat)
        VALUES
            (#{userId}, #{recordDate}, #{totalCalories}, #{totalCarbs}, #{totalProtein}, #{totalFat},
             #{recommendedCalories}, #{recommendedCarbs}, #{recommendedProtein}, #{recommendedFat})
        ON DUPLICATE KEY UPDATE
            total_calories = VALUES(total_calories),
            total_carbs = VALUES(total_carbs),
            total_protein = VALUES(total_protein),
            total_fat = VALUES(total_fat)
    """)
    void insertOrUpdate(NutritionSummary summary);

    @Select("""
        SELECT * FROM daily_nutrition_summary 
        WHERE user_id = #{userId} 
        AND record_date BETWEEN #{startDate} AND #{endDate}
        ORDER BY record_date
    """)
    List<NutritionSummary> findByUserIdAndDateRange(
        @Param("userId") Integer userId, 
        @Param("startDate") LocalDate startDate, 
        @Param("endDate") LocalDate endDate
    );
} 
