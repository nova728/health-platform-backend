package com.health.healthplatform.service.health_data;

import com.health.healthplatform.DTO.ExerciseGoalDTO;
import com.health.healthplatform.DTO.ExerciseRecordDTO;
import com.health.healthplatform.DTO.WeeklyStats;
import com.health.healthplatform.entity.healthdata.ExerciseGoal;
import com.health.healthplatform.entity.healthdata.ExerciseRecord;
import com.health.healthplatform.mapper.health_data.ExerciseGoalMapper;
import com.health.healthplatform.mapper.health_data.ExerciseRecordMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ExerciseRecordService {
    private final ExerciseRecordMapper exerciseRecordMapper;

    @Resource
    ExerciseGoalMapper exerciseGoalMapper;

    public ExerciseRecordService(ExerciseRecordMapper exerciseRecordMapper,
                                 ExerciseGoalMapper exerciseGoalMapper) {
        this.exerciseRecordMapper = exerciseRecordMapper;
        this.exerciseGoalMapper = exerciseGoalMapper;
    }

    public List<ExerciseRecordDTO> getUserExerciseRecords(Integer userId, String period) {
        List<ExerciseRecord> records;

        // 如果period为空或为"all"，获取所有记录
        if (period == null || period.equals("all")) {
            log.info("Fetching all exercise records for user: {}", userId);
            records = exerciseRecordMapper.findAllByUserId(userId);
        } else {
            LocalDate endDate = LocalDate.now();
            LocalDate startDate;

            switch (period) {
                case "week":
                    startDate = endDate.with(WeekFields.of(Locale.getDefault()).dayOfWeek(), 1);
                    log.info("Fetching weekly exercise records for user: {} from {} to {}",
                            userId, startDate, endDate);
                    break;
                case "month":
                    startDate = endDate.withDayOfMonth(1);
                    log.info("Fetching monthly exercise records for user: {} from {} to {}",
                            userId, startDate, endDate);
                    break;
                default:
                    log.warn("Invalid period: {}, defaulting to all records", period);
                    records = exerciseRecordMapper.findAllByUserId(userId);
                    return records.stream()
                            .map(this::convertToDTO)
                            .collect(Collectors.toList());
            }

            records = exerciseRecordMapper.findByDateRange(userId, startDate, endDate);
        }

        log.info("Found {} exercise records", records.size());
        return records.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public ExerciseRecordDTO recordExercise(Integer userId, ExerciseRecordDTO dto) {
        validateExerciseData(dto);

        ExerciseRecord record = new ExerciseRecord();
        BeanUtils.copyProperties(dto, record);
        record.setUserId(userId);

        exerciseRecordMapper.insert(record);
        return convertToDTO(record);
    }

    public void deleteExerciseRecord(Integer userId, Long recordId) {
        ExerciseRecord record = exerciseRecordMapper.selectById(recordId);
        if (record != null && record.getUserId().equals(userId)) {
            exerciseRecordMapper.deleteById(recordId);
        } else {
            throw new IllegalArgumentException("Record not found or unauthorized");
        }
    }

    public ExerciseRecordDTO updateExerciseRecord(Integer userId, Long recordId, ExerciseRecordDTO dto) {
        ExerciseRecord existing = exerciseRecordMapper.selectById(recordId);
        if (existing == null || !existing.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Record not found or unauthorized");
        }

        validateExerciseData(dto);
        BeanUtils.copyProperties(dto, existing);
        exerciseRecordMapper.updateById(existing);
        return convertToDTO(existing);
    }

    public WeeklyStats getWeeklyStats(Integer userId) {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(WeekFields.of(Locale.getDefault()).dayOfWeek(), 1);
        
        WeeklyStats stats = new WeeklyStats();

        // 添加空值处理
        Integer totalDurationMinutes = exerciseRecordMapper.getTotalDuration(userId, startOfWeek, today);
        Integer totalCalories = exerciseRecordMapper.getTotalCalories(userId, startOfWeek, today);
        Integer exerciseCount = exerciseRecordMapper.getExerciseCount(userId, startOfWeek, today);

        // 将数据转换为安全值，Minutes转换为Hours
        stats.setTotalDuration(totalDurationMinutes != null ? totalDurationMinutes / 60.0 : 0.0);
        stats.setTotalCalories(totalCalories != null ? totalCalories : 0);
        stats.setExerciseCount(exerciseCount != null ? exerciseCount : 0);

        log.debug("Weekly stats for user {}: duration={} hours, calories={}, count={}",
                userId, stats.getTotalDuration(), stats.getTotalCalories(), stats.getExerciseCount());

        return stats;
    }

    private ExerciseRecordDTO convertToDTO(ExerciseRecord entity) {
        ExerciseRecordDTO dto = new ExerciseRecordDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    private void validateExerciseData(ExerciseRecordDTO dto) {
        if (dto.getDuration() == null || dto.getDuration() < 1) {
            throw new IllegalArgumentException("运动时长必须大于0分钟");
        }
        if (dto.getCalories() != null && dto.getCalories() < 0) {
            throw new IllegalArgumentException("消耗热量不能为负数");
        }
        if (dto.getIntensity() != null && (dto.getIntensity() < 1 || dto.getIntensity() > 3)) {
            throw new IllegalArgumentException("运动强度必须在1-3之间");
        }
    }

    // 获取用户运动目标
    public ExerciseGoalDTO getUserGoals(Integer userId) {
        ExerciseGoal goal = exerciseGoalMapper.findByUserId(userId);
        ExerciseGoalDTO dto = new ExerciseGoalDTO();

        if (goal == null) {
            dto.setWeeklyDurationGoal(7.0);  // 默认每周7小时
            dto.setWeeklyCaloriesGoal(3500); // 默认每周3500千卡
            dto.setWeeklyCountGoal(5);       // 默认每周5次
        } else {
            BeanUtils.copyProperties(goal, dto);
        }

        return dto;
    }

    // 更新用户运动目标
    public ExerciseGoalDTO updateUserGoals(Integer userId, ExerciseGoalDTO dto) {
        validateGoals(dto);

        ExerciseGoal goal = exerciseGoalMapper.findByUserId(userId);
        if (goal == null) {
            goal = new ExerciseGoal();
            goal.setUserId(userId);
        }

        goal.setWeeklyDurationGoal(dto.getWeeklyDurationGoal());
        goal.setWeeklyCaloriesGoal(dto.getWeeklyCaloriesGoal());
        goal.setWeeklyCountGoal(dto.getWeeklyCountGoal());

        if (goal.getId() == null) {
            exerciseGoalMapper.insert(goal);
        } else {
            exerciseGoalMapper.updateById(goal);
        }

        return getUserGoals(userId);
    }

    private void validateGoals(ExerciseGoalDTO dto) {
        if (dto.getWeeklyDurationGoal() != null &&
                (dto.getWeeklyDurationGoal() < 0 || dto.getWeeklyDurationGoal() > 168)) {
            throw new IllegalArgumentException("每周运动时长目标必须在0-168小时之间");
        }
        if (dto.getWeeklyCaloriesGoal() != null &&
                (dto.getWeeklyCaloriesGoal() < 0 || dto.getWeeklyCaloriesGoal() > 50000)) {
            throw new IllegalArgumentException("每周消耗热量目标必须在0-50000千卡之间");
        }
        if (dto.getWeeklyCountGoal() != null &&
                (dto.getWeeklyCountGoal() < 0 || dto.getWeeklyCountGoal() > 50)) {
            throw new IllegalArgumentException("每周运动次数目标必须在0-50次之间");
        }
    }
}