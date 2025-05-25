package com.health.healthplatform.service.DietRecord;

import com.health.healthplatform.DTO.DietRecord.DailyNutritionDTO;
import com.health.healthplatform.DTO.DietRecord.NutritionStatsDTO;
import com.health.healthplatform.entity.DietRecord.NutritionSummary;
import com.health.healthplatform.mapper.DietRecord.NutritionSummaryMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class NutritionStatsService {
    private final NutritionSummaryMapper nutritionSummaryMapper;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM-dd");

    public NutritionStatsService(NutritionSummaryMapper nutritionSummaryMapper) {
        this.nutritionSummaryMapper = nutritionSummaryMapper;
    }

    public NutritionStatsDTO getStats(Integer userId, String range) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = getStartDate(endDate, range);

        log.info("获取用户 {} 从 {} 到 {} 的营养统计数据", userId, startDate, endDate);

        // 获取日期范围内的所有营养记录
        List<NutritionSummary> summaries = nutritionSummaryMapper.findByUserIdAndDateRange(
                userId, startDate, endDate);

        // 将记录转换为按日期索引的Map
        Map<LocalDate, NutritionSummary> summaryMap = summaries.stream()
                .collect(Collectors.toMap(NutritionSummary::getRecordDate, s -> s));

        // 准备返回数据
        NutritionStatsDTO stats = new NutritionStatsDTO();
        List<String> dates = new ArrayList<>();
        List<Double> calories = new ArrayList<>();
        List<Double> carbs = new ArrayList<>();
        List<Double> protein = new ArrayList<>();
        List<Double> fat = new ArrayList<>();
        List<Double> recommendedCalories = new ArrayList<>();
        List<Double> recommendedCarbs = new ArrayList<>();
        List<Double> recommendedProtein = new ArrayList<>();
        List<Double> recommendedFat = new ArrayList<>();

        // 填充数据
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            NutritionSummary summary = summaryMap.get(date);
            dates.add(date.format(DATE_FORMATTER));

            if (summary != null) {
                calories.add(summary.getTotalCalories());
                carbs.add(summary.getTotalCarbs());
                protein.add(summary.getTotalProtein());
                fat.add(summary.getTotalFat());
                recommendedCalories.add(summary.getRecommendedCalories());
                recommendedCarbs.add(summary.getRecommendedCarbs());
                recommendedProtein.add(summary.getRecommendedProtein());
                recommendedFat.add(summary.getRecommendedFat());
            } else {
                // 如果某天没有记录，填充0和默认推荐值
                calories.add(0.0);
                carbs.add(0.0);
                protein.add(0.0);
                fat.add(0.0);
                recommendedCalories.add(2000.0);
                recommendedCarbs.add(250.0);
                recommendedProtein.add(60.0);
                recommendedFat.add(70.0);
            }
        }

        // 设置统计数据
        stats.setDates(dates);
        stats.setCalories(calories);
        stats.setCarbs(carbs);
        stats.setProtein(protein);
        stats.setFat(fat);
        stats.setRecommendedCalories(recommendedCalories);
        stats.setRecommendedCarbs(recommendedCarbs);
        stats.setRecommendedProtein(recommendedProtein);
        stats.setRecommendedFat(recommendedFat);

        return stats;
    }

    private LocalDate getStartDate(LocalDate endDate, String range) {
        return switch (range) {
            case "week" -> endDate.minusWeeks(1);
            case "month" -> endDate.minusMonths(1);
            case "halfYear" -> endDate.minusMonths(6);
            case "year" -> endDate.minusYears(1);
            default -> endDate.minusWeeks(1);
        };
    }

    public List<DailyNutritionDTO> getMonthlyNutrition(Integer userId, Integer year, Integer month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        List<NutritionSummary> summaries = nutritionSummaryMapper.findByUserIdAndDateRange(
                userId, startDate, endDate);

        // 创建日期到记录的映射
        Map<LocalDate, NutritionSummary> summaryMap = summaries.stream()
                .collect(Collectors.toMap(NutritionSummary::getRecordDate, s -> s));

        List<DailyNutritionDTO> monthlyData = new ArrayList<>();

        // 遍历整个月的每一天
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            DailyNutritionDTO dto = new DailyNutritionDTO();
            dto.setDate(date);

            // 如果有记录则使用记录数据，否则使用默认值
            NutritionSummary summary = summaryMap.getOrDefault(date, createDefaultSummary(date));
            dto = convertToDTO(summary);
            monthlyData.add(dto);
        }

        return monthlyData;
    }

    private NutritionSummary createDefaultSummary(LocalDate date) {
        NutritionSummary summary = new NutritionSummary();
        summary.setRecordDate(date);
        summary.setTotalCalories(0.0);
        summary.setTotalCarbs(0.0);
        summary.setTotalProtein(0.0);
        summary.setTotalFat(0.0);
        summary.setRecommendedCalories(2000.0);
        summary.setRecommendedCarbs(250.0);
        summary.setRecommendedProtein(60.0);
        summary.setRecommendedFat(70.0);
        return summary;
    }

    private DailyNutritionDTO convertToDTO(NutritionSummary summary) {
        DailyNutritionDTO dto = new DailyNutritionDTO();
        dto.setDate(summary.getRecordDate());
        dto.setTotalCalories(summary.getTotalCalories());
        dto.setTotalCarbs(summary.getTotalCarbs());
        dto.setTotalProtein(summary.getTotalProtein());
        dto.setTotalFat(summary.getTotalFat());
        dto.setRecommendedCalories(summary.getRecommendedCalories());
        dto.setRecommendedCarbs(summary.getRecommendedCarbs());
        dto.setRecommendedProtein(summary.getRecommendedProtein());
        dto.setRecommendedFat(summary.getRecommendedFat());

        // 计算百分比
        dto.setCaloriesPercentage(calculatePercentage(summary.getTotalCalories(), summary.getRecommendedCalories()));
        dto.setCarbsPercentage(calculatePercentage(summary.getTotalCarbs(), summary.getRecommendedCarbs()));
        dto.setProteinPercentage(calculatePercentage(summary.getTotalProtein(), summary.getRecommendedProtein()));
        dto.setFatPercentage(calculatePercentage(summary.getTotalFat(), summary.getRecommendedFat()));

        return dto;
    }

    private Double calculatePercentage(Double actual, Double recommended) {
        if (recommended == null || recommended == 0 || actual == null) {
            return 0.0;
        }
        return Math.min((actual / recommended) * 100, 100.0);
    }
}