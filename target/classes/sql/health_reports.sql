-- 健康报告表创建脚本
CREATE TABLE IF NOT EXISTS health_reports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    report_time DATETIME NOT NULL,
    bmi DOUBLE NULL,
    bmi_status VARCHAR(20) NULL,
    weight DOUBLE NULL,
    height DOUBLE NULL,
    systolic DOUBLE NULL,
    diastolic DOUBLE NULL,
    blood_pressure_status VARCHAR(20) NULL,
    heart_rate DOUBLE NULL,
    heart_rate_status VARCHAR(20) NULL,
    weekly_exercise_duration DOUBLE NULL,
    weekly_exercise_count INT NULL,
    weekly_calories_burned DOUBLE NULL,
    exercise_goal_achieved TINYINT(1) NULL,
    daily_steps INT NULL,
    daily_distance DOUBLE NULL,
    steps_goal_achieved TINYINT(1) NULL,
    average_sleep_duration DOUBLE NULL,
    deep_sleep_percentage DOUBLE NULL,
    light_sleep_percentage DOUBLE NULL,
    rem_sleep_percentage DOUBLE NULL,
    sleep_quality VARCHAR(20) NULL,
    overall_score INT NULL,
    health_suggestions TEXT NULL,
    abnormal_indicators TEXT NULL,
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL,
    deleted TINYINT DEFAULT 0 NULL
);

CREATE INDEX idx_user_report_time ON health_reports (user_id, report_time);

-- 健康数据表创建脚本
CREATE TABLE IF NOT EXISTS health_data (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    heart_rate INT NULL COMMENT '心率',
    sleep_duration DECIMAL(4, 2) NULL COMMENT '睡眠时长(小时)',
    sleep_quality VARCHAR(20) NULL COMMENT '睡眠质量',
    steps INT NULL COMMENT '步数',
    blood_pressure_systolic INT NULL COMMENT '收缩压',
    blood_pressure_diastolic INT NULL COMMENT '舒张压',
    weight DECIMAL(4, 1) NULL COMMENT '体重(kg)',
    bmi DECIMAL(3, 1) NULL COMMENT 'BMI指数',
    record_date DATE NOT NULL COMMENT '记录日期',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NULL,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    height DOUBLE NULL COMMENT '身高',
    CONSTRAINT unique_user_date UNIQUE (user_id, record_date)
);
