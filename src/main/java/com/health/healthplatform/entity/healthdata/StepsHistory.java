package com.health.healthplatform.entity.healthdata;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("steps_history")
public class StepsHistory {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Integer userId;
    private Integer steps;
    private Double distance;
    private Double calories;
    private LocalDate recordDate;
    private Integer target;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}