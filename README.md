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

前置说明：

- 后端最低 Java 基线：`JDK 17+`（Spring Boot 3.3 支持 Java 17+）
- 如果你本机没有可用的 `javac`（例如只装了 JRE，或未配置 `JAVA_HOME`），`code/backend/gradlew.bat` 会自动从 Adoptium 下载 **Temurin JDK 17（ZIP 免安装）** 并缓存到 `code/backend/.gradle/jdks/temurin-17/`（需要联网）。

```bash
cd code/backend
./gradlew bootRun
```

健康检查：

- `GET http://localhost:8080/api/health`

Windows（PowerShell）也可以：

```powershell
cd code/backend
$env:CSZD_DB_PASSWORD="123456"
.\gradlew.bat bootRun
```

如果你在 cmd 里运行，请用：

```bat
cd code\backend
set CSZD_DB_PASSWORD=123456
gradlew.bat bootRun
```

## 数据库初始化

参考 `code/db/README.md`，脚本位于 `code/db/schema.sql`。

后端连接（可按需修改 `code/backend/src/main/resources/application.yml`；默认读取环境变量，不把密码写入仓库）：

- DB：`cszd_db`
- Host：`192.168.3.52:3306`
- 账号：`cszd`
- 密码：通过环境变量注入（PowerShell 示例）：

```powershell
$env:CSZD_DB_PASSWORD="123456"
```

可选：如需覆盖 host/port/db/user，也可设置：

```powershell
$env:CSZD_DB_HOST="192.168.3.52"
$env:CSZD_DB_PORT="3306"
$env:CSZD_DB_NAME="cszd_db"
$env:CSZD_DB_USER="cszd"
```
