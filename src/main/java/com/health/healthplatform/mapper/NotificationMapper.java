package com.health.healthplatform.mapper;

import com.health.healthplatform.entity.Notification;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface NotificationMapper {

    @Insert("INSERT INTO notifications(user_id, sender_id, type, message, article_id, is_read, create_time) " +
            "VALUES(#{userId}, #{senderId}, #{type}, #{message}, #{articleId}, #{isRead}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Notification notification);

    @Select("SELECT n.*, u.username as sender_name, u.avatar as sender_avatar " +
            "FROM notifications n " +
            "LEFT JOIN user u ON n.sender_id = u.id " +
            "WHERE n.user_id = #{userId} " +
            "ORDER BY n.create_time DESC")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "sender", column = "sender_id",
                    one = @One(select = "com.health.healthplatform.mapper.UserMapper.selectById"))
    })
    List<Notification> selectByUserId(Integer userId);

    @Update("UPDATE notifications SET is_read = true " +
            "WHERE user_id = #{userId} AND id = #{notificationId}")
    void markAsRead(@Param("userId") Integer userId, @Param("notificationId") Long notificationId);

    @Update("UPDATE notifications SET is_read = true " +
            "WHERE user_id = #{userId}")
    void markAllAsRead(Integer userId);

    @Select("SELECT COUNT(*) FROM notifications " +
            "WHERE user_id = #{userId} AND is_read = false")
    int countUnread(Integer userId);
}