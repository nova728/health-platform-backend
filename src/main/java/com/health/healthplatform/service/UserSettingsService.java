package com.health.healthplatform.service;

import com.health.healthplatform.entity.UserSettings;

/**
 * 用户设置服务接口
 */
public interface UserSettingsService {
    
    /**
     * 根据用户ID获取用户设置
     */
    UserSettings getByUserId(Integer userId);
    
    /**
     * 保存用户设置
     */
    boolean save(UserSettings userSettings);
    
    /**
     * 更新用户设置
     */
    boolean updateByUserId(UserSettings userSettings);
}
