-- ============================================
-- 宠物商店 - Docker 初始化脚本 v2.0
-- 数据库：pet_store / 字符集：utf8mb4
-- 主键：雪花算法 BIGINT（MyBatis-Plus ASSIGN_ID）
-- 说明：11 张表，注释已修正，含预设数据
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
    status      TINYINT       NOT NULL DEFAULT 0 COMMENT '0待审核 1营业中 2已关闭',
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

-- user
INSERT IGNORE INTO user(id, username, password, phone, avatar, email, member_level, real_name, birthday, role, status) VALUES
(1, 'admin',     'admin123',  '13800138000', 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=100', 'admin@petstore.com', 3, '系统管理员', '1990-01-01', 'admin',    1),
(2, 'merchant1', '123456',    '13800001111', 'https://images.unsplash.com/photo-1543466835-00a7907e9de1?w=100', NULL,                 0, '暖窝小暖',   NULL,        'merchant', 1),
(3, 'merchant2', '123456',    '13800002222', 'https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?w=100', NULL,                 0, '猫咖日记',   NULL,        'merchant', 1),
(4, 'user1',     '123456',    '13900139000', 'https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=100', 'user@petstore.com',  1, '测试用户',   '1995-06-15', 'user',     1);

-- user_address
INSERT IGNORE INTO user_address(id, user_id, receiver_name, phone, province, city, district, detail, defaulted) VALUES
(1, 4, '测试用户', '13900139000', '福建省', '厦门市', '集美区', '理工路600号', 1);

-- store
INSERT IGNORE INTO store(id, user_id, store_name, store_logo, store_phone, store_desc, province, city, district, address, longitude, latitude, status) VALUES
(10, 2, '暖窝宠物馆', 'https://images.unsplash.com/photo-1543466835-00a7907e9de1?w=200', '13800001111', '专注金毛繁育，健康保障',          '福建省', '厦门市', '集美区', '理工路600号', 118.1000000, 24.6000000, 1),
(11, 3, '猫咖日记',   'https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?w=200', '13800002222', '英短蓝猫、布偶猫，品相极佳',      '福建省', '厦门市', '思明区', '中山路200号', 118.0800000, 24.4500000, 1);

-- product
INSERT IGNORE INTO product(id, store_id, product_name, product_type, category, product_desc, price, stock, main_image, status, video_id) VALUES
(1,  10, '金毛幼犬', 1, 'dog',   '纯种金毛，温顺可爱，已打疫苗',         1888.00, 5,  'https://images.unsplash.com/photo-1552053831-71594a27632d?w=800', 1, 1),
(2,  11, '英短蓝猫', 1, 'cat',   '包子脸，性格温顺粘人',                 2580.00, 3,  'https://images.unsplash.com/photo-1573865526739-10659fec78a5?w=800', 1, 2),
(3,  10, '柯基犬',   1, 'dog',   '小短腿，活泼可爱',                     3200.00, 2,  'https://images.unsplash.com/photo-1612536057832-2ff7ead58194?w=800', 1, 3),
(4,  11, '布偶猫',   1, 'cat',   '仙女猫本仙，颜值担当',                 4500.00, 2,  'https://images.unsplash.com/photo-1513360371669-4adf3dd7dff8?w=800', 1, 4),
(5,  10, '仓鼠',     1, 'other', '迷你小可爱，容易饲养',                   38.00, 20, 'https://images.unsplash.com/photo-1425082661705-1834bfd09dca?w=800', 1, 5),
(6,  11, '哈士奇',   1, 'dog',   '拆迁办主任，搞笑担当',                 2200.00, 3,  'https://images.unsplash.com/photo-1605568427561-40dd23c2acea?w=800', 1, 6);

-- video
INSERT IGNORE INTO video(id, user_id, title, description, url, cover, product_id, play_count, likes, comment_count, duration, status) VALUES
(1, 2, '金毛幼犬的日常撒娇', '每天早上都会叼着拖鞋来叫醒我，太治愈了',       '/video/1.mp4', 'https://images.unsplash.com/photo-1552053831-71594a27632d?w=800', 1, 2300, 156, 2,  45, 1),
(2, 3, '英短蓝猫卖萌合集',   '包子脸的终极奥义就是装无辜',                   '/video/2.mp4', 'https://images.unsplash.com/photo-1573865526739-10659fec78a5?w=800', 2, 5100, 432, 1,  60, 1),
(3, 2, '柯基小短腿赛跑大赛', '腿虽短但跑起来谁也不服',                       '/video/3.mp4', 'https://images.unsplash.com/photo-1612536057832-2ff7ead58194?w=800', 3, 1800, 89,  0,  30, 1),
(4, 3, '布偶猫的仙女日常',   '每天醒来看到这张脸，感觉世界都温柔了',         '/video/4.mp4', 'https://images.unsplash.com/photo-1513360371669-4adf3dd7dff8?w=800', 4, 3600, 278, 2,  50, 1),
(5, 2, '仓鼠跑轮停不下来',   '跑了一小时还在跑，这体力我服',                 '/video/5.mp4', 'https://images.unsplash.com/photo-1425082661705-1834bfd09dca?w=800', 5, 980,  45,  1,  35, 1),
(6, 3, '哈士奇拆家实况',     '出门两小时回来沙发没了，微笑面对',             '/video/6.mp4', 'https://images.unsplash.com/photo-1605568427561-40dd23c2acea?w=800', 6, 4200, 567, 3,  40, 1);

-- comment
INSERT IGNORE INTO comment(id, video_id, user_id, content) VALUES
(1, 1, 4, '太可爱了吧！每天都想看'),
(2, 1, 3, '同款在哪里买的呀？'),
(3, 4, 4, '哈哈哈哈太治愈了'),
(4, 4, 2, '我家也有同款！'),
(5, 6, 4, '拆家小能手哈哈'),
(6, 6, 2, '习惯了就好...'),
(7, 6, 3, '我家猫看了表示不屑');

-- purchase_order
INSERT IGNORE INTO purchase_order(id, order_no, user_id, address_id, total_amount, discount_amount, pay_amount, order_status, pay_time, ship_time, receive_time) VALUES
(1, 'PO20260624001', 4, 1, 1888.00, 0.00, 1888.00, 0, '2026-06-24 12:00:00', NULL, NULL),
(2, 'PO20260623002', 4, 1, 2580.00, 0.00, 2580.00, 1, '2026-06-23 10:00:00', '2026-06-24 08:00:00', NULL),
(3, 'PO20260622003', 4, 1, 3200.00, 0.00, 3200.00, 2, '2026-06-22 08:00:00', '2026-06-23 10:00:00', '2026-06-24 14:00:00'),
(4, 'PO20260620004', 4, 1, 4500.00, 0.00, 4500.00, 4, '2026-06-20 14:00:00', '2026-06-21 10:00:00', '2026-06-23 18:00:00');

-- order_item
INSERT IGNORE INTO order_item(id, order_id, product_id, product_name, product_image, price, quantity) VALUES
(1, 1, 1, '金毛幼犬', 'https://images.unsplash.com/photo-1552053831-71594a27632d?w=200', 1888.00, 1),
(2, 2, 2, '英短蓝猫', 'https://images.unsplash.com/photo-1573865526739-10659fec78a5?w=200', 2580.00, 1),
(3, 3, 3, '柯基犬',   'https://images.unsplash.com/photo-1612536057832-2ff7ead58194?w=200', 3200.00, 1),
(4, 4, 4, '布偶猫',   'https://images.unsplash.com/photo-1513360371669-4adf3dd7dff8?w=200', 4500.00, 1);
