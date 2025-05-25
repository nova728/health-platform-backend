package com.health.healthplatform.service.DietRecord;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.health.healthplatform.DTO.DietRecord.MealRecordDTO;
import com.health.healthplatform.DTO.DietRecord.NutritionSummaryDTO;
import com.health.healthplatform.entity.DietRecord.NutritionSummary;
import com.health.healthplatform.entity.DietRecord.MealRecord;
import com.health.healthplatform.mapper.DietRecord.MealRecordMapper;
import com.health.healthplatform.mapper.DietRecord.NutritionSummaryMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DietRecordService {
    private final MealRecordMapper mealRecordMapper;
    private final NutritionSummaryMapper nutritionSummaryMapper;
    private final FoodSearchService foodSearchService;

    public DietRecordService(MealRecordMapper mealRecordMapper,
                            FoodSearchService foodSearchService,
                            NutritionSummaryMapper nutritionSummaryMapper) {
        this.mealRecordMapper = mealRecordMapper;
        this.foodSearchService = foodSearchService;
        this.nutritionSummaryMapper = nutritionSummaryMapper;
    } // 构造函数注入

    @Transactional
    public NutritionSummaryDTO addMealRecord(Integer userId, LocalDate date, MealRecordDTO mealDTO) {
        // 获取或创建每日营养总结
        NutritionSummary summary = getNutritionSummary(userId, date);

        // 创建餐食记录
        MealRecord meal = new MealRecord();
        BeanUtils.copyProperties(mealDTO, meal);
        meal.setNutritionSummaryId(summary.getId());
        meal.setUserId(userId);
        mealRecordMapper.insert(meal);

        // 更新每日总计
        updateNutritionTotals(summary.getId());

        return getNutritionSummaryDTO(summary.getId());
    }
    
    @Transactional
    public NutritionSummaryDTO deleteMealRecord(Integer userId, Long mealId) {
        // 获取餐食记录
        MealRecord mealRecord = mealRecordMapper.selectById(mealId);
        if (mealRecord == null) {
            throw new RuntimeException("餐食记录不存在");
        }

        // 获取对应的每日营养总结
        NutritionSummary summary = nutritionSummaryMapper.selectById(mealRecord.getNutritionSummaryId());
        if (summary == null || !summary.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除此记录");
        }

        // 删除餐食记录
        mealRecordMapper.deleteById(mealId);

        // 更新每日总计
        updateNutritionTotals(summary.getId());

        // 返回更新后的每日营养总结
        return getNutritionSummaryDTO(summary.getId());
    }
    
    private NutritionSummary getNutritionSummary(Integer userId, LocalDate date) {
        log.info("开始获取每日营养总结，用户ID：{}，日期：{}", userId, date);
        NutritionSummary summary = nutritionSummaryMapper.findByUserIdAndDate(userId, date);
        
        if (summary == null) {
            log.info("未找到对应的 NutritionSummary，创建新的记录");
            summary = new NutritionSummary();
            summary.setUserId(userId);
            summary.setRecordDate(date);
            // 设置初始值
            summary.setTotalCalories(0.0);
            summary.setTotalCarbs(0.0);
            summary.setTotalProtein(0.0);
            summary.setTotalFat(0.0);
            // 设置推荐值
            summary.setRecommendedCalories(2000.0);
            summary.setRecommendedCarbs(250.0);
            summary.setRecommendedProtein(60.0);
            summary.setRecommendedFat(70.0);
            
            // 使用 insertOrUpdate 而不是 insert
            nutritionSummaryMapper.insertOrUpdate(summary);
            log.info("新的 NutritionSummary 已创建，ID：{}", summary.getId());
        }
        
        return summary;
    }
    
    private void updateNutritionTotals(Long summaryId) {
        List<MealRecord> meals = mealRecordMapper.findByNutritionSummaryId(summaryId);
        double totalCalories = meals.stream().mapToDouble(MealRecord::getCalories).sum();
        double totalCarbs = meals.stream().mapToDouble(MealRecord::getCarbs).sum();
        double totalProtein = meals.stream().mapToDouble(MealRecord::getProtein).sum();
        double totalFat = meals.stream().mapToDouble(MealRecord::getFat).sum();

        NutritionSummary summary = nutritionSummaryMapper.selectById(summaryId);
        summary.setTotalCalories(totalCalories);
        summary.setTotalCarbs(totalCarbs);
        summary.setTotalProtein(totalProtein);
        summary.setTotalFat(totalFat);
        nutritionSummaryMapper.updateById(summary);
    }
    
    public NutritionSummaryDTO getDailyNutritionSummary(Integer userId, LocalDate date) { //供外部调用
        log.info("开始获取每日营养总结，用户ID：{}，日期：{}", userId, date);
        NutritionSummary summary = getNutritionSummary(userId, date);
        if (summary == null) {
            log.warn("未找到 NutritionSummary，可能存在问题");
        } else {
            log.info("成功获取 NutritionSummary，ID：{}", summary.getId());
        }
        return getNutritionSummaryDTO(summary.getId());
    }
    
    private NutritionSummaryDTO getNutritionSummaryDTO(Long summaryId) {
        NutritionSummary summary = nutritionSummaryMapper.selectById(summaryId);
        List<MealRecord> meals = mealRecordMapper.findByNutritionSummaryId(summaryId);

        if (meals == null) {
            meals = new ArrayList<>();
        }

        NutritionSummaryDTO dto = new NutritionSummaryDTO();
        BeanUtils.copyProperties(summary, dto);
        dto.setMeals(meals.stream()
                .map(meal -> {
                    MealRecordDTO mealDTO = new MealRecordDTO();
                    BeanUtils.copyProperties(meal, mealDTO);
                    return mealDTO;
                })
                .collect(Collectors.toList()));

        dto.calculatePercentages();
        log.info("NutritionSummaryDTO 构建完成，包含 {} 条餐食记录", dto.getMeals().size());
        return dto;
    }
}