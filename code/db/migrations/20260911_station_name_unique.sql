-- 充电站名称唯一约束迁移
-- 背景：股东关系批量导入要求「充电站名称为业务主键」（模板按名称定位站点），
-- 因此在 biz_station.station_name 上建立唯一索引。
-- 执行前请先确认：
-- 1. 已连接目标开发库 cszd_db（与既有迁移口径一致）
-- 2. 先人工核对下方「重名排查」结果，确认无业务上需要保留的重名站点

USE cszd_db;

-- 1. 重名排查：列出所有重名充电站及对应主键，供人工核对
SELECT station_name, COUNT(*) AS cnt, GROUP_CONCAT(id ORDER BY id) AS ids
FROM biz_station
GROUP BY station_name
HAVING COUNT(*) > 1;

-- 2. 去重：每个重名名称保留最早一条（id 最小），其余记录在原名称后追加 '_<id>' 保证唯一。
--    为什么：充电站不做物理删除，重命名保留全部站点与关联数据，仅改变重名站点名称文本。
UPDATE biz_station s
JOIN (
  SELECT station_name, MIN(id) AS keep_id
  FROM biz_station
  GROUP BY station_name
  HAVING COUNT(*) > 1
) k ON s.station_name = k.station_name AND s.id <> k.keep_id
SET s.station_name = LEFT(CONCAT(s.station_name, '_', s.id), 128);

-- 3. 建唯一索引（替换原普通检索索引；唯一索引同时支撑按名称检索）
ALTER TABLE biz_station
  DROP INDEX idx_biz_station_station_name,
  ADD UNIQUE KEY uk_biz_station_station_name (station_name);

-- 结构复核
SHOW INDEX FROM biz_station;
