package com.health.healthplatform.controller;

import com.health.healthplatform.result.Result;
import com.health.healthplatform.entity.*;
import com.health.healthplatform.service.MedicineService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/medicines")
@RequiredArgsConstructor
@Slf4j
public class MedicineController {

    private final MedicineService medicineService;

    /**
     * 获取用药历史记录
     */
    @GetMapping("/{userId}/medicine")
    public Result getMedicineHistory(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "week") String period
    ) {
        try {
            List<Medicine> records = medicineService.getMedicineHistory(userId, period);
            return Result.success(records);
        } catch (Exception e) {
            return Result.failure(500, "获取用药历史失败：" + e.getMessage());
        }
    }

    /**
     * 添加用药记录
     */
    @PostMapping("/{userId}/medicine")
    public Result recordMedicine(
            @PathVariable Long userId,
            @RequestBody Medicine medicine
    ) {
        try {
            // 添加日志
            log.info("Received medicine record request for user {}: {}", userId, medicine);

            medicine.setUserId(userId);
            Medicine savedMedicine = medicineService.recordMedicine(medicine);
            return Result.success(savedMedicine);
        } catch (Exception e) {
            log.error("Failed to create medicine record", e);
            return Result.failure(500, "添加用药记录失败：" + e.getMessage());
        }
    }

    /**
     * 更新用药记录
     */
    @PutMapping("/{userId}/medicine/{medicineId}")
    public Result updateMedicine(
            @PathVariable Long userId,
            @PathVariable Long medicineId,
            @RequestBody Medicine medicine
    ) {
        try {
            medicine.setId(medicineId);
            medicine.setUserId(userId);
            medicineService.updateMedicine(medicine);
            return Result.success(null);
        } catch (Exception e) {
            return Result.failure(500, "更新用药记录失败：" + e.getMessage());
        }
    }

    /**
     * 删除用药记录
     */
    @DeleteMapping("/{userId}/medicine/{medicineId}")
    public Result deleteMedicine(
            @PathVariable Long userId,
            @PathVariable Long medicineId
    ) {
        try {
            medicineService.deleteMedicine(medicineId, userId);
            return Result.success(null);
        } catch (Exception e) {
            return Result.failure(500, "删除用药记录失败：" + e.getMessage());
        }
    }

    /**
     * 获取用药提醒列表
     */
    @GetMapping("/{userId}/medicine/reminders")
    public Result getMedicineReminders(
            @PathVariable Long userId
    ) {
        try {
            List<MedicineReminder> reminders = medicineService.getMedicineReminders(userId);
            return Result.success(reminders);
        } catch (Exception e) {
            return Result.failure(500, "获取用药提醒失败：" + e.getMessage());
        }
    }

    /**
     * 设置用药提醒
     */
    @PostMapping("/{userId}/medicine/{medicineId}/reminder")
    public Result setMedicineReminder(
            @PathVariable Long userId,
            @PathVariable Long medicineId,
            @RequestBody MedicineReminder reminder
    ) {
        try {
            reminder.setMedicineId(medicineId);
            MedicineReminder savedReminder = medicineService.setMedicineReminder(reminder);
            return Result.success(savedReminder);
        } catch (Exception e) {
            return Result.failure(500, "设置用药提醒失败：" + e.getMessage());
        }
    }

    /**
     * 更新提醒状态（启用/禁用）
     */
    @PutMapping("/{userId}/medicine/reminders/{reminderId}/status")
    public Result updateReminderStatus(
            @PathVariable Long userId,
            @PathVariable Long reminderId,
            @RequestBody ReminderStatusRequest request
    ) {
        try {
            medicineService.updateReminderStatus(reminderId, request.getIsActive());
            return Result.success(null);
        } catch (Exception e) {
            return Result.failure(500, "更新提醒状态失败：" + e.getMessage());
        }
    }

    /**
     * 删除用药提醒
     */
    @DeleteMapping("/{userId}/medicine/reminders/{reminderId}")
    public Result deleteMedicineReminder(
            @PathVariable Long userId,
            @PathVariable Long reminderId
    ) {
        try {
            medicineService.deleteMedicineReminder(reminderId, userId);
            return Result.success(null);
        } catch (IllegalArgumentException e) {
            return Result.failure(400, e.getMessage());
        } catch (Exception e) {
            log.error("Failed to delete medicine reminder", e);
            return Result.failure(500, "删除用药提醒失败：" + e.getMessage());
        }
    }
}

/**
 * 提醒状态请求体
 */
@Data
class ReminderStatusRequest {
    private Boolean isActive;  // 是否启用提醒
}