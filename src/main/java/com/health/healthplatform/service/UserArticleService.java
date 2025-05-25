package com.health.healthplatform.service;

import com.health.healthplatform.entity.Article;
import com.health.healthplatform.mapper.ArticleMapper;
import com.health.healthplatform.mapper.UserArticleMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.annotation.Resource;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class UserArticleService {

    @Resource
    private UserArticleMapper userArticleMapper;

    @Resource
    private ArticleMapper articleMapper;

    public Map<String, Object> getUserArticles(Integer userId) {
        if (userId == null) {
            throw new RuntimeException("用户ID不能为空");
        }

        Map<String, Object> result = new HashMap<>();
        try {
            // 获取已发布的文章
            List<Article> publishedArticles = userArticleMapper.selectUserArticles(userId, 1);
            // 获取草稿箱的文章
            List<Article> draftArticles = userArticleMapper.selectUserArticles(userId, 0);

            result.put("published", publishedArticles);
            result.put("drafts", draftArticles);

            return result;
        } catch (Exception e) {
            log.error("获取用户文章失败", e);
            throw new RuntimeException("获取用户文章失败: " + e.getMessage());
        }
    }

    public Article getUserArticle(Long id) {
        try {
            if (id == null) {
                throw new RuntimeException("文章ID不能为空");
            }
            return articleMapper.selectById(id);
        } catch (Exception e) {
            log.error("获取文章详情失败", e);
            throw new RuntimeException("获取文章详情失败: " + e.getMessage());
        }
    }

    @Transactional
    public Article publishArticle(Long id, LocalDateTime publishTime) {
        try {
            userArticleMapper.updateArticleStatus(id, 1, publishTime);
            // 可以返回更新后的文章信息
            return articleMapper.selectById(id);
        } catch (Exception e) {
            log.error("发布文章失败", e);
            throw new RuntimeException("发布文章失败: " + e.getMessage());
        }
    }

    @Transactional
    public void deleteArticle(Long id) {
        try {
            // 按顺序删除相关数据以避免外键约束问题
            userArticleMapper.deleteArticleComments(id);
            userArticleMapper.deleteArticleLikes(id);
            userArticleMapper.deleteArticleFavorites(id);
            articleMapper.deleteArticleTags(id);
            userArticleMapper.deleteArticle(id);
        } catch (Exception e) {
            log.error("删除文章失败", e);
            throw new RuntimeException("删除文章失败: " + e.getMessage());
        }
    }
}