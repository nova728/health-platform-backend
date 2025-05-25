package com.health.healthplatform.service;

import com.health.healthplatform.entity.VerificationCode;
import com.health.healthplatform.mapper.VerificationCodeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class VerificationCodeService {

    @Autowired
    private VerificationCodeMapper verificationCodeMapper;

    private static final int CODE_LENGTH = 6;
    private static final int CODE_EXPIRE_MINUTES = 5;
    private static final int SEND_INTERVAL_MINUTES = 1;

    // 生成验证码
    private String generateCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }

    // 创建并存储验证码
    public VerificationCode createVerificationCode(Integer userId, String phone, String type) {
        // 检查是否存在未过期的验证码
        VerificationCode existingCode = verificationCodeMapper.findLatestValidCode(userId, phone, type);
        if (existingCode != null) {
            LocalDateTime lastSendTime = existingCode.getCreateTime();
            if (lastSendTime.plusMinutes(SEND_INTERVAL_MINUTES).isAfter(LocalDateTime.now())) {
                throw new RuntimeException("请求过于频繁，请稍后再试");
            }
        }

        // 创建新的验证码
        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setUserId(userId);
        verificationCode.setPhone(phone);
        verificationCode.setCode(generateCode());
        verificationCode.setType(type);
        verificationCode.setCreateTime(LocalDateTime.now());
        verificationCode.setExpireTime(LocalDateTime.now().plusMinutes(CODE_EXPIRE_MINUTES));
        verificationCode.setUsed(false);

        // 保存到数据库
        verificationCodeMapper.insert(verificationCode);

        return verificationCode;
    }

    // 验证验证码
    public boolean verifyCode(Integer userId, String phone, String code, String type) {
        VerificationCode verificationCode = verificationCodeMapper.findLatestValidCode(userId, phone, type);

        if (verificationCode == null) {
            return false;
        }

        if (verificationCode.getExpireTime().isBefore(LocalDateTime.now())) {
            return false;
        }

        if (verificationCode.getUsed()) {
            return false;
        }

        if (verificationCode.getCode().equals(code)) {
            // 标记验证码已使用
            verificationCodeMapper.markAsUsed(verificationCode.getId());
            return true;
        }

        return false;
    }
}