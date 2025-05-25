package com.health.healthplatform.DTO;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BmiDTO {
    private Long id;
    private Integer userId;
    private Double bmi;
    private Double height;
    private Double weight;
    private LocalDateTime measurementTime;
}