package com.health.healthplatform.controller;

import com.health.healthplatform.DTO.*;
import com.health.healthplatform.service.health_data.*;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/health")
@Slf4j
public class HealthDataController {
    private final HealthDataService healthDataService;

    @Resource
    HeartRateService heartRateService;

    @Resource
    StepsService stepsService;

    @Resource
    SleepService sleepService;

    @Resource
    BloodPressureService bloodPressureService;

    @Resource
    WeightService weightService;

    @Resource
    HeightService heightService;

    @Resource
    BmiService bmiService;

    public HealthDataController(HealthDataService healthDataService) {
        this.healthDataService = healthDataService;
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<HealthDataDTO>> getHealthHistory(@PathVariable("id") Integer userId) {
        log.info("Received request for user id: {}", userId);

        if (userId <= 0) {
            log.warn("Invalid user id: {}", userId);
            return ResponseEntity.badRequest().build();
        }

        List<HealthDataDTO> healthData = healthDataService.getHealthHistory(userId);
        log.info("Found {} records for user {}", healthData.size(), userId);
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(healthData);
    }

    //心率详情
    @GetMapping("/{id}/heart-rate")
    public ResponseEntity<List<HeartRateDTO>> getHeartRateHistory(
            @PathVariable("id") Integer userId,
            @RequestParam(value = "period", defaultValue = "day") String period) {

        log.info("Fetching heart rate history for user: {}, period: {}", userId, period);

        if (userId <= 0) {
            log.warn("Invalid user id: {}", userId);
            return ResponseEntity.badRequest().build();
        }

        List<HeartRateDTO> heartRateData = heartRateService.getUserHeartRateHistory(userId, period);
        log.info("Found {} heart rate records for user {}", heartRateData.size(), userId);

        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(heartRateData);
    }

    @PostMapping("/{id}/heart-rate")
    public ResponseEntity<HeartRateDTO> recordHeartRate(
            @PathVariable("id") Integer userId,
            @RequestBody HeartRateDTO heartRateDTO) {

        log.info("Recording new heart rate for user: {}", userId);

        if (userId <= 0) {
            log.warn("Invalid user id: {}", userId);
            return ResponseEntity.badRequest().build();
        }

        try {
            HeartRateDTO recorded = heartRateService.recordHeartRate(userId, heartRateDTO.getHeartRate());
            return ResponseEntity.ok(recorded);
        } catch (IllegalArgumentException e) {
            log.error("Invalid heart rate data: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    //步数
    @GetMapping("/{id}/steps")
    public ResponseEntity<List<StepsDTO>> getStepsHistory(
            @PathVariable("id") Integer userId,
            @RequestParam(value = "period", defaultValue = "day") String period) {

        log.info("Fetching steps history for user: {}, period: {}", userId, period);

        if (userId <= 0) {
            log.warn("Invalid user id: {}", userId);
            return ResponseEntity.badRequest().build();
        }

        List<StepsDTO> stepsData = stepsService.getUserStepsHistory(userId, period);
        log.info("Found {} steps records for user {}", stepsData.size(), userId);

        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(stepsData);
    }

    @PostMapping("/{id}/steps")
    public ResponseEntity<StepsDTO> recordSteps(
            @PathVariable("id") Integer userId,
            @RequestBody StepsDTO stepsDTO) {

        log.info("Recording new steps for user: {}", userId);

        if (userId <= 0) {
            log.warn("Invalid user id: {}", userId);
            return ResponseEntity.badRequest().build();
        }

        try {
            StepsDTO recorded = stepsService.recordSteps(userId, stepsDTO.getSteps());
            return ResponseEntity.ok(recorded);
        } catch (IllegalArgumentException e) {
            log.error("Invalid steps data: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    //睡眠
    @GetMapping("/{id}/sleep")
    public ResponseEntity<List<SleepDTO>> getSleepHistory(
            @PathVariable("id") Integer userId,
            @RequestParam(value = "period", defaultValue = "day") String period) {

        if (userId <= 0) {
            return ResponseEntity.badRequest().build();
        }

        List<SleepDTO> sleepData = sleepService.getUserSleepHistory(userId, period);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(sleepData);
    }

    @PostMapping("/{id}/sleep")
    public ResponseEntity<SleepDTO> recordSleep(
            @PathVariable("id") Integer userId,
            @RequestBody SleepDTO sleepDTO) {

        if (userId <= 0) {
            return ResponseEntity.badRequest().build();
        }

        try {
            SleepDTO recorded = sleepService.recordSleep(
                    userId,
                    sleepDTO.getDuration(),
                    sleepDTO.getQuality()
            );
            return ResponseEntity.ok(recorded);
        } catch (IllegalArgumentException e) {
            log.error("Invalid sleep data: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}/blood-pressure")
    public ResponseEntity<List<BloodPressureDTO>> getBloodPressureHistory(
            @PathVariable("id") Integer userId,
            @RequestParam(value = "period", defaultValue = "day") String period) {

        log.info("Fetching blood pressure history for user: {}, period: {}", userId, period);

        if (userId <= 0) {
            log.warn("Invalid user id: {}", userId);
            return ResponseEntity.badRequest().build();
        }

        List<BloodPressureDTO> bloodPressureData =
                bloodPressureService.getUserBloodPressureHistory(userId, period);
        log.info("Found {} blood pressure records for user {}", bloodPressureData.size(), userId);

        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(bloodPressureData);
    }

    @PostMapping("/{id}/blood-pressure")
    public ResponseEntity<BloodPressureDTO> recordBloodPressure(
            @PathVariable("id") Integer userId,
            @RequestBody BloodPressureDTO bloodPressureDTO) {

        log.info("Recording new blood pressure for user: {}", userId);

        if (userId <= 0) {
            log.warn("Invalid user id: {}", userId);
            return ResponseEntity.badRequest().build();
        }

        try {
            BloodPressureDTO recorded = bloodPressureService.recordBloodPressure(
                    userId,
                    bloodPressureDTO.getSystolic(),
                    bloodPressureDTO.getDiastolic()
            );
            return ResponseEntity.ok(recorded);
        } catch (IllegalArgumentException e) {
            log.error("Invalid blood pressure data: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}/height")
    public ResponseEntity<List<HeightDTO>> getHeightHistory(
            @PathVariable("id") Integer userId,
            @RequestParam(value = "period", defaultValue = "day") String period) {

        log.info("Fetching height history for user: {}, period: {}", userId, period);

        if (userId <= 0) {
            log.warn("Invalid user id: {}", userId);
            return ResponseEntity.badRequest().build();
        }

        List<HeightDTO> heightData = heightService.getUserHeightHistory(userId, period);
        log.info("Found {} height records for user {}", heightData.size(), userId);

        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(heightData);
    }

    @PostMapping("/{id}/height")
    public ResponseEntity<HeightDTO> recordHeight(
            @PathVariable("id") Integer userId,
            @RequestBody HeightDTO heightDTO) {

        log.info("Recording new height for user: {}", userId);

        if (userId <= 0) {
            log.warn("Invalid user id: {}", userId);
            return ResponseEntity.badRequest().build();
        }

        try {
            HeightDTO recorded = heightService.recordHeight(userId, heightDTO.getHeight());
            return ResponseEntity.ok(recorded);
        } catch (IllegalArgumentException e) {
            log.error("Invalid height data: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // 体重相关接口
    @GetMapping("/{id}/weight")
    public ResponseEntity<List<WeightDTO>> getWeightHistory(
            @PathVariable("id") Integer userId,
            @RequestParam(value = "period", defaultValue = "day") String period) {

        log.info("Fetching weight history for user: {}, period: {}", userId, period);

        if (userId <= 0) {
            log.warn("Invalid user id: {}", userId);
            return ResponseEntity.badRequest().build();
        }

        List<WeightDTO> weightData = weightService.getUserWeightHistory(userId, period);
        log.info("Found {} weight records for user {}", weightData.size(), userId);

        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(weightData);
    }

    @PostMapping("/{id}/weight")
    public ResponseEntity<WeightDTO> recordWeight(
            @PathVariable("id") Integer userId,
            @RequestBody WeightDTO weightDTO) {

        log.info("Recording new weight for user: {}", userId);

        if (userId <= 0) {
            log.warn("Invalid user id: {}", userId);
            return ResponseEntity.badRequest().build();
        }

        try {
            WeightDTO recorded = weightService.recordWeight(userId, weightDTO.getWeight());
            return ResponseEntity.ok(recorded);
        } catch (IllegalArgumentException e) {
            log.error("Invalid weight data: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // BMI相关接口
    @GetMapping("/{id}/bmi")
    public ResponseEntity<List<BmiDTO>> getBmiHistory(
            @PathVariable("id") Integer userId,
            @RequestParam(value = "period", defaultValue = "day") String period) {

        log.info("Fetching BMI history for user: {}, period: {}", userId, period);

        if (userId <= 0) {
            log.warn("Invalid user id: {}", userId);
            return ResponseEntity.badRequest().build();
        }

        List<BmiDTO> bmiData = bmiService.getUserBmiHistory(userId, period);
        log.info("Found {} BMI records for user {}", bmiData.size(), userId);

        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(bmiData);
    }

    @PostMapping("/{id}/bmi/calculate")
    public ResponseEntity<BmiDTO> calculateAndRecordBmi(
            @PathVariable("id") Integer userId,
            @RequestBody BmiDTO bmiDTO) {

        log.info("Calculating and recording BMI for user: {}", userId);

        if (userId <= 0) {
            log.warn("Invalid user id: {}", userId);
            return ResponseEntity.badRequest().build();
        }

        try {
            // 验证必需的数据
            if (bmiDTO.getHeight() == null || bmiDTO.getWeight() == null) {
                throw new IllegalArgumentException("身高和体重数据不能为空");
            }

            // 计算BMI
            double heightInMeters = bmiDTO.getHeight() / 100.0;
            double bmi = bmiDTO.getWeight() / (heightInMeters * heightInMeters);
            bmi = Math.round(bmi * 10) / 10.0; // 保留一位小数

            // 记录BMI
            BmiDTO recorded = bmiService.recordBmi(
                    userId,
                    bmi,
                    bmiDTO.getWeight(),
                    bmiDTO.getHeight()
            );
            return ResponseEntity.ok(recorded);
        } catch (IllegalArgumentException e) {
            log.error("Invalid BMI data: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // BMI类别评估接口
    @GetMapping("/{id}/bmi/category")
    public ResponseEntity<BmiCategoryDTO> getBmiCategory(
            @PathVariable("id") Integer userId) {

        log.info("Getting BMI category for user: {}", userId);

        if (userId <= 0) {
            log.warn("Invalid user id: {}", userId);
            return ResponseEntity.badRequest().build();
        }

        try {
            List<BmiDTO> bmiHistory = bmiService.getUserBmiHistory(userId, "day");
            if (bmiHistory.isEmpty()) {
                return ResponseEntity.noContent().build();
            }

            BmiDTO latestBmi = bmiHistory.get(0);
            BmiCategoryDTO category = new BmiCategoryDTO();
            category.setBmi(latestBmi.getBmi());

            // 根据WHO标准设置BMI分类
            if (latestBmi.getBmi() < 18.5) {
                category.setCategory("偏瘦");
                category.setAdvice("建议适当增加营养摄入，保持均衡饮食");
            } else if (latestBmi.getBmi() < 24) {
                category.setCategory("正常");
                category.setAdvice("恭喜！请继续保持健康的生活方式");
            } else if (latestBmi.getBmi() < 28) {
                category.setCategory("偏胖");
                category.setAdvice("建议适当控制饮食，增加运动量");
            } else {
                category.setCategory("肥胖");
                category.setAdvice("建议咨询医生，制定科学的减重计划");
            }

            return ResponseEntity.ok(category);
        } catch (Exception e) {
            log.error("Error getting BMI category: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}