-- ============================================
-- 宠物商店 - 种子数据
-- 安全可重复执行（INSERT IGNORE）
-- ============================================

USE pet_store;

-- ========== comment 表（安全建表，字段已修正） ==========
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

-- ========== 1. user ==========
INSERT IGNORE INTO user (id, username, password, phone, avatar, email, member_level, real_name, birthday, role, status) VALUES
(1, 'admin', 'admin123', '13800138000', 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200', 'admin@petstore.com', 3, '系统管理员', '1990-01-01', 'admin', 1),
(2, 'user',  'user123',  '13900139000', 'https://images.unsplash.com/photo-1599566150163-29194dcaad36?w=200', 'user@petstore.com', 1, '张三', '1995-06-15', 'user', 1);

-- ========== 2. user_address ==========
INSERT IGNORE INTO user_address (id, user_id, receiver_name, phone, province, city, district, detail, defaulted) VALUES
(1, 2, '张三', '13800138000', '福建省', '厦门市', '集美区', '理工路600号', 1),
(2, 2, '张三', '13900139000', '福建省', '厦门市', '思明区', '中山路100号', 0);

-- ========== 3. store ==========
INSERT IGNORE INTO store (id, user_id, store_name, store_logo, store_phone, store_desc, province, city, district, address, longitude, latitude, status) VALUES
(1, 3, '暖窝·思明店', 'https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?w=400', '13800000001', '猫狗小宠一站式，厦门思明区老店，服务超过3000个宠物家庭。', '福建省', '厦门市', '思明区', '中山路128号', 118.0805000, 24.4535000, 1),
(2, 4, '暖窝·湖里店', 'https://images.unsplash.com/photo-1534361960057-19889db9621e?w=400', '13800000002', '猫狗鸟类水族专门店，品种齐全，专业繁育。', '福建省', '厦门市', '湖里区', '万达广场3F', 118.0975000, 24.5095000, 1),
(3, 3, '暖窝·集美店', 'https://images.unsplash.com/photo-1574158622682-e40e69881006?w=400', '13800000003', '水族小宠专门店，提供宠物寄养、美容服务。', '福建省', '厦门市', '集美区', '石鼓路56号', 118.0910000, 24.5670000, 1),
(4, 4, '暖窝·翔安店', 'https://images.unsplash.com/photo-1552728089-57bdde30beb3?w=400', '13800000004', '鸟类主题店铺，鹦鹉品类齐全。', '福建省', '厦门市', '翔安区', '新兴街99号', 118.2440000, 24.6170000, 1),
(5, 3, '暖窝·同安店', 'https://images.unsplash.com/photo-1585110396000-c9ffd4e4b308?w=400', '13800000005', '兔子、仓鼠、龙猫等小宠专卖。', '福建省', '厦门市', '同安区', '城南路88号', 118.1540000, 24.7240000, 1);

-- ========== 4. product ==========
INSERT IGNORE INTO product (id, store_id, product_name, product_type, category, product_desc, price, stock, main_image, images, status, video_id) VALUES
(1, 1, '金毛幼犬', 1, 'dog', '纯种金毛，双血统，已打第一针疫苗，驱虫完成，性格温顺。', 1888.00, 3,
 'https://images.unsplash.com/photo-1552053831-71594a27632d?w=400',
 '["https://images.unsplash.com/photo-1552053831-71594a27632d?w=400","https://images.unsplash.com/photo-1543466835-00a7907e9de1?w=400"]', 1, 1),

(2, 1, '英短蓝猫', 1, 'cat', '包子脸，3个月大，性格粘人，已驱虫。', 2580.00, 2,
 'https://images.unsplash.com/photo-1574231164645-d6f0e8553590?w=400',
 '["https://images.unsplash.com/photo-1574231164645-d6f0e8553590?w=400"]', 1, 2),

(3, 1, '柯基犬', 1, 'dog', '小短腿，2个月大，三色柯基，活泼可爱。', 3200.00, 2,
 'https://images.unsplash.com/photo-1543466835-00a7907e9de1?w=400',
 '["https://images.unsplash.com/photo-1543466835-00a7907e9de1?w=400"]', 1, 3),

(4, 1, '布偶猫', 1, 'cat', '海豹双色布偶，2个月大，CFA认证猫舍。', 4500.00, 2,
 'https://images.unsplash.com/photo-1513360371669-4adf3dd7dff8?w=400',
 '["https://images.unsplash.com/photo-1513360371669-4adf3dd7dff8?w=400"]', 1, 4),

(5, 1, '幼宠主粮 2kg', 2, 'food', '天然粮，无谷物配方，适合全品种幼犬/幼猫。', 168.00, 20,
 'https://images.unsplash.com/photo-1589924691995-400dc9ecc119?w=400',
 '[]', 1, NULL),

(6, 2, '宠物航空箱', 2, 'accessory', '中型犬猫通用航空箱，ABS材质，透气安全。', 198.00, 15,
 'https://images.unsplash.com/photo-1601758228041-f3b2795255f1?w=400',
 '["https://images.unsplash.com/photo-1587402092301-725e37c70fd8?w=400"]', 1, NULL),

(7, 2, '宠物梳毛手套', 2, 'accessory', '硅胶按摩刷毛手套，清理浮毛同时按摩皮肤。', 39.00, 30,
 'https://images.unsplash.com/photo-1516734212186-a967f81ad0d7?w=400',
 '[]', 1, NULL),

(8, 1, '金丝熊·团子', 1, 'other', '叙利亚金丝熊，1个月大，亲人活泼，笼具用品齐全。', 68.00, 5,
 'https://images.unsplash.com/photo-1425082661705-1834bfd09dca?w=400',
 '["https://images.unsplash.com/photo-1585110396000-c9ffd4e4b308?w=400"]', 1, NULL),

(9, 4, '玄凤鹦鹉·蓝蓝', 1, 'bird', '黄化玄凤，2个月大，手养亲人，会吹口哨。', 580.00, 2,
 'https://images.unsplash.com/photo-1552728089-57bdde30beb3?w=400',
 '["https://images.unsplash.com/photo-1552921289-7a7b0d0b76f8?w=400"]', 1, NULL),

(10, 3, '龙鱼·赤焰', 1, 'fish', '亚洲红龙，35cm，品相极佳，发色艳丽。', 6800.00, 1,
 'https://images.unsplash.com/photo-1520366498724-709889c0c685?w=400',
 '["https://images.unsplash.com/photo-1535591273668-578e31182c4f?w=400"]', 1, NULL),

(11, 3, '垂耳兔·棉花', 1, 'other', '荷兰垂耳兔，1个月大，性格温顺稳定。', 188.00, 3,
 'https://images.unsplash.com/photo-1585110396000-c9ffd4e4b308?w=400',
 '["https://images.unsplash.com/photo-1535241749838-299277b6305f?w=400"]', 1, NULL),

(12, 1, '暹罗猫·奶茶', 1, 'cat', '重点色暹罗，2个月大，粘人话痨，聪明活泼。', 2200.00, 2,
 'https://images.unsplash.com/photo-1495360010541-f48722b34f7d?w=400',
 '["https://images.unsplash.com/photo-1519052537078-e6302a4968d4?w=400"]', 1, NULL),

(13, 2, '英短蓝猫·灰灰', 1, 'cat', '英短蓝猫，3个月大，包子脸性格好。', 2800.00, 1,
 'https://images.unsplash.com/photo-1574231164645-d6f0e8553590?w=400',
 '["https://images.unsplash.com/photo-1592194996308-7b43878e84a6?w=400"]', 1, NULL);

-- ========== 5. video ==========
INSERT IGNORE INTO video (id, user_id, title, description, url, cover, product_id, play_count, likes, comment_count, duration, status) VALUES
(1, 2, '金毛幼犬的日常撒欢', '每天早上都会叼着拖鞋来叫我起床，太治愈了。', '', 'https://images.unsplash.com/photo-1513360371669-4adf3dd7dff8?w=400', 1, 12000, 2300, 156, 45, 1),
(2, 2, '英短蓝猫卖萌合集', '包子脸的终极奥义就是装无辜。', '', 'https://images.unsplash.com/photo-1574231164645-d6f0e8553590?w=400', 2, 35000, 5100, 432, 60, 1),
(3, 2, '柯基小短腿赛跑大赛', '腿虽短但跑起来谁也不服。', '', 'https://images.unsplash.com/photo-1543466835-00a7907e9de1?w=400', 3, 8500, 1800, 89, 30, 1),
(4, 2, '布偶猫的仙女日常', '每天醒来看到这张脸，感觉世界都温柔了。', '', 'https://images.unsplash.com/photo-1513360371669-4adf3dd7dff8?w=400', 4, 22000, 3600, 278, 50, 1),
(5, 2, '幼宠用品清单', '笼具、食盆、牵引和清洁用品先准备基础款。', '', 'https://images.unsplash.com/photo-1589924691995-400dc9ecc119?w=400', 5, 5000, 980, 45, 35, 1);

-- ========== 6. comment ==========
INSERT IGNORE INTO comment (id, video_id, user_id, content) VALUES
(1, 1, 2, '太可爱了吧！每天都想看'),
(2, 1, 2, '同款在哪里买的呢？'),
(3, 2, 2, '哈哈哈哈太治愈了'),
(4, 3, 2, '小短腿跑起来好萌啊'),
(5, 4, 2, '仙女猫果然名不虚传'),
(6, 5, 2, '收藏了，准备买');

-- ========== 7. purchase_order ==========
INSERT IGNORE INTO purchase_order (id, order_no, user_id, address_id, total_amount, discount_amount, pay_amount, order_status, pay_time, ship_time, receive_time) VALUES
(1, 'PO20260624001', 2, 1, 1888.00, 0.00, 1888.00, 0, '2026-06-24 12:00:00', NULL, NULL),
(2, 'PO20260623002', 2, 1, 2580.00, 0.00, 2580.00, 1, '2026-06-23 10:00:00', '2026-06-24 08:00:00', NULL),
(3, 'PO20260622003', 2, 1, 3200.00, 0.00, 3200.00, 2, '2026-06-22 08:00:00', '2026-06-23 10:00:00', '2026-06-24 14:00:00'),
(4, 'PO20260620004', 2, 1, 4500.00, 0.00, 4500.00, 4, '2026-06-20 14:00:00', '2026-06-21 10:00:00', '2026-06-23 18:00:00');

-- ========== 8. order_item ==========
INSERT IGNORE INTO order_item (id, order_id, product_id, product_name, product_image, price, quantity) VALUES
(1, 1, 1, '金毛幼犬', 'https://images.unsplash.com/photo-1513360371669-4adf3dd7dff8?w=200', 1888.00, 1),
(2, 2, 2, '英短蓝猫', 'https://images.unsplash.com/photo-1574231164645-d6f0e8553590?w=200', 2580.00, 1),
(3, 3, 3, '柯基犬', 'https://images.unsplash.com/photo-1543466835-00a7907e9de1?w=200', 3200.00, 1),
(4, 4, 4, '布偶猫', 'https://images.unsplash.com/photo-1513360371669-4adf3dd7dff8?w=200', 4500.00, 1);
