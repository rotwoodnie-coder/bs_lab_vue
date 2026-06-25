# 移动端文档索引

> 更新：2026-06-08  
## 维护原则

详表只写一处；其余文档用链接，避免重复（省 token、防漂移）。  
**改代码后同步文档** → [.cursor/rules/docs-sync-on-code-change.mdc](../.cursor/rules/docs-sync-on-code-change.mdc)

## 权威来源（查什么看哪份）

| 查什么 | 只看这一份 |
|--------|------------|
| `mb_*` 表结构、脚本、必要性 | **[mobile-sql-decision.md](./mobile-sql-decision.md)** |
| 产品规则、审核状态机 | **[mobile-product-spec.md](./mobile-product-spec.md)** |
| API 是否存在 | **代码** `backend/.../mobile/controller/Mobile*Controller.java`（不在 doc 重复列表） |
| 联调造数 | **[mobile-test-data-playbook.md](./mobile-test-data-playbook.md)** |
| SQL 怎么跑 | **[../sql/mobile/README.md](../sql/mobile/README.md)** |
| **服务器部署** | **[server-deployment.md](./server-deployment.md)** |

## 辅助文档

| 文档 | 用途 | 新鲜度 |
|------|------|--------|
| [mobile-dev-plan.md](./mobile-dev-plan.md) | 架构原则、分期、Mock 策略 | 已勘误；API 细节以代码为准 |
| [mobile-data-source-audit.md](./mobile-data-source-audit.md) | 页面↔数据源对照 | ⚠️ 部分路由过时，见文首说明 |
| [mobile-no-mock-analysis.md](./mobile-no-mock-analysis.md) | 去 Mock 分析 | 参考 |
| [mobile-no-mock-execution-plan.md](./mobile-no-mock-execution-plan.md) | Phase  checklist | 📦 已归档，仅历史参考 |
| [../frontend/mobile/DEVELOPMENT_PLAN.md](../frontend/mobile/DEVELOPMENT_PLAN.md) | 原型对照 | 📦 已停更，见本索引 |

**库基线**：`sql/bs_exp_vue.sql`（无 `mb_*`；移动端表见 sql-decision）

## SQL 快速入口

空库 / 已有库 **完整步骤与增量脚本** → [mobile-sql-decision.md §9](./mobile-sql-decision.md#9-执行顺序) 与 [sql/mobile/README.md](../sql/mobile/README.md)

```bash
# 空库最小集（3 文件；其余增量见 sql-decision §9）
mysql ... bs_exp_vue < sql/mobile/mobile_required_tables.sql
mysql ... bs_exp_vue < sql/mobile/mobile_feature_tables.sql
mysql ... bs_exp_vue < sql/mobile/mb_parent_child_audit_alter.sql
```

**完整性检查**：`sql/mobile/check_integrity.java`（用法见 sql/mobile/README.md）

## 2026-06 实现补充（规格未单独成章的）

- **绑定审核**：教师移动端 `/parent-binds`（`GET/PATCH /api/mobile/teacher/parent-binds`），共用 `mb_parent_child`；管理端审核为并行能力（见 product-spec §八）。
- **教师 UX**：与学生同底栏；AI 统一 `/chat`（无 `/teacher-ai`）。
- **每日答题**：`mb_quiz_config` + `exp_question`；`mb_quiz_daily` 已废弃（见 sql-decision §3.7）。
