package com.health.healthplatform.service;

import com.health.healthplatform.DTO.MedicineReminderDTO;
import com.health.healthplatform.mapper.MedicineMapper;
import com.health.healthplatform.mapper.MedicineReminderMapper;
import com.health.healthplatform.entity.*;
import com.health.healthplatform.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MedicineService {

    private final MedicineMapper medicineMapper;
    private final MedicineReminderMapper reminderMapper;

    // 记录用药信息
    @Transactional
    public Medicine recordMedicine(Medicine medicine) {
        // 验证时间点数量和频率是否匹配
        validateFrequencyTiming(medicine);

        // 验证日期
        validateDates(medicine);

        medicineMapper.insert(medicine);
        return medicine;
    }

    // 获取用药历史
    public List<Medicine> getMedicineHistory(Long userId, String period) {
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = null;

        switch (period.toLowerCase()) {
            case "week":
                startTime = endTime.minusWeeks(1);
                break;
            case "month":
                startTime = endTime.minusMonths(1);
                break;
            case "all":
            default:
                break;
        }

        return medicineMapper.selectByUserIdAndTimeRange(userId, startTime, endTime);
    }

    // 设置用药提醒
    @Transactional
    public MedicineReminder setMedicineReminder(MedicineReminder reminder) {
        reminderMapper.insert(reminder);
        return reminder;
    }

    // 获取用药提醒列表
    public List<MedicineReminder> getMedicineReminders(Long userId) {
        return reminderMapper.selectByUserId(userId);
    }

    // 更新提醒状态
    public void updateReminderStatus(Long reminderId, Boolean isActive) {
        reminderMapper.updateStatus(reminderId, isActive);
    }

    // 更新药品记录
    @Transactional
    public void updateMedicine(Medicine medicine) {
        medicineMapper.update(medicine);
    }

    // 删除药品记录
    @Transactional
    public void deleteMedicine(Long medicineId, Long userId) {
        medicineMapper.deleteById(medicineId, userId);
    }

    // 获取特定药品的提醒列表
    public List<MedicineReminder> getMedicineRemindersByMedicineId(Long medicineId) {
        return reminderMapper.selectByMedicineId(medicineId);
    }

    // 转换为前端展示DTO
    private MedicineRecord toMedicineRecord(Medicine medicine) {
        MedicineRecord record = new MedicineRecord();
        record.setId(medicine.getId());
        record.setName(medicine.getName());
        record.setDosage(medicine.getDosage());
        record.setFrequency(medicine.getFrequency());
        record.setStartDate(medicine.getStartDate());
        record.setEndDate(medicine.getEndDate());
        record.setNotes(medicine.getNotes());

        // 获取并转换提醒列表
        List<MedicineReminder> reminders = getMedicineRemindersByMedicineId(medicine.getId());
        List<MedicineReminderDTO> reminderDTOs = reminders.stream()
                .map(this::toReminderDTO)
                .collect(Collectors.toList());
        record.setReminders(reminderDTOs);

        return record;
    }

    private MedicineReminderDTO toReminderDTO(MedicineReminder reminder) {
        MedicineReminderDTO dto = new MedicineReminderDTO();
        dto.setId(reminder.getId());
        dto.setMedicineId(reminder.getMedicineId());
        dto.setMedicineName(reminder.getMedicine() != null ? reminder.getMedicine().getName() : null);
        dto.setReminderTime(reminder.getReminderTime());
        dto.setRepeatType(reminder.getRepeatType());
        dto.setIsActive(reminder.getIsActive());
        dto.setCreateTime(reminder.getCreatedAt());
        return dto;
    }

    private void validateFrequencyTiming(Medicine medicine) {
        if (medicine.getFrequencyTiming() != null) {
            String frequency = medicine.getFrequency();
            int times = extractTimesFromFrequency(frequency);
            if (medicine.getFrequencyTiming().size() != times) {
                throw new IllegalArgumentException("服用时间点数量与频率不匹配");
            }
        }
    }

    private int extractTimesFromFrequency(String frequency) {
        // 从频率字符串中提取次数，例如"每天2次"返回2
        try {
            return Integer.parseInt(frequency.replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return 0;
        }
    }

    private void validateDates(Medicine medicine) {
        LocalDate now = LocalDate.now();
        if (medicine.getEndDate() != null && medicine.getStartDate() != null
                && medicine.getEndDate().isBefore(medicine.getStartDate())) {
            throw new IllegalArgumentException("结束日期不能早于开始日期");
        }
    }

    @Transactional
    public void deleteMedicineReminder(Long reminderId, Long userId) {
        // 验证提醒是否存在且属于该用户
        MedicineReminder reminder = reminderMapper.selectByIdAndUserId(reminderId, userId);
        if (reminder == null) {
            throw new IllegalArgumentException("提醒不存在或无权限删除");
        }
        reminderMapper.deleteById(reminderId);
    }


}