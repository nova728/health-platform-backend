package com.health.healthplatform.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Notification {
    private Long id;
    private Integer userId;      // 接收通知的用户ID
    private Integer senderId;    // 发送通知的用户ID
    private String type;         // 通知类型：comment, like 等
    private String message;      // 通知内容
    private Long articleId;      // 相关文章ID
    private Boolean isRead;      // 是否已读
    private LocalDateTime createTime;
    private User sender;         // 发送者信息（非数据库字段）
}