package com.health.healthplatform.mapper;

import com.health.healthplatform.entity.Article;
import com.health.healthplatform.entity.Comment;
import org.apache.ibatis.annotations.*;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ArticleMapper {

    @Select("SELECT c.*, u.username, u.avatar " +
            "FROM comments c " +
            "LEFT JOIN user u ON c.user_id = u.id " +
            "WHERE c.article_id = #{articleId} " +
            "ORDER BY " +
            "CASE WHEN c.parent_id IS NULL THEN c.created_at ELSE " +
            "(SELECT created_at FROM comments WHERE id = c.parent_id) END DESC, " +
            "c.parent_id ASC, " +
            "c.created_at ASC")
    @Results({
            @Result(property = "user", column = "user_id",
                    one = @One(select = "com.health.healthplatform.mapper.UserMapper.selectById")),
            @Result(property = "replies", column = "id",
                    many = @Many(select = "selectCommentReplies"))
    })
    List<Comment> selectAllComments(@Param("articleId") Long articleId);

    @Insert("INSERT INTO articles(title, content, html_content, cover_image, category_id, user_id, " +
            "status, visibility, allow_comment, view_count, like_count, comment_count, " +
            "created_at, updated_at, publish_time) " +
            "VALUES(#{title}, #{content}, #{htmlContent}, #{coverImage}, #{categoryId}, #{userId}, " +
            "#{status}, #{visibility}, #{allowComment}, #{viewCount}, #{likeCount}, #{commentCount}, " +
            "#{createdAt}, #{updatedAt}, #{publishTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")  // 添加 keyColumn
    void insert(Article article);

    @Update("UPDATE articles SET title=#{title}, content=#{content}, html_content=#{htmlContent}, " +
            "cover_image=#{coverImage}, category_id=#{categoryId}, status=#{status}, " +
            "visibility=#{visibility}, allow_comment=#{allowComment}, publish_time=#{publishTime} " +
            "WHERE id=#{id}")
    void update(Article article);

    @Select("SELECT * FROM articles WHERE id=#{id}")
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
                    many = @Many(select = "selectArticleTags")),
            @Result(property = "author", column = "user_id",
                    one = @One(select = "com.health.healthplatform.mapper.UserMapper.selectById")),
    })
    Article selectById(Long id);

    @Select("SELECT a.*, u.username as author_name, u.avatar as author_avatar " +
            "FROM articles a " +
            "LEFT JOIN user u ON a.user_id = u.id " +
            "WHERE a.status = 1 " +
            "AND IF(#{categoryId} IS NOT NULL, a.category_id = #{categoryId}, 1=1) " +
            "AND IF(#{search} IS NOT NULL AND #{search} != '', " +
            "(a.title LIKE CONCAT('%', #{search}, '%') OR a.content LIKE CONCAT('%', #{search}, '%')), 1=1) " +
            "ORDER BY a.created_at DESC " +
            "LIMIT #{offset}, #{size}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "tags", column = "id",
                    many = @Many(select = "selectArticleTags")),
            @Result(property = "author", column = "user_id",
                    one = @One(select = "com.health.healthplatform.mapper.UserMapper.selectById"))
    })
    List<Article> selectArticles(
            @Param("categoryId") Integer categoryId,
            @Param("search") String search,
            @Param("offset") int offset,
            @Param("size") int size
    );

    @Select("SELECT COUNT(*) FROM articles " +
            "WHERE status = 1 " +
            "AND IF(#{categoryId} IS NOT NULL, category_id = #{categoryId}, 1=1) " +
            "AND IF(#{search} IS NOT NULL AND #{search} != '', " +
            "(title LIKE CONCAT('%', #{search}, '%') OR content LIKE CONCAT('%', #{search}, '%')), 1=1)")
    int countArticles(@Param("categoryId") Integer categoryId, @Param("search") String search);


    @Select("SELECT name FROM tags t " +
            "INNER JOIN article_tags at ON t.id = at.tag_id " +
            "WHERE at.article_id = #{articleId}")
    List<String> selectArticleTags(Long articleId);

    @Insert("INSERT INTO article_tags(article_id, tag_id) "+
            "SELECT #{articleId}, #{tagId} FROM DUAL "+
            "WHERE NOT EXISTS ("+
            "    SELECT 1 FROM article_tags "+
            "    WHERE article_id = #{articleId} AND tag_id = #{tagId}"+
            ")")
    void insertArticleTag(@Param("articleId") Long articleId, @Param("tagId") Long tagId);

    @Update("UPDATE articles SET view_count = view_count + 1 WHERE id = #{id}")
    void incrementViewCount(Long id);

    @Select("SELECT COUNT(*) > 0 FROM likes WHERE article_id = #{articleId} AND user_id = #{userId}")
    boolean checkUserLiked(@Param("articleId") Long articleId, @Param("userId") Integer userId);

    @Select("SELECT COUNT(*) > 0 FROM favorites WHERE article_id = #{articleId} AND user_id = #{userId}")
    boolean checkUserFavorited(@Param("articleId") Long articleId, @Param("userId") Integer userId);

    @Insert("INSERT INTO likes(article_id, user_id) VALUES(#{articleId}, #{userId})")
    void insertLike(@Param("articleId") Long articleId, @Param("userId") Integer userId);

    @Delete("DELETE FROM likes WHERE article_id = #{articleId} AND user_id = #{userId}")
    void deleteLike(@Param("articleId") Long articleId, @Param("userId") Integer userId);

    @Update("UPDATE articles SET like_count = like_count + 1 WHERE id = #{id}")
    void increaseLikeCount(Long id);

    @Update("UPDATE articles SET like_count = like_count - 1 WHERE id = #{id}")
    void decreaseLikeCount(Long id);

    @Insert("INSERT INTO favorites(article_id, user_id) VALUES(#{articleId}, #{userId})")
    void insertFavorite(@Param("articleId") Long articleId, @Param("userId") Integer userId);

    @Delete("DELETE FROM favorites WHERE article_id = #{articleId} AND user_id = #{userId}")
    void deleteFavorite(@Param("articleId") Long articleId, @Param("userId") Integer userId);

    @Insert("INSERT INTO comments(content, article_id, user_id, parent_id, reply_to_user_id, created_at, like_count, reply_count) " +
            "VALUES(#{content}, #{articleId}, #{userId}, #{parentId}, #{replyToUserId}, #{createdAt}, #{likeCount}, #{replyCount})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertComment(Comment comment);

    @Select("SELECT c.*, u.username, u.avatar, " +
            "ru.id as reply_to_user_id, ru.username as reply_to_username, ru.avatar as reply_to_avatar " +
            "FROM comments c " +
            "LEFT JOIN user u ON c.user_id = u.id " +
            "LEFT JOIN user ru ON c.reply_to_user_id = ru.id " +
            "WHERE c.id = #{id}")
    @Results({
            @Result(property = "user", column = "user_id",
                    one = @One(select = "com.health.healthplatform.mapper.UserMapper.selectById")),
            @Result(property = "replyToUser", column = "reply_to_user_id",
                    one = @One(select = "com.health.healthplatform.mapper.UserMapper.selectById"))
    })
    Comment selectCommentById(Long id);

    @Select("SELECT c.*, u.username, u.avatar, " +
            "ru.id as reply_to_user_id, ru.username as reply_to_username, ru.avatar as reply_to_avatar " +
            "FROM comments c " +
            "LEFT JOIN user u ON c.user_id = u.id " +
            "LEFT JOIN user ru ON c.reply_to_user_id = ru.id " +
            "WHERE c.article_id = #{articleId} AND c.parent_id IS NULL " +
            "ORDER BY c.created_at DESC LIMIT #{offset}, #{size}")
    @Results({
            @Result(property = "user", column = "user_id",
                    one = @One(select = "com.health.healthplatform.mapper.UserMapper.selectById")),
            @Result(property = "replyToUser", column = "reply_to_user_id",
                    one = @One(select = "com.health.healthplatform.mapper.UserMapper.selectById")),
            @Result(property = "replies", column = "id",
                    many = @Many(select = "selectCommentReplies"))
    })
    List<Comment> selectComments(@Param("articleId") Long articleId, @Param("offset") int offset, @Param("size") int size);

    @Select("SELECT COUNT(*) FROM comments WHERE article_id = #{articleId} AND parent_id IS NULL")
    int countComments(Long articleId);

    @Update("UPDATE articles SET comment_count = comment_count + 1 WHERE id = #{id}")
    void increaseCommentCount(Long id);

    @Select("SELECT c.*, u.username, u.avatar, " +
            "ru.id as reply_to_user_id, ru.username as reply_to_username, ru.avatar as reply_to_avatar " +
            "FROM comments c " +
            "LEFT JOIN user u ON c.user_id = u.id " +
            "LEFT JOIN user ru ON c.reply_to_user_id = ru.id " +
            "WHERE c.parent_id = #{parentId} " +
            "ORDER BY c.created_at ASC")
    @Results({
            @Result(property = "user", column = "user_id",
                    one = @One(select = "com.health.healthplatform.mapper.UserMapper.selectById")),
            @Result(property = "replyToUser", column = "reply_to_user_id",
                    one = @One(select = "com.health.healthplatform.mapper.UserMapper.selectById"))
    })
    List<Comment> selectCommentReplies(Long parentId);

    @Update("UPDATE comments SET reply_count = reply_count + 1 WHERE id = #{id}")
    void increaseReplyCount(Long id);

    @Update("UPDATE articles SET favorite_count = favorite_count + 1 WHERE id = #{id}")
    void increaseFavoriteCount(Long id);

    @Update("UPDATE articles SET favorite_count = favorite_count - 1 WHERE id = #{id}")
    void decreaseFavoriteCount(Long id);

    @Select("SELECT a.*, u.username as author_name, u.avatar as author_avatar " +
            "FROM articles a " +
            "LEFT JOIN user u ON a.user_id = u.id " +
            "WHERE a.status = 1 " +  // 已发布的文章
            "AND a.publish_time >= DATE_SUB(NOW(), INTERVAL 7 DAY) " +  // 最近7天发布的
            "ORDER BY a.hot_score DESC " +  // 按积分排序
            "LIMIT #{limit}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "title", column = "title"),
            @Result(property = "content", column = "content"),
            @Result(property = "coverImage", column = "cover_image"),
            @Result(property = "viewCount", column = "view_count"),
            @Result(property = "likeCount", column = "like_count"),
            @Result(property = "commentCount", column = "comment_count"),
            @Result(property = "favoriteCount", column = "favorite_count"),
            @Result(property = "pointsAwarded", column = "points_awarded"),
            @Result(property = "author", column = "user_id",
                    one = @One(select = "com.health.healthplatform.mapper.UserMapper.selectById"))
    })
    List<Article> selectHotArticles(@Param("limit") int limit);

    @Select("SELECT DISTINCT a.* FROM articles a " +
            "LEFT JOIN article_tags at ON a.id = at.article_id " +
            "LEFT JOIN user_read_history urh ON a.id = urh.article_id " +
            "WHERE a.status = 1 " +
            "AND (at.tag_id IN (SELECT tag_id FROM article_tags WHERE article_id IN " +
            "   (SELECT article_id FROM user_read_history WHERE user_id = #{userId})) " +
            "OR a.category_id IN (SELECT DISTINCT category_id FROM articles " +
            "   WHERE id IN (SELECT article_id FROM user_read_history WHERE user_id = #{userId}))) " +
            "ORDER BY a.hot_score DESC LIMIT 10")
    List<Article> selectRecommendedArticles(@Param("userId") Integer userId);

    @Update("UPDATE articles SET hot_score = " +
            "(view_count + like_count * 3 + comment_count * 5) WHERE id = #{id}")
    void updateHotScore(Long id);

    @Select("SELECT a.*, u.username as author_name, u.avatar as author_avatar " +
            "FROM articles a " +
            "LEFT JOIN user u ON a.user_id = u.id " +
            "WHERE a.status = 1 " +
            "ORDER BY a.created_at DESC")
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
                    many = @Many(select = "selectArticleTags")),
            @Result(property = "author", column = "user_id",
                    one = @One(select = "com.health.healthplatform.mapper.UserMapper.selectById"))
    })
    List<Article> selectAll();

    @Select("SELECT * FROM articles WHERE user_id = #{userId} AND status = #{status}")
    @Results({
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
                    many = @Many(select = "selectArticleTags")),
            @Result(property = "author", column = "user_id",
                    one = @One(select = "com.health.healthplatform.mapper.UserMapper.selectById"))
    })
    List<Article> selectUserArticles(@Param("userId") Integer userId, @Param("status") Integer status);

    // 查找草稿
    // 修改 findDraft 方法，确保返回所有字段，特别是 id
    @Select("SELECT id, title, content, html_content, cover_image, category_id, user_id, " +
            "status, visibility, allow_comment, view_count, like_count, comment_count, " +
            "favorite_count, points_awarded, hot_score, created_at, updated_at, publish_time " +
            "FROM articles " +
            "WHERE user_id = #{userId} AND status = 0 " +
            "AND title = #{title} AND content = #{content}")
    @Results({
            @Result(property = "id", column = "id"),  // 显式映射 id
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
                    many = @Many(select = "selectArticleTags"))
    })
    Article findDraft(@Param("userId") Integer userId, @Param("title") String title,
                      @Param("content") String content);

    // 修改删除方法，添加错误处理
    @Delete("DELETE FROM article_tags WHERE article_id = #{articleId}")
    int deleteArticleTags(Long articleId);

    @Delete("DELETE FROM articles WHERE id = #{id}")
    int deleteArticle(Long id);

    // 获取用户收藏的文章列表
    @Select("SELECT a.*, u.username as author_name, u.avatar as author_avatar " +
            "FROM articles a " +
            "INNER JOIN favorites f ON a.id = f.article_id " +
            "LEFT JOIN user u ON a.user_id = u.id " +
            "WHERE f.user_id = #{userId} " +
            "AND IF(#{search} IS NOT NULL AND #{search} != '', " +
            "(a.title LIKE CONCAT('%', #{search}, '%') OR a.content LIKE CONCAT('%', #{search}, '%')), 1=1) " +
            "ORDER BY f.created_at DESC " +
            "LIMIT #{offset}, #{size}")
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
                    many = @Many(select = "selectArticleTags")),
            @Result(property = "author", column = "user_id",
                    one = @One(select = "com.health.healthplatform.mapper.UserMapper.selectById"))
    })
    List<Article> selectUserFavorites(@Param("userId") Integer userId,
                                      @Param("search") String search,
                                      @Param("offset") int offset,
                                      @Param("size") int size);

    // 获取用户收藏文章的总数
    @Select("SELECT COUNT(*) FROM favorites f " +
            "INNER JOIN articles a ON f.article_id = a.id " +
            "WHERE f.user_id = #{userId} " +
            "AND IF(#{search} IS NOT NULL AND #{search} != '', " +
            "(a.title LIKE CONCAT('%', #{search}, '%') OR a.content LIKE CONCAT('%', #{search}, '%')), 1=1)")
    int countUserFavorites(@Param("userId") Integer userId, @Param("search") String search);

    @Select("SELECT COUNT(*) FROM articles " +
            "WHERE status = 1 AND publish_time >= #{date}")
    int countArticlesByDate(@Param("date") LocalDateTime date);

}