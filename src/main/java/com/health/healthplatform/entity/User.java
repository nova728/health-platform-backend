package com.health.healthplatform.entity;

import lombok.Data;
import org.springframework.stereotype.Component;
import com.baomidou.mybatisplus.annotation.TableField;

@Data
@Component
public class User {
    private int id;

    private String username;

    private String password;

    private int gender;

    private String phone;

    private String email;

    private String address;

    private String avatar;

    @TableField(exist = false)
    private String token;
}
