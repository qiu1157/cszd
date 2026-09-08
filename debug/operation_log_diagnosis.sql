-- 操作日志诊断SQL
-- 用于检查充电站 ST-202608-0001 的操作日志记录

-- 1. 查找充电站的ID
SELECT id, station_code, station_name, address 
FROM biz_station 
WHERE station_code = 'ST-202608-0001';

-- 2. 查询该充电站的所有操作日志(假设充电站ID为某个值,请替换)
-- 替换 <STATION_ID> 为上面查询到的充电站ID
SELECT 
    id,
    operator_account,
    operator_role,
    target_type,
    target_id,
    target_name,
    operation_type,
    before_content,
    after_content,
    remark,
    station_id,
    created_at
FROM biz_operation_log
WHERE station_id = <STATION_ID>
ORDER BY created_at DESC;

-- 3. 查询所有充电站类型的操作日志
SELECT 
    id,
    operator_account,
    target_type,
    target_id,
    target_name,
    operation_type,
    station_id,
    created_at
FROM biz_operation_log
WHERE target_type = 'STATION' AND target_id = 'ST-202608-0001'
ORDER BY created_at DESC;

-- 4. 检查最近的所有操作日志(最近10条)
SELECT 
    id,
    operator_account,
    target_type,
    target_id,
    target_name,
    operation_type,
    station_id,
    created_at
FROM biz_operation_log
ORDER BY created_at DESC
LIMIT 10;

-- 5. 检查表结构
DESCRIBE biz_operation_log;
