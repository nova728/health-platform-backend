package com.health.healthplatform.mapper;

import com.health.healthplatform.entity.UserSettings;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SettingMapper {
    // 更新用户密码
    void updatePassword(@Param("userId") Integer userId, @Param("newPassword") String newPassword);

    // 更新用户手机号
    void updatePhone(@Param("userId") Integer userId, @Param("phone") String phone);

    // 更新用户邮箱
    void updateEmail(@Param("userId") Integer userId, @Param("email") String email);

    // 获取用户设置
    UserSettings getUserSettings(@Param("userId") Integer userId);

    // 更新通知设置
    void updateNotificationSettings(UserSettings settings);

    // 更新隐私设置
    void updatePrivacySettings(UserSettings settings);

    // 更新通用设置
    void updateGeneralSettings(UserSettings settings);

    // 创建用户设置
    void createUserSettings(UserSettings settings);
}