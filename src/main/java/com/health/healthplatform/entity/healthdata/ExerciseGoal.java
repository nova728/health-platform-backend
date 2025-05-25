package com.health.healthplatform.entity.healthdata;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("exercise_goals")
public class ExerciseGoal {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Integer userId;

    private Double weeklyDurationGoal;

    private Integer weeklyCaloriesGoal;

    private Integer weeklyCountGoal;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}