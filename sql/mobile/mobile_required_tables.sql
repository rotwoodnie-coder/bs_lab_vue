-- ============================================================
-- 移动端 · 必需表（Tier 1，开发必跑）
-- 库：bs_exp_vue（先具备 sql/bs_exp_vue.sql 基线）
-- 共 7 张表：任务/作品/答题/绑定/每日排期
-- 文档：docs/mobile-sql-decision.md · sql/mobile/README.md
-- ============================================================

SET NAMES utf8mb4;

-- ----------------------------
-- 教师布置任务 / 作业
-- ----------------------------
CREATE TABLE IF NOT EXISTS `mb_task` (
  `task_id` varchar(32) NOT NULL COMMENT '主键',
  `title` varchar(200) NOT NULL COMMENT '任务标题',
  `description` text COMMENT '任务说明',
  `task_type` varchar(20) NOT NULL DEFAULT 'homework' COMMENT '类型：homework / remix',
  `teacher_user_id` varchar(32) DEFAULT NULL COMMENT '布置教师 user_id',
  `class_org_id` varchar(32) DEFAULT NULL COMMENT '班级组织 id',
  `deadline` datetime DEFAULT NULL COMMENT '截止时间',
  `video_id` varchar(32) DEFAULT NULL COMMENT '关联视频/实验 id → exp_msg.exp_id',
  `video_title` varchar(200) DEFAULT NULL COMMENT '视频标题',
  `video_duration` varchar(50) DEFAULT NULL COMMENT '视频时长文案',
  `video_meta` varchar(200) DEFAULT NULL COMMENT '视频元信息',
  `teacher_hint` varchar(500) DEFAULT NULL COMMENT '教师提示',
  `teacher_hint_class` varchar(50) DEFAULT NULL COMMENT '提示样式 class',
  `task_type_label` varchar(50) DEFAULT NULL COMMENT '任务类型展示文案',
  `requirements_json` text COMMENT '提交要求 JSON 数组',
  `steps_json` text COMMENT '完成步骤 JSON 数组',
  `upload_query` varchar(100) DEFAULT NULL COMMENT '上传页 query 参数',
  `status` varchar(1) NOT NULL DEFAULT 'y' COMMENT '状态 y/n',
  `create_user_id` varchar(32) DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_user_id` varchar(32) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`task_id`),
  KEY `idx_mb_task_teacher` (`teacher_user_id`, `create_time` DESC),
  KEY `idx_mb_task_class` (`class_org_id`, `deadline`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='移动端任务/作业';

-- ----------------------------
-- 学生任务提交 / 进度
-- ----------------------------
CREATE TABLE IF NOT EXISTS `mb_task_submission` (
  `submission_id` varchar(32) NOT NULL COMMENT '主键',
  `task_id` varchar(32) NOT NULL COMMENT '任务 id',
  `student_user_id` varchar(32) NOT NULL COMMENT '学生 user_id',
  `state` varchar(20) NOT NULL DEFAULT 'pending' COMMENT 'pending/doing/done/submitted/reviewed',
  `state_label` varchar(50) DEFAULT NULL COMMENT '状态展示文案',
  `badge_class` varchar(50) DEFAULT NULL COMMENT '状态 badge class',
  `grade` varchar(20) DEFAULT NULL COMMENT '评级 A+/A/B 等',
  `review_comment` text COMMENT '教师评语',
  `reviewer_user_id` varchar(32) DEFAULT NULL COMMENT '批阅教师',
  `submit_time` datetime DEFAULT NULL COMMENT '提交时间',
  `review_time` datetime DEFAULT NULL COMMENT '批阅时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`submission_id`),
  UNIQUE KEY `uk_mb_task_student` (`task_id`, `student_user_id`),
  KEY `idx_mb_submission_student` (`student_user_id`, `update_time` DESC),
  KEY `idx_mb_submission_state` (`state`, `submit_time` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='移动端任务提交';

-- ----------------------------
-- 学生作品
-- ----------------------------
CREATE TABLE IF NOT EXISTS `mb_work` (
  `work_id` varchar(32) NOT NULL COMMENT '主键',
  `student_user_id` varchar(32) NOT NULL COMMENT '学生 user_id',
  `title` varchar(200) NOT NULL COMMENT '作品标题',
  `description` text COMMENT '作品描述',
  `work_type` varchar(20) NOT NULL DEFAULT 'homework' COMMENT 'homework/remix/creative',
  `task_id` varchar(32) DEFAULT NULL COMMENT '关联任务 id',
  `source_exp_id` varchar(32) DEFAULT NULL COMMENT '关联实验 exp_msg.exp_id（拍同款来源）',
  `submission_id` varchar(32) DEFAULT NULL COMMENT '关联提交 id',
  `grade` varchar(50) DEFAULT NULL COMMENT '批阅等级文案',
  `review_status` varchar(20) DEFAULT 'pending' COMMENT 'pending/reviewed',
  `is_featured` varchar(1) NOT NULL DEFAULT 'n' COMMENT '教师标记展示 y/n',
  `teacher_review_name` varchar(60) DEFAULT NULL COMMENT '批阅教师名',
  `teacher_review_text` text COMMENT '教师评语',
  `teacher_review_stars` int DEFAULT NULL COMMENT '星级 1-5',
  `like_count` int NOT NULL DEFAULT 0,
  `comment_count` int NOT NULL DEFAULT 0,
  `class_name` varchar(100) DEFAULT NULL COMMENT '班级展示名',
  `school_name` varchar(100) DEFAULT NULL COMMENT '学校名',
  `tint` varchar(50) DEFAULT NULL COMMENT '卡片色调 class',
  `status` varchar(1) NOT NULL DEFAULT 'y' COMMENT 'y/n',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`work_id`),
  KEY `idx_mb_work_student` (`student_user_id`, `create_time` DESC),
  KEY `idx_mb_work_type` (`work_type`, `create_time` DESC),
  KEY `idx_mb_work_review` (`review_status`, `create_time` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='移动端作品';

-- ----------------------------
-- 作品附件（照片/视频）
-- ----------------------------
CREATE TABLE IF NOT EXISTS `mb_work_file` (
  `file_id` varchar(32) NOT NULL COMMENT '主键',
  `work_id` varchar(32) NOT NULL COMMENT '作品 id',
  `file_type` varchar(20) NOT NULL DEFAULT 'image' COMMENT 'image/video',
  `file_name` varchar(200) DEFAULT NULL,
  `file_size` varchar(50) DEFAULT NULL,
  `file_url` varchar(500) DEFAULT NULL COMMENT '文件访问 URL',
  `grad_class` varchar(50) DEFAULT NULL COMMENT '预览渐变 class',
  `icon_emoji` varchar(10) DEFAULT NULL,
  `duration` varchar(20) DEFAULT NULL COMMENT '视频时长',
  `sort_order` int NOT NULL DEFAULT 0,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`file_id`),
  KEY `idx_mb_work_file_work` (`work_id`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='移动端作品附件';

-- ----------------------------
-- 每日答题记录
-- ----------------------------
CREATE TABLE IF NOT EXISTS `mb_quiz_record` (
  `record_id` varchar(32) NOT NULL COMMENT '主键',
  `user_id` varchar(32) NOT NULL COMMENT '用户 id',
  `question_id` varchar(32) DEFAULT NULL COMMENT '当日首题 → exp_question.question_id',
  `question_ids_json` text COMMENT '当日题目 ID 列表 JSON',
  `quiz_date` date NOT NULL COMMENT '答题日期',
  `score` int NOT NULL DEFAULT 0 COMMENT '答对题数',
  `total` int NOT NULL DEFAULT 3 COMMENT '总题数',
  `points` int NOT NULL DEFAULT 0 COMMENT '获得积分',
  `perfect` varchar(1) NOT NULL DEFAULT 'n' COMMENT '是否全对 y/n',
  `answers_json` text COMMENT '作答 JSON',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`record_id`),
  UNIQUE KEY `uk_mb_quiz_user_date` (`user_id`, `quiz_date`),
  KEY `idx_mb_quiz_user_time` (`user_id`, `create_time` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='移动端每日答题记录';

-- ----------------------------
-- 每日答题全局配置
-- ----------------------------
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

-- ----------------------------
-- 每日一题排期（已废弃：现由系统按用户+日期从题库自动分配，本表保留兼容）
-- ----------------------------
CREATE TABLE IF NOT EXISTS `mb_quiz_daily` (
  `daily_id` varchar(32) NOT NULL COMMENT '主键',
  `quiz_date` date NOT NULL COMMENT '答题日期',
  `question_id` varchar(32) NOT NULL COMMENT '题目 id → exp_question.question_id',
  `bonus_points` int NOT NULL DEFAULT 5 COMMENT '连对额外积分',
  `status` varchar(1) NOT NULL DEFAULT 'y' COMMENT 'y 启用 n 停用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`daily_id`),
  UNIQUE KEY `uk_mb_quiz_daily_date` (`quiz_date`),
  KEY `idx_mb_quiz_daily_question` (`question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='移动端每日答题排期';

-- ----------------------------
-- 家长-孩子绑定
-- ----------------------------
CREATE TABLE IF NOT EXISTS `mb_parent_child` (
  `bind_id` varchar(32) NOT NULL COMMENT '主键',
  `parent_user_id` varchar(32) NOT NULL COMMENT '家长 user_id',
  `child_user_id` varchar(32) NOT NULL COMMENT '孩子 user_id',
  `relation` varchar(20) DEFAULT NULL COMMENT '关系：妈妈/爸爸',
  `is_default` varchar(1) NOT NULL DEFAULT 'n' COMMENT '是否默认查看 y/n',
  `school_name` varchar(100) DEFAULT NULL,
  `grade_name` varchar(50) DEFAULT NULL,
  `class_name` varchar(50) DEFAULT NULL,
  `class_org_id` varchar(32) DEFAULT NULL COMMENT '绑定申请时的班级组织 id',
  `bind_status` varchar(20) NOT NULL DEFAULT 'approved' COMMENT 'pending/approved/rejected',
  `confirm_user_id` varchar(32) DEFAULT NULL COMMENT '审核人 user_id',
  `confirm_time` datetime DEFAULT NULL COMMENT '审核时间',
  `reject_reason` varchar(500) DEFAULT NULL COMMENT '驳回原因',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`bind_id`),
  UNIQUE KEY `uk_mb_parent_child` (`parent_user_id`, `child_user_id`),
  KEY `idx_mb_parent` (`parent_user_id`, `is_default`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='移动端家长孩子绑定';
