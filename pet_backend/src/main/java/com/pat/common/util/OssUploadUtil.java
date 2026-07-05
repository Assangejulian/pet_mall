package com.pat.common.util;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectResult;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@Component
public class OssUploadUtil {

    @Value("${oss.endpoint}")
    private String endpoint;

    @Value("${oss.access-key-id}")
    private String accessKeyId;

    @Value("${oss.access-key-secret}")
    private String accessKeySecret;

    @Value("${oss.bucket-name}")
    private String bucketName;

    @Value("${oss.domain}")
    private String domain;

    private OSS ossClient;

    @PostConstruct
    public void init() {
        if (!StringUtils.hasText(accessKeyId) || !StringUtils.hasText(accessKeySecret)
                || !StringUtils.hasText(bucketName) || !StringUtils.hasText(domain)) {
            log.warn("OSS 配置不完整，文件上传不可用");
            return;
        }
        this.ossClient = new OSSClientBuilder().build(normalizeEndpoint(endpoint), accessKeyId, accessKeySecret);
        log.info("OSS 客户端初始化完成");
    }

    @PreDestroy
    public void destroy() {
        if (ossClient != null) {
            ossClient.shutdown();
        }
    }

    public String upload(MultipartFile file) throws IOException {
        if (ossClient == null) {
            throw new IllegalStateException("OSS 未配置，请设置 OSS 相关参数");
        }

        String key = buildObjectKey(file);

        try (InputStream input = file.getInputStream()) {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            if (StringUtils.hasText(file.getContentType())) {
                metadata.setContentType(file.getContentType());
            }
            PutObjectResult result = ossClient.putObject(bucketName, key, input, metadata);
            log.info("OSS 上传成功: {} (eTag={})", key, result.getETag());
        }

        return normalizeDomain(domain) + "/" + key;
    }

    private String buildObjectKey(MultipartFile file) {
        String ext = getExtension(file.getOriginalFilename());
        String folder = resolveFolder(file.getContentType(), ext);
        return folder + "/" + UUID.randomUUID().toString().replace("-", "") + ext;
    }

    private String resolveFolder(String contentType, String ext) {
        String lowerType = contentType == null ? "" : contentType.toLowerCase(Locale.ROOT);
        String lowerExt = ext.toLowerCase(Locale.ROOT);
        if (lowerType.startsWith("image/") || isImageExtension(lowerExt)) {
            return "covers";
        }
        if (lowerType.startsWith("video/") || isVideoExtension(lowerExt)) {
            return "videos";
        }
        return "files";
    }

    private boolean isImageExtension(String ext) {
        return ".jpg".equals(ext) || ".jpeg".equals(ext) || ".png".equals(ext) || ".webp".equals(ext);
    }

    private boolean isVideoExtension(String ext) {
        return ".mp4".equals(ext) || ".webm".equals(ext) || ".mov".equals(ext) || ".m4v".equals(ext);
    }

    private String getExtension(String filename) {
        if (!StringUtils.hasText(filename)) {
            return "";
        }
        int index = filename.lastIndexOf(".");
        if (index < 0 || index == filename.length() - 1) {
            return "";
        }
        return filename.substring(index).toLowerCase(Locale.ROOT);
    }

    private String normalizeEndpoint(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        if (value.startsWith("http://") || value.startsWith("https://")) {
            return value;
        }
        return "https://" + value;
    }

    private String normalizeDomain(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
