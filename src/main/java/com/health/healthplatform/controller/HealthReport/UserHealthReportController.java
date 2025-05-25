package com.health.healthplatform.controller.HealthReport;

import com.health.healthplatform.DTO.HealthReport.HealthReportDTO;
import com.health.healthplatform.service.HealthReport.HealthReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@CrossOrigin
@RequestMapping("/api/health-reports")
public class UserHealthReportController {

    @Resource
    private HealthReportService healthReportService;

    // 生成健康报告
    @PostMapping("/{userId}")
    public ResponseEntity<HealthReportDTO> generateReport(@PathVariable Integer userId) {
        log.info("开始为用户{}生成健康报告", userId);
        
        if (userId <= 0) {
            log.warn("无效的用户ID: {}", userId);
            return ResponseEntity.badRequest().build();
        }

        try {
            HealthReportDTO report = healthReportService.generateReport(userId);
            return ResponseEntity
                    .ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(report);
        } catch (Exception e) {
            log.error("生成健康报告时发生错误: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // 获取最新的健康报告
    @GetMapping("/{userId}/latest")
    public ResponseEntity<HealthReportDTO> getLatestReport(@PathVariable Integer userId) {
        log.info("获取用户{}的最新健康报告", userId);
        
        if (userId <= 0) {
            log.warn("无效的用户ID: {}", userId);
            return ResponseEntity.badRequest().build();
        }

        try {
            HealthReportDTO report = healthReportService.getLatestReport(userId);
            if (report == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity
                    .ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(report);
        } catch (Exception e) {
            log.error("获取最新健康报告时发生错误: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // 获取健康报告历史记录
    @GetMapping("/{userId}/history")
public ResponseEntity<List<HealthReportDTO>> getReportHistory(
        @PathVariable Integer userId,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
    
    if (startTime == null) {
        startTime = LocalDateTime.now().minusDays(30); // 默认查询最近30天
    }
    
    if (endTime == null) {
        endTime = LocalDateTime.now();
    }
    
    log.info("获取用户{}从{}到{}的健康报告历史", userId, startTime, endTime);
    
    if (userId <= 0) {
        log.warn("无效的用户ID: {}", userId);
        return ResponseEntity.badRequest().build();
    }

    if (startTime.isAfter(endTime)) {
        log.warn("开始时间不能晚于结束时间");
        return ResponseEntity.badRequest().build();
    }

    try {
        List<HealthReportDTO> reports = healthReportService.getReportHistory(userId, startTime, endTime);
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(reports);
    } catch (Exception e) {
        log.error("获取健康报告历史时发生错误: {}", e.getMessage());
        return ResponseEntity.internalServerError().build();
    }
}
}