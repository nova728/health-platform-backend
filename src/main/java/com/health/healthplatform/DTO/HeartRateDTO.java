package com.health.healthplatform.DTO;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class HeartRateDTO {
    private Long id;
    private Integer userId;
    private Double heartRate;
    private LocalDateTime measurementTime;
}
