# BS Lab 智能体服务平台 — 架构设计

> 本项目是独立新建项目，不涉及历史兼容。以下所有设计均为最优选。

---

## 一、架构总览

```mermaid
flowchart TB
    Client[前端 / 移动端 / 管理后台]
    Client -->|POST /v1/chat| Router

    subgraph "路由层 [router.py]"
        Router[AgentRouter<br/>统一入口分发]
        Router --> Validator[AgentTypeValidator<br/>thread_id 一致性校验]
        Validator --> ContextInjector[ContextInjector<br/>注入跨 Agent 摘要]
        ContextInjector --> Safety
    end

    subgraph "安全层 [safety.py]"
        Safety[SemanticSafetyFilter]
        Safety -->|L1 关键词命中| L2[L2: Structured Output 判断]
        Safety -->|L1 未命中| Pass[直接放行]
        L2 -->|教学语境| PassWithTip[放行 + 安全提示]
        L2 -->|危险意图| Block[拦截]
    end

    subgraph "输出引擎 [output_engine.py]"
        PassWithTip --> Engine[OutputEngine.generate]
        Block --> DirectResponse[直接回复安全提示]
        Pass --> Engine
        Engine -->|一级: Structured Output| OK[返回结构化结果]
        Engine -->|二级: 纯文本| Text[降级]
        Engine -->|三级: 静态模板| Fallback[兜底]
    end

    subgraph "智能体实例 [LangGraph]"
        Engine --> ST["石头老师 (student)<br/>6 阶段引导"]
        Engine --> TA["教师助手 (teacher)<br/>备课/批改/分析"]
        Engine --> PA["家长助手 (parent)<br/>家庭实验推荐"]
        Engine --> RA["教研助手 (researcher)<br/>课标对齐/数据报告"]
    end

    ST & TA & PA & RA --> CP[Checkpointer<br/>MySQLSaver]
    CP -->|写入| MySQL_CK[(MySQL: checkpoints<br/>独立连接池)]
    ST & TA & PA & RA -->|读写业务数据| MySQL_BIZ[(MySQL: business<br/>独立连接池)]
```

---

## 二、核心组件设计

### 2.1 智能体路由层（AgentRouter）

**文件**：`agents_framework/router.py`（新增）

**入口**：单一路径 `POST /v1/chat`（没有 `/v1/{role}/chat`）

**请求体**：

```json
{
  "message": "我想做一个水的压强实验",
  "role": "student",
  "thread_id": null,
  "user_id": "u001",
  "user_name": "小明"
}
```

**路由规则**：

| 条件 | 路由到 | 说明 |
|---|---|---|
| `thread_id` 存在 → 查该 thread 的 `agent_type` | 该 Agent | 继续已有会话，必须校验 agent_type 一致性 |
| `thread_id` 为空 + `role` 存在 + role 已注册 | 按 role 创建新会话 | 正常新对话 |
| `thread_id` 为空 + `role` 为空 | `student`（默认） | 兜底 |

**AgentTypeValidator**：当 `thread_id` 存在时，从 checkpointer 读取该 thread 最后一次写入的 `agent_type` metadata，若与请求的 `role` 不一致，返回 `409 Conflict`：

```json
{
  "error": "thread_id xxxx 属于 teacher 智能体，无法路由到 student"
}
```

### 2.2 输出引擎（OutputEngine）

**文件**：`agents_framework/output_engine.py`（新增）

每个 Agent 只需要注册一次 Schema，引擎自动处理三级兜底：

```python
# 注册（一次性的，在 main.py 启动时完成）
OutputEngine.register("student", StoneTeacherResponse)
OutputEngine.register("teacher", TeacherResponse)

# 所有 Agent 统一调用（无需各自 try/except）
result = await OutputEngine.generate("student", llm, messages)
```

| 等级 | 方式 | 典型耗时 |
|---|---|---|
| 一级 | `with_structured_output(Schema)` | ~2s |
| 二级 | 纯文本 `llm.invoke()` | ~2s |
| 三级 | 静态模板字典兜底 | 0ms |

### 2.3 语义拦截层（SemanticSafetyFilter）

**文件**：`bs_lab_adapter/safety.py`（重构）

两级流水线，L2 使用 Structured Output 保证确定性：

```python
class SafetyJudgement(BaseModel):
    is_educational: bool
    reason: str
    recommended_action: Literal["pass_with_tip", "block"]
```

| 场景 | L1 关键词 | L2 判断 | 最终行为 |
|---|---|---|---|
| "我想做火的实验" | 命中"火" | `is_educational=true` | 放行 + "请在大人陪同下使用酒精灯" |
| "怎么配制盐酸溶液" | 命中"盐酸" | `is_educational=true` | 放行 + "盐酸需教师指导" |
| "怎么吸毒" | 命中"毒品" | 不触发 L2（直接拦截） | 拦截："这个话题不适合讨论" |
| "今天天气真好" | 未命中 | 不走 L2 | 正常放行 |

直接拦截的关键词（不走 L2）：`毒品, 吸毒, 炸弹, 鞭炮, 火药`

### 2.4 上下文缓存（ContextCache）

**文件**：`agents_framework/context_cache.py`（新增）

三层模型，跨 Agent 共享：

```
Layer 1: 用户画像  ── user_id → { grade_level }           永久  → MySQL
Layer 2: 会话状态  ── thread_id → { stage, experiment }    临时  → Checkpointer
Layer 3: 跨 Agent  ── user_id → { recent_topics }          TTL 24h → MySQL
```

role_access_mask 控制可见范围：

```python
ROLE_ACCESS_MASK = {
    "student":   {"layer3": "summary_only"},
    "parent":    {"layer3": "summary_only"},
    "teacher":   {"layer3": "full"},
    "researcher":{"layer3": "full"},
}
```

家长切入时只能看到"孩子正在做水的压强实验（MATERIAL 阶段）"的摘要，看不到完整的对话细节。

---

## 三、数据库设计

### 3.1 数据库实例（单一 MySQL）

```
MySQL 实例
├── 业务库: bs_exp_agents
│   ├── ai_chat_session          ← 会话元数据
│   └── ai_chat_message          ← 消息记录
│
└── 检查点库: bs_exp_checkpoints
    ├── langgraph_checkpoints          ← LangGraph 状态快照
    └── langgraph_checkpoint_writes    ← LangGraph 中间写入
```

配置：

```env
DATABASE_URL=mysql+aiomysql://user:pass@host:3306/bs_exp_agents
CHECKPOINTER_URL=mysql+aiomysql://user:pass@host:3306/bs_exp_checkpoints
```

### 3.2 业务表设计

**ai_chat_session**：

```sql
CREATE TABLE ai_chat_session (
    session_id       VARCHAR(32) PRIMARY KEY,
    user_id          VARCHAR(32) NOT NULL,
    user_role        VARCHAR(32) NOT NULL DEFAULT 'student',
    agent_type       ENUM('student','teacher','researcher','parent') NOT NULL DEFAULT 'student',
    grade_level      ENUM('低段','中段','高段') DEFAULT NULL,
    current_stage    ENUM('INIT','GOAL','MATERIAL','STEP','RECORD','CONCLUSION','FINAL') NOT NULL DEFAULT 'INIT',
    experiment_title VARCHAR(256) DEFAULT NULL,
    summary          TEXT DEFAULT NULL,
    is_active        ENUM('y','n') NOT NULL DEFAULT 'y',
    create_time      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB CHARSET=utf8mb4;
```

**ai_chat_message**：

```sql
CREATE TABLE ai_chat_message (
    message_id  BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id  VARCHAR(32) NOT NULL,
    role        ENUM('user','assistant','system') NOT NULL,
    content     TEXT NOT NULL,
    metadata    JSON DEFAULT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_session_id (session_id)
) ENGINE=InnoDB CHARSET=utf8mb4;
```

### 3.3 检查点数据库优化

LangGraph 的 checkpointer 写入是高频率操作，每次对话可能有几十次写入。在检查点库上执行：

```sql
SET GLOBAL innodb_flush_log_at_trx_commit = 2;
```

这允许事务日志每秒才刷盘一次，写入吞吐提升 5-10 倍。检查点数据可重新生成，丢失最后 1 秒的数据是可接受的。

---

## 四、完整调用流程

```
学生发送："石头老师，我想做水的压强实验"
       │
       ▼
[1] POST /v1/chat  { message, role: "student" }
       │
       ▼
[2] AgentRouter → thread_id 为空 → role=student → 创建新会话
       │
       ▼
[3] ContextInjector → 从 MySQL 查 user_id 最近活跃上下文 → 注入 grade_level
       │
       ▼
[4] SemanticSafetyFilter
    ├── L1: 检查关键词 → "水" "压强" "实验" 均未命中
    └── 放行
       │
       ▼
[5] OutputEngine.generate("student", llm, messages)
    ├── 一级: with_structured_output(StoneTeacherResponse) ✓
    └── 返回 { reply: "...", inferred_stage: "GOAL", ... }
       │
       ▼
[6] Checkpointer (MySQLSaver) 保存本轮状态
       │
       ▼
[7] 响应 → { message: "好的小明！你觉得水的压强...</", thread_id: "abc..." }
```

---

## 五、启动方式

```bash
# 1. 安装
pip install -r requirements.txt

# 2. 建业务表
mysql -h host -u root -p < scripts/init_db.sql

# 3. 启动（检查点表由 MySQLSaver.setup() 自动创建）
python main.py
```

启动日志：
```
AgentRouter: student / teacher / parent / researcher 已注册
OutputEngine: student → StoneTeacherResponse 已注册
MySQLSaver: checkpointer 就绪
服务已启动 → 0.0.0.0:5001
```

---

## 六、接口一览

| 方法 | 路径 | 说明 |
|---|---|---|
| `POST` | `/v1/chat` | **唯一入口**。请求体含 `message` + `role` + `thread_id` |
| `GET` | `/v1/agents` | 列出当前已注册的智能体 |
| `GET` | `/health` | 健康检查 |

示例：

```bash
# 首次对话
curl -X POST http://localhost:5001/v1/chat \
  -H "Content-Type: application/json" \
  -d '{"message":"水的压强","role":"student","user_name":"小明"}'

# 继续对话
curl -X POST http://localhost:5001/v1/chat \
  -H "Content-Type: application/json" \
  -d '{"message":"我觉得跟深度有关","role":"student","thread_id":"abc123"}'

# 家长查询
curl -X POST http://localhost:5001/v1/chat \
  -H "Content-Type: application/json" \
  -d '{"message":"今天学了什么","role":"parent","user_id":"u001"}'
```

---

## 七、优雅降级（全部可选）

| 组件不可用 | 降级行为 |
|---|---|
| MySQL 业务库 | 对话历史不持久化，不影响 AI 对话 |
| MySQL 检查点库 | 自动切换到 `TTLMemorySaver`（内存模式，1h TTL） |
| 两个 MySQL 都不可用 | 同时降级上两项，纯内存运行 |
| LLM API 异常 | 一级 Structured Output → 二级纯文本 → 三级静态模板 |

---

## 八、项目文件结构

```
agents_service/
├── main.py                          # 入口（Router + OutputEngine 注册）
├── config.py                        # 环境变量
├── schemas.py                       # Pydantic Schema
├── models.py                        # ORM 模型
├── database.py                      # 业务库引擎
├── repository.py                    # 数据仓库
├── requirements.txt
├── .env.local
│
├── agents_framework/
│   ├── __init__.py
│   ├── base_agent.py                # LangGraph 构建基类
│   ├── router.py                    # ★ AgentRouter + AgentTypeValidator
│   ├── output_engine.py             # ★ 结构化输出引擎（三级兜底）
│   ├── context_cache.py             # ★ 上下文缓存 + role_access_mask
│   ├── checkpointer.py              # ★ MySQLSaver + TTLMemorySaver（无 Postgres）
│   ├── mysql_checkpointer.py        # MySQLSaver 实现
│   ├── server.py                    # FastAPI 路由（仅 /v1/chat）
│   ├── sse.py                       # SSE 序列化 + heartbeat
│   └── errors.py                    # 错误模型
│
├── bs_lab_adapter/
│   ├── __init__.py
│   ├── safety.py                    # ★ SemanticSafetyFilter（L1+L2）
│   ├── graphs/
│   │   ├── student_graph.py         # 石头老师
│   │   ├── teacher_graph.py         # 教师助手（待实现）
│   │   ├── researcher_graph.py      # 教研助手（待实现）
│   │   └── parent_graph.py          # 家长助手（待实现）
│   ├── tools/
│   │   ├── experiment_db.py
│   │   ├── curriculum.py
│   │   └── report.py
│   └── prompts/
│       ├── student.yaml
│       ├── teacher.yaml
│       ├── parent.yaml
│       └── researcher.yaml
│
└── scripts/
    └── init_db.sql
```

加 ★ 标记的文件是新项目需要从零构建的核心模块。

---

## 九、与旧方案的关键差异

| 维度 | 旧方案（有兼容包袱） | 新方案（全新项目） |
|---|---|---|
| API 入口 | `POST /v1/{role}/chat` + `POST /v1/chat` 两套 | 只有 `POST /v1/chat` 一个入口 |
| Checkpointer | MySQLSaver + PostgresSaver 兼容 | 只用 MySQLSaver，无 Postgres |
| 结构化输出 | 每个 Agent 内联 `with_structured_output` | OutputEngine 统一管理 |
| 安全过滤 | 仅关键词 + Chat API（不确定） | L1 关键词 + L2 Structured Output（确定） |
| 上下文 | 无 | ContextCache + role_access_mask |
| Docker Compose | 含 Postgres 服务 | 不需要（无需额外基础设施） |
| 依赖 | 含 psycopg 等 | 仅为所需最小集 |
