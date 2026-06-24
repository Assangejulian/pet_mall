package com.pat.video.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pat.video.entity.Video;

public interface IVideoService extends IService<Video> {
    void incrementPlayCount(Long id);
}
