-- 持股关系表
-- 适用场景：里程碑 E-7 股东详情展示关联充电站列表（本迁移先落地只读链路所需表结构），
-- 维护能力（关联股东、新增持股、占比调整、退出逻辑）由里程碑 F 在同一张表上补齐。
-- 执行前请先确认：
-- 1. 已连接目标开发库（当前开发库为 cszd_dev_db，与 20260904 股东迁移口径一致）
-- 2. 依赖 biz_shareholder / biz_station 两张主档表已存在（前置迁移均已执行）

USE cszd_dev_db;

-- 为什么：持股关系是股东与充电站的中间实体，spec 7.5 已定义字段与状态；
-- 关联两侧只存主键ID（与 biz_pile.station_id 同口径），不做物理外键，主档与关系解耦，
-- 由应用层维护引用一致性。
-- holding_status 两态：ACTIVE 持股中 / EXITED 已退出；退股不物理删除（spec 8.2 删除策略），
-- 同一股东对同一站可能存在"退出后再持股"的历史记录，因此 DB 层不加 (station_id, shareholder_id)
-- 唯一约束，"同一站点同一股东仅一条持股中"由里程碑 F 的维护逻辑在应用层校验。
-- ownership_percentage 用 DECIMAL(5,2)：占比支持 0.01% 精度即可满足"同一站点合计 100%"的组合校验。
-- effective_at 记录生效时间：spec 规定股权调整立即生效，由写入方在保存时取当前时间。
CREATE TABLE IF NOT EXISTS biz_shareholding (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  station_id BIGINT NOT NULL COMMENT '充电站ID；引用 biz_station.id',
  shareholder_id BIGINT NOT NULL COMMENT '股东ID；引用 biz_shareholder.id',
  ownership_percentage DECIMAL(5,2) NOT NULL COMMENT '股份占比；0-100，同一站点所有持股中股东合计须为100',
  holding_status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' COMMENT '持股状态：ACTIVE 持股中 / EXITED 已退出',
  effective_at TIMESTAMP NULL DEFAULT NULL COMMENT '生效时间；股权调整立即生效，保存时记录当前时间',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_biz_shareholding_shareholder_status (shareholder_id, holding_status),
  KEY idx_biz_shareholding_station (station_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='持股关系表';

-- 结构复核
SHOW CREATE TABLE biz_shareholding;