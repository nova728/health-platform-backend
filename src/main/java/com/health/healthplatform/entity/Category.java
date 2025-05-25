package com.health.healthplatform.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Category {
    private Integer id;
    private String name;
    private String icon;
    private String description;
    private LocalDateTime createdAt;
}