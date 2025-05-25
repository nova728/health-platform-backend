package com.health.healthplatform.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class VerificationCode {
    private Integer id;
    private Integer userId;
    private String phone;
    private String email;
    private String code;
    private LocalDateTime createTime;
    private LocalDateTime expireTime;
    private Boolean used;
    private String type;  // PHONE or EMAIL
}