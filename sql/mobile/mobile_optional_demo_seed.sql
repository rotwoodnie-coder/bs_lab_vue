-- ============================================================
-- 移动端 · Tier 2 演示种子（可选）
-- 前置：mobile_optional_demo_tables.sql
-- 说明：勋章/成长/评论 demo；与 prototypeFallbacks.js 对齐
-- ============================================================

SET NAMES utf8mb4;

DELETE FROM `mb_growth_event`;
DELETE FROM `mb_badge_progress`;
DELETE FROM `mb_badge_def`;
DELETE FROM `mb_comment`;

INSERT INTO `mb_badge_def` (`badge_id`,`icon`,`title`,`description`,`criteria_type`,`criteria_value`,`action_route`,`sort_order`,`status`) VALUES
('exp-master','🏆','实验达人','完成 5 个实验','exp_count',5,NULL,1,'y'),
('science-star','🔬','科学新星','首次提交作品','work_first',1,NULL,2,'y'),
('creative','🌟','创意之星','作品被展示','work_featured',1,NULL,3,'y'),
('record','📸','记录达人','上传 5 次成果','upload_count',5,NULL,4,'y'),
('quiz-starter','🧠','答题新手','完成首次每日答题','quiz_first',1,NULL,5,'y'),
('explorer','💡','探索者','完成 10 个实验','exp_count',10,NULL,6,'y'),
('quiz-king','🏅','答题王者','累计答对 20 题','quiz_correct',20,'/quiz',7,'y'),
('streak','🔥','连对达人','连续 7 天全对','quiz_streak',7,NULL,8,'y');

INSERT INTO `mb_badge_progress` (`user_id`,`badge_id`,`earned`,`progress_current`,`progress_target`,`earned_time`) VALUES
('demo-student-001','exp-master','y',5,5,'2026-05-28 09:30:00'),
('demo-student-001','science-star','y',1,1,'2026-05-20 10:00:00'),
('demo-student-001','creative','y',1,1,'2026-05-25 16:20:00'),
('demo-student-001','record','y',5,5,'2026-05-22 10:15:00'),
('demo-student-001','quiz-starter','y',1,1,'2026-05-18 08:00:00'),
('demo-student-001','explorer','n',6,10,NULL),
('demo-student-001','quiz-king','n',8,20,NULL),
('demo-student-001','streak','n',3,7,NULL);

INSERT INTO `mb_growth_event` (`event_id`,`user_id`,`event_time_label`,`sort_time`,`emoji`,`title`,`hint`,`badges_json`,`points_label`,`dot_class`,`badge_class`,`source_type`,`create_time`) VALUES
('ge-1','demo-student-001','今天 09:30','2026-06-05 09:30:00','🌈','完成"彩虹分层实验"','获得 🏆 实验达人勋章',NULL,'+50分','','badge-success','work',NOW()),
('ge-2','demo-student-001','昨天 16:20','2026-06-04 16:20:00','🪁','提交"纸风车"作品',NULL,'["作品已展示","+20分"]',NULL,'violet',NULL,'work',NOW()),
('ge-3','demo-student-001','前天 10:15','2026-06-03 10:15:00','💧','完成"水的表面张力"实验','首次独立操作成功 🔬',NULL,'+30分','blue','badge-success','manual',NOW()),
('ge-4','demo-student-001','上周','2026-05-29 12:00:00','📊','完成 3 个实验','班级排名第 5，超越 85% 同学',NULL,'排名 ↑3','amber','badge-warning','manual',NOW()),
('ge-5','demo-student-001','5天前','2026-05-31 12:00:00','📸','上传 3 份实验成果照片',NULL,'["获得记录达人"]','+15分','amber','badge-success','work',NOW()),
('ge-6','demo-student-001','4月28日','2026-04-28 10:00:00','🌸','完成"植物生长观察"系列实验','连续观察 7 天',NULL,'+40分','slate','badge-success','manual',NOW());

INSERT INTO `mb_comment` (`comment_id`,`target_type`,`target_id`,`user_id`,`user_name`,`user_role_tag`,`content`,`like_count`,`status`,`create_time`) VALUES
('cmt-1','work','rainbow','u-xiaoke','小科学迷',NULL,'好漂亮的彩虹！我也想试试，请问用的什么容器？',3,'y','2026-06-03 10:00:00'),
('cmt-2','work','rainbow','u-liming','李明轩',NULL,'我们组也做了这个实验，不过颜色没有这么明显，可能是蜂蜜放少了',5,'y','2026-06-03 11:00:00'),
('cmt-3','work','rainbow','demo-student-001','张小明','author','我用的是细试管，每层大约 2cm，加的时候要沿着管壁慢慢倒～',8,'y','2026-06-04 09:00:00'),
('cmt-4','work','rainbow','u-wang','王老师','teacher','解释得很清楚！密度分层是理解物质性质的重要实验',12,'y','2026-06-04 14:00:00');
