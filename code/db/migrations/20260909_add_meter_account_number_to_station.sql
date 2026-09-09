-- 为充电站表添加电表户号字段
-- 用途：记录充电站的电表户号，用于后续匹配电费发票，记录每月电费情况
-- 执行日期：2026-09-09

USE cszd_db;

ALTER TABLE biz_station
ADD COLUMN meter_account_number VARCHAR(64) DEFAULT NULL COMMENT '电表户号；用于匹配电费发票，记录每月电费情况' AFTER contact_phone;
