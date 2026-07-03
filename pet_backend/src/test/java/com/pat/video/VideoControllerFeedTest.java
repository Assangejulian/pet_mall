package com.pat.video;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pat.common.domain.Result;
import com.pat.user.domain.entity.User;
import com.pat.user.service.UserService;
import com.pat.video.controller.VideoController;
import com.pat.video.domain.entity.Video;
import com.pat.video.service.ICommentService;
import com.pat.video.service.IVideoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VideoControllerFeedTest {

    private final IVideoService videoService = mock(IVideoService.class);
    private final ICommentService commentService = mock(ICommentService.class);
    private final UserService userService = mock(UserService.class);
    private final VideoController controller = new VideoController();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(controller, "videoService", videoService);
        ReflectionTestUtils.setField(controller, "commentService", commentService);
        ReflectionTestUtils.setField(controller, "userService", userService);
    }

    @Test
    void missingVideoDetailReturnsBusiness404InsteadOf500() {
        when(videoService.getById(404L)).thenReturn(null);

        Result<Video> response = controller.getById(404L);

        assertThat(response.getCode()).isEqualTo(404);
        assertThat(response.getMessage()).isEqualTo("视频不存在");
    }

    @Test
    void existingVideoDetailIncrementsPlayCount() {
        Video video = video(12L);
        when(videoService.getById(12L)).thenReturn(video);

        Result<Video> response = controller.getById(12L);

        assertThat(response.getCode()).isEqualTo(200);
        assertThat(response.getData()).isSameAs(video);
        verify(videoService).incrementPlayCount(12L);
    }

    private Page<Video> page(Video video) {
        Page<Video> page = new Page<>(1, 20, 1);
        page.setRecords(List.of(video));
        return page;
    }

    private Video video(Long id) {
        Video video = new Video();
        video.setId(id);
        return video;
    }
}
