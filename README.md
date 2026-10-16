# 充电站管理系统（第一期）

面向内部管理人员、公网部署的充电站管理后台，统一维护充电站、充电桩、股东与持股关系，支持股权调整、时间线可视化与全局操作审计。PC 与手机自适应访问。

## 技术栈

- 前端：Vue 3 + TypeScript + Element Plus + Pinia + Vue Router + Vite
- 后端：Spring Boot 3.3 + Java 17 + Spring Data JPA
- 数据库：MySQL 8

## 目录说明

| 目录 | 说明 |
|------|------|
| `doc/` | 项目文档（需求 / 设计 / 开发 Task / 部署 / 测试 / 归档），入口见 [`doc/README.md`](doc/README.md) |
| `prototype/` | 第一期 UI/流程基准，强基准文件 `prototype/charging-station-system-prototype.html` |
| `code/frontend/` | 前端工程（Vue 3 + Element Plus） |
| `code/backend/` | 后端工程（Spring Boot + MySQL） |
| `code/db/` | 数据库初始化脚本与说明 |

## 文档导航

- 需求：`doc/requirements/需求说明书.md`
- 设计：`doc/design/系统规格说明.md`（权威规格）、`doc/design/前端开发规范.md`
- 开发：`doc/development/任务拆解.md`、`doc/development/验收清单.md`、`doc/development/开发流程与规范.md`
- 部署：`doc/deployment/部署指南.md`
- 测试报告：`doc/testing/`
- 历史归档：`doc/archive/`

## 强约束

- UI/交互以 `prototype/charging-station-system-prototype.html` 为第一期基准。
- 开发流程严格按 `doc/design/系统规格说明.md` → `doc/development/任务拆解.md` → `doc/development/验收清单.md`。
- 前端规范 `doc/design/前端开发规范.md` 为强约束。
- 每完成一个 task 执行一次 conventional commit。
- 仓库公开托管，禁止提交真实密码、密钥、Token、公网地址与业务/个人隐私数据。

## 快速开始

完整的环境要求、配置注入、数据库初始化与前后端启动步骤见 [`doc/deployment/部署指南.md`](doc/deployment/部署指南.md)。

```bash
# 前端
cd code/frontend && npm install && npm run dev

# 后端（需先注入数据库连接环境变量）
cd code/backend && ./gradlew bootRun

# 健康检查
curl http://localhost:8080/api/health
```

## 安全说明

- 数据库连接、默认管理员初始密码、开发期业务账号均通过环境变量注入，真实值不入库（见 `.env.example`）。
- 生产环境必须覆盖默认管理员初始密码与数据库连接信息。
