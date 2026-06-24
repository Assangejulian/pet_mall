package com.pat.video.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pat.video.entity.Video;
import com.pat.video.mapper.VideoMapper;
import com.pat.video.service.IVideoService;
import org.springframework.stereotype.Service;

@Service
public class VideoServiceImpl extends ServiceImpl<VideoMapper, Video> implements IVideoService {

    @Override
    public void incrementPlayCount(Long id) {
        Video video = this.getById(id);
        if (video != null) {
            video.setPlayCount((video.getPlayCount() == null ? 0 : video.getPlayCount()) + 1);
            this.updateById(video);
        }
    }

    @Override
    public void incrementLikes(Long id) {
        Video video = this.getById(id);
        if (video != null) {
            video.setLikes((video.getLikes() == null ? 0 : video.getLikes()) + 1);
            this.updateById(video);
        }
    }
}