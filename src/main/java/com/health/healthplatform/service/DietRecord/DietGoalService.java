package com.health.healthplatform.service.DietRecord;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.health.healthplatform.DTO.DietRecord.DietGoalDTO;
import com.health.healthplatform.entity.DietGoal;
import com.health.healthplatform.mapper.DietGoalMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 饮食目标服务类
 */
@Service
@Slf4j
public class DietGoalService {
    
    private final DietGoalMapper dietGoalMapper;
    
    public DietGoalService(DietGoalMapper dietGoalMapper) {
        this.dietGoalMapper = dietGoalMapper;
    }
    
    /**
     * 保存或更新用户饮食目标
     */
    @Transactional
    public DietGoalDTO saveOrUpdateDietGoal(Integer userId, DietGoalDTO dietGoalDTO) {
        try {
            // 首先将之前的活跃目标设为非活跃
            UpdateWrapper<DietGoal> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("user_id", userId)
                        .eq("is_active", true)
                        .set("is_active", false)
                        .set("update_time", LocalDateTime.now());
            dietGoalMapper.update(null, updateWrapper);
            
            // 创建新的目标记录
            DietGoal dietGoal = new DietGoal();
            BeanUtils.copyProperties(dietGoalDTO, dietGoal);
            dietGoal.setUserId(userId);
            dietGoal.setIsActive(true);
            dietGoal.setCreateTime(LocalDateTime.now());
            dietGoal.setUpdateTime(LocalDateTime.now());
            
            // 如果没有指定目标类型，设为自定义
            if (dietGoal.getGoalType() == null || dietGoal.getGoalType().isEmpty()) {
                dietGoal.setGoalType("custom");
            }
            
            dietGoalMapper.insert(dietGoal);
            
            log.info("用户{}保存饮食目标成功", userId);
            return dietGoalDTO;
            
        } catch (Exception e) {
            log.error("保存饮食目标失败，用户ID: {}", userId, e);
            throw new RuntimeException("保存饮食目标失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户当前活跃的饮食目标
     */
    public DietGoalDTO getCurrentDietGoal(Integer userId) {
        try {
            QueryWrapper<DietGoal> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId)
                       .eq("is_active", true)
                       .orderByDesc("create_time")
                       .last("LIMIT 1");
            
            DietGoal dietGoal = dietGoalMapper.selectOne(queryWrapper);
            
            if (dietGoal != null) {
                DietGoalDTO dietGoalDTO = new DietGoalDTO();
                BeanUtils.copyProperties(dietGoal, dietGoalDTO);
                return dietGoalDTO;
            }
            
            // 如果没有找到目标，返回默认值
            return getDefaultDietGoal();
            
        } catch (Exception e) {
            log.error("获取饮食目标失败，用户ID: {}", userId, e);
            // 返回默认值而不是抛出异常
            return getDefaultDietGoal();
        }
    }
    
    /**
     * 获取默认饮食目标
     */
    private DietGoalDTO getDefaultDietGoal() {
        DietGoalDTO defaultGoal = new DietGoalDTO();
        defaultGoal.setCalories(2000.0);
        defaultGoal.setCarbs(250.0);
        defaultGoal.setProtein(120.0);
        defaultGoal.setFat(65.0);
        defaultGoal.setNote("系统默认目标");
        defaultGoal.setGoalType("maintain");
        return defaultGoal;
    }
}
