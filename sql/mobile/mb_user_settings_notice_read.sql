-- 用户设置与公告已读（P1）
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `mb_user_settings` (
  `user_id` varchar(32) NOT NULL COMMENT 'sys_user.user_id',
  `settings_json` text NOT NULL COMMENT '通知/隐私等偏好 JSON',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='移动端用户偏好设置';

CREATE TABLE IF NOT EXISTS `mb_notice_read` (
  `user_id` varchar(32) NOT NULL COMMENT 'sys_user.user_id',
  `notice_id` varchar(32) NOT NULL COMMENT 'school_notice.notice_id',
  `read_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`, `notice_id`),
  KEY `idx_mb_notice_read_user` (`user_id`, `read_time` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='移动端公告已读记录';
