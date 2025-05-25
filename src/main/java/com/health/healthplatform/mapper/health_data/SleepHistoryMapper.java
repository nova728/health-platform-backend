package com.health.healthplatform.mapper.health_data;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.health.healthplatform.entity.healthdata.SleepHistory;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface SleepHistoryMapper extends BaseMapper<SleepHistory> {
    @Select("SELECT * FROM sleep_history WHERE user_id = #{userId} " +
            "AND sleep_start BETWEEN #{startTime} AND #{endTime} " +
            "AND deleted = 0 ORDER BY sleep_start DESC")
    List<SleepHistory> findByTimeRange(
            @Param("userId") Integer userId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );
}