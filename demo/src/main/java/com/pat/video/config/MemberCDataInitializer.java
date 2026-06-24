package com.pat.video.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class MemberCDataInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Value("${app.seed.member-c:true}")
    private boolean enabled;

    public MemberCDataInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!enabled) {
            return;
        }
        ensureSchema();
        seedDemoData();
    }

    private void ensureSchema() {
        safeExecute("ALTER TABLE video ADD COLUMN user_id BIGINT NULL COMMENT '发布用户ID' AFTER id");
        safeExecute("ALTER TABLE video ADD COLUMN description VARCHAR(500) NULL COMMENT '视频描述' AFTER title");
        safeExecute("ALTER TABLE video ADD COLUMN likes INT NOT NULL DEFAULT 0 COMMENT '点赞数' AFTER play_count");
        safeExecute("ALTER TABLE video ADD COLUMN comment_count INT NOT NULL DEFAULT 0 COMMENT '评论数' AFTER likes");
        safeExecute("ALTER TABLE video ADD COLUMN duration INT NULL COMMENT '时长秒' AFTER comment_count");
        safeExecute("ALTER TABLE video ADD INDEX idx_video_user_id (user_id)");
        safeExecute("ALTER TABLE video ADD INDEX idx_video_status_create_time (status, create_time)");

        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS comment (
                    id BIGINT NOT NULL PRIMARY KEY COMMENT '雪花ID',
                    video_id BIGINT NOT NULL COMMENT '视频ID',
                    user_id BIGINT COMMENT '用户ID',
                    content TEXT NOT NULL COMMENT '评论内容',
                    create_time DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
                    update_time DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) NOT NULL,
                    INDEX idx_comment_video_id (video_id),
                    INDEX idx_comment_user_id (user_id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视频评论'
                """);

        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS ai_chat_record (
                    id BIGINT NOT NULL PRIMARY KEY COMMENT '雪花ID',
                    user_id BIGINT COMMENT '用户ID',
                    session_id VARCHAR(64) NOT NULL COMMENT '会话ID',
                    role VARCHAR(20) NOT NULL COMMENT '角色 user/assistant',
                    content TEXT NOT NULL COMMENT '消息内容',
                    create_time DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
                    INDEX idx_session_id (session_id),
                    INDEX idx_user_id (user_id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI对话记录'
                """);
    }

    private void seedDemoData() {
        seedUsers();
        seedVideos();
        seedComments();
    }

    private void seedUsers() {
        jdbcTemplate.update("""
                        INSERT INTO user(id, username, password, role, status, real_name, avatar)
                        VALUES (?, ?, ?, ?, ?, ?, ?)
                        ON DUPLICATE KEY UPDATE real_name = VALUES(real_name), avatar = VALUES(avatar)
                        """,
                910000000000000101L,
                "warmcat",
                "123456",
                "user",
                1,
                "暖窝小鱼",
                "https://images.unsplash.com/photo-1517849845537-4d257902454a?auto=format&fit=crop&w=200&q=80");
        jdbcTemplate.update("""
                        INSERT INTO user(id, username, password, role, status, real_name, avatar)
                        VALUES (?, ?, ?, ?, ?, ?, ?)
                        ON DUPLICATE KEY UPDATE real_name = VALUES(real_name), avatar = VALUES(avatar)
                        """,
                910000000000000102L,
                "warmdog",
                "123456",
                "user",
                1,
                "布偶田田",
                "https://images.unsplash.com/photo-1548199973-03cce0bbc87b?auto=format&fit=crop&w=200&q=80");
    }

    private void seedVideos() {
        insertVideo(
                910000000000001001L,
                910000000000000101L,
                "第一次接它回家",
                "从到家动线、隔离区到第一晚观察，把小家伙安稳接回家。",
                "https://samplelib.com/preview/mp4/sample-5s.mp4",
                "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?auto=format&fit=crop&w=900&q=80",
                1280,
                2300,
                18,
                8);
        insertVideo(
                910000000000001002L,
                910000000000000102L,
                "狗狗兴奋乱扑怎么办",
                "先让它学会坐下等待，再把奖励和社交绑定起来。",
                "https://samplelib.com/preview/mp4/sample-10s.mp4",
                "https://images.unsplash.com/photo-1552053831-71594a27632d?auto=format&fit=crop&w=900&q=80",
                980,
                1860,
                24,
                4);
        insertVideo(
                910000000000001003L,
                910000000000000101L,
                "猫咪食欲变差怎么办",
                "排查换粮、温度、压力和精神状态，先观察重点信号。",
                "https://samplelib.com/preview/mp4/sample-15s.mp4",
                "https://images.unsplash.com/photo-1573865526739-10659fec78a5?auto=format&fit=crop&w=900&q=80",
                764,
                1420,
                31,
                12);
        insertVideo(
                910000000000001004L,
                910000000000000102L,
                "幼宠用品清单",
                "笼具、食盆、牵引和清洁用品先准备基础款，别一开始买太多。",
                "https://samplelib.com/preview/mp4/sample-20s.mp4",
                "https://images.unsplash.com/photo-1583337130417-3346a1be7dee?auto=format&fit=crop&w=900&q=80",
                640,
                1190,
                14,
                49);
    }

    private void insertVideo(
            Long id,
            Long userId,
            String title,
            String description,
            String url,
            String cover,
            Integer likes,
            Integer playCount,
            Integer commentCount,
            Integer duration) {
        jdbcTemplate.update("""
                        INSERT INTO video(
                            id, user_id, title, description, url, cover,
                            product_id, play_count, likes, comment_count, duration,
                            status, deleted, create_time, update_time
                        )
                        VALUES (?, ?, ?, ?, ?, ?, NULL, ?, ?, ?, ?, 1, 0, NOW(3), NOW(3))
                        ON DUPLICATE KEY UPDATE
                            title = VALUES(title),
                            description = VALUES(description),
                            url = VALUES(url),
                            cover = VALUES(cover),
                            likes = VALUES(likes),
                            play_count = VALUES(play_count),
                            comment_count = VALUES(comment_count),
                            duration = VALUES(duration),
                            status = 1,
                            deleted = 0
                        """,
                id, userId, title, description, url, cover, playCount, likes, commentCount, duration);
    }

    private void seedComments() {
        insertComment(910000000000002001L, 910000000000001001L, 910000000000000102L, "隔离区这个点很有用，第一晚确实别太频繁打扰。");
        insertComment(910000000000002002L, 910000000000001002L, 910000000000000101L, "坐下等待比直接压住它有效多了。");
        insertComment(910000000000002003L, 910000000000001003L, 910000000000000102L, "食欲和精神状态一起看，这个提醒很关键。");
    }

    private void insertComment(Long id, Long videoId, Long userId, String content) {
        jdbcTemplate.update("""
                        INSERT INTO comment(id, video_id, user_id, content, create_time, update_time)
                        VALUES (?, ?, ?, ?, NOW(3), NOW(3))
                        ON DUPLICATE KEY UPDATE content = VALUES(content)
                        """,
                id, videoId, userId, content);
    }

    private void safeExecute(String sql) {
        try {
            jdbcTemplate.execute(sql);
        } catch (DataAccessException ignored) {
            // Existing columns/indexes are expected when the dev database was initialized before this module.
        }
    }
}
