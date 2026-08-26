# 充电站管理系统（第一期）

第一期目标：完成充电站、充电桩、股东与持股关系的后台管理闭环，并具备日志审计与时间线可视化能力。

## 目录说明

- `doc/`：需求、任务与验收文档（开发必须严格按 `doc/tasks.md` 顺序推进）
- `prototype/`：第一期 UI/流程基准  
  - 强基准文件：`prototype/charging-station-system-prototype.html`
- `code/`
  - `frontend/`：前端（Vue 3 + Element Plus）
  - `backend/`：后端（Spring Boot + MySQL）
  - `db/`：数据库初始化说明与脚本

## 强约束（务必遵守）

- UI/交互：以 `prototype/charging-station-system-prototype.html` 为第一期基准
- 前端规范：`doc/前端开发规范和要求.md` 为强约束
- 开发流程：严格按 `doc/spec.md` → `doc/tasks.md` → `doc/checklist.md`
- 每完成一个 task 必须执行一次 Git commit（Conventional Commits）

## 前端启动

```bash
cd code/frontend
npm install
npm run dev
```

常用命令：

```bash
npm run lint
npm run format
```

## 后端启动

> 当前环境未检测到 Maven 命令（`mvn`），如本机未安装 Maven，请先安装后再启动。

```bash
cd code/backend
mvn spring-boot:run
```

健康检查：

- `GET http://localhost:8080/api/health`

## 数据库初始化

参考 `code/db/README.md`，脚本位于 `code/db/schema.sql`。

后端默认连接（可按需修改 `code/backend/src/main/resources/application.yml`）：

- DB：`charging_station`
- URL：`jdbc:mysql://localhost:3306/charging_station?serverTimezone=Asia/Shanghai`

