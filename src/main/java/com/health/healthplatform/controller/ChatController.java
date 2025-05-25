package com.health.healthplatform.controller;

import com.health.healthplatform.service.ChatService;
import com.health.healthplatform.DTO.ChatRequest;
import com.health.healthplatform.DTO.ChatResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(@RequestBody ChatRequest request) {
        try {
            log.info("收到聊天请求: {}", request.getMessage());
            ChatResponse response = chatService.processMessage(request);
            log.info("生成响应: {}", response.getResponse());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("处理消息时发生错误: ", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "处理消息时发生错误: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }
}