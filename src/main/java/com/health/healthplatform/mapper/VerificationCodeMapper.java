package com.health.healthplatform.mapper;

import com.health.healthplatform.entity.VerificationCode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface VerificationCodeMapper {
    void insert(VerificationCode code);

    VerificationCode findLatestValidCode(
            @Param("userId") Integer userId,
            @Param("target") String target,
            @Param("type") String type);

    void markAsUsed(@Param("id") Integer id);
}