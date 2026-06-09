-- 移动端 JWT Refresh Token（Phase 0 认证）
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `mb_auth_refresh_token` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` varchar(32) NOT NULL COMMENT 'sys_user.user_id',
  `device_id` varchar(64) NOT NULL COMMENT '客户端设备标识',
  `token_hash` varchar(64) NOT NULL COMMENT 'refresh token SHA-256',
  `expire_time` datetime NOT NULL,
  `revoked` tinyint(1) NOT NULL DEFAULT 0,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `last_used_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_mb_auth_refresh_hash` (`token_hash`),
  KEY `idx_mb_auth_refresh_user_device` (`user_id`, `device_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='移动端 Refresh Token';
