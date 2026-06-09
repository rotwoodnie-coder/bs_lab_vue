-- 学生成长档案 · 展示方案（按用户持久化）
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `mb_growth_plan` (
  `user_id` varchar(32) NOT NULL COMMENT 'sys_user.user_id',
  `content_keys_json` varchar(200) NOT NULL DEFAULT '["exp","work","badge","quiz"]' COMMENT '展示内容 keys JSON 数组',
  `visibility` varchar(20) NOT NULL DEFAULT 'parent' COMMENT 'self / parent / class',
  `range` varchar(20) NOT NULL DEFAULT 'term' COMMENT 'term / year / all',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='移动端成长展示方案';
