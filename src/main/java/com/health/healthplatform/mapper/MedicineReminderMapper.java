package com.health.healthplatform.mapper;

import com.health.healthplatform.entity.MedicineReminder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MedicineReminderMapper {
    // 添加用药提醒
    Integer insert(MedicineReminder reminder);

    // 更新用药提醒
    Integer update(MedicineReminder reminder);

    // 删除用药提醒
    Integer deleteById(@Param("id") Long id);

    // 删除药品相关的所有提醒
    Integer deleteByMedicineId(@Param("medicineId") Long medicineId);

    // 查询特定药品的提醒列表
    List<MedicineReminder> selectByMedicineId(@Param("medicineId") Long medicineId);

    // 查询用户的所有提醒
    List<MedicineReminder> selectByUserId(@Param("userId") Long userId);

    // 更新提醒状态
    Integer updateStatus(@Param("id") Long id, @Param("isActive") Boolean isActive);

    // 根据ID和用户ID查询提醒
    MedicineReminder selectByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}