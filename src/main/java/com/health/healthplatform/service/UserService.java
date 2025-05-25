package com.health.healthplatform.service;

import com.health.healthplatform.entity.User;
import org.apache.ibatis.annotations.Param;

public interface UserService {
    // 通过用户名和密码查找对应id
    public User findUserByNameAndPwd(User user);
    // 通过用户名查找用户
    public User findUserByName(User user);
    // 添加用户
    public void addUser(User user);

    public String login(User user);

    public User selectById(Integer integer);

    public void updateAvatar(@Param("userId") Integer userId, @Param("avatarUrl") String avatarUrl);


}