package com.health.healthplatform.service;

import com.health.healthplatform.entity.Article;
import com.health.healthplatform.entity.Category;
import com.health.healthplatform.entity.Comment;
import com.health.healthplatform.entity.User;
import com.health.healthplatform.mapper.ArticleMapper;
import com.health.healthplatform.mapper.CategoryMapper;
import com.health.healthplatform.mapper.TagMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.annotation.Resource;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

@Slf4j
@Service
public class  ArticleService {

    @Resource
    private ArticleMapper articleMapper;

    @Resource
    private TagMapper tagMapper;

    @Resource
    CategoryMapper categoryMapper;

    @Resource
    UserService userService;

    @Resource
    private NotificationService notificationService;

    @Transactional
    public Article createArticle(Article article, Integer userId) {
        System.out.println("Starting createArticle in service");
        try {
            // 检查标签是否为空
            if (article.getTags() == null) {
                System.out.println("Tags is null, initializing empty list");
                article.setTags(new ArrayList<>());
            }

            // 设置基本信息
            article.setUserId(userId);
            LocalDateTime now = LocalDateTime.now();
            article.setCreatedAt(now);
            article.setUpdatedAt(now);

            if (article.getStatus() == 1 && article.getPublishTime() == null) {
                System.out.println("Setting publish time to now");
                article.setPublishTime(now);
            }

            // 插入文章并获取生成的ID
            articleMapper.insert(article);
            Long articleId = article.getId();
            System.out.println("Article inserted with ID: " + articleId);

            if (articleId == null) {
                throw new RuntimeException("Failed to get generated article ID");
            }

            // 处理标签
            for (String tagName : article.getTags()) {
                if (tagName != null && !tagName.trim().isEmpty()) {
                    Long tagId = tagMapper.getOrCreateTag(tagName.trim());
                    articleMapper.insertArticleTag(articleId, tagId);
                }
            }

            // 重要：直接使用返回的article对象，确保ID已经设置
            return articleMapper.selectById(articleId);

        } catch (Exception e) {
            System.err.println("Error in createArticle service: " + e);
            e.printStackTrace();
            throw e;
        }
    }

    @Transactional
    public Article updateArticle(Article article, Integer userId) {
        // 验证文章所有权
        Article existingArticle = articleMapper.selectById(article.getId());
        if (existingArticle == null) {
            throw new RuntimeException("文章不存在");
        }

        if (!existingArticle.getUserId().equals(userId)) {
            throw new RuntimeException("无权限修改此文章");
        }

        // 更新文章
        articleMapper.update(article);

        // 更新标签
        if (article.getTags() != null) {
            articleMapper.deleteArticleTags(article.getId());
            if (!article.getTags().isEmpty()) {
                for (String tagName : article.getTags()) {
                    Long tagId = tagMapper.getOrCreateTag(tagName);
                    articleMapper.insertArticleTag(article.getId(), tagId);
                }
            }
        }

        return getArticle(article.getId(), userId);
    }

    public Article getArticle(Long id, Integer userId) {
        if (id == null) {
            throw new RuntimeException("文章ID不能为空");
        }
        Article article = articleMapper.selectById(id);
        System.out.println(article);
        if (article == null) {
            System.out.println(id);
            throw new RuntimeException("文章不存在");
        }

        // 增加浏览量
        articleMapper.incrementViewCount(id);

        // 检查用户是否点赞和收藏
        if (userId != null) {
            article.setIsLiked(articleMapper.checkUserLiked(id, userId));
            article.setIsFavorited(articleMapper.checkUserFavorited(id, userId));
        }

        return article;
    }

    public Map<String, Object> getArticles(Integer categoryId, String search, int page, int size, Integer userId) {
        Map<String, Object> result = new HashMap<>();

        // 计算偏移量
        int offset = (page - 1) * size;

        // 获取文章列表
        List<Article> articles = articleMapper.selectArticles(categoryId, search, offset, size);

        // 检查用户是否点赞和收藏
        if (userId != null) {
            for (Article article : articles) {
                article.setIsLiked(articleMapper.checkUserLiked(article.getId(), userId));
                article.setIsFavorited(articleMapper.checkUserFavorited(article.getId(), userId));
            }
        }

        // 获取总数
        int total = articleMapper.countArticles(categoryId, search);

        result.put("articles", articles);
        result.put("total", total);

        return result;
    }

    @Transactional
    public Article likeArticle(Long articleId, Integer userId) {
        if (userId == null) {
            throw new RuntimeException("请先登录");
        }

        if (articleMapper.checkUserLiked(articleId, userId)) {
            throw new RuntimeException("已经点赞过了");
        }

        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new RuntimeException("文章不存在");
        }

        articleMapper.insertLike(articleId, userId);
        articleMapper.increaseLikeCount(articleId);

        // 发送点赞通知
        String message = String.format("%s 点赞了你的文章 《%s》",
                userService.selectById(userId).getUsername(),
                article.getTitle());
        notificationService.createNotification(
                article.getUserId(),  // 文章作者ID
                userId,              // 点赞者ID
                "like",             // 通知类型
                message,            // 通知内容
                articleId           // 文章ID
        );

        return getArticle(articleId, userId);
    }

    @Transactional
    public Article unlikeArticle(Long articleId, Integer userId) {
        if (userId == null) {
            throw new RuntimeException("请先登录");
        }

        if (!articleMapper.checkUserLiked(articleId, userId)) {
            throw new RuntimeException("尚未点赞");
        }

        articleMapper.deleteLike(articleId, userId);
        articleMapper.decreaseLikeCount(articleId);

        // 返回更新后的文章信息
        return getArticle(articleId, userId);
    }

    @Transactional
    public void favoriteArticle(Long articleId, Integer userId) {
        if (userId == null) {
            throw new RuntimeException("请先登录");
        }

        if (articleMapper.checkUserFavorited(articleId, userId)) {
            throw new RuntimeException("已经收藏过了");
        }

        articleMapper.insertFavorite(articleId, userId);
        articleMapper.increaseFavoriteCount(articleId); // Add this line
    }

    @Transactional
    public void unfavoriteArticle(Long articleId, Integer userId) {
        if (userId == null) {
            throw new RuntimeException("请先登录");
        }

        if (!articleMapper.checkUserFavorited(articleId, userId)) {
            throw new RuntimeException("尚未收藏");
        }

        articleMapper.deleteFavorite(articleId, userId);
        articleMapper.decreaseFavoriteCount(articleId); // Add this line
    }

    // 增加浏览量
    public void incrementViewCount(Long articleId) {
        articleMapper.incrementViewCount(articleId);
    }

    // ArticleService.java


    @Transactional
    public Comment createComment(Long articleId, Integer userId, String content, Long parentId, Integer replyToUserId) {
        try {
            Comment comment = new Comment();
            comment.setContent(content);
            comment.setArticleId(articleId);
            comment.setUserId(userId);
            comment.setParentId(parentId);
            comment.setReplyToUserId(replyToUserId);
            comment.setCreatedAt(LocalDateTime.now());
            comment.setLikeCount(0);
            comment.setReplyCount(0);

            // 插入评论
            articleMapper.insertComment(comment);

            // 如果是回复评论，增加父评论的回复数
            if (parentId != null) {
                articleMapper.increaseReplyCount(parentId);
            }

            // 增加文章评论数
            articleMapper.increaseCommentCount(articleId);

            // 获取文章和评论者信息
            Article article = articleMapper.selectById(articleId);
            User commenter = userService.selectById(userId);

            // 发送评论通知
            if (!userId.equals(article.getUserId())) { // 不给自己发通知
                String message;
                if (parentId != null) {
                    // 如果是回复评论
                    Comment parentComment = articleMapper.selectCommentById(parentId);
                    message = String.format("%s 回复了你在文章《%s》中的评论",
                            commenter.getUsername(), article.getTitle());

                    // 通知评论作者
                    notificationService.createNotification(
                            replyToUserId != null ? replyToUserId : parentComment.getUserId(),
                            userId,
                            "comment",
                            message,
                            articleId
                    );
                } else {
                    // 如果是直接评论文章
                    message = String.format("%s 评论了你的文章《%s》",
                            commenter.getUsername(), article.getTitle());

                    // 通知文章作者
                    notificationService.createNotification(
                            article.getUserId(),
                            userId,
                            "comment",
                            message,
                            articleId
                    );
                }
            }

            // 返回完整的评论信息
            Comment fullComment = articleMapper.selectCommentById(comment.getId());
            System.out.println("评论创建成功: " + fullComment);
            return fullComment;

        } catch (Exception e) {
            System.err.println("创建评论失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("创建评论失败", e);
        }
    }


    public Map<String, Object> getComments(Long articleId, int page, int size) {
        Map<String, Object> result = new HashMap<>();

        // 获取所有评论
        List<Comment> allComments = articleMapper.selectAllComments(articleId);

        // 构建评论树结构
        Map<Long, Comment> commentMap = new HashMap<>();
        List<Comment> rootComments = new ArrayList<>();

        for (Comment comment : allComments) {
            commentMap.put(comment.getId(), comment);
            if (comment.getReplies() == null) {
                comment.setReplies(new ArrayList<>());
            }
        }

        for (Comment comment : allComments) {
            if (comment.getParentId() == null) {
                rootComments.add(comment);
            } else {
                Comment parentComment = commentMap.get(comment.getParentId());
                if (parentComment != null && parentComment.getReplies() != null) {
                    parentComment.getReplies().add(comment);
                }
            }
        }

        // 分页处理
        int start = (page - 1) * size;
        int end = Math.min(start + size, rootComments.size());
        List<Comment> pagedComments = start < rootComments.size() ?
                rootComments.subList(start, end) :
                new ArrayList<>();

        result.put("comments", pagedComments);
        result.put("total", rootComments.size());
        result.put("page", page);
        result.put("size", size);
        result.put("pages", (rootComments.size() + size - 1) / size);

        return result;
    }

    public List<Article> getHotArticles() {
        // 更新所有文章的热度分数
        try{
            List<Article> allArticles = articleMapper.selectAll();
            for (Article article : allArticles) {
                int hotScore = article.getViewCount() * 1 +
                        article.getLikeCount() * 3 +
                        article.getCommentCount() * 5;
                article.setHotScore(hotScore);
                article.setIsHot(hotScore > 1000);
                articleMapper.updateHotScore(article.getId());
            }

            // 获取热门文章
            return articleMapper.selectHotArticles(10);  // 返回前10篇热门文章
        }catch (Exception e) {
            log.error("获取热门文章失败", e);
            throw new RuntimeException("获取热门文章失败: " + e.getMessage());
        }
    }

    public List<Article> getRecommendedArticles(Integer userId) {
        // 基于用户阅读历史和标签推荐文章
        return articleMapper.selectRecommendedArticles(userId);
    }

    public Map<String, Object> getUserArticles(Integer userId) {
        if (userId == null) {
            throw new RuntimeException("用户ID不能为空");
        }

        Map<String, Object> result = new HashMap<>();
        try {
            // 获取已发布的文章
            List<Article> publishedArticles = articleMapper.selectUserArticles(userId, 1);
            // 获取草稿箱的文章
            List<Article> draftArticles = articleMapper.selectUserArticles(userId, 0);

            result.put("published", publishedArticles);
            result.put("drafts", draftArticles);

            return result;
        } catch (Exception e) {
            log.error("获取用户文章失败", e);
            throw new RuntimeException("获取用户文章失败: " + e.getMessage());
        }
    }

    // 获取用户收藏的文章列表
    public Map<String, Object> getUserFavorites(Integer userId, String search, int page, int size) {
        if (userId == null) {
            throw new RuntimeException("用户ID不能为空");
        }

        Map<String, Object> result = new HashMap<>();
        try {
            // 计算偏移量
            int offset = (page - 1) * size;

            // 获取收藏文章列表
            List<Article> articles = articleMapper.selectUserFavorites(userId, search, offset, size);

            // 检查文章的点赞和收藏状态
            for (Article article : articles) {
                article.setIsLiked(articleMapper.checkUserLiked(article.getId(), userId));
                article.setIsFavorited(true); // 这是收藏列表，所以都是已收藏的
            }

            // 获取总数
            int total = articleMapper.countUserFavorites(userId, search);

            result.put("articles", articles);
            result.put("total", total);

            return result;
        } catch (Exception e) {
            log.error("获取用户收藏文章失败", e);
            throw new RuntimeException("获取用户收藏文章失败: " + e.getMessage());
        }
    }
}