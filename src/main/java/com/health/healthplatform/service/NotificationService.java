package com.health.healthplatform.service;

import com.health.healthplatform.entity.Notification;
import java.util.List;

public interface NotificationService {
    void createNotification(Integer userId, Integer senderId, String type, String message, Long articleId);
    List<Notification> getUserNotifications(Integer userId);
    void markAsRead(Integer userId, Long notificationId);
    void markAllAsRead(Integer userId);
    int getUnreadCount(Integer userId);
    void sendNotification(Notification notification);
}