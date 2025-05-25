package com.health.healthplatform.controller;

import com.health.healthplatform.DTO.ExerciseGoalDTO;
import com.health.healthplatform.DTO.ExerciseRecordDTO;
import com.health.healthplatform.DTO.WeeklyStats;
import com.health.healthplatform.service.health_data.ExerciseRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/health")
@Slf4j
public class ExerciseRecordController {
    private final ExerciseRecordService exerciseRecordService;

    public ExerciseRecordController(ExerciseRecordService exerciseRecordService) {
        this.exerciseRecordService = exerciseRecordService;
    }

    @GetMapping("/{userId}/exercise")
    public ResponseEntity<List<ExerciseRecordDTO>> getExerciseRecords(
            @PathVariable Integer userId,
            @RequestParam(defaultValue = "week") String period) {
        return ResponseEntity.ok(exerciseRecordService.getUserExerciseRecords(userId, period));
    }

    @PostMapping("/{userId}/exercise")
    public ResponseEntity<ExerciseRecordDTO> addExerciseRecord(
            @PathVariable Integer userId,
            @RequestBody ExerciseRecordDTO record) {
        return ResponseEntity.ok(exerciseRecordService.recordExercise(userId, record));
    }

    @PutMapping("/{userId}/exercise/{recordId}")
    public ResponseEntity<ExerciseRecordDTO> updateExerciseRecord(
            @PathVariable Integer userId,
            @PathVariable Long recordId,
            @RequestBody ExerciseRecordDTO record) {
        return ResponseEntity.ok(exerciseRecordService.updateExerciseRecord(userId, recordId, record));
    }

    @DeleteMapping("/{userId}/exercise/{recordId}")
    public ResponseEntity<Void> deleteExerciseRecord(
            @PathVariable Integer userId,
            @PathVariable Long recordId) {
        exerciseRecordService.deleteExerciseRecord(userId, recordId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{userId}/exercise/stats/weekly")
    public ResponseEntity<WeeklyStats> getWeeklyStats(@PathVariable Integer userId) {
        return ResponseEntity.ok(exerciseRecordService.getWeeklyStats(userId));
    }

    @GetMapping("/{userId}/exercise/goals")
    public ResponseEntity<ExerciseGoalDTO> getUserGoals(@PathVariable Integer userId) {
        return ResponseEntity.ok(exerciseRecordService.getUserGoals(userId));
    }

    @PutMapping("/{userId}/exercise/goals")
    public ResponseEntity<ExerciseGoalDTO> updateUserGoals(
            @PathVariable Integer userId,
            @RequestBody ExerciseGoalDTO goals) {
        return ResponseEntity.ok(exerciseRecordService.updateUserGoals(userId, goals));
    }
}