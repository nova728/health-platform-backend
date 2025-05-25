package com.health.healthplatform.mapper.health_data;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.health.healthplatform.entity.healthdata.ExerciseRecord;
import com.health.healthplatform.DTO.UserRankDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface ExerciseRecordMapper extends BaseMapper<ExerciseRecord> {

    @Select("SELECT " +
            "exercise_type as TYPE, " +
            "CAST(COALESCE(SUM(duration), 0) AS DECIMAL(10,2)) as DURATION " +
            "FROM exercise_records " +
            "WHERE user_id = #{userId} " +
            "GROUP BY exercise_type")
    List<Map<String, Object>> getTotalDurationByType(@Param("userId") Integer userId);

    @Select("SELECT " +
            "u.id as userId, " +
            "u.username, " +
            "u.avatar, " +
            "SUM(e.duration) as totalDuration " +
            "FROM exercise_records e " +
            "JOIN user u ON e.user_id = u.id " +
            "WHERE e.record_date BETWEEN #{startDate} AND #{endDate} " +
            "GROUP BY u.id, u.username, u.avatar " +
            "ORDER BY totalDuration DESC")
    List<UserRankDTO> getUserRankings(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
    // 原有的注解查询方法
    @Select("SELECT * FROM exercise_records WHERE user_id = #{userId} " +
            "AND record_date BETWEEN #{startDate} AND #{endDate} " +
            "ORDER BY record_date DESC")
    List<ExerciseRecord> findByDateRange(
            @Param("userId") Integer userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Select("SELECT * FROM exercise_records WHERE user_id = #{userId} " +
            "ORDER BY record_date DESC")
    List<ExerciseRecord> findAllByUserId(@Param("userId") Integer userId);

    @Select("SELECT SUM(duration) FROM exercise_records WHERE user_id = #{userId} " +
            "AND record_date BETWEEN #{startDate} AND #{endDate}")
    Integer getTotalDuration(
            @Param("userId") Integer userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Select("SELECT SUM(calories) FROM exercise_records WHERE user_id = #{userId} " +
            "AND record_date BETWEEN #{startDate} AND #{endDate}")
    Integer getTotalCalories(
            @Param("userId") Integer userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Select("SELECT COUNT(*) FROM exercise_records WHERE user_id = #{userId} " +
            "AND record_date BETWEEN #{startDate} AND #{endDate} ")
    Integer getExerciseCount(
            @Param("userId") Integer userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );


}