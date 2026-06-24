CREATE TABLE IF NOT EXISTS `video` (
  `id` bigint NOT NULL COMMENT '主键 (雪花算法)',
  `title` varchar(200) NOT NULL COMMENT '视频标题',
  `video_url` varchar(500) NOT NULL COMMENT '视频播放地址',
  `cover_url` varchar(500) DEFAULT NULL COMMENT '视频封面地址',
  `product_id` bigint DEFAULT NULL COMMENT '关联商品ID',
  `play_count` int DEFAULT '0' COMMENT '播放次数',
  `duration` int DEFAULT '0' COMMENT '视频时长(秒)',
  `status` tinyint DEFAULT '1' COMMENT '状态 0-下架 1-上架',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除 0-正常 1-已删除',
  `create_time` datetime(3) DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `update_time` datetime(3) DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='视频表';
