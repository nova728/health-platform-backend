package com.health.healthplatform.DTO;

import lombok.Data;

@Data
public class BmiCategoryDTO {
    private Double bmi;
    private String category;
    private String advice;
}