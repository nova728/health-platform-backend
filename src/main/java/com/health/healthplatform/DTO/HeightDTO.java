package com.health.healthplatform.DTO;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class HeightDTO {
    private Long id;
    private Integer userId;
    private Double height;
    private LocalDateTime measurementTime;
}