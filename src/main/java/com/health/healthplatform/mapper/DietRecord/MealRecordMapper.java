package com.health.healthplatform.mapper.DietRecord;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.health.healthplatform.entity.DietRecord.MealRecord;
import com.health.healthplatform.enums.MealType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MealRecordMapper extends BaseMapper<MealRecord> { // 继承 BaseMapper 接口，可以直接使用selectById等方法
    
    @Select("SELECT * FROM meal_records WHERE nutrition_summary_id = #{nutritionSummaryId}")
    List<MealRecord> findByNutritionSummaryId(@Param("nutritionSummaryId") Long nutritionSummaryId);
    
    @Select("SELECT * FROM meal_records WHERE nutrition_summary_id = #{nutritionSummaryId} AND meal_type = #{mealType}")
    List<MealRecord> findByNutritionSummaryIdAndType(
        @Param("nutritionSummaryId") Long nutritionSummaryId, 
        @Param("mealType") MealType mealType
    );
}