package com.health.healthplatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.health.healthplatform.entity.VerificationCode;

public interface VerificationCodeService extends IService<VerificationCode> {
    
    /**
     * 创建验证码
     * @param userId 用户ID
     * @param target 目标手机号或邮箱
     * @param type 类型: PHONE或EMAIL
     * @return 生成的验证码对象
     */
    VerificationCode createVerificationCode(Integer userId, String target, String type);
    
    /**
     * 验证验证码
     * @param userId 用户ID
     * @param target 目标手机号或邮箱
     * @param code 验证码
     * @param type 类型: PHONE或EMAIL
     * @return 验证是否成功
     */
    boolean verifyCode(Integer userId, String target, String code, String type);
}