package com.pat.video.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pat.common.controller.BaseController;
import com.pat.common.domain.Result;
import com.pat.user.domain.entity.User;
import com.pat.user.service.UserService;
import com.pat.video.domain.entity.Comment;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.pat.video.domain.entity.Video;
import com.pat.video.service.ICommentService;
import com.pat.video.service.IVideoService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@Tag(name = "视频管理", description = "视频 Feed/播放/点赞/评论")
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
    protected Video toVO(Video entity) {
        return entity;
    }

    @Override
    protected Video toDO(Video param) {
        return param;
    }

    @Override
    protected QueryWrapper<Video> buildQueryWrapper(Video param) {
        QueryWrapper<Video> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("create_time");
        if (param == null || param.getStatus() == null) {
            wrapper.eq("status", 1);
        }
        if (param != null) {
            if (param.getStatus() != null) {
                wrapper.eq("status", param.getStatus());
            }
            if (param.getUserId() != null) {
                wrapper.eq("user_id", param.getUserId());
            }
            if (param.getProductId() != null) {
                wrapper.eq("product_id", param.getProductId());
            }
            if (param.getTitle() != null && !param.getTitle().isBlank()) {
                wrapper.and(w -> w.like("title", param.getTitle()).or().like("description", param.getTitle()));
            }
        }
        return wrapper;
    }

    @Override
    @Operation(summary = "视频分页搜索")
    @GetMapping("/search")
    public Result<IPage<Video>> search(Video param,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<Video> pageParam = new Page<>(page, size);
        Page<Video> result = videoService.page(pageParam, buildQueryWrapper(param));
        return Result.success(result);
    }

    @Operation(summary = "视频详情（播放量+1）")
    @GetMapping("/{id}")
    @Override
    public Result<Video> getById(@PathVariable Long id) {
        Video video = videoService.getById(id);
        if (video == null) {
            return Result.error(404, "视频不存在");
        }
        videoService.incrementPlayCount(id);
        return Result.success(videoService.getById(id));
    }

    @Operation(summary = "视频播放（播放量+1）")
    @GetMapping("/play/{id}")
    public Result<Video> play(@PathVariable Long id) {
        Video video = videoService.getById(id);
        if (video == null) {
            return Result.error(404, "视频不存在");
        }
        videoService.incrementPlayCount(id);
        return Result.success(videoService.getById(id));
    }

    @Operation(summary = "点赞视频")
    @PostMapping("/{id}/like")
    public Result<Void> like(@PathVariable Long id) {
        videoService.incrementLikes(id);
        return Result.success();
    }

    @Operation(summary = "视频评论列表")
    @GetMapping("/{id}/comments")
    public Result<List<Map<String, Object>>> comments(@PathVariable Long id) {
        QueryWrapper<Comment> wrapper = new QueryWrapper<>();
        wrapper.eq("video_id", id).orderByDesc("create_time");
        List<Map<String, Object>> records = new ArrayList<>();
        for (Comment comment : commentService.list(wrapper)) {
            records.add(toCommentItem(comment));
        }
        return Result.success(records);
    }

    @Operation(summary = "发表评论")
    @PostMapping("/{id}/comment")
    public Result<Comment> addComment(@PathVariable Long id, @RequestBody Comment comment) {
        comment.setVideoId(id);
        if (comment.getUserId() == null) {
            comment.setUserId(2L);
        }
        commentService.save(comment);

        Video video = videoService.getById(id);
        if (video != null) {
            video.setCommentCount((video.getCommentCount() == null ? 0 : video.getCommentCount()) + 1);
            videoService.updateById(video);
        }
        return Result.success(comment);
    }

    @Operation(summary = "上传视频文件")
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error(400, "文件不能为空");
        }
        try {
            String uploadDir = System.getProperty("user.dir") + "/uploads/";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : ".mp4";
            String newFilename = UUID.randomUUID().toString().replace("-", "") + extension;
            file.transferTo(new File(uploadDir + newFilename));
            return Result.success("/uploads/" + newFilename);
        } catch (IOException e) {
            return Result.error(500, "文件上传失败");
        }
    }

    private Map<String, Object> toCommentItem(Comment comment) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", comment.getId());
        item.put("text", comment.getContent());
        item.put("content", comment.getContent());
        item.put("time", comment.getCreateTime());

        User user = comment.getUserId() == null ? null : userService.getById(comment.getUserId());
        item.put("user", user != null ? user.getRealName() : "匿名用户");
        item.put("avatar", user != null ? user.getAvatar() : "");
        return item;
    }
}
