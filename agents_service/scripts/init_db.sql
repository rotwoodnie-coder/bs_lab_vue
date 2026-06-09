-- ============================================================
-- BS Lab Agents Service — 完整数据库初始化脚本
-- ============================================================
-- 数据库规划（同一 MySQL 实例，两个独立数据库）：
--   业务库:         bs_exp_vue       ← 已有业务数据库
--      ai_chat_session              — 聊天会话
--      ai_chat_message              — 聊天消息
--   检查点库:       bs_exp_checkpoints  ← 新增（LangGraph 持久化专用）
--      langgraph_checkpoints        — 检查点快照
--      langgraph_checkpoint_writes  — 检查点中间写入
--
-- 使用方式：
--   mysql -h 10.0.181.204 -P 13306 -u root -p < scripts/init_db.sql
-- ============================================================


-- ============================================================
-- 第一部分：业务库（bs_exp_vue）
-- ============================================================

CREATE DATABASE IF NOT EXISTS `bs_exp_vue`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE `bs_exp_vue`;


-- ─── 1. 聊天会话表 ──────────────────────────────────────
-- ORM: AiChatSession
-- 用途：记录每个 AI 对话会话的元信息

CREATE TABLE IF NOT EXISTS `ai_chat_session` (
    `session_id`       VARCHAR(32)    NOT NULL COMMENT '会话主键',
    `user_id`          VARCHAR(32)    NOT NULL COMMENT '发起用户 id',
    `user_role`        VARCHAR(32)    NOT NULL DEFAULT 'student' COMMENT '用户角色（推荐: student/teacher/parent/researcher/admin）',
    `agent_type`       ENUM('student', 'teacher', 'researcher', 'parent', 'admin')
                                      DEFAULT 'student' COMMENT '智能体类型',
    `grade_level`      ENUM('低段', '中段', '高段')
                                      DEFAULT NULL COMMENT '年级属性：低段(1-2)/中段(3-4)/高段(5-6)',
    `current_stage`    ENUM('INIT', 'GOAL', 'MATERIAL', 'STEP', 'RECORD', 'CONCLUSION', 'FINAL')
                                      NOT NULL DEFAULT 'INIT' COMMENT '当前环节状态',
    `experiment_title` VARCHAR(256)   DEFAULT NULL COMMENT '实验标题（从对话中提取）',
    `experiment_id`    VARCHAR(32)    DEFAULT NULL COMMENT '关联实验主键',
    `summary`          TEXT           DEFAULT NULL COMMENT '会话摘要（最终产出的完整方案）',
    `is_active`        ENUM('y', 'n') NOT NULL DEFAULT 'y' COMMENT '会话是否有效',
    `create_time`      DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`      DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`session_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI 聊天会话';


-- ─── 2. 聊天消息表 ──────────────────────────────────────
-- ORM: AiChatMessage
-- 用途：存储对话中的每条消息

CREATE TABLE IF NOT EXISTS `ai_chat_message` (
    `message_id`   BIGINT         NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    `session_id`   VARCHAR(32)    NOT NULL COMMENT '所属会话 id',
    `role`         ENUM('user', 'assistant', 'system')
                                  NOT NULL COMMENT '消息角色',
    `content`      TEXT           NOT NULL COMMENT '消息内容',
    `metadata`     JSON           DEFAULT NULL COMMENT '附加元数据（如 token 用量、stage 快照等）',
    `create_time`  DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`message_id`),
    INDEX `idx_session_id` (`session_id`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI 聊天消息';


-- ============================================================
-- 第二部分：检查点库（bs_exp_checkpoints）
-- ============================================================
-- 独立数据库，专门承载 LangGraph checkpointer 的高频写入，
-- 可与业务库采用不同的性能参数（如 innodb_flush_log_at_trx_commit = 2）。

CREATE DATABASE IF NOT EXISTS `bs_exp_checkpoints`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE `bs_exp_checkpoints`;


-- ─── 3. LangGraph 检查点主表 ────────────────────────────
-- 用途：存储每个会话的检查点快照（序列化的图状态 + 元数据）
-- 注意：此表也可由 MySQLSaver.setup() 自动创建

CREATE TABLE IF NOT EXISTS `langgraph_checkpoints` (
    `thread_id`           VARCHAR(128)   NOT NULL,
    `checkpoint_ns`       VARCHAR(255)   NOT NULL DEFAULT '',
    `checkpoint_id`       VARCHAR(64)    NOT NULL,
    `parent_checkpoint_id` VARCHAR(64)   DEFAULT NULL,
    `checkpoint`          LONGBLOB       DEFAULT NULL,
    `serde_type`          VARCHAR(32)    DEFAULT NULL COMMENT '序列化类型（如 msgpack/json）',
    `metadata`            JSON           DEFAULT NULL,
    `created_at`          TIMESTAMP(6)   DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (`thread_id`, `checkpoint_ns`, `checkpoint_id`),
    INDEX `idx_thread_created` (`thread_id`, `checkpoint_ns`, `created_at` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ─── 4. LangGraph 检查点中间写入表 ──────────────────────
-- 用途：存储节点执行过程中的中间写入（错误、中断等特殊通道）

CREATE TABLE IF NOT EXISTS `langgraph_checkpoint_writes` (
    `thread_id`      VARCHAR(128)  NOT NULL,
    `checkpoint_ns`  VARCHAR(255)  NOT NULL DEFAULT '',
    `checkpoint_id`  VARCHAR(64)   NOT NULL,
    `task_id`        VARCHAR(64)   NOT NULL,
    `idx`            INT           NOT NULL,
    `channel`        VARCHAR(128)  NOT NULL,
    `write_type`     VARCHAR(32)   DEFAULT NULL COMMENT '写入类型（error/interrupt/resume）',
    `value`          LONGBLOB      DEFAULT NULL,
    `serde_type`     VARCHAR(32)   DEFAULT NULL COMMENT '序列化类型（如 msgpack/json）',
    PRIMARY KEY (`thread_id`, `checkpoint_ns`, `checkpoint_id`, `task_id`, `idx`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ============================================================
-- 检查点库性能优化（高频写入场景，按需执行）
-- ============================================================
-- ALTER DATABASE `bs_exp_checkpoints`
--     DEFAULT CHARACTER SET utf8mb4
--     DEFAULT COLLATE utf8mb4_unicode_ci;
--
-- -- 允许事务日志每秒刷盘一次，写入吞吐提升 5-10 倍
-- SET GLOBAL innodb_flush_log_at_trx_commit = 2;


-- ============================================================
-- 验证（取消注释以查看建表结果）
-- ============================================================
-- SHOW DATABASES;
-- USE bs_exp_vue;         SHOW TABLES;
-- USE bs_exp_checkpoints; SHOW TABLES;
