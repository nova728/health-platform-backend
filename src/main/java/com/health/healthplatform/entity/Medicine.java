package com.health.healthplatform.entity;

import lombok.Data;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

@Data
public class Medicine {
    private Long id;                  // 药品记录ID
    private Long userId;              // 用户ID
    private String name;              // 药品名称
    private String dosage;            // 用药剂量
    private String frequency;         // 服用频率
    private List<String> frequencyTiming; // 服用时间点
    private LocalDate startDate;      // 开始服药日期
    private LocalDate endDate;        // 结束服药日期
    private String notes;             // 备注信息
    private String drugInfo;          // 药品详细信息(JSON格式存储)
    private LocalDateTime createdAt;  // 创建时间
    private LocalDateTime updatedAt;  // 更新时间
}