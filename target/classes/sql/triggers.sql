-- 健康数据自动同步触发器
-- 当health_data表有数据插入或更新时，自动更新health_reports表

DELIMITER $$

-- 创建插入触发器
CREATE TRIGGER trigger_health_data_insert 
AFTER INSERT ON health_data 
FOR EACH ROW 
BEGIN
    DECLARE v_bmi_status VARCHAR(20);
    DECLARE v_bp_status VARCHAR(20);
    DECLARE v_hr_status VARCHAR(20);
    DECLARE v_overall_score INT;
    DECLARE v_health_suggestions TEXT;
    DECLARE v_abnormal_indicators TEXT;
    
    -- 计算BMI状态
    SET v_bmi_status = CASE 
        WHEN NEW.bmi < 18.5 THEN '偏瘦'
        WHEN NEW.bmi < 24 THEN '正常'
        WHEN NEW.bmi < 28 THEN '超重'
        ELSE '肥胖'
    END;
    
    -- 计算血压状态
    SET v_bp_status = CASE 
        WHEN NEW.blood_pressure_systolic < 90 OR NEW.blood_pressure_diastolic < 60 THEN '低血压'
        WHEN NEW.blood_pressure_systolic <= 120 AND NEW.blood_pressure_diastolic <= 80 THEN '正常'
        WHEN NEW.blood_pressure_systolic <= 140 AND NEW.blood_pressure_diastolic <= 90 THEN '偏高'
        ELSE '高血压'
    END;
    
    -- 计算心率状态
    SET v_hr_status = CASE 
        WHEN NEW.heart_rate < 60 THEN '偏低'
        WHEN NEW.heart_rate <= 100 THEN '正常'
        ELSE '偏高'
    END;
    
    -- 计算综合评分
    SET v_overall_score = 100;
    IF NEW.bmi IS NOT NULL AND v_bmi_status != '正常' THEN
        SET v_overall_score = v_overall_score - 15;
    END IF;
    IF v_bp_status != '正常' THEN
        SET v_overall_score = v_overall_score - 20;
    END IF;
    IF v_hr_status != '正常' THEN
        SET v_overall_score = v_overall_score - 15;
    END IF;
    IF NEW.steps IS NOT NULL AND NEW.steps < 8000 THEN
        SET v_overall_score = v_overall_score - 15;
    END IF;
    IF NEW.sleep_duration IS NOT NULL AND NEW.sleep_duration < 7 THEN
        SET v_overall_score = v_overall_score - 10;
    END IF;
    SET v_overall_score = GREATEST(v_overall_score, 0);
    
    -- 生成健康建议
    SET v_health_suggestions = '[';
    IF NEW.bmi IS NOT NULL THEN
        IF v_bmi_status = '偏瘦' THEN
            SET v_health_suggestions = CONCAT(v_health_suggestions, '"适当增加营养摄入，建议咨询营养师制定增重计划",');
        ELSEIF v_bmi_status IN ('超重', '肥胖') THEN
            SET v_health_suggestions = CONCAT(v_health_suggestions, '"建议控制饮食，增加运动量，必要时咨询专业医生",');
        END IF;
    END IF;
    IF v_bp_status != '正常' THEN
        SET v_health_suggestions = CONCAT(v_health_suggestions, '"建议监测血压变化，保持良好作息，减少盐分摄入",');
    END IF;
    IF v_hr_status != '正常' THEN
        SET v_health_suggestions = CONCAT(v_health_suggestions, '"建议适度运动，保持心情舒畅，如有不适及时就医",');
    END IF;
    IF NEW.steps IS NOT NULL AND NEW.steps < 8000 THEN
        SET v_health_suggestions = CONCAT(v_health_suggestions, '"建议增加日常活动量，每天至少步行8000步",');
    END IF;
    IF NEW.sleep_duration IS NOT NULL AND NEW.sleep_duration < 7 THEN
        SET v_health_suggestions = CONCAT(v_health_suggestions, '"建议保证充足睡眠，每晚至少7-8小时",');
    END IF;
    
    -- 移除最后的逗号并闭合数组
    IF RIGHT(v_health_suggestions, 1) = ',' THEN
        SET v_health_suggestions = CONCAT(LEFT(v_health_suggestions, LENGTH(v_health_suggestions) - 1), ']');
    ELSE
        SET v_health_suggestions = CONCAT(v_health_suggestions, '"您的健康状况良好，请继续保持良好的生活习惯"]');
    END IF;
    
    -- 生成异常指标
    SET v_abnormal_indicators = '[';
    IF NEW.bmi IS NOT NULL AND v_bmi_status != '正常' THEN
        SET v_abnormal_indicators = CONCAT(v_abnormal_indicators, '"BMI: ', v_bmi_status, '",');
    END IF;
    IF v_bp_status != '正常' THEN
        SET v_abnormal_indicators = CONCAT(v_abnormal_indicators, '"血压: ', v_bp_status, '",');
    END IF;
    IF v_hr_status != '正常' THEN
        SET v_abnormal_indicators = CONCAT(v_abnormal_indicators, '"心率: ', v_hr_status, '",');
    END IF;
    IF NEW.sleep_quality IS NOT NULL AND NEW.sleep_quality = '差' THEN
        SET v_abnormal_indicators = CONCAT(v_abnormal_indicators, '"睡眠质量: ', NEW.sleep_quality, '",');
    END IF;
    
    -- 移除最后的逗号并闭合数组
    IF RIGHT(v_abnormal_indicators, 1) = ',' THEN
        SET v_abnormal_indicators = CONCAT(LEFT(v_abnormal_indicators, LENGTH(v_abnormal_indicators) - 1), ']');
    ELSE
        SET v_abnormal_indicators = CONCAT(v_abnormal_indicators, ']');
    END IF;
    
    -- 插入或更新健康报告
    INSERT INTO health_reports (
        user_id, 
        report_time, 
        bmi, 
        bmi_status, 
        weight, 
        height,
        systolic, 
        diastolic, 
        blood_pressure_status,
        heart_rate, 
        heart_rate_status,
        daily_steps, 
        daily_distance,
        steps_goal_achieved,
        average_sleep_duration, 
        sleep_quality,
        overall_score, 
        health_suggestions, 
        abnormal_indicators,
        create_time, 
        update_time
    ) VALUES (
        NEW.user_id,
        CONCAT(NEW.record_date, ' 23:59:59'),
        NEW.bmi,
        v_bmi_status,
        NEW.weight,
        NEW.height,
        NEW.blood_pressure_systolic,
        NEW.blood_pressure_diastolic,
        v_bp_status,
        NEW.heart_rate,
        v_hr_status,
        NEW.steps,
        CASE WHEN NEW.steps IS NOT NULL THEN NEW.steps * 0.0007 ELSE NULL END, -- 假设每步0.7米
        CASE WHEN NEW.steps IS NOT NULL THEN NEW.steps >= 10000 ELSE NULL END,
        NEW.sleep_duration,
        NEW.sleep_quality,
        v_overall_score,
        v_health_suggestions,
        v_abnormal_indicators,
        NOW(),
        NOW()
    ) ON DUPLICATE KEY UPDATE
        bmi = NEW.bmi,
        bmi_status = v_bmi_status,
        weight = NEW.weight,
        height = NEW.height,
        systolic = NEW.blood_pressure_systolic,
        diastolic = NEW.blood_pressure_diastolic,
        blood_pressure_status = v_bp_status,
        heart_rate = NEW.heart_rate,
        heart_rate_status = v_hr_status,
        daily_steps = NEW.steps,
        daily_distance = CASE WHEN NEW.steps IS NOT NULL THEN NEW.steps * 0.0007 ELSE daily_distance END,
        steps_goal_achieved = CASE WHEN NEW.steps IS NOT NULL THEN NEW.steps >= 10000 ELSE steps_goal_achieved END,
        average_sleep_duration = NEW.sleep_duration,
        sleep_quality = NEW.sleep_quality,
        overall_score = v_overall_score,
        health_suggestions = v_health_suggestions,
        abnormal_indicators = v_abnormal_indicators,
        update_time = NOW();
END$$

-- 创建更新触发器
CREATE TRIGGER trigger_health_data_update 
AFTER UPDATE ON health_data 
FOR EACH ROW 
BEGIN
    DECLARE v_bmi_status VARCHAR(20);
    DECLARE v_bp_status VARCHAR(20);
    DECLARE v_hr_status VARCHAR(20);
    DECLARE v_overall_score INT;
    DECLARE v_health_suggestions TEXT;
    DECLARE v_abnormal_indicators TEXT;
    
    -- 计算BMI状态
    SET v_bmi_status = CASE 
        WHEN NEW.bmi < 18.5 THEN '偏瘦'
        WHEN NEW.bmi < 24 THEN '正常'
        WHEN NEW.bmi < 28 THEN '超重'
        ELSE '肥胖'
    END;
    
    -- 计算血压状态
    SET v_bp_status = CASE 
        WHEN NEW.blood_pressure_systolic < 90 OR NEW.blood_pressure_diastolic < 60 THEN '低血压'
        WHEN NEW.blood_pressure_systolic <= 120 AND NEW.blood_pressure_diastolic <= 80 THEN '正常'
        WHEN NEW.blood_pressure_systolic <= 140 AND NEW.blood_pressure_diastolic <= 90 THEN '偏高'
        ELSE '高血压'
    END;
    
    -- 计算心率状态
    SET v_hr_status = CASE 
        WHEN NEW.heart_rate < 60 THEN '偏低'
        WHEN NEW.heart_rate <= 100 THEN '正常'
        ELSE '偏高'
    END;
    
    -- 计算综合评分
    SET v_overall_score = 100;
    IF NEW.bmi IS NOT NULL AND v_bmi_status != '正常' THEN
        SET v_overall_score = v_overall_score - 15;
    END IF;
    IF v_bp_status != '正常' THEN
        SET v_overall_score = v_overall_score - 20;
    END IF;
    IF v_hr_status != '正常' THEN
        SET v_overall_score = v_overall_score - 15;
    END IF;
    IF NEW.steps IS NOT NULL AND NEW.steps < 8000 THEN
        SET v_overall_score = v_overall_score - 15;
    END IF;
    IF NEW.sleep_duration IS NOT NULL AND NEW.sleep_duration < 7 THEN
        SET v_overall_score = v_overall_score - 10;
    END IF;
    SET v_overall_score = GREATEST(v_overall_score, 0);
    
    -- 生成健康建议
    SET v_health_suggestions = '[';
    IF NEW.bmi IS NOT NULL THEN
        IF v_bmi_status = '偏瘦' THEN
            SET v_health_suggestions = CONCAT(v_health_suggestions, '"适当增加营养摄入，建议咨询营养师制定增重计划",');
        ELSEIF v_bmi_status IN ('超重', '肥胖') THEN
            SET v_health_suggestions = CONCAT(v_health_suggestions, '"建议控制饮食，增加运动量，必要时咨询专业医生",');
        END IF;
    END IF;
    IF v_bp_status != '正常' THEN
        SET v_health_suggestions = CONCAT(v_health_suggestions, '"建议监测血压变化，保持良好作息，减少盐分摄入",');
    END IF;
    IF v_hr_status != '正常' THEN
        SET v_health_suggestions = CONCAT(v_health_suggestions, '"建议适度运动，保持心情舒畅，如有不适及时就医",');
    END IF;
    IF NEW.steps IS NOT NULL AND NEW.steps < 8000 THEN
        SET v_health_suggestions = CONCAT(v_health_suggestions, '"建议增加日常活动量，每天至少步行8000步",');
    END IF;
    IF NEW.sleep_duration IS NOT NULL AND NEW.sleep_duration < 7 THEN
        SET v_health_suggestions = CONCAT(v_health_suggestions, '"建议保证充足睡眠，每晚至少7-8小时",');
    END IF;
    
    -- 移除最后的逗号并闭合数组
    IF RIGHT(v_health_suggestions, 1) = ',' THEN
        SET v_health_suggestions = CONCAT(LEFT(v_health_suggestions, LENGTH(v_health_suggestions) - 1), ']');
    ELSE
        SET v_health_suggestions = CONCAT(v_health_suggestions, '"您的健康状况良好，请继续保持良好的生活习惯"]');
    END IF;
    
    -- 生成异常指标
    SET v_abnormal_indicators = '[';
    IF NEW.bmi IS NOT NULL AND v_bmi_status != '正常' THEN
        SET v_abnormal_indicators = CONCAT(v_abnormal_indicators, '"BMI: ', v_bmi_status, '",');
    END IF;
    IF v_bp_status != '正常' THEN
        SET v_abnormal_indicators = CONCAT(v_abnormal_indicators, '"血压: ', v_bp_status, '",');
    END IF;
    IF v_hr_status != '正常' THEN
        SET v_abnormal_indicators = CONCAT(v_abnormal_indicators, '"心率: ', v_hr_status, '",');
    END IF;
    IF NEW.sleep_quality IS NOT NULL AND NEW.sleep_quality = '差' THEN
        SET v_abnormal_indicators = CONCAT(v_abnormal_indicators, '"睡眠质量: ', NEW.sleep_quality, '",');
    END IF;
    
    -- 移除最后的逗号并闭合数组
    IF RIGHT(v_abnormal_indicators, 1) = ',' THEN
        SET v_abnormal_indicators = CONCAT(LEFT(v_abnormal_indicators, LENGTH(v_abnormal_indicators) - 1), ']');
    ELSE
        SET v_abnormal_indicators = CONCAT(v_abnormal_indicators, ']');
    END IF;
    
    -- 更新健康报告
    UPDATE health_reports SET
        bmi = NEW.bmi,
        bmi_status = v_bmi_status,
        weight = NEW.weight,
        height = NEW.height,
        systolic = NEW.blood_pressure_systolic,
        diastolic = NEW.blood_pressure_diastolic,
        blood_pressure_status = v_bp_status,
        heart_rate = NEW.heart_rate,
        heart_rate_status = v_hr_status,
        daily_steps = NEW.steps,
        daily_distance = CASE WHEN NEW.steps IS NOT NULL THEN NEW.steps * 0.0007 ELSE daily_distance END,
        steps_goal_achieved = CASE WHEN NEW.steps IS NOT NULL THEN NEW.steps >= 10000 ELSE steps_goal_achieved END,
        average_sleep_duration = NEW.sleep_duration,
        sleep_quality = NEW.sleep_quality,
        overall_score = v_overall_score,
        health_suggestions = v_health_suggestions,
        abnormal_indicators = v_abnormal_indicators,
        update_time = NOW()
    WHERE user_id = NEW.user_id 
      AND DATE(report_time) = NEW.record_date;
      
    -- 如果没有找到对应的报告记录，则插入新记录
    IF ROW_COUNT() = 0 THEN
        INSERT INTO health_reports (
            user_id, 
            report_time, 
            bmi, 
            bmi_status, 
            weight, 
            height,
            systolic, 
            diastolic, 
            blood_pressure_status,
            heart_rate, 
            heart_rate_status,
            daily_steps, 
            daily_distance,
            steps_goal_achieved,
            average_sleep_duration, 
            sleep_quality,
            overall_score, 
            health_suggestions, 
            abnormal_indicators,
            create_time, 
            update_time
        ) VALUES (
            NEW.user_id,
            CONCAT(NEW.record_date, ' 23:59:59'),
            NEW.bmi,
            v_bmi_status,
            NEW.weight,
            NEW.height,
            NEW.blood_pressure_systolic,
            NEW.blood_pressure_diastolic,
            v_bp_status,
            NEW.heart_rate,
            v_hr_status,
            NEW.steps,
            CASE WHEN NEW.steps IS NOT NULL THEN NEW.steps * 0.0007 ELSE NULL END,
            CASE WHEN NEW.steps IS NOT NULL THEN NEW.steps >= 10000 ELSE NULL END,
            NEW.sleep_duration,
            NEW.sleep_quality,
            v_overall_score,
            v_health_suggestions,
            v_abnormal_indicators,
            NOW(),
            NOW()
        );
    END IF;
END$$

DELIMITER ;

-- 显示创建的触发器
SHOW TRIGGERS LIKE 'health_data';
