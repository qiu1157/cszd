-- 股权调整事件表
-- 适用场景：股权调整独立页面保存成功后，记录调整前后持股快照、占比合计与操作人账号，
-- 为里程碑 G 的历史变化时间线与审计提供数据来源（对照 biz_station_status_log 模式，spec 8.3）。
-- 执行前请先确认：
-- 1. 已连接目标开发库（当前开发库为 cszd_db）
-- 2. biz_station 与 biz_shareholding 已存在

USE cszd_db;

CREATE TABLE IF NOT EXISTS biz_equity_adjustment_event (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  station_id BIGINT NOT NULL COMMENT '充电站ID；引用 biz_station.id',
  before_snapshot TEXT NOT NULL COMMENT '调整前持股快照；JSON数组，元素含持股关系ID/股东ID/股东名称/股份占比',
  after_snapshot TEXT NOT NULL COMMENT '调整后持股快照；JSON数组，结构同调整前快照',
  total_percentage DECIMAL(5,2) NOT NULL COMMENT '调整后持股中占比合计；正常为100.00，非100需保存前二次确认',
  operator_account VARCHAR(32) DEFAULT NULL COMMENT '操作人登录账号',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_biz_equity_adjustment_event_station_id (station_id),
  KEY idx_biz_equity_adjustment_event_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='股权调整事件表';

-- 结构复核
SHOW CREATE TABLE biz_equity_adjustment_event;