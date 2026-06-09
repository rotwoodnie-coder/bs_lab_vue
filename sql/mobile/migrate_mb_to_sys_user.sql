-- ============================================================
-- 一次性：将 mb_* 演示数据改绑到内建 sys_user（不创建新用户）
-- 执行前请备份相关表
-- 学生 zhangxm / 教师 gaoy — 按实际 login_name 核对 user_id
-- ============================================================

SET NAMES utf8mb4;

SET @student_id := 'a6ef4dd7298d4914ae58d2dccc136ae8';  -- login: zhangxm, role: Student
SET @teacher_id := '9a77dde99aaa4b689bd69ddfdedb6cb0';   -- login: gaoy, role: Teacher

UPDATE `mb_task`
SET `teacher_user_id` = @teacher_id
WHERE `teacher_user_id` IS NULL OR `teacher_user_id` LIKE 'demo-%';

UPDATE `mb_task_submission`
SET `student_user_id` = @student_id
WHERE `student_user_id` LIKE 'demo-%';

UPDATE `mb_work`
SET `student_user_id` = @student_id
WHERE `student_user_id` LIKE 'demo-%';

UPDATE `mb_quiz_record`
SET `user_id` = @student_id
WHERE `user_id` LIKE 'demo-%';

UPDATE `mb_badge_progress`
SET `user_id` = @student_id
WHERE `user_id` LIKE 'demo-%';

UPDATE `mb_growth_event`
SET `user_id` = @student_id
WHERE `user_id` LIKE 'demo-%';

-- 家长绑定：库内无 Parent 用户时，删除 demo 绑定行
DELETE FROM `mb_parent_child` WHERE `parent_user_id` LIKE 'demo-%' OR `child_user_id` LIKE 'demo-%';

-- 验证
SELECT 'mb_task_submission' AS tbl, student_user_id, COUNT(*) AS cnt FROM mb_task_submission GROUP BY student_user_id;
SELECT 'mb_work' AS tbl, student_user_id, COUNT(*) AS cnt FROM mb_work GROUP BY student_user_id;
SELECT 'mb_quiz_record' AS tbl, user_id, COUNT(*) AS cnt FROM mb_quiz_record GROUP BY user_id;
