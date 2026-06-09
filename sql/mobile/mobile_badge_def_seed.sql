-- ============================================================
-- 移动端 · 勋章定义种子（**仅 dev 可选**）
-- 生产环境由管理端配置 mb_badge_def，见 docs/mobile-product-spec.md §七
-- 前置：mobile_feature_tables.sql
-- ============================================================

SET NAMES utf8mb4;

DELETE FROM `mb_badge_def`;

INSERT INTO `mb_badge_def` (`badge_id`,`icon`,`title`,`description`,`criteria_type`,`criteria_value`,`action_route`,`sort_order`,`status`) VALUES
('exp-master','🏆','实验达人','完成 5 个实验任务','exp_task_done',5,NULL,1,'y'),
('science-star','🔬','科学新星','首次提交作品','work_first',1,NULL,2,'y'),
('creative','🌟','创意之星','作品被教师展示','work_featured',1,NULL,3,'y'),
('record','📸','记录达人','上传 5 次成果','work_submit_count',5,NULL,4,'y'),
('quiz-starter','🧠','答题新手','完成首次每日答题','quiz_first',1,'/quiz',5,'y'),
('explorer','💡','探索者','完成 10 个实验任务','exp_task_done',10,NULL,6,'y'),
('quiz-king','🏅','答题王者','累计答对 20 题','quiz_correct',20,'/quiz',7,'y'),
('streak','🔥','连对达人','连续 7 天全对','quiz_streak',7,'/quiz',8,'y');
