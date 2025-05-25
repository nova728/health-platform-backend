package com.health.healthplatform.entity;

import jdk.jfr.Category;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class Article {
    private Long id;
    private String title;
    private String content;
    private String htmlContent;
    private String coverImage;
    private Integer categoryId;
    private Integer userId;
    private Integer status;
    private String visibility;
    private Boolean allowComment;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Integer favoriteCount;
    private Integer pointsAwarded;
    private Integer hotScore;      // 热度分数
    private List<String> sharedPlatforms; // 分享平台记录
    private Boolean isHot;         // 是否热门

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "GMT+8")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "GMT+8")
    private LocalDateTime updatedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "GMT+8")
    private LocalDateTime publishTime;

    // 非数据库字段
    private List<String> tags;
    private User author;
    private Boolean isLiked;
    private Boolean isFavorited;
    private Integer category;
}