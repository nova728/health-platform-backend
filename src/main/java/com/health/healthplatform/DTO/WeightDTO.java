package com.health.healthplatform.DTO;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WeightDTO {
    private Long id;
    private Integer userId;
    private Double weight;
    private Double height;
    private Double bmi;
    private LocalDateTime measurementTime;
}