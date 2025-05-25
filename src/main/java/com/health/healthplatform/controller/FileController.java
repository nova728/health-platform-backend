package com.health.healthplatform.controller;

import com.health.healthplatform.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@RestController
public class FileController {


    @Autowired
    private FileService fileService;

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file) throws IOException {

        // 获取文件的输入流
        InputStream inputStream = file.getInputStream();
        // 生成文件名
        String filename = UUID.randomUUID().toString() + file.getOriginalFilename();
        // 调用文件上传方法
        String fileUrl = fileService.uploadFile(filename, inputStream);
        return fileUrl;
    }

    @GetMapping("/download/{filename}")
    public ResponseEntity<InputStreamResource> downloadFile(@PathVariable("filename") String filename) throws IOException {
        InputStream inputStream = fileService.downloadFile(filename);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=" + filename);
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(inputStream));
    }

    @GetMapping("/file/url/{filename}")
    public String getFileUrl(@PathVariable("filename") String filename) {
        // 返回新的URL
        return fileService.getPresignedUrl(filename);
    }


}