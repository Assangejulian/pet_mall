package com.pat.common.oss;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.PutObjectResult;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Slf4j
@Service
public class OssService {

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
        if (accessKeyId == null || accessKeyId.isBlank()) {
            log.warn("OSS 配置不完整，上传将使用本地文件系统");
            return;
        }
        this.ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        log.info("OSS 客户端初始化完成");
    }

    public String upload(MultipartFile file) throws Exception {
        if (ossClient == null) {
            throw new RuntimeException("OSS 未配置，请设置 OSS 相关参数");
        }

        String ext = "";
        String name = file.getOriginalFilename();
        if (name != null && name.contains(".")) {
            ext = name.substring(name.lastIndexOf("."));
        }
        String key = "videos/" + UUID.randomUUID().toString().replace("-", "") + ext;

        try (InputStream input = file.getInputStream()) {
            PutObjectResult result = ossClient.putObject(bucketName, key, input);
            log.info("OSS 上传成功: {} (eTag={})", key, result.getETag());
        }

        return domain + "/" + key;
    }
}
