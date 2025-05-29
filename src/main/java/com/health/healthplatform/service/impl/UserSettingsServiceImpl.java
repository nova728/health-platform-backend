package com.health.healthplatform.service.impl;

import com.health.healthplatform.entity.UserSettings;
import com.health.healthplatform.mapper.UserSettingsMapper;
import com.health.healthplatform.service.UserSettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 用户设置服务实现类
 */
@Service
public class UserSettingsServiceImpl implements UserSettingsService {
    
    @Autowired
    private UserSettingsMapper userSettingsMapper;
    
    @Override
    public UserSettings getByUserId(Integer userId) {
        return userSettingsMapper.selectByUserId(userId);
    }
    
    @Override
    public boolean save(UserSettings userSettings) {
        try {
            return userSettingsMapper.insert(userSettings) > 0;
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public boolean updateByUserId(UserSettings userSettings) {
        try {
            return userSettingsMapper.updateByUserId(userSettings) > 0;
        } catch (Exception e) {
            return false;
        }
    }
}
