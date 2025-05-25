package com.health.healthplatform.controller;

import com.health.healthplatform.entity.Article;
import com.health.healthplatform.service.UserArticleService;
import com.health.healthplatform.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/user/articles")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:8088"}, allowCredentials = "true")
public class UserArticleController {

    @Resource
    private UserArticleService userArticleService;

    @GetMapping("/my/{userId}")
    public Result getUserArticles(@PathVariable Integer userId) {
        try {
            if (userId == null) {
                return Result.failure(401, "请先登录");
            }
            Map<String, Object> articles = userArticleService.getUserArticles(userId);
            return Result.success(articles);
        } catch (Exception e) {
            return Result.failure(500, "获取文章列表失败: " + e.getMessage());
        }
    }

    @PutMapping("/{userId}/{id}/publish")
    public Result publishArticle(@PathVariable Integer userId, @PathVariable Long id) {
        try {
            if (userId == null) {
                return Result.failure(401, "请先登录");
            }
            // 验证文章所有权
            Article article = userArticleService.getUserArticle(id);
            if (article == null) {
                return Result.failure(404, "文章不存在");
            }
            if (!article.getUserId().equals(userId)) {
                return Result.failure(403, "无权限操作此文章");
            }

            Article publishedArticle = userArticleService.publishArticle(id, LocalDateTime.now());
            return Result.success(publishedArticle);
        } catch (Exception e) {
            return Result.failure(500, "发布文章失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{userId}/{id}")
    public Result deleteArticle(@PathVariable Integer userId, @PathVariable Long id) {
        try {
            if (userId == null) {
                return Result.failure(401, "请先登录");
            }

            // 验证文章所有权
            Article article = userArticleService.getUserArticle(id);
            if (article == null) {
                return Result.failure(404, "文章不存在");
            }
            if (!article.getUserId().equals(userId)) {
                return Result.failure(403, "无权限删除此文章");
            }

            userArticleService.deleteArticle(id);
            return Result.success(null);
        } catch (Exception e) {
            return Result.failure(500, "删除文章失败: " + e.getMessage());
        }
    }
}