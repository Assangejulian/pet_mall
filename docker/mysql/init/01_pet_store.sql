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
(1,  10, '金毛幼犬', 1, 'dog',   '纯种金毛，温顺可爱，已打疫苗',         1888.00, 1, 'https://images.unsplash.com/photo-1552053831-71594a27632d?w=800', 1, 1),
(2,  11, '英短蓝猫', 1, 'cat',   '包子脸，性格温顺粘人',                 2580.00, 1, 'https://images.unsplash.com/photo-1573865526739-10659fec78a5?w=800', 1, 2),
(3,  10, '柯基犬',   1, 'dog',   '小短腿，活泼可爱',                     3200.00, 1, 'https://images.unsplash.com/photo-1612536057832-2ff7ead58194?w=800', 1, 3),
(4,  11, '布偶猫',   1, 'cat',   '仙女猫本仙，颜值担当',                 4500.00, 1, 'https://images.unsplash.com/photo-1513360371669-4adf3dd7dff8?w=800', 1, 4),
(5,  10, '仓鼠',     1, 'other', '迷你小可爱，容易饲养',                   38.00, 1, 'https://images.unsplash.com/photo-1425082661705-1834bfd09dca?w=800', 1, 5),
(6,  11, '哈士奇',   1, 'dog',   '拆迁办主任，搞笑担当',                 2200.00, 1, 'https://images.unsplash.com/photo-1605568427561-40dd23c2acea?w=800', 1, 6);

UPDATE product SET stock = 3, status = 1 WHERE id = 1 AND product_name = '金毛幼犬';
UPDATE product SET stock = 2, status = 1 WHERE id = 4 AND product_name = '布偶猫';

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

-- ============================================
-- 补充种子数据（商户订单演示用）
-- ============================================

-- ========== 补充 user ==========
INSERT IGNORE INTO user (id, username, password, phone, avatar, email, member_level, real_name, birthday, role, status) VALUES
(5, 'merchant3', '123456', '13800003333', 'https://images.unsplash.com/photo-1583337130417-3346c1be7dee?w=200', NULL, 0, '鱼乐无穷', NULL, 'merchant', 1),
(6, 'user2', '123456', '13600001111', 'https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=200', 'user2@test.com', 1, '李四', '1998-08-08', 'user', 1),
(7, 'user3', '123456', '13600002222', 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=200', 'user3@test.com', 2, '王五', '2000-01-15', 'user', 1);

-- ========== 补充 user_address ==========
INSERT IGNORE INTO user_address(id, user_id, receiver_name, phone, province, city, district, detail, defaulted) VALUES
(2, 6, '李四', '13600001111', '福建省', '厦门市', '湖里区', '湖里大道88号', 1),
(3, 6, '李四', '13600001112', '福建省', '厦门市', '思明区', '厦禾路200号', 0),
(4, 7, '王五', '13600002222', '福建省', '厦门市', '集美区', '杏林湾路1号', 1);

-- ========== 补充 store ==========
INSERT IGNORE INTO store(id, user_id, store_name, store_logo, store_phone, store_desc, province, city, district, address, longitude, latitude, status) VALUES
(12, 5, '鱼乐无穷·水族馆', 'https://images.unsplash.com/photo-1520366498724-709889c0c685?w=200', '13800000006', '专注观赏鱼、水族造景，厦门最大水族馆。', '福建省', '厦门市', '思明区', '环岛路300号', 118.1200000, 24.4600000, 1);

-- ========== 补充 product ==========
INSERT IGNORE INTO product(id, store_id, product_name, product_type, category, product_desc, price, stock, main_image, status, video_id) VALUES
(7,  10, '贵宾幼犬·奶茶',  1, 'dog',   '玩具贵宾，1.5kg迷你体，已打疫苗。',       2800.00, 1, 'https://images.unsplash.com/photo-1615469031033-23db999e47ec?w=800', 1, NULL),
(8,  10, '宠物窝垫 M号',   2, 'accessory', '四季通用宠物窝，可拆洗，柔软保暖。',      89.00, 50, 'https://images.unsplash.com/photo-1516734212186-a967f81ad0d7?w=800', 1, NULL),
(9,  10, '仓鼠套餐·豪华版',2, 'other',  '含笼子+跑轮+食盆+木屑+粮食，新手一站式。', 198.00, 20, 'https://images.unsplash.com/photo-1425082661705-1834bfd09dca?w=800', 1, NULL),
(10, 10, '龙猫·银斑',      1, 'other',  '银斑龙猫，2个月大，毛色漂亮，温顺亲人。',   1280.00, 1, 'https://images.unsplash.com/photo-1585110396000-c9ffd4e4b308?w=800', 1, NULL),
(11, 11, '泰迪犬·巧克力',  1, 'dog',   '迷你泰迪，2个月大，已驱虫，聪明好训练。',   2600.00, 1, 'https://images.unsplash.com/photo-1534361960057-19889db9621e?w=800', 1, NULL),
(12, 11, '猫抓板·仙人掌',  2, 'accessory', '网红猫抓板，剑麻材质，耐抓不掉屑。',        49.00, 40, 'https://images.unsplash.com/photo-1601758228041-f3b2795255f1?w=800', 1, NULL),
(13, 12, '神仙鱼·熊猫',    1, 'fish',  '熊猫神仙鱼，体长4-5cm，温顺群游。',           38.00, 30, 'https://images.unsplash.com/photo-1520366498724-709889c0c685?w=800', 1, NULL),
(14, 12, '水族箱 60cm',     2, 'accessory', '超白玻璃鱼缸，60x30x36cm，含过滤系统。',  399.00, 10, 'https://images.unsplash.com/photo-1535591273668-578e31182c4f?w=800', 1, NULL),
(15, 12, '斗鱼·半月',      1, 'fish',  '半月斗鱼，泰国进口，大尾展，色彩艳丽。',        88.00,  8, 'https://images.unsplash.com/photo-1535591273668-578e31182c4f?w=800', 1, NULL),
(16, 12, '鱼粮套餐',        2, 'food',  '热带鱼粮+金鱼粮+底栖鱼粮，三瓶装。',            45.00, 60, 'https://images.unsplash.com/photo-1589924691995-400dc9ecc119?w=800', 1, NULL);

-- ========== 补充 video ==========
INSERT IGNORE INTO video(id, user_id, title, description, url, cover, product_id, play_count, likes, comment_count, duration, status) VALUES
(7, 2, '贵宾犬的才艺表演', '坐下趴下握手装死，样样精通的小机灵。', '/video/7.mp4', 'https://images.unsplash.com/photo-1615469031033-23db999e47ec?w=800', 7, 6800, 1200, 2, 55, 1),
(8, 3, '神仙鱼群游好治愈', '看着它们在鱼缸里慢慢游，心情都平静了。', '/video/8.mp4', 'https://images.unsplash.com/photo-1520366498724-709889c0c685?w=800', 13, 3500, 680, 45, 30, 1);

-- ========== 补充 comment ==========
INSERT IGNORE INTO comment(id, video_id, user_id, content) VALUES
(8, 7, 4, '太聪明了！想养一只'),
(9, 7, 3, '这个价格含训练课程吗'),
(10, 8, 4, '好漂亮的神仙鱼'),
(11, 8, 5, '鱼缸造景也很好看');

-- ========== 补充 purchase_order（覆盖多商户店铺） ==========
INSERT IGNORE INTO purchase_order(id, order_no, user_id, address_id, total_amount, discount_amount, pay_amount, order_status, remark, pay_time, ship_time, receive_time, create_time) VALUES
-- 订单5：暖窝(store10)商品 → merchant1 可见，已支付待发货
(5, 'PO20260625005', 6, 2, 2649.00, 0.00, 2649.00, 1, '请尽快发货哦~', '2026-06-25 10:30:00', NULL, NULL, '2026-06-25 10:25:00'),
-- 订单6：猫咖日记(store11)商品 → merchant2 可见，已收货已评价
(6, 'PO20260620006', 6, 2, 2649.00, 0.00, 2649.00, 4, NULL, '2026-06-20 15:00:00', '2026-06-21 09:00:00', '2026-06-23 11:00:00', '2026-06-20 14:55:00'),
-- 订单7：鱼乐无穷(store12)商品 → merchant3 可见，已发货
(7, 'PO20260624007', 7, 4, 525.00, 0.00, 525.00, 2, '鱼缸请小心轻放', '2026-06-24 09:00:00', '2026-06-25 08:30:00', NULL, '2026-06-24 08:50:00'),
-- 订单8：暖窝(store10)商品 → merchant1 可见，已取消
(8, 'PO20260622008', 7, 4, 89.00, 0.00, 89.00, -1, '不想要了', NULL, NULL, NULL, '2026-06-22 16:00:00'),
-- 订单9：猫咖日记(store11)商品 → merchant2 可见，申请退单
(9, 'PO20260619009', 4, 1, 2600.00, 0.00, 2600.00, -2, '猫咪到家后有点应激反应', '2026-06-19 09:00:00', '2026-06-20 10:00:00', '2026-06-22 14:00:00', '2026-06-19 08:55:00'),
-- 订单10：鱼乐无穷(store12)商品 → merchant3 可见，待支付
(10, 'PO20260627010', 4, 1, 133.00, 0.00, 133.00, 0, NULL, NULL, NULL, NULL, '2026-06-27 14:00:00');

-- ========== 补充 order_item ==========
INSERT IGNORE INTO order_item(id, order_id, product_id, product_name, product_image, price, quantity) VALUES
-- 订单5：贵宾(store10) + 仓鼠套餐(store10) → merchant1 可见
(5, 5, 7, '贵宾幼犬·奶茶', 'https://images.unsplash.com/photo-1615469031033-23db999e47ec?w=200', 2800.00, 1),
(6, 5, 9, '仓鼠套餐·豪华版', 'https://images.unsplash.com/photo-1425082661705-1834bfd09dca?w=200', 198.00, 1),
-- 订单6：泰迪(store11) + 猫抓板(store11) → merchant2 可见
(7, 6, 11, '泰迪犬·巧克力', 'https://images.unsplash.com/photo-1534361960057-19889db9621e?w=200', 2600.00, 1),
(8, 6, 12, '猫抓板·仙人掌', 'https://images.unsplash.com/photo-1601758228041-f3b2795255f1?w=200', 49.00, 1),
-- 订单7：神仙鱼×3(store12) + 水族箱(store12) → merchant3 可见
(9, 7, 13, '神仙鱼·熊猫', 'https://images.unsplash.com/photo-1520366498724-709889c0c685?w=200', 38.00, 3),
(10, 7, 14, '水族箱 60cm', 'https://images.unsplash.com/photo-1535591273668-578e31182c4f?w=200', 399.00, 1),
-- 订单8：宠物窝垫(store10) → merchant1 可见
(11, 8, 8, '宠物窝垫 M号', 'https://images.unsplash.com/photo-1516734212186-a967f81ad0d7?w=200', 89.00, 1),
-- 订单9：泰迪(store11) → merchant2 可见
(12, 9, 11, '泰迪犬·巧克力', 'https://images.unsplash.com/photo-1534361960057-19889db9621e?w=200', 2600.00, 1),
-- 订单10：斗鱼(store12) + 鱼粮(store12) → merchant3 可见
(13, 10, 15, '斗鱼·半月', 'https://images.unsplash.com/photo-1535591273668-578e31182c4f?w=200', 88.00, 1),
(14, 10, 16, '鱼粮套餐', 'https://images.unsplash.com/photo-1589924691995-400dc9ecc119?w=200', 45.00, 1);

-- ========== 补充 evaluate ==========
UPDATE order_item SET evaluate_content = '泰迪犬很可爱，健康活泼，物流也很快！', evaluate_star = 5, evaluate_time = '2026-06-25 10:00:00' WHERE id = 5;
UPDATE order_item SET evaluate_content = '仓鼠套餐很齐全，小朋友很喜欢', evaluate_star = 4, evaluate_time = '2026-06-25 10:05:00' WHERE id = 6;
UPDATE order_item SET evaluate_content = '狗狗很健康，超级聪明！', evaluate_star = 5, evaluate_time = '2026-06-24 12:00:00' WHERE id = 7;
UPDATE order_item SET evaluate_content = '猫抓板质量不错，猫咪很喜欢', evaluate_star = 4, evaluate_time = '2026-06-24 12:05:00' WHERE id = 8;

UPDATE video v
LEFT JOIN (
    SELECT video_id, COUNT(*) AS cnt
    FROM comment
    GROUP BY video_id
) c ON c.video_id = v.id
LEFT JOIN (
    SELECT product_id, COUNT(*) AS cnt
    FROM order_item
    WHERE evaluate_content IS NOT NULL
    GROUP BY product_id
) r ON r.product_id = v.product_id
SET v.comment_count = COALESCE(c.cnt, 0) + COALESCE(r.cnt, 0);

-- ========== 补充 addressSnapshot（所有订单的收货地址快照） ==========
UPDATE purchase_order SET address_snapshot = '{"receiverName":"测试用户","phone":"13900139000","province":"福建省","city":"厦门市","district":"集美区","detail":"理工路600号"}' WHERE id IN (1,2,3,4,9,10);
UPDATE purchase_order SET address_snapshot = '{"receiverName":"李四","phone":"13600001111","province":"福建省","city":"厦门市","district":"湖里区","detail":"湖里大道88号"}' WHERE id IN (5,6);
UPDATE purchase_order SET address_snapshot = '{"receiverName":"王五","phone":"13600002222","province":"福建省","city":"厦门市","district":"集美区","detail":"杏林湾路1号"}' WHERE id IN (7,8);
