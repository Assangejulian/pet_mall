package com.pat.video.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.pat.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("video")
public class Video extends BaseEntity {
    private String title;
    private String videoUrl;
    private String coverUrl;
    private Long productId;
    private Integer playCount;
    private Integer duration;
    private Integer status;
    
    @TableLogic
    private Integer deleted;
}
