package com.health.healthplatform.entity.healthdata;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sleep_history")
public class SleepHistory {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Integer userId;
    private Double sleepDuration;
    private String sleepQuality;
    private Double deepSleep;
    private Double lightSleep;
    private Double remSleep;
    private LocalDateTime sleepStart;
    private LocalDateTime sleepEnd;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}