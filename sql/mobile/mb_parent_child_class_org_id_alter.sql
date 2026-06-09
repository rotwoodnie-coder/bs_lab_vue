-- ============================================================

-- 仅补加 class_org_id（审核列已存在时使用）

-- ============================================================



SET @db = DATABASE();

SET @exists = (

  SELECT COUNT(*) FROM information_schema.COLUMNS

  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'mb_parent_child' AND COLUMN_NAME = 'class_org_id'

);

SET @sql = IF(@exists = 0,

  'ALTER TABLE `mb_parent_child` ADD COLUMN `class_org_id` varchar(32) DEFAULT NULL COMMENT ''class org id when applying'' AFTER `class_name`',

  'SELECT ''[SKIP] class_org_id already exists'' AS migration_msg'

);

PREPARE stmt FROM @sql;

EXECUTE stmt;

DEALLOCATE PREPARE stmt;

