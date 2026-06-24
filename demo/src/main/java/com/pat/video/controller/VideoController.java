package com.pat.video.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pat.common.domain.Result;
import com.pat.video.entity.Video;
import com.pat.video.service.IVideoService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.annotation.Resource;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/video")
public class VideoController {

    @Resource
    private IVideoService videoService;

    // 获取视频列表 (支持分页)
    @GetMapping("/list")
    public Result<Page<Video>> list(@RequestParam(defaultValue = "1") Integer page,
                                    @RequestParam(defaultValue = "10") Integer size) {
        Page<Video> pageParam = new Page<>(page, size);
        QueryWrapper<Video> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time");
        return Result.success(videoService.page(pageParam, queryWrapper));
    }

    // 获取视频详情并增加播放量
    @GetMapping("/play/{id}")
    public Result<Video> play(@PathVariable Long id) {
        videoService.incrementPlayCount(id);
        Video video = videoService.getById(id);
        return Result.success(video);
    }

    // 上传视频文件 (本地存储简易版)
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error(400, "文件不能为空");
        }
        try {
            // 本地存储目录 (需确保存在)
            String uploadDir = System.getProperty("user.dir") + "/uploads/";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".mp4";
            String newFilename = UUID.randomUUID().toString().replace("-", "") + extension;
            
            File dest = new File(uploadDir + newFilename);
            file.transferTo(dest);
            
            // 返回本地访问 URL (假设配置了静态资源映射 /uploads/**)
            String fileUrl = "/uploads/" + newFilename;
            return Result.success(fileUrl);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error(500, "文件上传失败");
        }
    }

    // 新增/更新视频信息
    @PostMapping("/save")
    public Result<Void> save(@RequestBody Video video) {
        videoService.saveOrUpdate(video);
        return Result.success();
    }

    // 删除视频
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        videoService.removeById(id);
        return Result.success();
    }
}
