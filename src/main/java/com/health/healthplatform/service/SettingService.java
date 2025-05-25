package com.health.healthplatform.service;

import com.health.healthplatform.entity.UserSettings;
import com.health.healthplatform.mapper.SettingMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SettingService {
    @Autowired
    private SettingMapper settingMapper;

    // 更新密码
    @Transactional
    public void updatePassword(Integer userId, String newPassword) {
        settingMapper.updatePassword(userId, newPassword);
    }

    // 更新手机号
    @Transactional
    public void updatePhone(Integer userId, String phone) {
        settingMapper.updatePhone(userId, phone);
    }

    // 更新邮箱
    @Transactional
    public void updateEmail(Integer userId, String email) {
        settingMapper.updateEmail(userId, email);
    }

    // 获取用户设置
    public UserSettings getUserSettings(Integer userId) {
        return settingMapper.getUserSettings(userId);
    }

    // 更新通知设置
    @Transactional
    public void updateNotificationSettings(UserSettings settings) {
        settingMapper.updateNotificationSettings(settings);
    }

    // 更新隐私设置
    @Transactional
    public void updatePrivacySettings(UserSettings settings) {
        settingMapper.updatePrivacySettings(settings);
    }

    // 更新通用设置
    @Transactional
    public void updateGeneralSettings(UserSettings settings) {
        settingMapper.updateGeneralSettings(settings);
    }

    // 创建用户设置
    @Transactional
    public void createUserSettings(UserSettings settings) {
        settingMapper.createUserSettings(settings);
    }
}