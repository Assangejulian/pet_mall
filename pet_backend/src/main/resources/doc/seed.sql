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
-- WARNING: 生产部署前请修改默认管理员密码
INSERT IGNORE INTO user (id, username, password, phone, avatar, email, member_level, real_name, birthday, role, status) VALUES
(1, 'admin', 'admin123'  -- 部署后请立即修改密码, '13800138000', 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200', 'admin@petstore.com', 3, '系统管理员', '1990-01-01', 'admin', 1),
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
(1, 1, '金毛幼犬', 1, 'dog', '纯种金毛，双血统，已打第一针疫苗，驱虫完成，性格温顺。', 1888.00, 1,
 'https://images.unsplash.com/photo-1552053831-71594a27632d?w=400',
 '["https://images.unsplash.com/photo-1552053831-71594a27632d?w=400","https://images.unsplash.com/photo-1543466835-00a7907e9de1?w=400"]', 1, 1),

(2, 1, '英短蓝猫', 1, 'cat', '包子脸，3个月大，性格粘人，已驱虫。', 2580.00, 1,
 'https://images.unsplash.com/photo-1574231164645-d6f0e8553590?w=400',
 '["https://images.unsplash.com/photo-1574231164645-d6f0e8553590?w=400"]', 1, 2),

(3, 1, '柯基犬', 1, 'dog', '小短腿，2个月大，三色柯基，活泼可爱。', 3200.00, 1,
 'https://images.unsplash.com/photo-1543466835-00a7907e9de1?w=400',
 '["https://images.unsplash.com/photo-1543466835-00a7907e9de1?w=400"]', 1, 3),

(4, 1, '布偶猫', 1, 'cat', '海豹双色布偶，2个月大，CFA认证猫舍。', 4500.00, 1,
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

(8, 1, '金丝熊·团子', 1, 'other', '叙利亚金丝熊，1个月大，亲人活泼，笼具用品齐全。', 68.00, 1,
 'https://images.unsplash.com/photo-1425082661705-1834bfd09dca?w=400',
 '["https://images.unsplash.com/photo-1585110396000-c9ffd4e4b308?w=400"]', 1, NULL),

(9, 4, '玄凤鹦鹉·蓝蓝', 1, 'bird', '黄化玄凤，2个月大，手养亲人，会吹口哨。', 580.00, 1,
 'https://images.unsplash.com/photo-1552728089-57bdde30beb3?w=400',
 '["https://images.unsplash.com/photo-1552921289-7a7b0d0b76f8?w=400"]', 1, NULL),

(10, 3, '龙鱼·赤焰', 1, 'fish', '亚洲红龙，35cm，品相极佳，发色艳丽。', 6800.00, 1,
 'https://images.unsplash.com/photo-1520366498724-709889c0c685?w=400',
 '["https://images.unsplash.com/photo-1535591273668-578e31182c4f?w=400"]', 1, NULL),

(11, 3, '垂耳兔·棉花', 1, 'other', '荷兰垂耳兔，1个月大，性格温顺稳定。', 188.00, 1,
 'https://images.unsplash.com/photo-1585110396000-c9ffd4e4b308?w=400',
 '["https://images.unsplash.com/photo-1535241749838-299277b6305f?w=400"]', 1, NULL),

(12, 1, '暹罗猫·奶茶', 1, 'cat', '重点色暹罗，2个月大，粘人话痨，聪明活泼。', 2200.00, 1,
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

-- ============================================
-- 补充种子数据（商户订单演示用）
-- ============================================

-- ========== 补充 user（商户 + 更多用户） ==========
INSERT IGNORE INTO user (id, username, password, phone, avatar, email, member_level, real_name, birthday, role, status) VALUES
(3, 'merchant1', '123456', '13800001111', 'https://images.unsplash.com/photo-1543466835-00a7907e9de1?w=200', NULL, 0, '暖窝小暖', NULL, 'merchant', 1),
(4, 'merchant2', '123456', '13800002222', 'https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?w=200', NULL, 0, '猫咖日记', NULL, 'merchant', 1),
(5, 'merchant3', '123456', '13800003333', 'https://images.unsplash.com/photo-1583337130417-3346c1be7dee?w=200', NULL, 0, '鱼乐无穷', NULL, 'merchant', 1),
(6, 'user2', '123456', '13600001111', 'https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=200', 'user2@test.com', 1, '李四', '1998-08-08', 'user', 1),
(7, 'user3', '123456', '13600002222', 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=200', 'user3@test.com', 2, '王五', '2000-01-15', 'user', 1);

-- ========== 补充 user_address ==========
INSERT IGNORE INTO user_address (id, user_id, receiver_name, phone, province, city, district, detail, defaulted) VALUES
(3, 6, '李四', '13600001111', '福建省', '厦门市', '湖里区', '湖里大道88号', 1),
(4, 6, '李四', '13600001112', '福建省', '厦门市', '思明区', '厦禾路200号', 0),
(5, 7, '王五', '13600002222', '福建省', '厦门市', '集美区', '杏林湾路1号', 1);

-- ========== 补充 store（给 merchant3） ==========
INSERT IGNORE INTO store (id, user_id, store_name, store_logo, store_phone, store_desc, province, city, district, address, longitude, latitude, status) VALUES
(6, 5, '鱼乐无穷·水族馆', 'https://images.unsplash.com/photo-1520366498724-709889c0c685?w=400', '13800000006', '专注观赏鱼、水族造景，厦门最大水族馆。', '福建省', '厦门市', '思明区', '环岛路300号', 118.1200000, 24.4600000, 1),
(7, 5, '鱼乐无穷·海沧店', 'https://images.unsplash.com/photo-1535591273668-578e31182c4f?w=400', '13800000007', '热带鱼、海水鱼、水草专营店。', '福建省', '厦门市', '海沧区', '滨湖北路66号', 118.0360000, 24.4850000, 1);

-- ========== 补充 product（覆盖所有店铺） ==========
INSERT IGNORE INTO product (id, store_id, product_name, product_type, category, product_desc, price, stock, main_image, images, status, video_id) VALUES
(14, 1, '贵宾幼犬·奶茶', 1, 'dog', '玩具贵宾，1.5kg迷你体，已打疫苗，性格活泼。', 2800.00, 1,
 '/images/mock/corgi.jpg',
 '["/images/mock/corgi.jpg"]', 1, NULL),
(15, 1, '宠物窝垫 M号', 2, 'accessory', '四季通用宠物窝，可拆洗，柔软保暖。', 89.00, 50,
 'https://images.unsplash.com/photo-1516734212186-a967f81ad0d7?w=400',
 '[]', 1, NULL),
(16, 2, '泰迪犬·巧克力', 1, 'dog', '迷你泰迪，2个月大，已驱虫，聪明好训练。', 2600.00, 1,
 'https://images.unsplash.com/photo-1534361960057-19889db9621e?w=400',
 '["https://images.unsplash.com/photo-1534361960057-19889db9621e?w=400"]', 1, NULL),
(17, 2, '猫抓板·仙人掌', 2, 'accessory', '网红猫抓板，剑麻材质，耐抓不掉屑。', 49.00, 40,
 'https://images.unsplash.com/photo-1601758228041-f3b2795255f1?w=400',
 '[]', 1, NULL),
(18, 3, '仓鼠套餐·豪华版', 2, 'other', '含笼子+跑轮+食盆+木屑+粮食，新手一站式。', 198.00, 20,
 'https://images.unsplash.com/photo-1425082661705-1834bfd09dca?w=400',
 '["https://images.unsplash.com/photo-1425082661705-1834bfd09dca?w=400"]', 1, NULL),
(19, 3, '宠物龟·巴西龟', 1, 'other', '健康巴西龟，约5cm，好饲养，长寿宠物。', 28.00, 10,
 'https://images.unsplash.com/photo-1535591273668-578e31182c4f?w=400',
 '[]', 1, NULL),
(20, 4, '虎皮鹦鹉·彩虹', 1, 'bird', '虎皮鹦鹉，多种花色可选，手养亲人。', 168.00, 5,
 'https://images.unsplash.com/photo-1552921289-7a7b0d0b76f8?w=400',
 '["https://images.unsplash.com/photo-1552921289-7a7b0d0b76f8?w=400"]', 1, NULL),
(21, 4, '鹦鹉站架', 2, 'accessory', '实木鹦鹉站架，含食杯和玩具挂件。', 128.00, 15,
 'https://images.unsplash.com/photo-1552728089-57bdde30beb3?w=400',
 '[]', 1, NULL),
(22, 5, '龙猫·银斑', 1, 'other', '银斑龙猫，2个月大，毛色漂亮，温顺亲人。', 1280.00, 1,
 'https://images.unsplash.com/photo-1585110396000-c9ffd4e4b308?w=400',
 '["https://images.unsplash.com/photo-1585110396000-c9ffd4e4b308?w=400"]', 1, NULL),
(23, 5, '兔子提摩西草 500g', 2, 'food', '进口提摩西草，高纤维助消化，兔子必备。', 25.00, 100,
 'https://images.unsplash.com/photo-1589924691995-400dc9ecc119?w=400',
 '[]', 1, NULL),
(24, 6, '神仙鱼·熊猫', 1, 'fish', '熊猫神仙鱼，体长4-5cm，温顺群游。', 38.00, 30,
 'https://images.unsplash.com/photo-1520366498724-709889c0c685?w=400',
 '["https://images.unsplash.com/photo-1520366498724-709889c0c685?w=400"]', 1, NULL),
(25, 6, '水族箱 60cm', 2, 'accessory', '超白玻璃鱼缸，60x30x36cm，含过滤系统。', 399.00, 10,
 'https://images.unsplash.com/photo-1535591273668-578e31182c4f?w=400',
 '[]', 1, NULL),
(26, 6, '水草种子套装', 2, 'other', '迷你矮珍珠+莫斯，新手造景必备，易存活。', 35.00, 50,
 'https://images.unsplash.com/photo-1520366498724-709889c0c685?w=400',
 '[]', 1, NULL),
(27, 7, '斗鱼·半月', 1, 'fish', '半月斗鱼，泰国进口，大尾展，色彩艳丽。', 88.00, 8,
 'https://images.unsplash.com/photo-1535591273668-578e31182c4f?w=400',
 '["https://images.unsplash.com/photo-1535591273668-578e31182c4f?w=400"]', 1, NULL),
(28, 7, '鱼粮套餐', 2, 'food', '热带鱼粮+金鱼粮+底栖鱼粮，三瓶装。', 45.00, 60,
 'https://images.unsplash.com/photo-1589924691995-400dc9ecc119?w=400',
 '[]', 1, NULL);

-- ========== 补充 video ==========
INSERT IGNORE INTO video (id, user_id, title, description, url, cover, product_id, play_count, likes, comment_count, duration, status) VALUES
(6, 2, '贵宾犬的才艺表演', '坐下趴下握手装死，样样精通的小机灵。', '', '/images/mock/corgi.jpg', 14, 6800, 1200, 89, 55, 1),
(7, 2, '龙猫揉脸合集', '圆滚滚的龙猫揉脸太解压了！', '', 'https://images.unsplash.com/photo-1585110396000-c9ffd4e4b308?w=400', 22, 9200, 1800, 112, 40, 1),
(8, 3, '神仙鱼群游好治愈', '看着它们在鱼缸里慢慢游，心情都平静了。', '', 'https://images.unsplash.com/photo-1520366498724-709889c0c685?w=400', 24, 3500, 680, 45, 30, 1);

UPDATE video SET title = '黑白猫的镜头日常', description = '黑白小猫在镜头前放松伸展，适合慢慢看的一段陪伴视频。', url = 'http://video.sonetto.online/Black-and-white_cat_video_202607022313.mp4', cover = '/images/mock/cat-cover.jpg', product_id = 2, status = 1 WHERE id = 1;
UPDATE video SET title = '猫咪下巴挠挠时刻', description = '室内猫咪被轻轻挠下巴，表情很放松。', url = 'http://video.sonetto.online/Cat_chin_scratch_indoor_video_202607022322.mp4', cover = '/images/mock/ragdoll.jpg', product_id = 4, status = 1 WHERE id = 2;
UPDATE video SET title = '猫咪小跑上楼梯', description = '猫咪轻快地一路小跑上楼，动作灵活又可爱。', url = 'http://video.sonetto.online/Cat_trotting_up_stairs_202607022329.mp4', cover = '/images/mock/cat-avatar.jpg', product_id = 2, status = 1 WHERE id = 3;
UPDATE video SET title = '柯基毯上乖坐', description = '柯基坐在毯子上看镜头，短腿和圆脸都很治愈。', url = 'http://video.sonetto.online/Corgi_sitting_on_blanket_202607022304.mp4', cover = '/images/mock/corgi.jpg', product_id = 3, status = 1 WHERE id = 4;
UPDATE video SET title = '金毛叼着郁金香', description = '金毛叼着花靠近镜头，温柔又有春天感。', url = 'http://video.sonetto.online/Golden_retriever_holding_tulip_g%E2%80%A6_202607022323.mp4', cover = '/images/mock/golden.jpg', product_id = 1, status = 1 WHERE id = 5;
UPDATE video SET title = '开心比格犬户外跑跳', description = '比格犬在户外开心活动，适合喜欢活泼狗狗的用户。', url = 'http://video.sonetto.online/Happy_beagle_dog_outdoors_202607022337.mp4', cover = '/images/mock/dog-avatar.jpg', product_id = 14, status = 1 WHERE id = 6;
UPDATE video SET title = '橘猫木桌观察日记', description = '橘猫趴在木桌上观察周围，节奏安静又舒服。', url = 'http://video.sonetto.online/Orange_cat_on_wooden_table_202607022346.mp4', cover = '/images/mock/blue-cat.jpg', product_id = 2, status = 1 WHERE id = 7;
UPDATE video SET status = 0 WHERE id = 8;

-- ========== 补充 comment ==========
INSERT IGNORE INTO comment (id, video_id, user_id, content) VALUES
(7, 6, 2, '太聪明了！想养一只'),
(8, 6, 3, '这个价格含训练课程吗'),
(9, 7, 2, '橘猫趴在木桌上太放松了'),
(10, 7, 4, '这个镜头很安静，适合循环看'),
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
UPDATE comment SET video_id = 7, user_id = 2, content = '橘猫趴在木桌上太放松了' WHERE id = 9;
UPDATE comment SET video_id = 7, user_id = 4, content = '这个镜头很安静，适合循环看' WHERE id = 10;
UPDATE comment SET video_id = 8, user_id = 2, content = '好漂亮的神仙鱼' WHERE id = 11;
UPDATE comment SET video_id = 8, user_id = 5, content = '鱼缸造景也很好看' WHERE id = 12;

UPDATE video v
LEFT JOIN (
  SELECT video_id, COUNT(*) AS cnt
  FROM comment
  GROUP BY video_id
) c ON c.video_id = v.id
SET v.comment_count = COALESCE(c.cnt, 0);

-- ========== 补充 purchase_order（覆盖更多商户的店铺） ==========
INSERT IGNORE INTO purchase_order (id, order_no, user_id, address_id, total_amount, discount_amount, pay_amount, order_status, remark, pay_time, ship_time, receive_time, create_time) VALUES
(5, 'PO20260625005', 6, 3, 2649.00, 0.00, 2649.00, 1, '请尽快发货哦~', '2026-06-25 10:30:00', NULL, NULL, '2026-06-25 10:25:00'),
(6, 'PO20260620006', 6, 3, 296.00, 0.00, 296.00, 4, NULL, '2026-06-20 15:00:00', '2026-06-21 09:00:00', '2026-06-23 11:00:00', '2026-06-20 14:55:00'),
(7, 'PO20260626007', 6, 4, 466.00, 10.00, 456.00, 1, NULL, '2026-06-26 11:00:00', NULL, NULL, '2026-06-26 10:50:00'),
(8, 'PO20260624008', 7, 5, 434.00, 0.00, 434.00, 2, '鱼缸请小心轻放', '2026-06-24 09:00:00', '2026-06-25 08:30:00', NULL, '2026-06-24 08:50:00'),
(9, 'PO20260622009', 7, 5, 89.00, 0.00, 89.00, -1, '不想要了', NULL, NULL, NULL, '2026-06-22 16:00:00'),
(10, 'PO20260627010', 2, 1, 133.00, 0.00, 133.00, 0, NULL, NULL, NULL, NULL, '2026-06-27 14:00:00'),
(11, 'PO20260618011', 7, 5, 1508.00, 0.00, 1508.00, 3, '龙猫很可爱！', '2026-06-18 13:00:00', '2026-06-19 10:00:00', '2026-06-21 16:00:00', '2026-06-18 12:50:00'),
(12, 'PO20260619012', 2, 1, 2600.00, 0.00, 2600.00, -2, '猫咪到家后有点应激反应', '2026-06-19 09:00:00', '2026-06-20 10:00:00', '2026-06-22 14:00:00', '2026-06-19 08:55:00'),
(13, 'PO20260628013', 6, 3, 88.00, 0.00, 88.00, 1, NULL, '2026-06-28 16:20:00', NULL, NULL, '2026-06-28 16:15:00'),
(14, 'PO20260625014', 2, 1, 25.00, 0.00, 25.00, 2, NULL, '2026-06-25 20:00:00', '2026-06-26 09:00:00', NULL, '2026-06-25 19:55:00');

-- ========== 补充 order_item ==========
INSERT IGNORE INTO order_item (id, order_id, product_id, product_name, product_image, price, quantity) VALUES
(5, 5, 16, '泰迪犬·巧克力', 'https://images.unsplash.com/photo-1534361960057-19889db9621e?w=200', 2600.00, 1),
(6, 5, 17, '猫抓板·仙人掌', 'https://images.unsplash.com/photo-1601758228041-f3b2795255f1?w=200', 49.00, 1),
(7, 6, 20, '虎皮鹦鹉·彩虹', 'https://images.unsplash.com/photo-1552921289-7a7b0d0b76f8?w=200', 168.00, 1),
(8, 6, 21, '鹦鹉站架', 'https://images.unsplash.com/photo-1552728089-57bdde30beb3?w=200', 128.00, 1),
(9, 7, 15, '宠物窝垫 M号', 'https://images.unsplash.com/photo-1516734212186-a967f81ad0d7?w=200', 89.00, 1),
(10, 7, 24, '神仙鱼·熊猫', 'https://images.unsplash.com/photo-1520366498724-709889c0c685?w=200', 38.00, 1),
(11, 7, 27, '斗鱼·半月', 'https://images.unsplash.com/photo-1535591273668-578e31182c4f?w=200', 88.00, 1),
(12, 7, 28, '鱼粮套餐', 'https://images.unsplash.com/photo-1589924691995-400dc9ecc119?w=200', 45.00, 1),
(13, 7, 25, '水族箱 60cm', 'https://images.unsplash.com/photo-1535591273668-578e31182c4f?w=200', 399.00, 1),
(14, 7, 26, '水草种子套装', 'https://images.unsplash.com/photo-1520366498724-709889c0c685?w=200', 35.00, 1),
(15, 8, 24, '神仙鱼·熊猫', 'https://images.unsplash.com/photo-1520366498724-709889c0c685?w=200', 38.00, 3),
(16, 8, 25, '水族箱 60cm', 'https://images.unsplash.com/photo-1535591273668-578e31182c4f?w=200', 399.00, 1),
(17, 9, 15, '宠物窝垫 M号', 'https://images.unsplash.com/photo-1516734212186-a967f81ad0d7?w=200', 89.00, 1),
(18, 10, 27, '斗鱼·半月', 'https://images.unsplash.com/photo-1535591273668-578e31182c4f?w=200', 88.00, 1),
(19, 10, 28, '鱼粮套餐', 'https://images.unsplash.com/photo-1589924691995-400dc9ecc119?w=200', 45.00, 1),
(20, 11, 18, '仓鼠套餐·豪华版', 'https://images.unsplash.com/photo-1425082661705-1834bfd09dca?w=200', 198.00, 1),
(21, 11, 22, '龙猫·银斑', 'https://images.unsplash.com/photo-1585110396000-c9ffd4e4b308?w=200', 1280.00, 1),
(22, 11, 23, '兔子提摩西草 500g', 'https://images.unsplash.com/photo-1589924691995-400dc9ecc119?w=200', 25.00, 1),
(23, 11, 19, '宠物龟·巴西龟', 'https://images.unsplash.com/photo-1535591273668-578e31182c4f?w=200', 28.00, 1),
(24, 12, 16, '泰迪犬·巧克力', 'https://images.unsplash.com/photo-1534361960057-19889db9621e?w=200', 2600.00, 1),
(25, 13, 27, '斗鱼·半月', 'https://images.unsplash.com/photo-1535591273668-578e31182c4f?w=200', 88.00, 1),
(26, 14, 23, '兔子提摩西草 500g', 'https://images.unsplash.com/photo-1589924691995-400dc9ecc119?w=200', 25.00, 1);

-- ========== 补充 evaluate（部分订单已评价） ==========
UPDATE order_item SET evaluate_content = '猫咪非常可爱，健康活泼，物流也很快！', evaluate_star = 5, evaluate_time = '2026-06-25 10:00:00' WHERE id = 4;
UPDATE order_item SET evaluate_content = '鹦鹉会说话了！太惊喜了~', evaluate_star = 5, evaluate_time = '2026-06-24 12:00:00' WHERE id = 7;
UPDATE order_item SET evaluate_content = '站架质量不错，鹦鹉很喜欢', evaluate_star = 4, evaluate_time = '2026-06-24 12:05:00' WHERE id = 8;
UPDATE order_item SET evaluate_content = '仓鼠套餐很齐全，小朋友很喜欢', evaluate_star = 4, evaluate_time = '2026-06-22 09:00:00' WHERE id = 20;
UPDATE order_item SET evaluate_content = '龙猫超级可爱！毛色漂亮', evaluate_star = 5, evaluate_time = '2026-06-22 09:05:00' WHERE id = 21;
UPDATE order_item SET evaluate_content = '草很新鲜，兔子爱吃', evaluate_star = 4, evaluate_time = '2026-06-22 09:10:00' WHERE id = 22;
UPDATE order_item SET evaluate_content = '小乌龟很健康，小朋友的宠物', evaluate_star = 4, evaluate_time = '2026-06-22 09:15:00' WHERE id = 23;

UPDATE video v
LEFT JOIN (
    SELECT video_id, COUNT(*) AS cnt
    FROM comment
    WHERE deleted = 0 AND status = 1
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
UPDATE purchase_order SET address_snapshot = '{"receiverName":"张三","phone":"13800138000","province":"福建省","city":"厦门市","district":"集美区","detail":"理工路600号"}' WHERE id IN (1,2,3,4,10,12,14);
UPDATE purchase_order SET address_snapshot = '{"receiverName":"李四","phone":"13600001111","province":"福建省","city":"厦门市","district":"湖里区","detail":"湖里大道88号"}' WHERE id IN (5,6,13);
UPDATE purchase_order SET address_snapshot = '{"receiverName":"李四","phone":"13600001112","province":"福建省","city":"厦门市","district":"思明区","detail":"厦禾路200号"}' WHERE id = 7;
UPDATE purchase_order SET address_snapshot = '{"receiverName":"王五","phone":"13600002222","province":"福建省","city":"厦门市","district":"集美区","detail":"杏林湾路1号"}' WHERE id IN (8,9,11);
