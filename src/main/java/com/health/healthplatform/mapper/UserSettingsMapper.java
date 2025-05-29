package com.health.healthplatform.mapper;

import com.health.healthplatform.entity.UserSettings;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户设置数据访问层
 */
@Mapper
public interface UserSettingsMapper {
    
    /**
     * 根据用户ID获取用户设置
     */
    @Select("SELECT * FROM user_settings WHERE user_id = #{userId}")
    UserSettings selectByUserId(@Param("userId") Integer userId);
    
    /**
     * 插入用户设置
     */
    int insert(UserSettings userSettings);
    
    /**
     * 根据用户ID更新用户设置
     */
    int updateByUserId(UserSettings userSettings);
}
