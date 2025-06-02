package com.health.healthplatform.controller;

import com.health.healthplatform.mapper.UserMapper;
import com.health.healthplatform.service.WeeklyReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CountDownLatch;

@Component
@Slf4j
public class WeeklyReportController {

    @Autowired
    private WeeklyReportService weeklyReportService;

    @Autowired
    private UserMapper userMapper;

    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    // 每周一上午9点发送（cron表达式：秒 分 时 日 月 周）
    @Scheduled(cron = "0 0 9 ? * MON")
    public void sendWeeklyReports() {
        log.info("开始执行健康周报发送任务...");

        // 获取所有用户ID
        List<Integer> userIds = userMapper.getAllUserIds();
        log.info("需要检查的用户数量: {}", userIds.size());

        CountDownLatch latch = new CountDownLatch(userIds.size());

        userIds.forEach(userId -> executor.submit(() -> {
            try {
                weeklyReportService.generateAndSendWeeklyReport(userId);
            } catch (Exception e) {
                log.error("用户周报发送失败: {}", userId, e);
            } finally {
                latch.countDown();
            }
        }));

        try {
            // 等待所有任务完成，最多2小时
            latch.await(2, TimeUnit.HOURS);
            log.info("健康周报发送任务完成");
        } catch (InterruptedException e) {
            log.error("周报发送任务被中断", e);
            Thread.currentThread().interrupt();
        }
    }
}