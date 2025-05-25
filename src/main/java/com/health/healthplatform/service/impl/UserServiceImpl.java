package com.health.healthplatform.service.impl;

import com.health.healthplatform.entity.User;
import com.health.healthplatform.mapper.UserMapper;
import com.health.healthplatform.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    @Resource
    private UserMapper userMapper;

    // 通过用户名和密码查找对应id
    @Resource
    public User findUserByNameAndPwd(User user){
        return userMapper.findUserByNameAndPwd(user);
    }

    // 通过用户名查找用户
    @Resource
    public User findUserByName(User user){
        return userMapper.findUserByName(user);
    }

    public User selectById(Integer integer){
        return userMapper.selectById(integer);
    }

    //     添加用户
    @Resource
    public void addUser(User user){
        if (user == null || user.getUsername() == null || user.getUsername().isEmpty() || user.getPassword() == null || user.getPassword().isEmpty() || user.getPhone()==null||user.getPhone().isEmpty()) {
            // 添加日志，避免报错并记录
            System.out.println("用户名/手机号/密码为空，跳过添加用户操作");
            return;
        }
        else userMapper.addUser(user);
    }

    @Resource
    public String login(User user) {
        User foundUser = this.findUserByNameAndPwd(user);
        if (foundUser != null) {
            // 生成Token的逻辑
            return foundUser.getToken();
        }
        return null;
    }

    public void updateAvatar(Integer userId, String avatarUrl) {
        userMapper.updateAvatar(userId, avatarUrl);
    }

}
