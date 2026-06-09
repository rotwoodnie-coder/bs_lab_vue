-- ============================================================
-- 移动端 · 清理 demo / 种子业务数据
-- 用途：移除 demo-* user_id 及 optional_demo_seed 写入的行
-- 不删：表结构、Layer A（sys_user / exp_* 等）
-- 执行后：按 docs/mobile-test-data-playbook.md 用管理端 + 真实账号造数
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE `mb_work_file`;
TRUNCATE TABLE `mb_task_submission`;
TRUNCATE TABLE `mb_work`;
TRUNCATE TABLE `mb_quiz_record`;
TRUNCATE TABLE `mb_comment`;
TRUNCATE TABLE `mb_user_reaction`;
TRUNCATE TABLE `mb_badge_progress`;
TRUNCATE TABLE `mb_growth_event`;
TRUNCATE TABLE `mb_parent_child`;
TRUNCATE TABLE `mb_task`;
TRUNCATE TABLE `mb_quiz_daily`;

-- 旧 seed 中的勋章定义；生产由管理端「勋章规则」配置
TRUNCATE TABLE `mb_badge_def`;

SET FOREIGN_KEY_CHECKS = 1;

-- 验证（可选，Navicat 中执行）：
-- SELECT 'mb_task' t, COUNT(*) c FROM mb_task
-- UNION ALL SELECT 'mb_work', COUNT(*) FROM mb_work
-- UNION ALL SELECT 'mb_parent_child', COUNT(*) FROM mb_parent_child;
