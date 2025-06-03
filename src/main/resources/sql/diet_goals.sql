-- 饮食目标表
CREATE TABLE IF NOT EXISTS diet_goals (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id INT NOT NULL COMMENT '用户ID',
    calories DOUBLE NULL COMMENT '每日热量目标(kcal)',
    carbs DOUBLE NULL COMMENT '每日碳水化合物目标(g)',
    protein DOUBLE NULL COMMENT '每日蛋白质目标(g)',
    fat DOUBLE NULL COMMENT '每日脂肪目标(g)',
    goal_type VARCHAR(20) DEFAULT 'custom' COMMENT '目标类型：lose_weight, gain_weight, maintain, custom',
    note TEXT NULL COMMENT '备注',
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否活跃',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_user_id (user_id),
    INDEX idx_user_active (user_id, is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户饮食目标表';