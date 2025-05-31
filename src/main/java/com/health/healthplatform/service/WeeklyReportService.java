package com.health.healthplatform.service;

import com.health.healthplatform.controller.AIAnalysisController;
import com.health.healthplatform.entity.User;
import com.health.healthplatform.entity.UserSettings;
import com.health.healthplatform.mapper.UserMapper;
import com.health.healthplatform.mapper.UserSettingsMapper;
import com.health.healthplatform.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class HealthWeeklyReportService {

    @Autowired
    private SmsService smsService;

    @Autowired
    private UserSettingsMapper userSettingsMapper; // 新增

    @Autowired
    private UserMapper userMapper; // 新增

    private AIAnalysisController aiAnalysisController;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 生成并发送健康周报给指定用户
     * @param userId 用户ID
     */
    public void generateAndSendWeeklyReport(Integer userId) {
        // 1. 检查用户是否开启系统通知
        UserSettings settings = userSettingsMapper.findByUserId(userId);
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
     * 摘要生成（限制长度）
     */
    private String summarize(String content, int maxLength) {
        String compressed = content.replaceAll("\\s+", " ");
        if (compressed.length() <= maxLength) return compressed;
        return compressed.substring(0, maxLength) + "...";
    }
}