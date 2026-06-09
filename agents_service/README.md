# BS Lab LangGraph Agent Service

**bs-lab-agents** — 基于 Python FastAPI + LangGraph 的 AI 智能体服务平台。

当前版本：**1.0.0** | Python：**>= 3.11**

---

## 目录

- [1. 架构概览](#1-架构概览)
- [2. 前置条件](#2-前置条件)
- [3. 环境配置](#3-环境配置)
- [4. 启动服务](#4-启动服务)
- [5. API 接口](#5-api-接口)
- [6. 调用示例](#6-调用示例)
- [7. 流式输出（SSE）格式](#7-流式输出sse格式)
- [8. 优雅降级机制](#8-优雅降级机制)
- [9. 常见问题](#9-常见问题)

---

## 1. 架构概览

```
┌─────────────┐     HTTP/SSE     ┌─────────────────────────────┐
│  前端/客户端  │ ──────────────> │   agents_service (:5001)    │
│  (Vue / curl) │ <────────────── │  FastAPI + LangGraph        │
└─────────────┘                  │  │                           │
                                  │  ├── student (石头老师)      │
                                  │  │   └─ 三节点 LangGraph     │
                                  │  │      analyze→prompt→llm   │
                                  │  │                           │
                                  │  └── Checkpointer            │
                                  │       ├─ MySQLSaver (推荐)    │
                                  │       └─ TTLMemorySaver      │
                                  └──────────┬──────────────────┘
                                             │
                                  ┌──────────▼──────────┐
                                  │    MySQL 数据库       │
                                  │  ├─ ai_chat_session  │
                                  │  ├─ ai_chat_message  │
                                  │  ├─ langgraph_checkpoints      │
                                  │  └─ langgraph_checkpoint_writes │
                                  └─────────────────────┘
```

### 当前注册的智能体

| 角色 | 名称 | 说明 |
|---|---|---|
| `student` | 石头老师 | 面向小学科学实验教学（1-6年级），引导式教师助手 |

### 技术栈

| 组件 | 技术 |
|---|---|
| Web 框架 | FastAPI + uvicorn |
| AI 框架 | LangGraph (v1.2) + LangChain |
| LLM | DeepSeek Chat（OpenAI 兼容 API） |
| 数据库 | MySQL 8.0+（aiomysql 异步驱动） |
| Checkpointer | MySQLSaver（自有实现） |

---

## 2. 前置条件

| 依赖 | 版本要求 | 说明 |
|---|---|---|
| Python | >= 3.11 | 运行环境 |
| pip | — | Python 包管理器 |
| MySQL | 8.0+ | 业务数据 + 检查点持久化 |
| DeepSeek API Key | — | LLM 调用密钥 |

---

## 3. 环境配置

服务使用环境变量配置，通过 `.env.local` 文件加载。

### 3.1 配置项说明

| 变量名 | 必填 | 默认值 | 说明 |
|---|---|---|---|
| `LLM_API_KEY` | **是** | — | DeepSeek API 密钥 |
| `LLM_BASE_URL` | 否 | `https://api.deepseek.com/v1` | LLM API 地址 |
| `LLM_MODEL` | 否 | `deepseek-chat` | 模型名称 |
| `LLM_TIMEOUT_MS` | 否 | `60000` | LLM 请求超时（毫秒） |
| `DATABASE_URL` | 推荐 | — | MySQL 连接（业务数据 + checkpointer） |
| `CHECKPOINTER_URL` | 否 | 复用 `DATABASE_URL` | 独立 checkpointer 实例（可选） |
| `HOST` | 否 | `0.0.0.0` | 监听地址 |
| `PORT` | 否 | `5001` | 监听端口 |
| `LOG_LEVEL` | 否 | `info` | 日志级别 |

### 3.2 配置示例

```env
# ── LLM ──
LLM_API_KEY=sk-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
LLM_BASE_URL=https://api.deepseek.com/v1
LLM_MODEL=deepseek-chat
LLM_TIMEOUT_MS=60000

# ── MySQL 数据库（单一数据库部署） ──
DATABASE_URL=mysql+aiomysql://root:password@10.0.181.204:13306/bs_exp_data

# ── 服务 ──
HOST=0.0.0.0
PORT=5001
LOG_LEVEL=info
```

> **注意**：`DATABASE_URL` 使用 `mysql://` 前缀也可以，启动时会被自动转换为 `mysql+aiomysql://`。

### 3.3 环境变量加载顺序

1. `../.env.local`（优先）
2. `.env.local`
3. `.env`

---

## 4. 启动服务

### 4.1 安装依赖

```bash
cd agents_service
pip install -r requirements.txt
```

### 4.2 方式一：直接运行（推荐）

```bash
python main.py
```

等价于：

```bash
uvicorn main:app --host 0.0.0.0 --port 5001 --reload
```

### 4.3 方式二：Debug 模式（IDE）

在 `.vscode/launch.json` 中添加：

```json
{
  "version": "0.2.0",
  "configurations": [
    {
      "name": "Agents Service",
      "type": "python",
      "request": "launch",
      "module": "uvicorn",
      "args": [
        "main:app",
        "--host", "0.0.0.0",
        "--port", "5001",
        "--reload"
      ],
      "cwd": "${workspaceFolder}/agents_service",
      "envFile": "${workspaceFolder}/agents_service/.env.local"
    }
  ]
}
```

### 4.4 验证启动

启动后日志输出示例：

```
MySQL checkpointer 引擎已创建 (pool_size=5, max_overflow=10)
MySQL checkpointer 数据表已就绪 (langgraph_checkpoints, langgraph_checkpoint_writes)
MySQL checkpointer 初始化完成
已注册 agent: student（石头老师）
Agent 服务启动完成，可用角色: ['student']
端口: 5001, LLM: deepseek-chat @ https://api.deepseek.com/v1
```

验证服务状态：

```bash
curl http://localhost:5001/health
```

预期返回：

```json
{
  "status": "ok",
  "service": "BS Lab LangGraph Agent Service",
  "version": "1.0.0",
  "checks": {
    "agent_count": 1,
    "agents": ["student"],
    "llm_configured": true,
    "llm_model": "deepseek-chat",
    "checkpointer_type": "MySQLSaver",
    "db_configured": true
  }
}
```

---

## 5. API 接口

### 5.1 全局接口

#### `GET /health` — 健康检查

返回服务状态和各组件健康信息。

#### `GET /v1/agents` — 角色列表

返回当前注册的所有智能体角色名。

### 5.2 聊天接口

#### `POST /v1/agents/{role}/chat` — 非流式聊天

**路径参数：**

| 参数 | 类型 | 说明 |
|---|---|---|
| `role` | string | 智能体角色名，当前仅支持 `student` |

**请求体（JSON）：**

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|---|---|---|---|---|
| `message` | string | **是** | — | 用户消息（1-8000 字符） |
| `thread_id` | string | 否 | 自动生成 | 会话 ID，用于多轮对话记忆 |
| `user_name` | string | 否 | `""` | 用户名（个性化称呼） |
| `user_id` | string | 否 | `thread_id` | 用户标识 |
| `grade_level` | string | 否 | `null` | 年级段：`低段`/`中段`/`高段` |

**响应：**

| 字段 | 类型 | 说明 |
|---|---|---|
| `message` | string | AI 回复内容 |
| `thread_id` | string | 会话 ID（透传或新生成） |

#### `POST /v1/agents/{role}/chat/stream` — 流式（SSE）聊天

参数同非流式接口，但响应为 [SSE (Server-Sent Events)](https://html.spec.whatwg.org/multipage/server-sent-events.html) 格式。

---

## 6. 调用示例

### 6.1 健康检查

```bash
curl http://localhost:5001/health
```

### 6.2 角色列表

```bash
curl http://localhost:5001/v1/agents
```

### 6.3 非流式聊天（新会话）

```bash
curl -X POST http://localhost:5001/v1/student/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "老师，我想做一个关于水的压强的实验",
    "user_name": "小明",
    "grade_level": "高段"
  }'
```

### 6.4 非流式聊天（多轮对话）

第一次调用（新会话）：

```bash
curl -X POST http://localhost:5001/v1/student/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "我想探究水的压强",
    "user_name": "小明",
    "user_id": "user_001"
  }'
```

返回示例：

```json
{
  "message": "好的，小明同学！今天我们来探究水的压强。首先，你觉得水的压强可能和什么有关系呢？",
  "thread_id": "a1b2c3d4e5f6..."
}
```

第二次调用（传入 `thread_id` 继续对话）：

```bash
curl -X POST http://localhost:5001/v1/student/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "我觉得可能跟水的深度有关系",
    "thread_id": "a1b2c3d4e5f6...",
    "user_name": "小明",
    "user_id": "user_001"
  }'
```

### 6.5 流式聊天（SSE）

```bash
curl -N -X POST http://localhost:5001/v1/student/chat/stream \
  -H "Content-Type: application/json" \
  -d '{
    "message": "老师，我想做一个关于水的压强的实验",
    "user_name": "小明",
    "grade_level": "高段"
  }'
```

### 6.6 使用 `x-trace-id` 追踪请求

```bash
curl -X POST http://localhost:5001/v1/student/chat \
  -H "Content-Type: application/json" \
  -H "x-trace-id: my-trace-001" \
  -d '{
    "message": "我想探究水的压强"
  }'
```

---

## 7. 流式输出（SSE）格式

流式接口使用 [SSE (Server-Sent Events)](https://html.spec.whatwg.org/multipage/server-sent-events.html) 协议，每个事件以 `data:` 开头，以 `\n\n` 结束。

### 7.1 事件类型

#### meta 事件（首个事件）

```json
data: {"type": "meta", "session_id": "a1b2c3d4e5f6..."}
```

#### token 事件（逐字内容）

```json
data: {"content": "好"}
data: {"content": "的"}
data: {"content": "，"}
data: {"content": "小"}
data: {"content": "明"}
data: {"content": "同"}
data: {"content": "学"}
data: {"content": "！"}
// ...
```

前端拼接方式：

```javascript
let fullReply = "";
const eventSource = ...;
eventSource.onmessage = (event) => {
  if (event.data === "[DONE]") return;
  const data = JSON.parse(event.data);
  if (data.type === "meta") {
    console.log("Session ID:", data.session_id);
  } else if (data.content) {
    fullReply += data.content;
    // 更新 UI
  }
};
```

#### stage 事件（环节变更）

```json
data: {"type": "stage", "stage": "MATERIAL", "grade_level": "高段"}
```

各环节含义：

| 阶段 | 说明 |
|---|---|
| `INIT` | 初始阶段，了解学生需求 |
| `GOAL` | 明确实验目标 |
| `MATERIAL` | 讨论实验材料和工具 |
| `STEP` | 讨论实验步骤 |
| `RECORD` | 讨论如何记录实验数据 |
| `CONCLUSION` | 得出结论 |
| `FINAL` | 实验完成总结 |

#### error 事件（异常）

```json
data: {"type": "error", "data": "错误信息摘要"}
```

#### done 事件（结束标记）

```text
data: [DONE]
```

### 7.2 完整流式响应示例

```
data: {"type": "meta", "session_id": "abc123..."}

data: {"content": "好"}
data: {"content": "的"}
data: {"content": "！"}
data: {"content": "我"}
data: {"content": "们"}
data: {"content": "今"}
data: {"content": "天"}
data: {"content": "..."}
...
data: {"type": "stage", "stage": "MATERIAL", "grade_level": "高段"}

data: [DONE]
```

---

## 8. 优雅降级机制

服务具备多层降级能力，任一组件不可用时不影响核心 AI 对话功能：

| 组件 | 降级行为 |
|---|---|
| **MySQL** | Checkpointer 降级为 `TTLMemorySaver`（内存模式，1h TTL）；聊天历史不可持久化 |
| **LLM API** | 自动重试 → 降级为纯文本模式 → 兜底静态模板回复 |
| **JSON 解析** | 结构化输出失败 → 纯文本提取 → 静态模板回复 |
| **幂等拦截** | 同 thread_id + 同消息 30 秒内重复请求不会重复处理 |

---

## 9. 常见问题

### Q1: 启动后提示 `数据库引擎初始化失败`

检查 `DATABASE_URL` 配置是否正确，MySQL 服务是否可达。MySQL 不可用时代理会降级为非持久化模式继续运行。

### Q2: 启动后提示 `MySQL checkpointer 初始化失败`

同上 — 检查 MySQL 连接。Checkpointer 会自动降级为 `TTLMemorySaver`（内存模式），服务仍可正常对话，但重启后会话状态会丢失。

### Q3: `400` 错误

```
{"detail": "message 不能为空"}
```

检查请求体是否包含 `message` 字段且不为空。

### Q4: `404` 错误

```
{"detail": "未知角色: xxx"}
```

检查 `role` 路径参数是否正确，调用 `GET /v1/agents` 查看可用角色列表。

### Q5: 如何查看正在使用的 checkpointer 类型？

```bash
curl http://localhost:5001/health
```

返回的 `checks.checkpointer_type` 字段显示当前使用的 checkpointer：

- `MySQLSaver` — MySQL 持久化
- `TTLMemorySaver` — 内存模式（带 TTL 自动清理）

### Q6: 如何创建新的聊天会话？

不传 `thread_id` 即可，服务会自动生成新的 `thread_id`。保存返回的 `thread_id` 可用于后续多轮对话。

### Q7: 如何清理 MySQL 中的检查点数据？

```sql
-- 查看检查点数据量
SELECT COUNT(*) FROM langgraph_checkpoints;
SELECT COUNT(*) FROM langgraph_checkpoint_writes;

-- 清理所有检查点（慎用，会丢失所有未完成的会话状态）
-- DELETE FROM langgraph_checkpoint_writes;
-- DELETE FROM langgraph_checkpoints;

-- 清理特定会话的检查点
DELETE FROM langgraph_checkpoint_writes WHERE thread_id = 'xxx';
DELETE FROM langgraph_checkpoints WHERE thread_id = 'xxx';
```

---

## 附录

### 文件结构

```
agents_service/
├── main.py                          # 服务入口
├── config.py                        # 环境配置
├── schemas.py                       # 数据模式
├── models.py                        # ORM 模型
├── database.py                      # 数据库引擎
├── repository.py                    # 数据仓库
├── requirements.txt                 # Python 依赖
├── .env.local                       # 本地环境变量
├── .env.example                     # 环境变量模板
│
├── agents_framework/                # LangGraph 框架层
│   ├── base_agent.py                # Agent 基类
│   ├── server.py                    # FastAPI 路由工厂
│   ├── checkpointer.py              # Checkpointer 工厂
│   ├── mysql_checkpointer.py        # MySQLSaver（新版）
│   ├── sse.py                       # SSE 序列化
│   ├── errors.py                    # 错误模型
│   └── tool_registry.py             # 工具注册
│
└── bs_lab_adapter/                  # 业务适配层
    ├── safety.py                    # 安全过滤
    ├── graphs/
    │   └── student_graph.py         # 石头老师 LangGraph
    ├── tools/
    │   ├── experiment_db.py         # 实验数据库工具
    │   ├── curriculum.py            # 课程对齐工具
    │   └── report.py                # 报告生成工具
    └── prompts/
        ├── student.yaml
        ├── teacher.yaml
        ├── researcher.yaml
        └── parent.yaml
```
