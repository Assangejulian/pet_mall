package com.pat.video.domain.dto;

import lombok.Data;

@Data
public class VideoQueryParam {
    private String keyword;
    private Integer status;
    private Long userId;
}