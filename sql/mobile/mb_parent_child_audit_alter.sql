-- ============================================================

-- 家长绑定审核字段（产品规格 9.5 · 可重复执行）

-- 前置：mobile_required_tables.sql

-- 关联：docs/mobile-product-spec.md §2.2

--

-- 若某列已存在，对应步骤会自动跳过，不会报 Duplicate column。

-- ============================================================



SET @db = DATABASE();



-- class_org_id：绑定申请时的班级组织 id（教师审核范围匹配）

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



-- confirm_user_id

SET @exists = (

  SELECT COUNT(*) FROM information_schema.COLUMNS

  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'mb_parent_child' AND COLUMN_NAME = 'confirm_user_id'

);

SET @sql = IF(@exists = 0,

  'ALTER TABLE `mb_parent_child` ADD COLUMN `confirm_user_id` varchar(32) DEFAULT NULL COMMENT ''auditor user_id'' AFTER `bind_status`',

  'SELECT ''[SKIP] confirm_user_id already exists'' AS migration_msg'

);

PREPARE stmt FROM @sql;

EXECUTE stmt;

DEALLOCATE PREPARE stmt;



-- confirm_time

SET @exists = (

  SELECT COUNT(*) FROM information_schema.COLUMNS

  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'mb_parent_child' AND COLUMN_NAME = 'confirm_time'

);

SET @sql = IF(@exists = 0,

  'ALTER TABLE `mb_parent_child` ADD COLUMN `confirm_time` datetime DEFAULT NULL COMMENT ''audit time'' AFTER `confirm_user_id`',

  'SELECT ''[SKIP] confirm_time already exists'' AS migration_msg'

);

PREPARE stmt FROM @sql;

EXECUTE stmt;

DEALLOCATE PREPARE stmt;



-- reject_reason

SET @exists = (

  SELECT COUNT(*) FROM information_schema.COLUMNS

  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'mb_parent_child' AND COLUMN_NAME = 'reject_reason'

);

SET @sql = IF(@exists = 0,

  'ALTER TABLE `mb_parent_child` ADD COLUMN `reject_reason` varchar(500) DEFAULT NULL COMMENT ''reject reason'' AFTER `confirm_time`',

  'SELECT ''[SKIP] reject_reason already exists'' AS migration_msg'

);

PREPARE stmt FROM @sql;

EXECUTE stmt;

DEALLOCATE PREPARE stmt;



-- 已有库若默认 approved，生产应改为 pending（按需一次性执行）：

-- ALTER TABLE `mb_parent_child` MODIFY COLUMN `bind_status` varchar(20) NOT NULL DEFAULT 'pending';

