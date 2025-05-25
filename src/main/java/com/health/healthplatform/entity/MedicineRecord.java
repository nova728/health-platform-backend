package com.health.healthplatform.entity;

import com.health.healthplatform.DTO.MedicineReminderDTO;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class MedicineRecord {
    private Long id;
    private String name;              // 药品名称
    private String dosage;            // 用药剂量
    private String frequency;         // 服用频率
    private LocalDate startDate;      // 开始服药日期
    private LocalDate endDate;        // 结束服药日期
    private String notes;             // 备注信息
    private List<MedicineReminderDTO> reminders;  // 关联的提醒（非数据库字段）
}