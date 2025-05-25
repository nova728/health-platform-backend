package com.health.healthplatform.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.ConcurrentHashMap;

@Component("notificationWebSocketHandler")  // 添加具体的bean名称
public class NotificationWebSocketHandler extends TextWebSocketHandler {

    // 存储WebSocket会话，key是用户ID
    private static final ConcurrentHashMap<Integer, WebSocketSession> SESSIONS = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 获取用户ID（从URL中获取）
        Integer userId = getUserIdFromSession(session);
        if (userId != null) {
            SESSIONS.put(userId, session);
            System.out.println("WebSocket连接已建立，用户ID: " + userId);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 处理接收到的消息（如果需要）
        System.out.println("收到消息: " + message.getPayload());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Integer userId = getUserIdFromSession(session);
        if (userId != null) {
            SESSIONS.remove(userId);
            System.out.println("WebSocket连接已关闭，用户ID: " + userId);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.out.println("WebSocket传输错误: " + exception.getMessage());
        if (session.isOpen()) {
            session.close();
        }
    }

    // 发送通知给指定用户
    public void sendNotificationToUser(Integer userId, Object notification) {
        WebSocketSession session = SESSIONS.get(userId);
        if (session != null && session.isOpen()) {
            try {
                String message = objectMapper.writeValueAsString(notification);
                session.sendMessage(new TextMessage(message));
                System.out.println("已发送通知给用户ID " + userId + ": " + message);
            } catch (Exception e) {
                System.out.println("发送通知失败: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("用户 " + userId + " 不在线或会话已关闭");
        }
    }

    private Integer getUserIdFromSession(WebSocketSession session) {
        String path = session.getUri().getPath();
        String[] pathParts = path.split("/");
        try {
            return Integer.parseInt(pathParts[pathParts.length - 1]);
        } catch (NumberFormatException e) {
            System.out.println("无法从会话中获取用户ID: " + e.getMessage());
            return null;
        }
    }
}