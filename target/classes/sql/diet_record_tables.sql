-- 饮食记录系统相关表创建脚本

-- 创建每日营养汇总表
CREATE TABLE IF NOT EXISTS daily_nutrition_summary (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id INT NOT NULL COMMENT '用户ID',
    record_date DATE NOT NULL COMMENT '记录日期',
    total_calories DOUBLE DEFAULT 0 COMMENT '总热量(千卡)',
    total_carbs DOUBLE DEFAULT 0 COMMENT '总碳水化合物(克)',
    total_protein DOUBLE DEFAULT 0 COMMENT '总蛋白质(克)',
    total_fat DOUBLE DEFAULT 0 COMMENT '总脂肪(克)',
    recommended_calories DOUBLE COMMENT '推荐热量(千卡)',
    recommended_carbs DOUBLE COMMENT '推荐碳水化合物(克)',
    recommended_protein DOUBLE COMMENT '推荐蛋白质(克)',
    recommended_fat DOUBLE COMMENT '推荐脂肪(克)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_date (user_id, record_date) COMMENT '用户每天只能有一条记录'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='每日营养汇总表';

-- 创建餐次记录表
CREATE TABLE IF NOT EXISTS meal_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    nutrition_summary_id BIGINT NOT NULL COMMENT '每日营养汇总ID',
    user_id INT NOT NULL COMMENT '用户ID',
    meal_type VARCHAR(20) NOT NULL COMMENT '餐次类型(BREAKFAST/LUNCH/DINNER/SNACK)',
    food_id VARCHAR(100) COMMENT '食物ID',
    food_name VARCHAR(200) NOT NULL COMMENT '食物名称',
    serving_amount DOUBLE NOT NULL COMMENT '份量',
    serving_unit VARCHAR(50) NOT NULL COMMENT '份量单位',
    calories DOUBLE DEFAULT 0 COMMENT '热量(千卡)',
    carbs DOUBLE DEFAULT 0 COMMENT '碳水化合物(克)',
    protein DOUBLE DEFAULT 0 COMMENT '蛋白质(克)',
    fat DOUBLE DEFAULT 0 COMMENT '脂肪(克)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_nutrition_summary_id (nutrition_summary_id),
    INDEX idx_user_id (user_id),
    INDEX idx_meal_type (meal_type),
    FOREIGN KEY (nutrition_summary_id) REFERENCES daily_nutrition_summary(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='餐次记录表';

-- 创建食物信息缓存表（用于存储从API获取的食物信息）
CREATE TABLE IF NOT EXISTS food_info_cache (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    food_id VARCHAR(100) NOT NULL COMMENT '食物ID',
    food_name VARCHAR(200) NOT NULL COMMENT '食物名称',
    brand_name VARCHAR(200) COMMENT '品牌名称',
    calories_per_100g DOUBLE COMMENT '每100g热量',
    carbs_per_100g DOUBLE COMMENT '每100g碳水化合物',
    protein_per_100g DOUBLE COMMENT '每100g蛋白质',
    fat_per_100g DOUBLE COMMENT '每100g脂肪',
    serving_description VARCHAR(500) COMMENT '份量描述',
    metric_serving_amount DOUBLE COMMENT '标准份量',
    metric_serving_unit VARCHAR(50) COMMENT '标准份量单位',
    measurement_description VARCHAR(500) COMMENT '计量描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_food_id (food_id),
    INDEX idx_food_name (food_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='食物信息缓存表';

-- 插入一些示例数据
INSERT IGNORE INTO food_info_cache (food_id, food_name, calories_per_100g, carbs_per_100g, protein_per_100g, fat_per_100g, serving_description, metric_serving_amount, metric_serving_unit) VALUES
('001', '白米饭', 116, 25.9, 2.6, 0.3, '1碗(150g)', 150, 'g'),
('002', '鸡胸肉', 165, 0, 31, 3.6, '1块(100g)', 100, 'g'),
('003', '鸡蛋', 147, 1.1, 12.8, 10.6, '1个(50g)', 50, 'g'),
('004', '牛奶', 54, 3.4, 3.0, 3.2, '1杯(250ml)', 250, 'ml'),
('005', '苹果', 52, 13.8, 0.2, 0.2, '1个中等大小(150g)', 150, 'g'),
('006', '香蕉', 89, 22.8, 1.1, 0.3, '1根(100g)', 100, 'g'),
('007', '燕麦', 389, 66.3, 16.9, 6.9, '1份(30g)', 30, 'g'),
('008', '三文鱼', 208, 0, 25.4, 10.4, '1片(100g)', 100, 'g');
