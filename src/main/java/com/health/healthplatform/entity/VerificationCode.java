package com.health.healthplatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("verification_code")
public class VerificationCode {
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    private Integer userId;
    
    private String target;  // 目标手机号或邮箱
    
    private String code;    // 验证码
    
    private String type;    // 类型: PHONE 或 EMAIL
    
    private LocalDateTime expireTime;  // 过期时间
    
    private LocalDateTime createTime;  // 创建时间
    
    private Boolean used;   // 是否已使用
}