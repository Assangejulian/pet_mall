-- 门店审核与商品监管增量升级（可重复执行）
-- 仅新增批准的7个可空字段，不修改旧数据。
SET @schema_name = DATABASE();

SET @ddl = IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='store' AND COLUMN_NAME='audit_user_id'), 'SELECT 1', 'ALTER TABLE store ADD COLUMN audit_user_id BIGINT NULL COMMENT ''审核人员ID'' AFTER status');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @ddl = IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='store' AND COLUMN_NAME='audit_time'), 'SELECT 1', 'ALTER TABLE store ADD COLUMN audit_time DATETIME(3) NULL COMMENT ''审核时间'' AFTER audit_user_id');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @ddl = IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='store' AND COLUMN_NAME='audit_remark'), 'SELECT 1', 'ALTER TABLE store ADD COLUMN audit_remark VARCHAR(500) NULL COMMENT ''审核意见或驳回原因'' AFTER audit_time');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @ddl = IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='store' AND COLUMN_NAME='close_reason'), 'SELECT 1', 'ALTER TABLE store ADD COLUMN close_reason VARCHAR(500) NULL COMMENT ''关闭原因'' AFTER audit_remark');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @ddl = IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='product' AND COLUMN_NAME='offline_reason'), 'SELECT 1', 'ALTER TABLE product ADD COLUMN offline_reason VARCHAR(500) NULL COMMENT ''平台强制下架原因'' AFTER video_id');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @ddl = IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='product' AND COLUMN_NAME='offline_user_id'), 'SELECT 1', 'ALTER TABLE product ADD COLUMN offline_user_id BIGINT NULL COMMENT ''平台下架操作人员ID'' AFTER offline_reason');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @ddl = IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='product' AND COLUMN_NAME='offline_time'), 'SELECT 1', 'ALTER TABLE product ADD COLUMN offline_time DATETIME(3) NULL COMMENT ''平台下架时间'' AFTER offline_user_id');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
