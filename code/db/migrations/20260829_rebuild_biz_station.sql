-- 充电站表命名规范重建脚本
-- 适用场景：旧表 charging_station 仅为开发阶段试建表，尚无正式业务数据
-- 执行前请先确认：
-- 1. 已连接 cszd_db
-- 2. charging_station 若存在，数据量为 0 或数据可丢弃

USE cszd_db;

-- 第一步：人工确认旧表状态
SHOW TABLES LIKE 'charging_station';
SHOW TABLES LIKE 'biz_station';
SELECT COUNT(*) AS charging_station_count FROM charging_station;

-- 第二步：确认旧表无有效数据后，删除错误命名的旧表
DROP TABLE IF EXISTS charging_station;

-- 第三步：如之前存在半成品新表，也一并删除后按统一规范重建
DROP TABLE IF EXISTS biz_station;

-- 第四步：按 sys_ / biz_ 分层规范重建充电站表
CREATE TABLE biz_station (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  station_code VARCHAR(32) NOT NULL COMMENT '充电站编号；充电站编号由系统自动生成，且全局唯一',
  station_name VARCHAR(128) NOT NULL COMMENT '充电站名称',
  platform_item_id BIGINT NOT NULL COMMENT '所在平台字典项ID；关联 PLATFORM 类型字典项',
  platform_station_code VARCHAR(64) DEFAULT NULL COMMENT '平台站点编号；人工录入，可为空，平台站点编号在同一平台内唯一',
  address VARCHAR(255) DEFAULT NULL COMMENT '地址',
  contact_name VARCHAR(64) DEFAULT NULL COMMENT '联系人',
  contact_phone VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
  station_status_item_id BIGINT NOT NULL COMMENT '生命周期状态字典项ID；单一状态，默认指向签合同',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_biz_station_station_code (station_code),
  UNIQUE KEY uk_biz_station_platform_station_code (platform_item_id, platform_station_code),
  KEY idx_biz_station_station_name (station_name),
  KEY idx_biz_station_platform_item_id (platform_item_id),
  KEY idx_biz_station_station_status_item_id (station_status_item_id),
  CONSTRAINT fk_biz_station_platform_item_id FOREIGN KEY (platform_item_id) REFERENCES sys_dict_item (id),
  CONSTRAINT fk_biz_station_station_status_item_id FOREIGN KEY (station_status_item_id) REFERENCES sys_dict_item (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='充电站表';

-- 第五步：结构复核
SHOW CREATE TABLE biz_station;
