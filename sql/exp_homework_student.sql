/*
 Navicat Premium Data Transfer

 Source Server         : xuanyue-exp
 Source Server Type    : MySQL
 Source Server Version : 90001
 Source Host           : 10.0.181.204:13306
 Source Schema         : bs_exp

 Target Server Type    : MySQL
 Target Server Version : 90001
 File Encoding         : 65001

 Date: 10/06/2026 22:43:26
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for exp_homework_student
-- ----------------------------
DROP TABLE IF EXISTS `exp_homework_student`;
CREATE TABLE `exp_homework_student`  (
  `seq_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键id',
  `homework_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '实验作业布置作业id',
  `teacher_exp_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '教师实验id',
  `teacher_user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '教师user_id',
  `require_date` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '要求完成日期',
  `student_exp_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '学生实验id',
  `student_submit_date` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '学生提交日期',
  `mark_user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '批改人user_id',
  `mark_time` datetime NULL DEFAULT NULL COMMENT '批改时间',
  `mark_comments` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '批改意见',
  `mark_result` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '批改评分',
  PRIMARY KEY (`seq_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '学生实验作业记录' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
