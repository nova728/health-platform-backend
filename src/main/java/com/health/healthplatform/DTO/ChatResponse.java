package com.health.healthplatform.DTO;

import lombok.Data;

@Data
public class ChatResponse {
    private String response;

    public ChatResponse() {}

    public ChatResponse(String response) {
        this.response = response;
    }
}