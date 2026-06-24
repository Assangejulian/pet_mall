package com.pat.common.controller;

import com.pat.common.domain.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
public class FileController {

    @Value("${upload.dir:uploads}")
    private String uploadDir;

    @PostMapping
    public Result<String> upload(@RequestParam("file") MultipartFile file) throws Exception {
        // 确保目录存在
        Path dir = Path.of(uploadDir);
        Files.createDirectories(dir);

        // 保持后缀，重命名防冲突
        String ext = "";
        String name = file.getOriginalFilename();
        if (name != null && name.contains(".")) {
            ext = name.substring(name.lastIndexOf("."));
        }
        String filename = UUID.randomUUID().toString() + ext;

        // 保存文件
        Path target = dir.resolve(filename);
        file.transferTo(target.toFile());

        // 返回可访问的 URL
        return Result.success("/uploads/" + filename);
    }
}