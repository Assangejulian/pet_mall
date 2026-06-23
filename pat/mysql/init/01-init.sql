-- ============================================
-- 宠物商店 - 完整建库建表脚本
-- 数据库名：pet_store
-- 字符集：utf8mb4
-- 主键策略：雪花算法（数据库不生成，由 Java IdType.ASSIGN_ID 负责）
-- 逻辑删除：deleted TINYINT(1) DEFAULT 0
-- ============================================

CREATE DATABASE IF NOT EXISTS pet_store DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE pet_store;

-- ============================================
-- 1. user — 用户
-- ============================================
CREATE TABLE user (
    id BIGINT NOT NULL PRIMARY KEY COMMENT '雪花ID',
    username VARCHAR(50) NOT NULL COMMENT '登录名',
    password VARCHAR(255) NOT NULL COMMENT '密码（加密）',
    phone VARCHAR(20) COMMENT '手机号',
    avatar VARCHAR(500) COMMENT '头像',
    email VARCHAR(100) COMMENT '邮箱',
    member_level TINYINT DEFAULT 0 COMMENT '会员等级 0-普通 1-银卡 2-金卡 3-钻石',
    real_name VARCHAR(50) COMMENT '真实姓名',
    birthday DATE COMMENT '生日',
    status TINYINT DEFAULT 1 COMMENT '状态 0-禁用 1-正常',
    create_time DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3),
    update_time DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    UNIQUE INDEX idx_username (username),
    UNIQUE INDEX idx_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户';

-- ============================================
-- 2. user_address — 收货地址
-- ============================================
CREATE TABLE user_address (
    id BIGINT NOT NULL PRIMARY KEY COMMENT '雪花ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    receiver_name VARCHAR(50) NOT NULL COMMENT '收件人',
    phone VARCHAR(20) NOT NULL COMMENT '联系电话',
    province VARCHAR(50) COMMENT '省',
    city VARCHAR(50) COMMENT '市',
    district VARCHAR(50) COMMENT '区',
    detail VARCHAR(200) NOT NULL COMMENT '详细地址',
    defaulted TINYINT(1) DEFAULT 0 COMMENT '是否默认 0-否 1-是',
    create_time DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3),
    update_time DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收货地址';

-- ============================================
-- 3. store — 商店（商户）
-- ============================================
CREATE TABLE store (
    id BIGINT NOT NULL PRIMARY KEY COMMENT '雪花ID',
    user_id BIGINT NOT NULL COMMENT '店主（关联 user）',
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
    status TINYINT DEFAULT 0 COMMENT '状态 0-待审核 1-营业中 2-已关闭',
    deleted TINYINT(1) DEFAULT 0 COMMENT '逻辑删除 0-未删 1-已删',
    create_time DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3),
    update_time DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    INDEX idx_user_id (user_id),
    INDEX idx_city (city),
    INDEX idx_location (longitude, latitude)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商店';

-- ============================================
-- 4. product — 商品
-- ============================================
CREATE TABLE product (
    id BIGINT NOT NULL PRIMARY KEY COMMENT '雪花ID',
    store_id BIGINT NOT NULL COMMENT '所属商店ID',
    product_name VARCHAR(200) NOT NULL COMMENT '商品名称',
    product_type TINYINT NOT NULL DEFAULT 1 COMMENT '商品类型 1-宠物 2-周边',
    category VARCHAR(50) COMMENT '分类 狗/猫/鱼/鸟/其他',
    product_desc TEXT COMMENT '商品描述',
    price DECIMAL(10,2) NOT NULL COMMENT '价格',
    stock INT DEFAULT 1 COMMENT '库存',
    main_image VARCHAR(500) COMMENT '主图',
    images VARCHAR(2000) COMMENT '多图(JSON数组)',
    status TINYINT DEFAULT 1 COMMENT '状态 0-下架 1-上架 2-已售出',
    video_id BIGINT COMMENT '关联视频ID',
    deleted TINYINT(1) DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3),
    update_time DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    INDEX idx_store_id (store_id),
    INDEX idx_type (product_type),
    INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品';

-- ============================================
-- 5. cart — 购物车
-- ============================================
CREATE TABLE cart (
    id BIGINT NOT NULL PRIMARY KEY COMMENT '雪花ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    quantity INT NOT NULL DEFAULT 1 COMMENT '数量',
    checked TINYINT(1) DEFAULT 1 COMMENT '是否选中 0-否 1-是',
    create_time DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3),
    update_time DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    UNIQUE KEY uk_user_product (user_id, product_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车';

-- ============================================
-- 6. purchase_order — 订单
-- ============================================
CREATE TABLE purchase_order (
    id BIGINT NOT NULL PRIMARY KEY COMMENT '雪花ID',
    order_no VARCHAR(32) NOT NULL COMMENT '订单编号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    address_id BIGINT NOT NULL COMMENT '收货地址ID',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
    discount_amount DECIMAL(10,2) DEFAULT 0 COMMENT '优惠金额',
    pay_amount DECIMAL(10,2) NOT NULL COMMENT '实付金额',
    order_status INT NOT NULL DEFAULT 0 COMMENT '订单状态',
    cancel_reason VARCHAR(200) COMMENT '取消/退单原因',
    cancel_time DATETIME(3) COMMENT '取消时间',
    pay_time DATETIME(3) COMMENT '支付时间',
    ship_time DATETIME(3) COMMENT '发货时间',
    receive_time DATETIME(3) COMMENT '收货时间',
    evaluate_time DATETIME(3) COMMENT '评价时间',
    refund_apply_time DATETIME(3) COMMENT '申请退单时间',
    refund_audit_time DATETIME(3) COMMENT '退单审核时间',
    create_time DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3),
    update_time DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    UNIQUE INDEX idx_order_no (order_no),
    INDEX idx_user_id (user_id),
    INDEX idx_status (order_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单';

/*
订单状态说明：
  0  - 已下单（待支付）
  1  - 已支付（待发货）
  2  - 已发货（待收货）
  3  - 已收货（待评价）
  4  - 已评价（结束）
 -1  - 取消订单（仅 0 时可取消）
 -2  - 申请退单（2 时可申请）
 -3  - 退单审核通过（结束）
 -4  - 管理员直接退单（结束）
*/

-- ============================================
-- 7. order_item — 订单明细
-- ============================================
CREATE TABLE order_item (
    id BIGINT NOT NULL PRIMARY KEY COMMENT '雪花ID',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    product_name VARCHAR(200) NOT NULL COMMENT '商品名称(快照)',
    product_image VARCHAR(500) COMMENT '商品主图(快照)',
    price DECIMAL(10,2) NOT NULL COMMENT '购买时单价',
    quantity INT NOT NULL DEFAULT 1 COMMENT '数量',
    evaluate_content VARCHAR(500) COMMENT '评价内容',
    evaluate_star TINYINT COMMENT '评价星级 1-5',
    evaluate_time DATETIME(3) COMMENT '评价时间',
    INDEX idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细';

-- ============================================
-- 8. video — 视频
-- ============================================
CREATE TABLE video (
    id BIGINT NOT NULL PRIMARY KEY COMMENT '雪花ID',
    title VARCHAR(200) NOT NULL COMMENT '视频标题',
    video_url VARCHAR(500) NOT NULL COMMENT '视频地址',
    cover_url VARCHAR(500) COMMENT '封面图',
    product_id BIGINT COMMENT '关联商品ID',
    play_count INT DEFAULT 0 COMMENT '播放次数',
    status TINYINT DEFAULT 1 COMMENT '状态 0-下架 1-上架',
    deleted TINYINT(1) DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3),
    update_time DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    INDEX idx_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视频';

-- ============================================
-- 9. ai_chat_record — AI对话记录
-- ============================================
CREATE TABLE ai_chat_record (
    id BIGINT NOT NULL PRIMARY KEY COMMENT '雪花ID',
    user_id BIGINT COMMENT '用户ID(游客可为空)',
    session_id VARCHAR(64) NOT NULL COMMENT '会话ID',
    role VARCHAR(20) NOT NULL COMMENT '角色 user/assistant',
    content TEXT NOT NULL COMMENT '消息内容',
    create_time DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3),
    INDEX idx_session_id (session_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI对话记录';

-- ============================================
-- 10. sys_role — 角色
-- ============================================
CREATE TABLE sys_role (
    id BIGINT NOT NULL PRIMARY KEY COMMENT '雪花ID',
    name VARCHAR(50) NOT NULL COMMENT '角色名',
    code VARCHAR(50) NOT NULL COMMENT '角色编码',
    description VARCHAR(200) COMMENT '描述',
    status TINYINT DEFAULT 1 COMMENT '状态 0-禁用 1-正常',
    create_time DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3),
    update_time DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    UNIQUE INDEX idx_name (name),
    UNIQUE INDEX idx_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色';

-- ============================================
-- 11. sys_menu — 菜单/权限
-- ============================================
CREATE TABLE sys_menu (
    id BIGINT NOT NULL PRIMARY KEY COMMENT '雪花ID',
    parent_id BIGINT DEFAULT 0 NOT NULL COMMENT '父菜单ID',
    name VARCHAR(50) NOT NULL COMMENT '菜单名称',
    path VARCHAR(200) COMMENT '前端路由',
    permission VARCHAR(100) COMMENT '权限标识',
    type TINYINT NOT NULL COMMENT '类型 0-目录 1-菜单 2-按钮',
    icon VARCHAR(100) COMMENT '图标',
    sort INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单权限';

-- ============================================
-- 12. sys_role_menu — 角色-菜单关联
-- ============================================
CREATE TABLE sys_role_menu (
    role_id BIGINT NOT NULL COMMENT '角色ID',
    menu_id BIGINT NOT NULL COMMENT '菜单ID',
    PRIMARY KEY (role_id, menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联';

-- ============================================
-- 13. sys_user_role — 用户-角色关联
-- ============================================
CREATE TABLE sys_user_role (
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    PRIMARY KEY (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联';

-- ============================================
-- 14. sys_message — 聊天 & 系统通知
-- ============================================
CREATE TABLE sys_message (
    id BIGINT NOT NULL PRIMARY KEY COMMENT '雪花ID',
    conversation_id VARCHAR(64) NOT NULL COMMENT '会话ID',
    sender_id BIGINT NOT NULL COMMENT '发送人ID(0=系统)',
    receiver_id BIGINT NOT NULL COMMENT '接收人ID',
    content TEXT NOT NULL COMMENT '消息内容',
    type TINYINT NOT NULL COMMENT '类型 1-聊天 2-系统通知',
    readed TINYINT(1) DEFAULT 0 COMMENT '是否已读',
    create_time DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3),
    INDEX idx_conversation_id (conversation_id),
    INDEX idx_receiver_id (receiver_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息(聊天+通知)';
