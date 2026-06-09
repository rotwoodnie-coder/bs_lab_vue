-- ============================================================
-- 移动端 · 可选演示表（Tier 2）
-- 用途：勋章墙 / 成长档案 / 评论 demo seed
-- 无这些表时，对应 API 会回退 MobilePrototypeData
-- 决策文档：docs/mobile-sql-decision.md
-- ============================================================

SET NAMES utf8mb4;

-- ----------------------------
-- 评论（作品 / 实验）— 等 Comment API 落地后再执行亦可
-- ----------------------------
CREATE TABLE IF NOT EXISTS `mb_comment` (
  `comment_id` varchar(32) NOT NULL COMMENT '主键',
  `target_type` varchar(20) NOT NULL COMMENT '目标类型：work / exp_msg',
  `target_id` varchar(32) NOT NULL COMMENT '目标 id',
  `user_id` varchar(32) NOT NULL COMMENT '评论用户 id',
  `user_name` varchar(60) DEFAULT NULL COMMENT '评论用户显示名',
  `user_role_tag` varchar(20) DEFAULT NULL COMMENT '角色标签：author / teacher',
  `content` text NOT NULL COMMENT '评论内容',
  `like_count` int NOT NULL DEFAULT 0 COMMENT '点赞数',
  `parent_comment_id` varchar(32) DEFAULT NULL COMMENT '父评论 id（回复）',
  `status` varchar(1) NOT NULL DEFAULT 'y' COMMENT '状态 y/n',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`comment_id`),
  KEY `idx_mb_comment_target` (`target_type`, `target_id`, `create_time` DESC),
  KEY `idx_mb_comment_user` (`user_id`, `create_time` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='移动端评论';

-- ----------------------------
-- 勋章定义 + 用户进度
-- ----------------------------
CREATE TABLE IF NOT EXISTS `mb_badge_def` (
  `badge_id` varchar(32) NOT NULL COMMENT '主键',
  `icon` varchar(20) NOT NULL COMMENT 'emoji 图标',
  `title` varchar(100) NOT NULL COMMENT '勋章名',
  `description` varchar(200) DEFAULT NULL COMMENT '获得条件说明',
  `criteria_type` varchar(50) DEFAULT NULL COMMENT '判定类型：exp_count/quiz_count/...',
  `criteria_value` int DEFAULT NULL COMMENT '判定阈值',
  `action_route` varchar(100) DEFAULT NULL COMMENT '未获得时跳转路由',
  `sort_order` int NOT NULL DEFAULT 0,
  `status` varchar(1) NOT NULL DEFAULT 'y',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`badge_id`),
  KEY `idx_mb_badge_def_sort` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='移动端勋章定义';

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

-- ----------------------------
-- 成长轨迹（后期可改为 work/quiz 事件聚合）
-- ----------------------------
CREATE TABLE IF NOT EXISTS `mb_growth_event` (
  `event_id` varchar(32) NOT NULL COMMENT '主键',
  `user_id` varchar(32) NOT NULL COMMENT '学生 user_id',
  `event_time_label` varchar(50) NOT NULL COMMENT '展示时间文案',
  `sort_time` datetime NOT NULL COMMENT '排序时间',
  `emoji` varchar(10) DEFAULT NULL,
  `title` varchar(200) NOT NULL,
  `hint` varchar(500) DEFAULT NULL,
  `badges_json` text COMMENT '附加 badge 数组 JSON',
  `points_label` varchar(50) DEFAULT NULL COMMENT '积分/排名标签',
  `dot_class` varchar(50) DEFAULT NULL COMMENT 'timeline dot',
  `badge_class` varchar(50) DEFAULT NULL COMMENT '右侧 badge class',
  `source_type` varchar(20) DEFAULT NULL COMMENT '来源：work/quiz/badge/manual',
  `source_id` varchar(32) DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`event_id`),
  KEY `idx_mb_growth_user` (`user_id`, `sort_time` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='移动端成长轨迹';

-- ----------------------------
-- 点赞/收藏/播放（做 Reaction API 时再执行本段）
-- ----------------------------
CREATE TABLE IF NOT EXISTS `mb_user_reaction` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` varchar(32) NOT NULL COMMENT '用户id',
  `target_id` varchar(32) NOT NULL COMMENT '目标id（exp_msg.exp_id / mb_work.work_id）',
  `target_type` varchar(20) NOT NULL COMMENT '目标类型：exp_msg / exp_video / work',
  `reaction_type` varchar(20) NOT NULL COMMENT '反应类型：like / collect / play',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_target_reaction` (`user_id`, `target_id`, `target_type`, `reaction_type`),
  KEY `idx_user_reaction_time` (`user_id`, `created_at` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='移动端用户互动记录';
