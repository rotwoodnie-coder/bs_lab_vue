-- ============================================================
-- 勋章 · 积分功能增量 DDL（可重复执行）
-- 前置：mobile_feature_tables.sql、mobile_required_tables.sql
-- 说明：mb_growth_event 若已由 mobile_optional_demo_tables.sql 创建，
--       source_type / source_id 已存在，本脚本会自动跳过
-- ============================================================

SET NAMES utf8mb4;

SET @db = DATABASE();

-- mb_badge_def.reward_points
SET @sql = IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'mb_badge_def' AND COLUMN_NAME = 'reward_points') = 0,
  'ALTER TABLE `mb_badge_def` ADD COLUMN `reward_points` int NOT NULL DEFAULT 0 COMMENT ''获得勋章奖励积分'' AFTER `criteria_value`',
  'SELECT ''skip mb_badge_def.reward_points'' AS msg'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- mb_work.is_featured
SET @sql = IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'mb_work' AND COLUMN_NAME = 'is_featured') = 0,
  'ALTER TABLE `mb_work` ADD COLUMN `is_featured` varchar(1) NOT NULL DEFAULT ''n'' COMMENT ''教师标记展示 y/n'' AFTER `review_status`',
  'SELECT ''skip mb_work.is_featured'' AS msg'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS `mb_points_ledger` (
  `ledger_id` varchar(32) NOT NULL COMMENT '主键',
  `user_id` varchar(32) NOT NULL COMMENT '用户 id',
  `delta` int NOT NULL COMMENT '变动值',
  `balance_after` int NOT NULL COMMENT '变动后积分',
  `source_type` varchar(30) NOT NULL COMMENT 'quiz/work/task/badge/featured/manual',
  `source_id` varchar(32) DEFAULT NULL COMMENT '来源 id',
  `remark` varchar(200) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`ledger_id`),
  UNIQUE KEY `uk_points_source` (`user_id`, `source_type`, `source_id`),
  KEY `idx_points_user_time` (`user_id`, `create_time` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='积分流水';

-- mb_growth_event.source_type（demo 表已含此列时跳过）
SET @sql = IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'mb_growth_event' AND COLUMN_NAME = 'source_type') = 0,
  'ALTER TABLE `mb_growth_event` ADD COLUMN `source_type` varchar(20) DEFAULT NULL COMMENT ''来源类型'' AFTER `badge_class`',
  'SELECT ''skip mb_growth_event.source_type'' AS msg'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- mb_growth_event.source_id
SET @sql = IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'mb_growth_event' AND COLUMN_NAME = 'source_id') = 0,
  'ALTER TABLE `mb_growth_event` ADD COLUMN `source_id` varchar(32) DEFAULT NULL COMMENT ''来源 id'' AFTER `source_type`',
  'SELECT ''skip mb_growth_event.source_id'' AS msg'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE `mb_badge_def` SET `reward_points` = 50 WHERE `badge_id` = 'exp-master' AND `reward_points` = 0;
UPDATE `mb_badge_def` SET `reward_points` = 30 WHERE `badge_id` = 'science-star' AND `reward_points` = 0;
UPDATE `mb_badge_def` SET `reward_points` = 20 WHERE `badge_id` = 'creative' AND `reward_points` = 0;
UPDATE `mb_badge_def` SET `reward_points` = 15 WHERE `badge_id` = 'record' AND `reward_points` = 0;
UPDATE `mb_badge_def` SET `reward_points` = 10 WHERE `badge_id` = 'quiz-starter' AND `reward_points` = 0;
