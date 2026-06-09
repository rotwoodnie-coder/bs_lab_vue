/*
 Navicat Premium Data Transfer

 Source Server         : xuanyue-exp
 Source Server Type    : MySQL
 Source Server Version : 90001
 Source Host           : 10.0.181.204:13306
 Source Schema         : bs_exp_data

 Target Server Type    : MySQL
 Target Server Version : 90001
 File Encoding         : 65001

 Date: 15/05/2026 10:20:29
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for data_file_type
-- ----------------------------
DROP TABLE IF EXISTS `data_file_type`;
CREATE TABLE `data_file_type`  (
  `type_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '主键id',
  `type_name` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '类型名称',
  `comments` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '说明',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '状态：y 启用，n 停用',
  `sort_order` int NULL DEFAULT NULL COMMENT '排序',
  `logo_class` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图标样式标识',
  PRIMARY KEY (`type_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '素材类别' ROW_FORMAT = DYNAMIC;

SET FOREIGN_KEY_CHECKS = 1;
