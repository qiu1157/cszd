-- 第一期开发表结构占位脚本
-- 为什么：先把字符集/时区/命名约定落地，后续每个里程碑按 tasks.md 逐步补齐真实表结构

CREATE DATABASE IF NOT EXISTS cszd_db
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE cszd_db;

-- 为什么：避免直接使用 MySQL 保留字 user，账号体系统一落到 sys_user。
-- 第一阶段只定义支持登录、角色、状态和后续改密/重置密码所需的最小字段，
-- 不提前引入最后登录时间、删除标记等当前任务未要求的字段，避免过度设计。
CREATE TABLE IF NOT EXISTS sys_user (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  account VARCHAR(32) NOT NULL COMMENT '登录账号；系统管理员为 admin，业务管理员阶段要求与手机号一致',
  phone VARCHAR(20) DEFAULT NULL COMMENT '手机号；业务管理员必填且唯一，系统管理员可为空',
  password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希',
  name VARCHAR(64) NOT NULL COMMENT '用户名称',
  role_code VARCHAR(32) NOT NULL COMMENT '角色编码：SYSTEM_ADMIN / BUSINESS_ADMIN',
  user_status VARCHAR(16) NOT NULL DEFAULT 'ENABLED' COMMENT '用户状态：ENABLED / DISABLED',
  is_default_admin TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否为默认系统管理员',
  last_login_at TIMESTAMP NULL DEFAULT NULL COMMENT '最后登录时间',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_sys_user_account (account),
  UNIQUE KEY uk_sys_user_phone (phone),
  KEY idx_sys_user_role_code (role_code),
  KEY idx_sys_user_status (user_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统用户表';

-- 为什么：平台、充电站状态、充电桩状态统一收口到系统字典，避免后续各页面维护独立枚举。
-- 第一版仅内置 PLATFORM / STATION_STATUS / PILE_STATUS 三类，页面暂不开放新增字典类型。
CREATE TABLE IF NOT EXISTS sys_dict_type (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  type_code VARCHAR(64) NOT NULL COMMENT '类型编码：PLATFORM / STATION_STATUS / PILE_STATUS',
  type_name VARCHAR(64) NOT NULL COMMENT '类型名称',
  type_status VARCHAR(16) NOT NULL DEFAULT 'ENABLED' COMMENT '类型状态：ENABLED / DISABLED',
  sort_order INT NOT NULL DEFAULT 0 COMMENT '排序',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_sys_dict_type_code (type_code),
  UNIQUE KEY uk_sys_dict_type_name (type_name),
  KEY idx_sys_dict_type_status (type_status),
  KEY idx_sys_dict_type_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统字典类型表';

-- 为什么：字典项统一使用编码、名称、状态、排序四个核心字段，满足后续列表、下拉和筛选复用。
CREATE TABLE IF NOT EXISTS sys_dict_item (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  type_id BIGINT NOT NULL COMMENT '字典类型ID',
  item_code VARCHAR(64) NOT NULL COMMENT '项目编码',
  item_name VARCHAR(64) NOT NULL COMMENT '项目名称',
  item_status VARCHAR(16) NOT NULL DEFAULT 'ENABLED' COMMENT '项目状态：ENABLED / DISABLED',
  sort_order INT NOT NULL DEFAULT 0 COMMENT '排序',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_sys_dict_item_type_code (type_id, item_code),
  UNIQUE KEY uk_sys_dict_item_type_name (type_id, item_name),
  KEY idx_sys_dict_item_status (item_status),
  KEY idx_sys_dict_item_sort_order (sort_order),
  CONSTRAINT fk_sys_dict_item_type_id FOREIGN KEY (type_id) REFERENCES sys_dict_type (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统字典项表';

-- TODO: 里程碑 B 后续任务继续补齐：默认管理员初始化、系统字典页面与接口；
-- TODO: 里程碑 C 起补齐：station/pile/shareholder/... 等业务表。
