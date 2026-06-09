-- ============================================================
-- 移动端 · Tier 1 演示种子（可选）
-- 前置：mobile_required_tables.sql
-- 说明：任务/作品/答题/绑定 demo 数据；user_id 为占位符
-- ============================================================

SET NAMES utf8mb4;

DELETE FROM `mb_quiz_record`;
DELETE FROM `mb_work_file`;
DELETE FROM `mb_work`;
DELETE FROM `mb_task_submission`;
DELETE FROM `mb_task`;
DELETE FROM `mb_parent_child`;

INSERT INTO `mb_task` (`task_id`,`title`,`description`,`task_type`,`teacher_user_id`,`deadline`,`video_id`,`video_title`,`video_duration`,`video_meta`,`teacher_hint`,`teacher_hint_class`,`task_type_label`,`requirements_json`,`steps_json`,`upload_query`,`status`,`create_time`) VALUES
('task-rainbow','彩虹液体分层实验','观看实验视频，完成步骤操作并上传成果照片或短视频。','homework','demo-teacher-001','2026-06-05 20:00:00','rainbow','安全实验：彩虹液体分层','2 分钟',NULL,'指导老师：李老师 · 老师布置的作业','tint-orange',NULL,
 '["至少上传 1 张步骤照片","可附 1 段 30 秒以内视频","需写明实验名称和观察结果"]',
 '["观看关联视频","准备材料并完成实验","整理成果后提交"]','','y',NOW()),
('task-windmill','自制简易风向标','观察风向变化并记录实验过程。','homework','demo-teacher-001','2026-06-06 18:00:00','windmill','自制简易风向标','7 分钟',NULL,'指导老师：王老师 · 老师布置的作业','tint-orange',NULL,
 '["至少上传 1 张步骤照片","记录风向观察结果","注明实验日期与地点"]',
 '["观看关联视频","准备材料并完成实验","整理成果后提交"]','','y',NOW()),
('task-volcano','拍同款 · 自制简易风向标','参考下方实验视频，拍摄并上传你的同款实验作品。','remix',NULL,NULL,'windmill','自制简易风向标','7 分钟','王老师 · 科学探究 · 1.2 万次播放','来自视频「拍同款」· 非老师布置作业','tint-amber','自愿完成',
 '["拍摄与参考视频同主题的实验过程","至少 1 张照片或 1 段短视频","上传后归入作品墙「拍同款」"]',
 '["回看参考视频，了解步骤","动手完成同款实验并拍摄","上传作品完成拍同款"]','type=remix','y',NOW());

INSERT INTO `mb_task_submission` (`submission_id`,`task_id`,`student_user_id`,`state`,`state_label`,`badge_class`,`create_time`) VALUES
('sub-rainbow','task-rainbow','demo-student-001','pending','待完成','badge-warning',NOW()),
('sub-windmill','task-windmill','demo-student-001','doing','进行中','badge-info',NOW()),
('sub-volcano','task-volcano','demo-student-001','done','已完成','badge-success',NOW());

INSERT INTO `mb_work` (`work_id`,`student_user_id`,`title`,`description`,`work_type`,`grade`,`review_status`,`teacher_review_name`,`teacher_review_text`,`teacher_review_stars`,`like_count`,`comment_count`,`class_name`,`school_name`,`tint`,`status`,`create_time`) VALUES
('rainbow','demo-student-001','彩虹液体分层','利用不同密度的液体依次加入试管，形成了漂亮的彩虹分层效果。','homework','已批阅 A+','reviewed','李老师','实验操作规范，照片清晰，分层效果非常明显！如果能标注每种液体的名称就更完整了。继续加油！',5,24,5,'三年级(1)班','宝山实验小学','tint-violet','y','2026-05-28 14:30:00'),
('candle','demo-student-002','蜡烛燃烧与空气',NULL,'remix',NULL,'pending',NULL,NULL,NULL,12,2,'三(2)班','友谊路小学','tint-blue','y',NOW()),
('seed','demo-student-001','种子发芽观察日记',NULL,'homework',NULL,'pending',NULL,NULL,NULL,8,1,'三(1)班','宝山实验小学','tint-green','y',NOW()),
('volcano','demo-student-001','火山喷发模拟',NULL,'remix',NULL,'pending',NULL,NULL,NULL,15,3,'三(1)班','宝山实验小学','tint-amber','y',NOW()),
('windmill','demo-student-001','纸风车创意实验',NULL,'creative',NULL,'pending',NULL,NULL,NULL,6,0,'三(1)班','宝山实验小学','tint-cyan','y',NOW()),
('magnet','demo-student-003','磁铁吸引实验',NULL,'homework',NULL,'pending',NULL,NULL,NULL,4,0,'三(3)班','实验小学','tint-rose','y',NOW());

INSERT INTO `mb_quiz_record` (`record_id`,`user_id`,`quiz_date`,`score`,`total`,`points`,`perfect`,`create_time`) VALUES
('qr-20260530','demo-student-001','2026-05-30',1,1,15,'y','2026-05-30 18:00:00'),
('qr-20260529','demo-student-001','2026-05-29',0,1,0,'n','2026-05-29 18:00:00'),
('qr-20260528','demo-student-001','2026-05-28',1,1,15,'y','2026-05-28 18:00:00');

INSERT INTO `mb_parent_child` (`bind_id`,`parent_user_id`,`child_user_id`,`relation`,`is_default`,`school_name`,`grade_name`,`class_name`,`bind_status`,`create_time`) VALUES
('bind-001','demo-parent-001','demo-student-001','妈妈','y','宝山实验小学','三年级','(1)班','approved',NOW()),
('bind-002','demo-parent-001','demo-student-002','妈妈','n','宝山实验小学','二年级','(3)班','approved',NOW());

-- 每日答题：题目由系统按 user_id + quiz_date 从 exp_question 自动分配，无需 mb_quiz_daily 排期
-- 需 exp_question 有 status='y' 且带选项的题目
