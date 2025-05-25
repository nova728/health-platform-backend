package com.health.healthplatform.mapper;

import com.health.healthplatform.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {
    public User selectById(Integer integer);
    // 通过用户名和密码查找对应用户
    public User findUserByNameAndPwd(User user);
    // 通过用户名查找用户
    public User findUserByName(User user);
    // 添加用户
    public void addUser(User user);
    //更新头像
    public void updateAvatar(@Param("userId") Integer userId, @Param("avatarUrl") String avatarUrl);
    //更新信息
    public void updateUser(User user);

    @Select("SELECT COUNT(*) FROM user")
    int countUsers();
}