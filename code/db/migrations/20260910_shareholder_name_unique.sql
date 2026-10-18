-- 股东名称唯一约束迁移
-- 背景：股东 Excel 批量导入要求「股东名称为业务主键」，只允许新增、不允许修改/删除。
-- 因此在 biz_shareholder.shareholder_name 上建立唯一索引，作为业务主键的数据库兜底。
-- 执行前请先确认：
-- 1. 已连接目标开发库 cszd_db（与既有迁移口径一致）
-- 2. 先人工核对下方「重名排查」结果，确认无业务上需要保留的重名主档

USE cszd_db;

-- 1. 重名排查：列出所有重名股东及对应主键，供人工核对
SELECT shareholder_name, COUNT(*) AS cnt, GROUP_CONCAT(id ORDER BY id) AS ids
FROM biz_shareholder
GROUP BY shareholder_name
HAVING COUNT(*) > 1;

-- 2. 去重：每个重名名称保留最早一条（id 最小），其余记录在原名称后追加 '_<id>' 保证唯一。
--    为什么：股东主档不做物理删除，且持股关系按 id 关联，重命名可保留全部主档与关联关系，
--    仅改变重名主档的名称文本，使唯一索引可落地。
UPDATE biz_shareholder s
JOIN (
  SELECT shareholder_name, MIN(id) AS keep_id
  FROM biz_shareholder
  GROUP BY shareholder_name
  HAVING COUNT(*) > 1
) k ON s.shareholder_name = k.shareholder_name AND s.id <> k.keep_id
SET s.shareholder_name = LEFT(CONCAT(s.shareholder_name, '_', s.id), 128);

-- 3. 建唯一索引
--    为什么：唯一索引是「股东名称为业务主键」的数据库兜底，在导入与手工新增的应用层查重之外，
--    再防并发/漏判下的重名写入。
ALTER TABLE biz_shareholder
  ADD UNIQUE KEY uk_biz_shareholder_shareholder_name (shareholder_name);

-- 结构复核
SHOW INDEX FROM biz_shareholder;
