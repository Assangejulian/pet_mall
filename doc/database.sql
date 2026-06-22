-- ============================================
-- 宠物商店 - 数据库结构设计
-- 数据库名：pet_store
-- ============================================

CREATE DATABASE IF NOT EXISTS pet_store DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE pet_store;

-- ============================================
-- 1. 用户表（扩展现有 sys_user）
-- 在现有 user 表基础上添加会员等级字段
-- ============================================
ALTER TABLE user ADD COLUMN member_level TINYINT DEFAULT 0 COMMENT '会员等级 0-普通 1-银卡 2-金卡 3-钻石';
ALTER TABLE user ADD COLUMN real_name VARCHAR(50) COMMENT '真实姓名';
ALTER TABLE user ADD COLUMN birthday DATE COMMENT '生日';

-- ============================================
-- 2. 收货地址表
-- ============================================
CREATE TABLE user_address (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    receiver_name VARCHAR(50) NOT NULL COMMENT '收件人',
    phone VARCHAR(20) NOT NULL COMMENT '联系电话',
    province VARCHAR(50) COMMENT '省',
    city VARCHAR(50) COMMENT '市',
    district VARCHAR(50) COMMENT '区',
    detail VARCHAR(200) NOT NULL COMMENT '详细地址',
    is_default TINYINT DEFAULT 0 COMMENT '是否默认 0-否 1-是',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收货地址';

-- ============================================
-- 3. 商店表
-- ============================================
CREATE TABLE store (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    store_name VARCHAR(100) NOT NULL COMMENT '商店名称',
    store_logo VARCHAR(500) COMMENT '商店Logo',
    store_phone VARCHAR(20) COMMENT '联系电话',
    store_desc TEXT COMMENT '商店描述',
    province VARCHAR(50) COMMENT '省',
    city VARCHAR(50) COMMENT '市',
    district VARCHAR(50) COMMENT '区',
    address VARCHAR(200) COMMENT '详细地址',
    longitude DECIMAL(10,7) NOT NULL COMMENT '经度',
    latitude DECIMAL(10,7) NOT NULL COMMENT '纬度',
    status TINYINT DEFAULT 1 COMMENT '状态 0-关闭 1-营业',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_delete TINYINT DEFAULT 0 COMMENT '逻辑删除 0-未删 1-已删',
    INDEX idx_location (longitude, latitude),
    INDEX idx_city (city)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商店';

-- ============================================
-- 4. 商品表
-- ============================================
CREATE TABLE product (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    store_id BIGINT NOT NULL COMMENT '所属商店ID',
    product_name VARCHAR(200) NOT NULL COMMENT '商品名称',
    product_type TINYINT NOT NULL DEFAULT 1 COMMENT '商品类型 1-宠物(唯一) 2-宠物周边(不限量)',
    category VARCHAR(50) COMMENT '分类 狗/猫/鱼/鸟/其他',
    product_desc TEXT COMMENT '商品描述',
    price DECIMAL(10,2) NOT NULL COMMENT '原价',
    stock INT DEFAULT 1 COMMENT '库存(宠物为1，周边不限)',
    main_image VARCHAR(500) COMMENT '主图',
    images VARCHAR(2000) COMMENT '多图(JSON数组)',
    status TINYINT DEFAULT 1 COMMENT '状态 0-下架 1-上架 2-已售出',
    video_id BIGINT COMMENT '关联视频ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_delete TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_store_id (store_id),
    INDEX idx_type (product_type),
    INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品';

-- ============================================
-- 5. 购物车表
-- ============================================
CREATE TABLE cart (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    quantity INT NOT NULL DEFAULT 1 COMMENT '数量',
    checked TINYINT DEFAULT 1 COMMENT '是否选中 0-否 1-是',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_product (user_id, product_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车';

-- ============================================
-- 6. 订单表
-- ============================================
CREATE TABLE order_info (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(32) NOT NULL UNIQUE COMMENT '订单编号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    address_id BIGINT NOT NULL COMMENT '收货地址ID',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
    discount_amount DECIMAL(10,2) DEFAULT 0 COMMENT '优惠金额',
    pay_amount DECIMAL(10,2) NOT NULL COMMENT '实付金额',
    order_status INT NOT NULL DEFAULT 0 COMMENT '订单状态(见下方状态说明)',
    cancel_reason VARCHAR(200) COMMENT '取消/退单原因',
    cancel_time DATETIME COMMENT '取消时间',
    pay_time DATETIME COMMENT '支付时间',
    ship_time DATETIME COMMENT '发货时间',
    receive_time DATETIME COMMENT '收货时间',
    evaluate_time DATETIME COMMENT '评价时间',
    refund_apply_time DATETIME COMMENT '申请退单时间',
    refund_audit_time DATETIME COMMENT '退单审核时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_order_no (order_no),
    INDEX idx_status (order_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单';

/*
订单状态说明：
  0  - 已下单（待支付）        -- 前端用户操作
  1  - 已支付（待发货）        -- 前端用户操作
  2  - 已发货（待收货）        -- 管理员操作
  3  - 已收货（待评价）        -- 前端用户操作
  4  - 已评价                  -- 前端用户操作（结束）
 -1  - 取消订单                -- 仅0→1之间可取消（结束）
 -2  - 申请退单                -- 2→3之间可申请
 -3  - 退单审核通过            -- 管理员操作（结束）
 -4  - 管理员直接退单          -- 管理员操作（结束）
*/

-- ============================================
-- 7. 订单明细表
-- ============================================
CREATE TABLE order_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL COMMENT '订单ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    product_name VARCHAR(200) NOT NULL COMMENT '商品名称(快照)',
    product_image VARCHAR(500) COMMENT '商品主图(快照)',
    price DECIMAL(10,2) NOT NULL COMMENT '购买时单价',
    quantity INT NOT NULL DEFAULT 1 COMMENT '数量',
    evaluate_content VARCHAR(500) COMMENT '评价内容',
    evaluate_star TINYINT COMMENT '评价星级 1-5',
    evaluate_time DATETIME COMMENT '评价时间',
    INDEX idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细';

-- ============================================
-- 8. 视频表
-- ============================================
CREATE TABLE video (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL COMMENT '视频标题',
    video_url VARCHAR(500) NOT NULL COMMENT '视频地址',
    cover_url VARCHAR(500) COMMENT '封面图',
    product_id BIGINT COMMENT '关联商品ID',
    play_count INT DEFAULT 0 COMMENT '播放次数',
    status TINYINT DEFAULT 1 COMMENT '状态 0-下架 1-上架',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_delete TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视频';

-- ============================================
-- 9. AI对话记录表（可选）
-- ============================================
CREATE TABLE ai_chat_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT COMMENT '用户ID(游客可为空)',
    session_id VARCHAR(64) NOT NULL COMMENT '会话ID',
    role VARCHAR(20) NOT NULL COMMENT '角色 user/assistant',
    content TEXT NOT NULL COMMENT '消息内容',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_session_id (session_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI对话记录';
