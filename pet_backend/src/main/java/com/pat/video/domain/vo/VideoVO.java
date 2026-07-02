package com.pat.video.domain.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class VideoVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    private String title;
    private String description;
    private String url;
    private String cover;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long productId;

    private Integer playCount;
    private Integer likes;
    private Integer commentCount;
    private Integer duration;
    private Integer status;
    private LocalDateTime createTime;
}
