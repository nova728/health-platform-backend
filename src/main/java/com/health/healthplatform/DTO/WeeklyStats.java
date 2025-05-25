package com.health.healthplatform.DTO;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyStats {
    private Double totalDuration; // 小时
    private Integer totalCalories;
    private Integer exerciseCount;
}