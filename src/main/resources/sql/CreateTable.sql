/*
 Navicat Premium Dump SQL

 Source Server         : 本地开发环境
 Source Server Type    : MySQL
 Source Server Version : 80033 (8.0.33)
 Source Host           : localdev:3306
 Source Schema         : smart_medical

 Target Server Type    : MySQL
 Target Server Version : 80033 (8.0.33)
 File Encoding         : 65001

 Date: 11/04/2026 09:24:38

 数据分离说明：
 本文件包含：DDL（建表语句）+ 基础配置数据（角色/用户/科室/医生/药品等）
 TestData.sql包含：业务流程数据（排班/挂号/病历/处方等）
 基础配置数据只读，业务数据可清空重建
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for account
-- ----------------------------
DROP TABLE IF EXISTS `account`;
CREATE TABLE `account`  (
  `id` bigint UNSIGNED NOT NULL COMMENT '账户id',
  `is_enabled` tinyint(1) NULL DEFAULT NULL COMMENT '启用（0否1是）',
  `user_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '用户id',
  `role_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '用户角色',
  `password` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '密码(加密)',
  `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
  `phone` char(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日期',
  `is_deleted` tinyint(1) NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `account_phone_uindex`(`phone` ASC) USING BTREE COMMENT '手机号索引',
  INDEX `account_role_id_fk`(`role_id` ASC) USING BTREE,
  INDEX `account_admin_id_fk`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '账户表（用于登录）' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of account
-- ----------------------------
INSERT INTO `account` VALUES (2, 1, 1, 1, 'e10adc3949ba59abbe56e057f20f883e', 'admin@example.com', '13996001338', '2026-02-28 09:54:02', '2026-02-28 09:54:02', 0);

-- ----------------------------
-- Table structure for admin
-- ----------------------------
DROP TABLE IF EXISTS `admin`;
CREATE TABLE `admin`  (
  `id` bigint UNSIGNED NOT NULL COMMENT '管理员id',
  `name` char(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '管理员名称',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '备注',
  `department_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '部门id',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日期',
  `is_deleted` tinyint(1) NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_department_id`(`department_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '管理员信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of admin
-- ----------------------------
INSERT INTO `admin` VALUES (1, '系统管理员', 'https://example.com/admin1.jpg', '超级管理员', 1, '2026-02-28 09:54:02', '2026-02-28 09:54:02', 0);

-- ----------------------------
-- Table structure for appointment_blacklist
-- ----------------------------
DROP TABLE IF EXISTS `appointment_blacklist`;
CREATE TABLE `appointment_blacklist`  (
  `id` bigint UNSIGNED NOT NULL COMMENT 'ID',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '用户ID',
  `patient_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '就诊人ID',
  `blacklist_type` tinyint(1) NOT NULL COMMENT '类型(1爽约2频繁取消3其他)',
  `reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '加入黑名单原因',
  `start_date` date NOT NULL COMMENT '开始日期',
  `end_date` date NOT NULL COMMENT '结束日期',
  `is_active` tinyint(1) NULL DEFAULT 1 COMMENT '是否生效',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_is_active`(`is_active` ASC, `end_date` ASC) USING BTREE,
  INDEX `idx_patient_id`(`patient_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '预约黑名单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of appointment_blacklist
-- ----------------------------
INSERT INTO `appointment_blacklist` VALUES (1, 3, 3, 1, '连续3次爽约', '2024-03-01', '2024-04-01', 1, '2026-02-28 09:54:04', '2026-02-28 09:54:04', 0);
INSERT INTO `appointment_blacklist` VALUES (2, 5, 5, 2, '单日取消预约5次', '2024-03-05', '2024-03-12', 0, '2026-02-28 09:54:04', '2026-02-28 09:54:04', 0);
INSERT INTO `appointment_blacklist` VALUES (3, 1, 7, 3, '恶意刷号', '2024-02-15', '2024-05-15', 1, '2026-02-28 09:54:04', '2026-02-28 09:54:04', 0);

-- ----------------------------
-- Table structure for appointment_rule
-- ----------------------------
DROP TABLE IF EXISTS `appointment_rule`;
CREATE TABLE `appointment_rule`  (
  `id` bigint UNSIGNED NOT NULL COMMENT '规则ID',
  `rule_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '规则名称',
  `rule_type` tinyint(1) NOT NULL COMMENT '规则类型(0普通1专家2急诊)',
  `department_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '适用科室ID(NULL表示全院通用)',
  `doctor_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '适用医生ID(NULL表示科室通用)',
  `advance_days` int NULL DEFAULT 7 COMMENT '可提前预约天数',
  `appointment_start_time` time NULL DEFAULT NULL COMMENT '开始预约时间(如08:00)',
  `appointment_end_time` time NULL DEFAULT NULL COMMENT '结束预约时间(如17:00)',
  `min_advance_hours` int NULL DEFAULT 2 COMMENT '最少提前小时数',
  `max_advance_days` int NULL DEFAULT 30 COMMENT '最大提前天数',
  `daily_quota` int NULL DEFAULT 50 COMMENT '每日号源总数',
  `per_time_quota` int NULL DEFAULT 10 COMMENT '每时间段号源数',
  `time_interval` int NULL DEFAULT 30 COMMENT '时间间隔(分钟)',
  `cancel_before_hours` int NULL DEFAULT 24 COMMENT '可取消预约提前小时数',
  `cancel_limit_per_day` int NULL DEFAULT 3 COMMENT '每日取消次数限制',
  `no_show_limit` int NULL DEFAULT 3 COMMENT '爽约次数限制',
  `no_show_lock_days` int NULL DEFAULT 30 COMMENT '爽约锁定天数',
  `holiday_quota_ratio` decimal(3, 2) NULL DEFAULT 0.50 COMMENT '节假日号源比例',
  `enable_holiday_appointment` tinyint(1) NULL DEFAULT 1 COMMENT '是否开启节假日预约',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '状态(0停用1启用)',
  `effective_date` date NOT NULL COMMENT '生效日期',
  `expiry_date` date NULL DEFAULT NULL COMMENT '失效日期',
  `priority` int NULL DEFAULT NULL COMMENT '优先级(0:医生 1、科室、2、医院）',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '规则描述',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_department_id`(`department_id` ASC) USING BTREE,
  INDEX `idx_doctor_id`(`doctor_id` ASC) USING BTREE,
  INDEX `idx_rule_type`(`rule_type` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '预约规则配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of appointment_rule
-- ----------------------------
INSERT INTO `appointment_rule` VALUES (1, '普通门诊默认规则', 0, NULL, NULL, 7, '08:00:00', '17:00:00', 2, 7, 50, 10, 30, 24, 3, 3, 30, 0.50, 1, 1, '2024-01-01', '2024-12-31', 2, '全院普通门诊默认规则', '2026-02-28 09:54:03', '2026-02-28 09:54:03', 0);
INSERT INTO `appointment_rule` VALUES (5, '急诊规则', 2, NULL, NULL, 1, '00:00:00', '23:59:00', 0, 7, 100, 20, 15, 2, 5, 5, 7, 1.00, 1, 1, '2024-01-01', '2024-12-31', 2, '急诊科预约规则', '2026-02-28 09:54:03', '2026-02-28 09:54:03', 0);

-- ----------------------------
-- Table structure for chat
-- ----------------------------
DROP TABLE IF EXISTS `chat`;
CREATE TABLE `chat`  (
  `id` bigint UNSIGNED NOT NULL COMMENT '聊天id',
  `send_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '发送id',
  `receive_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '接收id',
  `type` tinyint(1) NULL DEFAULT NULL COMMENT '聊天类型 0、用户-医生，1，医生-管理员',
  `content_type` tinyint(1) NULL DEFAULT NULL COMMENT '0文字，1图片，2、视频',
  `content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '内容',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日期',
  `is_deleted` tinyint(1) NULL DEFAULT 0 COMMENT '逻辑删除标记（0-正常，1-删除）',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_send_receive_id`(`send_id` ASC, `receive_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '聊天消息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of chat
-- ----------------------------

-- ----------------------------
-- Table structure for consultation_room
-- ----------------------------
DROP TABLE IF EXISTS `consultation_room`;
CREATE TABLE `consultation_room`  (
  `id` bigint UNSIGNED NOT NULL COMMENT '诊室ID',
  `sn` bigint UNSIGNED NOT NULL COMMENT '诊室编号',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '诊室名称(如: 专家诊室一)',
  `department_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '所属科室',
  `location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '具体位置',
  `equipment_desc` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '设备描述',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除标记（0-正常，1-删除）',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `fk_room_department`(`department_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '诊室信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for consultation_room_status
-- ----------------------------
DROP TABLE IF EXISTS `consultation_room_status`;
CREATE TABLE `consultation_room_status`  (
  `id` bigint UNSIGNED NOT NULL COMMENT 'ID',
  `consulting_room_id` bigint UNSIGNED NOT NULL COMMENT '诊室ID',
  `occupancy_start_time` datetime NULL DEFAULT NULL COMMENT '占用开始时间',
  `occupancy_end_time` datetime NULL DEFAULT NULL COMMENT '占用结束时间',
  `occupancy_reason` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '占用原因（如：设备维护、会议、午休等）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NULL DEFAULT 0 COMMENT '逻辑删除标记（0-正常，1-删除）',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '诊室占用状态记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of consultation_room_status
-- ----------------------------

-- ----------------------------
-- Table structure for degree
-- ----------------------------
DROP TABLE IF EXISTS `degree`;
CREATE TABLE `degree`  (
  `id` bigint UNSIGNED NOT NULL COMMENT '学历ID',
  `name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '学历名称（例如：博士、硕士、本科、大专等）',
  `level` tinyint UNSIGNED NULL DEFAULT NULL COMMENT '学历等级（用于排序，数字越小等级越高，如：1-博士，2-硕士，3-本科，4-大专）',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '学历描述',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NULL DEFAULT 0 COMMENT '是否删除（0-正常，1-删除）',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_level`(`level` ASC) USING BTREE,
  INDEX `idx_name`(`name` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '学历字典表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of degree
-- ----------------------------
INSERT INTO `degree` VALUES (1, '博士', 1, '最高学历', '2026-02-28 09:54:01', '2026-02-28 09:54:01', 0);
INSERT INTO `degree` VALUES (2, '硕士', 2, '研究生学历', '2026-02-28 09:54:01', '2026-02-28 09:54:01', 0);
INSERT INTO `degree` VALUES (3, '本科', 3, '本科学历', '2026-02-28 09:54:01', '2026-02-28 09:54:01', 0);
INSERT INTO `degree` VALUES (4, '大专', 4, '大专学历', '2026-02-28 09:54:01', '2026-02-28 09:54:01', 0);
INSERT INTO `degree` VALUES (5, '中专', 5, '中专学历', '2026-02-28 09:54:01', '2026-02-28 09:54:01', 0);

-- ----------------------------
-- Table structure for department
-- ----------------------------
DROP TABLE IF EXISTS `department`;
CREATE TABLE `department`  (
  `id` bigint UNSIGNED NOT NULL COMMENT '科室id',
  `sn` bigint UNSIGNED NULL DEFAULT NULL COMMENT '编号',
  `name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '科室名称',
  `type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '科室类别(0.临床，1、医技，2、行政)',
  `parent_department_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '上级科室ID',
  `manager` bigint UNSIGNED NULL DEFAULT NULL COMMENT '部门负责人id',
  `phone` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '科室电话',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '科室地址',
  `status` tinyint(1) NULL DEFAULT NULL COMMENT '状态(1启用/0停用)',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '描述',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间',
  `is_deleted` tinyint(1) NULL DEFAULT 0 COMMENT '删除（1是0否）',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_parent_department_id`(`parent_department_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '科室信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for doctor
-- ----------------------------
DROP TABLE IF EXISTS `doctor`;
CREATE TABLE `doctor`  (
  `id` bigint UNSIGNED NOT NULL COMMENT '医生id',
  `department_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '科室id',
  `doctor_position_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '医生职位id',
  `degree_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '学历',
  `name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '姓名',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '家庭住址',
  `scope` double NULL DEFAULT NULL COMMENT '评分',
  `tags` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '标签',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '描述',
  `status` tinyint(1) NULL DEFAULT NULL COMMENT '状态，0、在职、1、休假、2、出差、3、离职',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日期',
  `is_deleted` tinyint(1) NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `doctor_degree_id_fk`(`degree_id` ASC) USING BTREE,
  INDEX `doctor_department_id_fk`(`department_id` ASC) USING BTREE,
  INDEX `doctor_doctor_position_id_fk`(`doctor_position_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '医生信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for doctor_position
-- ----------------------------
DROP TABLE IF EXISTS `doctor_position`;
CREATE TABLE `doctor_position`  (
  `id` bigint UNSIGNED NOT NULL COMMENT '职位id',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '职位名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '职位描述',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日期',
  `is_deleted` tinyint(1) NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '医生职位字典表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of doctor_position
-- ----------------------------
INSERT INTO `doctor_position` VALUES (1, '主任医师', '正高级专业技术职称', '2026-02-28 09:54:02', '2026-02-28 09:54:02', 0);
INSERT INTO `doctor_position` VALUES (2, '副主任医师', '副高级专业技术职称', '2026-02-28 09:54:02', '2026-02-28 09:54:02', 0);
INSERT INTO `doctor_position` VALUES (3, '主治医师', '中级专业技术职称', '2026-02-28 09:54:02', '2026-02-28 09:54:02', 0);
INSERT INTO `doctor_position` VALUES (4, '住院医师', '初级专业技术职称', '2026-02-28 09:54:02', '2026-02-28 09:54:02', 0);
INSERT INTO `doctor_position` VALUES (5, '实习医师', '实习阶段医师', '2026-02-28 09:54:02', '2026-02-28 09:54:02', 0);

-- ----------------------------
-- Table structure for drug
-- ----------------------------
DROP TABLE IF EXISTS `drug`;
CREATE TABLE `drug`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '药品ID',
  `drug_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '药品编码',
  `common_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '通用名',
  `trade_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '商品名',
  `category_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '分类ID',
  `specification` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '规格',
  `unit` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '单位（盒/瓶/支等）',
  `manufacturer` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '生产厂家',
  `approval_number` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '批准文号',
  `dosage_form` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '剂型',
  `is_prescription` tinyint(1) NULL DEFAULT 1 COMMENT '是否处方药(0否1是)',
  `is_narcotic` tinyint(1) NULL DEFAULT 0 COMMENT '是否麻醉药品',
  `is_psychotropic` tinyint(1) NULL DEFAULT 0 COMMENT '是否精神药品',
  `is_antibiotic` tinyint(1) NULL DEFAULT 0 COMMENT '是否抗生素',
  `usage_method` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '使用方法',
  `side_effects` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '副作用',
  `contraindications` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '禁忌',
  `price` int NOT NULL COMMENT '单价（分）',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '状态(0停用1启用)',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_drug_code`(`drug_code` ASC) USING BTREE,
  INDEX `idx_category_id`(`category_id` ASC) USING BTREE,
  INDEX `idx_common_name`(`common_name` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9000000000000000002 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '药品信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of drug
-- ----------------------------
INSERT INTO `drug` VALUES (1, 'DRG001', '阿司匹林肠溶片', '拜阿司匹灵', 5, '100mg*30片', '盒', '拜耳医药', '国药准字H20065051', '片剂', 1, 0, 0, 0, '口服，每日一次，每次一片', '胃肠道反应，出血风险', '活动性出血禁用', 2800, 1, '2026-02-28 09:54:03', '2026-02-28 09:54:03', 0);
INSERT INTO `drug` VALUES (2, 'DRG002', '阿托伐他汀钙片', '立普妥', 5, '20mg*7片', '盒', '辉瑞制药', '国药准字H20051408', '片剂', 1, 0, 0, 0, '口服，每晚一次', '肌肉疼痛，肝酶升高', '活动性肝病禁用', 4500, 1, '2026-02-28 09:54:03', '2026-02-28 09:54:03', 0);
INSERT INTO `drug` VALUES (3, 'DRG003', '氨氯地平片', '络活喜', 5, '5mg*7片', '盒', '辉瑞制药', '国药准字H20093662', '片剂', 1, 0, 0, 0, '口服，每日一次', '水肿，头痛', '低血压禁用', 3200, 1, '2026-02-28 09:54:03', '2026-02-28 09:54:03', 0);
INSERT INTO `drug` VALUES (4, 'DRG004', '奥美拉唑肠溶胶囊', '洛赛克', 6, '20mg*14粒', '盒', '阿斯利康', '国药准字H20046379', '胶囊', 1, 0, 0, 0, '口服，每日一次', '头痛，腹泻', '严重肝肾功能不全慎用', 3800, 1, '2026-02-28 09:54:03', '2026-02-28 09:54:03', 0);
INSERT INTO `drug` VALUES (5, 'DRG005', '头孢克肟分散片', '世福素', 7, '100mg*6片', '盒', '广州白云山', '国药准字H20051838', '分散片', 1, 0, 0, 1, '口服，每日两次', '腹泻，皮疹', '青霉素过敏慎用', 2500, 1, '2026-02-28 09:54:03', '2026-02-28 09:54:03', 0);
INSERT INTO `drug` VALUES (6, 'DRG006', '连花清瘟胶囊', NULL, 8, '0.35g*24粒', '盒', '以岭药业', '国药准字Z20040063', '胶囊', 0, 0, 0, 0, '口服，每日三次', '恶心，腹泻', '风寒感冒不适用', 1500, 1, '2026-02-28 09:54:03', '2026-02-28 09:54:03', 0);
INSERT INTO `drug` VALUES (7, 'DRG007', '布洛芬缓释胶囊', '芬必得', 1, '0.3g*20粒', '盒', '中美史克', '国药准字H10900089', '缓释胶囊', 0, 0, 0, 0, '口服，每日两次', '胃肠道不适', '活动性消化道溃疡禁用', 1800, 1, '2026-02-28 09:54:03', '2026-02-28 09:54:03', 0);
INSERT INTO `drug` VALUES (8, 'DRG008', '盐酸二甲双胍片', '格华止', 1, '0.5g*20片', '盒', '默克制药', '国药准字H20023371', '片剂', 1, 0, 0, 0, '口服，每日两次', '胃肠道反应', '严重肾功能不全禁用', 2200, 1, '2026-02-28 09:54:03', '2026-02-28 09:54:03', 0);
INSERT INTO `drug` VALUES (9, 'DRG009', '氯化钠注射液', NULL, 4, '0.9% 250ml', '瓶', '大冢制药', '国药准字H20043271', '注射剂', 1, 0, 0, 0, '静脉滴注', '局部刺激', '心衰患者慎用', 850, 1, '2026-02-28 09:54:03', '2026-02-28 09:54:03', 0);
INSERT INTO `drug` VALUES (10, 'DRG010', '葡萄糖注射液', NULL, 4, '5% 250ml', '瓶', '大冢制药', '国药准字H20043272', '注射剂', 1, 0, 0, 0, '静脉滴注', '高血糖', '糖尿病患者慎用', 750, 1, '2026-02-28 09:54:04', '2026-02-28 09:54:04', 0);

-- ----------------------------
-- Table structure for drug_category
-- ----------------------------
DROP TABLE IF EXISTS `drug_category`;
CREATE TABLE `drug_category`  (
  `id` bigint UNSIGNED NOT NULL COMMENT '药品分类ID',
  `parent_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '父级分类ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分类名称',
  `level` tinyint NULL DEFAULT 1 COMMENT '分类层级',
  `sort` int NULL DEFAULT 0 COMMENT '排序',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '描述',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NULL DEFAULT 0 COMMENT '是否删除(0否1是)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '药品分类表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of drug_category
-- ----------------------------
INSERT INTO `drug_category` VALUES (1, NULL, '西药', 1, 1, '化学药品', '2026-02-28 09:54:03', '2026-02-28 09:54:03', 0);
INSERT INTO `drug_category` VALUES (2, NULL, '中成药', 1, 2, '中药制剂', '2026-02-28 09:54:03', '2026-02-28 09:54:03', 0);
INSERT INTO `drug_category` VALUES (3, NULL, '中药饮片', 1, 3, '中药材', '2026-02-28 09:54:03', '2026-02-28 09:54:03', 0);
INSERT INTO `drug_category` VALUES (4, NULL, '医疗器械', 1, 4, '医用耗材', '2026-02-28 09:54:03', '2026-02-28 09:54:03', 0);
INSERT INTO `drug_category` VALUES (5, 1, '心血管用药', 2, 1, '心血管系统药物', '2026-02-28 09:54:03', '2026-02-28 09:54:03', 0);
INSERT INTO `drug_category` VALUES (6, 1, '消化系统用药', 2, 2, '消化系统药物', '2026-02-28 09:54:03', '2026-02-28 09:54:03', 0);
INSERT INTO `drug_category` VALUES (7, 1, '抗生素', 2, 3, '抗感染药物', '2026-02-28 09:54:03', '2026-02-28 09:54:03', 0);
INSERT INTO `drug_category` VALUES (8, 2, '感冒用药', 2, 4, '感冒类中成药', '2026-02-28 09:54:03', '2026-02-28 09:54:03', 0);

-- ----------------------------
-- Table structure for drug_inventory
-- ----------------------------
-- 豁免说明：drug_inventory 为高频热点更新表，按 AGENTS.md 省略 create_time / is_deleted；id 保留。
DROP TABLE IF EXISTS `drug_inventory`;
CREATE TABLE `drug_inventory`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '库存记录ID',
  `drug_id` bigint UNSIGNED NOT NULL COMMENT '药品ID',
  `warehouse_id` bigint UNSIGNED NOT NULL COMMENT '仓库ID',
  `stock_quantity` int NOT NULL DEFAULT 0 COMMENT '库存数量',
  `available_quantity` int NOT NULL DEFAULT 0 COMMENT '可用数量(可销售/可出库)',
  `locked_quantity` int NOT NULL DEFAULT 0 COMMENT '锁定数量(已下单未出库)',
  `min_stock` int NOT NULL DEFAULT 10 COMMENT '最低库存预警线',
  `max_stock` int NOT NULL DEFAULT 1000 COMMENT '最高库存',
  `last_inbound_time` datetime NULL DEFAULT NULL COMMENT '最后入库时间',
  `last_outbound_time` datetime NULL DEFAULT NULL COMMENT '最后出库时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_drug_warehouse`(`drug_id` ASC, `warehouse_id` ASC) USING BTREE,
  INDEX `idx_drug_id`(`drug_id` ASC) USING BTREE,
  INDEX `idx_stock_quantity`(`stock_quantity` ASC) USING BTREE,
  INDEX `idx_warehouse_id`(`warehouse_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9000000000000000002 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '药品库存表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for file
-- ----------------------------
DROP TABLE IF EXISTS `file`;
CREATE TABLE `file`  (
  `id` bigint UNSIGNED NOT NULL COMMENT '文件ID',
  `admin_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '上传管理员id',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件名',
  `md5` char(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件md5值',
  `path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件位置',
  `size` bigint UNSIGNED NULL DEFAULT NULL COMMENT '文件大小',
  `enable` tinyint(1) NULL DEFAULT NULL COMMENT '是否启用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '文件上传记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of file
-- ----------------------------
INSERT INTO `file` VALUES (1, 1, '挂号排班模板.cvs', '123123', 'admin/registration/template/排班导入模板.csv', 12, 1, '2026-03-18 16:59:24', '2026-03-18 16:59:26', 0);

-- ----------------------------
-- Table structure for inventory_transaction
-- ----------------------------
-- 豁免说明：inventory_transaction 为纯 append-only 库存流水，按 AGENTS.md 省略 update_time / is_deleted；id、create_time 保留。
DROP TABLE IF EXISTS `inventory_transaction`;
CREATE TABLE `inventory_transaction`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '库存异动流水ID',
  `drug_id` bigint UNSIGNED NOT NULL COMMENT '药品ID',
  `warehouse_id` bigint UNSIGNED NOT NULL COMMENT '仓库ID',
  `transaction_type` tinyint NOT NULL COMMENT '异动类型(1:入库,2:出库,3:盘点调整,4:锁定,5:解锁)',
  `related_order` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '关联单号(采购单/销售单等)',
  `quantity_change` int NOT NULL COMMENT '数量变化(正数表示增加，负数表示减少)',
  `quantity_before` int NOT NULL COMMENT '变化前数量',
  `quantity_after` int NOT NULL COMMENT '变化后数量',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `operator_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '操作人ID',
  `operator_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作人姓名',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  INDEX `idx_drug_warehouse`(`drug_id` ASC, `warehouse_id` ASC) USING BTREE,
  INDEX `idx_related_order`(`related_order` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9000000000000000002 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '库存异动流水表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for invoice
-- ----------------------------
DROP TABLE IF EXISTS `invoice`;
CREATE TABLE `invoice`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '发票ID',
  `sn` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '发票编号',
  `invoice_date` date NOT NULL COMMENT '开票日期',
  `customer_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '客户名称',
  `customer_address` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '客户地址',
  `customer_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系电话',
  `tax_sn` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '税号',
  `total_amount` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '总金额',
  `total_tax` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '总税额',
  `grand_total` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '含税总额',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `sn`(`sn` ASC) USING BTREE,
  INDEX `idx_invoice_number`(`sn` ASC) USING BTREE,
  INDEX `idx_customer_name`(`customer_name` ASC) USING BTREE,
  INDEX `idx_invoice_date`(`invoice_date` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '发票主表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of invoice
-- ----------------------------

-- ----------------------------
-- Table structure for order
-- ----------------------------
DROP TABLE IF EXISTS `order`;
CREATE TABLE `order`  (
  `id` bigint UNSIGNED NOT NULL COMMENT '订单唯一标识',
  `user_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '下单用户id',
  `patient_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '订单对应患者id',
  `order_type_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '订单类型id',
  `invoice_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '发票id',
  `sn` bigint UNSIGNED NULL DEFAULT NULL COMMENT '订单号，用于对外展示',
  `status` tinyint NULL DEFAULT NULL COMMENT '订单状态：0待支付，1已支付，2已取消，3已完成，4支付失败，5退款中，6已退款，7退款失败，8退款取消，9退款超时，10退款拒绝，11退款申请',
  `total_amount` int NULL DEFAULT NULL COMMENT '实际支付金额',
  `order_create_time` datetime NULL DEFAULT NULL COMMENT '订单创建时间',
  `order_update_time` datetime NULL DEFAULT NULL COMMENT '订单更新时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日期',
  `is_deleted` tinyint(1) NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `order_order_type_id_fk`(`order_type_id` ASC) USING BTREE,
  INDEX `order_user_id_fk`(`user_id` ASC) USING BTREE,
  INDEX `idx_order_patient`(`patient_id` ASC, `is_deleted` ASC) USING BTREE,
  INDEX `idx_sn`(`sn` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单主表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of order
-- ----------------------------

-- ----------------------------
-- Table structure for order_item
-- ----------------------------
DROP TABLE IF EXISTS `order_item`;
CREATE TABLE `order_item`  (
  `id` bigint UNSIGNED NOT NULL COMMENT '订单明细项id',
  `order_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '订单id',
  `production_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '项目明细ID',
  `production_type_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '项目类型ID',
  `production_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '订单名称 冗余字段',
  `quantity` int NULL DEFAULT NULL COMMENT '购买数量',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日期',
  `is_deleted` tinyint(1) NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `order_item_drug_id_fk`(`production_id` ASC) USING BTREE,
  INDEX `order_item_order_id_fk`(`order_id` ASC) USING BTREE,
  INDEX `order_item_production_type_id_fk`(`production_type_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单明细表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of order_item
-- ----------------------------

-- ----------------------------
-- Table structure for order_status_log
-- ----------------------------
DROP TABLE IF EXISTS `order_status_log`;
CREATE TABLE `order_status_log`  (
  `id` bigint UNSIGNED NOT NULL COMMENT '日志ID',
  `order_id` bigint UNSIGNED NOT NULL COMMENT '关联订单ID',
  `from_status` tinyint(1) NOT NULL COMMENT '变更前状态',
  `to_status` tinyint(1) NOT NULL COMMENT '变更后状态',
  `operator_id` bigint UNSIGNED NOT NULL COMMENT '操作人ID (0=系统)',
  `operator_role` varchar(19) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '操作人角色',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '变更备注/原因',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE COMMENT '创建时间索引',
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE COMMENT '订单ID索引'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单状态流转日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of order_status_log
-- ----------------------------

-- ----------------------------
-- Table structure for order_type
-- ----------------------------
DROP TABLE IF EXISTS `order_type`;
CREATE TABLE `order_type`  (
  `id` bigint UNSIGNED NOT NULL COMMENT '订单类型id',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '订单类型名称',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日期',
  `is_deleted` tinyint(1) NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单类型字典表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of order_type
-- ----------------------------
INSERT INTO `order_type` VALUES (1, '挂号订单', '2026-02-28 09:54:03', '2026-02-28 09:54:03', 0);
INSERT INTO `order_type` VALUES (2, '药品订单', '2026-02-28 09:54:03', '2026-02-28 09:54:03', 0);
INSERT INTO `order_type` VALUES (3, '检查订单', '2026-02-28 09:54:03', '2026-02-28 09:54:03', 0);
INSERT INTO `order_type` VALUES (4, '住院押金', '2026-02-28 09:54:03', '2026-02-28 09:54:03', 0);
INSERT INTO `order_type` VALUES (5, '体检订单', '2026-02-28 09:54:03', '2026-02-28 09:54:03', 0);

-- ----------------------------
-- Table structure for patient
-- ----------------------------
DROP TABLE IF EXISTS `patient`;
CREATE TABLE `patient`  (
  `id` bigint UNSIGNED NOT NULL COMMENT '患者ID',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '患者姓名',
  `id_card` varchar(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '规范化身份证号',
  `phone` char(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '患者联系电话',
  `sex` tinyint(1) NULL DEFAULT NULL COMMENT '性别（0男，1女）',
  `emergency_phone` char(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '紧急联系电话',
  `owner_user_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '本人绑定账号ID',
  `created_by_user_id` bigint UNSIGNED NOT NULL COMMENT '创建账号ID',
  `patient_card_id` bigint UNSIGNED NOT NULL COMMENT '当前就诊卡ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日期',
  `is_deleted` tinyint(1) NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_patient_id_card`(`id_card` ASC) USING BTREE,
  UNIQUE INDEX `uk_patient_owner_user`(`owner_user_id` ASC) USING BTREE,
  UNIQUE INDEX `uk_patient_card`(`patient_card_id` ASC) USING BTREE,
  INDEX `idx_patient_created_by`(`created_by_user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '就诊人档案表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for patient_card
-- ----------------------------
DROP TABLE IF EXISTS `patient_card`;
CREATE TABLE `patient_card`  (
  `id` bigint UNSIGNED NOT NULL COMMENT '就诊卡id',
  `sn` bigint UNSIGNED NULL DEFAULT NULL COMMENT '就诊卡卡号',
  `inpatient_balance` int NULL DEFAULT NULL COMMENT '住院余额',
  `outpatient_balance` int NULL DEFAULT NULL COMMENT '门诊余额',
  `payment_password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '支付密码（加密存储）',
  `status` tinyint(1) NULL DEFAULT NULL COMMENT '就诊卡状态，0-正常，1-冻结 2-注销',
  `qr_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '就诊卡二维码URL',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日期',
  `is_deleted` tinyint(1) NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `sn_unique`(`sn` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '就诊卡信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for payment_method
-- ----------------------------
DROP TABLE IF EXISTS `payment_method`;
CREATE TABLE `payment_method`  (
  `id` bigint UNSIGNED NOT NULL COMMENT '支付方式id',
  `name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '支付方式名称',
  `parent_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '父级支付类型',
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图标',
  `is_default` tinyint(1) NULL DEFAULT NULL COMMENT '是否是默认支付方式',
  `sort` tinyint(1) NULL DEFAULT NULL COMMENT '排序',
  `is_enabled` tinyint(1) NULL DEFAULT NULL COMMENT '是否启用',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '支付方式描述',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日期',
  `is_deleted` tinyint(1) NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '支付方式字典表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of payment_method
-- ----------------------------
INSERT INTO `payment_method` VALUES (1, '微信支付', NULL, 'wechat_icon', 1, 1, 1, '微信支付方式', '2026-02-28 09:54:03', '2026-02-28 09:54:03', 0);
INSERT INTO `payment_method` VALUES (2, '支付宝', NULL, 'alipay_icon', 0, 2, 1, '支付宝支付方式', '2026-02-28 09:54:03', '2026-02-28 09:54:03', 0);
INSERT INTO `payment_method` VALUES (3, '医保支付', NULL, 'yibao_icon', 0, 3, 1, '医保卡支付', '2026-02-28 09:54:03', '2026-02-28 09:54:03', 0);
INSERT INTO `payment_method` VALUES (4, '现金支付', NULL, 'cash_icon', 0, 4, 1, '现金支付', '2026-02-28 09:54:03', '2026-02-28 09:54:03', 0);
INSERT INTO `payment_method` VALUES (5, '银行卡', NULL, 'card_icon', 0, 5, 1, '银行卡支付', '2026-02-28 09:54:03', '2026-02-28 09:54:03', 0);

-- ----------------------------
-- Table structure for payment_record
-- ----------------------------
DROP TABLE IF EXISTS `payment_record`;
CREATE TABLE `payment_record`  (
  `id` bigint UNSIGNED NOT NULL COMMENT '支付记录ID',
  `sn` bigint UNSIGNED NULL DEFAULT NULL COMMENT '支付流水号',
  `order_id` bigint UNSIGNED NOT NULL COMMENT '关联订单ID',
  `total_amount` int NOT NULL COMMENT '支付金额',
  `real_amount` int NULL DEFAULT NULL COMMENT '实际支付金额',
  `payment_method_id` tinyint(1) NOT NULL COMMENT '支付渠道: 1微信 2支付宝 3医保 4现金',
  `status` tinyint(1) NOT NULL COMMENT '支付状态: 0待支付 1支付中 2成功 3失败 4已退款',
  `transaction_sn` bigint UNSIGNED NULL DEFAULT NULL COMMENT '第三方交易流水号',
  `payment_time` datetime NULL DEFAULT NULL COMMENT '支付成功时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0未删除 1已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE COMMENT '订单ID索引',
  UNIQUE KEY `uk_transaction_sn`(`transaction_sn` ASC) USING BTREE COMMENT '第三方交易流水号唯一索引，防止重复回调产生多条支付记录'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '支付记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of payment_record
-- ----------------------------

-- ----------------------------
-- Table structure for price
-- ----------------------------
DROP TABLE IF EXISTS `price`;
CREATE TABLE `price`  (
  `id` bigint UNSIGNED NOT NULL COMMENT '价格ID',
  `type` tinyint(1) NOT NULL COMMENT '价格类型（1:普通门诊 2:急诊 3:节假日）',
  `price` int NOT NULL COMMENT '挂号价格（分）',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '价格描述（如：主任医师普通门诊）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NULL DEFAULT 0 COMMENT '是否删除（0:正常 1:删除）',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_price_type`(`type` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '挂号价格表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of price
-- ----------------------------
INSERT INTO `price` VALUES (1, 1, 2000, '普通门诊', '2026-02-28 09:54:03', '2026-02-28 09:54:03', 0);
INSERT INTO `price` VALUES (2, 1, 5000, '主任医师门诊', '2026-02-28 09:54:03', '2026-02-28 09:54:03', 0);
INSERT INTO `price` VALUES (3, 2, 3000, '急诊', '2026-02-28 09:54:03', '2026-02-28 09:54:03', 0);
INSERT INTO `price` VALUES (4, 3, 2500, '节假日门诊', '2026-02-28 09:54:03', '2026-02-28 09:54:03', 0);
INSERT INTO `price` VALUES (5, 1, 8000, '特需门诊', '2026-02-28 09:54:03', '2026-02-28 09:54:03', 0);

-- ----------------------------
-- Table structure for production_type
-- ----------------------------
DROP TABLE IF EXISTS `production_type`;
CREATE TABLE `production_type`  (
  `id` bigint UNSIGNED NOT NULL COMMENT '类型id',
  `name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '类型名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '描述',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日期',
  `is_deleted` tinyint(1) NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '项目类型字典表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of production_type
-- ----------------------------
INSERT INTO `production_type` VALUES (1, '药品', '药品类项目', '2026-02-28 09:54:04', '2026-02-28 09:54:04', 0);
INSERT INTO `production_type` VALUES (2, '检查', '检查检验类项目', '2026-02-28 09:54:04', '2026-02-28 09:54:04', 0);
INSERT INTO `production_type` VALUES (3, '治疗', '治疗处置类项目', '2026-02-28 09:54:04', '2026-02-28 09:54:04', 0);
INSERT INTO `production_type` VALUES (4, '手术', '手术类项目', '2026-02-28 09:54:04', '2026-02-28 09:54:04', 0);
INSERT INTO `production_type` VALUES (5, '材料', '医用材料类项目', '2026-02-28 09:54:04', '2026-02-28 09:54:04', 0);
INSERT INTO `production_type` VALUES (6, '挂号', '挂号类项目', '2026-02-28 09:54:04', '2026-02-28 09:54:04', 0);

-- ----------------------------
-- Table structure for registration
-- ----------------------------
DROP TABLE IF EXISTS `registration`;
CREATE TABLE `registration`  (
  `id` bigint UNSIGNED NOT NULL COMMENT '挂号记录ID',
  `user_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '关联用户ID',
  `patient_id` bigint UNSIGNED NOT NULL COMMENT '实际就诊患者ID',
  `registration_schedule_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '关联挂号时段id',
  `order_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '关联订单ID',
  `status` tinyint(1) NULL DEFAULT NULL COMMENT '0-等待支付 1-支付成功/待就诊 2-支付失败 3-取消 4-完成 5-已报到 6-就诊中 7-待支付(处方补缴)',
  `registration_time` datetime NULL DEFAULT NULL COMMENT '挂号时间',
  `check_in_time` datetime NULL DEFAULT NULL COMMENT '患者报到时间',
  `visit_start_time` datetime NULL DEFAULT NULL COMMENT '医生接诊时间',
  `visit_end_time` datetime NULL DEFAULT NULL COMMENT '医生结束就诊时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日期',
  `is_deleted` tinyint(1) NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_reg_schedule_patient`(`registration_schedule_id` ASC, `patient_id` ASC) USING BTREE COMMENT '同患者同排班唯一，防并发重复挂号',
  INDEX `registration_schedule_id_fk`(`registration_schedule_id` ASC) USING BTREE,
  INDEX `registration_order_id_fk`(`order_id` ASC) USING BTREE,
  INDEX `registration_user_id_fk`(`user_id` ASC) USING BTREE,
  INDEX `idx_registration_patient`(`patient_id` ASC, `is_deleted` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '挂号记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of registration
-- ----------------------------

-- ----------------------------
-- Table structure for registration_schedule
-- ----------------------------
DROP TABLE IF EXISTS `registration_schedule`;
CREATE TABLE `registration_schedule`  (
  `id` bigint UNSIGNED NOT NULL COMMENT '排班ID',
  `doctor_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '关联医生ID',
  `registration_schedule_template_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '生成模板id',
  `start_time` datetime NULL DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '结束时间',
  `status` tinyint(1) NULL DEFAULT NULL COMMENT '状态: 1-正常, 0-停诊, 2-已满',
  `remaining_quota` int NULL DEFAULT NULL COMMENT '总号源数' CHECK (`remaining_quota` >= 0),
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日期',
  `is_deleted` tinyint(1) NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `registration_schedule_doctor_id_fk`(`doctor_id` ASC) USING BTREE,
  INDEX `registration_schedule_registration_schedule_template_id_fk`(`registration_schedule_template_id` ASC) USING BTREE,
  INDEX `idx_start_time`(`start_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '分配到每个小时的挂号' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of registration_schedule
-- ----------------------------

-- ----------------------------
-- Table structure for registration_schedule_template
-- ----------------------------
DROP TABLE IF EXISTS `registration_schedule_template`;
CREATE TABLE `registration_schedule_template`  (
  `id` bigint UNSIGNED NOT NULL COMMENT '挂号排班模板id',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '名称',
  `doctor_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '关联医生id',
  `registration_date` date NULL DEFAULT NULL COMMENT '挂号日期',
  `registration_type` tinyint(1) NULL DEFAULT NULL COMMENT '挂号类型（0普通号 1、专家号 2、 外院专家号）',
  `start_time` time NULL DEFAULT NULL COMMENT '开始时间',
  `end_time` time NULL DEFAULT NULL COMMENT '结束时间',
  `total_quota` int NULL DEFAULT NULL COMMENT '总号源数',
  `price` int UNSIGNED NULL DEFAULT NULL COMMENT '挂号价格',
  `priority` int NULL DEFAULT NULL COMMENT '优先级(0、普通;1、特殊;2、紧急)',
  `is_enabled` tinyint(1) NULL DEFAULT NULL COMMENT '是否启用',
  `consultation_room_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '就诊房间id',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '备注',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日期',
  `is_deleted` tinyint(1) NULL DEFAULT 0 COMMENT '是否删除',
  `status` int NULL DEFAULT NULL COMMENT '模板状态',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '挂号排班模板表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of registration_schedule_template
-- ----------------------------

-- ----------------------------
-- Table structure for role
-- ----------------------------
DROP TABLE IF EXISTS `role`;
CREATE TABLE `role`  (
  `id` bigint UNSIGNED NOT NULL COMMENT '角色id',
  `name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '角色名称',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '角色描述',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统角色字典表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of role
-- ----------------------------
-- 角色与 RoleEnum 对齐：仅保留 1/2/4/6，护士(3)、收费员(5) 已废弃
INSERT INTO `role` VALUES (1, '系统管理员', '系统超级管理员', '2026-02-28 09:54:02', '2026-02-28 09:54:02', 0);
INSERT INTO `role` VALUES (2, '医生', '医生角色', '2026-02-28 09:54:02', '2026-02-28 09:54:02', 0);
INSERT INTO `role` VALUES (4, '患者', '患者角色', '2026-02-28 09:54:02', '2026-02-28 09:54:02', 0);
INSERT INTO `role` VALUES (6, '药师', '药房工作人员', '2026-02-28 09:54:02', '2026-02-28 09:54:02', 0);

-- ----------------------------
-- Table structure for shift
-- ----------------------------
DROP TABLE IF EXISTS `shift`;
CREATE TABLE `shift`  (
  `id` bigint UNSIGNED NOT NULL COMMENT '班次ID',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '班次名称',
  `start_time` time NULL DEFAULT NULL COMMENT '开始时间',
  `end_time` time NULL DEFAULT NULL COMMENT '结束时间',
  `work_hours` double NULL DEFAULT NULL COMMENT '工时',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '描述',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日期',
  `is_deleted` tinyint(1) NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '班次字典表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of shift
-- ----------------------------
INSERT INTO `shift` VALUES (1, '早班', '08:00:00', '12:00:00', 4, '上午工作班次', '2026-02-28 09:54:02', '2026-02-28 09:54:02', 0);
INSERT INTO `shift` VALUES (2, '午班', '14:00:00', '18:00:00', 4, '下午工作班次', '2026-02-28 09:54:02', '2026-02-28 09:54:02', 0);
INSERT INTO `shift` VALUES (3, '晚班', '18:00:00', '22:00:00', 4, '晚间工作班次', '2026-02-28 09:54:02', '2026-02-28 09:54:02', 0);
INSERT INTO `shift` VALUES (4, '全天班', '08:00:00', '18:00:00', 8, '全天工作班次（午休2小时）', '2026-02-28 09:54:02', '2026-02-28 09:54:02', 0);
INSERT INTO `shift` VALUES (5, '夜班', '22:00:00', '08:00:00', 10, '夜间值班', '2026-02-28 09:54:02', '2026-02-28 09:54:02', 0);

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint UNSIGNED NOT NULL COMMENT '用户id',
  `nickname` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户姓名',
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '登录用户名（冗余字段）',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '家庭住址',
  `sex` tinyint(1) NULL DEFAULT NULL COMMENT '性别（0男，1女)',
  `id_card` char(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '身份证',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日期',
  `is_deleted` tinyint(1) NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_id_card`(`id_card` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '患者/用户信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, '系统管理员', 'admin', '', NULL, NULL, NULL, '2026-02-28 09:54:02', '2026-02-28 09:54:02', 0);

-- ----------------------------
-- Table structure for user_patient_relation
-- ----------------------------
DROP TABLE IF EXISTS `user_patient_relation`;
CREATE TABLE `user_patient_relation`  (
  `id` bigint UNSIGNED NOT NULL COMMENT '就诊人关系Id',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '用户id',
  `patient_id` bigint UNSIGNED NOT NULL COMMENT '患者id',
  `remark` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注标题',
  `is_default` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否是默认就诊人',
  `relation` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '与就诊人关系',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NULL DEFAULT 0 COMMENT '是否删除',
  `active_patient_id` bigint UNSIGNED GENERATED ALWAYS AS (
    CASE WHEN `is_deleted` = 0 THEN `patient_id` ELSE NULL END
  ) STORED COMMENT '有效关系患者ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_relation_user_patient_active`(`user_id` ASC, `active_patient_id` ASC) USING BTREE,
  INDEX `idx_relation_patient`(`patient_id` ASC, `is_deleted` ASC) USING BTREE,
  INDEX `user_patient_relation_user_id_fk`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '管理关系表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for warehouse
-- ----------------------------
DROP TABLE IF EXISTS `warehouse`;
CREATE TABLE `warehouse`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '仓库ID',
  `code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '仓库编码',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '仓库名称',
  `type` tinyint NULL DEFAULT 1 COMMENT '类型(1:药房,2:中心库,...)',
  `address` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '地址',
  `contact_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系电话',
  `manager` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '负责人',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '状态(0停用1启用)',
  `is_default` tinyint(1) NULL DEFAULT 0 COMMENT '是否默认仓库',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NULL DEFAULT 0 COMMENT '逻辑删除标记（0-正常，1-删除）',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_warehouse_code`(`code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9000000000000000002 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '仓库/药房表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for medical_record
-- ----------------------------
DROP TABLE IF EXISTS `medical_record`;
CREATE TABLE `medical_record`  (
  `id` bigint UNSIGNED NOT NULL COMMENT '病历ID',
  `registration_id` bigint UNSIGNED NOT NULL COMMENT '挂号ID(强绑定)',
  `doctor_id` bigint UNSIGNED NOT NULL COMMENT '接诊医生ID',
  `patient_id` bigint UNSIGNED NOT NULL COMMENT '就诊人ID',
  `chief_complaint` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '主诉',
  `present_illness` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '现病史',
  `past_history` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '既往史',
  `physical_exam` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '体格检查',
  `diagnosis` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '诊断结果',
  `treatment_plan` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '治疗方案',
  `status` tinyint(1) NULL DEFAULT NULL COMMENT '0-草稿 1-已提交',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NULL DEFAULT 0 COMMENT '逻辑删除标记（0-正常，1-删除）',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_registration_id`(`registration_id` ASC) USING BTREE,
  INDEX `idx_doctor_id`(`doctor_id` ASC) USING BTREE,
  INDEX `idx_patient_id`(`patient_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '电子病历表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for prescription
-- ----------------------------
DROP TABLE IF EXISTS `prescription`;
CREATE TABLE `prescription`  (
  `id` bigint UNSIGNED NOT NULL COMMENT '处方ID',
  `medical_record_id` bigint UNSIGNED NOT NULL COMMENT '关联病历ID',
  `order_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '关联药品订单',
  `status` tinyint(1) NULL DEFAULT NULL COMMENT '0-待支付 1-已支付 2-已发药 3-已取消',
  `total_amount` int NULL DEFAULT NULL COMMENT '处方金额(分)',
  `pharmacist_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '发药药师ID',
  `dispensed_at` datetime NULL DEFAULT NULL COMMENT '发药时间',
  `version` int NULL DEFAULT 0 COMMENT '乐观锁',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NULL DEFAULT 0 COMMENT '逻辑删除标记（0-正常，1-删除）',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_medical_record_id`(`medical_record_id` ASC) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '处方表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for prescription_item
-- ----------------------------
DROP TABLE IF EXISTS `prescription_item`;
CREATE TABLE `prescription_item`  (
  `id` bigint UNSIGNED NOT NULL COMMENT '处方明细ID',
  `prescription_id` bigint UNSIGNED NOT NULL COMMENT '处方ID',
  `drug_id` bigint UNSIGNED NOT NULL COMMENT '药品ID',
  `quantity` int NOT NULL COMMENT '数量',
  `unit_price` int NOT NULL COMMENT '快照单价(分)',
  `usage_method` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用药方式',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NULL DEFAULT 0 COMMENT '逻辑删除标记（0-正常，1-删除）',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_prescription_id`(`prescription_id` ASC) USING BTREE,
  INDEX `idx_drug_id`(`drug_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '处方明细表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for registration_status_log
-- ----------------------------
DROP TABLE IF EXISTS `registration_status_log`;
CREATE TABLE `registration_status_log`  (
  `id` bigint UNSIGNED NOT NULL COMMENT '日志ID',
  `registration_id` bigint UNSIGNED NOT NULL COMMENT '挂号记录ID',
  `from_status` tinyint(1) NULL DEFAULT NULL COMMENT '变更前状态（NULL=初始创建无前序状态）',
  `to_status` tinyint(1) NOT NULL COMMENT '变更后状态',
  `operator_id` bigint UNSIGNED NOT NULL COMMENT '操作人ID (0=系统/患者自助)',
  `operator_role` varchar(19) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作人角色 user/doctor/pharmacist/system',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '变更备注/原因',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记（0-正常，1-删除）',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_registration_id`(`registration_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '挂号状态变更日志' ROW_FORMAT = Dynamic;
