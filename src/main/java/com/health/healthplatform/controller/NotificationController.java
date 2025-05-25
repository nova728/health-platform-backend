package com.health.healthplatform.controller;

import com.health.healthplatform.entity.Notification;
import com.health.healthplatform.service.NotificationService;
import com.health.healthplatform.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = {"http://localhost:5173"}, allowCredentials = "true")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/{userId}")
    public Result getNotifications(@PathVariable Integer userId) {
        try {
            List<Notification> notifications = notificationService.getUserNotifications(userId);
            return Result.success(notifications);
        } catch (Exception e) {
            return Result.failure(500, "获取通知失败: " + e.getMessage());
        }
    }

    @PostMapping("/{userId}/{notificationId}/mark-read")
    public Result markAsRead(
            @PathVariable Integer userId,
            @PathVariable Long notificationId) {
        try {
            notificationService.markAsRead(userId, notificationId);
            return Result.success(null);
        } catch (Exception e) {
            return Result.failure(500, "标记已读失败: " + e.getMessage());
        }
    }

    @PostMapping("/{userId}/mark-all-read")
    public Result markAllAsRead(@PathVariable Integer userId) {
        try {
            notificationService.markAllAsRead(userId);
            return Result.success(null);
        } catch (Exception e) {
            return Result.failure(500, "标记全部已读失败: " + e.getMessage());
        }
    }

    @GetMapping("/{userId}/unread-count")
    public Result getUnreadCount(@PathVariable Integer userId) {
        try {
            int count = notificationService.getUnreadCount(userId);
            return Result.success(count);
        } catch (Exception e) {
            return Result.failure(500, "获取未读数量失败: " + e.getMessage());
        }
    }
}