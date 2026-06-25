-- ============================================
-- 宠物商店 - 完整建库建表脚本
-- 版本：v1.0（基于业务设计.md + 数据库设计规范）
-- 变更说明见 doc/变更说明.md
-- 数据库名：pet_store
-- 字符集：utf8mb4
-- 主键策略：雪花算法（IdType.ASSIGN_ID，Java 生成）
-- ============================================

CREATE DATABASE IF NOT EXISTS pet_store DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE pet_store;

-- ============================================
-- 1. user — 用户（含管理员）
-- [C1] 新增 role 字段替代 RBAC 四张表
-- ============================================
CREATE TABLE user (
    id          BIGINT      NOT NULL PRIMARY KEY COMMENT '雪花ID',
    username    VARCHAR(50) NOT NULL COMMENT '登录名',
    password    VARCHAR(255) NOT NULL COMMENT '密码（开发阶段明文，上线前 BCrypt）',
    phone       VARCHAR(20) COMMENT '手机号',
    avatar      VARCHAR(500) COMMENT '头像',
    email       VARCHAR(100) COMMENT '邮箱',
    member_level TINYINT    DEFAULT 0 NOT NULL COMMENT '会员等级 0-普通 1-银卡 2-金卡 3-钻石',
    real_name   VARCHAR(50) COMMENT '真实姓名',
    birthday    DATE        COMMENT '生日',
    role        VARCHAR(20) DEFAULT 'user' NOT NULL COMMENT '角色 user-普通用户 admin-管理员',
    status      TINYINT     DEFAULT 1 NOT NULL COMMENT '状态 0-禁用 1-正常',
    create_time DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
    update_time DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) NOT NULL,
    UNIQUE INDEX idx_username (username),
    UNIQUE INDEX idx_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户';
-- 补充微信登录字段（安全重复执行）
-- 补充微信登录字段
SET @sql_openid = (SELECT IF(COUNT(*) = 0,
    'ALTER TABLE user ADD COLUMN openid VARCHAR(100) COMMENT ''微信小程序 openid'' AFTER email',
    'SELECT 1'
) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user' AND COLUMN_NAME = 'openid');
PREPARE stmt FROM @sql_openid; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql_unionid = (SELECT IF(COUNT(*) = 0,
    'ALTER TABLE user ADD COLUMN unionid VARCHAR(100) COMMENT ''微信开放平台 unionid'' AFTER openid',
    'SELECT 1'
) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user' AND COLUMN_NAME = 'unionid');
PREPARE stmt FROM @sql_unionid; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ============================================
-- 2. user_address — 收货地址
-- ============================================
CREATE TABLE user_address (
    id            BIGINT       NOT NULL PRIMARY KEY COMMENT '雪花ID',
    user_id       BIGINT       NOT NULL COMMENT '用户ID',
    receiver_name VARCHAR(50)  NOT NULL COMMENT '收件人',
    phone         VARCHAR(20)  NOT NULL COMMENT '联系电话',
    province      VARCHAR(50)  COMMENT '省',
    city          VARCHAR(50)  COMMENT '市',
    district      VARCHAR(50)  COMMENT '区',
    detail        VARCHAR(200) NOT NULL COMMENT '详细地址',
    defaulted     TINYINT(1)   DEFAULT 0 NOT NULL COMMENT '是否默认 0-否 1-是',
    create_time   DATETIME(3)  DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
    update_time   DATETIME(3)  DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) NOT NULL,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收货地址';

-- ============================================
-- 3. store — 商店（商户）
-- ============================================
CREATE TABLE store (
    id          BIGINT        NOT NULL PRIMARY KEY COMMENT '雪花ID',
    user_id     BIGINT        NOT NULL COMMENT '店主（关联 user）',
    store_name  VARCHAR(100)  NOT NULL COMMENT '商店名称',
    store_logo  VARCHAR(500)  COMMENT '商店Logo',
    store_phone VARCHAR(20)   COMMENT '联系电话',
    store_desc  TEXT          COMMENT '商店描述',
    province    VARCHAR(50)   COMMENT '省',
    city        VARCHAR(50)   COMMENT '市',
    district    VARCHAR(50)   COMMENT '区',
    address     VARCHAR(200)  COMMENT '详细地址',
    longitude   DECIMAL(10,7) NOT NULL COMMENT '经度',
    latitude    DECIMAL(10,7) NOT NULL COMMENT '纬度',
    status      TINYINT       DEFAULT 0 NOT NULL COMMENT '状态 0-待审核 1-营业中 2-已关闭',
    deleted     TINYINT(1)    DEFAULT 0 NOT NULL COMMENT '逻辑删除',
    create_time DATETIME(3)   DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
    update_time DATETIME(3)   DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) NOT NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_city (city),
    INDEX idx_location (longitude, latitude)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商店';

-- ============================================
-- 4. product — 商品
-- ============================================
CREATE TABLE product (
    id           BIGINT         NOT NULL PRIMARY KEY COMMENT '雪花ID',
    store_id     BIGINT         NOT NULL COMMENT '所属商店ID',
    product_name VARCHAR(200)   NOT NULL COMMENT '商品名称',
    product_type TINYINT        NOT NULL DEFAULT 1 COMMENT '商品类型 1-宠物 2-周边',
    category     VARCHAR(50)    COMMENT '分类 狗/猫/鱼/鸟/其他',
    product_desc TEXT           COMMENT '商品详情',
    price        DECIMAL(10,2)  NOT NULL COMMENT '价格',
    stock        INT            DEFAULT 1 NOT NULL COMMENT '库存',
    main_image   VARCHAR(500)   COMMENT '主图',
    images       JSON           COMMENT '多图JSON',
    status       TINYINT        DEFAULT 1 NOT NULL COMMENT '状态 0-下架 1-上架 2-已售出',
    video_id     BIGINT         COMMENT '关联视频ID',
    deleted      TINYINT(1)     DEFAULT 0 NOT NULL COMMENT '逻辑删除',
    create_time  DATETIME(3)    DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
    update_time  DATETIME(3)    DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) NOT NULL,
    INDEX idx_store_id (store_id),
    INDEX idx_type (product_type),
    INDEX idx_category (category),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品';

-- ============================================
-- 5. cart — 购物车
-- ============================================
CREATE TABLE cart (
    id          BIGINT      NOT NULL PRIMARY KEY COMMENT '雪花ID',
    user_id     BIGINT      NOT NULL COMMENT '用户ID',
    product_id  BIGINT      NOT NULL COMMENT '商品ID',
    quantity    INT         NOT NULL DEFAULT 1 COMMENT '数量',
    checked     TINYINT(1)  DEFAULT 1 NOT NULL COMMENT '是否选中 0-否 1-是',
    create_time DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
    update_time DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) NOT NULL,
    UNIQUE INDEX idx_user_product (user_id, product_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车';

-- ============================================
-- 6. purchase_order — 订单（避免保留字 order）
-- ============================================
CREATE TABLE purchase_order (
    id                BIGINT         NOT NULL PRIMARY KEY COMMENT '雪花ID',
    order_no          VARCHAR(32)    NOT NULL COMMENT '订单号',
    user_id           BIGINT         NOT NULL COMMENT '用户ID',
    address_id        BIGINT         NOT NULL COMMENT '收货地址ID',
    total_amount      DECIMAL(10,2)  NOT NULL COMMENT '总金额',
    discount_amount   DECIMAL(10,2)  DEFAULT 0 NOT NULL COMMENT '优惠金额',
    pay_amount        DECIMAL(10,2)  NOT NULL COMMENT '实付金额',
    order_status      INT            NOT NULL DEFAULT 0 COMMENT '状态 0-待支付 1-已支付 2-已发货 3-已收货 4-已评价 -1-取消 -2-申请退单 -3-退单审核通过 -4-管理员退单',
    cancel_reason     VARCHAR(200)   COMMENT '取消原因',
    cancel_time       DATETIME(3)    COMMENT '取消时间',
    pay_time          DATETIME(3)    COMMENT '支付时间',
    ship_time         DATETIME(3)    COMMENT '发货时间',
    receive_time      DATETIME(3)    COMMENT '收货时间',
    evaluate_time     DATETIME(3)    COMMENT '评价时间',
    refund_apply_time DATETIME(3)    COMMENT '退单申请时间',
    refund_audit_time DATETIME(3)    COMMENT '退单审核时间',
    create_time       DATETIME(3)    DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
    update_time       DATETIME(3)    DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) NOT NULL,
    UNIQUE INDEX idx_order_no (order_no),
    INDEX idx_user_id (user_id),
    INDEX idx_order_status (order_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单';

-- ============================================
-- 7. order_item — 订单项
-- ============================================
CREATE TABLE order_item (
    id                BIGINT         NOT NULL PRIMARY KEY COMMENT '雪花ID',
    order_id          BIGINT         NOT NULL COMMENT '订单ID',
    product_id        BIGINT         NOT NULL COMMENT '商品ID',
    product_name      VARCHAR(200)   NOT NULL COMMENT '商品名称（快照）',
    product_image     VARCHAR(500)   COMMENT '商品图片（快照）',
    price             DECIMAL(10,2)  NOT NULL COMMENT '购买时单价',
    quantity          INT            NOT NULL DEFAULT 1 COMMENT '数量',
    evaluate_content  VARCHAR(500)   COMMENT '评价内容',
    evaluate_star     TINYINT        COMMENT '评价星级 1-5',
    evaluate_time     DATETIME(3)    COMMENT '评价时间',
    create_time       DATETIME(3)    DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
    INDEX idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单项';

-- ============================================
-- 8. video — 视频
-- [C3] video_url → url, cover_url → cover
-- ============================================
CREATE TABLE video (
    id          BIGINT       NOT NULL PRIMARY KEY COMMENT '雪花ID',
    title       VARCHAR(200) NOT NULL COMMENT '视频标题',
    url         VARCHAR(500) NOT NULL COMMENT '视频地址',
    cover       VARCHAR(500) COMMENT '封面图',
    product_id  BIGINT       COMMENT '关联商品ID',
    play_count  INT          DEFAULT 0 NOT NULL COMMENT '播放次数',
    status      TINYINT      DEFAULT 1 NOT NULL COMMENT '状态 0-下架 1-上架',
    deleted     TINYINT(1)   DEFAULT 0 NOT NULL COMMENT '逻辑删除',
    create_time DATETIME(3)  DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
    update_time DATETIME(3)  DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) NOT NULL,
    INDEX idx_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视频';

-- ============================================
-- 视频评论

-- 视频评论
CREATE TABLE IF NOT EXISTS `comment` (
    `id`          BIGINT       NOT NULL PRIMARY KEY COMMENT '雪花ID',
    `video_id`    BIGINT       NOT NULL COMMENT '视频ID',
    `user_id`     BIGINT       COMMENT '评论用户ID',
    `content`     TEXT         NOT NULL COMMENT '评论内容',
    `create_time` DATETIME(3)  DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
    `update_time` DATETIME(3)  DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) NOT NULL,
    INDEX idx_video_id (video_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视频评论';

-- 9. ai_chat_record — AI对话记录
-- ============================================
CREATE TABLE ai_chat_record (
    id          BIGINT       NOT NULL PRIMARY KEY COMMENT '雪花ID',
    user_id     BIGINT       COMMENT '用户ID(游客可为空)',
    session_id  VARCHAR(64)  NOT NULL COMMENT '会话ID',
    role        VARCHAR(20)  NOT NULL COMMENT '角色 user/assistant',
    content     TEXT         NOT NULL COMMENT '消息内容',
    create_time DATETIME(3)  DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
    INDEX idx_session_id (session_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI对话记录';

-- ============================================
-- 10. sys_message — 聊天 & 系统通知
-- ============================================
CREATE TABLE sys_message (
    id              BIGINT       NOT NULL PRIMARY KEY COMMENT '雪花ID',
    conversation_id VARCHAR(64)  NOT NULL COMMENT '会话ID',
    sender_id       BIGINT       NOT NULL COMMENT '发送人ID(0=系统)',
    receiver_id     BIGINT       NOT NULL COMMENT '接收人ID',
    content         TEXT         NOT NULL COMMENT '消息内容',
    type            TINYINT      NOT NULL COMMENT '类型 1-聊天 2-系统通知',
    readed          TINYINT(1)   DEFAULT 0 NOT NULL COMMENT '是否已读',
    create_time     DATETIME(3)  DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
    INDEX idx_conversation_id (conversation_id),
    INDEX idx_receiver_id (receiver_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息(聊天+通知)';

-- ============================================
-- 预设数据
-- ============================================
INSERT INTO user(id, username, password, role, status, real_name) VALUES
(1, 'admin', 'admin123', 'admin', 1, '系统管理员'),
(2, 'user',  'user123',  'user',  1, '测试用户');
