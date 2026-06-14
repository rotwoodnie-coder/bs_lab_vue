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

 Date: 10/06/2026 22:43:17
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for exp_homework
-- ----------------------------
DROP TABLE IF EXISTS `exp_homework`;
CREATE TABLE `exp_homework`  (
  `homework_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
  `teacher_exp_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '关联实验id',
  `tearcher_user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '教师user_id',
  `class_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '班级org_id',
  `require_date` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '要求完成日期',
  `status` varchar(20)  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '状态(c-草稿，y-已布置，n-已作废)' AFTER `require_date`;
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`homework_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '实验作业布置记录' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
