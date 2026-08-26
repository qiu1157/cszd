-- 第一期开发表结构占位脚本
-- 为什么：先把字符集/时区/命名约定落地，后续每个里程碑按 tasks.md 逐步补齐真实表结构

CREATE DATABASE IF NOT EXISTS cszd_db
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE cszd_db;

-- TODO: 里程碑 B 起会逐步补齐：user/platform；里程碑 C 起补齐：station/pile/shareholder/... 等
