package com.health.healthplatform.service.impl;

import com.health.healthplatform.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendVerificationCode(String email, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(email);
            helper.setSubject("健康平台验证码");
            
            String content = "<html><body>"
                    + "<h3>健康平台验证码</h3>"
                    + "<p>您的验证码是: <strong style='color:#ff6600;font-size:20px;'>" + code + "</strong></p>"
                    + "<p>验证码有效期为10分钟，请勿将验证码泄露给他人。</p>"
                    + "</body></html>";
            
            helper.setText(content, true);
            mailSender.send(message);
            
            log.info("邮件发送成功 - 收件人: {}", email);
        } catch (Exception e) {
            log.error("邮件发送失败 - 收件人: {}, 错误: {}", email, e.getMessage());
            throw new RuntimeException("邮件发送失败", e);
        }
    }
}
