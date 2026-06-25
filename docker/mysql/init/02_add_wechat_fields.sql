-- ============================================
-- 微信登录支持 - 新增字段
-- ============================================

ALTER TABLE user
    ADD COLUMN openid  VARCHAR(64)  COMMENT '微信openid' AFTER email,
    ADD COLUMN unionid VARCHAR(64)  COMMENT '微信unionid' AFTER openid;

ALTER TABLE user
    ADD UNIQUE INDEX idx_openid (openid);
