package com.health.healthplatform.DTO;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class StepsDTO {
    private Long id;
    private Integer userId;
    private Integer steps;
    private Double distance;
    private Double calories;
    private LocalDate recordDate;
    private Integer target;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}