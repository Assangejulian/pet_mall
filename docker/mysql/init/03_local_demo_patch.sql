USE pet_store;
SET NAMES utf8mb4;

-- Local development patch for existing Docker volumes:
-- - older databases may miss WeChat login columns expected by User.java
-- - miniapp demo pages need non-empty records and stable local images

SET @has_openid := (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'user'
      AND column_name = 'openid'
);
SET @sql := IF(
    @has_openid = 0,
    'ALTER TABLE user ADD COLUMN openid VARCHAR(64) COMMENT ''微信openid'' AFTER email, ADD COLUMN unionid VARCHAR(64) COMMENT ''微信unionid'' AFTER openid',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_idx_openid := (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'user'
      AND index_name = 'idx_openid'
);
SET @sql := IF(
    @has_idx_openid = 0,
    'ALTER TABLE user ADD UNIQUE INDEX idx_openid (openid)',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_face_id := (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'user'
      AND column_name = 'face_id'
);
SET @sql := IF(
    @has_face_id = 0,
    'ALTER TABLE user ADD COLUMN face_id VARCHAR(100) COMMENT ''face auth id'' AFTER openid',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE user
SET real_name = CASE id
        WHEN 910000000000000101 THEN '暖窝小鱼'
        WHEN 910000000000000102 THEN '布偶田田'
        ELSE real_name
    END,
    avatar = CASE id
        WHEN 910000000000000101 THEN '/images/mock/cat-avatar.jpg'
        WHEN 910000000000000102 THEN '/images/mock/dog-avatar.jpg'
        ELSE avatar
    END
WHERE id IN (910000000000000101, 910000000000000102);

INSERT INTO store(id, user_id, store_name, store_logo, store_phone, store_desc, province, city, district, address, longitude, latitude, status, deleted)
VALUES
    (10, 2, '暖窝宠物馆', '/images/mock/cat-cover.jpg', '13800001111', '主打健康宠物和日常护理用品', '福建省', '厦门市', '集美区', '理工路100号', 118.1000000, 24.6000000, 1, 0),
    (11, 3, '猫咪日记', '/images/mock/blue-cat.jpg', '13800002222', '猫咪领养、用品和洗护咨询', '福建省', '厦门市', '思明区', '中山路200号', 118.0800000, 24.4500000, 1, 0)
ON DUPLICATE KEY UPDATE
    store_name = VALUES(store_name),
    store_logo = VALUES(store_logo),
    store_phone = VALUES(store_phone),
    store_desc = VALUES(store_desc),
    province = VALUES(province),
    city = VALUES(city),
    district = VALUES(district),
    address = VALUES(address),
    longitude = VALUES(longitude),
    latitude = VALUES(latitude),
    status = VALUES(status),
    deleted = VALUES(deleted);

INSERT INTO product(id, store_id, product_name, product_type, category, product_desc, price, stock, main_image, status, video_id, deleted)
VALUES
    (1, 10, '金毛幼犬', 1, 'dog', '性格温顺，适合家庭陪伴，已完成基础免疫', 1888.00, 5, '/images/mock/golden.jpg', 1, 910000000000001001, 0),
    (2, 11, '英短蓝猫', 1, 'cat', '圆脸亲人，适合新手饲养', 2580.00, 3, '/images/mock/blue-cat.jpg', 1, 910000000000001002, 0),
    (3, 10, '低敏主粮', 2, 'food', '适合肠胃敏感宠物的日常主粮', 168.00, 30, '/images/mock/cat-cover.jpg', 1, NULL, 0),
    (4, 11, '观察记录卡', 2, 'care', '记录饮食、排便和精神状态，便于复盘护理', 29.90, 50, '/images/mock/ragdoll.jpg', 1, NULL, 0)
ON DUPLICATE KEY UPDATE
    store_id = VALUES(store_id),
    product_name = VALUES(product_name),
    product_type = VALUES(product_type),
    category = VALUES(category),
    product_desc = VALUES(product_desc),
    price = VALUES(price),
    stock = VALUES(stock),
    main_image = VALUES(main_image),
    status = VALUES(status),
    video_id = VALUES(video_id),
    deleted = VALUES(deleted);

UPDATE video
SET title = CASE id
        WHEN 910000000000001001 THEN '第一次接它回家'
        WHEN 910000000000001002 THEN '狗狗兴奋乱扑怎么办'
        WHEN 910000000000001003 THEN '猫咪食欲变差怎么办'
        WHEN 910000000000001004 THEN '幼宠用品清单'
        ELSE title
    END,
    description = CASE id
        WHEN 910000000000001001 THEN '从隔离区到第一晚观察，把小家伙安稳接回家。'
        WHEN 910000000000001002 THEN '先让它学会坐下等待，再把奖励和社交绑定起来。'
        WHEN 910000000000001003 THEN '排查换粮、温度、压力和精神状态，先观察重点信号。'
        WHEN 910000000000001004 THEN '笼具、食盆、牵引和清洁用品先准备基础款，别一开始买太多。'
        ELSE description
    END,
    cover = CASE id
        WHEN 910000000000001001 THEN '/images/mock/cat-cover.jpg'
        WHEN 910000000000001002 THEN '/images/mock/golden.jpg'
        WHEN 910000000000001003 THEN '/images/mock/blue-cat.jpg'
        WHEN 910000000000001004 THEN '/images/mock/corgi.jpg'
        ELSE cover
    END,
    product_id = CASE id
        WHEN 910000000000001001 THEN 1
        WHEN 910000000000001002 THEN 3
        WHEN 910000000000001003 THEN 2
        WHEN 910000000000001004 THEN 4
        ELSE product_id
    END
WHERE deleted = 0;
