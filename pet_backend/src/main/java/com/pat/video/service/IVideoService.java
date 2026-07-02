package com.pat.video.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pat.video.domain.entity.Video;

public interface IVideoService extends IService<Video> {
    /**
     * 增加视频播放次数。
     *
     * @param id 视频 ID
     */
    void incrementPlayCount(Long id);
    /**
     * 增加视频点赞数。
     *
     * @param id 视频 ID
     */
    void incrementLikes(Long id);
}