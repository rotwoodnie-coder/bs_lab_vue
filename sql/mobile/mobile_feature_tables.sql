-- ============================================================
-- 移动端 · 功能必需表（Tier 1.5）
-- 用途：勋章正常化 + 全站社交（实验/模拟实验/作品）
-- 决策：docs/mobile-sql-decision.md · docs/mobile-no-mock-analysis.md §十一
-- 说明：自 mobile_optional_demo_tables.sql 升格；不含 growth_event（仍可选）
-- ============================================================

SET NAMES utf8mb4;

-- 评论（实验 exp_msg / 模拟实验 exp_simulator / 作品 work）
CREATE TABLE IF NOT EXISTS `mb_comment` (
  `comment_id` varchar(32) NOT NULL COMMENT '主键',
  `target_type` varchar(20) NOT NULL COMMENT 'exp_msg / exp_simulator / exp_video / work',
  `target_id` varchar(32) NOT NULL COMMENT '目标 id',
  `user_id` varchar(32) NOT NULL COMMENT '评论用户 id',
  `user_name` varchar(60) DEFAULT NULL COMMENT '评论用户显示名',
  `user_role_tag` varchar(20) DEFAULT NULL COMMENT 'student / teacher / researcher / parent / author',
  `content` text NOT NULL COMMENT '评论内容',
  `like_count` int NOT NULL DEFAULT 0 COMMENT '评论点赞数',
  `parent_comment_id` varchar(32) DEFAULT NULL COMMENT '父评论 id（回复）',
  `status` varchar(1) NOT NULL DEFAULT 'y' COMMENT 'y 正常 n 软删（移动端不可见）',
  `deleted_by` varchar(32) DEFAULT NULL COMMENT '软删操作人 sys_user.user_id',
  `deleted_time` datetime DEFAULT NULL COMMENT '软删时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`comment_id`),
  KEY `idx_mb_comment_target` (`target_type`, `target_id`, `create_time` DESC),
  KEY `idx_mb_comment_user` (`user_id`, `create_time` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='移动端评论';

-- 点赞 / 收藏 / 播放记录
CREATE TABLE IF NOT EXISTS `mb_user_reaction` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` varchar(32) NOT NULL COMMENT '用户 id',
  `target_id` varchar(32) NOT NULL COMMENT '目标 id',
  `target_type` varchar(20) NOT NULL COMMENT 'exp_msg / exp_simulator / exp_video / work',
  `reaction_type` varchar(20) NOT NULL COMMENT 'like / collect / play',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_target_reaction` (`user_id`, `target_id`, `target_type`, `reaction_type`),
  KEY `idx_user_reaction_target` (`target_type`, `target_id`, `reaction_type`),
  KEY `idx_user_reaction_time` (`user_id`, `created_at` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='移动端用户互动';

-- 勋章定义
CREATE TABLE IF NOT EXISTS `mb_badge_def` (
  `badge_id` varchar(32) NOT NULL COMMENT '主键',
  `icon` varchar(20) NOT NULL COMMENT 'emoji 图标',
  `title` varchar(100) NOT NULL COMMENT '勋章名',
  `description` varchar(200) DEFAULT NULL COMMENT '获得条件说明',
  `criteria_type` varchar(50) DEFAULT NULL COMMENT 'exp_count/work_first/quiz_correct/...',
  `criteria_value` int DEFAULT NULL COMMENT '判定阈值',
  `reward_points` int NOT NULL DEFAULT 0 COMMENT '获得勋章奖励积分',
  `action_route` varchar(100) DEFAULT NULL COMMENT '未获得时跳转路由',
  `sort_order` int NOT NULL DEFAULT 0,
  `status` varchar(1) NOT NULL DEFAULT 'y',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`badge_id`),
  KEY `idx_mb_badge_def_sort` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='移动端勋章定义';

-- 用户勋章进度
CREATE TABLE IF NOT EXISTS `mb_badge_progress` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` varchar(32) NOT NULL COMMENT '用户 id',
  `badge_id` varchar(32) NOT NULL COMMENT '勋章 id',
  `earned` varchar(1) NOT NULL DEFAULT 'n' COMMENT '是否已获得 y/n',
  `progress_current` int NOT NULL DEFAULT 0 COMMENT '当前进度',
  `progress_target` int NOT NULL DEFAULT 0 COMMENT '目标进度',
  `earned_time` datetime DEFAULT NULL COMMENT '获得时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_mb_badge_user` (`user_id`, `badge_id`),
  KEY `idx_mb_badge_progress_user` (`user_id`, `earned`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='移动端用户勋章进度';
