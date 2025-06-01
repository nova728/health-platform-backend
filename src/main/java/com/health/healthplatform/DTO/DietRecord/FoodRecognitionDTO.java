package com.health.healthplatform.DTO.DietRecord;

import lombok.Data;
import java.util.List;

@Data
public class FoodRecognitionDTO {
    private String name;
    private Double calorie;
    private Double trust;
}
