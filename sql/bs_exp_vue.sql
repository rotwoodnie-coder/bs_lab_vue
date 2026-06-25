/*
 Navicat Premium Dump SQL

 Source Server         : 10.0.181.204
 Source Server Type    : MySQL
 Source Server Version : 90001 (9.0.1)
 Source Host           : 10.0.181.204:13306
 Source Schema         : bs_exp_vue

 Target Server Type    : MySQL
 Target Server Version : 90001 (9.0.1)
 File Encoding         : 65001

 Date: 25/06/2026 14:52:58
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for ai_chat_message
-- ----------------------------
DROP TABLE IF EXISTS `ai_chat_message`;
CREATE TABLE `ai_chat_message`  (
  `message_id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `session_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '所属会话 id',
  `role` enum('user','assistant','system') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消息角色',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消息内容',
  `metadata` json NULL COMMENT '附加元数据（如 token 用量、stage 快照等）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`message_id`) USING BTREE,
  INDEX `idx_session_id`(`session_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'AI 聊天消息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ai_chat_session
-- ----------------------------
DROP TABLE IF EXISTS `ai_chat_session`;
CREATE TABLE `ai_chat_session`  (
  `session_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会话主键',
  `user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发起用户 id',
  `user_role` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'student' COMMENT '用户角色（推荐: student/teacher/parent/researcher/admin）',
  `agent_type` enum('student','teacher','researcher','parent','admin') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'student' COMMENT '智能体类型',
  `grade_level` enum('低段','中段','高段') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '年级属性：低段(1-2)/中段(3-4)/高段(5-6)',
  `current_stage` enum('INIT','GOAL','MATERIAL','STEP','RECORD','CONCLUSION','FINAL') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'INIT' COMMENT '当前环节状态',
  `experiment_title` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '实验标题（从对话中提取）',
  `experiment_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '关联实验主键',
  `summary` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '会话摘要（最终产出的完整方案）',
  `is_active` enum('y','n') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'y' COMMENT '会话是否有效',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`session_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'AI 聊天会话' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for data_coursebook
-- ----------------------------
DROP TABLE IF EXISTS `data_coursebook`;
CREATE TABLE `data_coursebook`  (
  `coursebook_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '主键id',
  `coursebook_name` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '教材名称',
  `edition_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '教材版本id',
  `subject_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '学科id',
  `level_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '学段id',
  `grade_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '年级id',
  `semester_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '学期id',
  `file_url` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '电子文件URL',
  `comments` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '说明',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '状态：y 启用，n 停用',
  PRIMARY KEY (`coursebook_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '教材' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for data_coursebook_content
-- ----------------------------
DROP TABLE IF EXISTS `data_coursebook_content`;
CREATE TABLE `data_coursebook_content`  (
  `content_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '主键id',
  `content_name` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '目录名称',
  `content_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '目录类型(单元unit,章chapter,节section)',
  `parent_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '上级目录id',
  `coursebook_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '教材id',
  `comments` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '说明',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '状态：y 启用，n 停用',
  `sort_order` int NULL DEFAULT NULL COMMENT '排序',
  PRIMARY KEY (`content_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '教材目录' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for data_difficulty_type
-- ----------------------------
DROP TABLE IF EXISTS `data_difficulty_type`;
CREATE TABLE `data_difficulty_type`  (
  `difficulty_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '主键id',
  `difficulty_name` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '题库难度类型名称',
  `comments` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '说明',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '状态：y 启用，n 停用',
  `sort_order` int NULL DEFAULT NULL COMMENT '排序',
  PRIMARY KEY (`difficulty_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '难度' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for data_file
-- ----------------------------
DROP TABLE IF EXISTS `data_file`;
CREATE TABLE `data_file`  (
  `file_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '主键id',
  `file_name` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文件名称',
  `file_tag` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件标签',
  `file_url` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文件URL',
  `file_type_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件类型id',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '状态：y 启用，n 停用',
  `owner_user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '归属人id',
  `cover_image_url` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '封面图URL',
  `file_size` bigint NULL DEFAULT NULL COMMENT '文件大小（字节）',
  `file_ext` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件后缀',
  `is_public` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '0' COMMENT '是否公开（y=公开，n=不公开',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人',
  `comments` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`file_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '素材资源' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for data_file_log
-- ----------------------------
DROP TABLE IF EXISTS `data_file_log`;
CREATE TABLE `data_file_log`  (
  `log_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键id',
  `file_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '关联file_id',
  `log_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作类型',
  `log_type_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作类型名称',
  `log_user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作人user_id',
  `log_user_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作人姓名',
  `log_time` datetime NOT NULL COMMENT '操作时间',
  PRIMARY KEY (`log_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '文件素材操作日志表' ROW_FORMAT = DYNAMIC;

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

-- ----------------------------
-- Table structure for data_material_prop
-- ----------------------------
DROP TABLE IF EXISTS `data_material_prop`;
CREATE TABLE `data_material_prop`  (
  `prop_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '主键id',
  `prop_name` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '属性名称',
  `comments` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '说明',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '状态：y 启用，n 停用',
  `sort_order` int NULL DEFAULT NULL COMMENT '排序',
  PRIMARY KEY (`prop_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '材料属性' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for data_material_security
-- ----------------------------
DROP TABLE IF EXISTS `data_material_security`;
CREATE TABLE `data_material_security`  (
  `security_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '主键id',
  `security_name` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '安全性名称',
  `comments` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '说明',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '状态：y 启用，n 停用',
  `sort_order` int NULL DEFAULT NULL COMMENT '排序',
  `security_level` int NULL DEFAULT NULL COMMENT '危险等级（数值越低越危险，0最危险）',
  PRIMARY KEY (`security_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '安全性' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for data_material_type
-- ----------------------------
DROP TABLE IF EXISTS `data_material_type`;
CREATE TABLE `data_material_type`  (
  `type_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '主键id',
  `type_name` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分类名称',
  `comments` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '说明',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '状态：y 启用，n 停用',
  `sort_order` int NULL DEFAULT NULL COMMENT '排序',
  PRIMARY KEY (`type_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '材料分类' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for data_material_unit
-- ----------------------------
DROP TABLE IF EXISTS `data_material_unit`;
CREATE TABLE `data_material_unit`  (
  `unit_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '主键id',
  `unit_name` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '计量单位名称',
  `comments` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '说明',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '状态：y 启用，n 停用',
  `sort_order` int NULL DEFAULT NULL COMMENT '排序',
  PRIMARY KEY (`unit_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '计量单位' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for data_msg_type
-- ----------------------------
DROP TABLE IF EXISTS `data_msg_type`;
CREATE TABLE `data_msg_type`  (
  `msg_type_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '主键id',
  `msg_type_name` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '名称',
  `comments` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '说明',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '状态：y 启用，n 停用',
  `sort_order` int NULL DEFAULT NULL COMMENT '排序',
  PRIMARY KEY (`msg_type_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '消息分类' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for data_org_type
-- ----------------------------
DROP TABLE IF EXISTS `data_org_type`;
CREATE TABLE `data_org_type`  (
  `org_type_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '主键id',
  `org_type_name` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '组织类型名称',
  `comments` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '说明',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '状态：y 启用，n 停用',
  `sort_order` int NULL DEFAULT NULL COMMENT '排序',
  PRIMARY KEY (`org_type_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '组织类型（初始化，不能修改）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for data_pref_title
-- ----------------------------
DROP TABLE IF EXISTS `data_pref_title`;
CREATE TABLE `data_pref_title`  (
  `title_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '主键id',
  `title_name` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '职称名称',
  `comments` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '说明',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '状态：y 启用，n 停用',
  `sort_order` int NULL DEFAULT NULL COMMENT '排序',
  PRIMARY KEY (`title_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '职称' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for data_question_capacity
-- ----------------------------
DROP TABLE IF EXISTS `data_question_capacity`;
CREATE TABLE `data_question_capacity`  (
  `capacity_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '主键id',
  `capacity_name` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '能力侧重点名称',
  `comments` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '说明',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '状态：y 启用，n 停用',
  `sort_order` int NULL DEFAULT NULL COMMENT '排序',
  PRIMARY KEY (`capacity_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '题目能力侧重点' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for data_question_type
-- ----------------------------
DROP TABLE IF EXISTS `data_question_type`;
CREATE TABLE `data_question_type`  (
  `type_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '主键id',
  `type_name` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '题型名称',
  `comments` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '说明',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '状态：y 启用，n 停用',
  `sort_order` int NULL DEFAULT NULL COMMENT '排序',
  PRIMARY KEY (`type_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '题型' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for data_rating_scale
-- ----------------------------
DROP TABLE IF EXISTS `data_rating_scale`;
CREATE TABLE `data_rating_scale`  (
  `scale_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '主键id',
  `scale_name` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '评分名称',
  `comments` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '说明',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '状态：y 启用，n 停用',
  `sort_order` int NULL DEFAULT NULL COMMENT '排序',
  PRIMARY KEY (`scale_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '评分等级' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for data_role
-- ----------------------------
DROP TABLE IF EXISTS `data_role`;
CREATE TABLE `data_role`  (
  `role_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '主键id',
  `role_name` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '类型名称',
  `comments` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '说明',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '状态：y 启用，n 停用',
  `sort_order` int NULL DEFAULT NULL COMMENT '排序',
  PRIMARY KEY (`role_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户类型（初始化，不能修改）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for data_school_grade
-- ----------------------------
DROP TABLE IF EXISTS `data_school_grade`;
CREATE TABLE `data_school_grade`  (
  `grade_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '主键id',
  `grade_name` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '年级名称',
  `comments` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '说明',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '状态：y 启用，n 停用',
  `sort_order` int NULL DEFAULT NULL COMMENT '排序',
  `school_level_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '所属学段id',
  PRIMARY KEY (`grade_id`) USING BTREE,
  INDEX `idx_data_school_grade_level`(`school_level_id` ASC) USING BTREE,
  CONSTRAINT `fk_data_school_grade_level` FOREIGN KEY (`school_level_id`) REFERENCES `data_school_level` (`level_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '年级' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for data_school_level
-- ----------------------------
DROP TABLE IF EXISTS `data_school_level`;
CREATE TABLE `data_school_level`  (
  `level_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '主键id',
  `level_name` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '学段名称',
  `comments` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '说明',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '状态：y 启用，n 停用',
  `sort_order` int NULL DEFAULT NULL COMMENT '排序',
  PRIMARY KEY (`level_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '学段' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for data_school_semester
-- ----------------------------
DROP TABLE IF EXISTS `data_school_semester`;
CREATE TABLE `data_school_semester`  (
  `semester_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '主键id',
  `semester_name` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '学期名称',
  `comments` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '说明',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '状态：y 启用，n 停用',
  `sort_order` int NULL DEFAULT NULL COMMENT '排序',
  PRIMARY KEY (`semester_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '学期' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for data_school_subject
-- ----------------------------
DROP TABLE IF EXISTS `data_school_subject`;
CREATE TABLE `data_school_subject`  (
  `subject_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '主键id',
  `subject_name` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '学科名称',
  `comments` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '说明',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '状态：y 启用，n 停用',
  `sort_order` int NULL DEFAULT NULL COMMENT '排序',
  PRIMARY KEY (`subject_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '学科' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for data_textbook_edition
-- ----------------------------
DROP TABLE IF EXISTS `data_textbook_edition`;
CREATE TABLE `data_textbook_edition`  (
  `edition_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '主键id',
  `edition_name` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '版本名称',
  `comments` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '说明',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '状态：y 启用，n 停用',
  `sort_order` int NULL DEFAULT NULL COMMENT '排序',
  PRIMARY KEY (`edition_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '教材版本' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for exp_grade
-- ----------------------------
DROP TABLE IF EXISTS `exp_grade`;
CREATE TABLE `exp_grade`  (
  `seq_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '主键id',
  `exp_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '实验 ID（关联 exp_msg）',
  `grade_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '年级id（关联 data_school_grade',
  `sort_order` int NULL DEFAULT NULL COMMENT '排序',
  PRIMARY KEY (`seq_id`) USING BTREE,
  INDEX `idx_exp_grade_grade_exp`(`grade_id` ASC, `exp_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '试验适用年级' ROW_FORMAT = DYNAMIC;

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
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '状态(c-草稿，y-已布置，n-已作废)',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`homework_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '实验作业布置记录' ROW_FORMAT = DYNAMIC;

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
  `student_user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '学生user_id',
  `student_exp_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '学生实验id',
  `student_submit_date` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '学生提交日期',
  `mark_user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '批改人user_id',
  `mark_time` datetime NULL DEFAULT NULL COMMENT '批改时间',
  `mark_comments` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '批改意见',
  `mark_result` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '批改评分',
  PRIMARY KEY (`seq_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '学生实验作业记录' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for exp_log
-- ----------------------------
DROP TABLE IF EXISTS `exp_log`;
CREATE TABLE `exp_log`  (
  `log_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键id',
  `exp_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '关联exp_id',
  `log_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作类型',
  `log_type_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作类型名称',
  `log_comments` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '操作说明',
  `log_user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作人user_id',
  `log_user_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作人姓名',
  `log_time` datetime NOT NULL COMMENT '操作时间',
  PRIMARY KEY (`log_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '实验日志表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for exp_material
-- ----------------------------
DROP TABLE IF EXISTS `exp_material`;
CREATE TABLE `exp_material`  (
  `exp_material_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '主键id',
  `exp_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '试验id（关联exp_msg表）',
  `material_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '材料主键id(关联material_msg表)',
  `material_name` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '材料名称',
  `material_prop_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '材料属性id',
  `material_type_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '材料分类id',
  `material_num` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '建议用量（含单位，如500 毫升）',
  `main_pic_url` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '主图片URL',
  `exp_purpose` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '试验用途',
  `security_comments` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '安全说明',
  `additional_comments` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '补充说明',
  `sort_order` int NULL DEFAULT NULL COMMENT '排序',
  PRIMARY KEY (`exp_material_id`) USING BTREE,
  INDEX `idx_exp_material_exp`(`exp_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '试验相关材料' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for exp_material_pic
-- ----------------------------
DROP TABLE IF EXISTS `exp_material_pic`;
CREATE TABLE `exp_material_pic`  (
  `seq_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '主键id',
  `exp_material_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '试验材料id（关联exp_material表）',
  `material_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '试验材料id',
  `material_url` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图片URL',
  `sort_order` int NULL DEFAULT NULL COMMENT '排序',
  PRIMARY KEY (`seq_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '试验材料图片' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for exp_msg
-- ----------------------------
DROP TABLE IF EXISTS `exp_msg`;
CREATE TABLE `exp_msg`  (
  `exp_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '主键id',
  `exp_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '试验名称',
  `choose_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '必做/选做：must必做，choose 选做',
  `subject_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '学科id（关联 data_school_subject）',
  `school_level_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '学段id（关联 data_school_level）',
  `grade_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '年级id（关联 data_school_grade）',
  `difficulty_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '难度id（关联 data_exp_difficulty）',
  `exp_principle` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '试验原理（富文本）',
  `exp_caution` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '注意事项',
  `exp_danger` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '危险提示',
  `class_hour` decimal(6, 2) NULL DEFAULT NULL COMMENT '课时',
  `simulator_url` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模拟器URL',
  `simulator_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模拟器id（关联 exp_simulator）',
  `semester_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '学期id',
  `coursebook_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '对应教材id （关联 data_coursebook）',
  `unit_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '对应教材单元id （关联 data_coursebook_content)',
  `chapter_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '对应教材章id（关联 data_coursebook_content)',
  `section_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '对应教材节id（关联 data_coursebook_content',
  `link_exp_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '关联试验id（关联 exp_msg',
  `exp_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '试验类型(standard标准试验，teach教学试验，student学生试验）',
  `exp_task_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '作业类型：hw 作业，tk 拍同款，self 自主试验',
  `like_num` int NOT NULL DEFAULT 0 COMMENT '点赞数（聚合统计）',
  `notlike_num` int NOT NULL DEFAULT 0 COMMENT '倒赞数（聚合统计）',
  `collection_num` int NOT NULL DEFAULT 0 COMMENT '收藏数（聚合统计）',
  `evaluate_num` int NOT NULL DEFAULT 0 COMMENT '评价数（聚合统计）',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'c' COMMENT '审核状态：c草稿，t待审核，y 通过，n 不通过',
  `create_user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '发布人id',
  `create_time` datetime NULL DEFAULT NULL COMMENT '发布时间',
  `confirm_user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '审核人id',
  `confirm_time` datetime NULL DEFAULT NULL COMMENT '审核时间',
  `confirm_comments` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '审批意见',
  `update_user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '最后更新人id',
  `update_time` datetime NULL DEFAULT NULL COMMENT '最后更新时间',
  PRIMARY KEY (`exp_id`) USING BTREE,
  INDEX `idx_exp_msg_status_create_time`(`status` ASC, `create_time` ASC) USING BTREE,
  INDEX `idx_exp_msg_simulator_status`(`simulator_id` ASC, `status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '试验信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for exp_question
-- ----------------------------
DROP TABLE IF EXISTS `exp_question`;
CREATE TABLE `exp_question`  (
  `question_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '主键id',
  `question_content` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '题干内容（富文本）',
  `question_type_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '题型id',
  `difficulty_type_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '难度类型id',
  `question_capacity_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '能力侧重点id',
  `grade_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '年级id',
  `subject_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '学科id',
  `coursebook_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '教材id',
  `content_unit_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '教材单元id',
  `content_chapter_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '教材章id',
  `content_section_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '教材节id',
  `knowledge_content` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '知识点',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '状态：y 启用，n 停用，t 草稿',
  `create_user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人id',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '最后更新人id',
  `update_time` datetime NULL DEFAULT NULL COMMENT '最后更新时间',
  PRIMARY KEY (`question_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '试验题库' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for exp_question_select
-- ----------------------------
DROP TABLE IF EXISTS `exp_question_select`;
CREATE TABLE `exp_question_select`  (
  `select_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '主键id',
  `question_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '题目id',
  `select_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '选项内容',
  `sort_order` int NULL DEFAULT NULL COMMENT '排序',
  `is_right` varchar(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'n' COMMENT '是否正确答案：y 正确，n 错误',
  PRIMARY KEY (`select_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '题目选项' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for exp_reference
-- ----------------------------
DROP TABLE IF EXISTS `exp_reference`;
CREATE TABLE `exp_reference`  (
  `reference_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '主键id',
  `exp_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '试验id',
  `reference_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '引用名称',
  `reference_source` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '引用出处/URL',
  `reference_comments` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '引用说明',
  `sort_order` int NULL DEFAULT NULL COMMENT '排序',
  PRIMARY KEY (`reference_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '试验参考引用' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for exp_result
-- ----------------------------
DROP TABLE IF EXISTS `exp_result`;
CREATE TABLE `exp_result`  (
  `result_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '主键id',
  `exp_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '试验id（关联exp_msg表）',
  `result_name` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '结果名称',
  `result_comments` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '结果内容（富文本）',
  `sort_order` int NULL DEFAULT NULL COMMENT '排序',
  PRIMARY KEY (`result_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '试验结果' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for exp_scientist
-- ----------------------------
DROP TABLE IF EXISTS `exp_scientist`;
CREATE TABLE `exp_scientist`  (
  `scientist_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '主键id',
  `exp_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '试验id',
  `scientist_name` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '科学家名称',
  `story_name` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '故事名称',
  `story_comments` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '故事内容',
  `sort_order` int NULL DEFAULT NULL COMMENT '排序',
  PRIMARY KEY (`scientist_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '科学家故事' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for exp_simulator
-- ----------------------------
DROP TABLE IF EXISTS `exp_simulator`;
CREATE TABLE `exp_simulator`  (
  `simulator_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '主键id',
  `simulator_name` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '模拟器名称',
  `subject_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '学科id',
  `cover_image_url` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '封面图片URL',
  `simulator_url` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '模拟器URL',
  `comments` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '说明',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '状态：y 启用，n 停用',
  `create_user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '创建人id',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '最后更新人id',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`simulator_id`) USING BTREE,
  INDEX `idx_exp_simulator_status_create_time`(`status` ASC, `create_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '试验模拟器' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for exp_simulator_log
-- ----------------------------
DROP TABLE IF EXISTS `exp_simulator_log`;
CREATE TABLE `exp_simulator_log`  (
  `log_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '主键id',
  `simulator_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模拟器id',
  `user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '访问人id',
  `log_time` datetime NULL DEFAULT NULL COMMENT '记录时间',
  PRIMARY KEY (`log_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '试验模拟器访问日志' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for exp_step
-- ----------------------------
DROP TABLE IF EXISTS `exp_step`;
CREATE TABLE `exp_step`  (
  `step_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '主键id',
  `exp_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '试验id（关联exp_msg表）',
  `step_name` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '步骤名称',
  `step_comments` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '步骤内容（富文本）',
  `sort_order` int NULL DEFAULT NULL COMMENT '排序',
  PRIMARY KEY (`step_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '试验步骤' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for exp_video
-- ----------------------------
DROP TABLE IF EXISTS `exp_video`;
CREATE TABLE `exp_video`  (
  `seq_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '主键id',
  `video_url` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '视频URL',
  `exp_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '试验id',
  `sort_order` int NULL DEFAULT NULL COMMENT '排序',
  `file_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '素材id(关联data_file表)',
  PRIMARY KEY (`seq_id`) USING BTREE,
  INDEX `idx_exp_video_exp_sort`(`exp_id` ASC, `sort_order` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '试验视频' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for material_log
-- ----------------------------
DROP TABLE IF EXISTS `material_log`;
CREATE TABLE `material_log`  (
  `log_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键id',
  `material_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '关联material_id',
  `log_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作类型',
  `log_type_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作类型名称',
  `log_user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作人user_id',
  `log_user_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作人姓名',
  `log_time` datetime NOT NULL COMMENT '操作时间',
  PRIMARY KEY (`log_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '实验材料操作日志表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for material_msg
-- ----------------------------
DROP TABLE IF EXISTS `material_msg`;
CREATE TABLE `material_msg`  (
  `material_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '主键id',
  `material_name` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '材料名称',
  `material_prop_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '材料属性id',
  `material_type_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '材料分类id',
  `material_num` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '建议用量（含单位，如500 毫升）',
  `main_pic_url` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '主图片URL',
  `exp_purpose` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '试验用途',
  `security_comments` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '安全说明',
  `additional_comments` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '补充说明',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '状态：y 启用，n 停用',
  `is_public` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '0' COMMENT '是否公开（y=公开，n=不公开',
  `create_user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人id',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '最后更新人id',
  `update_time` datetime NULL DEFAULT NULL COMMENT '最后更新时间',
  PRIMARY KEY (`material_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '试验材料主库' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for material_pic
-- ----------------------------
DROP TABLE IF EXISTS `material_pic`;
CREATE TABLE `material_pic`  (
  `seq_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '主键id',
  `material_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '材料id',
  `material_url` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图片URL',
  `sort_order` int NULL DEFAULT NULL COMMENT '排序',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`seq_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '材料图片' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for mb_auth_refresh_token
-- ----------------------------
DROP TABLE IF EXISTS `mb_auth_refresh_token`;
CREATE TABLE `mb_auth_refresh_token`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'sys_user.user_id',
  `device_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '客户端设备标识',
  `token_hash` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'refresh token SHA-256',
  `expire_time` datetime NOT NULL,
  `revoked` tinyint(1) NOT NULL DEFAULT 0,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `last_used_at` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_mb_auth_refresh_hash`(`token_hash` ASC) USING BTREE,
  INDEX `idx_mb_auth_refresh_user_device`(`user_id` ASC, `device_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 611 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '移动端 Refresh Token' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for mb_badge_def
-- ----------------------------
DROP TABLE IF EXISTS `mb_badge_def`;
CREATE TABLE `mb_badge_def`  (
  `badge_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
  `icon` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'emoji 图标',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '勋章名',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '获得条件说明',
  `criteria_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '判定类型：exp_count/quiz_count/...',
  `criteria_value` int NULL DEFAULT NULL COMMENT '判定阈值',
  `reward_points` int NOT NULL DEFAULT 0 COMMENT '获得勋章奖励积分',
  `action_route` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '未获得时跳转路由',
  `sort_order` int NOT NULL DEFAULT 0,
  `status` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'y',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`badge_id`) USING BTREE,
  INDEX `idx_mb_badge_def_sort`(`sort_order` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '移动端勋章定义' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for mb_badge_progress
-- ----------------------------
DROP TABLE IF EXISTS `mb_badge_progress`;
CREATE TABLE `mb_badge_progress`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户 id',
  `badge_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '勋章 id',
  `earned` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'n' COMMENT '是否已获得 y/n',
  `progress_current` int NOT NULL DEFAULT 0 COMMENT '当前进度',
  `progress_target` int NOT NULL DEFAULT 0 COMMENT '目标进度',
  `earned_time` datetime NULL DEFAULT NULL COMMENT '获得时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_mb_badge_user`(`user_id` ASC, `badge_id` ASC) USING BTREE,
  INDEX `idx_mb_badge_progress_user`(`user_id` ASC, `earned` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 17 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '移动端用户勋章进度' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for mb_comment
-- ----------------------------
DROP TABLE IF EXISTS `mb_comment`;
CREATE TABLE `mb_comment`  (
  `comment_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
  `target_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '目标类型：work / exp_msg',
  `target_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '目标 id',
  `user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '评论用户 id',
  `user_name` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '评论用户显示名',
  `user_role_tag` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '角色标签：author / teacher',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '评论内容',
  `like_count` int NOT NULL DEFAULT 0 COMMENT '点赞数',
  `parent_comment_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '父评论 id（回复）',
  `status` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'y' COMMENT '状态 y/n',
  `deleted_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `deleted_time` datetime NULL DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`comment_id`) USING BTREE,
  INDEX `idx_mb_comment_target`(`target_type` ASC, `target_id` ASC, `create_time` DESC) USING BTREE,
  INDEX `idx_mb_comment_user`(`user_id` ASC, `create_time` DESC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '移动端评论' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for mb_dingtalk_bind
-- ----------------------------
DROP TABLE IF EXISTS `mb_dingtalk_bind`;
CREATE TABLE `mb_dingtalk_bind`  (
  `user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'sys_user.user_id',
  `ding_union_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '钉钉 unionId',
  `ding_open_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '钉钉 openId',
  `ding_nick` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '钉钉昵称',
  `bind_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE INDEX `uk_mb_dingtalk_union`(`ding_union_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '移动端钉钉绑定' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for mb_growth_event
-- ----------------------------
DROP TABLE IF EXISTS `mb_growth_event`;
CREATE TABLE `mb_growth_event`  (
  `event_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
  `user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '学生 user_id',
  `event_time_label` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '展示时间文案',
  `sort_time` datetime NOT NULL COMMENT '排序时间',
  `emoji` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `hint` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `badges_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '附加 badge 数组 JSON',
  `points_label` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '积分/排名标签',
  `dot_class` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'timeline dot',
  `badge_class` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '右侧 badge class',
  `source_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '来源：work/quiz/badge/manual',
  `source_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`event_id`) USING BTREE,
  UNIQUE INDEX `uk_ge_source`(`user_id` ASC, `source_type` ASC, `source_id` ASC) USING BTREE,
  INDEX `idx_mb_growth_user`(`user_id` ASC, `sort_time` DESC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '移动端成长轨迹' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for mb_growth_plan
-- ----------------------------
DROP TABLE IF EXISTS `mb_growth_plan`;
CREATE TABLE `mb_growth_plan`  (
  `user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'sys_user.user_id',
  `content_keys_json` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '[\"exp\",\"work\",\"badge\",\"quiz\"]' COMMENT '展示内容 keys JSON 数组',
  `visibility` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'parent' COMMENT 'self / parent / class',
  `range` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'term' COMMENT 'term / year / all',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '移动端成长展示方案' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for mb_parent_child
-- ----------------------------
DROP TABLE IF EXISTS `mb_parent_child`;
CREATE TABLE `mb_parent_child`  (
  `bind_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
  `parent_user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '家长 user_id',
  `child_user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '孩子 user_id',
  `relation` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '关系：妈妈/爸爸',
  `is_default` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'n' COMMENT '是否默认查看 y/n',
  `school_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `grade_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `class_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `class_org_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'class org id when applying',
  `bind_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'approved' COMMENT 'pending/approved/rejected',
  `confirm_user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `confirm_time` datetime NULL DEFAULT NULL,
  `reject_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`bind_id`) USING BTREE,
  UNIQUE INDEX `uk_mb_parent_child`(`parent_user_id` ASC, `child_user_id` ASC) USING BTREE,
  INDEX `idx_mb_parent`(`parent_user_id` ASC, `is_default` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '移动端家长孩子绑定' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for mb_points_ledger
-- ----------------------------
DROP TABLE IF EXISTS `mb_points_ledger`;
CREATE TABLE `mb_points_ledger`  (
  `ledger_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
  `user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户 id',
  `delta` int NOT NULL COMMENT '变动值',
  `balance_after` int NOT NULL COMMENT '变动后积分',
  `source_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'quiz/work/task/badge/featured/manual',
  `source_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '来源 id',
  `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`ledger_id`) USING BTREE,
  UNIQUE INDEX `uk_points_source`(`user_id` ASC, `source_type` ASC, `source_id` ASC) USING BTREE,
  INDEX `idx_points_user_time`(`user_id` ASC, `create_time` DESC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '积分流水' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for mb_quiz_config
-- ----------------------------
DROP TABLE IF EXISTS `mb_quiz_config`;
CREATE TABLE `mb_quiz_config`  (
  `config_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'default' COMMENT '固定 default',
  `questions_per_day` int NOT NULL DEFAULT 3 COMMENT '每日题数 1~10',
  `base_points` int NOT NULL DEFAULT 10 COMMENT '全对基础积分',
  `streak_bonus` int NOT NULL DEFAULT 5 COMMENT '连对额外积分',
  `enabled` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'y' COMMENT 'y 启用 n 关闭',
  `update_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`config_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '每日答题全局配置' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for mb_quiz_daily
-- ----------------------------
DROP TABLE IF EXISTS `mb_quiz_daily`;
CREATE TABLE `mb_quiz_daily`  (
  `daily_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
  `quiz_date` date NOT NULL COMMENT '答题日期',
  `question_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '题目 id → exp_question.question_id',
  `bonus_points` int NOT NULL DEFAULT 5 COMMENT '连对额外积分',
  `status` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'y' COMMENT 'y 启用 n 停用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`daily_id`) USING BTREE,
  UNIQUE INDEX `uk_mb_quiz_daily_date`(`quiz_date` ASC) USING BTREE,
  INDEX `idx_mb_quiz_daily_question`(`question_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '移动端每日答题排期' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for mb_quiz_record
-- ----------------------------
DROP TABLE IF EXISTS `mb_quiz_record`;
CREATE TABLE `mb_quiz_record`  (
  `record_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
  `user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户 id',
  `question_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '当日题目 → exp_question.question_id',
  `question_ids_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '当日题目 ID 列表 JSON',
  `quiz_date` date NOT NULL COMMENT '答题日期',
  `score` int NOT NULL DEFAULT 0 COMMENT '答对题数',
  `total` int NOT NULL DEFAULT 1 COMMENT '总题数（前期固定 1）',
  `points` int NOT NULL DEFAULT 0 COMMENT '获得积分',
  `perfect` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'n' COMMENT '是否全对 y/n',
  `answers_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '作答 JSON',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`record_id`) USING BTREE,
  UNIQUE INDEX `uk_mb_quiz_user_date`(`user_id` ASC, `quiz_date` ASC) USING BTREE,
  INDEX `idx_mb_quiz_user_time`(`user_id` ASC, `create_time` DESC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '移动端每日答题记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for mb_task
-- ----------------------------
DROP TABLE IF EXISTS `mb_task`;
CREATE TABLE `mb_task`  (
  `task_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '任务说明',
  `task_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'homework' COMMENT '类型：homework / remix',
  `teacher_user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '布置教师 user_id',
  `class_org_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '班级组织 id',
  `deadline` datetime NULL DEFAULT NULL COMMENT '截止时间',
  `video_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '关联视频/实验 id → exp_msg.exp_id',
  `video_title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '视频标题',
  `video_duration` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '视频时长文案',
  `video_meta` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '视频元信息',
  `teacher_hint` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '教师提示',
  `teacher_hint_class` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '提示样式 class',
  `task_type_label` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '任务类型展示文案',
  `requirements_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '提交要求 JSON 数组',
  `steps_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '完成步骤 JSON 数组',
  `upload_query` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '上传页 query 参数',
  `status` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'y' COMMENT '状态 y/n',
  `create_user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`task_id`) USING BTREE,
  INDEX `idx_mb_task_teacher`(`teacher_user_id` ASC, `create_time` DESC) USING BTREE,
  INDEX `idx_mb_task_class`(`class_org_id` ASC, `deadline` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '移动端任务/作业' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for mb_task_submission
-- ----------------------------
DROP TABLE IF EXISTS `mb_task_submission`;
CREATE TABLE `mb_task_submission`  (
  `submission_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
  `task_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务 id',
  `student_user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '学生 user_id',
  `state` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'pending' COMMENT 'pending/doing/done/submitted/reviewed',
  `state_label` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '状态展示文案',
  `badge_class` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '状态 badge class',
  `grade` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '评级 A+/A/B 等',
  `review_comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '教师评语',
  `reviewer_user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '批阅教师',
  `submit_time` datetime NULL DEFAULT NULL COMMENT '提交时间',
  `review_time` datetime NULL DEFAULT NULL COMMENT '批阅时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`submission_id`) USING BTREE,
  UNIQUE INDEX `uk_mb_task_student`(`task_id` ASC, `student_user_id` ASC) USING BTREE,
  INDEX `idx_mb_submission_student`(`student_user_id` ASC, `update_time` DESC) USING BTREE,
  INDEX `idx_mb_submission_state`(`state` ASC, `submit_time` DESC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '移动端任务提交' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for mb_user_reaction
-- ----------------------------
DROP TABLE IF EXISTS `mb_user_reaction`;
CREATE TABLE `mb_user_reaction`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户id',
  `target_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '目标id（exp_msg.exp_id / mb_work.work_id）',
  `target_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '目标类型：exp_msg / exp_video / work',
  `reaction_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '反应类型：like / collect / play',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_target_reaction`(`user_id` ASC, `target_id` ASC, `target_type` ASC, `reaction_type` ASC) USING BTREE,
  INDEX `idx_user_reaction_time`(`user_id` ASC, `created_at` DESC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 31 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '移动端用户互动记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for mb_work
-- ----------------------------
DROP TABLE IF EXISTS `mb_work`;
CREATE TABLE `mb_work`  (
  `work_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
  `student_user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '学生 user_id',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '作品标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '作品描述',
  `work_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'homework' COMMENT 'homework/remix/creative',
  `task_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '关联任务 id',
  `source_exp_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '关联实验 exp_msg.exp_id（拍同款来源）',
  `submission_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '关联提交 id',
  `grade` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '批阅等级文案',
  `review_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'pending' COMMENT 'pending/reviewed',
  `is_featured` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'n' COMMENT '教师标记展示 y/n',
  `teacher_review_name` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '批阅教师名',
  `teacher_review_text` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '教师评语',
  `teacher_review_stars` int NULL DEFAULT NULL COMMENT '星级 1-5',
  `like_count` int NOT NULL DEFAULT 0,
  `comment_count` int NOT NULL DEFAULT 0,
  `class_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '班级展示名',
  `school_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '学校名',
  `class_org_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '学生班级 org_id',
  `school_grade_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '学生年级 g1~g6',
  `review_time` datetime NULL DEFAULT NULL COMMENT '教师审核通过时间',
  `tint` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '卡片色调 class',
  `status` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'y' COMMENT 'y/n',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`work_id`) USING BTREE,
  INDEX `idx_mb_work_student`(`student_user_id` ASC, `create_time` DESC) USING BTREE,
  INDEX `idx_mb_work_type`(`work_type` ASC, `create_time` DESC) USING BTREE,
  INDEX `idx_mb_work_review`(`review_status` ASC, `create_time` DESC) USING BTREE,
  INDEX `idx_mb_work_feed`(`review_status` ASC, `review_time` ASC, `create_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '移动端作品' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for mb_work_file
-- ----------------------------
DROP TABLE IF EXISTS `mb_work_file`;
CREATE TABLE `mb_work_file`  (
  `file_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
  `work_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '作品 id',
  `file_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'image' COMMENT 'image/video',
  `file_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `file_size` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `file_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件访问 URL',
  `grad_class` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '预览渐变 class',
  `icon_emoji` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `duration` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '视频时长',
  `sort_order` int NOT NULL DEFAULT 0,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`file_id`) USING BTREE,
  INDEX `idx_mb_work_file_work`(`work_id` ASC, `sort_order` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '移动端作品附件' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for school_notice
-- ----------------------------
DROP TABLE IF EXISTS `school_notice`;
CREATE TABLE `school_notice`  (
  `notice_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键id',
  `notice_name` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '通知名称',
  `notice_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `notice_org_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '组织id',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '状态(c草稿,y正式,n作废,r已发布)',
  `create_user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建人id',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '更新人id',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `release_user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发布人id',
  `release_time` datetime NULL DEFAULT NULL COMMENT '发布时间',
  PRIMARY KEY (`notice_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '通知公共' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for subject_group
-- ----------------------------
DROP TABLE IF EXISTS `subject_group`;
CREATE TABLE `subject_group`  (
  `group_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '组唯一ID',
  `group_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '组名称',
  `subject_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '所属学科ID',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'y' COMMENT '状态: y-启用, n-停用',
  `comments` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '说明/备注',
  `create_user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '创建人id',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '最后更新人id',
  `update_time` datetime NULL DEFAULT NULL COMMENT '最后更新时间',
  PRIMARY KEY (`group_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '课题组' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for subject_group_member
-- ----------------------------
DROP TABLE IF EXISTS `subject_group_member`;
CREATE TABLE `subject_group_member`  (
  `seq_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '序列ID',
  `group_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '关联组ID',
  `member_user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '成员用户ID',
  `create_user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '添加人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  PRIMARY KEY (`seq_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '课题组成员' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for subject_group_researcher
-- ----------------------------
DROP TABLE IF EXISTS `subject_group_researcher`;
CREATE TABLE `subject_group_researcher`  (
  `seq_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '序列ID',
  `group_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '关联组ID',
  `researcher_user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '研究员用户ID',
  `create_user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '添加人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  PRIMARY KEY (`seq_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '课题组教研员' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for subject_group_tearcher
-- ----------------------------
DROP TABLE IF EXISTS `subject_group_tearcher`;
CREATE TABLE `subject_group_tearcher`  (
  `seq_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '序列ID',
  `group_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '关联组ID',
  `teacher_user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '教师用户ID',
  `create_user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '添加人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  PRIMARY KEY (`seq_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '课题组教师' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_log`;
CREATE TABLE `sys_log`  (
  `log_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '主键id',
  `user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作人id',
  `log_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '日志类型',
  `log_time` datetime NULL DEFAULT NULL COMMENT '记录时间',
  `log_data_type` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作数据类型',
  `log_data_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作数据id',
  `log_data_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '操作数据内容（JSON 格式的详细上下文）',
  PRIMARY KEY (`log_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统日志' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu`  (
  `menu_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '主键id',
  `parent_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '父级菜单ID',
  `menu_name` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '菜单/页面名称',
  `menu_code` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '稳定业务编码，用于权限锚点',
  `menu_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'menu/page/button',
  `path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '路由路径',
  `component` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '前端组件或页面标识',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序',
  `status` varchar(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'y' COMMENT '状态：y启用/n停用',
  `comments` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`menu_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统菜单目录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_msg
-- ----------------------------
DROP TABLE IF EXISTS `sys_msg`;
CREATE TABLE `sys_msg`  (
  `msg_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '主键id',
  `receiver_user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '接收人id',
  `sender_user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '发送人id',
  `msg_type_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '消息类型id',
  `msg_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息内容',
  `read_tag` varchar(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '0' COMMENT '阅读状态：0 未读，1 已读',
  `send_time` datetime NULL DEFAULT NULL COMMENT '发送时间',
  `read_time` datetime NULL DEFAULT NULL COMMENT '阅读时间',
  `link_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '关联id',
  PRIMARY KEY (`msg_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '消息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_org
-- ----------------------------
DROP TABLE IF EXISTS `sys_org`;
CREATE TABLE `sys_org`  (
  `org_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '主键id',
  `org_name` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '组织名称',
  `org_type_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '组织类型id',
  `parent_org_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '父组织id',
  `org_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '物化路径，如/root/school1/class1',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '状态：y 启用，n 停用',
  `sort_order` int NULL DEFAULT NULL COMMENT '排序',
  `create_user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人id',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '最后更新人id',
  `update_time` datetime NULL DEFAULT NULL COMMENT '最后更新时间',
  PRIMARY KEY (`org_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '组织（学校/班级/课题组等）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `role_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `role_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `role_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` varchar(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'y' COMMENT '状态：y启用/n停用',
  PRIMARY KEY (`role_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu`  (
  `seq_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '主键id',
  `role_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色id',
  `menu_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '菜单id',
  PRIMARY KEY (`seq_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色菜单关联' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '主键id',
  `user_name` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '姓名',
  `user_org_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '所属组织id',
  `user_role_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '主要用户类型id',
  `root_org_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '根组织id',
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
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `seq_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '主键id',
  `user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户id',
  `role_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色id',
  PRIMARY KEY (`seq_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户角色关联' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for teacher_class
-- ----------------------------
DROP TABLE IF EXISTS `teacher_class`;
CREATE TABLE `teacher_class`  (
  `seq_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `teacher_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '教师id',
  `class_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '班级id',
  PRIMARY KEY (`seq_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '教师授课班级关系表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for teacher_subject
-- ----------------------------
DROP TABLE IF EXISTS `teacher_subject`;
CREATE TABLE `teacher_subject`  (
  `seq_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '主键id',
  `teacher_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '教师人员id（sys_user.user_id）',
  `subject_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '学科id（data_school_subject.subject_id）',
  `status` varchar(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'y' COMMENT '状态：y启用 n停用',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `create_user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`seq_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '教师授课关系' ROW_FORMAT = DYNAMIC;

SET FOREIGN_KEY_CHECKS = 1;
