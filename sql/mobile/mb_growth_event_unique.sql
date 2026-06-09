-- mb_growth_event 来源去重唯一索引（可重复执行）
SET NAMES utf8mb4;

SET @db = DATABASE();

SET @sql = IF(
  (SELECT COUNT(*) FROM information_schema.STATISTICS
   WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'mb_growth_event' AND INDEX_NAME = 'uk_ge_source') = 0,
  'ALTER TABLE `mb_growth_event` ADD UNIQUE KEY `uk_ge_source` (`user_id`, `source_type`, `source_id`)',
  'SELECT ''skip uk_ge_source'' AS msg'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
