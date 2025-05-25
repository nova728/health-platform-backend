package com.health.healthplatform.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum MealType {
    BREAKFAST("BREAKFAST"),
    LUNCH("LUNCH"),
    DINNER("DINNER"),
    SNACK("SNACK");

    @EnumValue
    private final String value;

    MealType(String value) {
        this.value = value;
    }
}