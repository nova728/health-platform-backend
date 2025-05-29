-- 测试数据和触发器验证脚本

-- 1. 插入测试数据到health_data表
INSERT INTO health_data (user_id, heart_rate, sleep_duration, sleep_quality, steps, blood_pressure_systolic, blood_pressure_diastolic, weight, bmi, height, record_date) VALUES
(1, 72, 7.5, '良好', 8500, 120, 80, 65.5, 21.8, 175.0, '2025-05-28'),
(1, 75, 7.0, '一般', 9200, 125, 82, 65.3, 21.7, 175.0, '2025-05-27'),
(1, 70, 8.0, '良好', 10500, 118, 78, 65.7, 21.9, 175.0, '2025-05-26'),
(1, 68, 6.5, '差', 7200, 130, 85, 65.4, 21.8, 175.0, '2025-05-25'),
(1, 74, 7.8, '良好', 11000, 122, 79, 65.6, 21.8, 175.0, '2025-05-24');

-- 2. 插入更多用户的测试数据
INSERT INTO health_data (user_id, heart_rate, sleep_duration, sleep_quality, steps, blood_pressure_systolic, blood_pressure_diastolic, weight, bmi, height, record_date) VALUES
(2, 78, 6.8, '一般', 7800, 135, 88, 72.0, 24.5, 175.0, '2025-05-28'),
(2, 76, 7.2, '良好', 8900, 128, 85, 71.8, 24.4, 175.0, '2025-05-27'),
(3, 65, 8.5, '优秀', 12000, 115, 75, 58.2, 20.1, 170.0, '2025-05-28'),
(3, 63, 8.2, '良好', 11500, 112, 72, 58.0, 20.0, 170.0, '2025-05-27');

-- 3. 验证触发器是否正常工作
-- 查看health_reports表中是否有对应的记录生成
SELECT 'health_reports表记录数量:' as info, COUNT(*) as count FROM health_reports;

-- 查看具体的报告数据
SELECT user_id, DATE(report_time) as report_date, bmi_status, blood_pressure_status, heart_rate_status, overall_score 
FROM health_reports 
ORDER BY user_id, report_time DESC;

-- 4. 测试更新触发器
UPDATE health_data 
SET heart_rate = 85, steps = 12000 
WHERE user_id = 1 AND record_date = '2025-05-28';

-- 验证更新后的结果
SELECT user_id, DATE(report_time) as report_date, heart_rate, heart_rate_status, daily_steps, overall_score 
FROM health_reports 
WHERE user_id = 1 AND DATE(report_time) = '2025-05-28';

-- 5. 查看健康建议和异常指标
SELECT user_id, DATE(report_time) as report_date, health_suggestions, abnormal_indicators 
FROM health_reports 
WHERE user_id = 1 
ORDER BY report_time DESC 
LIMIT 3;
