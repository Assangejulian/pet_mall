package com.pat.video.domain.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class VideoVO {
    private Long id;
    private Long userId;
    private String title;
    private String description;
    private String url;
    private String cover;
    private Long productId;
    private Integer playCount;
    private Integer likes;
    private Integer commentCount;
    private Integer duration;
    private Integer status;
    private LocalDateTime createTime;
}