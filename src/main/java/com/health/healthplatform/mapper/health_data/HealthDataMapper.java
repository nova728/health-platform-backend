package com.health.healthplatform.mapper.health_data;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.health.healthplatform.entity.healthdata.HealthData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface HealthDataMapper extends BaseMapper<HealthData> {
    /**
     * 获取指定用户最新的健康数据
     * @param userId 用户ID
     * @return 健康数据列表
     */
    List<HealthData> findLatestDataByUserId(@Param("userId") Integer userId);

    @Select("SELECT * FROM health_data WHERE user_id = #{userId}")
    HealthData findByUserId(@Param("userId") Integer userId);

    @Select("SELECT * FROM health_data WHERE user_id = #{userId} ORDER BY create_time DESC LIMIT 1")
    HealthData findLatestByUserId(@Param("userId") Integer userId);

}