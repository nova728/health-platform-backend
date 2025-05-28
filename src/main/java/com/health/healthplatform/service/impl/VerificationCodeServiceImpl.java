package com.health.healthplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.health.healthplatform.entity.VerificationCode;
import com.health.healthplatform.mapper.VerificationCodeMapper;
import com.health.healthplatform.service.VerificationCodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Slf4j

@Service
public class VerificationCodeServiceImpl extends ServiceImpl<VerificationCodeMapper, VerificationCode> implements VerificationCodeService {    @Override
    public VerificationCode createVerificationCode(Integer userId, String target, String type) {
        log.info("创建验证码 - 用户ID: {}, 目标: {}, 类型: {}", userId, target, type);
        
        try {
            // 先检查是否已存在未过期的验证码，如果存在则删除
            LambdaQueryWrapper<VerificationCode> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(VerificationCode::getUserId, userId)
                    .eq(VerificationCode::getTarget, target)
                    .eq(VerificationCode::getType, type)
                    .gt(VerificationCode::getExpireTime, LocalDateTime.now());
            this.remove(queryWrapper);

            // 创建新验证码
            String code = generateRandomCode(6);
            VerificationCode verificationCode = new VerificationCode();
            verificationCode.setUserId(userId);
            verificationCode.setTarget(target);
            verificationCode.setType(type);
            verificationCode.setCode(code);
            verificationCode.setExpireTime(LocalDateTime.now().plusMinutes(10)); // 10分钟有效期
            verificationCode.setCreateTime(LocalDateTime.now());
            verificationCode.setUsed(false);

            boolean saveResult = this.save(verificationCode);
            if (saveResult) {
                log.info("验证码创建成功 - 用户ID: {}, 目标: {}, 验证码: {}", userId, target, code);
            } else {
                log.error("验证码保存失败 - 用户ID: {}, 目标: {}", userId, target);
                throw new RuntimeException("验证码保存失败");
            }
            
            return verificationCode;
        } catch (Exception e) {
            log.error("创建验证码异常 - 用户ID: {}, 目标: {}, 错误: {}", userId, target, e.getMessage(), e);
            throw new RuntimeException("创建验证码失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean verifyCode(Integer userId, String target, String code, String type) {
        LambdaQueryWrapper<VerificationCode> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(VerificationCode::getUserId, userId)
                .eq(VerificationCode::getTarget, target)
                .eq(VerificationCode::getType, type)
                .eq(VerificationCode::getCode, code)
                .eq(VerificationCode::getUsed, false)
                .gt(VerificationCode::getExpireTime, LocalDateTime.now());
        
        VerificationCode verificationCode = this.getOne(queryWrapper);
        
        if (verificationCode != null) {
            // 标记验证码已使用
            verificationCode.setUsed(true);
            this.updateById(verificationCode);
            return true;
        }
        
        return false;
    }

    // 生成指定长度的随机数字验证码
    private String generateRandomCode(int length) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}
