package com.health.healthplatform.controller;

import com.health.healthplatform.entity.Article;
import com.health.healthplatform.service.ArticleService;
import com.health.healthplatform.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/articles/list")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:8088"}, allowCredentials = "true")
public class ArticleFeatureController {

    @Resource
    private ArticleService articleService;

    @GetMapping("/hot")
    public Result getHotArticles() {
        try {
            List<Article> hotArticles = articleService.getHotArticles();
            if (hotArticles.isEmpty()) {
                return Result.success(new ArrayList<>());
            }
            return Result.success(hotArticles);
        } catch (Exception e) {
            log.error("获取热门文章失败", e);
            return Result.failure(500, e.getMessage());
        }
    }

    @GetMapping("/recommended/{userId}")
    public Result getRecommendedArticles(@PathVariable Integer userId) {
        try {
            if (userId == null) {
                return Result.failure(401, "请先登录");
            }
            List<Article> recommendedArticles = articleService.getRecommendedArticles(userId);
            return Result.success(recommendedArticles);
        } catch (Exception e) {
            return Result.failure(500, "获取推荐文章失败: " + e.getMessage());
        }
    }
}