# 移动端 · 全量原型对照开发计划

> 📦 **已停更（2026-06-08）**。路由/API/完成度以 **[docs/README.md](../../docs/README.md)** 与代码为准；勿在此维护。  
> 对照 `frontend/mobile_prototypes/pages/` 共 **42** 个 HTML 原型（历史参考）

---

## 模块对照总表

| 阶段 | 原型 | 页面 | 路由 | 状态 | 后端 API |
|------|------|------|------|------|----------|
| **P0** | 00 | 登录 | `/login` | done | ✅ auth |
| **P0** | 01 | 绑定孩子 | `/bind-child` | done | ❌ |
| **P0** | 02 | 绑定成功 | `/bind-success` | done | ❌ |
| **P0** | 26 | 家长注册 | `/register/parent` | done | ❌ |
| **P1** | 03 | 学生首页 | `/home` | done | ✅ home |
| **P1** | 03-no-notice | 无通知首页 | `/home` | partial | ✅ |
| **P1** | 03-search | 搜索 | `/search` | done | ✅ |
| **P1** | 03-voice | 语音搜索 | `/search/voice` | done | ❌ |
| **P1** | 05 | 家长首页 | `/home` (parent) | done | ⚠️ stub |
| **P1** | 04 | 中学首页 | 合并至学生首页 | n/a | — |
| **P2** | 06 | 视频/实验详情 | `/video/:id` `/exp/:id` | done | ✅ experiment |
| **P2** | 07-* | 模拟实验 | `/experiments` `/sim/:id` | done | ✅ simulator |
| **P2** | 08/09 | 石头老师 | `/chat` | done | ✅ chat |
| **P3** | 10 | 我的任务 | `/tasks` | done | ⚠️ stub |
| **P3** | 11 | 任务详情 | `/tasks/:id` | done | ⚠️ 404 |
| **P4** | 12 | 作品墙 | `/works` | done | ❌ |
| **P4** | 13 | 作品详情 | `/works/:id` | done | ❌ |
| **P4** | 14 | 成果上传 | `/upload` | done | ❌ |
| **P5** | 15–17 | 个人中心 | `/profile` | done | ✅ profile |
| **P5** | 25 | 设置 | `/settings` | done | ✅ profile |
| **P6** | 18 | 成长档案 | `/growth` | done | ❌ |
| **P6** | 19 | 勋章墙 | `/badges` | done | ❌ |
| **P7** | 20-* | 每日答题(9页) | `/quiz/*` | done | ❌ |
| **P8** | 21 | 布置实验任务 | `/assign` | partial | ❌ |
| **P8** | 22 | 审核批阅 | `/review` | done | ❌ |
| **P8** | 23 | 班级看板 | `/board` | partial | ❌ |
| **P8** | 24 | 教师 AI | `/teacher-ai` | done | ✅ chat |
| **P9** | 27-* | 消息通知 | `/notifications` | done | ✅ notification |

图例：✅ 已完成 · partial 部分完成 · 🔲 待开发 · 🔄 当前迭代

---

## 分步执行计划

### Phase 0 · 基础设施（1 天）
- [x] 原型优先规则 & 数据库变更审批规则
- [x] `prototypeFallbacks.js` 基础数据
- [ ] **路由表补全**：所有 Profile/首页 跳转不再 404
- [ ] **统一点击占位**：`stubAction` / 导航工具
- [ ] 更新本计划进度

### Phase 1 · 学生内容闭环（2–3 天）— **当前执行**
> 目标：个人中心快捷入口全部可打开

1. **P12 作品墙** `WorksView.vue` — tabs + 卡片网格 + 原型数据
2. **P13 作品详情** `WorkDetailView.vue` — 媒体区 + 批阅 + 互动按钮
3. **P19 勋章墙** `BadgeWallView.vue` — 筛选 + 进度 + 徽章网格
4. **P18 成长档案** `GrowthArchiveView.vue` — 统计 + 轨迹 + 展示方案
5. 注册路由，更新 `ProfileView` 导航白名单

### Phase 2 · 上传与家长 onboarding（2 天）
1. **P14 成果上传** `UploadView.vue` — pad 双栏 + 手机单列
2. **P01 绑定孩子** `BindChildView.vue` — 学校/年级/班级级联
3. **P02 绑定成功** `BindSuccessView.vue`
4. **P26 家长注册** `ParentRegisterView.vue` + 登录页链接
5. 扩展 `prototypeFallbacks` 绑定/上传演示数据

### Phase 3 · 每日答题（2–3 天）
1. **P20 答题中** `/quiz` — 5 题流程 + 进度点
2. **P20-submit** 提交确认 `/quiz/submit`
3. **P20-result-*** 结果页 `/quiz/result/:type` (perfect/partial/low/practice)
4. **P20-completed** 今日已完成 `/quiz/completed`
5. **P20-review-wrong** 错题解析 `/quiz/review`
6. **P20-history** 答题记录 `/quiz/history`
7. 本地 state 模拟计分（待 `/mobile/quiz/*` API）

### Phase 4 · 教师工具（2–3 天）
1. **P21 布置实验任务** `/assign`
2. **P22 审核批阅** `/review`
3. **P23 班级看板** `/board`
4. **P24 教师 AI** `/teacher-ai`（复用 chat + 快捷入口网格）
5. **角色化 BottomNav**：教师/家长/学生三套导航（store 驱动）
6. **P17 教师首页**：`/home` 教师分支增强

### Phase 5 · 已有页面深化（2 天）— **已完成**
1. **P10/P11 任务**：详情补全（截止/视频/步骤/上传 CTA）+ 原型 fallback ✅
2. **P05 家长首页**：孩子切换 + 材料助手 + 动态 ✅
3. **P08 助手**：语音按钮 + 识别演示 ✅
4. **P03-voice 语音搜索**：`/search/voice` 独立页 ✅
5. **角色化 BottomNav**：学生/家长/教师三套导航 ✅
6. **P17 教师首页**：`HomeTeacher.vue` 工作台 ✅
7. **P20 submit/completed**：`/quiz/submit` `/quiz/completed` ✅

### Phase 6 · 前端原型补全（2026-06-05）— **已完成**
1. **P00 登录**：Pad 双栏 + 角色 tabs + 产品名对齐 ✅
2. **P14 上传**：Pad 双栏 + 缩略图网格 + 提交成功页 ✅
3. **P13 作品详情**：评论列表 + 社交交互 + 拍同款链 `/upload?type=remix` ✅
4. **P22 批阅**：评级四档 + 评语 + 提交 ✅
5. **P24 教师 AI**：GPT-4o badge + 快捷入口 + prompt chips ✅
6. **P06 详情**：点赞/收藏 toggle + 留言发表 ✅
7. **P18 成长档案**：6 条轨迹 + 手机顶栏 + 方案弹层 ✅
8. **设置**：绑定新孩子 → `/bind-child` ✅
9. **模拟实验搜索**：语音 mic 入口 ✅

### Phase 7 · 移动端演示 API（2026-06-05）— **已完成（无新建表）**
| 模块 | 路径 | 状态 |
|------|------|------|
| 任务 | `GET /api/mobile/tasks` `/{id}` | ✅ 演示数据 |
| 作品 | `GET /api/mobile/works` `/{id}` | ✅ 演示数据 |
| 答题 | `GET /api/mobile/quiz/today` | ✅ 演示数据 |
| 勋章 | `GET /api/mobile/badges` | ✅ 演示数据 |
| 成长 | `GET /api/mobile/growth` | ✅ 演示数据 |
| 家长首页 | `GET /api/mobile/home/parent-dashboard` | ✅ 演示数据 |

前端已对接上述 API，失败时仍回退 `prototypeFallbacks.js`。

### Phase 8 · 持久化后端（2026-06-05 进行中）

**Step 1 — 执行 SQL（见 `sql/mobile/README.md`）**
1. **必跑** `mobile_required_tables.sql`（7 张表）
2. 可选 `mobile_optional_demo_tables.sql` + `mobile_demo_seed.sql` + `mobile_optional_demo_seed.sql`

**Step 2 — 后端持久化层（已完成代码）**
- Entity / Repository：`MbTask`, `MbWork`, `MbQuizRecord`, `MbBadge*`, `MbParentChild`, `MbGrowthEvent`
- Service：读库优先，空表时回退 `MobilePrototypeData`

**Step 3 — 写 API（已完成）**
- `POST /api/mobile/tasks` 布置实验任务
- `POST /api/mobile/works` 成果上传
- `POST /api/mobile/quiz/submit` 答题提交
- `POST /api/mobile/parent/bind` 绑定孩子
- `POST /api/mobile/parent/bind` 绑定孩子

---

## 每步验收标准

1. 打开对应原型 HTML 与 Vue 页面 **并排对比**，布局/文案/分组一致
2. 无 API 时展示 `prototypeFallbacks` 数据，不出现空白区块
3. 所有按钮有可感知反馈（跳转 / alert / toggle）
4. 平板 + 手机断点均可用（`pad-shell` 布局）
5. 无 ESLint 错误

---

## 进度日志

| 日期 | 内容 |
|------|------|
| 2026-06-05 | 完成 P15/P16/P17/P25 原型对齐；创建本计划 |
| 2026-06-05 | 启动 Phase 1：作品墙/详情/勋章/成长 + 路由补全 |
| 2026-06-05 | 完成 Phase 1–4 路由与页面骨架：作品/勋章/成长/上传/绑定/答题/教师工具 |
| 2026-06-05 | Phase 6 前端原型补全：登录/上传/批阅/教师AI/成长档案等 |
| 2026-06-05 | Phase 7 移动端演示 API + 前端对接（tasks/works/quiz/badges/growth/parent-dashboard） |
