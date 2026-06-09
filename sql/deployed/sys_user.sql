/*
 Navicat Premium Data Transfer

 Source Server         : xuanyue-exp
 Source Server Type    : MySQL
 Source Server Version : 90001
 Source Host           : 10.0.181.204:13306
 Source Schema         : bs_exp_data

 Target Server Type   : MySQL
 Target Server Version : 90001
 File Encoding         : 65001

 Date: 11/05/2026 10:56:47
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '主键id',
  `user_name` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '姓名',
  `user_org_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '所属组织id',
  `root_org_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '根组织id',
  `user_role_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '主角色id',
  `user_logo` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像URL',
  `user_nick_name` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '昵称',
  `login_name` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '登录名',
  `login_pwd` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '登录密码（哈希存储）',
  `user_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
  `user_email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
  `expire_date` datetime NULL DEFAULT NULL COMMENT '账号有效期',
  `comments` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '状态：y 启用，n 停用',
  `last_login_time` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
  `pref_title_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '个人职称id',
  `per_resume` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '个人简介',
  `per_score` int NULL DEFAULT 0 COMMENT '个人积分',
  `create_user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人id',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '最后更新人id',
  `update_time` datetime NULL DEFAULT NULL COMMENT '最后更新时间',
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE INDEX `uk_sys_user_login`(`login_name` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES ('u_admin', '超级管理员', NULL, NULL, NULL, NULL, 'admin', '$2a$10$7uQn0m8D6rYbqWv9JY8tQeQ4zCk5wL1dD7g3w3pGQ6zYj8m2QYQ0a', NULL, NULL, NULL, NULL, 'y', NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL);
INSERT INTO `sys_user` VALUES ('u_test_01', '测试用户1', NULL, NULL, NULL, NULL, 'test1', '$2a$10$7uQn0m8D6rYbqWv9JY8tQeQ4zCk5wL1dD7g3w3pGQ6zYj8m2QYQ0a', NULL, NULL, NULL, NULL, 'y', NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL);
INSERT INTO `sys_user` VALUES ('u_test_02', '测试用户2', NULL, NULL, NULL, NULL, 'test2', '$2a$10$7uQn0m8D6rYbqWv9JY8tQeQ4zCk5wL1dD7g3w3pGQ6zYj8m2QYQ0a', NULL, NULL, NULL, NULL, 'y', NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL);

SET FOREIGN_KEY_CHECKS = 1;
