package com.health.healthplatform.entity.healthdata;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("bmi_history")
public class BmiHistory {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Integer userId;
    private Double bmi;
    private Double height;
    private Double weight;
    private LocalDateTime measurementTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}