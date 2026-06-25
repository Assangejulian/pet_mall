package com.pat.video.controller;
import cn.hutool.core.bean.BeanUtil;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pat.common.controller.BaseController;
import com.pat.common.domain.Result;
import com.pat.video.domain.dto.VideoQueryParam;
import com.pat.video.domain.entity.Video;
import com.pat.video.service.IVideoService;
import com.pat.video.domain.vo.VideoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/video")
@Tag(name = "视频管理（后台）")
public class VideoAdminController extends BaseController<Video, VideoQueryParam, VideoVO> {

    private final IVideoService videoService;

    public VideoAdminController(IVideoService videoService) {
        super(videoService);
        this.videoService = videoService;
    }

    @Override
    protected VideoVO toVO(Video entity) {
        VideoVO vo = new VideoVO();
        BeanUtil.copyProperties(entity, vo);
        return vo;
    }

    protected Video toDO(VideoQueryParam param) {
        Video video = new Video();
        if (param != null) {
            video.setTitle(param.getKeyword());
            video.setStatus(param.getStatus());
            video.setUserId(param.getUserId());
        }
        return video;
    }

    @Override
    protected QueryWrapper<Video> buildQueryWrapper(VideoQueryParam param) {
        QueryWrapper<Video> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("create_time");
        if (param == null) return wrapper;
        if (param.getKeyword() != null && !param.getKeyword().isBlank())
            wrapper.like("title", param.getKeyword());
        if (param.getStatus() != null) wrapper.eq("status", param.getStatus());
        if (param.getUserId() != null) wrapper.eq("user_id", param.getUserId());
        return wrapper;
    }

    @Operation(summary = "视频上架")
    @PutMapping("/{id}/online")
    public Result<VideoVO> online(@PathVariable Long id) {
        Video video = videoService.getById(id);
        if (video != null) { video.setStatus(1); videoService.updateById(video); }
        return Result.success(toVO(video));
    }

    @Operation(summary = "视频下架")
    @PutMapping("/{id}/offline")
    public Result<VideoVO> offline(@PathVariable Long id) {
        Video video = videoService.getById(id);
        if (video != null) { video.setStatus(0); videoService.updateById(video); }
        return Result.success(toVO(video));
    }
}
