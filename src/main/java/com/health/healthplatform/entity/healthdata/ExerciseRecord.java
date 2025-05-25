package com.health.healthplatform.entity.healthdata;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("exercise_records")
public class ExerciseRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Integer userId;

    private String exerciseType;

    private Integer duration;

    private Integer calories;

    private Integer intensity;

    private String notes;

    private LocalDate recordDate;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}