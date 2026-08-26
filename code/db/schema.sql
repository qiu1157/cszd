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

-- TODO: 里程碑 B 后续任务继续补齐：默认管理员初始化、平台字典；
-- TODO: 里程碑 C 起补齐：station/pile/shareholder/... 等业务表。
