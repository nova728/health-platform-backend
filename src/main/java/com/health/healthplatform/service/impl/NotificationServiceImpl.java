package com.health.healthplatform.service.impl;

import com.health.healthplatform.entity.Notification;
import com.health.healthplatform.mapper.NotificationMapper;
import com.health.healthplatform.service.NotificationService;
import com.health.healthplatform.websocket.NotificationWebSocketHandler;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private NotificationWebSocketHandler webSocketHandler;

    @Override
    public void createNotification(Integer userId, Integer senderId, String type, String message, Long articleId) {
        try {
            Notification notification = new Notification();
            notification.setUserId(userId);
            notification.setSenderId(senderId);
            notification.setType(type);
            notification.setMessage(message);
            notification.setArticleId(articleId);
            notification.setIsRead(false);
            notification.setCreateTime(LocalDateTime.now());

            notificationMapper.insert(notification);

            // 通过WebSocket发送实时通知
            webSocketHandler.sendNotificationToUser(userId, notification);

        } catch (Exception e) {
            // 添加错误日志
            System.err.println("创建通知失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("创建通知失败", e);
        }
    }

    @Override
    public List<Notification> getUserNotifications(Integer userId) {
        return notificationMapper.selectByUserId(userId);
    }

    @Override
    public void markAsRead(Integer userId, Long notificationId) {
        notificationMapper.markAsRead(userId, notificationId);
    }

    @Override
    public void markAllAsRead(Integer userId) {
        notificationMapper.markAllAsRead(userId);
    }

    @Override
    public int getUnreadCount(Integer userId) {
        return notificationMapper.countUnread(userId);
    }

    @Override
    public void sendNotification(Notification notification) {
        if (notification != null && notification.getUserId() != null) {
            webSocketHandler.sendNotificationToUser(notification.getUserId(), notification);
        }
    }
}