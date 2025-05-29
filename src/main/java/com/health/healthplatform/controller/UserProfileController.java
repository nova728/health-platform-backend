package com.health.healthplatform.controller;

import com.health.healthplatform.entity.User;
import com.health.healthplatform.entity.UserSettings;
import com.health.healthplatform.result.Result;
import com.health.healthplatform.service.UserService;
import com.health.healthplatform.service.UserSettingsService;
import com.health.healthplatform.service.health_data.ExerciseRecordService;
import com.health.healthplatform.DTO.ExerciseRecordDTO;
import com.health.healthplatform.DTO.WeeklyStats;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

/**
 * 用户资料控制器
 */
@RestController
@RequestMapping("/api/users")
@Slf4j
@CrossOrigin(origins = "*")
public class UserProfileController {    @Autowired
    private UserService userService;

    @Autowired
    private UserSettingsService userSettingsService;

    @Autowired
    private ExerciseRecordService exerciseRecordService;
    /**
     * 获取用户基本信息
     */    
    @GetMapping("/{userId}")
    public Result getUserProfile(@PathVariable Integer userId) {
        try {
            User user = userService.getById(userId);
            if (user == null) {
                return Result.failure(404, "用户不存在");
            }
            
            // 不返回密码等敏感信息
            user.setPassword(null);
            
            return Result.success(user);
        } catch (Exception e) {
            log.error("获取用户资料失败", e);
            return Result.failure(500, "获取用户资料失败");
        }
    }   
     
    /**
     * 获取用户统计信息
     */    
    @GetMapping("/{userId}/stats")
    public Result getUserStats(@PathVariable Integer userId) {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // 基础统计信息
            stats.put("articleCount", userService.getArticleCount(userId));
            stats.put("totalLikes", userService.getTotalLikes(userId));
            
            // 获取用户创建时间
            User user = userService.getById(userId);
            if (user != null) {
                stats.put("createTime", user.getCreateTime());
            }

            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取用户统计信息失败", e);
            return Result.failure(500, "获取用户统计信息失败");
        }
    }    
      /**
     * 获取用户运动数据（需要根据隐私设置）
     */
    @GetMapping("/{userId}/exercise")
    public Result getUserExerciseData(@PathVariable Integer userId) {
        try {
            // 获取用户设置
            UserSettings settings = userSettingsService.getByUserId(userId);
            
            if (settings == null || !"public".equals(settings.getExerciseVisibility())) {
                return Result.failure(403, "该用户的运动数据不对外公开");
            }
            
            // 使用专门的运动记录服务获取运动数据
            Map<String, Object> exerciseData = new HashMap<>();
            
            // 获取用户的周统计数据
            WeeklyStats weeklyStats = exerciseRecordService.getWeeklyStats(userId);
            
            // 匹配前端期望的字段名
            exerciseData.put("totalWorkouts", weeklyStats.getExerciseCount());
            // 将小时转换为分钟
            exerciseData.put("totalMinutes", Math.round(weeklyStats.getTotalDuration() * 60));
            exerciseData.put("totalCalories", weeklyStats.getTotalCalories());
            
            // 获取最近的运动记录（最近7天）
            List<ExerciseRecordDTO> recentRecords = exerciseRecordService.getUserExerciseRecords(userId, "week");
            exerciseData.put("recentRecords", recentRecords);
              // 获取最后一次活动时间
            if (recentRecords != null && !recentRecords.isEmpty()) {
                exerciseData.put("lastActivity", recentRecords.get(0).getRecordDate());
            } else {
                exerciseData.put("lastActivity", null);
            }
            
            // 保留原有的数据结构以保持兼容性
            exerciseData.put("weeklyStats", weeklyStats);
            exerciseData.put("totalDuration", weeklyStats.getTotalDuration());
            exerciseData.put("totalCount", weeklyStats.getExerciseCount());
            
            // 获取用户的运动目标
            var exerciseGoals = exerciseRecordService.getUserGoals(userId);
            exerciseData.put("goals", exerciseGoals);
            
            return Result.success(exerciseData);
        } catch (Exception e) {
            log.error("获取用户运动数据失败", e);
            return Result.failure(500, "获取用户运动数据失败");
        }
    }
}
