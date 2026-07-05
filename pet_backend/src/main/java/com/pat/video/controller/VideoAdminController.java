package com.pat.video.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pat.common.domain.Result;
import com.pat.video.domain.dto.VideoQueryParam;
import com.pat.video.domain.entity.Video;
import com.pat.video.service.IVideoService;
import com.pat.video.domain.vo.VideoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/video")
@Tag(name = "视频管理（后台）")
public class VideoAdminController {

    private final IVideoService videoService;

    public VideoAdminController(IVideoService videoService) {
        this.videoService = videoService;
    }

    @GetMapping("/{id}")
    public Result<VideoVO> getById(@PathVariable Long id) {
        Video entity = videoService.getById(id);
        return entity == null ? Result.error("数据不存在") : Result.success(toVO(entity));
    }

    @PostMapping
    public Result<Boolean> save(@RequestBody @Valid VideoQueryParam param) {
        Video entity = toDO(param);
        return Result.success(videoService.save(entity));
    }

    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody @Valid VideoQueryParam param) {
        Video entity = toDO(param);
        entity.setId(id);
        return Result.success(videoService.updateById(entity));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> remove(@PathVariable Long id) {
        return Result.success(videoService.removeById(id));
    }

    @DeleteMapping("/batch")
    public Result<Boolean> removeBatch(@RequestBody List<Long> ids) {
        return Result.success(videoService.removeByIds(ids));
    }

    @GetMapping("/search")
    public Result<IPage<VideoVO>> search(VideoQueryParam param, Page<Video> page) {
        Page<Video> result = videoService.page(page, buildQueryWrapper(param));
        return Result.success(result.convert(this::toVO));
    }

    @GetMapping("/list")
    public Result<List<VideoVO>> getList(VideoQueryParam param) {
        List<Video> list = videoService.list(buildQueryWrapper(param));
        return Result.success(list.stream().map(this::toVO).collect(Collectors.toList()));
    }

    @GetMapping("/by-ids")
    public Result<List<VideoVO>> getByIds(@RequestParam List<Long> ids) {
        List<Video> list = videoService.listByIds(ids);
        return Result.success(list.stream().map(this::toVO).collect(Collectors.toList()));
    }

    @PostMapping("/batch")
    public Result<Boolean> saveBatch(@RequestBody @Valid List<VideoQueryParam> paramList) {
        List<Video> entities = paramList.stream().map(this::toDO).collect(Collectors.toList());
        return Result.success(videoService.saveBatch(entities));
    }

    @PutMapping("/batch")
    public Result<Boolean> updateBatch(@RequestBody @Valid List<VideoQueryParam> paramList) {
        List<Video> entities = paramList.stream().map(this::toDO).collect(Collectors.toList());
        return Result.success(videoService.updateBatchById(entities));
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

    private VideoVO toVO(Video entity) {
        VideoVO vo = new VideoVO();
        BeanUtil.copyProperties(entity, vo);
        return vo;
    }

    private Video toDO(VideoQueryParam param) {
        Video video = new Video();
        if (param != null) {
            video.setTitle(param.getKeyword());
            video.setStatus(param.getStatus());
            video.setUserId(param.getUserId());
        }
        return video;
    }

    private QueryWrapper<Video> buildQueryWrapper(VideoQueryParam param) {
        QueryWrapper<Video> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("create_time");
        if (param == null) return wrapper;
        if (param.getKeyword() != null && !param.getKeyword().isBlank())
            wrapper.like("title", param.getKeyword());
        if (param.getStatus() != null) wrapper.eq("status", param.getStatus());
        if (param.getUserId() != null) wrapper.eq("user_id", param.getUserId());
        return wrapper;
    }
}
