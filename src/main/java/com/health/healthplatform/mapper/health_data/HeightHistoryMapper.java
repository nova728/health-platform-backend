package com.health.healthplatform.mapper.health_data;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.health.healthplatform.entity.healthdata.HeightHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface HeightHistoryMapper extends BaseMapper<HeightHistory> {
    @Select("SELECT * FROM height_history WHERE user_id = #{userId} " +
            "AND measurement_time BETWEEN #{startTime} AND #{endTime} " +
            "AND deleted = 0 ORDER BY measurement_time ASC")
    List<HeightHistory> findByTimeRange(
            @Param("userId") Integer userId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );
}