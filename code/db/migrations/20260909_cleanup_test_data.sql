-- 清理测试数据脚本
-- 目标：只保留 admin 系统管理员账号和系统字典数据，清理所有业务测试数据
-- 执行前请确保已备份重要数据

USE cszd_db;

-- 禁用外键检查，避免删除顺序问题
SET FOREIGN_KEY_CHECKS = 0;

-- ==========================================
-- 清理业务数据表
-- ==========================================

-- 清理统一操作日志
TRUNCATE TABLE biz_operation_log;

-- 清理股权调整事件
TRUNCATE TABLE biz_equity_adjustment_event;

-- 清理持股关系
TRUNCATE TABLE biz_shareholding;

-- 清理股东主档
TRUNCATE TABLE biz_shareholder;

-- 清理充电桩状态变更日志
TRUNCATE TABLE biz_pile_status_log;

-- 清理充电桩
TRUNCATE TABLE biz_pile;

-- 清理充电站状态变更日志
TRUNCATE TABLE biz_station_status_log;

-- 清理充电站
TRUNCATE TABLE biz_station;

-- ==========================================
-- 清理非 admin 用户
-- ==========================================

-- 删除所有非默认系统管理员的用户（保留 is_default_admin = 1 的 admin 账号）
DELETE FROM sys_user WHERE is_default_admin = 0;

-- ==========================================
-- 保留系统字典数据（不做任何清理）
-- ==========================================
-- sys_dict_type 和 sys_dict_item 保持不变
-- 包含：PLATFORM（平台）、STATION_STATUS（充电站状态）、PILE_STATUS（充电桩状态）等字典数据

-- 启用外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- ==========================================
-- 验证清理结果
-- ==========================================

-- 查看保留的用户（应该只有 admin）
SELECT id, account, name, role_code, user_status, is_default_admin 
FROM sys_user;

-- 查看保留的字典类型
SELECT id, type_code, type_name, type_status 
FROM sys_dict_type 
ORDER BY sort_order;

-- 查看保留的字典项数量
SELECT 
    dt.type_code,
    dt.type_name,
    COUNT(di.id) as item_count
FROM sys_dict_type dt
LEFT JOIN sys_dict_item di ON dt.id = di.type_id
GROUP BY dt.id, dt.type_code, dt.type_name
ORDER BY dt.sort_order;

-- 验证业务表已清空
SELECT 
    '充电站' as table_name, COUNT(*) as count FROM biz_station
UNION ALL
SELECT '充电桩', COUNT(*) FROM biz_pile
UNION ALL
SELECT '股东', COUNT(*) FROM biz_shareholder
UNION ALL
SELECT '持股关系', COUNT(*) FROM biz_shareholding
UNION ALL
SELECT '股权调整事件', COUNT(*) FROM biz_equity_adjustment_event
UNION ALL
SELECT '操作日志', COUNT(*) FROM biz_operation_log;

-- ==========================================
-- 清理完成提示
-- ==========================================
-- 清理完成后，数据库将保留：
-- 1. admin 系统管理员账号（is_default_admin = 1）
-- 2. 所有系统字典类型和字典项（PLATFORM、STATION_STATUS、PILE_STATUS 等）
-- 3. 所有业务数据表已清空，可以开始正式录入生产数据
