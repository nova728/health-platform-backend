package com.health.healthplatform.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Tag {
    private Long id;
    private String name;
    private LocalDateTime createdAt;
}