package com.health.healthplatform.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AchievementDTO {
    private String name;           // 成就名称
    private String description;    // 成就描述
    private String icon;          // 成就图标
    private boolean unlocked;     // 是否解锁
}
