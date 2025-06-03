package com.health.healthplatform.controller;

import com.health.healthplatform.mapper.UserMapper;
import com.health.healthplatform.service.WeeklyReportService;
import com.health.healthplatform.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CountDownLatch;

@Component
@RestController
@RequestMapping("/api/weekly-report")
@CrossOrigin
@Slf4j
public class WeeklyReportController {

    @Autowired
    private WeeklyReportService weeklyReportService;

    @Autowired
    private UserMapper userMapper;

    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    /**
     * 手动测试周报发送（单个用户）
     */
    @PostMapping("/test/{userId}")
    public Result testSendWeeklyReport(@PathVariable Integer userId) {
        log.info("手动测试发送周报给用户: {}", userId);
        
        try {
            weeklyReportService.generateAndSendWeeklyReport(userId);
            return Result.success("测试周报发送完成，请查看日志和短信");
        } catch (Exception e) {
            log.error("测试周报发送失败", e);
            return Result.failure(500, "测试失败: " + e.getMessage());
        }
    }

    /**
     * 手动测试所有用户周报发送
     */
    @PostMapping("/test/all")
    public Result testSendAllWeeklyReports() {
        log.info("手动测试发送所有用户周报");
        
        try {
            // 直接调用定时任务方法
            sendWeeklyReports();
            return Result.success("测试所有用户周报发送任务已启动，请查看日志");
        } catch (Exception e) {
            log.error("测试所有用户周报发送失败", e);
            return Result.failure(500, "测试失败: " + e.getMessage());
        }
    }

    /**
     * 获取周报发送状态和统计信息
     */
    @GetMapping("/status")
    public Result getWeeklyReportStatus() {
        Map<String, Object> status = new HashMap<>();
        
        try {
            // 获取用户总数
            List<Integer> userIds = userMapper.getAllUserIds();
            status.put("totalUsers", userIds.size());
            
            // 这里可以添加更多状态检查，比如开启通知的用户数
            status.put("scheduledTime", "每周一上午9点");
            status.put("cronExpression", "0 0 9 ? * MON");
            status.put("lastRunTime", "手动触发或定时执行");
            
            return Result.success(status);
        } catch (Exception e) {
            log.error("获取周报状态失败", e);
            return Result.failure(500, "获取状态失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户的周报设置
     */
    @GetMapping("/settings/{userId}")
    public Result getWeeklyReportSettings(@PathVariable Integer userId) {
        try {
            Map<String, Object> settings = weeklyReportService.getWeeklyReportSettings(userId);
            return Result.success(settings);
        } catch (Exception e) {
            log.error("获取用户{}的周报设置失败", userId, e);
            return Result.failure(500, "获取周报设置失败: " + e.getMessage());
        }
    }

    /**
     * 更新用户的周报设置
     */
    @PutMapping("/settings/{userId}")
    public Result updateWeeklyReportSettings(@PathVariable Integer userId, @RequestBody Map<String, Object> settings) {
        try {
            weeklyReportService.updateWeeklyReportSettings(userId, settings);
            return Result.success("周报设置更新成功");
        } catch (Exception e) {
            log.error("更新用户{}的周报设置失败", userId, e);
            return Result.failure(500, "周报设置更新失败: " + e.getMessage());
        }
    }

    /**
     * 每周一上午9点自动发送周报（cron表达式：秒 分 时 日 月 周）
     */
    @Scheduled(cron = "0 0 9 ? * MON")
    public void sendWeeklyReports() {
        log.info("开始执行健康周报定时发送任务...");

        try {
            // 获取所有用户ID
            List<Integer> userIds = userMapper.getAllUserIds();
            log.info("需要检查的用户数量: {}", userIds.size());

            CountDownLatch latch = new CountDownLatch(userIds.size());

            userIds.forEach(userId -> executor.submit(() -> {
                try {
                    weeklyReportService.generateAndSendWeeklyReport(userId);
                } catch (Exception e) {
                    log.error("用户{}的周报发送失败", userId, e);
                } finally {
                    latch.countDown();
                }
            }));

            // 等待所有任务完成，最多2小时
            boolean finished = latch.await(2, TimeUnit.HOURS);
            if (finished) {
                log.info("健康周报定时发送任务完成");
            } else {
                log.warn("健康周报发送任务超时，部分任务可能未完成");
            }
        } catch (InterruptedException e) {
            log.error("周报发送任务被中断", e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("执行周报发送任务时发生异常", e);
        }
    }
}