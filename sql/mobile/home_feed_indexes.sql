-- 首页 feed 性能索引（P1）
-- 在 bs_exp_vue 执行；若索引已存在会报错，可忽略重复项
SET NAMES utf8mb4;

-- 瀑布流：exp_msg 按 status + create_time 排序
ALTER TABLE `exp_msg`
  ADD INDEX `idx_exp_msg_status_create_time` (`status`, `create_time`);

-- 独立模拟实验列表
ALTER TABLE `exp_simulator`
  ADD INDEX `idx_exp_simulator_status_create_time` (`status`, `create_time`);

-- NOT EXISTS 子查询：exp_msg.simulator_id
ALTER TABLE `exp_msg`
  ADD INDEX `idx_exp_msg_simulator_status` (`simulator_id`, `status`);

-- 年级筛选 JOIN exp_grade
ALTER TABLE `exp_grade`
  ADD INDEX `idx_exp_grade_grade_exp` (`grade_id`, `exp_id`);

-- 封面批量查询
ALTER TABLE `exp_video`
  ADD INDEX `idx_exp_video_exp_sort` (`exp_id`, `sort_order`);

ALTER TABLE `exp_material`
  ADD INDEX `idx_exp_material_exp` (`exp_id`);
