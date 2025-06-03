package com.health.healthplatform.service;

import com.health.healthplatform.controller.AIAnalysisController;
import com.health.healthplatform.entity.User;
import com.health.healthplatform.entity.UserSettings;
import com.health.healthplatform.mapper.UserMapper;
import com.health.healthplatform.mapper.UserSettingsMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class WeeklyReportService {    @Autowired
    private SmsService smsService;

    @Autowired
    private UserSettingsMapper userSettingsMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AIAnalysisController aiAnalysisController;

    @SuppressWarnings("unused")
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 生成并发送健康周报给指定用户
     * @param userId 用户ID
     */
    public void generateAndSendWeeklyReport(Integer userId) {
        // 1. 检查用户是否开启系统通知
        UserSettings settings = userSettingsMapper.selectByUserId(userId);
        if (settings == null || !Boolean.TRUE.equals(settings.getSystemNotification())) {
            log.info("用户 {} 关闭了系统通知，跳过周报发送", userId);
            return;
        }

        String phoneNumber = userMapper.findPhoneNumberById(userId);
        User user = userMapper.selectById(userId);
        if (user == null || user.getPhone().isEmpty()) {
            log.warn("用户 {} 无有效手机号，跳过周报发送", userId);
            return;
        }

        // 3. 生成健康周报内容
        Object reportContent = aiAnalysisController.getPersonalizedAdvice(userId).getData();

        // 4. 发送短信
        if (smsService.isValidPhoneNumber(phoneNumber)) {
            smsService.sendWeeklyReport(phoneNumber, reportContent);
            log.info("健康周报已发送给用户: {}", userId);
        }
    }

    /**
     * 获取用户的周报设置
     * @param userId 用户ID
     * @return 周报设置信息
     */
    public Map<String, Object> getWeeklyReportSettings(Integer userId) {
        Map<String, Object> settings = new HashMap<>();
        
        // 获取用户的系统通知设置
        UserSettings userSettings = userSettingsMapper.selectByUserId(userId);
        if (userSettings != null) {
            settings.put("enabled", Boolean.TRUE.equals(userSettings.getSystemNotification()));
        } else {
            settings.put("enabled", false);
        }
        
        // 设置默认的周报发送时间（目前是固定的）
        settings.put("day", "1"); // 周一
        settings.put("time", "09:00"); // 上午9点
        
        return settings;
    }    /**
     * 更新用户的周报设置
     * @param userId 用户ID
     * @param settings 周报设置
     */
    public void updateWeeklyReportSettings(Integer userId, Map<String, Object> settings) {
        // 目前周报设置主要通过系统通知开关控制
        // 这里可以记录日志，实际的开关在系统通知设置中
        boolean enabled = Boolean.parseBoolean(settings.get("enabled").toString());
        log.info("用户 {} 更新周报设置: enabled={}", userId, enabled);
        
        // 如果需要，可以在这里更新用户的系统通知设置
        UserSettings existingSettings = userSettingsMapper.selectByUserId(userId);
        if (existingSettings != null) {
            existingSettings.setSystemNotification(enabled);
            userSettingsMapper.updateByUserId(existingSettings);
        }
    }

    /**
     * 摘要生成（限制长度）
     */
    @SuppressWarnings("unused")
    private String summarize(String content, int maxLength) {
        String compressed = content.replaceAll("\\s+", " ");
        if (compressed.length() <= maxLength) return compressed;
        return compressed.substring(0, maxLength) + "...";
    }
}