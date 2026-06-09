# 去 Mock 执行计划（基于真实 sys_user）

> 更新：2026-06-05 · **状态：📦 归档（2026-06-08）**  
> 不再维护 Phase checklist；当前入口见 **[README.md](./README.md)**。产品规格 → [mobile-product-spec.md](./mobile-product-spec.md)

---

## 1. 当前状态摘要

### 1.1 数据库

| 项 | 状态 |
|----|------|
| Tier 1 七表 + Tier 1.5 四表 | ✅ 结构完整 |
| `mb_*` 业务数据 | ✅ 已 truncate，**0 行**，无 `demo-*` user_id |
| audit / soft-delete 字段 | ✅ 已执行 alter |
| `sys_user` | ✅ 6 个内建账号（无需再建种子用户） |
| 禁止表 | ✅ 未创建 |

### 1.2 内建账号（`bs_exp_vue.sys_user`）

| login_name | user_id | user_role_id | 移动端用途 |
|------------|---------|--------------|------------|
| `zhangxm` | `a6ef4dd7298d4914ae58d2dccc136ae8` | **Student** | 学生端主测试账号 |
| `gaoy` | `9a77dde99aaa4b689bd69ddfdedb6cb0` | **Teacher** | 教师端（布置/批阅） |
| `liangwy` | `220f1898089145dd92e0c0f351e9d9bb` | **Teacher** | 教师端备选 |
| `bhy_admin` | `0601ff00334343d49e116055fb3be56a` | School_Admin | 管理端，非移动端主路径 |
| `admin` | `Sys_Admin` | Sys_Admin | 系统管理 |
| `yuanf` | `ab786f33c05b4df198079458365688df` | Researcher | 教研员 |

**注意**：内建 6 账号用于学生/教师联调；**家长账号不在 seed 中**，由 `POST /mobile/auth/parent/register` 现场创建。

### 1.3 Mock 仍存在的三层

```
mobile_demo_seed.sql     demo-* user_id（与 sys_user 脱节）
        ↓
MobilePrototypeData.java  空表/404 时整包替换
MobileUserContext         查无数据时回退 demo-student-001
        ↓
prototypeFallbacks.js     页面 onMounted 主路径仍读 PROTOTYPE
```

**结论**：即使用真实账号登录，页面仍可能显示 mock，因为 (1) mb 数据不属于当前 user_id；(2) 后端主动 fallback；(3) 前端默认 PROTOTYPE。

---

## 2. 总体策略

| 原则 | 做法 |
|------|------|
| **不建种子用户** | 学生/教师用内建账号；**家长仅通过注册 API 写入 sys_user** |
| **单一真相源** | 运行时只信 DB + 已有 `exp_*` / `sys_*` |
| **空即空** | 无数据返回 `[]` / 空态 UI，不回退 Java/JS mock |
| **一次性数据迁移** | 可选：把现有 mb 演示行 **改绑** 到 `zhangxm` / `gaoy`（见 §3），或 **清空 mb 业务表** 从零走 App |

---

## 3. Step 0 · SQL + 测试数据（先做，约 0.5 天）

**推荐（当前环境）**：功能表 + audit alter 已就绪；**清空 demo 业务数据**已完成。

```bash
# 若新环境尚未执行：
mysql ... bs_exp_vue < sql/mobile/mobile_required_tables.sql
mysql ... bs_exp_vue < sql/mobile/mobile_feature_tables.sql
mysql ... bs_exp_vue < sql/mobile/mb_parent_child_audit_alter.sql
mysql ... bs_exp_vue < sql/mobile/truncate_mb_demo_data.sql
mysql ... bs_exp_vue < sql/mobile/mobile_sys_menu.sql   # 仅菜单，不含角色授权
```

**角色授权**：在管理后台 **角色管理** 为 `School_Admin` / `Sys_Admin` 勾选移动端菜单（**不写 SQL**）。

按 **[mobile-test-data-playbook.md](./mobile-test-data-playbook.md)** 在管理端发布实验、维护模拟器/题库，再用 `gaoy`/`zhangxm` 移动端产生任务/作品。

**不再执行**：`mobile_demo_seed.sql`、`mobile_optional_demo_seed.sql`、`mobile_badge_def_seed.sql`（生产；dev 可选）

---

## 4. 分步执行计划

> **前置**：阅读 [mobile-product-spec.md](./mobile-product-spec.md)；**R0 管理端未完成前不启动 R2 家长链路**。

### Phase R0 · 管理端（2–3 天）— **9.2 已批准，可开工**

| # | 任务 | 说明 |
|---|------|------|
| R0-0 | SQL | `mobile_feature_tables.sql` + `mb_parent_child_audit_alter.sql` +（已有 comment 表时）`mb_comment_soft_delete_alter.sql` |
| R0-1 | **家长角色审核** API + 页 | `sys_user` Parent + `status=t` → y/n；菜单 `mobile-parent-audit` |
| R0-2 | **绑定审核** API + 页 | pending→approved/rejected；写 confirm_* / reject_reason |
| R0-3 | **勋章规则** CRUD + **手动授予** | `POST .../admin/badges/grant` |
| R0-4 | **评论管理**（仅 Sys_Admin） | 列表含 status=n；PATCH 软删 + deleted_by/time |
| R0-5 | **`mobile_sys_menu.sql`** | 仅注册菜单；**角色授权在管理后台「角色管理」完成** |

后端均在 `com.xuanyue.exp.mobile.admin` **新包**；管理端 **新建** Vue 页面（不修改已有 View 业务逻辑）。

### R0 分步验收（每步完成后暂停，等你确认再进下一步）

| 步骤 | 交付 | 你怎么验 |
|------|------|----------|
| **R0-1** | 菜单 SQL + 家长角色审核 API + 管理页 | 执行 `mobile_sys_menu.sql` → 角色管理勾选菜单 → 执行 `dev_r0_parent_pending_seed.sql` → `admin` 打开「家长注册审核」→ 通过/驳回 |
| **R0-2** | 绑定审核 API + 页 | 手工插入 pending 绑定 → 校管审核 → 字段 confirm_* 写入 |
| **R0-3** | 勋章 CRUD + 手动授予 | 新建勋章 → grant 给 zhangxm → DB 有 progress |
| **R0-4** | 评论管理 | Sys_Admin 软删评论 → 移动端不可见、后台可见 |

---

### Phase A · 后端断 Mock 根（2–3 天）

| # | 任务 | 文件/范围 | 验收 |
|---|------|-----------|------|
| A1 | 配置开关 `mobile.demo-fallback-enabled: false`（dev 默认可 false） | `application-dev.yml`, `MobileWebProperties` | 关后 API 不返回 prototype 数据 |
| A2 | **删除 `MobileUserContext` 的 DEMO_* 回退**；空 userId 直接 401 | `MobileUserContext.java` | 登录用户只查本人数据 |
| A3 | `MobileTaskService` / `MobileWorkService`：空表 → 空列表；404 → null，**不调** `prototypeData` | 两 Service | `zhangxm` 登录只见改绑后的任务/作品 |
| A4 | `MobileLearningService`：badges **读 feature 表**；growth 可聚合；quiz 接 exp_question | LearningService | 有 badge_def 即展示列表 |
| A5 | **每日一题**：读 `mb_quiz_daily` + `exp_question`/`exp_question_select`；删除 `QUIZ_CORRECT_ANSWERS` | LearningService + 只读 inject `ExpQuestion*` | 答题全链路无 Java mock |
| A6 | `MobileHomeService.getParentDashboard`：去掉 prototype 实验/动态填充；无绑定 → 空 dashboard | HomeService | 家长端空态（直至有 Parent 用户） |
| A7 | `MobileParentService`：查真实 Student；`bind_status=pending`（**禁止 auto-approve**） | ParentService | 待 R0-2 管理端审核 |
| A8 | **`POST /mobile/auth/parent/register`**：`login_name`=手机号；`status=t`；**无短信、不返回 token** | 新 Controller | 注册后提示待审核 |
| A8b | **`POST /mobile/auth/login`** 包装：status=t/n 友好错误；y 时等同现有 login | mobile 包 | 待审核不可登录 |
| A9 | `LoginView`：**使用服务端 `userRoleId`**，Tab 仅预填 | 前端 `LoginView.vue` | 家长注册后登录进家长首页 |

**Phase A 完成后**：`zhangxm` / `gaoy` 核心 API 无 mock；**新注册家长**可登录（dashboard 仍待 Phase B/C）。

---

### Phase B · 补全读 API（2–3 天）

| # | 任务 | 替换的前端 Mock |
|---|------|----------------|
| B1 | `GET /works/{id}` 返回 `mb_work_file` 附件 | UploadView / WorkDetail 媒体区 |
| B2 | **统一 Social API** → `mb_comment` + `mb_user_reaction`（exp_msg / exp_simulator / work） | ContentDetail、WorkDetail、VirtualExpDetail |
| B2b | **`MobileBadgeGrantService`**：works/quiz/task 提交后更新 `mb_badge_progress` | BadgeWall 真实 earned |
| B3 | `GET /teacher/reviews` + `POST /teacher/grade` | `reviewPending` |
| B4 | `GET /org/bind-options` → `sys_org` 树 | `PROTOTYPE.bind`、注册页选学校 |
| B4b | `GET /parent/students/search?orgId&name&studentNo` | 绑定页查孩子 |
| B5 | Profile / browse-stats **SQL 聚合**（work 数、task 数、per_score） | `profile.*.stats` |
| B6 | 教师看板 `GET /teacher/board` 聚合 `mb_task_submission` | ProgressBoardView 硬编码 |
| B7 | `getParentDashboard`：**仅 approved 绑定**；实验/动态 SQL 聚合，**零 prototype 填充** | `parentDashboard.*` |

---

### Phase C · 前端去 Mock 主路径（2 天）

| 页面 | 改动 |
|------|------|
| `TasksView` / `TaskDetailView` | 去掉 `PROTOTYPE.tasksDemo` 默认；仅 `catch` 网络错误 |
| `WorksView` / `WorkDetailView` | 同上 + 评论接 API |
| `QuizView` / `QuizHistoryView` | 初始 `[]`，只信 API |
| `BadgeWallView` / `GrowthArchiveView` | 读 API；无 earned 显示未获得列表 |
| `ContentDetailView` / `VirtualExpDetailView` | 接 B2 社交 API |
| `HomeTeacher` / `ProfileView` | 统计为 0 显示 0，不用 `withPrototypeStat` |
| `ReviewView` | 接 B3 |
| `BindChildView` | 接 B4 + B4b + bind API |
| ParentRegisterView | 接 register API；无验证码；用户名占用校验 + 组织级联 + 学生查询 |
| `HomeParent` / `ParentDashboardBody` | 空态 / pending / approved 三态；去掉 PROTOTYPE |
| `UploadView` | 真实选文件 + `/api/files/upload` |
| `SettingsView` | 去掉 `PROTOTYPE.device/app/teacher` 展示块或改静态文案 |

**Phase D · 语音 + 教研员 + 治理（1–2 天）**

| # | 任务 | 说明 |
|---|------|------|
| D1 | `VoiceSearchView` + Assistant 语音：**Web Speech API** | 不支持时降级文字搜索 |
| D2 | **`POST /mobile/asr/transcribe`** — **阿里云 NLS**（推荐），可切换讯飞 | 见 product-spec §6 |
| D3 | `app.js` 增加 **RESEARCHER_TABS**；`yuanf` 登录识别 | 教研点评 `user_role_tag=researcher` |
| D4 | 删除/隔离 `MobilePrototypeData`；`offlineFallbacks` | grep 验收 |

~~**保留 mock**~~：`VoiceSearchView` demo 词在 D1 后移除；`TeacherAiView` 可接 chat 或标演示。

---

### Phase E · 删除后端 Mock 组件（0.5 天，可并入 D4）

- 标记 deprecated：`mobile_demo_seed.sql`、`mobile_optional_demo_seed.sql`
- 文档：`mobile-sql-decision.md`、`mobile-test-data-playbook.md`

---

## 5. 推荐执行顺序（时间线）

```
Week 1
  Day 1   Step 0 SQL 改绑 + collation 备忘
  Day 2–3 Phase A（A1–A7，含每日一题接 exp_question）
  Day 4–5 Phase B1–B3 + **B7 家长 dashboard**（可与 A8 并行）

Week 2
  Day 1   Phase A8–A9 + C 家长页（register/bind/home）
  Day 2   Phase B4–B6 + Phase C 核心页（tasks/works/quiz/profile）
  Day 3   Phase C 其余页 + Phase D 删除 MobilePrototypeData
  Day 4   全链路验收：zhangxm / gaoy 登录，关闭 fallback，无 PROTOTYPE 主路径
```

---

## 6. 验收标准（「彻底消除 mock」）

1. **`mobile.demo-fallback-enabled=false`** 且 **`zhangxm` 登录**：
   - 任务/作品/答题/勋章/成长均来自 DB 或空态，**不出现**「张小明/demo-student」除非就是该用户姓名。
2. 后端日志/响应中 **无** `MobilePrototypeData` 调用。
3. 前端 Network 面板：页面首屏数据 **均来自 `/api/mobile/*` 或 `/api/*`**，非 `prototypeFallbacks` 初始化。
4. **`sys_user` 无 demo-* 账号**；mb 表 user_id 均为真实 UUID；**家长 user 仅来自注册 API**
5. 新产生的任务/作品/答题/绑定，刷新后仍在库中且归属当前登录 user
6. **新注册家长** E2E：register → login → bind → pending/approved 态正确，无 prototype 主数据

---

## 7. 风险与依赖

| 风险 | 缓解 |
|------|------|
| 绑定审核无管理端 UI | dev `mobile.parent-bind-auto-approve=true`；生产暂由 DBA/管理端改 `mb_parent_child.bind_status` |
| 短信网关未接 | `mobile.sms-enabled=false`，注册仅校验手机号唯一与格式 |
| 登录 Tab 与真实角色不一致 | A9 强制用服务端 `userRoleId` |
| `exp_msg.exp_id` 与 task.video_id 不一致 | Phase 2 任务详情关联真实实验 |
| Collation 导致 quiz JOIN 失败 | A5 中 SQL 加 COLLATE 或 ALTER mb 表 |
| 清空 mb 后页面全空 | 用 `gaoy` 布置任务、`zhangxm` 上传作品；家长用注册+绑定产生数据 |

---

## 8. 下一步（建议立即动手）

1. **执行 §3 改绑 SQL**（或确认选择清空 mb 表）
2. **Phase A1 + A2 + A3**（关 fallback、删 DEMO 回退、Task/Work 空即空）
3. 用 **`zhangxm` / `gaoy`** 登录冒烟任务列表与作品墙

需要开发实施时，从 **Phase A** 开始提交代码即可；本文档作为迭代检查清单。

---

## 9. 家长自助注册 · API 草案（Release 2）

### 9.1 `POST /api/mobile/auth/parent/register`（`permitAll`）

**Request**（**无 smsCode**）

```json
{
  "phone": "13800138000",
  "password": "xxxxxxxx",
  "nickname": "王美丽",
  "rootOrgId": "学校 org_id"
}
```

**行为**：`login_name=phone`，`status=t`，**不签发 token**。

### 9.2 配置项

```yaml
mobile:
  parent-bind-auto-approve: false   # 生产必须 false
  asr:
    provider: aliyun
    fallback-browser: true
  demo-fallback-enabled: false
```

### 9.3 绑定孩子（改既有 API）

`POST /api/mobile/parent/bind`：

- `resolveChildUserId` → `SELECT sys_user WHERE user_role_id=Student AND user_org_id IN (班级树) AND (user_name=? OR login_name=?)`
- `bind_status` = `auto-approve ? approved : pending`
- 删除 `CHILD_NAME_TO_USER_ID`、`MobileUserContext.DEMO_PARENT_ID` 回退

### 9.4 验收脚本（无 mock）

```
1. POST register（新手机号）→ 200 + userId
2. POST login（该手机号）→ userRoleId=Parent
3. GET parent-dashboard → 空态（未绑定）
4. POST bind（zhangxm 姓名+班级）→ bindStatus=pending|approved
5. GET parent-dashboard → 孩子列表来自 DB；无「张小明/张小美」除非库里真叫这名
```
