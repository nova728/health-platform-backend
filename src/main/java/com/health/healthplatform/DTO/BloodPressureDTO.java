package com.health.healthplatform.DTO;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BloodPressureDTO {
    private Long id;
    private Integer userId;
    private Double systolic;
    private Double diastolic;
    private LocalDateTime measurementTime;
}