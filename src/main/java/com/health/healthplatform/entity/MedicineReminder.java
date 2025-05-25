package com.health.healthplatform.entity;

import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class MedicineReminder {
    private Long id;                  // 提醒ID
    private Long medicineId;          // 关联的药品记录ID
    private LocalTime reminderTime;   // 提醒时间
    private String repeatType;        // 重复类型：DAILY, WEEKLY, MONTHLY
    private Boolean isActive;         // 是否启用提醒
    private LocalDateTime createdAt;  // 创建时间
    private LocalDateTime updatedAt;  // 更新时间
    private Medicine medicine;        // 关联的药品信息（非数据库字段）
}