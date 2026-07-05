package com.pat.common.controller;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSSException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.pat.common.domain.Result;
import com.pat.common.util.OssUploadUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@RestController
@Tag(name = "文件上传", description = "通用文件上传")
@RequestMapping("/api/upload")
public class FileController {

    private final OssUploadUtil ossUploadUtil;

    public FileController(OssUploadUtil ossUploadUtil) {
        this.ossUploadUtil = ossUploadUtil;
    }

    @Operation(summary = "上传文件")
    @PostMapping
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.error(400, "文件不能为空");
        }
        try {
            return Result.success(ossUploadUtil.upload(file));
        } catch (IllegalStateException ex) {
            log.warn("OSS 上传配置不可用: {}", ex.getMessage());
            return Result.error(500, ex.getMessage());
        } catch (OSSException | ClientException | IOException ex) {
            log.error("OSS 文件上传失败", ex);
            return Result.error(500, "文件上传失败");
        }
    }
}
