# 移动端前端数据来源审计

> 生成：2026-06-05 · **勘误**：2026-06-08  
> ⚠️ **本文未全量刷新**。以下条目已过时，**以 [README.md](./README.md)「2026-06 实现补充」为准**：`HomeTeacher`、`/teacher-ai`、`mb_user_settings` 禁止建表、绑定仅管理端审核。  
> 表结构 → [mobile-sql-decision.md](./mobile-sql-decision.md) · API → `Mobile*Controller.java`

---

## 1. 图例说明

| 标记 | 含义 |
|------|------|
| **DB** | 数据主要来自数据库（经 API 读写） |
| **混合** | 已调 API，但空数据/失败时回退 mock，或页面内仍有 mock 字段 |
| **Mock** | 纯前端 mock / 硬编码演示，未接 API 或 API 未入库 |
| **业务库** | 使用管理端已有表（非 `mb_*`），经 `/api/mobile/*` 门面访问 |

**兜底策略（全局）**

- 前端：`try/catch` 或 API 返回空 → 使用 `PROTOTYPE`（`prototypeFallbacks.js`）
- 后端：`mb_*` 表无数据 → `MobilePrototypeData`；登录用户无个人数据 → 回退 `demo-student-001` / `demo-parent-001` 种子

---

## 2. 数据源总览

```
┌─────────────────────────────────────────────────────────────────┐
│                        移动端前端页面                              │
└────────────┬───────────────────────────────┬────────────────────┘
             │                               │
     ┌───────▼────────┐              ┌───────▼────────┐
     │  /api/mobile/* │              │  门面委托       │
     │  (mobile 包)   │              │  exp/system 等  │
     └───────┬────────┘              └───────┬────────┘
             │                               │
    ┌────────┼────────┐            ┌─────────┼─────────┐
    │ mb_* 表 │+ 混合  │            │ sys_user │ exp_*  │
    │         │prototype│           │ notice   │ data_* │
    └─────────┴────────┘            └─────────┴─────────┘
             │
    ┌────────▼────────┐
    │ prototypeFallbacks.js │  ← 前端本地 mock（最终兜底）
    └─────────────────┘
```

---

## 3. 按页面明细

### 3.1 认证与账号

| 路由 | 页面 | 数据来源 | API | 说明 |
|------|------|----------|-----|------|
| `/login` | LoginView | **业务库** | `POST /api/mobile/auth/login` | `sys_user` 登录，移动端 JWT 存 localStorage |
| `/register/parent` | ParentRegisterView | **Mock** → **R2 接 register API** | `POST /api/mobile/auth/parent/register` | 写 `sys_user`（Parent）；**不预建种子家长** |

### 3.2 首页与发现

| 路由 | 页面 | 数据来源 | API | 说明 |
|------|------|----------|-----|------|
| `/home`（学生） | HomeStudent | **业务库** | `GET /api/mobile/home/feed` | 实验/模拟实验信息流，来自 `exp_*`、`data_*` 等 |
| `/home`（学生） | 年级筛选 | **混合** | `GET /api/mobile/home/grade-filters` | 筛选项后端固定配置，非 DB 动态 |
| `/home`（学生） | 未读角标 | **业务库** | `GET /api/mobile/notifications/unread-count` | 系统消息表 |
| `/home`（家长） | HomeParent + ParentDashboardBody | **业务库** | `GET /api/mobile/home/parent-dashboard` | 孩子列表与进度来自 `mb_task_submission`；最近动态由 `MobileParentActivityService` 直读 `mb_task_submission`/`mb_work`/`mb_badge_progress`；今日答题状态直读 `mb_quiz_record` |
| `/home`（教师） | HomeTeacher | **Mock** | 无 | 统计数字来自 `PROTOTYPE.profile.teacher.stats` |
| `/search` | SearchView | **业务库** | `GET /api/mobile/home/search`、`/hot-keywords` | 与首页 feed 同源 |
| `/search/voice` | VoiceSearchView | **Mock** | 无 | 随机 demo 关键词，跳转搜索页带 `?q=` |
| `/video/:id`、`/exp/:id` | ContentDetailView | **混合** | `GET /api/mobile/content/exp/*` | 实验详情、视频、步骤、材料等来自**业务库**；**点赞/收藏/留言为前端本地 mock**，未调 API |
| `/notifications` | NotificationListView | **业务库** | `GET /api/mobile/notifications` | 系统消息 |
| `/notifications/*` | NotificationDetailView | **混合** | messages + `GET /api/mobile/home/notices/latest` | 消息详情 + 公告 |
| `/chat` | AssistantChatView | **混合** | `POST /api/mobile/chat/send` | AI 对话走后端代理；欢迎语/快捷场景部分为前端配置 |

### 3.3 模拟实验

| 路由 | 页面 | 数据来源 | API | 说明 |
|------|------|----------|-----|------|
| `/experiments` | VirtualExpView | **业务库** | `GET /api/mobile/simulators` | `exp_simulator` 等 |
| `/experiments/search` | VirtualExpSearchView | **业务库** | simulators + `GET /api/mobile/dict/{type}` | 字典为业务库 |
| `/sim/:id` | VirtualExpDetailView | **业务库** | `GET /api/mobile/simulators/{id}` | 模拟实验详情 |

### 3.4 任务与上传

| 路由 | 页面 | 数据来源 | API | 说明 |
|------|------|----------|-----|------|
| `/tasks` | TasksView | **混合** | `GET /api/mobile/tasks` | 有 `mb_task` 数据则读库；**列表为空时回退 `PROTOTYPE.tasksDemo`** |
| `/tasks/:id` | TaskDetailView | **混合** | `GET /api/mobile/tasks/{id}` | API 与 `getTaskDetail()` prototype **合并**；404 时纯 prototype |
| `/assign` | AssignView | **DB（写）** | `POST /api/mobile/tasks` | 写入 `mb_task`；班级/实验选项为页面硬编码 |
| `/upload` | UploadView | **混合** | `POST /api/mobile/works` | **提交入库**；预览文件列表、关联作业下拉来自 **`PROTOTYPE.upload`**，非真实选文件/上传 OSS（可选 `/api/files/upload` 已封装未在页内使用） |

### 3.5 作品墙

| 路由 | 页面 | 数据来源 | API | 说明 |
|------|------|----------|-----|------|
| `/works` | WorksView | **混合** | `GET /api/mobile/works` | 有 `mb_work` 则读库；空则 **`PROTOTYPE.works.items`** |
| `/works/:id` | WorkDetailView | **混合** | `GET /api/mobile/works/{id}` | 作品主体可来自库；**评论列表、点赞 toggle 为 mock**（`PROTOTYPE.workComments` + 本地 state） |

### 3.6 答题与勋章

| 路由 | 页面 | 数据来源 | API | 说明 |
|------|------|----------|-----|------|
| `/quiz` | QuizView | **混合** | `GET /api/mobile/quiz/today` | **题目内容始终来自后端 prototype**（`MobilePrototypeData`）；history 可叠加 `mb_quiz_record` |
| `/quiz` | 提交 | **DB（写）** | `POST /api/mobile/quiz/submit` | 写入 `mb_quiz_record`；练习模式不入库 |
| `/quiz/submit` | QuizSubmitView | **混合** | submit | 答案来自 sessionStorage 草稿 |
| `/quiz/result/:type` | QuizResultView | **混合** | 提交结果 sessionStorage | 失败时用 prototype 文案 |
| `/quiz/history` | QuizHistoryView | **混合** | `GET /api/mobile/quiz/today` | 使用返回的 `history`（来自 `mb_quiz_record`）；无则 prototype |
| `/quiz/completed` | QuizCompletedView | **Mock** | 无 | 硬编码「5/5 +20 分」等 |
| `/quiz/review` | QuizReviewView | **Mock** | 无 | 错题来自 `PROTOTYPE.quiz.questions` 切片 |
| `/badges` | BadgeWallView | **混合** | `GET /api/mobile/badges` | 有 `mb_badge_def` / `mb_badge_progress` 则读库；否则 prototype |

### 3.7 成长档案与家长绑定

| 路由 | 页面 | 数据来源 | API | 说明 |
|------|------|----------|-----|------|
| `/growth` | GrowthArchiveView | **DB** | `GET /api/mobile/growth`、`PUT /api/mobile/growth/plan` | 时间线 `mb_growth_event`；stats 与 plan 过滤一致；`visibility=self` 时家长不可见详情；方案持久化 `mb_growth_plan` |
| `/bind-child` | BindChildView | **混合** | `POST /api/mobile/parent/bind` | 学校/年级/班级选项为 **`PROTOTYPE.bind`**；提交写入 `mb_parent_child` |
| `/bind-success` | BindSuccessView | **Mock** | 无 | 展示 query 中的姓名 |

### 3.8 教师端

| 路由 | 页面 | 数据来源 | API | 说明 |
|------|------|----------|-----|------|
| `/review` | ReviewView | **Mock** | 无 | `PROTOTYPE.reviewPending`，批阅 alert 演示 |
| `/board` | ProgressBoardView | **Mock** | 无 | 学生提交列表硬编码 |
| `/teacher-ai` | TeacherAiView | **Mock** | 无 | `PROTOTYPE.teacherAi` 文案与 chips |

### 3.9 个人中心与设置

| 路由 | 页面 | 数据来源 | API | 说明 |
|------|------|----------|-----|------|
| `/profile` | ProfileView | **混合** | profile、tasks、browse-stats、messages | 用户资料 **DB**（`sys_user`）；统计为 browse-stats + tasks 计数；**为 0 时用 prototype 数字**；家长孩子卡片/通知/时间线大量 prototype |
| `/settings` | SettingsView | **混合** | profile、updateProfile、changePassword | 账号读写 **DB**；通知偏好/设备/评语模板等 **alert 演示**；多处展示 `PROTOTYPE.*` 文案 |

---

## 4. API 与数据库对照

### 4.1 移动端专属 API（`/api/mobile/*`）

| API | 方法 | 前端模块 | 主要表/数据源 | 读/写 | 后端 fallback |
|-----|------|----------|---------------|-------|---------------|
| `/mobile/home/feed` | GET | home | `exp_msg`、`exp_simulator`、`data_file` 等 | 读 | 无（空列表） |
| `/mobile/home/search` | GET | home | 同上 | 读 | 无 |
| `/mobile/home/hot-keywords` | GET | home | feed 实验名 | 读 | 无 |
| `/mobile/home/browse-stats` | GET | home, profile | feed/simulator count | 读 | 无 |
| `/mobile/home/grade-filters` | GET | home | 固定配置 | 读 | — |
| `/mobile/home/notices/latest` | GET | home, notification | `school_notice` | 读 | null |
| `/mobile/home/parent-dashboard` | GET | home | `mb_parent_child`、`mb_task_submission`、`mb_work`、`mb_badge_progress`、`mb_quiz_record` | 读 | `MobileParentActivityService` |
| `/mobile/profile` | GET/PUT | profile, settings, growth | `sys_user`、`sys_org` | 读写 | 401 |
| `/mobile/profile/password` | POST | settings | `sys_user` | 写 | — |
| `/mobile/tasks` | GET | tasks, profile | `mb_task`、`mb_task_submission` | 读 | prototype |
| `/mobile/tasks` | POST | assign | `mb_task` | 写 | — |
| `/mobile/tasks/{id}` | GET | task-detail | 同上 | 读 | prototype |
| `/mobile/works` | GET | works | `mb_work` | 读 | prototype |
| `/mobile/works` | POST | upload | `mb_work`、`mb_work_file`、`mb_task_submission` | 写 | — |
| `/mobile/works/{id}` | GET | work-detail | `mb_work` | 读 | prototype |
| `/mobile/quiz/today` | GET | quiz | 题目=prototype（待接 `exp_question`）；history=`mb_quiz_record` | 读 | history prototype |
| `/mobile/quiz/submit` | POST | quiz | `mb_quiz_record` | 写 | — |
| `/mobile/badges` | GET | badges | `mb_badge_def`、`mb_badge_progress` | 读 | prototype |
| `/mobile/growth` | GET | growth | `mb_growth_event` + prototype stats 部分 | 读 | prototype |
| `/mobile/parent/bind` | POST | bind-child | `mb_parent_child` | 写 | — |
| `/mobile/chat/send` | POST | chat | 外部 AI / 代理 | 写 | — |

### 4.2 移动端门面 API（`/api/mobile/*` 全隔离）

> **2026-06 改造**：移动端前端不再调用非 `/mobile` 路径。下列接口为只读/写门面，内部委托 `exp`、`system` 等既有 Service，Controller 均位于 `com.xuanyue.exp.mobile` 包。

| API | 前端模块 | 主要表 | 说明 |
|-----|----------|--------|------|
| `GET /api/mobile/content/exp/{id}/detail` 等 | experiment, content | `exp_*`、`data_*` | 实验详情全套 |
| `GET /api/mobile/simulators` | simulator | `exp_simulator` | 模拟实验 |
| `GET /api/mobile/dict/{type}` | dict | 字典表 | 筛选字典 |
| `GET /api/mobile/notifications` | notification, home, profile | 系统消息表 | 消息列表/未读/已读 |
| `POST /api/mobile/files/upload` | work | 文件存储 | 作品附件上传 |
| `GET/PUT /api/mobile/admin/quiz-config` | 管理端 exp.js | `mb_quiz_config` | 每日答题配置（管理端 SPA） |

移动端认证：`POST /api/mobile/auth/login`（`sys_user` + 移动端 JWT），与管理端 `POST /api/auth/login` 完全分离。

---

## 5. `mb_*` 表使用情况

> 建表档位的权威说明见 [mobile-sql-decision.md](./mobile-sql-decision.md)。下表为**代码/API 实际引用**状态。

| 表 | SQL 档位 | 后端读 | 后端写 | 前端 | 说明 |
|----|----------|--------|--------|------|------|
| `mb_task` | Tier 1 | ✅ | ✅ | 任务/布置 | 必需 |
| `mb_task_submission` | Tier 1 | ✅ | ✅ | 任务/上传 | 必需 |
| `mb_work` | Tier 1 | ✅ | ✅ | 作品墙 | 必需 |
| `mb_work_file` | Tier 1 | ⚠️ 写已接，读详情未返附件 | ✅ | 上传 | 必需 |
| `mb_quiz_record` | Tier 1 | ✅ | ✅ | 答题 | 必需 |
| `mb_parent_child` | Tier 1 | ✅ | ✅ | 绑定 | 必需 |
| `mb_quiz_daily` | Tier 1 | ❌ 待接 | — | 答题 | 接 `exp_question` 时用 |
| `mb_badge_def` / `mb_badge_progress` | Tier 2 | ✅ | — | 勋章 | 无表则 prototype |
| `mb_growth_event` | Tier 2 | ✅ | — | 成长 | 无表则 prototype |
| `mb_comment` | Tier 2 | ❌ | ❌ | 评论 mock | 等 Comment API |
| `mb_user_reaction` | Tier 2 | ❌ | ❌ | 点赞 mock | 等 Reaction API |
| `mb_user_settings` | **禁止** | ❌ | ❌ | 设置 mock | 无 Entity，勿建表 |
| `mb_user_history` | **禁止** | ❌ | ❌ | — | 无功能 |
| `mb_notice_read` | **禁止** | ❌ | ❌ | — | 用 `sys_msg.read_tag` |

---

## 6. `prototypeFallbacks.js` 主要块与用途

| 键名 | 用途 | 仍依赖 mock 的页面 |
|------|------|-------------------|
| `tasksDemo` / `taskDetails` | 任务列表/详情兜底 | TasksView、TaskDetailView |
| `works` / `workComments` | 作品墙/详情/评论 | WorksView、WorkDetailView |
| `quiz` | 题目、history、streak | QuizView、QuizHistoryView、QuizResultView、QuizReviewView |
| `badges` | 勋章墙 | BadgeWallView |
| `growth` / `growth.plan` | 成长档案、方案弹层 | GrowthArchiveView |
| `parentDashboard` | 家长看板实验/动态/今日答题 | HomeParent、ParentDashboardBody |
| `bind` | 绑定向导选项 | BindChildView |
| `upload` | 上传页文件预览 | UploadView |
| `reviewPending` | 教师批阅队列 | ReviewView |
| `teacherAi` | 教师 AI 页 | TeacherAiView |
| `profile.*` | 个人中心统计/时间线/通知 | ProfileView、SettingsView |
| `parent` / `student` / `teacher` | 各角色展示文案 | ProfileView、SettingsView |

---

## 7. 建议优先级（后续排期参考）

### P0 — 高优先级（影响核心闭环）

1. **教师批阅** `POST /api/mobile/teacher/grade` → `mb_task_submission` + `mb_work.review_*`（ReviewView 纯 mock）
2. **评论** `GET/POST /api/mobile/comments` → `mb_comment`（WorkDetailView、ContentDetailView）
3. **点赞/收藏** → `mb_user_reaction`（作品详情、内容详情）
4. **作品详情返回附件** `mb_work_file` 读接口 + WorkDetailView 展示
5. **QuizCompletedView** 对接今日是否已答 + 真实分数（读 `mb_quiz_record`）

### P1 — 中优先级（体验完整度）

6. **UploadView** 真实文件选择 + `POST /api/files/upload` 后再 `createWork`
7. **家长看板** 活动流/当前实验读库（替代 prototype 填充）
8. **绑定孩子** 学校/年级/班级读 **`sys_org`**，孩子匹配 `sys_user`
9. **用户设置** — **`mb_user_settings` 禁止提前建表**；待 Entity/API 设计后再定（可 JSON 或 `sys_user` 扩展）
10. **ProfileView / HomeTeacher** 教师统计读 `mb_task_submission` 聚合

### P2 — 低优先级 / 演示页

11. ProgressBoardView、TeacherAiView、ParentRegisterView、VoiceSearchView
12. 成长方案保存入库
13. 登录用户 ID 全面替换 demo-student/parent/teacher 种子回退逻辑

---

## 8. 快速自检命令

```bash
# 后端编译
cd backend && mvn compile -DskipTests

# 前端构建
cd frontend/mobile && npm run build

# 确认 mb 表有数据（MySQL）
SELECT COUNT(*) FROM mb_task;
SELECT COUNT(*) FROM mb_work;
SELECT COUNT(*) FROM mb_quiz_record;
SELECT COUNT(*) FROM mb_parent_child;
```

---

## 9. 相关文档

- [README.md](./README.md) — 文档索引
- [mobile-dev-plan.md](./mobile-dev-plan.md) — 开发计划与 Mock 策略
- [mobile-sql-decision.md](./mobile-sql-decision.md) — 建表档位与 SQL 执行
- `frontend/mobile/DEVELOPMENT_PLAN.md` — 前端原型完成度

---

*本文档随 API 对接进度更新；修改 mock/API 策略时请同步修订第 3、5、7 节。*
