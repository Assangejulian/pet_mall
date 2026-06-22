package com.pat.demo.admin.model.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_news")
public class SysNews {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;
    private String summary;
    private String content;
    private String coverImage;
    private String source;
    private String category;
    private LocalDateTime publishTime;

    @TableField("is_published")
    private Boolean isPublished;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    private String createBy;
}
