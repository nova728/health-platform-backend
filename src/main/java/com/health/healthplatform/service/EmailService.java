package com.health.healthplatform.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendVerificationCode(String toEmail, String code) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject("验证码");

        String content = String.format(
                "<html><body><h3>您的验证码是：%s</h3>" +
                        "<p>验证码有效期为5分钟，请尽快使用。</p>" +
                        "<p>如果这不是您的操作，请忽略此邮件。</p></body></html>",
                code
        );

        helper.setText(content, true);
        mailSender.send(message);
    }
}