-- 执行数据库迁移：添加 meter_account_number 字段
USE charging_station_system;

-- 检查字段是否已存在
SELECT COUNT(*) as field_exists 
FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = 'charging_station_system' 
  AND TABLE_NAME = 'station' 
  AND COLUMN_NAME = 'meter_account_number';

-- 如果字段不存在，则添加
ALTER TABLE station 
ADD COLUMN IF NOT EXISTS meter_account_number VARCHAR(64) DEFAULT NULL 
COMMENT '电表户号；用于匹配电费发票，记录每月电费情况' 
AFTER contact_phone;

-- 验证字段已添加
DESCRIBE station;
