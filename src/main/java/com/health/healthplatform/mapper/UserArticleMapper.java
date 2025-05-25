package com.health.healthplatform.mapper;

import com.health.healthplatform.entity.Article;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface UserArticleMapper {
    @Select("SELECT * FROM articles WHERE user_id = #{userId} AND status = #{status}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "htmlContent", column = "html_content"),
            @Result(property = "coverImage", column = "cover_image"),
            @Result(property = "categoryId", column = "category_id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "allowComment", column = "allow_comment"),
            @Result(property = "viewCount", column = "view_count"),
            @Result(property = "likeCount", column = "like_count"),
            @Result(property = "commentCount", column = "comment_count"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at"),
            @Result(property = "publishTime", column = "publish_time"),
            @Result(property = "tags", column = "id",
                    javaType = List.class,
                    many = @Many(select = "com.health.healthplatform.mapper.ArticleMapper.selectArticleTags")),
            @Result(property = "author", column = "user_id",
                    one = @One(select = "com.health.healthplatform.mapper.UserMapper.selectById"))
    })
    List<Article> selectUserArticles(@Param("userId") Integer userId, @Param("status") Integer status);

    @Delete("DELETE FROM articles WHERE id = #{id}")
    void deleteArticle(Long id);

    @Update("UPDATE articles SET status = #{status}, publish_time = #{publishTime} WHERE id = #{id}")
    void updateArticleStatus(@Param("id") Long id, @Param("status") Integer status,
                             @Param("publishTime") LocalDateTime publishTime);

    @Delete("DELETE FROM likes WHERE article_id = #{articleId}")
    void deleteArticleLikes(Long articleId);

    @Delete("DELETE FROM favorites WHERE article_id = #{articleId}")
    void deleteArticleFavorites(Long articleId);

    @Delete("DELETE FROM comments WHERE article_id = #{articleId}")
    void deleteArticleComments(Long articleId);


}