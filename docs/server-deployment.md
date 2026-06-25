# 科学实验云平台 · 服务器部署指南

> 文档版本：2026-06-23  
> 适用项目：`exp-new1.0`（PC 管理端 + 移动端 + 后端 API + AI 对话服务）  
> 用途：生产/测试环境部署存档与操作指导

---

## 1. 系统架构概览

```
                    ┌─────────────────────────────────────────┐
                    │           用户 / 浏览器 / 手机           │
                    └───────────────┬─────────────────────────┘
                                    │
          ┌─────────────────────────┼─────────────────────────┐
          │                         │                         │
          ▼                         ▼                         ▼
   ┌─────────────┐          ┌─────────────┐          ┌─────────────┐
   │ PC 管理端    │          │ 移动端 H5    │          │ AI 助手      │
   │ Nginx :8080 │          │ /m 或 :8081 │          │ FastAPI :5001│
   └──────┬──────┘          └──────┬──────┘          └──────┬──────┘
          │  /api 反代              │  /api 同源或反代         │
          └─────────────────────────┼─────────────────────────┘
                                    ▼
                          ┌─────────────────┐
                          │ Spring Boot API │
                          │     :8010       │
                          └────────┬────────┘
                                   │
              ┌────────────────────┼────────────────────┐
              ▼                    ▼                    ▼
        ┌──────────┐         ┌──────────┐         ┌──────────┐
        │  MySQL   │         │  MinIO   │         │ 本地文件  │
        │ bs_exp_vue│        │ 对象存储  │         │ uploads  │
        └──────────┘         └──────────┘         └──────────┘
```

### 1.1 组件说明

| 组件 | 技术栈 | 默认端口 | 说明 |
|------|--------|----------|------|
| 后端 API | Spring Boot 2.7 + JPA | **8010** | PC/移动端统一 API；可选托管移动端静态资源 `/m/` |
| PC 管理端 | Vue 3 + Vite + Nginx | **8080** | 管理后台，Nginx 反代 `/api` 到后端 |
| 移动端 H5 | Vue 3 + Vite + Hash 路由 | **8010/m** 或 **8081** | 推荐由后端单端口托管；也可独立 Nginx 容器 |
| AI 对话服务 | Python FastAPI + LangGraph | **5001** | 移动端「AI 助手」；后端通过 `agents-base-url` 调用 |
| MySQL | 5.7+ / 8.0 | 3306 | 业务库，默认库名 **`bs_exp_vue`** |
| MinIO | S3 兼容对象存储 | 9000 | 媒体文件（图片/视频）主存储 |

### 1.2 移动端两种部署方式

| 方案 | 访问地址示例 | 适用场景 |
|------|-------------|----------|
| **A. 单端口（推荐）** | `http://服务器IP:8010/m/#/login` | 手机只需访问 8010；后端 `app.mobile.enabled=true` 托管 `frontend/mobile/dist` |
| **B. 独立 Nginx** | `http://服务器IP:8081/#/login` | Docker profile `mobile-only`；需额外配置 API 反代或 CORS |

---

## 2. 服务器环境要求

### 2.1 硬件建议

| 环境 | CPU | 内存 | 磁盘 |
|------|-----|------|------|
| 测试 | 2 核 | 4 GB | 40 GB |
| 生产 | 4 核+ | 8 GB+ | 100 GB+（含 MinIO 媒体） |

### 2.2 软件依赖

**Docker 部署（推荐）**

- Docker 24+ / Docker Compose v2
- 可访问外网（构建镜像时需拉取 Maven/Node 基础镜像）

**传统部署**

| 软件 | 版本 |
|------|------|
| JDK | 11（与 Dockerfile 一致） |
| Maven | 3.9+ |
| Node.js | 20+ |
| pnpm | 10.33+ |
| Nginx | 1.20+ |
| Python | 3.11+（AI 服务） |

**外部服务（需提前准备）**

- MySQL：创建数据库 `bs_exp_vue`，账号具备 DDL/DML 权限
- MinIO：创建 Bucket（如 `bslab-media-prod`），配置公开读或 CDN 前缀

---

## 3. 数据库初始化

### 3.1 库名与基线

- **业务库名**：`bs_exp_vue`
- **基线表结构**：`sql/deployed/` 目录下各业务表（若已有历史库，跳过全量导入，只做增量）
- **移动端增量表**：见 `sql/mobile/README.md`

### 3.2 新库 / 空库最小执行顺序

```bash
# 1. 创建数据库
mysql -h <MYSQL_HOST> -P <PORT> -u root -p -e "CREATE DATABASE IF NOT EXISTS bs_exp_vue DEFAULT CHARSET utf8mb4;"

# 2. 导入已有业务基线（按实际环境选择 sql/deployed 或现有 dump）
# mysql ... bs_exp_vue < sql/bs_exp_vue.sql   # 若有全量 dump

# 3. 移动端必跑脚本（Tier 1）
mysql -h <HOST> -P <PORT> -u <USER> -p bs_exp_vue < sql/mobile/mobile_required_tables.sql
mysql -h <HOST> -P <PORT> -u <USER> -p bs_exp_vue < sql/mobile/mobile_feature_tables.sql
mysql -h <HOST> -P <PORT> -u <USER> -p bs_exp_vue < sql/mobile/mb_parent_child_audit_alter.sql

# 4. 可选：管理端菜单、索引优化
mysql ... bs_exp_vue < sql/mobile/mobile_sys_menu.sql
mysql ... bs_exp_vue < sql/mobile/home_feed_indexes.sql
```

> **注意**：若库中已有 `mb_parent_child` 等表，**禁止**重复建平行表，只执行 ALTER 类脚本。详见 `docs/mobile-sql-decision.md`。

### 3.3 完整性检查

```bash
cd sql/mobile
# 需配置 JDBC 连接后执行
java check_integrity.java
```

---

## 4. 配置说明

### 4.1 后端核心配置

主配置文件：`backend/src/main/resources/application.yml`  
生产覆盖：`backend/src/main/resources/application-prod.yml`（激活 `--spring.profiles.active=prod`）

| 配置项 | YAML 路径 | 说明 |
|--------|-----------|------|
| 服务端口 | `server.port` | 默认 `8010` |
| 数据库 | `spring.datasource.*` | url / username / password |
| JWT 密钥 | `app.jwt.secret` | **生产必填**，≥32 字符，不得使用开发默认值 |
| MinIO | `app.minio.*` | endpoint、access-key、secret-key、bucket、url-prefix |
| 本地上传 | `app.file.*` | upload-dir、url-prefix（遗留本地文件） |
| 移动端托管 | `app.mobile.enabled` | `true` 时后端提供 `/m/` 静态页 |
| 移动端目录 | `app.mobile.static-dir` | 默认 `../frontend/mobile/dist` |
| AI 服务地址 | `app.mobile.agents-base-url` | 如 `http://agents:5001` |
| 钉钉登录 | `app.dingtalk.*` | 可选 |

**生产环境 JWT 校验**：启动时会执行 `JwtProdSecretValidator`，若 `JWT_SECRET` 未设置或为开发默认值，**服务拒绝启动**。

### 4.2 通过环境变量覆盖（Spring Boot 标准写法）

Docker / systemd 部署时，推荐使用 Spring  relaxed binding，无需改 YAML：

```bash
export SPRING_PROFILES_ACTIVE=prod

export SPRING_DATASOURCE_URL="jdbc:mysql://<HOST>:3306/bs_exp_vue?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai"
export SPRING_DATASOURCE_USERNAME=bslab
export SPRING_DATASOURCE_PASSWORD=<强密码>

export JWT_SECRET="<至少32位随机字符串>"

export APP_MINIO_ENDPOINT=http://<MINIO_HOST>:9000
export APP_MINIO_ACCESS_KEY=<key>
export APP_MINIO_SECRET_KEY=<secret>
export APP_MINIO_BUCKET=bslab-media-prod
export APP_MINIO_URL_PREFIX=https://cdn.example.com/bslab-media-prod

export APP_MOBILE_AGENTS_BASE_URL=http://127.0.0.1:5001

export APP_FILE_UPLOAD_DIR=/data/bslab/uploads
export APP_FILE_URL_PREFIX=https://api.example.com/uploads
```

> 参考模板：`backend/.env.example`（需转换为上述 Spring 变量名或写入 `application-prod.yml`）。

### 4.3 AI 对话服务配置

模板：`agents_service/.env.example`

```env
LLM_API_KEY=sk-xxxxxxxx
LLM_BASE_URL=https://api.deepseek.com/v1
LLM_MODEL=deepseek-chat
DATABASE_URL=mysql+aiomysql://user:pass@<HOST>:3306/bs_exp_vue
HOST=0.0.0.0
PORT=5001
LOG_LEVEL=info
```

### 4.4 前端构建说明

| 项目 | 目录 | 构建命令 | 产物 |
|------|------|----------|------|
| PC 管理端 | `frontend/` | `pnpm install && pnpm build` | `frontend/dist/` |
| 移动端 | `frontend/mobile/` | `pnpm install && pnpm build:lan` | `frontend/mobile/dist/`（base=`/m/`） |

移动端 API 地址逻辑见 `frontend/mobile/src/api/config.js`：后端托管 `/m/` 时自动走同源 `/api`。

---

## 5. 部署方式一：Docker Compose（推荐）

项目根目录已提供 `docker-compose.yml`，包含 `backend`、`frontend-pc`、`agents` 三个核心服务。

### 5.1 准备

```bash
# 1. 克隆代码到服务器
git clone <仓库地址> /opt/bslab
cd /opt/bslab

# 2. 配置密钥（至少 JWT 与 LLM）
cp backend/.env.example .env
# 编辑 .env，设置 JWT_SECRET、LLM_API_KEY 等

# 3. 修改 docker-compose.yml 中的数据库、MinIO 地址
#    （当前 compose 内嵌了开发 IP，上线前必须替换为生产地址）
```

### 5.2 构建并启动

```bash
# 标准三件套：后端 + PC 前端 + AI
docker compose up -d --build

# 若使用独立移动端 Nginx（方案 B）
docker compose --profile mobile-only up -d --build
```

### 5.3 默认端口映射

| 服务 | 宿主机端口 | 容器端口 |
|------|-----------|----------|
| backend | 8010 | 8010 |
| frontend-pc | 8080 | 80 |
| frontend-mobile | 8081 | 80（profile: mobile-only） |
| agents | 5001 | 5001 |

### 5.4 单端口移动端（方案 A，与 compose 配合）

若希望手机只访问 `8010/m/`：

```bash
# 1. 构建移动端静态资源（在宿主机或 CI）
cd frontend/mobile && pnpm install && pnpm build:lan

# 2. 确保 application-prod.yml 中 app.mobile.enabled=true
# 3. 后端容器需能访问 dist 目录（挂载 volume 或在 Dockerfile 中 COPY）
# 4. 重启 backend
docker compose restart backend
```

访问：`http://<服务器IP>:8010/m/#/login`

### 5.5 健康检查

```bash
# 后端
curl http://127.0.0.1:8010/actuator/health

# PC 前端
curl -I http://127.0.0.1:8080/

# AI 服务
curl http://127.0.0.1:5001/health
```

---

## 6. 部署方式二：传统手动部署

适用于无法使用 Docker 或需与现有 Tomcat/Nginx 共存的环境。

### 6.1 后端 JAR

```bash
cd backend
mvn clean package -DskipTests

# 启动（示例）
export SPRING_PROFILES_ACTIVE=prod
export JWT_SECRET="<生产密钥>"
export SPRING_DATASOURCE_URL="jdbc:mysql://..."
export SPRING_DATASOURCE_USERNAME=...
export SPRING_DATASOURCE_PASSWORD=...

java -Xms512m -Xmx1024m -jar target/*.jar
```

建议使用 **systemd** 托管进程，配置 `Restart=always`，日志输出到 `/var/log/bslab/`。

### 6.2 PC 管理端 Nginx

构建后将 `frontend/dist` 部署到 Nginx，参考 `frontend/nginx.conf`：

```nginx
server {
    listen 80;
    server_name admin.example.com;

    root /var/www/bslab-admin;
    index index.html;
    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://127.0.0.1:8010/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

### 6.3 移动端（单端口方案）

```bash
cd frontend/mobile
pnpm install
pnpm build:lan

# 将 dist 放到后端可读路径，或在 application-prod.yml 设置：
# app.mobile.static-dir: /var/www/bslab-mobile/dist
```

访问：`https://api.example.com/m/#/login`（若 Nginx 反代 8010）

### 6.4 AI 对话服务

```bash
cd agents_service
python -m venv .venv
source .venv/bin/activate   # Windows: .venv\Scripts\activate
pip install -r requirements.txt
cp .env.example .env.local
# 编辑 LLM_API_KEY、DATABASE_URL

python main.py
# 或：uvicorn main:app --host 0.0.0.0 --port 5001
```

确保后端 `app.mobile.agents-base-url` 指向该地址。

---

## 7. 生产环境 Nginx 反向代理（HTTPS 示例）

```nginx
# PC 管理端
server {
    listen 443 ssl http2;
    server_name admin.example.com;
    ssl_certificate     /etc/ssl/admin.example.com.crt;
    ssl_certificate_key /etc/ssl/admin.example.com.key;

    root /var/www/bslab-admin;
    location / { try_files $uri $uri/ /index.html; }
    location /api/ {
        proxy_pass http://127.0.0.1:8010/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        client_max_body_size 1024m;
    }
}

# API + 移动端（单端口）
server {
    listen 443 ssl http2;
    server_name m.example.com;
    ssl_certificate     /etc/ssl/m.example.com.crt;
    ssl_certificate_key /etc/ssl/m.example.com.key;

    location /api/ {
        proxy_pass http://127.0.0.1:8010/api/;
        client_max_body_size 1024m;
    }
    location /m/ {
        proxy_pass http://127.0.0.1:8010/m/;
    }
    location / {
        return 302 /m/;
    }
}
```

**钉钉扫码登录**：在 `application-prod.yml` 设置 `app.dingtalk.frontend-origin` 为移动端对外域名（含 `/m` 路径），并在钉钉开放平台登记回调：

`{frontend-origin}/#/settings/dingtalk/callback`

---

## 8. 部署后验证清单

| # | 检查项 | 命令 / 操作 | 预期 |
|---|--------|-------------|------|
| 1 | 后端健康 | `curl :8010/actuator/health` | `{"status":"UP"}` |
| 2 | PC 登录 | 浏览器打开管理端 | 可登录、菜单正常 |
| 3 | 移动端 | 打开 `/m/#/login` | 页面加载、可登录 |
| 4 | 文件上传 | 移动端上传作品图片 | MinIO 可访问、首页可展示 |
| 5 | AI 助手 | 移动端进入「AI助手」发消息 | 有流式回复 |
| 6 | JWT | 使用错误 prod 密钥启动 | 应启动失败（校验生效） |
| 7 | 审核流 | 教师提交实验 → 教研员/管理员审核 | 首页仅展示已通过内容 |

---

## 9. 安全与运维建议

### 9.1 上线前必做

- [ ] 修改所有默认密码（MySQL、MinIO、JWT）
- [ ] `JWT_SECRET` 使用 ≥32 位随机字符串，且与开发环境不同
- [ ] `application.yml` / compose 中**不得**提交真实生产密钥到 Git
- [ ] MinIO Bucket 权限最小化；对外 URL 使用 HTTPS/CDN
- [ ] MySQL 仅内网可达，禁用 root 远程登录
- [ ] 配置防火墙：仅开放 80/443（或指定管理端口）
- [ ] 开启 Nginx / 应用访问日志与磁盘轮转

### 9.2 备份

| 对象 | 建议频率 | 方式 |
|------|----------|------|
| MySQL `bs_exp_vue` | 每日 | mysqldump + 异地存储 |
| MinIO Bucket | 每日 | mc mirror / 对象存储跨区域复制 |
| 配置文件 | 变更时 | 版本化管理（不含密钥） |

### 9.3 日志位置

| 组件 | 默认 |
|------|------|
| Spring Boot | stdout（Docker：`docker compose logs -f backend`） |
| Nginx | `/var/log/nginx/` |
| agents_service | stdout |

生产建议 `logging.level.com.xuanyue.exp=info`，关闭 Hibernate SQL（`application-prod.yml` 已配置）。

---

## 10. 常见问题

### Q1：生产启动报 JWT_SECRET 错误

```
生产环境 JWT_SECRET 不得使用开发默认值，且长度不少于 32 字符
```

**处理**：设置环境变量 `JWT_SECRET` 为新的随机字符串（≥32 字符），重启后端。

### Q2：移动端白屏或 API 404

- 确认已执行 `pnpm build:lan`，且 `dist/index.html` 存在
- 单端口方案：访问地址必须带 `/m/`，如 `http://IP:8010/m/#/login`
- 检查后端日志是否有「移动端静态目录不存在」警告

### Q3：图片/视频无法显示

- 检查 MinIO `endpoint`、`url-prefix` 是否可从浏览器访问
- 确认 Bucket 策略或 CDN 配置正确
- 后端 `app.minio.url-prefix` 需与对外访问地址一致

### Q4：AI 助手无响应

```bash
curl http://127.0.0.1:5001/health
```

- 确认 `LLM_API_KEY` 有效
- 确认后端 `app.mobile.agents-base-url` 可达 agents 容器/进程
- Docker 内网可使用 `http://agents:5001`

### Q5：docker-compose 中的 DB_URL 不生效

当前 `docker-compose.yml` 部分变量名（如 `DB_URL`）**未自动映射**到 Spring 配置。请使用本文 **§4.2** 的 `SPRING_DATASOURCE_*` 变量，或直接修改 `application-prod.yml` 后重新构建镜像。

### Q6：数据库脚本报错「表已存在」

说明库非空库，跳过 `CREATE TABLE` 脚本，只执行对应 **ALTER** 增量脚本，参见 `sql/mobile/README.md`。

---

## 11. 目录与文档索引

| 路径 | 说明 |
|------|------|
| `docker-compose.yml` | Docker 一键编排 |
| `backend/Dockerfile` | 后端镜像构建 |
| `backend/.env.example` | 后端环境变量参考 |
| `backend/src/main/resources/application-prod.yml` | 生产 Profile |
| `frontend/Dockerfile` | PC 前端镜像 |
| `frontend/mobile/Dockerfile` | 移动端镜像 |
| `agents_service/` | AI 对话服务 |
| `sql/mobile/README.md` | 移动端 SQL 脚本说明 |
| `docs/mobile-sql-decision.md` | 表结构决策文档 |
| `docs/mobile-product-spec.md` | 产品规则与审核流程 |

---

## 12. 快速命令备忘

```bash
# ── Docker ──
docker compose up -d --build
docker compose logs -f backend
docker compose restart backend

# ── 本地构建 ──
cd backend && mvn clean package -DskipTests
cd frontend && pnpm install && pnpm build
cd frontend/mobile && pnpm install && pnpm build:lan

# ── 健康检查 ──
curl http://127.0.0.1:8010/actuator/health
curl http://127.0.0.1:5001/health

# ── 数据库（最小移动端表） ──
mysql ... bs_exp_vue < sql/mobile/mobile_required_tables.sql
mysql ... bs_exp_vue < sql/mobile/mobile_feature_tables.sql
mysql ... bs_exp_vue < sql/mobile/mb_parent_child_audit_alter.sql
```

---

*本文档随代码仓库维护；配置项变更时请同步更新 `backend/.env.example` 与本文件。*
