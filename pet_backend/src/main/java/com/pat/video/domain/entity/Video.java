package com.pat.video.domain.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.pat.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("video")
public class Video extends BaseEntity {
    private Long userId;
    private String title;
    private String description;
    @TableField("video_url")
    private String url;
    @TableField("cover_url")
    private String cover;
    private Long productId;
    private Integer playCount;
    private Integer likes;
    private Integer commentCount;
    private Integer duration;
    private Integer status;
    
    @TableLogic
    private Integer deleted;
}
