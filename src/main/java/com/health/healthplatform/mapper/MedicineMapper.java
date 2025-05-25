package com.health.healthplatform.mapper;

import com.health.healthplatform.entity.Medicine;
import com.health.healthplatform.entity.MedicineReminder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.time.LocalDateTime;

@Mapper
public interface MedicineMapper {
    // 添加药品记录
    Integer insert(Medicine medicine);

    // 更新药品记录
    Integer update(Medicine medicine);

    // 删除药品记录
    Integer deleteById(@Param("id") Long id, @Param("userId") Long userId);

    // 根据ID和用户ID查询药品记录
    Medicine selectByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    // 查询用户的药品记录（支持时间范围）
    List<Medicine> selectByUserIdAndTimeRange(
            @Param("userId") Long userId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );
}
