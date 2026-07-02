-- ============================================
-- video 表定义
-- 字段已与完整 schema 对齐（url/cover 而非 video_url/cover_url）
-- ============================================
CREATE TABLE IF NOT EXISTS video (
    id            BIGINT       NOT NULL PRIMARY KEY COMMENT '雪花ID',
    user_id       BIGINT       NOT NULL COMMENT '发布者',
    title         VARCHAR(200) NOT NULL COMMENT '标题',
    description   VARCHAR(500) COMMENT '简介',
    url           VARCHAR(500) NOT NULL COMMENT '视频地址',
    cover         VARCHAR(500) COMMENT '封面图',
    product_id    BIGINT       COMMENT '关联商品',
    play_count    INT          NOT NULL DEFAULT 0 COMMENT '播放次数',
    likes         INT          NOT NULL DEFAULT 0 COMMENT '点赞数',
    comment_count INT          NOT NULL DEFAULT 0 COMMENT '评论数',
    duration      INT          NOT NULL DEFAULT 0 COMMENT '时长(秒)',
    status        TINYINT      NOT NULL DEFAULT 1 COMMENT '0下架 1上架',
    deleted       TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    create_time   DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    update_time   DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    INDEX idx_user_id (user_id),
    INDEX idx_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视频';
