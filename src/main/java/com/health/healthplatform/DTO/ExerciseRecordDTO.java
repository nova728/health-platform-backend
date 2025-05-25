package com.health.healthplatform.DTO;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ExerciseRecordDTO {
    private Long id;
    private Integer userId;
    private String exerciseType;
    private Integer duration;
    private Integer calories;
    private Integer intensity;
    private String notes;
    private LocalDate recordDate;
}