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

 Date: 14/06/2026 15:51:24
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for teacher_class
-- ----------------------------
DROP TABLE IF EXISTS `teacher_class`;
CREATE TABLE `teacher_class`  (
  `seq_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `teacher_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '教师id',
  `class_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '班级id',
  PRIMARY KEY (`seq_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '教师授课班级关系表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
