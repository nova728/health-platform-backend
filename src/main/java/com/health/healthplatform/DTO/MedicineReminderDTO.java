package com.health.healthplatform.DTO;

import lombok.Data;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class MedicineReminderDTO {
    private Long id;
    private Long medicineId;          // 关联的药品ID
    private String medicineName;      // 药品名称
    private LocalTime reminderTime;   // 提醒时间
    private String repeatType;        // 重复类型
    private Boolean isActive;         // 是否启用
    private LocalDateTime createTime; // 创建时间
}