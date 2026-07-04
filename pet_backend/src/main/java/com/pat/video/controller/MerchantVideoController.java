package com.pat.video.controller;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pat.common.domain.Result;
import com.pat.common.util.UserHolder;
import com.pat.video.domain.entity.Video;
import com.pat.video.service.IVideoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
@Tag(name = "视频管理(商家端)", description = "商家端视频 CRUD、上下架")
@RestController
@RequestMapping("/api/merchant/video")
public class MerchantVideoController {
    private final IVideoService videoService;
    public MerchantVideoController(IVideoService videoService) { this.videoService = videoService; }
    
    @Operation(summary = "商家端视频分页查询")
    @GetMapping("/search")
    public Result<IPage<Video>> search(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        QueryWrapper<Video> wrapper = new QueryWrapper();
        wrapper.eq("user_id", UserHolder.getUserId());
        wrapper.orderByDesc("create_time");
        if (keyword != null && !keyword.isBlank()) wrapper.like("title", keyword);
        if (status != null) wrapper.eq("status", status);
        return Result.success(videoService.page(new Page(page, size), wrapper));
    }
    
    @Operation(summary = "视频详情(商家端)")
    @GetMapping("/{id}")
    public Result<Video> detail(@PathVariable Long id) {
        Video video = videoService.getById(id);
        if (video == null) return Result.error(404, "视频不存在");
        if (!video.getUserId().equals(UserHolder.getUserId())) return Result.error(403, "无权访问此视频");
        return Result.success(video);
    }
    
    @Operation(summary = "新增视频")
    @PostMapping
    public Result<Video> create(@RequestBody Video video) {
        video.setId(null);
        video.setUserId(UserHolder.getUserId());
        video.setStatus(0); video.setPlayCount(0); video.setLikes(0); video.setCommentCount(0);
        videoService.save(video);
        return Result.success(video);
    }
    
    @Operation(summary = "修改视频")
    @PutMapping("/{id}")
    public Result<Video> update(@PathVariable Long id, @RequestBody Video video) {
        Video exist = videoService.getById(id);
        if (exist == null) return Result.error(404, "视频不存在");
        if (!exist.getUserId().equals(UserHolder.getUserId())) return Result.error(403, "无权修改此视频");
        video.setId(id); video.setUserId(null);
        videoService.updateById(video);
        return Result.success(videoService.getById(id));
    }
    
    @Operation(summary = "删除视频")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        Video video = videoService.getById(id);
        if (video == null) return Result.error(404, "视频不存在");
        if (!video.getUserId().equals(UserHolder.getUserId())) return Result.error(403, "无权删除此视频");
        return Result.success(videoService.removeById(id));
    }
    
    @Operation(summary = "视频上架")
    @PutMapping("/{id}/online")
    public Result<Video> online(@PathVariable Long id) {
        Video video = videoService.getById(id);
        if (video == null) return Result.error(404, "视频不存在");
        if (!video.getUserId().equals(UserHolder.getUserId())) return Result.error(403, "无权操作此视频");
        video.setStatus(1); videoService.updateById(video);
        return Result.success(video);
    }
    
    @Operation(summary = "视频下架")
    @PutMapping("/{id}/offline")
    public Result<Video> offline(@PathVariable Long id) {
        Video video = videoService.getById(id);
        if (video == null) return Result.error(404, "视频不存在");
        if (!video.getUserId().equals(UserHolder.getUserId())) return Result.error(403, "无权操作此视频");
        video.setStatus(0); videoService.updateById(video);
        return Result.success(video);
    }
}
