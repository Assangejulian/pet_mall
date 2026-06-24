package com.pat.video.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pat.common.controller.BaseController;
import com.pat.common.domain.Result;
import com.pat.video.entity.Comment;
import com.pat.video.entity.Video;
import com.pat.video.service.ICommentService;
import com.pat.video.service.IVideoService;
import com.pat.user.entity.User;
import com.pat.user.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/video")
public class VideoController extends BaseController<Video, Video, Video> {

    @Resource
    private IVideoService videoService;

    @Resource
    private ICommentService commentService;

    @Resource
    private UserService userService;

    public VideoController(IVideoService service) {
        super(service);
    }

    @Override
    protected Video toVO(Video entity) { return entity; }

    @Override
    protected Video toDO(Video param) { return param; }

    @Override
    protected QueryWrapper<Video> buildQueryWrapper(Video param) {
        QueryWrapper<Video> qw = new QueryWrapper<>();
        qw.orderByDesc("create_time");
        if (param.getStatus() != null) qw.eq("status", param.getStatus());
        if (param.getUserId() != null) qw.eq("user_id", param.getUserId());
        return qw;
    }

    // 列表（含作者信息）
    @GetMapping("/list")
    public Result<Page<Map<String, Object>>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<Video> pageParam = new Page<>(page, size);
        QueryWrapper<Video> qw = new QueryWrapper<>();
        qw.eq("status", 1).orderByDesc("create_time");
        Page<Video> result = videoService.page(pageParam, qw);

        Page<Map<String, Object>> voPage = new Page<>(page, size, result.getTotal());
        List<Map<String, Object>> list = new ArrayList<>();
        for (Video v : result.getRecords()) {
            Map<String, Object> vo = new LinkedHashMap<>();
            vo.put("id", v.getId());
            vo.put("title", v.getTitle());
            vo.put("desc", v.getDescription());
            vo.put("cover", v.getCover());
            vo.put("url", v.getUrl());
            vo.put("likes", v.getLikes());
            vo.put("commentCount", v.getCommentCount());
            vo.put("playCount", v.getPlayCount());
            vo.put("productId", v.getProductId());
            vo.put("createTime", v.getCreateTime());
            User author = userService.getById(v.getUserId());
            vo.put("author", author != null ? author.getRealName() : "未知");
            vo.put("avatar", author != null ? author.getAvatar() : "");
            list.add(vo);
        }
        voPage.setRecords(list);
        return Result.success(voPage);
    }

    // 详情（播放 + 评论列表 + 商品）
    @GetMapping("/{id}")
    @Override
    public Result<Video> getById(@PathVariable Long id) {
        videoService.incrementPlayCount(id);
        return super.getById(id);
    }

    // 播放（+播放量）
    @GetMapping("/play/{id}")
    public Result<Video> play(@PathVariable Long id) {
        videoService.incrementPlayCount(id);
        return Result.success(videoService.getById(id));
    }

    // 点赞
    @PostMapping("/{id}/like")
    public Result<Void> like(@PathVariable Long id) {
        videoService.incrementLikes(id);
        return Result.success();
    }

    // 获取评论列表
    @GetMapping("/{id}/comments")
    public Result<List<Map<String, Object>>> comments(@PathVariable Long id) {
        QueryWrapper<Comment> qw = new QueryWrapper<>();
        qw.eq("video_id", id).orderByDesc("create_time");
        List<Comment> comments = commentService.list(qw);
        List<Map<String, Object>> list = new ArrayList<>();
        for (Comment c : comments) {
            Map<String, Object> vo = new LinkedHashMap<>();
            vo.put("id", c.getId());
            vo.put("text", c.getContent());
            vo.put("time", c.getCreateTime());
            User u = userService.getById(c.getUserId());
            vo.put("user", u != null ? u.getRealName() : "匿名");
            vo.put("avatar", u != null ? u.getAvatar() : "");
            list.add(vo);
        }
        return Result.success(list);
    }

    // 发表评论
    @PostMapping("/{id}/comment")
    public Result<Comment> addComment(@PathVariable Long id, @RequestBody Comment comment) {
        comment.setVideoId(id);
        commentService.save(comment);
        // 更新评论数
        Video video = videoService.getById(id);
        if (video != null) {
            video.setCommentCount((video.getCommentCount() == null ? 0 : video.getCommentCount()) + 1);
            videoService.updateById(video);
        }
        return Result.success(comment);
    }

    // 上传视频文件
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return Result.error(400, "文件不能为空");
        try {
            String uploadDir = System.getProperty("user.dir") + "/uploads/";
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null
                    ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".mp4";
            String newFilename = UUID.randomUUID().toString().replace("-", "") + extension;
            file.transferTo(new File(uploadDir + newFilename));
            return Result.success("/uploads/" + newFilename);
        } catch (IOException e) {
            return Result.error(500, "文件上传失败");
        }
    }
}