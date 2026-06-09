UPDATE `mb_work` w
INNER JOIN `mb_task` t ON w.`task_id` = t.`task_id`
SET w.`source_exp_id` = t.`video_id`
WHERE w.`source_exp_id` IS NULL AND t.`video_id` IS NOT NULL;

UPDATE `mb_work` w
INNER JOIN `exp_msg` e ON e.`exp_name` COLLATE utf8mb4_unicode_ci = w.`title` COLLATE utf8mb4_unicode_ci
  AND e.`status` = 'y'
SET w.`source_exp_id` = e.`exp_id`
WHERE w.`source_exp_id` IS NULL;
