package com.health.healthplatform.mapper.health_data;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.health.healthplatform.entity.healthdata.StepsHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface StepsHistoryMapper extends BaseMapper<StepsHistory> {

    @Select("SELECT * FROM steps_history WHERE user_id = #{userId} " +
            "AND record_date BETWEEN #{startDate} AND #{endDate} " +
            "AND deleted = 0 ORDER BY record_date DESC")
    List<StepsHistory> findByDateRange(@Param("userId") Integer userId,
                                       @Param("startDate") LocalDate startDate,
                                       @Param("endDate") LocalDate endDate);
}