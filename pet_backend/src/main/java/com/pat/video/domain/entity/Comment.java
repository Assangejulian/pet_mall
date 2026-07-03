package com.pat.video.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.pat.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("comment")
public class Comment extends BaseEntity {
    private static final long serialVersionUID = 1L;
    private Long videoId;
    private Long userId;
    private String content;
}
