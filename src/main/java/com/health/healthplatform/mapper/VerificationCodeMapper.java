package com.health.healthplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.health.healthplatform.entity.VerificationCode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface VerificationCodeMapper extends BaseMapper<VerificationCode> {
    
    VerificationCode findLatestValidCode(
            @Param("userId") Integer userId,
            @Param("target") String target,
            @Param("type") String type);
}