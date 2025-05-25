package com.health.healthplatform.entity;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class Comment {
    private Long id;
    private String content;
    private Long articleId;
    private Integer userId;
    private Long parentId;
    private Integer replyToUserId;
    private Integer likeCount;
    private Integer replyCount;
    private LocalDateTime createdAt;
    private User user;
    private User replyToUser;
    private List<Comment> replies;  // 子评论列表
}