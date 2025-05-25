package com.health.healthplatform.service;

import com.health.healthplatform.DTO.ChatRequest;
import com.health.healthplatform.DTO.ChatResponse;
import com.health.healthplatform.config.ErnieBotConfig;
import okhttp3.*;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
public class ChatService {
    private final OkHttpClient httpClient;
    private final AtomicReference<String> cachedAccessToken;
    private volatile long tokenExpirationTime;
    private volatile long lastRequestTime = 0;
    private static final long MIN_REQUEST_INTERVAL = 1000; // 最小请求间隔（毫秒）

    @Autowired
    private ErnieBotConfig ernieBotConfig;

    public ChatService() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        this.cachedAccessToken = new AtomicReference<>();
    }

    private void ensureRequestInterval() throws InterruptedException {
        long currentTime = System.currentTimeMillis();
        long timeSinceLastRequest = currentTime - lastRequestTime;
        if (timeSinceLastRequest < MIN_REQUEST_INTERVAL) {
            Thread.sleep(MIN_REQUEST_INTERVAL - timeSinceLastRequest);
        }
        lastRequestTime = System.currentTimeMillis();
    }

    public ChatResponse processMessage(ChatRequest request) {
        try {
            ensureRequestInterval();
            String accessToken = getOrRefreshAccessToken();
            String response = callErnieBot(request.getMessage(), accessToken);
            return new ChatResponse(response);
        } catch (Exception e) {
            log.error("处理消息失败: ", e);
            return new ChatResponse("抱歉，系统当前访问人数较多，请稍后再试");
        }
    }

    private synchronized String getOrRefreshAccessToken() throws IOException {
        String currentToken = cachedAccessToken.get();
        if (currentToken != null && System.currentTimeMillis() < tokenExpirationTime) {
            return currentToken;
        }

        log.info("开始获取访问令牌");
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "");

        String url = String.format("https://aip.baidubce.com/oauth/2.0/token?grant_type=client_credentials&client_id=%s&client_secret=%s",
                ernieBotConfig.getApiKey(), ernieBotConfig.getSecretKey());

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body().string();
            log.info("Token API响应: {}", responseBody);

            if (!response.isSuccessful()) {
                throw new IOException("获取访问令牌失败: " + response.code());
            }

            JSONObject jsonResponse = new JSONObject(responseBody);
            String newToken = jsonResponse.getString("access_token");
            int expiresIn = jsonResponse.getInt("expires_in");

            tokenExpirationTime = System.currentTimeMillis() + (expiresIn - 300) * 1000L;
            cachedAccessToken.set(newToken);

            return newToken;
        }
    }

    private String callErnieBot(String message, String accessToken) throws IOException {
        log.info("调用文心一言API, 消息: {}", message);

        JSONObject requestBody = new JSONObject()
                .put("messages", new JSONObject[]{
                        new JSONObject()
                                .put("role", "user")
                                .put("content", message)
                });

        log.info("请求体: {}", requestBody);

        Request request = new Request.Builder()
                .url("https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/eb-instant?access_token=" + accessToken)
                .post(RequestBody.create(MediaType.parse("application/json"), requestBody.toString()))
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body().string();
            log.info("API响应: {}", responseBody);

            if (!response.isSuccessful()) {
                throw new IOException("调用API失败: " + response.code());
            }

            JSONObject jsonResponse = new JSONObject(responseBody);

            if (jsonResponse.has("error_code")) {
                String errorMsg = jsonResponse.optString("error_msg", "未知错误");
                log.error("API返回错误: {}", errorMsg);
                throw new IOException("API错误: " + errorMsg);
            }

            return jsonResponse.optString("result", "抱歉，无法获取有效回复");
        } catch (Exception e) {
            log.error("调用文心一言API失败: ", e);
            throw e;
        }
    }
}