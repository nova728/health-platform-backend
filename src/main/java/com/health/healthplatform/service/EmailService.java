package com.health.healthplatform.service;

public interface EmailService {
    
    /**
     * 发送验证码邮件
     * @param email 目标邮箱
     * @param code 验证码
     */
    void sendVerificationCode(String email, String code);
}