-- 作品 Feed 排序字段：班级、年级、审核通过时间
-- 执行前请备份。请先 USE 到应用库（默认 bs_exp_vue），可重复执行。
-- 若客户端报 Error 1175（safe update mode），本脚本已临时关闭该限制。

SET NAMES utf8mb4;

-- Navicat / Workbench 常见：WHERE 未用到主键会拦截 UPDATE
SET @safe := @@SQL_SAFE_UPDATES;
SET SQL_SAFE_UPDATES = 0;

SET @db = DATABASE();

SET @sql = IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'mb_work' AND COLUMN_NAME = 'class_org_id') = 0,
  'ALTER TABLE `mb_work` ADD COLUMN `class_org_id` varchar(32) DEFAULT NULL COMMENT ''学生班级 org_id'' AFTER `school_name`',
  'SELECT ''skip mb_work.class_org_id'' AS msg'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'mb_work' AND COLUMN_NAME = 'school_grade_id') = 0,
  'ALTER TABLE `mb_work` ADD COLUMN `school_grade_id` varchar(32) DEFAULT NULL COMMENT ''学生年级 g1~g6'' AFTER `class_org_id`',
  'SELECT ''skip mb_work.school_grade_id'' AS msg'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'mb_work' AND COLUMN_NAME = 'review_time') = 0,
  'ALTER TABLE `mb_work` ADD COLUMN `review_time` datetime DEFAULT NULL COMMENT ''教师审核通过时间'' AFTER `school_grade_id`',
  'SELECT ''skip mb_work.review_time'' AS msg'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  (SELECT COUNT(*) FROM information_schema.STATISTICS
   WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'mb_work' AND INDEX_NAME = 'idx_mb_work_feed') = 0,
  'ALTER TABLE `mb_work` ADD KEY `idx_mb_work_feed` (`review_status`, `review_time`, `create_time`)',
  'SELECT ''skip idx_mb_work_feed'' AS msg'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 回填班级 org_id（依赖同库 sys_user 表）
UPDATE `mb_work` w
INNER JOIN `sys_user` u ON w.`student_user_id` = u.`user_id`
SET w.`class_org_id` = u.`user_org_id`
WHERE w.`work_id` IS NOT NULL
  AND w.`class_org_id` IS NULL
  AND u.`user_org_id` IS NOT NULL
  AND u.`user_org_id` <> '';

-- 回填审核时间（来自提交记录）
UPDATE `mb_work` w
INNER JOIN `mb_task_submission` s ON w.`submission_id` = s.`submission_id`
SET w.`review_time` = s.`review_time`
WHERE w.`work_id` IS NOT NULL
  AND w.`review_time` IS NULL
  AND s.`review_time` IS NOT NULL
  AND w.`review_status` IN ('reviewed', 'approved');

SET SQL_SAFE_UPDATES = @safe;

-- ---------- 执行后校验（应看到列存在、回填行数） ----------
SELECT COLUMN_NAME, COLUMN_TYPE, COLUMN_COMMENT
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'mb_work'
  AND COLUMN_NAME IN ('class_org_id', 'school_grade_id', 'review_time');

SELECT
  COUNT(*) AS total_works,
  SUM(CASE WHEN class_org_id IS NOT NULL AND class_org_id <> '' THEN 1 ELSE 0 END) AS with_class_org,
  SUM(CASE WHEN school_grade_id IS NOT NULL AND school_grade_id <> '' THEN 1 ELSE 0 END) AS with_grade,
  SUM(CASE WHEN review_time IS NOT NULL THEN 1 ELSE 0 END) AS with_review_time
FROM mb_work;

-- school_grade_id 需根据组织树解析，历史数据可在重启后端后由新上传/批阅自动写入；
-- 或在管理端触发一次「重新保存」作品。Feed 查询时对空值会 lazy 解析学生年级。
