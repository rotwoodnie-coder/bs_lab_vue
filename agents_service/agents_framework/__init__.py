"""
agents_framework — 通用 LangGraph Agent 框架层

提供：
- BaseAgent 基类（StateGraph 构建 + Checkpointer）
- ToolRegistry（通用 / 领域工具分层注册）
- Checkpointer 封装（MySQLSaver / TTLMemorySaver）
- AgentRouter（智能体路由层）
- OutputEngine（结构化输出引擎）
- ContextCache（上下文缓存层）
- SSE 序列化工具
- 标准 FastAPI 路由模板
- 标准错误模型
"""

from agents_framework.base_agent import BaseAgent
from agents_framework.tool_registry import ToolRegistry, AgentTool
from agents_framework.checkpointer import create_checkpointer, TTLMemorySaver
from agents_framework.mysql_checkpointer import MySQLSaver
from agents_framework.router import AgentRouter
from agents_framework.output_engine import OutputEngine
from agents_framework.context_cache import ContextCache
from agents_framework.server import create_agent_app
from agents_framework.errors import AgentError, classify_error

__all__ = [
    "BaseAgent",
    "ToolRegistry",
    "AgentTool",
    "create_checkpointer",
    "TTLMemorySaver",
    "MySQLSaver",
    "AgentRouter",
    "OutputEngine",
    "ContextCache",
    "create_agent_app",
    "AgentError",
    "classify_error",
]
