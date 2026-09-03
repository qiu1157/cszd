-- 股东主档表
-- 适用场景：里程碑 E 股东主档管理的股东级数据落地。
-- 执行前请先确认：
-- 1. 已连接目标开发库（当前开发库为 cszd_dev_db，与 20260901 迁移口径一致）
-- 2. 该表为独立主档实体，不依赖其他业务表，可直接执行

USE cszd_dev_db;

-- 为什么：股东为业务域实体，按 biz_ 前缀规范落到 biz_shareholder。
-- 股东是独立主档实体，与充电站解耦：充电站是否归属某股东由后续里程碑的持股关系表表达，
-- 主档本身不持有 station_id，避免一对多关系写死在主档侧，从而支持同一股东跨站复用。
-- shareholder_name 股东名称为必填文本，需求未要求唯一性，先以普通索引支撑列表页按名称搜索；
-- 联系人与联系电话为可选录入项，第一期不参与唯一性校验。
-- shareholder_status 仅有启用/停用两态（退股不属于主档状态，由后续股权调整记录表达），
-- 参照 sys_user 的实现方式，用应用层枚举校验，不引入字典项。
-- 第一期不做物理删除，股东通过停用状态管理（与用户管理删除策略一致）。
CREATE TABLE IF NOT EXISTS biz_shareholder (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  shareholder_name VARCHAR(128) NOT NULL COMMENT '股东名称；必填，不做唯一约束，支持按名称检索',
  contact_name VARCHAR(64) DEFAULT NULL COMMENT '联系人；可选',
  contact_phone VARCHAR(20) DEFAULT NULL COMMENT '联系电话；可选',
  shareholder_status VARCHAR(16) NOT NULL DEFAULT 'ENABLED' COMMENT '股东状态：ENABLED 启用 / DISABLED 停用',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_biz_shareholder_shareholder_name (shareholder_name),
  KEY idx_biz_shareholder_shareholder_status (shareholder_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='股东主档表';

-- 结构复核
SHOW CREATE TABLE biz_shareholder;