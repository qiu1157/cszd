# 数据库初始化（第一期占位）

## 基本约定

- 数据库：MySQL 8.x（建议）
- 字符集：`utf8mb4`
- 排序规则：`utf8mb4_0900_ai_ci`（或与线上一致）
- 时区：统一按北京时间（应用侧 JDBC `serverTimezone=Asia/Shanghai`）

## 命名规则

- 表：`snake_case`，如 `charging_station`
- 主键：`id`（自增或雪花，第一期先占位）
- 业务唯一键：按业务规则设计（如站点系统ID、桩编号等）
- 通用时间字段：`created_at` / `updated_at`

## 初始化方式（占位）

1. 创建数据库
2. 执行 `schema.sql`
3. 按需创建本地账号与授权

> 注意：本机目前未检测到 MySQL CLI，可先安装 MySQL 或用你习惯的客户端执行脚本。

## 当前环境连接信息（由你提供）

- ip：`192.168.3.52`
- port：`3306`
- db：`cszd_db`
- user：`cdzd`
- password：请通过环境变量 `CSZD_DB_PASSWORD` 注入，不写入仓库
