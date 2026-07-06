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

-- ============================================
-- 视频演示数据（与当前数据库修复结果同步）
-- ============================================
INSERT IGNORE INTO user(id, username, password, role, status, real_name) VALUES
(3, 'merchant1', '123456', 'merchant', 1, '暖窝小暖'),
(4, 'merchant2', '123456', 'merchant', 1, '猫咖日记'),
(5, 'merchant3', '123456', 'merchant', 1, '鱼乐无穷');

INSERT IGNORE INTO store (id, user_id, store_name, store_logo, store_phone, store_desc, province, city, district, address, longitude, latitude, status) VALUES
(1, 3, '暖窝·思明店', '/images/mock/cat-cover.jpg', '13800000001', '猫狗小宠一站式门店。', '福建省', '厦门市', '思明区', '中山路128号', 118.0805000, 24.4535000, 1),
(5, 3, '暖窝·同安店', '/images/mock/blue-cat.jpg', '13800000005', '兔子、仓鼠、龙猫等小宠专卖。', '福建省', '厦门市', '同安区', '城南路88号', 118.1540000, 24.7240000, 1),
(6, 5, '暖窝·水族馆', '/images/mock/dog-avatar.jpg', '13800000006', '水族宠物和鱼缸造景专门店。', '福建省', '厦门市', '海沧区', '滨湖路66号', 118.0310000, 24.4840000, 1);

INSERT IGNORE INTO product (id, store_id, product_name, product_type, category, product_desc, price, stock, main_image, images, status, video_id) VALUES
(1, 1, '金毛幼犬', 1, 'dog', '温顺活泼，已完成基础驱虫。', 1888.00, 1, '/images/mock/golden.jpg', '["/images/mock/golden.jpg"]', 1, 5),
(2, 1, '英短蓝猫', 1, 'cat', '包子脸，性格粘人，适合家庭陪伴。', 2580.00, 1, '/images/mock/blue-cat.jpg', '["/images/mock/blue-cat.jpg"]', 1, 1),
(3, 1, '柯基犬', 1, 'dog', '短腿柯基，活泼亲人。', 3200.00, 1, '/images/mock/corgi.jpg', '["/images/mock/corgi.jpg"]', 1, 4),
(4, 1, '布偶猫', 1, 'cat', '海豹双色布偶，性格稳定。', 4500.00, 1, '/images/mock/ragdoll.jpg', '["/images/mock/ragdoll.jpg"]', 1, 2),
(14, 1, '比格犬', 1, 'dog', '精力充沛，适合喜欢户外活动的家庭。', 2800.00, 1, '/images/mock/dog-avatar.jpg', '["/images/mock/dog-avatar.jpg"]', 1, 6),
(22, 5, '龙猫·团团', 1, 'other', '圆脸龙猫，性格温和。', 899.00, 1, '/images/mock/blue-cat.jpg', '["/images/mock/blue-cat.jpg"]', 1, 7),
(24, 6, '神仙鱼·熊猫', 1, 'fish', '熊猫神仙鱼，体长4-5cm，温顺群游。', 38.00, 30, '/images/mock/cat-cover.jpg', '["/images/mock/cat-cover.jpg"]', 1, 8);

INSERT IGNORE INTO video (id, user_id, title, description, url, cover, product_id, play_count, likes, comment_count, duration, status) VALUES
(1, 2, '黑白猫的镜头日常', '黑白小猫在镜头前放松伸展，适合慢慢看的一段陪伴视频。', 'http://video.sonetto.online/Black-and-white_cat_video_202607022313.mp4', '/images/mock/cat-cover.jpg', 2, 12000, 2300, 2, 45, 1),
(2, 2, '猫咪下巴挠挠时刻', '室内猫咪被轻轻挠下巴，表情很放松。', 'http://video.sonetto.online/Cat_chin_scratch_indoor_video_202607022322.mp4', '/images/mock/ragdoll.jpg', 4, 35000, 5100, 0, 60, 1),
(3, 2, '猫咪小跑上楼梯', '猫咪轻快地一路小跑上楼，动作灵活又可爱。', 'http://video.sonetto.online/Cat_trotting_up_stairs_202607022329.mp4', '/images/mock/cat-avatar.jpg', 2, 8500, 1800, 0, 30, 1),
(4, 2, '柯基毯上乖坐', '柯基坐在毯子上看镜头，短腿和圆脸都很治愈。', 'http://video.sonetto.online/Corgi_sitting_on_blanket_202607022304.mp4', '/images/mock/corgi.jpg', 3, 22000, 3600, 2, 50, 1),
(5, 2, '金毛叼着郁金香', '金毛叼着花靠近镜头，温柔又有春天感。', 'http://video.sonetto.online/Golden_retriever_holding_tulip_g%E2%80%A6_202607022323.mp4', '/images/mock/golden.jpg', 1, 5000, 980, 0, 35, 1),
(6, 2, '开心比格犬户外跑跳', '比格犬在户外开心活动，适合喜欢活泼狗狗的用户。', 'http://video.sonetto.online/Happy_beagle_dog_outdoors_202607022337.mp4', '/images/mock/dog-avatar.jpg', 14, 6800, 1200, 4, 55, 1),
(7, 2, '橘猫木桌观察日记', '橘猫趴在木桌上观察周围，节奏安静又舒服。', 'http://video.sonetto.online/Orange_cat_on_wooden_table_202607022346.mp4', '/images/mock/blue-cat.jpg', 2, 9200, 1800, 2, 40, 1),
(8, 3, '神仙鱼群游好治愈', '看着它们在鱼缸里慢慢游，心情都平静了。', '', '/images/mock/cat-cover.jpg', 24, 3500, 680, 2, 30, 0);

UPDATE video SET title = '黑白猫的镜头日常', description = '黑白小猫在镜头前放松伸展，适合慢慢看的一段陪伴视频。', url = 'http://video.sonetto.online/Black-and-white_cat_video_202607022313.mp4', cover = '/images/mock/cat-cover.jpg', product_id = 2, status = 1 WHERE id = 1;
UPDATE video SET title = '猫咪下巴挠挠时刻', description = '室内猫咪被轻轻挠下巴，表情很放松。', url = 'http://video.sonetto.online/Cat_chin_scratch_indoor_video_202607022322.mp4', cover = '/images/mock/ragdoll.jpg', product_id = 4, status = 1 WHERE id = 2;
UPDATE video SET title = '猫咪小跑上楼梯', description = '猫咪轻快地一路小跑上楼，动作灵活又可爱。', url = 'http://video.sonetto.online/Cat_trotting_up_stairs_202607022329.mp4', cover = '/images/mock/cat-avatar.jpg', product_id = 2, status = 1 WHERE id = 3;
UPDATE video SET title = '柯基毯上乖坐', description = '柯基坐在毯子上看镜头，短腿和圆脸都很治愈。', url = 'http://video.sonetto.online/Corgi_sitting_on_blanket_202607022304.mp4', cover = '/images/mock/corgi.jpg', product_id = 3, status = 1 WHERE id = 4;
UPDATE video SET title = '金毛叼着郁金香', description = '金毛叼着花靠近镜头，温柔又有春天感。', url = 'http://video.sonetto.online/Golden_retriever_holding_tulip_g%E2%80%A6_202607022323.mp4', cover = '/images/mock/golden.jpg', product_id = 1, status = 1 WHERE id = 5;
UPDATE video SET title = '开心比格犬户外跑跳', description = '比格犬在户外开心活动，适合喜欢活泼狗狗的用户。', url = 'http://video.sonetto.online/Happy_beagle_dog_outdoors_202607022337.mp4', cover = '/images/mock/dog-avatar.jpg', product_id = 14, status = 1 WHERE id = 6;
UPDATE video SET title = '橘猫木桌观察日记', description = '橘猫趴在木桌上观察周围，节奏安静又舒服。', url = 'http://video.sonetto.online/Orange_cat_on_wooden_table_202607022346.mp4', cover = '/images/mock/blue-cat.jpg', product_id = 2, status = 1 WHERE id = 7;
UPDATE video SET status = 0 WHERE id = 8;

INSERT IGNORE INTO comment (id, video_id, user_id, content) VALUES
(1, 1, 4, '太可爱了吧！每天都想看'),
(2, 1, 3, '同款在哪里买的呢？'),
(3, 4, 4, '这个镜头太治愈了，猫咪状态很放松。'),
(4, 4, 2, '我家也有同款！看起来特别亲人。'),
(5, 6, 4, '户外跑跳这一段很有活力。'),
(6, 6, 2, '习惯了就好，狗狗看起来很开心。'),
(7, 6, 3, '我家猫看了表示很感兴趣。'),
(8, 6, 3, '这个价格含训练课程吗'),
(9, 7, 2, '龙猫好圆啊！请问在哪里买的'),
(10, 7, 4, '手感一定很棒吧'),
(11, 8, 2, '好漂亮的神仙鱼'),
(12, 8, 5, '鱼缸造景也很好看');

UPDATE comment SET video_id = 1, user_id = 4, content = '太可爱了吧！每天都想看' WHERE id = 1;
UPDATE comment SET video_id = 1, user_id = 3, content = '同款在哪里买的呢？' WHERE id = 2;
UPDATE comment SET video_id = 4, user_id = 4, content = '这个镜头太治愈了，猫咪状态很放松。' WHERE id = 3;
UPDATE comment SET video_id = 4, user_id = 2, content = '我家也有同款！看起来特别亲人。' WHERE id = 4;
UPDATE comment SET video_id = 6, user_id = 4, content = '户外跑跳这一段很有活力。' WHERE id = 5;
UPDATE comment SET video_id = 6, user_id = 2, content = '习惯了就好，狗狗看起来很开心。' WHERE id = 6;
UPDATE comment SET video_id = 6, user_id = 3, content = '我家猫看了表示很感兴趣。' WHERE id = 7;
UPDATE comment SET video_id = 6, user_id = 3, content = '这个价格含训练课程吗' WHERE id = 8;
UPDATE comment SET video_id = 7, user_id = 2, content = '龙猫好圆啊！请问在哪里买的' WHERE id = 9;
UPDATE comment SET video_id = 7, user_id = 4, content = '手感一定很棒吧' WHERE id = 10;
UPDATE comment SET video_id = 8, user_id = 2, content = '好漂亮的神仙鱼' WHERE id = 11;
UPDATE comment SET video_id = 8, user_id = 5, content = '鱼缸造景也很好看' WHERE id = 12;

UPDATE video v
LEFT JOIN (
    SELECT video_id, COUNT(*) AS cnt
    FROM comment
    WHERE deleted = 0 AND status = 1
    GROUP BY video_id
) c ON c.video_id = v.id
SET v.comment_count = COALESCE(c.cnt, 0);



ALTER TABLE purchase_order ADD COLUMN cancel_type VARCHAR(20) DEFAULT NULL COMMENT '取消类型' AFTER cancel_reason;
ALTER TABLE purchase_order ADD COLUMN pre_refund_status INT DEFAULT NULL COMMENT '退款前状态' AFTER cancel_time;