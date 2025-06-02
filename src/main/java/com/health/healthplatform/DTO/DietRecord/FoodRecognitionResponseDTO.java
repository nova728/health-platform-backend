package com.health.healthplatform.DTO.DietRecord;

import lombok.Data;
import java.util.List;

@Data
public class FoodRecognitionResponseDTO {
    private List<FoodRecognitionDTO> foods;
}
