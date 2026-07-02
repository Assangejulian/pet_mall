-- ============================================
-- 宠物商店 - 完整建库建表脚本
-- 版本：v2.0（对照实际数据库结构修正）
-- 主键策略：雪花算法（IdType.ASSIGN_ID，Java 生成）
-- 字符集：utf8mb4 / utf8mb4_unicode_ci
-- ============================================

CREATE DATABASE IF NOT EXISTS pet_store DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE pet_store;

-- ============================================
-- 1. user — 用户（含管理员、商户）
-- ============================================
CREATE TABLE IF NOT EXISTS user (
    id           BIGINT        NOT NULL PRIMARY KEY COMMENT '雪花ID',
    username     VARCHAR(50)   NOT NULL COMMENT '登录名',
    password     VARCHAR(255)  NOT NULL COMMENT '密码',
    phone        VARCHAR(20)   COMMENT '手机号',
    avatar       VARCHAR(500)  COMMENT '头像',
    email        VARCHAR(100)  COMMENT '邮箱',
    openid       VARCHAR(100)  COMMENT '微信小程序 openid',
    face_id      VARCHAR(100)  COMMENT '人脸识别ID',
    unionid      VARCHAR(100)  COMMENT '微信开放平台 unionid',
    member_level TINYINT       NOT NULL DEFAULT 0 COMMENT '会员 0普通 1银卡 2金卡 3钻石',
    real_name    VARCHAR(50)   COMMENT '真实姓名',
    birthday     DATE          COMMENT '生日',
    role         VARCHAR(20)   NOT NULL DEFAULT 'user' COMMENT 'user / merchant / admin',
    status       TINYINT       NOT NULL DEFAULT 1 COMMENT '0禁用 1正常',
    create_time  DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    update_time  DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    UNIQUE INDEX idx_username (username),
    UNIQUE INDEX idx_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户';

-- ============================================
-- 2. user_address — 收货地址
-- ============================================
CREATE TABLE IF NOT EXISTS user_address (
    id            BIGINT       NOT NULL PRIMARY KEY COMMENT '雪花ID',
    user_id       BIGINT       NOT NULL COMMENT '用户ID',
    receiver_name VARCHAR(50)  NOT NULL COMMENT '收件人',
    phone         VARCHAR(20)  NOT NULL COMMENT '联系电话',
    province      VARCHAR(50)  COMMENT '省',
    city          VARCHAR(50)  COMMENT '市',
    district      VARCHAR(50)  COMMENT '区',
    detail        VARCHAR(200) NOT NULL COMMENT '详细地址',
    defaulted     TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否默认',
    create_time   DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    update_time   DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收货地址';

-- ============================================
-- 3. store — 店铺
-- ============================================
CREATE TABLE IF NOT EXISTS store (
    id          BIGINT        NOT NULL PRIMARY KEY COMMENT '雪花ID',
    user_id     BIGINT        NOT NULL COMMENT '店主',
    store_name  VARCHAR(100)  NOT NULL COMMENT '店铺名',
    store_logo  VARCHAR(500)  COMMENT 'Logo',
    store_phone VARCHAR(20)   COMMENT '联系电话',
    store_desc  TEXT          COMMENT '店铺描述',
    province    VARCHAR(50)   COMMENT '省',
    city        VARCHAR(50)   COMMENT '市',
    district    VARCHAR(50)   COMMENT '区',
    address     VARCHAR(200)  COMMENT '详细地址',
    longitude   DECIMAL(10,7) NOT NULL COMMENT '经度',
    latitude    DECIMAL(10,7) NOT NULL COMMENT '纬度',
    status      TINYINT       NOT NULL DEFAULT 0 COMMENT '0待审核 1营业中 2已关闭 3审核驳回',
    audit_user_id BIGINT      NULL COMMENT '审核人员ID',
    audit_time    DATETIME(3) NULL COMMENT '审核时间',
    audit_remark  VARCHAR(500) NULL COMMENT '审核意见或驳回原因',
    close_reason  VARCHAR(500) NULL COMMENT '关闭原因',
    deleted     TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    update_time DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    INDEX idx_user_id (user_id),
    INDEX idx_city (city),
    INDEX idx_location (longitude, latitude)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='店铺';

-- ============================================
-- 4. product — 商品
-- ============================================
CREATE TABLE IF NOT EXISTS product (
    id           BIGINT         NOT NULL PRIMARY KEY COMMENT '雪花ID',
    store_id     BIGINT         NOT NULL COMMENT '所属店铺',
    product_name VARCHAR(200)   NOT NULL COMMENT '商品名',
    product_type TINYINT        NOT NULL DEFAULT 1 COMMENT '1宠物 2周边',
    category     VARCHAR(50)    COMMENT '分类 dog/cat/other',
    product_desc TEXT           COMMENT '详情',
    price        DECIMAL(10,2)  NOT NULL COMMENT '价格',
    stock        INT            NOT NULL DEFAULT 1 COMMENT '库存',
    main_image   VARCHAR(500)   COMMENT '主图',
    images       JSON           COMMENT '多图JSON',
    status       TINYINT        NOT NULL DEFAULT 1 COMMENT '0下架 1上架 2已售出',
    video_id     BIGINT         COMMENT '关联视频',
    offline_reason  VARCHAR(500) NULL COMMENT '平台强制下架原因',
    offline_user_id BIGINT       NULL COMMENT '平台下架操作人员ID',
    offline_time    DATETIME(3)  NULL COMMENT '平台下架时间',
    deleted      TINYINT(1)     NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    create_time  DATETIME(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    update_time  DATETIME(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    INDEX idx_store_id (store_id),
    INDEX idx_product_type (product_type),
    INDEX idx_category (category),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品';

-- ============================================
-- 5. cart — 购物车
-- ============================================
CREATE TABLE IF NOT EXISTS cart (
    id          BIGINT      NOT NULL PRIMARY KEY COMMENT '雪花ID',
    user_id     BIGINT      NOT NULL COMMENT '用户ID',
    product_id  BIGINT      NOT NULL COMMENT '商品ID',
    quantity    INT         NOT NULL DEFAULT 1 COMMENT '数量',
    checked     TINYINT(1)  NOT NULL DEFAULT 1 COMMENT '是否选中',
    create_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    update_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    UNIQUE INDEX idx_user_product (user_id, product_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车';

-- ============================================
-- 6. purchase_order — 订单
-- ============================================
CREATE TABLE IF NOT EXISTS purchase_order (
    id                BIGINT         NOT NULL PRIMARY KEY COMMENT '雪花ID',
    order_no          VARCHAR(32)    NOT NULL COMMENT '订单号',
    user_id           BIGINT         NOT NULL COMMENT '用户ID',
    address_id        BIGINT         NOT NULL COMMENT '收货地址ID',
    address_snapshot  JSON           COMMENT '收货地址快照(JSON)',
    total_amount      DECIMAL(10,2)  NOT NULL COMMENT '总金额',
    discount_amount   DECIMAL(10,2)  NOT NULL DEFAULT 0.00 COMMENT '优惠金额',
    pay_amount        DECIMAL(10,2)  NOT NULL COMMENT '实付金额',
    remark            VARCHAR(200)   COMMENT '订单备注',
    order_status      INT            NOT NULL DEFAULT 0 COMMENT '0待支付 1已支付 2已发货 3已收货 4已评价 -1取消 -2申请退单 -3退单通过 -4管理员退单',
    cancel_reason     VARCHAR(200)   COMMENT '取消原因',
    cancel_time       DATETIME(3)    COMMENT '取消时间',
    pay_time          DATETIME(3)    COMMENT '支付时间',
    ship_time         DATETIME(3)    COMMENT '发货时间',
    receive_time      DATETIME(3)    COMMENT '收货时间',
    evaluate_time     DATETIME(3)    COMMENT '评价时间',
    refund_apply_time DATETIME(3)    COMMENT '退单申请时间',
    refund_audit_time DATETIME(3)    COMMENT '退单审核时间',
    create_time       DATETIME(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    update_time       DATETIME(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    UNIQUE INDEX idx_order_no (order_no),
    INDEX idx_user_id (user_id),
    INDEX idx_order_status (order_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单';

-- ============================================
-- 7. order_item — 订单项
-- ============================================
CREATE TABLE IF NOT EXISTS order_item (
    id               BIGINT         NOT NULL PRIMARY KEY COMMENT '雪花ID',
    order_id         BIGINT         NOT NULL COMMENT '订单ID',
    product_id       BIGINT         NOT NULL COMMENT '商品ID',
    product_name     VARCHAR(200)   NOT NULL COMMENT '商品名（快照）',
    product_image    VARCHAR(500)   COMMENT '商品图（快照）',
    price            DECIMAL(10,2)  NOT NULL COMMENT '购买时单价',
    quantity         INT            NOT NULL DEFAULT 1 COMMENT '数量',
    evaluate_content VARCHAR(500)   COMMENT '评价内容',
    evaluate_star    TINYINT        COMMENT '星级 1-5',
    evaluate_time    DATETIME(3)    COMMENT '评价时间',
    create_time      DATETIME(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    update_time      DATETIME(3)    COMMENT '更新时间',
    INDEX idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单项';

-- ============================================
-- 8. video — 视频
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

-- ============================================
-- 9. comment — 视频评论
-- ============================================
CREATE TABLE IF NOT EXISTS comment (
    id          BIGINT       NOT NULL PRIMARY KEY COMMENT '雪花ID',
    video_id    BIGINT       NOT NULL COMMENT '视频ID',
    user_id     BIGINT       NOT NULL COMMENT '评论者',
    content     VARCHAR(500) NOT NULL COMMENT '评论内容',
    status      TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '状态 0-隐藏 1-正常',
    deleted     TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除 0-正常 1-已删除',
    create_time DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    update_time DATETIME(3)  DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    delete_time DATETIME(3)  COMMENT '删除时间',
    create_by   BIGINT       COMMENT '创建人',
    update_by   BIGINT       COMMENT '更新人',
    INDEX idx_video_id (video_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视频评论';

-- ============================================
-- 10. ai_chat_record — AI对话记录
-- ============================================
CREATE TABLE IF NOT EXISTS ai_chat_record (
    id          BIGINT       NOT NULL PRIMARY KEY COMMENT '雪花ID',
    user_id     BIGINT       COMMENT '用户ID（游客可空）',
    session_id  VARCHAR(64)  NOT NULL COMMENT '会话ID',
    role        VARCHAR(20)  NOT NULL COMMENT 'user / assistant',
    content     TEXT         NOT NULL COMMENT '消息内容',
    create_time DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    INDEX idx_session_id (session_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI对话记录';

-- ============================================
-- 11. sys_message — 聊天 & 系统通知
-- ============================================
CREATE TABLE IF NOT EXISTS sys_message (
    id              BIGINT       NOT NULL PRIMARY KEY COMMENT '雪花ID',
    conversation_id VARCHAR(64)  NOT NULL COMMENT '会话ID',
    sender_id       BIGINT       NOT NULL COMMENT '发送人ID(0=系统)',
    receiver_id     BIGINT       NOT NULL COMMENT '接收人ID',
    content         TEXT         NOT NULL COMMENT '消息内容',
    type            TINYINT      NOT NULL COMMENT '类型 1-聊天 2-系统通知',
    readed          TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否已读',
    create_time     DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    INDEX idx_conversation_id (conversation_id),
    INDEX idx_receiver_id (receiver_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息(聊天+通知)';

-- ============================================
-- 预设数据
-- ============================================
INSERT IGNORE INTO user(id, username, password, role, status, real_name) VALUES
(1, 'admin', 'admin123', 'admin', 1, '系统管理员'),
(2, 'user',  'user123',  'user',  1, '测试用户');
