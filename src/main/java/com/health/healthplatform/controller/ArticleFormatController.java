package com.health.healthplatform.controller;

import com.health.healthplatform.entity.Article;
import com.health.healthplatform.service.ArticleService;
import com.health.healthplatform.result.Result;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/articles/format")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:8088"}, allowCredentials = "true")
public class ArticleFormatController {

    @Resource
    private ArticleService articleService;

    @PutMapping("/{userId}/{id}/style")
    public Result updateArticleStyle(
            @PathVariable Integer userId,
            @PathVariable Long id,
            @RequestBody Map<String, Object> formatData
    ) {
        try {
            if (userId == null) {
                return Result.failure(401, "请先登录");
            }

            String type = (String) formatData.get("type");
            String content = (String) formatData.get("content");
            Integer startPos = (Integer) formatData.get("startPos");
            Integer endPos = (Integer) formatData.get("endPos");

            if (type == null || content == null || startPos == null || endPos == null) {
                return Result.failure(400, "参数不完整");
            }

            // 获取当前文章
            Article article = articleService.getArticle(id, userId);
            if (article == null) {
                return Result.failure(404, "文章不存在");
            }

            // 处理文本格式化
            String newContent = formatContent(article.getContent(), type, startPos, endPos);
            String newHtmlContent = formatHtmlContent(article.getHtmlContent(), type, startPos, endPos);

            // 更新文章内容
            article.setContent(newContent);
            article.setHtmlContent(newHtmlContent);

            Article updatedArticle = articleService.updateArticle(article, userId);
            return Result.success(updatedArticle);
        } catch (Exception e) {
            return Result.failure(500, "更新格式失败: " + e.getMessage());
        }
    }

    @PutMapping("/{userId}/{id}/heading")
    public Result updateHeading(
            @PathVariable Integer userId,
            @PathVariable Long id,
            @RequestBody Map<String, Object> headingData
    ) {
        try {
            if (userId == null) {
                return Result.failure(401, "请先登录");
            }

            String level = (String) headingData.get("level");
            String content = (String) headingData.get("content");
            Integer startPos = (Integer) headingData.get("startPos");
            Integer endPos = (Integer) headingData.get("endPos");

            if (level == null || content == null || startPos == null || endPos == null) {
                return Result.failure(400, "参数不完整");
            }

            Article article = articleService.getArticle(id, userId);
            if (article == null) {
                return Result.failure(404, "文章不存在");
            }

            // 处理标题格式化
            String newContent = formatHeading(article.getContent(), level, startPos, endPos);
            String newHtmlContent = formatHtmlHeading(article.getHtmlContent(), level, startPos, endPos);

            article.setContent(newContent);
            article.setHtmlContent(newHtmlContent);

            Article updatedArticle = articleService.updateArticle(article, userId);
            return Result.success(updatedArticle);
        } catch (Exception e) {
            return Result.failure(500, "更新标题格式失败: " + e.getMessage());
        }
    }

    // 格式化文本内容的辅助方法
    private String formatContent(String content, String type, int startPos, int endPos) {
        if (startPos < 0 || endPos > content.length() || startPos > endPos) {
            throw new IllegalArgumentException("Invalid position parameters");
        }

        String selectedText = content.substring(startPos, endPos);
        String formattedText = "";

        switch (type) {
            case "bold":
                formattedText = "**" + selectedText + "**";
                break;
            case "italic":
                formattedText = "_" + selectedText + "_";
                break;
            case "underline":
                formattedText = "<u>" + selectedText + "</u>";
                break;
            default:
                throw new IllegalArgumentException("Unsupported format type: " + type);
        }

        return content.substring(0, startPos) + formattedText + content.substring(endPos);
    }

    // 格式化HTML内容的辅助方法
    private String formatHtmlContent(String htmlContent, String type, int startPos, int endPos) {
        if (startPos < 0 || endPos > htmlContent.length() || startPos > endPos) {
            throw new IllegalArgumentException("Invalid position parameters");
        }

        String selectedText = htmlContent.substring(startPos, endPos);
        String formattedText = "";

        switch (type) {
            case "bold":
                formattedText = "<strong>" + selectedText + "</strong>";
                break;
            case "italic":
                formattedText = "<em>" + selectedText + "</em>";
                break;
            case "underline":
                formattedText = "<u>" + selectedText + "</u>";
                break;
            default:
                throw new IllegalArgumentException("Unsupported format type: " + type);
        }

        return htmlContent.substring(0, startPos) + formattedText + htmlContent.substring(endPos);
    }

    // 格式化标题内容的辅助方法
    private String formatHeading(String content, String level, int startPos, int endPos) {
        if (startPos < 0 || endPos > content.length() || startPos > endPos) {
            throw new IllegalArgumentException("Invalid position parameters");
        }

        String selectedText = content.substring(startPos, endPos).trim();
        String headingMarks = "#".repeat(Integer.parseInt(level.substring(1)));
        String formattedText = headingMarks + " " + selectedText;

        // 确保标题前后有换行
        if (startPos > 0 && content.charAt(startPos - 1) != '\n') {
            formattedText = "\n" + formattedText;
        }
        if (endPos < content.length() && content.charAt(endPos) != '\n') {
            formattedText = formattedText + "\n";
        }

        return content.substring(0, startPos) + formattedText + content.substring(endPos);
    }

    // 格式化HTML标题的辅助方法
    private String formatHtmlHeading(String htmlContent, String level, int startPos, int endPos) {
        if (startPos < 0 || endPos > htmlContent.length() || startPos > endPos) {
            throw new IllegalArgumentException("Invalid position parameters");
        }

        String selectedText = htmlContent.substring(startPos, endPos).trim();
        String formattedText = String.format("<%s>%s</%s>", level, selectedText, level);

        return htmlContent.substring(0, startPos) + formattedText + htmlContent.substring(endPos);
    }

}