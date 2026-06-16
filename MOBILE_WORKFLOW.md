# 移动端开发协同工作流

## 分支说明

| 分支 | 来源 | 用途 |
|------|------|------|
| `origin/master` | GitHub | 项目主线（含管理端/共享基础设施） |
| `master` | 本地 | 开发工作分支，只含移动端改动 + 必要共享依赖 |
| `mobile-dev` | 本地新建 | 纯净的移动端提分支，不含未提交的管理端改动 |

## 目录职责

| 目录 | 你的职责 | 上传？ |
|------|---------|--------|
| `frontend/mobile/` | **是** - 前端移动端全部代码 | **必须上传** |
| `backend/.../mobile/` | **是** - 后端移动端全部代码 | **必须上传** |
| `agents_service/` | **是** - 智能体服务 | **必须上传** |
| `shared-deps/`（8 个共享文件） | **否，但移动端依赖** | **必须同步**到后端仓库 |

## 工作流

### 一、日常开发（你本地的 master）

```bash
git checkout master
# 已有移动端全部文件已就绪
# 修改 frontend/mobile/ 或 backend/.../mobile/
# ...
git add frontend/mobile/ backend/.../mobile/
git commit -m "your changes"
```

### 二、同步到 mobile-dev（保留干净提交历史）

```bash
# 把所有改动提交到 master 后：
git checkout mobile-dev
git cherry-pick master   # 只把 master 最新的提交同步过来
# 或者：
git checkout mobile-dev
git merge master         # 全量合并（推荐，较简单）
```

### 三、下载 GitHub 最新（只影响非 mobile 文件）

```bash
# 1. 保存你的移动端工作
git add frontend/mobile/ backend/.../mobile/ agents_service/
git stash --keep-index   # 将非移动端文件暂存

# 2. 拉取 GitHub 最新
git fetch origin
git rebase origin/master  # 或 git pull --rebase origin master

# 3. 恢复移动端工作（覆盖为你的版本）
git stash pop

# 4. 恢复 8 个共享依赖（从 mobile-dev 分支提取）
git checkout mobile-dev -- \
  backend/src/main/java/com/xuanyue/exp/common/storage/minio/ \
  backend/src/main/java/com/xuanyue/exp/exp/service/impl/ExpSimulatorServiceImpl.java \
  backend/src/main/java/com/xuanyue/exp/exp/repository/ExpSimulatorRepository.java \
  backend/src/main/java/com/xuanyue/exp/exp/repository/ExpGradeRepository.java \
  backend/src/main/java/com/xuanyue/exp/exp/dto/ExpSimulatorPageQuery.java \
  backend/src/main/resources/application.yml \
  backend/src/main/resources/application-dev.yml
```

### 四、上传到服务器

#### 方式 A：通过 Git（推荐）

```bash
# 服务器上：
git fetch origin
git checkout -b mobile-deploy origin/mobile-dev

# 编译后端
cd backend && mvn package -DskipTests

# 构建前端
cd frontend/mobile && pnpm install && pnpm build

# 部署
# ... 按你们的部署方式复制到对应目录
```

#### 方式 B：通过打包脚本

在本地运行：
```bash
deploy-mobile.bat mobile-dev
```

会在 `deploy-mobile-pkg/` 目录下生成 4 个 zip：
- `mobile-backend.zip` → 后端 `backend/.../mobile/`
- `mobile-frontend.zip` → 前端 `frontend/mobile/`
- `agents-service.zip` → `agents_service/`
- `shared-deps.zip` → 8 个共享文件

#### 方式 C：直接复制目录

直接复制以下 3 个目录到服务器对应位置：
```
frontend/mobile/              → 项目/frontend/mobile/
backend/src/.../mobile/       → 项目/backend/src/.../mobile/
agents_service/               → 项目/agents_service/
```

然后手动合 `mobile-required-patches/shared-deps.patch` 到服务器后端。

## 共享依赖清单（部署时需同步）

| # | 文件 | 改动类型 |
|---|------|----------|
| 1 | `common/storage/minio/MinioStorageService.java` | 新增 `resolveAccessibleUrl()`、`normalizeStorageKey()` |
| 2 | `common/storage/minio/MinioStorageServiceImpl.java` | 实现新接口 + `buildPreviewUrl` 签名变更 |
| 3 | `exp/service/impl/ExpSimulatorServiceImpl.java` | 分页加年级过滤 |
| 4 | `exp/repository/ExpSimulatorRepository.java` | 新增 native query |
| 5 | `exp/repository/ExpGradeRepository.java` | 新增 `findByExpIdIn()` |
| 6 | `exp/dto/ExpSimulatorPageQuery.java` | 新增 `gradeKey` 字段 |
| 7 | `resources/application.yml` | DB 名 `bs_exp`→`bs_exp_vue` |
| 8 | `resources/application-dev.yml` | MinIO/移动端配置 |

patch 文件位置：`mobile-required-patches/shared-deps.patch`
