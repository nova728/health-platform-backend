package com.health.healthplatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户饮食目标实体类
 */
@Data
@TableName("diet_goals")
public class DietGoal {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户ID
     */
    private Integer userId;
    
    /**
     * 每日热量目标 (kcal)
     */
    private Double calories;
    
    /**
     * 每日碳水化合物目标 (g)
     */
    private Double carbs;
    
    /**
     * 每日蛋白质目标 (g)
     */
    private Double protein;
    
    /**
     * 每日脂肪目标 (g)
     */
    private Double fat;
    
    /**
     * 备注说明
     */
    private String note;
    
    /**
     * 目标类型 (lose-weight, maintain, gain-muscle, custom)
     */
    private String goalType;
    
    /**
     * 是否为当前活跃目标
     */
    private Boolean isActive;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
