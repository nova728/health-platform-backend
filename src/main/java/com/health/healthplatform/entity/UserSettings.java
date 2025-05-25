package com.health.healthplatform.entity;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class UserSettings {
    private Integer id;
    private Integer userId;

    // 通知设置
    private Boolean systemNotification;
    private Boolean exerciseNotification;
    private Boolean dietNotification;

    // 隐私设置
    private String profileVisibility;  // public, followers, private
    private String exerciseVisibility; // public, followers, private

    // 通用设置
    private String language;  // zh-CN, en
    private String theme;    // light, dark

    // 创建时间和更新时间
    private String createTime;
    private String updateTime;
}
