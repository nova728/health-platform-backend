package com.health.healthplatform.controller;

import com.health.healthplatform.entity.User;
import com.health.healthplatform.entity.UserSettings;
import com.health.healthplatform.entity.VerificationCode;
import com.health.healthplatform.result.Result;
import com.health.healthplatform.service.SettingService;
import com.health.healthplatform.service.UserService;
import com.health.healthplatform.service.VerificationCodeService;
import com.health.healthplatform.service.EmailService;
import com.health.healthplatform.service.SmsService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/api/settings")
public class SettingController {

    @Resource
    private SettingService settingService;

    @Resource
    private UserService userService;    @Autowired
    private VerificationCodeService verificationCodeService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private SmsService smsService;

    // 修改密码
    @CrossOrigin
    @PostMapping("/{userId}/password")
    public Result updatePassword(
            @PathVariable Integer userId,
            @RequestBody Map<String, String> passwords) {
        try {
            String currentPassword = passwords.get("currentPassword");
            String newPassword = passwords.get("newPassword");

            // 验证当前密码是否正确
            User userCheck = new User();
            userCheck.setId(userId);
            userCheck.setPassword(currentPassword);
            User user = userService.findUserByNameAndPwd(userCheck);

            if (user == null) {
                return Result.failure(401, "当前密码错误");
            }

            // 更新密码
            settingService.updatePassword(userId, newPassword);
            return Result.success("密码修改成功");
        } catch (Exception e) {
            return Result.failure(500, "密码修改失败: " + e.getMessage());
        }
    }    // 更新手机号
    @CrossOrigin
    @PostMapping("/{userId}/phone")
    public Result updatePhone(
            @PathVariable Integer userId,
            @RequestBody Map<String, String> body) {
        try {
            String phone = body.get("phone");
            String verificationCode = body.get("verificationCode");

            // 验证手机号格式
            if (!smsService.isValidPhoneNumber(phone)) {
                return Result.failure(400, "手机号格式不正确");
            }

            // 验证验证码
            boolean isValid = verificationCodeService.verifyCode(userId, phone, verificationCode, "PHONE");
            if (!isValid) {
                return Result.failure(400, "验证码错误或已过期");
            }

            settingService.updatePhone(userId, phone);
            return Result.success("手机号更新成功");
        } catch (Exception e) {
            return Result.failure(500, "手机号更新失败: " + e.getMessage());
        }
    }    // 更新邮箱
    @CrossOrigin
    @PostMapping("/{userId}/email")
    public Result updateEmail(
            @PathVariable Integer userId,
            @RequestBody Map<String, String> body) {
        try {
            String email = body.get("email");
            String verificationCode = body.get("verificationCode");

            // 验证验证码
            boolean isValid = verificationCodeService.verifyCode(userId, email, verificationCode, "EMAIL");
            if (!isValid) {
                return Result.failure(400, "验证码错误或已过期");
            }

            settingService.updateEmail(userId, email);
            return Result.success("邮箱更新成功");
        } catch (Exception e) {
            return Result.failure(500, "邮箱更新失败: " + e.getMessage());
        }
    }

    // 获取用户设置
    @CrossOrigin
    @GetMapping("/{userId}")
    public Result getUserSettings(@PathVariable Integer userId) {
        try {
            UserSettings settings = settingService.getUserSettings(userId);
            if (settings == null) {
                // 如果设置不存在，创建默认设置
                UserSettings defaultSettings = new UserSettings();
                defaultSettings.setUserId(userId);
                defaultSettings.setSystemNotification(true);
                defaultSettings.setExerciseNotification(true);
                defaultSettings.setDietNotification(true);
                defaultSettings.setProfileVisibility("public");
                defaultSettings.setExerciseVisibility("public");
                defaultSettings.setLanguage("zh-CN");
                defaultSettings.setTheme("light");

                settingService.createUserSettings(defaultSettings);
                return Result.success(defaultSettings);
            }
            return Result.success(settings);
        } catch (Exception e) {
            return Result.failure(500, "获取设置失败: " + e.getMessage());
        }
    }

    // 更新通知设置
    @CrossOrigin
    @PutMapping("/{userId}/notifications")
    public Result updateNotificationSettings(
            @PathVariable Integer userId,
            @RequestBody UserSettings settings) {
        try {
            settings.setUserId(userId);
            settingService.updateNotificationSettings(settings);
            return Result.success("通知设置更新成功");
        } catch (Exception e) {
            return Result.failure(500, "通知设置更新失败: " + e.getMessage());
        }
    }

    // 更新隐私设置
    @CrossOrigin
    @PutMapping("/{userId}/privacy")
    public Result updatePrivacySettings(
            @PathVariable Integer userId,
            @RequestBody UserSettings settings) {
        try {
            settings.setUserId(userId);
            settingService.updatePrivacySettings(settings);
            return Result.success("隐私设置更新成功");
        } catch (Exception e) {
            return Result.failure(500, "隐私设置更新失败: " + e.getMessage());
        }
    }    // 更新通用设置
    @CrossOrigin
    @PutMapping("/{userId}/general")
    public Result updateGeneralSettings(
            @PathVariable Integer userId,
            @RequestBody UserSettings settings) {
        try {
            settings.setUserId(userId);
            settingService.updateGeneralSettings(settings);
            return Result.success("通用设置更新成功");
        } catch (Exception e) {
            return Result.failure(500, "通用设置更新失败: " + e.getMessage());        }
    }

    // 发送手机验证码
    @CrossOrigin
    @PostMapping("/{userId}/send-phone-code")
    public Result sendPhoneVerificationCode(
            @PathVariable Integer userId,
            @RequestBody Map<String, String> body) {
        try {
            String phone = body.get("phone");
            if (phone == null || phone.isEmpty()) {
                return Result.failure(400, "手机号不能为空");
            }

            // 验证手机号格式
            if (!smsService.isValidPhoneNumber(phone)) {
                return Result.failure(400, "手机号格式不正确");
            }

            // 生成并存储验证码
            VerificationCode verificationCode = verificationCodeService.createVerificationCode(
                    userId, phone, "PHONE");

            // 发送短信
            boolean sendSuccess = smsService.sendVerificationCode(phone, verificationCode.getCode());
            
            if (sendSuccess) {
                return Result.success("验证码发送成功");
            } else {
                return Result.failure(500, "验证码发送失败，请稍后重试");
            }
        } catch (Exception e) {
            log.error("发送手机验证码异常", e);
            return Result.failure(500, "验证码发送失败: " + e.getMessage());
        }
    }

    // 验证验证码
    @CrossOrigin
    @PostMapping("/{userId}/verify-code")
    public Result verifyCode(
            @PathVariable Integer userId,
            @RequestBody Map<String, String> body) {
        try {
            String target = body.get("target"); // phone或email
            String code = body.get("code");
            String type = body.get("type");  // PHONE或EMAIL

            boolean isValid = verificationCodeService.verifyCode(userId, target, code, type);
            if (isValid) {
                return Result.success("验证成功");
            } else {
                return Result.failure(400, "验证码错误或已过期");
            }
        } catch (Exception e) {
            return Result.failure(500, "验证失败: " + e.getMessage());
        }
    }

    // 发送邮箱验证码
    @CrossOrigin
    @PostMapping("/{userId}/send-email-code")
    public Result sendEmailVerificationCode(
            @PathVariable Integer userId,
            @RequestBody Map<String, String> body) {
        try {
            String email = body.get("email");
            if (email == null || email.isEmpty()) {
                return Result.failure(400, "邮箱地址不能为空");
            }

            // 生成并存储验证码
            VerificationCode verificationCode = verificationCodeService.createVerificationCode(
                    userId, email, "EMAIL");

            // 发送邮件
            emailService.sendVerificationCode(email, verificationCode.getCode());

            return Result.success("验证码发送成功");
        } catch (Exception e) {
            return Result.failure(500, "验证码发送失败: " + e.getMessage());
        }
    }

    // 检查阿里云配置状态
    @CrossOrigin
    @GetMapping("/check-sms-config")
    public Result checkSmsConfig() {
        try {
            Map<String, Object> configStatus = new HashMap<>();
            
            // 检查配置是否正确加载
            configStatus.put("accessKeyIdConfigured", smsService.isAccessKeyConfigured());
            configStatus.put("accessKeySecretConfigured", smsService.isAccessKeySecretConfigured());            configStatus.put("signNameConfigured", smsService.isSignNameConfigured());
            configStatus.put("templateCodeConfigured", smsService.isTemplateCodeConfigured());
            
            // 检查手机号格式验证
            configStatus.put("phoneValidationWorking", smsService.isValidPhoneNumber("13912345678"));
            
            return Result.success(configStatus);
        } catch (Exception e) {
            log.error("检查短信配置异常", e);
            return Result.failure(500, "检查短信配置异常: " + e.getMessage());
        }
    }

    // 测试短信发送接口 (仅用于调试)
    @CrossOrigin
    @PostMapping("/test-sms")
    public Result testSms(@RequestBody Map<String, String> body) {
        try {
            String phone = body.get("phone");
            String code = body.get("code");
            
            if (phone == null || phone.isEmpty()) {
                return Result.failure(400, "手机号不能为空");
            }
            
            if (code == null || code.isEmpty()) {
                code = "123456"; // 默认测试验证码
            }

            log.info("测试短信发送 - 手机号: {}, 验证码: {}", phone, code);
            
            // 验证手机号格式
            if (!smsService.isValidPhoneNumber(phone)) {
                return Result.failure(400, "手机号格式不正确");
            }

            // 直接调用短信服务
            boolean sendSuccess = smsService.sendVerificationCode(phone, code);
            
            if (sendSuccess) {
                return Result.success("测试短信发送成功");
            } else {
                return Result.failure(500, "测试短信发送失败");
            }
        } catch (Exception e) {
            log.error("测试短信发送异常", e);
            return Result.failure(500, "测试短信发送异常: " + e.getMessage());
        }
    }
}