package com.health.healthplatform.service;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.tea.TeaException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;


@Slf4j
@Service
public class SmsService {

    @Value("${aliyun.sms.access-key-id}")
    private String accessKeyId;

    @Value("${aliyun.sms.access-key-secret}")
    private String accessKeySecret;

    @Value("${aliyun.sms.sign-name}")
    private String signName;

    @Value("${aliyun.sms.template-code}")
    private String templateCode;

    @Value("${aliyun.sms.health-report-template-code}")
    private String healthReportTemplateCode;

    /**
     * 创建阿里云短信客户端
     * @return Client
     * @throws Exception
     */
    private Client createClient() throws Exception {
        Config config = new Config()
                .setAccessKeyId(accessKeyId)
                .setAccessKeySecret(accessKeySecret);
        // 短信服务的endpoint
        config.endpoint = "dysmsapi.aliyuncs.com";
        return new Client(config);
    }    /**
     * 发送短信验证码
     * @param phoneNumber 手机号码
     * @param code 验证码
     * @return 发送是否成功
     */
    public boolean sendVerificationCode(String phoneNumber, String code) {
        log.info("开始发送短信验证码 - 手机号: {}, 验证码: {}", phoneNumber, code);
        log.info("短信配置 - AccessKeyId: {}, SignName: {}, TemplateCode: {}", 
                accessKeyId != null ? accessKeyId.substring(0, 8) + "****" : "null", 
                signName, templateCode);
        
        try {
            Client client = createClient();
            log.info("阿里云短信客户端创建成功");
            
            String templateParam = "{\"code\":\"" + code + "\"}";
            log.info("短信模板参数: {}", templateParam);
            
            SendSmsRequest request = new SendSmsRequest()
                    .setPhoneNumbers(phoneNumber)
                    .setSignName(signName)
                    .setTemplateCode(templateCode)
                    .setTemplateParam(templateParam);

            log.info("发送短信请求 - 手机号: {}, 签名: {}, 模板: {}", phoneNumber, signName, templateCode);
            SendSmsResponse response = client.sendSms(request);
            
            log.info("短信发送响应 - RequestId: {}, Code: {}, Message: {}", 
                    response.getBody().getRequestId(),
                    response.getBody().getCode(), 
                    response.getBody().getMessage());
            
            if ("OK".equals(response.getBody().getCode())) {
                log.info("短信发送成功 - 手机号: {}, 验证码: {}", phoneNumber, code);
                return true;
            } else {
                log.error("短信发送失败 - 手机号: {}, 错误码: {}, 错误信息: {}, BizId: {}", 
                    phoneNumber, response.getBody().getCode(), response.getBody().getMessage(),
                    response.getBody().getBizId());
                return false;
            }
            
        } catch (TeaException error) {
            log.error("短信发送TeaException异常 - 手机号: {}, 错误信息: {}, 错误数据: {}", 
                    phoneNumber, error.getMessage(), error.getData());
            if (error.getData() != null && error.getData().containsKey("Recommend")) {
                log.error("建议处理方案: {}", error.getData().get("Recommend"));
            }
            return false;
        } catch (Exception error) {
            log.error("短信发送Exception异常 - 手机号: {}, 错误类型: {}, 错误信息: {}", 
                    phoneNumber, error.getClass().getSimpleName(), error.getMessage());
            log.error("异常堆栈:", error);
            return false;
        }
    }

    /**
     * 验证手机号格式
     * @param phoneNumber 手机号
     * @return 是否为有效手机号
     */
    public boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return false;
        }
        // 中国大陆手机号验证正则表达式
        String regex = "^1[3-9]\\d{9}$";
        return phoneNumber.matches(regex);
    }    public boolean sendWeeklyReport(String phoneNumber, Object reportContent) {
        log.info("发送健康周报 - 手机号: {}", phoneNumber);

        try {
            Client client = createClient();
              // 处理报告内容，限制长度以避免超出短信限制
            String content = reportContent != null ? reportContent.toString() : "暂无数据";
            String summarizedContent = summarizeHealthReport(content);
            
            log.info("原始报告长度: {}, 压缩后长度: {}", content.length(), summarizedContent.length());
            
            // 转义JSON特殊字符
            String escapedContent = summarizedContent
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
            
            String templateParam = "{\"content\":\"" + escapedContent + "\"}";

            SendSmsRequest request = new SendSmsRequest()
                    .setPhoneNumbers(phoneNumber)
                    .setSignName(signName)
                    .setTemplateCode(healthReportTemplateCode)
                    .setTemplateParam(templateParam);

            SendSmsResponse response = client.sendSms(request);

            if ("OK".equals(response.getBody().getCode())) {
                log.info("健康周报发送成功 - 手机号: {}", phoneNumber);
                return true;
            } else {
                log.error("健康周报发送失败 - 错误码: {}, 错误信息: {}", 
                    response.getBody().getCode(), response.getBody().getMessage());
                return false;
            }
        } catch (Exception e) {
            log.error("健康周报发送异常", e);
            return false;
        }
    }
    
    /**
     * 压缩健康报告内容，适合短信发送
     * @param content 原始报告内容
     * @return 压缩后的内容
     */
    private String summarizeHealthReport(String content) {
        if (content == null || content.isEmpty()) {
            return "本周健康数据暂无，建议多记录健康数据以获得个性化建议。";
        }
        
        // 移除多余的空白字符和换行符
        content = content.replaceAll("\\s+", " ").trim();
        
        // 短信内容限制在 300 字以内（考虑到模板参数的限制）
        final int MAX_LENGTH = 300;
        
        if (content.length() <= MAX_LENGTH) {
            return content;
        }
        
        // 截取前部分内容并添加省略号
        String truncated = content.substring(0, MAX_LENGTH - 10);
        
        // 尝试在句号处截断，保持内容完整性
        int lastPeriod = truncated.lastIndexOf("。");
        if (lastPeriod > MAX_LENGTH / 2) {
            truncated = truncated.substring(0, lastPeriod + 1);
        }
        
        return truncated + "...查看完整报告请登录APP。";
    }

    /**
     * 应用启动时验证配置
     */
    @PostConstruct
    public void validateConfig() {
        log.info("验证阿里云短信配置...");
        log.info("AccessKeyId: {}", accessKeyId != null ? accessKeyId.substring(0, Math.min(8, accessKeyId.length())) + "****" : "未配置");
        log.info("AccessKeySecret: {}", accessKeySecret != null ? "已配置" : "未配置");
        log.info("SignName: {}", signName);
        log.info("TemplateCode: {}", templateCode);
        
        if (accessKeyId == null || accessKeyId.isEmpty()) {
            log.error("阿里云短信AccessKeyId未配置！");
        }
        if (accessKeySecret == null || accessKeySecret.isEmpty()) {
            log.error("阿里云短信AccessKeySecret未配置！");
        }
        if (signName == null || signName.isEmpty()) {
            log.error("阿里云短信SignName未配置！");
        }
        if (templateCode == null || templateCode.isEmpty()) {
            log.error("阿里云短信TemplateCode未配置！");
        }
    }

    /**
     * 检查AccessKeyId是否配置
     */
    public boolean isAccessKeyConfigured() {
        return accessKeyId != null && !accessKeyId.isEmpty() && !"请在环境变量中设置".equals(accessKeyId);
    }

    /**
     * 检查AccessKeySecret是否配置
     */
    public boolean isAccessKeySecretConfigured() {
        return accessKeySecret != null && !accessKeySecret.isEmpty() && !"请在环境变量中设置".equals(accessKeySecret);
    }

    /**
     * 检查签名是否配置
     */
    public boolean isSignNameConfigured() {
        return signName != null && !signName.isEmpty();
    }

    /**
     * 检查模板代码是否配置
     */
    public boolean isTemplateCodeConfigured() {
        return templateCode != null && !templateCode.isEmpty();
    }
}