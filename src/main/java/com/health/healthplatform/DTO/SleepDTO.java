package com.health.healthplatform.DTO;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SleepDTO {
    private Long id;
    private Integer userId;
    private Double duration;
    private String quality;
    private Double deepSleepPercentage;
    private Double lightSleepPercentage;
    private Double remSleepPercentage;
    private LocalDateTime date;
}