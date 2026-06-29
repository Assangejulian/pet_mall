-- ============================================
-- 瀹犵墿鍟嗗簵 - 瀹屾暣寤哄簱寤鸿〃鑴氭湰
-- 鐗堟湰锛歷1.0锛堝熀浜庝笟鍔¤璁?md + 鏁版嵁搴撹璁¤鑼冿級
-- 鍙樻洿璇存槑瑙?doc/鍙樻洿璇存槑.md
-- 鏁版嵁搴撳悕锛歱et_store
-- 瀛楃闆嗭細utf8mb4
-- 涓婚敭绛栫暐锛氶洩鑺辩畻娉曪紙IdType.ASSIGN_ID锛孞ava 鐢熸垚锛?
-- ============================================

CREATE DATABASE IF NOT EXISTS pet_store DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE pet_store;

-- ============================================
-- 1. user 鈥?鐢ㄦ埛锛堝惈绠＄悊鍛橈級
-- [C1] 鏂板 role 瀛楁鏇夸唬 RBAC 鍥涘紶琛?
-- ============================================
CREATE TABLE user (
    id          BIGINT      NOT NULL PRIMARY KEY COMMENT '闆姳ID',
    username    VARCHAR(50) NOT NULL COMMENT '鐧诲綍鍚?,
    password    VARCHAR(255) NOT NULL COMMENT '瀵嗙爜锛堝紑鍙戦樁娈垫槑鏂囷紝涓婄嚎鍓?BCrypt锛?,
    phone       VARCHAR(20) COMMENT '鎵嬫満鍙?,
    avatar      VARCHAR(500) COMMENT '澶村儚',
    email       VARCHAR(100) COMMENT '閭',
    member_level TINYINT    DEFAULT 0 NOT NULL COMMENT '浼氬憳绛夌骇 0-鏅€?1-閾跺崱 2-閲戝崱 3-閽荤煶',
    real_name   VARCHAR(50) COMMENT '鐪熷疄濮撳悕',
    birthday    DATE        COMMENT '鐢熸棩',
    role        VARCHAR(20) DEFAULT 'user' NOT NULL COMMENT '瑙掕壊 user-鏅€氱敤鎴?admin-绠＄悊鍛?,
    status      TINYINT     DEFAULT 1 NOT NULL COMMENT '鐘舵€?0-绂佺敤 1-姝ｅ父',
    create_time DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
    update_time DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) NOT NULL,
    UNIQUE INDEX idx_username (username),
    UNIQUE INDEX idx_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='鐢ㄦ埛';
-- 琛ュ厖寰俊鐧诲綍瀛楁锛堝畨鍏ㄩ噸澶嶆墽琛岋級
-- 琛ュ厖寰俊鐧诲綍瀛楁
SET @sql_openid = (SELECT IF(COUNT(*) = 0,
    'ALTER TABLE user ADD COLUMN openid VARCHAR(100) COMMENT ''寰俊灏忕▼搴?openid'' AFTER email',
    'SELECT 1'
) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user' AND COLUMN_NAME = 'openid');
PREPARE stmt FROM @sql_openid; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 琛ュ厖浜鸿劯璇嗗埆瀛楁
SET @sql_faceid = (SELECT IF(COUNT(*) = 0,
    'ALTER TABLE user ADD COLUMN face_id VARCHAR(100) COMMENT ''浜鸿劯璇嗗埆ID'' AFTER openid',
    'SELECT 1'
) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user' AND COLUMN_NAME = 'face_id');
PREPARE stmt FROM @sql_faceid; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql_unionid = (SELECT IF(COUNT(*) = 0,
    'ALTER TABLE user ADD COLUMN unionid VARCHAR(100) COMMENT ''寰俊寮€鏀惧钩鍙?unionid'' AFTER face_id',
    'SELECT 1'
) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user' AND COLUMN_NAME = 'unionid');
PREPARE stmt FROM @sql_unionid; EXECUTE stmt; DEALLOCATE PREPARE stmt;
-- ============================================
-- 2. user_address 鈥?鏀惰揣鍦板潃
-- ============================================
CREATE TABLE user_address (
    id            BIGINT       NOT NULL PRIMARY KEY COMMENT '闆姳ID',
    user_id       BIGINT       NOT NULL COMMENT '鐢ㄦ埛ID',
    receiver_name VARCHAR(50)  NOT NULL COMMENT '鏀朵欢浜?,
    phone         VARCHAR(20)  NOT NULL COMMENT '鑱旂郴鐢佃瘽',
    province      VARCHAR(50)  COMMENT '鐪?,
    city          VARCHAR(50)  COMMENT '甯?,
    district      VARCHAR(50)  COMMENT '鍖?,
    detail        VARCHAR(200) NOT NULL COMMENT '璇︾粏鍦板潃',
    defaulted     TINYINT(1)   DEFAULT 0 NOT NULL COMMENT '鏄惁榛樿 0-鍚?1-鏄?,
    create_time   DATETIME(3)  DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
    update_time   DATETIME(3)  DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) NOT NULL,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='鏀惰揣鍦板潃';

-- ============================================
-- 3. store 鈥?鍟嗗簵锛堝晢鎴凤級
-- ============================================
CREATE TABLE store (
    id          BIGINT        NOT NULL PRIMARY KEY COMMENT '闆姳ID',
    user_id     BIGINT        NOT NULL COMMENT '搴椾富锛堝叧鑱?user锛?,
    store_name  VARCHAR(100)  NOT NULL COMMENT '鍟嗗簵鍚嶇О',
    store_logo  VARCHAR(500)  COMMENT '鍟嗗簵Logo',
    store_phone VARCHAR(20)   COMMENT '鑱旂郴鐢佃瘽',
    store_desc  TEXT          COMMENT '鍟嗗簵鎻忚堪',
    province    VARCHAR(50)   COMMENT '鐪?,
    city        VARCHAR(50)   COMMENT '甯?,
    district    VARCHAR(50)   COMMENT '鍖?,
    address     VARCHAR(200)  COMMENT '璇︾粏鍦板潃',
    longitude   DECIMAL(10,7) NOT NULL COMMENT '缁忓害',
    latitude    DECIMAL(10,7) NOT NULL COMMENT '绾害',
    status      TINYINT       DEFAULT 0 NOT NULL COMMENT '鐘舵€?0-寰呭鏍?1-钀ヤ笟涓?2-宸插叧闂?,
    deleted     TINYINT(1)    DEFAULT 0 NOT NULL COMMENT '閫昏緫鍒犻櫎',
    create_time DATETIME(3)   DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
    update_time DATETIME(3)   DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) NOT NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_city (city),
    INDEX idx_location (longitude, latitude)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='鍟嗗簵';

-- ============================================
-- 4. product 鈥?鍟嗗搧
-- ============================================
CREATE TABLE product (
    id           BIGINT         NOT NULL PRIMARY KEY COMMENT '闆姳ID',
    store_id     BIGINT         NOT NULL COMMENT '鎵€灞炲晢搴桰D',
    product_name VARCHAR(200)   NOT NULL COMMENT '鍟嗗搧鍚嶇О',
    product_type TINYINT        NOT NULL DEFAULT 1 COMMENT '鍟嗗搧绫诲瀷 1-瀹犵墿 2-鍛ㄨ竟',
    category     VARCHAR(50)    COMMENT '鍒嗙被 鐙?鐚?楸?楦?鍏朵粬',
    product_desc TEXT           COMMENT '鍟嗗搧璇︽儏',
    price        DECIMAL(10,2)  NOT NULL COMMENT '浠锋牸',
    stock        INT            DEFAULT 1 NOT NULL COMMENT '搴撳瓨',
    main_image   VARCHAR(500)   COMMENT '涓诲浘',
    images       JSON           COMMENT '澶氬浘JSON',
    status       TINYINT        DEFAULT 1 NOT NULL COMMENT '鐘舵€?0-涓嬫灦 1-涓婃灦 2-宸插敭鍑?,
    video_id     BIGINT         COMMENT '鍏宠仈瑙嗛ID',
    deleted      TINYINT(1)     DEFAULT 0 NOT NULL COMMENT '閫昏緫鍒犻櫎',
    create_time  DATETIME(3)    DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
    update_time  DATETIME(3)    DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) NOT NULL,
    INDEX idx_store_id (store_id),
    INDEX idx_type (product_type),
    INDEX idx_category (category),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='鍟嗗搧';

-- ============================================
-- 5. cart 鈥?璐墿杞?
-- ============================================
CREATE TABLE cart (
    id          BIGINT      NOT NULL PRIMARY KEY COMMENT '闆姳ID',
    user_id     BIGINT      NOT NULL COMMENT '鐢ㄦ埛ID',
    product_id  BIGINT      NOT NULL COMMENT '鍟嗗搧ID',
    quantity    INT         NOT NULL DEFAULT 1 COMMENT '鏁伴噺',
    checked     TINYINT(1)  DEFAULT 1 NOT NULL COMMENT '鏄惁閫変腑 0-鍚?1-鏄?,
    create_time DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
    update_time DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) NOT NULL,
    UNIQUE INDEX idx_user_product (user_id, product_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='璐墿杞?;

-- ============================================
-- 6. purchase_order 鈥?璁㈠崟锛堥伩鍏嶄繚鐣欏瓧 order锛?
-- ============================================
CREATE TABLE purchase_order (
    id                BIGINT         NOT NULL PRIMARY KEY COMMENT '闆姳ID',
    order_no          VARCHAR(32)    NOT NULL COMMENT '璁㈠崟鍙?,
    user_id           BIGINT         NOT NULL COMMENT '鐢ㄦ埛ID',
    address_id        BIGINT         NOT NULL COMMENT '鏀惰揣鍦板潃ID',
    total_amount      DECIMAL(10,2)  NOT NULL COMMENT '鎬婚噾棰?,
    discount_amount   DECIMAL(10,2)  DEFAULT 0 NOT NULL COMMENT '浼樻儬閲戦',
    pay_amount        DECIMAL(10,2)  NOT NULL COMMENT '瀹炰粯閲戦',
    order_status      INT            NOT NULL DEFAULT 0 COMMENT '鐘舵€?0-寰呮敮浠?1-宸叉敮浠?2-宸插彂璐?3-宸叉敹璐?4-宸茶瘎浠?-1-鍙栨秷 -2-鐢宠閫€鍗?-3-閫€鍗曞鏍搁€氳繃 -4-绠＄悊鍛橀€€鍗?,
    cancel_reason     VARCHAR(200)   COMMENT '鍙栨秷鍘熷洜',
    cancel_time       DATETIME(3)    COMMENT '鍙栨秷鏃堕棿',
    pay_time          DATETIME(3)    COMMENT '鏀粯鏃堕棿',
    ship_time         DATETIME(3)    COMMENT '鍙戣揣鏃堕棿',
    receive_time      DATETIME(3)    COMMENT '鏀惰揣鏃堕棿',
    evaluate_time     DATETIME(3)    COMMENT '璇勪环鏃堕棿',
    refund_apply_time DATETIME(3)    COMMENT '閫€鍗曠敵璇锋椂闂?,
    refund_audit_time DATETIME(3)    COMMENT '閫€鍗曞鏍告椂闂?,
    create_time       DATETIME(3)    DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
    update_time       DATETIME(3)    DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) NOT NULL,
    UNIQUE INDEX idx_order_no (order_no),
    INDEX idx_user_id (user_id),
    INDEX idx_order_status (order_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='璁㈠崟';

-- ============================================
-- 7. order_item 鈥?璁㈠崟椤?
-- ============================================
CREATE TABLE order_item (
    id                BIGINT         NOT NULL PRIMARY KEY COMMENT '闆姳ID',
    order_id          BIGINT         NOT NULL COMMENT '璁㈠崟ID',
    product_id        BIGINT         NOT NULL COMMENT '鍟嗗搧ID',
    product_name      VARCHAR(200)   NOT NULL COMMENT '鍟嗗搧鍚嶇О锛堝揩鐓э級',
    product_image     VARCHAR(500)   COMMENT '鍟嗗搧鍥剧墖锛堝揩鐓э級',
    price             DECIMAL(10,2)  NOT NULL COMMENT '璐拱鏃跺崟浠?,
    quantity          INT            NOT NULL DEFAULT 1 COMMENT '鏁伴噺',
    evaluate_content  VARCHAR(500)   COMMENT '璇勪环鍐呭',
    evaluate_star     TINYINT        COMMENT '璇勪环鏄熺骇 1-5',
    evaluate_time     DATETIME(3)    COMMENT '璇勪环鏃堕棿',
    create_time       DATETIME(3)    DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
    INDEX idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='璁㈠崟椤?;

-- ============================================
-- 8. video 鈥?瑙嗛
-- [C3] video_url 鈫?url, cover_url 鈫?cover
-- ============================================
CREATE TABLE video (
    id          BIGINT       NOT NULL PRIMARY KEY COMMENT '闆姳ID',
    title       VARCHAR(200) NOT NULL COMMENT '瑙嗛鏍囬',
    url         VARCHAR(500) NOT NULL COMMENT '瑙嗛鍦板潃',
    cover       VARCHAR(500) COMMENT '灏侀潰鍥?,
    product_id  BIGINT       COMMENT '鍏宠仈鍟嗗搧ID',
    play_count  INT          DEFAULT 0 NOT NULL COMMENT '鎾斁娆℃暟',
    status      TINYINT      DEFAULT 1 NOT NULL COMMENT '鐘舵€?0-涓嬫灦 1-涓婃灦',
    deleted     TINYINT(1)   DEFAULT 0 NOT NULL COMMENT '閫昏緫鍒犻櫎',
    create_time DATETIME(3)  DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
    update_time DATETIME(3)  DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) NOT NULL,
    INDEX idx_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='瑙嗛';

-- ============================================
-- 瑙嗛璇勮

-- 瑙嗛璇勮
CREATE TABLE IF NOT EXISTS `comment` (
    `id`          BIGINT       NOT NULL PRIMARY KEY COMMENT '闆姳ID',
    `video_id`    BIGINT       NOT NULL COMMENT '瑙嗛ID',
    `user_id`     BIGINT       COMMENT '璇勮鐢ㄦ埛ID',
    `content`     TEXT         NOT NULL COMMENT '璇勮鍐呭',
    `create_time` DATETIME(3)  DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
    `update_time` DATETIME(3)  DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) NOT NULL,
    INDEX idx_video_id (video_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='瑙嗛璇勮';

-- 9. ai_chat_record 鈥?AI瀵硅瘽璁板綍
-- ============================================
CREATE TABLE ai_chat_record (
    id          BIGINT       NOT NULL PRIMARY KEY COMMENT '闆姳ID',
    user_id     BIGINT       COMMENT '鐢ㄦ埛ID(娓稿鍙负绌?',
    session_id  VARCHAR(64)  NOT NULL COMMENT '浼氳瘽ID',
    role        VARCHAR(20)  NOT NULL COMMENT '瑙掕壊 user/assistant',
    content     TEXT         NOT NULL COMMENT '娑堟伅鍐呭',
    create_time DATETIME(3)  DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
    INDEX idx_session_id (session_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI瀵硅瘽璁板綍';

-- ============================================
-- 10. sys_message 鈥?鑱婂ぉ & 绯荤粺閫氱煡
-- ============================================
CREATE TABLE sys_message (
    id              BIGINT       NOT NULL PRIMARY KEY COMMENT '闆姳ID',
    conversation_id VARCHAR(64)  NOT NULL COMMENT '浼氳瘽ID',
    sender_id       BIGINT       NOT NULL COMMENT '鍙戦€佷汉ID(0=绯荤粺)',
    receiver_id     BIGINT       NOT NULL COMMENT '鎺ユ敹浜篒D',
    content         TEXT         NOT NULL COMMENT '娑堟伅鍐呭',
    type            TINYINT      NOT NULL COMMENT '绫诲瀷 1-鑱婂ぉ 2-绯荤粺閫氱煡',
    readed          TINYINT(1)   DEFAULT 0 NOT NULL COMMENT '鏄惁宸茶',
    create_time     DATETIME(3)  DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
    INDEX idx_conversation_id (conversation_id),
    INDEX idx_receiver_id (receiver_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='娑堟伅(鑱婂ぉ+閫氱煡)';

-- ============================================
-- 棰勮鏁版嵁
-- ============================================
INSERT INTO user(id, username, password, role, status, real_name) VALUES
(1, 'admin', 'admin123', 'admin', 1, '绯荤粺绠＄悊鍛?),
(2, 'user',  'user123',  'user',  1, '娴嬭瘯鐢ㄦ埛');

-- ============================================
-- 补充: address_snapshot 字段（订单地址快照）
-- ============================================
ALTER TABLE purchase_order ADD COLUMN address_snapshot JSON COMMENT '收货地址快照(JSON)' AFTER address_id;
