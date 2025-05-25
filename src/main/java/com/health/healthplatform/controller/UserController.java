package com.health.healthplatform.controller;


import com.health.healthplatform.entity.User;
import com.health.healthplatform.result.Result;
import com.health.healthplatform.service.ArticleService;
import com.health.healthplatform.service.FileService;
import com.health.healthplatform.service.UserService;
import com.health.healthplatform.util.JWTUtils;
import com.health.healthplatform.mapper.UserMapper;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Resource
    UserService userService;

    @Resource
    private UserMapper userMapper;

    @Resource
    FileService fileService;

    @Resource
    private ArticleService articleService;

    @GetMapping("/count")
    public Result getUserCount() {
        try {
            int count = userMapper.countUsers();
            return Result.success(count);
        } catch (Exception e) {
            return Result.failure(500, "获取用户总数失败：" + e.getMessage());
        }
    }

    // 登录
    @CrossOrigin
    @PostMapping(value = "/login")
    public Result login(@ModelAttribute("user") User user) {
        String username = user.getUsername();
        String password = user.getPassword();
        System.out.println("Login received username: " + username);
        System.out.println("Login received password: " + password);
        User userCheck = new User();
        userCheck.setUsername(username);
        userCheck.setPassword(password);
        System.out.println(userCheck.getUsername() + " " + userCheck.getPassword());
        try {
            User findUser = userService.findUserByNameAndPwd(userCheck);
            if (findUser != null) {
                String token=JWTUtils.createToken(String.valueOf(user.getId()),3600000L); // 假设有这个方法
                findUser.setToken(token);
                System.out.println("Generated Token: " + token);
                return Result.success(findUser);
            } else {
                return Result.failure(401, "用户名或密码错误");
            }
        } catch (Exception e) {
            return Result.failure(500, "服务器异常");
        }
    }

    // 注册
    @CrossOrigin
    @PostMapping(value = "/register")
    public Result register(@ModelAttribute("user") User user) {
        User userCheck = new User();
        userCheck.setUsername(user.getUsername());
        userCheck.setPhone(user.getPhone());
        userCheck.setPassword(user.getPassword());
        if (userCheck.getUsername() == null || userCheck.getUsername().isEmpty()) {
            System.out.println("用户名不可为空");
            return Result.failure(201, "用户名不可为空");
        } else if (userCheck.getPassword() == null || userCheck.getPassword().isEmpty()) {
            System.out.println("密码不可为空");
            return Result.failure(201, "密码不可为空");
        } else if (userCheck.getPhone() == null || userCheck.getPhone().isEmpty()) {
            System.out.println("手机号不可为空");
            return Result.failure(201, "手机号不可为空");
        } else {
            System.out.println("Register received username: " + userCheck.getUsername());
            System.out.println("Register received password: " + userCheck.getPassword());
            System.out.println("Register received phone: " + userCheck.getPhone());
            try {
                // 先在数据库中查找是否已有用户名相同的用户
                User findUser = userService.findUserByName(userCheck);
                if (findUser != null) {
                    // 用户名已存在
                    return Result.failure(202, "用户名已存在!");
                } else {
                    // 新用户，数据库添加记录
                    userService.addUser(userCheck);
                    return Result.success(userCheck);
                }
            } catch (Exception e) {
                return Result.failure(500, "服务器异常");
//            }
            }
        }
    }

    //头像上传
    @CrossOrigin
    @PostMapping("/{id}/uploadAvatar")
    public Result uploadAvatar(@PathVariable Integer id, @RequestParam("file") MultipartFile file) {
        try {
            // 检查用户是否存在
            User user = userMapper.selectById(id);
            if (user == null) {
                return Result.failure(404, "用户不存在");
            }

            // 检查文件是否为空
            if (file.isEmpty()) {
                return Result.failure(400, "文件不能为空");
            }

            // 检查文件类型
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !(originalFilename.endsWith(".jpg") ||
                    originalFilename.endsWith(".jpeg") ||
                    originalFilename.endsWith(".png") ||
                    originalFilename.endsWith(".gif"))) {
                return Result.failure(400, "不支持的文件类型");
            }

            // 生成唯一的文件名
            String filename = "avatars/" + UUID.randomUUID().toString() +
                    originalFilename.substring(originalFilename.lastIndexOf("."));

            // 上传文件到OSS
            String fileUrl = fileService.uploadFile(filename, file.getInputStream());

            // 更新用户头像URL
            user.setAvatar(fileUrl);
            userMapper.updateAvatar(id, fileUrl);

            return Result.success(fileUrl);

        } catch (IOException e) {
            return Result.failure(500, "文件上传失败: " + e.getMessage());
        } catch (Exception e) {
            return Result.failure(500, "服务器错误: " + e.getMessage());
        }
    }

    //更新个人信息
    @CrossOrigin
    @PutMapping("/{id}/update")
    public Result updateUserInfo(@PathVariable Integer id, @RequestBody User user) {
        try {
            // 确保更新的是当前用户
            User existingUser = userMapper.selectById(id);
            if (existingUser == null) {
                return Result.failure(404, "用户不存在");
            }

            // 更新用户信息
            user.setId(id);
            userMapper.updateUser(user);

            // 返回更新后的用户信息
            User updatedUser = userMapper.selectById(id);
            return Result.success(updatedUser);
        } catch (Exception e) {
            return Result.failure(500, "更新失败: " + e.getMessage());
        }
    }

    private String saveFile(MultipartFile file) {
        // 保存文件到服务器并返回路径
        // 这里可以实现具体的文件保存逻辑
        return "/uploads/" + file.getOriginalFilename();
    }

    @GetMapping("/{userId}/favorites")
    public Result getUserFavorites(
            @PathVariable Integer userId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            if (userId == null) {
                return Result.failure(401, "请先登录");
            }

            Map<String, Object> result = articleService.getUserFavorites(userId, search, page, size);
            return Result.success(result);
        } catch (Exception e) {
            return Result.failure(500, "获取收藏列表失败: " + e.getMessage());
        }
    }

}