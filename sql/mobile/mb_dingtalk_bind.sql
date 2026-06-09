-- 钉钉账号绑定（P2 · 仅钉钉）
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `mb_dingtalk_bind` (
  `user_id` varchar(32) NOT NULL COMMENT 'sys_user.user_id',
  `ding_union_id` varchar(64) NOT NULL COMMENT '钉钉 unionId',
  `ding_open_id` varchar(64) DEFAULT NULL COMMENT '钉钉 openId',
  `ding_nick` varchar(100) DEFAULT NULL COMMENT '钉钉昵称',
  `bind_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_mb_dingtalk_union` (`ding_union_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='移动端钉钉绑定';
