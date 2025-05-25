package com.health.healthplatform.entity.healthdata;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("blood_pressure_history")
public class BloodPressureHistory {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Integer userId;
    private Double systolic;
    private Double diastolic;
    private LocalDateTime measurementTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
