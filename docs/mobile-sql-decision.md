# 移动端 SQL 脚本决策

> 更新：2026-06-08  
> 脚本清单：`sql/mobile/README.md`  
> 文档索引：`docs/README.md`（**查文档先看索引，避免重复读多份**）

---

## 0. 核心约定

**联调/生产库通常已有 `mb_*` 表。开发前必须先核对实库，再决定复用或 ALTER。**

| 情况 | 做法 |
|------|------|
| 表已存在、字段够用 | 只改 Service/API/前端，**不建表** |
| 表已存在、缺字段 | 写 **可重复执行的 ALTER**（查 `information_schema` 跳过已有列） |
| 表确实不存在 | 再提议 CREATE TABLE，并说明核查依据 |

**禁止**为同一业务再建平行表。例如：家长绑定已用 **`mb_parent_child`**，审核走 `bind_status` / `class_org_id` 等列，**禁止**再建 `mb_parent_bind_audit` 等表。

`mobile_required_tables.sql` 中的 `CREATE TABLE IF NOT EXISTS` 仅用于**空库初始化**；已有表时只跑 **ALTER / 回填** 脚本。

---

## 1. `mb_` 表总览（19 张）

`mb_` = Mobile 端业务表，与平台基线表（`sys_user`、`sys_org`、`exp_question`、`exp_msg`、`sys_msg`、`school_notice` 等）配合使用。

| 层级 | 表数 | 说明 |
|------|------|------|
| 🔴 核心必须 | 7 | 任务 / 作品 / 答题 / 家长绑定主链路 |
| 🟠 功能必须 | 4 | 勋章墙 + 评论点赞（无表则 mock 或无法持久化） |
| 🟡 推荐 | 4 | 成长档案、积分流水、用户偏好、公告已读 |
| 🟢 条件必须 | 2 | JWT 刷新、钉钉登录（启用该能力时需要） |
| ⚪ 可废弃 | 1 | `mb_quiz_daily`（代码未使用） |

### 1.1 分域关系

```
核心学习闭环
  mb_task → mb_task_submission → mb_work → mb_work_file

每日答题
  mb_quiz_config + exp_question（非 mb_）→ mb_quiz_record
  mb_quiz_daily（已废弃，勿再写逻辑）

家长端
  mb_parent_child（含 pending/approved/rejected 审核）

社交
  mb_comment、mb_user_reaction

勋章积分
  mb_badge_def → mb_badge_progress → mb_points_ledger
  积分余额在 sys_user.per_score

成长档案
  mb_growth_event（事件写入）、mb_growth_plan（展示方案）

账号与设置
  mb_auth_refresh_token、mb_dingtalk_bind、mb_user_settings、mb_notice_read
```

### 1.2 必要性速查

| 判断 | 表 |
|------|-----|
| **空库必建（Tier 1）** | `mb_task`、`mb_task_submission`、`mb_work`、`mb_work_file`、`mb_quiz_record`、`mb_quiz_config`、`mb_parent_child` |
| **功能完整必建（Tier 1.5）** | `mb_badge_def`、`mb_badge_progress`、`mb_comment`、`mb_user_reaction` |
| **已有功能建议建（增量脚本）** | `mb_growth_event`、`mb_growth_plan`、`mb_points_ledger`、`mb_user_settings`、`mb_notice_read` |
| **按能力启用** | `mb_auth_refresh_token`、`mb_dingtalk_bind` |
| **不必再投入** | `mb_quiz_daily` |

---

## 2. 当前脚本

| 文件 | 必跑？ | 说明 |
|------|--------|------|
| **`mobile_required_tables.sql`** | **是（空库）** | Tier 1：7 张核心表（含 `mb_quiz_config`；`mb_quiz_daily` 保留兼容但已废弃） |
| **`mobile_feature_tables.sql`** | **是（空库）** | Tier 1.5：勋章 + 社交（含 comment 软删字段） |
| **`mb_parent_child_audit_alter.sql`** | **是（已有库）** | 绑定审核字段 + `class_org_id`（**可重复执行**） |
| `mb_parent_child_class_org_id_alter.sql` | 条件 | 仅补 `class_org_id`（审核列已有时） |
| `mb_parent_child_backfill_class_org_id.sql` | 可选 | 回填历史 pending 的 `class_org_id` |
| `mb_comment_soft_delete_alter.sql` | 条件 | comment 表已存在且无 `deleted_*` 列 |
| `mb_points_badge_migration.sql` | 推荐 | `mb_points_ledger` + `mb_badge_def.reward_points` 等 |
| `mb_growth_plan.sql` | 推荐 | 成长展示方案表 |
| `mb_growth_event_unique.sql` | 推荐 | 成长轨迹来源去重唯一索引 |
| `mb_user_settings_notice_read.sql` | 推荐 | 用户偏好 + 公告已读 |
| `mb_auth_refresh_token.sql` | 条件 | 启用 JWT Refresh 时 |
| `mb_dingtalk_bind.sql` | 条件 | 启用钉钉绑定时 |
| **`mobile_badge_def_seed.sql`** | 否（dev 可选） | 生产由 **管理端配置** |
| `mobile_sys_menu.sql` | 条件 | R0 管理端菜单 |
| `mobile_optional_demo_tables.sql` | 否 | **已 supersede** → 用 `feature_tables` + 增量脚本 |
| `mobile_demo_seed.sql` 等 | ❌ 不再推荐 | 含 demo-* user_id |

**开发最低要求（去 mock + 勋章/社交正常）**：

```bash
# 空库
mysql ... bs_exp_vue < sql/mobile/mobile_required_tables.sql
mysql ... bs_exp_vue < sql/mobile/mobile_feature_tables.sql
mysql ... bs_exp_vue < sql/mobile/mb_parent_child_audit_alter.sql

# 已有库：只跑 ALTER / 增量（见 sql/mobile/README.md）
mysql ... bs_exp_vue < sql/mobile/mb_parent_child_audit_alter.sql
```

测试数据改由 **[mobile-test-data-playbook.md](./mobile-test-data-playbook.md)**（管理端 + 移动端操作），不再跑 demo seed。

---

## 3. Tier 1 表详解（`mobile_required_tables.sql`）

### 3.1 `mb_task` — 教师布置的任务/作业 🔴

| 项 | 内容 |
|----|------|
| 主键 | `task_id` |
| 关键字段 | `teacher_user_id`、`class_org_id`、`deadline`、`video_id`→`exp_msg`、`task_type`(homework/remix)、`requirements_json`/`steps_json` |
| 作用 | 教师布置实验作业；学生「我的任务」列表 |
| API | `GET/POST /api/mobile/tasks` |
| Service | `MobileTaskService`、`MobileTeacherService` |

### 3.2 `mb_task_submission` — 学生任务进度 🔴

| 项 | 内容 |
|----|------|
| 主键 | `submission_id`；唯一 `(task_id, student_user_id)` |
| 关键字段 | `state`(pending/doing/done…)、`grade`、`review_comment`、`submit_time` |
| 作用 | 每生每任务一条；教师看板提交率、上传入口 |
| Service | `MobileTaskService`、`MobileWorkService`、`MobileTeacherService` |

### 3.3 `mb_work` — 学生作品 🔴

| 项 | 内容 |
|----|------|
| 主键 | `work_id` |
| 关键字段 | `work_type`(homework/remix/creative)、`task_id`、`submission_id`、`review_status`、`is_featured` |
| 作用 | 成果上传、作品墙、教师批阅队列 |
| API | `GET/POST /api/mobile/works` |
| 触发 | `mb_growth_event`、积分、勋章 |

### 3.4 `mb_work_file` — 作品附件 🔴

| 项 | 内容 |
|----|------|
| 主键 | `file_id`；索引 `(work_id, sort_order)` |
| 作用 | 作品下多图/视频 URL；不宜合并进 `mb_work` JSON |

### 3.5 `mb_quiz_record` — 每日答题记录 🔴

| 项 | 内容 |
|----|------|
| 主键 | `record_id`；唯一 `(user_id, quiz_date)` |
| 关键字段 | `score`/`total`/`points`/`perfect`、`question_ids_json`、`answers_json` |
| 作用 | 当天答题结果；任务列表「今日答题」、历史、成长时间线 |
| API | `POST /api/mobile/quiz/submit`、`GET /api/mobile/quiz/record` |
| 题库 | 使用 **`exp_question`**（不另建 `mb_quiz_question`） |

### 3.6 `mb_quiz_config` — 答题全局配置 🔴

| 项 | 内容 |
|----|------|
| 主键 | 固定 `config_id='default'` |
| 字段 | `questions_per_day`、`base_points`、`streak_bonus`、`enabled` |
| 作用 | 每日题数、积分规则 |

### 3.7 `mb_quiz_daily` — 每日排期 ⚪ 已废弃

| 项 | 内容 |
|----|------|
| 状态 | SQL 标注废弃；仅有 Entity/Repository，**无 Service 调用** |
| 现状 | 题目由 `QuizQuestionAllocator` 按 `user_id + quiz_date` 从 `exp_question` 自动分配 |
| 决策 | 已有库可保留空表；**新功能不要再写逻辑** |

### 3.8 `mb_parent_child` — 家长↔孩子绑定（含审核）🔴

| 项 | 内容 |
|----|------|
| 主键 | `bind_id`；唯一 `(parent_user_id, child_user_id)` |
| 关键字段 | `bind_status`(pending/approved/rejected)、`class_org_id`、`confirm_user_id`、`reject_reason`、学校/班级展示名 |
| 作用 | 家长绑定孩子；教师移动端「绑定审核」；家长切换孩子、查看任务 |
| API | `POST /api/mobile/parent/bind`；`GET/PATCH /api/mobile/teacher/parent-binds` |
| 闭环 | 家长申请 → `pending` → 教师审核 → `approved/rejected` → `sys_msg` 通知 |
| 增量 | `mb_parent_child_audit_alter.sql`（可重复执行） |

---

## 4. Tier 1.5 表（`mobile_feature_tables.sql`）— 勋章 + 社交 🟠

| 表 | 无表时 | 用途 | API |
|----|--------|------|-----|
| `mb_badge_def` | 勋章墙 mock | 勋章定义、达成条件、`reward_points` | `GET /api/mobile/badges` |
| `mb_badge_progress` | 无法授予 | 用户勋章进度 | 同上 |
| `mb_comment` | 留言 mock | 实验/模拟实验/作品评论 | `/api/mobile/social/comments` |
| `mb_user_reaction` | 点赞 mock | like / collect / play | `/api/mobile/social/reactions/toggle` |

**`mb_comment.target_type`**：`exp_msg` · `exp_simulator` · `exp_video` · `work`

**`mb_user_reaction` 唯一键**：`(user_id, target_id, target_type, reaction_type)`

---

## 5. 增量表（推荐 / 条件）

### 5.1 `mb_points_ledger` — 积分流水 🟡

| 项 | 内容 |
|----|------|
| 脚本 | `mb_points_badge_migration.sql` |
| 作用 | 积分变动审计；防重复加分（唯一 `user_id+source_type+source_id`） |
| 余额 | **`sys_user.per_score`** 存当前余额；本表存流水 |
| Service | `MobilePointsService` |

### 5.2 `mb_growth_event` — 成长时间线 🟡

| 项 | 内容 |
|----|------|
| 脚本 | `mobile_optional_demo_tables.sql` 段 / `mb_points_badge_migration.sql` / **`mb_growth_event_unique.sql`** |
| 唯一键 | `uk_ge_source (user_id, source_type, source_id)` |
| 写入 | `MobileGrowthEventService`（答题/上传/批阅/展示/获勋章）；`MobileGrowthBackfillService` 补历史 |
| 读取 | `MobileLearningService`（成长档案）、`MobileHomeService`（家长摘要，与 plan 过滤一致） |
| 说明 | 展示层事件表；stats 与 timeline 共用 `mb_growth_plan` 过滤 |

### 5.3 `mb_growth_plan` — 成长展示方案 🟡

| 项 | 内容 |
|----|------|
| 脚本 | `mb_growth_plan.sql` |
| 字段 | `content_keys_json`、`visibility`、`range` |
| 作用 | 用户自定义成长档案展示内容与时间范围 |

### 5.4 `mb_user_settings` — 用户偏好 🟡

| 项 | 内容 |
|----|------|
| 脚本 | `mb_user_settings_notice_read.sql` |
| 字段 | `settings_json`（通知开关等） |
| API | `GET/PUT /api/mobile/settings/preferences` |
| Service | `MobileSettingsService` |

### 5.5 `mb_notice_read` — 学校公告已读 🟡

| 项 | 内容 |
|----|------|
| 脚本 | `mb_user_settings_notice_read.sql` |
| 主键 | `(user_id, notice_id)` → **`school_notice`**（非 mb_） |
| 作用 | 首页公告未读标记；与 `sys_msg.read_tag`（私信）分离 |
| API | `GET/POST /api/mobile/home/notices/*` |

### 5.6 `mb_auth_refresh_token` — JWT Refresh 🟢

| 项 | 内容 |
|----|------|
| 脚本 | `mb_auth_refresh_token.sql` |
| 作用 | 移动端登录态刷新、多设备、登出吊销 |
| API | `POST /api/mobile/auth/refresh` |
| Service | `MobileAuthTokenService` |

### 5.7 `mb_dingtalk_bind` — 钉钉绑定 🟢

| 项 | 内容 |
|----|------|
| 脚本 | `mb_dingtalk_bind.sql` |
| 作用 | 钉钉 unionId ↔ `sys_user` |
| API | `/api/mobile/settings/dingtalk/*` |
| Service | `MobileDingTalkService` |

---

## 6. 主业务流程（表协作）

```
教师布置作业
  mb_task
    → mb_task_submission（学生进度）
      → mb_work + mb_work_file（上传）
        → 教师批阅（review_status / submission.state）
          → mb_growth_event、mb_points_ledger、mb_badge_progress
          → sys_msg 通知（非 mb_）

家长绑定
  POST bind → mb_parent_child (pending, class_org_id)
    → 教师审核 → approved
      → 家长切换孩子、看任务/成长

每日答题
  mb_quiz_config + exp_question
    → mb_quiz_record
      → 积分 / 勋章 / mb_growth_event
```

---

## 7. 与「非 mb_」表的分工（避免重复建表）

| 需求 | 应使用的表 | 不要重复建 |
|------|-----------|-----------|
| 用户 / 角色 / 积分余额 | `sys_user` | ❌ `mb_user` |
| 站内消息 / 绑定审核通知 | `sys_msg` | ❌ `mb_message` |
| 题库 | `exp_question` + `exp_question_select` | ❌ `mb_quiz_question` |
| 实验 / 视频内容 | `exp_msg` | ❌ `mb_exp` |
| 组织 / 班级 | `sys_org` | ❌ `mb_class` |
| 学校公告 | `school_notice` | ❌ `mb_notice` |
| 家长绑定 | **`mb_parent_child`**（已有） | ❌ 任何平行绑定表 |

---

## 8. 禁止 / 不推荐

| 项 | 原因 |
|----|------|
| `mb_user_history` | 无产品功能、无 Entity/API |
| 平行绑定表（如 `mb_parent_bind_audit`） | 与 `mb_parent_child` 重复 |
| `mb_quiz_daily` 新逻辑 | 已废弃，改用 `exp_question` 自动分配 |
| `mobile_demo_seed.sql` 等 | demo-* user_id；改用管理端造数 |

> **历史文档勘误**：曾写「禁止建 `mb_user_settings` / `mb_notice_read`」——当前代码**已使用**，见 §5.4、§5.5。

---

## 9. 执行顺序

### 9.1 空库初始化

```bash
mysql ... bs_exp_vue < sql/bs_exp_vue.sql
mysql ... bs_exp_vue < sql/mobile/mobile_required_tables.sql
mysql ... bs_exp_vue < sql/mobile/mobile_feature_tables.sql
mysql ... bs_exp_vue < sql/mobile/mb_parent_child_audit_alter.sql
# 可选增量
mysql ... bs_exp_vue < sql/mobile/mb_points_badge_migration.sql
mysql ... bs_exp_vue < sql/mobile/mb_growth_plan.sql
mysql ... bs_exp_vue < sql/mobile/mb_growth_event_unique.sql
mysql ... bs_exp_vue < sql/mobile/mb_user_settings_notice_read.sql
```

### 9.2 已有库（推荐流程）

```bash
# 1. 核对
SHOW TABLES LIKE 'mb_%';
SHOW COLUMNS FROM mb_parent_child;

# 2. 只跑缺失的增量（均可重复执行）
mysql ... bs_exp_vue < sql/mobile/mb_parent_child_audit_alter.sql
# 按需：mb_points_badge_migration / mb_growth_plan / mb_user_settings_notice_read 等

# 3. 回填（可选）
mysql ... bs_exp_vue < sql/mobile/mb_parent_child_backfill_class_org_id.sql
```

---

## 10. 表 ↔ 脚本 ↔ 档级 总览

| 表 | 档 | 脚本 |
|----|-----|------|
| `mb_task` / `mb_task_submission` / `mb_work` / `mb_work_file` | Tier 1 🔴 | `mobile_required_tables.sql` |
| `mb_quiz_record` / `mb_quiz_config` / `mb_parent_child` | Tier 1 🔴 | `mobile_required_tables.sql` + `mb_parent_child_audit_alter.sql` |
| `mb_quiz_daily` | ⚪ 废弃 | `mobile_required_tables.sql`（仅兼容，勿写逻辑） |
| `mb_badge_def` / `mb_badge_progress` / `mb_comment` / `mb_user_reaction` | Tier 1.5 🟠 | `mobile_feature_tables.sql` |
| `mb_points_ledger` | 增量 🟡 | `mb_points_badge_migration.sql` |
| `mb_growth_event` | 增量 🟡 | `mobile_optional_demo_tables.sql` 段 / `mb_points_badge_migration.sql` |
| `mb_growth_plan` | 增量 🟡 | `mb_growth_plan.sql` |
| `mb_user_settings` / `mb_notice_read` | 增量 🟡 | `mb_user_settings_notice_read.sql` |
| `mb_auth_refresh_token` | 条件 🟢 | `mb_auth_refresh_token.sql` |
| `mb_dingtalk_bind` | 条件 🟢 | `mb_dingtalk_bind.sql` |

---

## 11. 已删除 / 废弃脚本

| 文件 | 原因 |
|------|------|
| `mobile_home_init.sql` | 含禁止/延后表 |
| `mobile_core_tables.sql` | DROP 风险 + 冗余 |
| `mobile_demo_seed.sql` | demo-* 用户；改用管理端造数 |
| `mobile_optional_demo_seed.sql` | demo 进度与评论 |

---

## 12. 相关文档

- [sql/mobile/README.md](../sql/mobile/README.md)
- [mobile-no-mock-analysis.md](./mobile-no-mock-analysis.md) §十一
- [mobile-test-data-playbook.md](./mobile-test-data-playbook.md)
- [mobile-dev-plan.md](./mobile-dev-plan.md)
- [mobile-product-spec.md](./mobile-product-spec.md) §9.5（家长绑定审核）
