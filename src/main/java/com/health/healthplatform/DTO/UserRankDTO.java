package com.health.healthplatform.DTO;

import lombok.Data;

import java.util.List;

@Data
public class UserRankDTO {
    private Integer userId;           // 用户ID
    private String username;         // 用户名
    private String avatar;           // 头像
    private Integer totalDuration;   // 运动总时长(分钟)
    private Integer rank;            // 排名
    private List<String> achievements; // 已获得的成就
}