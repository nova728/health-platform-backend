package com.health.healthplatform.DTO.deepseek;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class DeepSeekRequest {
    private String model;
    private List<Message> messages;
    @JsonProperty("max_tokens")
    private Integer maxTokens;
    private Double temperature;

    // 构造函数
    public DeepSeekRequest() {}

    public DeepSeekRequest(String model, List<Message> messages, Integer maxTokens, Double temperature) {
        this.model = model;
        this.messages = messages;
        this.maxTokens = maxTokens;
        this.temperature = temperature;
    }

    @Data
    public static class Message {
        private String role;
        private String content;

        public Message() {}

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }
}