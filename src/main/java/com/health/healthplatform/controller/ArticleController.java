package com.health.healthplatform.controller;

import com.health.healthplatform.entity.Article;
import com.health.healthplatform.entity.Comment;
import com.health.healthplatform.mapper.ArticleMapper;
import com.health.healthplatform.service.ArticleService;
import com.health.healthplatform.service.FileService;
import com.health.healthplatform.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/articles")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:8088"}, allowCredentials = "true")
public class ArticleController {

    @Resource
    private ArticleService articleService;

    @Resource
    private ArticleMapper articleMapper;

    @Autowired
    private FileService fileService;

    // 创建文章
    @PostMapping("/{userId}")
    public Result createArticle(@PathVariable Integer userId, @RequestBody Article article) {
        try {
            // 打印接收到的数据
            //System.out.println("Received userId: " + userId);
            // System.out.println("Received article data: " + article);

            if (userId == null) {
                return Result.failure(401, "请先登录");
            }

            // 验证字段
            if (article.getTitle() == null || article.getTitle().trim().isEmpty()) {
                return Result.failure(400, "文章标题不能为空");
            }

            System.out.println("Article title validation passed");

//            // 打印关键字段
//            System.out.println("Title: " + article.getTitle());
//            System.out.println("Content: " + article.getContent());
//            System.out.println("CategoryId: " + article.getCategoryId());
//            System.out.println("Tags: " + article.getTags());
//            System.out.println("PublishTime: " + article.getPublishTime());

            // 设置默认值
            article.setUserId(userId);
            article.setStatus(article.getStatus() == null ? 0 : article.getStatus());
            article.setVisibility(article.getVisibility() == null ? "public" : article.getVisibility());
            article.setAllowComment(article.getAllowComment() == null ? true : article.getAllowComment());
            article.setViewCount(0);
            article.setLikeCount(0);
            article.setCommentCount(0);

            System.out.println("Default values set");

            try {
                Article savedArticle = articleService.createArticle(article, userId);
                System.out.println("Article saved successfully: " + savedArticle);
                return Result.success(savedArticle);
            } catch (Exception e) {
                System.err.println("Error in articleService.createArticle: " + e);
                e.printStackTrace();
                throw e;
            }
        } catch (Exception e) {
            System.err.println("Error in createArticle endpoint: " + e);
            e.printStackTrace();
            return Result.failure(500, "创建文章失败: " + e.getMessage());
        }
    }

    // 更新文章
    @PutMapping("/{userId}/{id}")
    public Result updateArticle(@PathVariable Integer userId, @PathVariable Long id, @RequestBody Article article) {
        try {
            if (userId == null) {
                return Result.failure(401, "请先登录");
            }

            article.setId(id);
            Article updatedArticle = articleService.updateArticle(article, userId);
            return Result.success(updatedArticle);
        } catch (Exception e) {
            return Result.failure(500, "更新文章失败: " + e.getMessage());
        }
    }

    // 获取文章详情
    @GetMapping("/{userId}/{id}")
    public Result getArticle(@PathVariable Integer userId, @PathVariable Long id) {
        try {
            Article article = articleService.getArticle(id, userId);
            return Result.success(article);
        } catch (Exception e) {
            return Result.failure(500, "获取文章失败: " + e.getMessage());
        }
    }

    // 获取文章列表
    @GetMapping("/{userId}")
    public Result getArticles(
            @PathVariable Integer userId,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Map<String, Object> result = articleService.getArticles(categoryId, search, page, size, userId);
            return Result.success(result);
        } catch (Exception e) {
            return Result.failure(500, "获取文章列表失败: " + e.getMessage());
        }
    }

    // 上传图片
    @PostMapping("/{userId}/upload-image")
    public Result uploadImage(@PathVariable Integer userId, @RequestParam("file") MultipartFile file) {
        try {
            if (userId == null) {
                return Result.failure(401, "请先登录");
            }

            if (file.isEmpty()) {
                return Result.failure(400, "文件为空");
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return Result.failure(400, "只支持上传图片文件");
            }

            // 限制文件大小（例如：2MB）
            long maxSize = 2 * 1024 * 1024; // 2MB in bytes
            if (file.getSize() > maxSize) {
                return Result.failure(400, "图片大小不能超过2MB");
            }

            // 生成文件名
            String originalFilename = file.getOriginalFilename();
            String filename = UUID.randomUUID().toString() + originalFilename;

            // 直接使用 FileService 的 uploadFile 方法
            String url = fileService.uploadFile(filename, file.getInputStream());

            // 返回正确格式的响应数据
            return Result.success(Map.of("url", url));

        } catch (IOException e) {
            return Result.failure(500, "图片上传失败: " + e.getMessage());
        }
    }

    @PostMapping("/{articleId}/{userId}/like")
    public Result likeArticle(@PathVariable Long articleId, @PathVariable Integer userId) {
        try {
            if (userId == null) {
                return Result.failure(401, "请先登录");
            }

            Article article = articleService.likeArticle(articleId, userId);
            return Result.success(article); // 返回更新后的文章信息
        } catch (Exception e) {
            return Result.failure(500, "点赞失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{articleId}/{userId}/like")
    public Result unlikeArticle(@PathVariable Long articleId, @PathVariable Integer userId) {
        try {
            if (userId == null) {
                return Result.failure(401, "请先登录");
            }

            Article article = articleService.unlikeArticle(articleId, userId);
            return Result.success(article); // 返回更新后的文章信息
        } catch (Exception e) {
            return Result.failure(500, "取消点赞失败: " + e.getMessage());
        }
    }

    // 收藏文章
    @PostMapping("/{userId}/{id}/favorite")
    public Result favoriteArticle(@PathVariable Integer userId,@PathVariable Long id, HttpSession session) {
        try {
            if (userId == null) {
                return Result.failure(401, "请先登录");
            }

            articleService.favoriteArticle(id, userId);
            return Result.success(null);
        } catch (Exception e) {
            return Result.failure(500, "收藏失败: " + e.getMessage());
        }
    }

    // 取消收藏
    @DeleteMapping("/{userId}/{id}/favorite")
    public Result unfavoriteArticle(@PathVariable Integer userId,@PathVariable Long id, HttpSession session) {
        try {
            if (userId == null) {
                return Result.failure(401, "请先登录");
            }

            articleService.unfavoriteArticle(id, userId);
            return Result.success(null);
        } catch (Exception e) {
            return Result.failure(500, "取消收藏失败: " + e.getMessage());
        }
    }

    // 增加浏览量
    @PostMapping("/{userId}/{id}/view")
    public Result incrementViewCount(@PathVariable Integer userId, @PathVariable Long id) {
        try {
            articleService.incrementViewCount(id);
            return Result.success(null);
        } catch (Exception e) {
            return Result.failure(500, "增加浏览量失败: " + e.getMessage());
        }
    }

    // 发表评论

    @PostMapping("/{userId}/{id}/comments")
    public Result createComment(
            @PathVariable Integer userId,
            @PathVariable Long id,
            @RequestBody Map<String, Object> commentData
    ) {
        try {
            if (userId == null) {
                return Result.failure(401, "请先登录");
            }

            String content = (String) commentData.get("content");
            if (content == null || content.trim().isEmpty()) {
                return Result.failure(400, "评论内容不能为空");
            }

            // 处理 parentId
            Long parentId = null;
            if (commentData.containsKey("parentId") && commentData.get("parentId") != null) {
                if (commentData.get("parentId") instanceof Integer) {
                    parentId = ((Integer) commentData.get("parentId")).longValue();
                } else if (commentData.get("parentId") instanceof Long) {
                    parentId = (Long) commentData.get("parentId");
                } else if (commentData.get("parentId") instanceof String) {
                    parentId = Long.parseLong((String) commentData.get("parentId"));
                }
            }

            // 处理 replyToUserId
            Integer replyToUserId = null;
            if (commentData.containsKey("replyToUserId") && commentData.get("replyToUserId") != null) {
                if (commentData.get("replyToUserId") instanceof Integer) {
                    replyToUserId = (Integer) commentData.get("replyToUserId");
                } else if (commentData.get("replyToUserId") instanceof String) {
                    replyToUserId = Integer.parseInt((String) commentData.get("replyToUserId"));
                }
            }

            // 打印调试信息
            System.out.println("Creating comment with parameters:");
            System.out.println("Article ID: " + id);
            System.out.println("User ID: " + userId);
            System.out.println("Content: " + content);
            System.out.println("Parent ID: " + parentId);
            System.out.println("Reply To User ID: " + replyToUserId);

            Comment comment = articleService.createComment(id, userId, content, parentId, replyToUserId);
            return Result.success(comment);

        } catch (Exception e) {
            e.printStackTrace();
            return Result.failure(500, "发表评论失败: " + e.getMessage());
        }
    }


    // 获取评论列表
    @GetMapping("/{userId}/{id}/comments")
    public Result getComments(
            @PathVariable Integer userId,
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            return Result.success(articleService.getComments(id, page, size));
        } catch (Exception e) {
            return Result.failure(500, "获取评论失败: " + e.getMessage());
        }
    }

    @Transactional
    @DeleteMapping("/{userId}/delete-draft")
    public Result deleteDraft(@PathVariable Integer userId, @RequestBody Map<String, Object> draftData) {
        try {
            if (userId == null) {
                return Result.failure(401, "请先登录");
            }

            String title = (String) draftData.get("title");
            String content = (String) draftData.get("content");

            if (title == null || content == null) {
                return Result.failure(400, "标题和内容不能为空");
            }

            // 查找草稿
            Article draft = articleMapper.findDraft(userId, title, content);
            System.out.println("查找到的草稿: " + draft);

            if (draft != null && draft.getId() != null) {
                // 删除标签关联
                int tagsDeleted = articleMapper.deleteArticleTags(draft.getId());

                // 删除文章
                int articlesDeleted = articleMapper.deleteArticle(draft.getId());

                if (articlesDeleted == 0) {
                    throw new RuntimeException("删除草稿失败，未找到匹配的记录");
                }
            } else {
                System.out.println("未找到匹配的草稿或草稿ID为空");
            }

            return Result.success(null);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.failure(500, "删除草稿失败: " + e.getMessage());
        }
    }

    @GetMapping("/count")
    public Result getArticlesCount() {
        try {
            // 获取已发布的文章总数
            int count = articleMapper.countArticles(null, null);
            return Result.success(count);
        } catch (Exception e) {
            return Result.failure(500, "获取文章总数失败：" + e.getMessage());
        }
    }

    @GetMapping("/count/today")
    public Result getTodayArticlesCount() {
        try {
            // 获取今日发布的文章数
            LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
            int count = articleMapper.countArticlesByDate(today);
            return Result.success(count);
        } catch (Exception e) {
            return Result.failure(500, "获取今日文章数失败：" + e.getMessage());
        }
    }
}