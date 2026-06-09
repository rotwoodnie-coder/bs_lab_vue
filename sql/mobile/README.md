# sql/mobile · 脚本说明

> 库：`bs_exp_vue`（基线见 `sql/bs_exp_vue.sql`）  
> 决策详情：`docs/mobile-sql-decision.md` · 文档索引：`docs/README.md`

## 重要约定

**联调/生产库通常已有业务表，开发前必须先核对实库，再决定复用或 ALTER。**

- **禁止**在未核查的情况下为同一业务再建平行表。
- 例如：家长绑定已用 **`mb_parent_child`**，审核、状态、班级仅通过 **ALTER 增列**（见 `mb_parent_child_audit_alter.sql`），Entity 为 `MbParentChild`。
- `mobile_required_tables.sql` 中的 `CREATE TABLE IF NOT EXISTS` 仅用于**空库初始化**；已有表时只跑 **ALTER / 回填** 脚本。

## 必跑（移动端功能正常化）

| 文件 | 内容 |
|------|------|
| **`mobile_required_tables.sql`** | Tier 1：7 张核心表 |
| **`mobile_feature_tables.sql`** | Tier 1.5：勋章 + 社交 |
| **`mb_parent_child_audit_alter.sql`** | 绑定审核字段（规格 9.5，**ALTER 已有 mb_parent_child**） |
| `mb_parent_child_class_org_id_alter.sql` | 仅补 `class_org_id` 列（审核列已有时） |
| `mb_parent_child_backfill_class_org_id.sql` | 回填历史 pending 的 `class_org_id`（可选） |
| `mb_comment_soft_delete_alter.sql` | 仅旧库 comment 表缺 deleted_* 时 |
| **`mobile_sys_menu.sql`** | R0 管理端菜单（**不含角色授权**；授权在管理后台角色管理完成） |
| `dev_r0_parent_pending_seed.sql` | **R0-1 验收用**：插入 1 条 `status=t` 待审家长（非生产） |
| `mobile_badge_def_seed.sql` | dev 可选；**生产由管理端配置** |

```bash
mysql ... bs_exp_vue < sql/mobile/mobile_required_tables.sql
mysql ... bs_exp_vue < sql/mobile/mobile_feature_tables.sql
mysql ... bs_exp_vue < sql/mobile/mb_parent_child_audit_alter.sql
```

## 工具脚本

| 文件 | 用途 |
|------|------|
| `migrate_mb_to_sys_user.sql` | 过渡：改绑 demo id（**已 truncate 则不需要**） |
| `truncate_mb_demo_data.sql` | **清空 mb 业务/demo 数据**（保留表结构） |
| `run_sql_file.java` | JDBC 执行 SQL 脚本（无 mysql CLI 时用） |
| `check_integrity.java` | 连库检查表/索引 |

## 不再推荐

`mobile_demo_seed.sql` · `mobile_optional_demo_seed.sql` · `mobile_optional_demo_tables.sql`

## 已删除（勿再使用）

`mobile_home_init.sql` · `mobile_core_tables.sql` · `mobile_core_seed.sql` · `mobile_quiz_daily.sql`
