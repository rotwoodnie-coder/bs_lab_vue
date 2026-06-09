# 移动端开发计划

> 更新：2026-06-08  
> 依据：`sql/bs_exp_vue.sql`（53 张业务表，**不含** `mb_*`）  
> **表清单与脚本**：只维护 [mobile-sql-decision.md](./mobile-sql-decision.md) · **API**：只维护 `Mobile*Controller.java`

---

## 1. 数据架构

### 1.1 三层模型

```
Layer A · bs_exp_vue.sql     登录/实验/题库/消息/组织/AI 会话…
Layer B · sql/mobile/*.sql   仅业务库没有的：任务/作品/答题记录/绑定…
Layer C · prototypeFallbacks  仅网络失败或未初始化时的薄兜底
```

**原则**：能复用 Layer A 的不建 Layer B；`mb_*` 用 `exp_id` / `question_id` / `user_id` 关联现有主键。

### 1.2 Layer A 重点复用

| 模块 | 表 | 移动端 |
|------|-----|--------|
| 账号/积分 | `sys_user`（**`per_score`**） | 登录、Profile、答题加分 |
| 组织 | `sys_org` | 绑定孩子、班级 |
| 实验 | `exp_msg`, `exp_video`, `data_file` | 首页、详情 |
| **题库** | **`exp_question`, `exp_question_select`** | 每日一题（**不建题库表**） |
| 消息 | `sys_msg`, `school_notice` | 通知 |
| AI | `ai_chat_session`, `ai_chat_message` | 石头老师 |
| 批阅档位 | `data_rating_scale` | 教师评分 |

### 1.3 Layer B（`mb_*`）

**不在此重复表结构** → [mobile-sql-decision.md §1](./mobile-sql-decision.md#1-mb_-表总览19-张)

要点：`mb_quiz_daily` 已废弃；`mb_user_settings` / `mb_notice_read` 等增量表见 sql-decision §5。禁止平行表（如重复建绑定表）。

---

## 2. 关键设计

### 2.1 任务 / 作品 ↔ `exp_msg`

- `mb_task.video_id` = `exp_msg.exp_id`
- `mb_task.task_type` ↔ `exp_task_type`（homework→hw, remix→tk, creative→self）
- `mb_task.class_org_id` → `sys_org.org_id`

### 2.2 每日答题

`mb_quiz_config` + `exp_question` → 按用户+日期分配题目 → `mb_quiz_record`。**勿使用** `mb_quiz_daily`（已废弃，见 sql-decision §3.7）。选项不下发 `is_right`；积分写 `sys_user.per_score` + 流水见 `mb_points_ledger`。

### 2.3 开发约束

- 不改 `frontend/src/`、`exp/`、`system/` 包内业务类
- 新 API 仅在 `com.xuanyue.exp.mobile`
- `ddl-auto: none`，表仅通过 SQL 脚本创建

---

## 3. 分期计划

### Phase 0 · 基线（≈95% 前端）

- [x] 原型页面、路由、`prototypeFallbacks`
- [x] 登录、首页、实验、模拟实验、通知、Profile、Chat
- [x] 部分 `/api/mobile/*` 读写（task/work/quiz/bind）

### Phase 1 · 核心闭环（1 周）

| 优先级 | 任务 | 依赖 |
|--------|------|------|
| P0 | 评论 CRUD | Tier 2 `mb_comment` |
| P0 | 点赞/收藏 | Tier 2 `mb_user_reaction` |
| P0 | 作品详情返回附件 | `mb_work_file` 读 |
| P0 | 教师批阅 | `mb_work` + `data_rating_scale` |
| P1 | 绑定向导读 `sys_org` | 业务库 |
| P1 | 积分写回 | `sys_user.per_score` |

### Phase 2 · 每日答题

- [x] `MobileLearningService` + `exp_question`；配置见 `mb_quiz_config`

### Phase 3 · 去 Mock（1 周）

- 任务关联真实 `exp_msg`
- **家长自助注册 + 绑定 + dashboard 聚合**
- 教师看板 SQL 聚合
- Profile 统计聚合
- test 环境关闭 demo fallback

### Phase 4 · 可选

系列订阅、管理端绑定/家长审核页（**教师移动端** `/parent-binds` 已实现）

---

## 4. API

**不在本文维护路径清单**（易过期、浪费维护成本）。以代码为准：

- `backend/src/main/java/com/xuanyue/exp/mobile/controller/Mobile*Controller.java`
- 表映射见 [mobile-sql-decision.md](./mobile-sql-decision.md)

移动端 API **100% 位于 `/api/mobile/*` 命名空间**（含 `/api/mobile/admin/*` 管理端接口）。移动端不再直接调用 `/api/exp/*`、`/api/system/*` 等管理端路径；只读内容通过 `Mobile*Controller` 门面委托既有 Service。

---

## 5. Mock 去兜底策略

### 5.1 问题

演示数据曾维护在三处：`prototypeFallbacks.js`、`MobilePrototypeData.java`、`mobile_demo_seed.sql`，易漂移。

### 5.2 目标

1. 演示数据 **只写 SQL seed**（或管理端录入）
2. API **只读 DB**；dev 可配置 fallback，**test/prod 禁止**
3. 前端 `PROTOTYPE` **仅 catch 网络失败**
4. 能 SQL 聚合的不复制 mock 数字

### 5.3 Mock → DB 接线优先级

| 优先级 | API / 改动 | 替换 Mock |
|--------|------------|-----------|
| P0 | 评论、批阅、作品附件 | `workComments`, `reviewPending` |
| P0 | 点赞/收藏 | 详情页本地 state |
| P1 | 每日一题读 `exp_question` | `MobilePrototypeData.buildQuiz()` |
| P1 | 绑定读 `sys_org` | `PROTOTYPE.bind` |
| P1 | **家长注册写 sys_user** | `ParentRegisterView` alert |
| P2 | 家长 feed / 教师看板聚合 | `parentDashboard`, ProgressBoard |
| 保留 mock | 语音搜索 demo 词 | 刻意不入库 |

### 5.4 环境

| 环境 | seed | fallback |
|------|------|----------|
| dev | 可选 | 可开 |
| test | 可选 | **关**（验收纯 DB） |
| prod | 无 | **关** |

建议配置：`mobile.demo-fallback-enabled: true/false`

### 5.5 验收

1. test 关闭 fallback 后，任务/作品/答题/批阅与 seed 一致  
2. 改演示数据 **只改 SQL**  
3. 写操作刷新后数据仍在库中  

页面级明细见 [mobile-data-source-audit.md](./mobile-data-source-audit.md)（⚠️ 部分过时，以 README 补充为准）。

---

## 6. 与旧方案差异

| 旧设想 | 现行 |
|--------|------|
| `mb_quiz_question` | 用 **`exp_question`** |
| 每日 5 题 | **每日 1 题** |
| 14 张 mb 表一次建全 | **Tier 1 仅 7 张** |
| `mobile_home_init.sql` | **废弃** |
| AI 在独立库 | 主库 **`ai_chat_*`** 为准 |

---

## 7. 下一步

1. **[mobile-no-mock-execution-plan.md](./mobile-no-mock-execution-plan.md)** — 改绑 mb 数据到内建 `sys_user`，分 Phase A–D 消除 mock  
2. 执行 `sql/mobile/migrate_mb_to_sys_user.sql`（或清空 mb 表）  
3. Phase A：关 fallback + 删 `MobileUserContext` DEMO 回退 + Task/Work 空即空  
4. Phase A5：每日一题接 `exp_question`  
