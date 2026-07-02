-- 警告：回滚会删除本轮新增字段，并永久丢失其中已经产生的门店审核、关闭及商品下架信息。
-- 仅删除本轮新增的7个字段；可重复执行。
SET @schema_name = DATABASE();

SET @ddl = IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='store' AND COLUMN_NAME='close_reason'), 'ALTER TABLE store DROP COLUMN close_reason', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @ddl = IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='store' AND COLUMN_NAME='audit_remark'), 'ALTER TABLE store DROP COLUMN audit_remark', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @ddl = IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='store' AND COLUMN_NAME='audit_time'), 'ALTER TABLE store DROP COLUMN audit_time', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @ddl = IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='store' AND COLUMN_NAME='audit_user_id'), 'ALTER TABLE store DROP COLUMN audit_user_id', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @ddl = IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='product' AND COLUMN_NAME='offline_time'), 'ALTER TABLE product DROP COLUMN offline_time', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @ddl = IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='product' AND COLUMN_NAME='offline_user_id'), 'ALTER TABLE product DROP COLUMN offline_user_id', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @ddl = IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='product' AND COLUMN_NAME='offline_reason'), 'ALTER TABLE product DROP COLUMN offline_reason', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
