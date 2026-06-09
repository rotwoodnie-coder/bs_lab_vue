-- 每日答题：全局配置 + 多题记录扩展
-- 执行前请备份；可重复执行（部分语句需手动确认是否已存在列）

CREATE TABLE IF NOT EXISTS `mb_quiz_config` (
  `config_id` varchar(32) NOT NULL DEFAULT 'default' COMMENT '固定 default',
  `questions_per_day` int NOT NULL DEFAULT 3 COMMENT '每日题数 1~10',
  `base_points` int NOT NULL DEFAULT 10 COMMENT '全对基础积分',
  `streak_bonus` int NOT NULL DEFAULT 5 COMMENT '连对额外积分',
  `enabled` varchar(1) NOT NULL DEFAULT 'y' COMMENT 'y 启用 n 关闭',
  `update_by` varchar(32) DEFAULT NULL,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`config_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='每日答题全局配置';

INSERT INTO `mb_quiz_config` (`config_id`, `questions_per_day`, `base_points`, `streak_bonus`, `enabled`)
VALUES ('default', 3, 10, 5, 'y')
ON DUPLICATE KEY UPDATE `config_id` = `config_id`;

-- 扩展答题记录（列已存在时会报错，可忽略）
ALTER TABLE `mb_quiz_record`
  ADD COLUMN `question_ids_json` text COMMENT '当日题目 ID 列表 JSON' AFTER `question_id`;
