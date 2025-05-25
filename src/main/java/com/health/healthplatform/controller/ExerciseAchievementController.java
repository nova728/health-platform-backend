package com.health.healthplatform.controller;

import com.health.healthplatform.DTO.AchievementDTO;
import com.health.healthplatform.DTO.UserRankDTO;
import com.health.healthplatform.service.ExerciseAchievementService;
import com.health.healthplatform.result.Result;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@RestController
@RequestMapping("/api/exercise/achievements")
@Slf4j
public class ExerciseAchievementController {
    private final ExerciseAchievementService achievementService;

    public ExerciseAchievementController(ExerciseAchievementService achievementService) {
        this.achievementService = achievementService;
    }

    // 获取运动排行榜
    @GetMapping("/rankings")
    public Result getRankings(@RequestParam(defaultValue = "weekly") String timeRange) {
        try {
            List<UserRankDTO> rankings = achievementService.getUserRankings(timeRange);
            return Result.success(rankings);
        } catch (Exception e) {
            log.error("获取运动排名失败", e);
            return Result.failure(500, "获取运动排名失败：" + e.getMessage());
        }
    }

    // 获取用户成就
    @GetMapping("/{userId}")
    public Result getUserAchievements(@PathVariable Integer userId) {
        try {
            List<AchievementDTO> achievements = achievementService.getUserAchievements(userId);
            return Result.success(achievements);
        } catch (Exception e) {
            log.error("获取用户成就失败", e);
            return Result.failure(500, "获取用户成就失败：" + e.getMessage());
        }
    }
}