-- 统一操作日志表
-- 适用场景：记录所有关键业务操作与系统管理操作，为里程碑 G 的时间线与全局日志页提供统一数据源。
-- 执行前请先确认：
-- 1. 已连接目标开发库（当前开发库为 cszd_dev_db）
-- 2. 数据库字符集为 utf8mb4

USE cszd_dev_db;

CREATE TABLE IF NOT EXISTS biz_operation_log (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  operator_account VARCHAR(32) DEFAULT NULL COMMENT '操作人登录账号',
  operator_role VARCHAR(32) DEFAULT NULL COMMENT '操作人角色：SYSTEM_ADMIN / BUSINESS_ADMIN',
  target_type VARCHAR(32) NOT NULL COMMENT '对象类型：STATION 充电站 / PILE 充电桩 / SHAREHOLDER 股东 / SHAREHOLDING 持股关系 / SYSTEM_DICT 系统字典 / USER 用户',
  target_id VARCHAR(64) DEFAULT NULL COMMENT '对象标识；业务对象编号或主键ID字符串',
  target_name VARCHAR(128) DEFAULT NULL COMMENT '对象名称；如充电站名称、股东名称，用于快速展示',
  operation_type VARCHAR(32) NOT NULL COMMENT '操作类型：CREATE 新增 / UPDATE 修改 / STATUS_CHANGE 状态变更 / ENABLE 启用 / DISABLE 停用 / RESET_PASSWORD 重置密码 / EQUITY_ADJUST 股权调整 / EXIT 退出',
  before_content TEXT DEFAULT NULL COMMENT '修改前内容；JSON或文本摘要',
  after_content TEXT DEFAULT NULL COMMENT '修改后内容；JSON或文本摘要',
  remark VARCHAR(500) DEFAULT NULL COMMENT '备注；如状态变更原因',
  station_id BIGINT DEFAULT NULL COMMENT '关联充电站ID；用于按充电站筛选，非站点操作可为空',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_biz_operation_log_created_at (created_at),
  KEY idx_biz_operation_log_operator_account (operator_account),
  KEY idx_biz_operation_log_target_type (target_type),
  KEY idx_biz_operation_log_station_id (station_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='统一操作日志表';

-- 结构复核
SHOW CREATE TABLE biz_operation_log;
