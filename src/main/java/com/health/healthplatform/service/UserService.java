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
    
    // 根据用户ID获取用户信息
    public User getById(Integer userId);

    public void updateAvatar(@Param("userId") Integer userId, @Param("avatarUrl") String avatarUrl);
    
    // 获取用户发表的文章数量
    public Integer getArticleCount(Integer userId);
      // 获取用户获得的总点赞数
    public Integer getTotalLikes(Integer userId);

}