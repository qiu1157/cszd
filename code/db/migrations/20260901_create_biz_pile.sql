-- 充电桩表
-- 适用场景：里程碑 D 充电桩管理的桩级数据落地。
-- 执行前请先确认：
-- 1. 已连接 cszd_db
-- 2. biz_station 与 sys_dict_item 已存在

USE cszd_db;

-- 为什么：充电桩为业务域实体，按 biz_ 前缀规范落到 biz_pile。
-- 当前管理粒度为桩级（不管理单枪明细），充电枪数量以 gun_count 记录在桩上；
-- 未来扩展到枪级时，通过新建 biz_gun 表以 pile_id 关联扩展，不在本期提前建表。
-- pile_code 为桩编号；桩编号由系统自动生成，且全局唯一（格式与充电站编号一致：前缀-年月-序号）。
-- charge_type 表示充电类型，第一期范围为快充 / 慢充，由应用层枚举校验，不引入字典。
-- pile_status_item_id 表示当前单一生命周期状态，第一期范围为：运营中 / 运营异常 / 停用，
-- 新增充电桩时默认应落到“运营中”对应的启用字典项。
-- gun_count 表示充电枪数量，业务上至少为 1，由应用层在新增/编辑时校验。
CREATE TABLE IF NOT EXISTS biz_pile (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  pile_code VARCHAR(32) NOT NULL COMMENT '桩编号；桩编号由系统自动生成，且全局唯一',
  station_id BIGINT NOT NULL COMMENT '所属充电站ID；关联 biz_station.id',
  charge_type VARCHAR(16) NOT NULL COMMENT '充电类型：FAST 快充 / SLOW 慢充',
  gun_count INT NOT NULL COMMENT '充电枪数量；至少为1，由应用层校验',
  pile_status_item_id BIGINT NOT NULL COMMENT '生命周期状态字典项ID；单一状态，默认指向运营中',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_biz_pile_pile_code (pile_code),
  KEY idx_biz_pile_station_id (station_id),
  KEY idx_biz_pile_pile_status_item_id (pile_status_item_id),
  CONSTRAINT fk_biz_pile_station_id FOREIGN KEY (station_id) REFERENCES biz_station (id),
  CONSTRAINT fk_biz_pile_pile_status_item_id FOREIGN KEY (pile_status_item_id) REFERENCES sys_dict_item (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='充电桩表';

-- 结构复核
SHOW CREATE TABLE biz_pile;
