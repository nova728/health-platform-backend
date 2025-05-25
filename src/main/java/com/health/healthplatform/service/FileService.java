package com.health.healthplatform.service;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.OSSObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Date;

@Service
public class FileService {

    @Autowired
    private OSSClient ossClient;

    @Value("${oss.bucketName}")
    private String bucketName;

    @Value("${oss.endpoint}")
    private String endpoint;

    public String uploadFile(String filename, InputStream inputStream) {
        // 上传文件到 OSS
        ossClient.putObject(bucketName, filename, inputStream);

        // 方法1：返回永久有效的URL (使用公共访问地址)
        return String.format("https://%s.%s/%s", bucketName, endpoint, filename);

        // 方法2：设置超长有效期 (比如100年)
        // return ossClient.generatePresignedUrl(bucketName, filename,
        //     new Date(System.currentTimeMillis() + 100L * 365 * 24 * 60 * 60 * 1000)).toString();
    }

    public InputStream downloadFile(String filename) {
        OSSObject ossObject = ossClient.getObject(bucketName, filename);
        return ossObject.getObjectContent();
    }

    public String getPresignedUrl(String filename) {
        // 同样返回永久有效的URL
        return String.format("https://%s.%s/%s", bucketName, endpoint, filename);
    }

}