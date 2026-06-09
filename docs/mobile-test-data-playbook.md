# 移动端测试数据生产手册（管理端驱动）

> 更新：2026-06-05  
> 原则：**废弃 `mobile_demo_seed.sql` 中的 demo-* 用户**；测试数据由 **管理端 + 移动端真实操作** 产生，与前端页面一一对应。  
> 关联：[mobile-no-mock-analysis.md](./mobile-no-mock-analysis.md) · [mobile-sql-decision.md](./mobile-sql-decision.md)

---

## 1. 总体策略

| 旧做法 | 新做法 |
|--------|--------|
| 执行 `mobile_demo_seed.sql` 写入 demo-student-001 | ❌ 不再执行 |
| `migrate_mb_to_sys_user.sql` 改绑演示行 | ⚠️ 仅过渡；**推荐清空 mb 业务表后走本手册** |
| `mobile_optional_demo_seed.sql` 写假评论/勋章进度 | ❌ 不再执行（勋章 **定义** 可保留，**进度** 由行为触发） |
| 前端 PROTOTYPE 当初始数据 | 空态 → 管理端/操作产生数据 → API 回显 |

**内建账号（`sys_user`，无需 seed）**

| login | 角色 | 用途 |
|-------|------|------|
| `zhangxm` | Student | 学生端全链路 |
| `gaoy` / `liangwy` | Teacher | 布置、批阅、看板 |
| `yuanf` | Researcher | 教研员：标准实验发布、审核、教研点评 |
| `admin` / `bhy_admin` | Admin | 组织、用户、模拟器、题库 |

**家长**：移动端 `/register/parent` 自助注册，不在此表。

---

## 2. 按前端页面 → 管理端/操作造数

### 2.1 首页与发现

| 移动端页面 | 需要什么数据 | 怎么产生 |
|------------|--------------|----------|
| 学生首页 Feed | 已发布 `exp_msg`、视频、模拟实验 | 管理端 **标准实验** / **教学实验** 创建并审核通过（`status=y`） |
| 搜索 / 语音搜索 | 同上 + 标题关键词 | 实验名称含「彩虹」「风向标」等；语音测 **真实 ASR 或浏览器识别** 后搜 |
| 实验详情 `/exp/:id` | `exp_msg` + 步骤/材料/视频 | 管理端 `ExpStandardCreateView` / `TeachingExpCreateView` |
| 模拟实验列表/详情 | `exp_simulator` | 管理端 `ExpSimulatorView` 维护 |
| 通知 | `sys_msg` | 系统消息或管理端触发 |

### 2.2 任务 / 作品 / 答题

| 移动端页面 | 需要什么数据 | 怎么产生 |
|------------|--------------|----------|
| 我的任务 | `mb_task` + `mb_task_submission` | **方式 A**：`gaoy` 移动端布置作业（关联真实 `exp_id`）<br>**方式 B**：管理端确认实验已存在后教师端布置 |
| 任务详情 | `mb_task.video_id` = 真实 `exp_msg.exp_id` | 布置时选管理端已发布实验，**禁止** rainbow/windmill 字符串假 id |
| 作品墙 / 详情 | `mb_work` + `mb_work_file` | `zhangxm` 移动端上传；附件走 `/api/files/upload` |
| 每日答题 | `exp_question` + `mb_quiz_config` | 管理端维护题库；题目按用户+日期自动分配（**不用** `mb_quiz_daily`） |
| 答题记录 | `mb_quiz_record` | 学生移动端答题提交 |

### 2.3 勋章 / 成长

| 移动端页面 | 需要什么数据 | 怎么产生 |
|------------|--------------|----------|
| 勋章墙 | `mb_badge_def` + `mb_badge_progress` | **管理端**配置规则；学生行为触发进度 |
| 成长档案 | `mb_growth_event` 或聚合 | 优先 **事件聚合**（work/quiz/badge 写入时同步）；可选手动补 `mb_growth_event` |

### 2.4 社交（实验 / 模拟实验 / 作品）

| 移动端页面 | 需要什么数据 | 怎么产生 |
|------------|--------------|----------|
| 实验详情留言/点赞 | `mb_comment` + `mb_user_reaction` | 先建表（`mobile_feature_tables.sql`）<br>各角色登录移动端 **真实发表评论** |
| 作品详情评论 | `mb_comment`（target_type=work） | 同上 |
| 模拟实验详情社交 | `mb_comment` / `mb_user_reaction`（target_type=exp_simulator） | 同上；计数从 reaction 聚合 |
| 教研员点评 | `mb_comment.user_role_tag=researcher` | `yuanf` 登录后在实验详情留言 |

### 2.5 教师 / 家长

| 移动端页面 | 需要什么数据 | 怎么产生 |
|------------|--------------|----------|
| 布置作业 | `exp_msg` + `sys_org` 班级 | 管理端维护组织树；教师移动端选真实班级/实验 |
| 批阅 | `mb_work` pending + `data_rating_scale` | 学生提交作品后 `gaoy` 批阅 |
| 家长注册/绑定 | `sys_user`(Parent) + `mb_parent_child` | 移动端 **家长注册**：选学校/年级/班级 → 查询学生 → 提交；管理端审核账号与绑定 |

---

## 3. 推荐初始化顺序（新环境）

```bash
# 1. 业务库基线
mysql ... bs_exp_vue < sql/bs_exp_vue.sql

# 2. 移动端 Tier 1（7 表）
mysql ... bs_exp_vue < sql/mobile/mobile_required_tables.sql

# 3. 功能表（勋章 + 社交，见 mobile-sql-decision §2.1）
mysql ... bs_exp_vue < sql/mobile/mobile_feature_tables.sql

# 4. 绑定审核字段
mysql ... bs_exp_vue < sql/mobile/mb_parent_child_audit_alter.sql

# 5. 清空 demo 业务数据
mysql ... bs_exp_vue < sql/mobile/truncate_mb_demo_data.sql

# 6. 管理端操作（浏览器）
#    - 发布 2～3 个标准实验（含视频/步骤）
#    - 维护 1～2 个模拟实验
#    - 录入 5+ 题库题目（exp_question）；确认 mb_quiz_config.default

# 7. 移动端操作
#    - gaoy 布置作业 → zhangxm 提交作品 → 答题
#    - yuanf 在实验详情发教研点评
#    - 注册家长并绑定孩子
```

**不再执行**：`mobile_demo_seed.sql`、`mobile_optional_demo_seed.sql`

---

## 4. 与前端 PROTOTYPE 的对照（优化 demo 数据）

执行本手册后，各页 **预期首屏**（`demo-fallback-enabled=false`）：

| 页面 | 无操作前 | 完成手册后 |
|------|----------|------------|
| TasksView | 空态 | gaoy 布置 ≥1 条，zhangxm 可见 |
| WorksView | 空态 | zhangxm 上传 ≥1 |
| BadgeWallView | 仅未获得勋章列表 | 完成行为后部分 earned |
| ContentDetailView | 0 留言，点赞来自 DB | 真实评论与 like_num |
| HomeParent | 空态或 pending | 绑定 approved 后有孩子统计 |

若页面仍出现「张小明 / demo-student」→ 检查 fallback 配置与 `mb_*` 是否仍有 demo id。

---

## 6. R0-1 验收（家长注册审核）

> 角色授权在 **管理后台 → 角色管理** 勾选「移动端管理」下菜单，**无需 SQL**。

```bash
# 1. 注册菜单（若未执行）
java -cp "backend/target/deps/*;sql/mobile" run_sql_file mobile_sys_menu.sql

# 2. 插入待审家长（R2 注册 API 完成前用于验收）
java -cp "backend/target/deps/*;sql/mobile" run_sql_file dev_r0_parent_pending_seed.sql
```

**浏览器验收**

1. 用 `admin` 登录管理后台 → **角色管理** → 为 `Sys_Admin`（及需要的 `School_Admin`）勾选「家长注册审核」
2. 刷新后进入 **移动端管理 → 家长注册审核**
3. 应看到 `13800138001` / 待审核
4. 点击 **通过** → 状态变「已通过」；或 **驳回** →「已驳回」
5. （可选）`bhy_admin` 登录：仅能看到 **同校** 待审家长

---

## 5. 完整性检查

```bash
javac -cp "backend/target/deps/*" sql/mobile/check_integrity.java -d sql/mobile/
java -cp "backend/target/deps/*;sql/mobile" check_integrity
```

确认：Tier 1 + feature 表存在；`mb_*` 无 `demo-%` user_id；业务表可为空（勋章/进度由 R0 管理端或联调操作产生）。
