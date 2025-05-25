package com.health.healthplatform.DTO;

import lombok.Data;

@Data
public class ExerciseGoalDTO {
    private Long id;
    private Integer userId;
    private Double weeklyDurationGoal;
    private Integer weeklyCaloriesGoal;
    private Integer weeklyCountGoal;
}