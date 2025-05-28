package com.health.healthplatform.mapper.health_data;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.health.healthplatform.entity.healthdata.HealthData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
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

    /**
     * 根据用户ID和日期范围查询健康数据
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 健康数据列表
     */
    @Select("SELECT * FROM health_data WHERE user_id = #{userId} AND record_date BETWEEN #{startDate} AND #{endDate} ORDER BY record_date, create_time")
    List<HealthData> findByUserIdAndDateRange(@Param("userId") Integer userId,
                                              @Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate);

    /**
     * 根据用户ID和日期查询健康数据
     * @param userId 用户ID
     * @param recordDate 记录日期
     * @return 健康数据列表
     */
    @Select("SELECT * FROM health_data WHERE user_id = #{userId} AND record_date = #{recordDate} ORDER BY create_time")
    List<HealthData> findByUserIdAndDate(@Param("userId") Integer userId,
                                         @Param("recordDate") LocalDate recordDate);
}