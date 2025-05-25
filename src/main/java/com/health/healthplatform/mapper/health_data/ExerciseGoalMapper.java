package com.health.healthplatform.mapper.health_data;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.health.healthplatform.entity.healthdata.ExerciseGoal;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ExerciseGoalMapper extends BaseMapper<ExerciseGoal> {
    @Select("SELECT * FROM exercise_goals WHERE user_id = #{userId}")
    ExerciseGoal findByUserId(Integer userId);
}