-- 充电站状态变更日志表
-- 适用场景：充电站状态切换时记录原状态、新状态、可选备注与操作人账号，
-- 为后续历史变化时间线与日志审计提供数据来源。
-- 执行前请先确认：
-- 1. 已连接目标开发库
-- 2. biz_station 与 sys_dict_item 已存在

USE cszd_db;

CREATE TABLE IF NOT EXISTS biz_station_status_log (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  station_id BIGINT NOT NULL COMMENT '充电站ID；关联 biz_station.id',
  from_status_item_id BIGINT NOT NULL COMMENT '变更前状态字典项ID',
  to_status_item_id BIGINT NOT NULL COMMENT '变更后状态字典项ID',
  remark VARCHAR(500) DEFAULT NULL COMMENT '状态变更备注；可选，最多500字符',
  operator_account VARCHAR(32) DEFAULT NULL COMMENT '操作人登录账号',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_biz_station_status_log_station_id (station_id),
  KEY idx_biz_station_status_log_created_at (created_at),
  CONSTRAINT fk_biz_station_status_log_station_id FOREIGN KEY (station_id) REFERENCES biz_station (id),
  CONSTRAINT fk_biz_station_status_log_from_status_item_id FOREIGN KEY (from_status_item_id) REFERENCES sys_dict_item (id),
  CONSTRAINT fk_biz_station_status_log_to_status_item_id FOREIGN KEY (to_status_item_id) REFERENCES sys_dict_item (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='充电站状态变更日志表';

-- 结构复核
SHOW CREATE TABLE biz_station_status_log;
