package com.health.healthplatform.controller.DietRecord;

import com.health.healthplatform.DTO.DietRecord.DietGoalDTO;
import com.health.healthplatform.service.DietRecord.DietGoalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

/**
 * 饮食目标控制器
 */
@RestController
@RequestMapping("/api/diet")
@Slf4j
public class DietGoalController {
    
    private final DietGoalService dietGoalService;
    
    public DietGoalController(DietGoalService dietGoalService) {
        this.dietGoalService = dietGoalService;
    }
    
    /**
     * 保存或更新用户饮食目标
     */
    @PostMapping("/{userId}/goals")
    public ResponseEntity<?> saveDietGoals(
            @PathVariable Integer userId,
            @RequestBody DietGoalDTO dietGoalDTO) {
        try {
            log.info("保存用户{}的饮食目标: {}", userId, dietGoalDTO);
            DietGoalDTO savedGoal = dietGoalService.saveOrUpdateDietGoal(userId, dietGoalDTO);
            return ResponseEntity.ok(savedGoal);
        } catch (Exception e) {
            log.error("保存饮食目标失败，用户ID: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "保存饮食目标失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取用户当前饮食目标
     */
    @GetMapping("/{userId}/goals")
    public ResponseEntity<?> getCurrentDietGoals(@PathVariable Integer userId) {
        try {
            log.info("获取用户{}的饮食目标", userId);
            DietGoalDTO goal = dietGoalService.getCurrentDietGoal(userId);
            return ResponseEntity.ok(goal);
        } catch (Exception e) {
            log.error("获取饮食目标失败，用户ID: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "获取饮食目标失败: " + e.getMessage()));
        }
    }
}
