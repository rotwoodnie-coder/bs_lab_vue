"""
自由聊天智能体 — 无阶段约束的自由对话
"""

from __future__ import annotations

import logging
from typing import Any, Optional, TypedDict, Annotated

from langgraph.graph import StateGraph, START, END, add_messages
from langchain_openai import ChatOpenAI
from langchain_core.messages import SystemMessage, HumanMessage, AIMessage
from langgraph.checkpoint.base import BaseCheckpointSaver

from agents_framework.base_agent import BaseAgent

logger = logging.getLogger("bs_lab_adapter.free_chat_graph")


class FreeChatState(TypedDict):
    """自由聊天状态 — 最小状态，无阶段/安全/实验约束。"""
    messages: Annotated[list, add_messages]
    user_name: str
    user_id: str
    session_id: str
    reply_content: str


SYSTEM_PROMPT = (
    "你是一个友好的AI科学助手，可以回答任何科学问题。"
    "回答要生动有趣，适合小学生理解。"
)


async def call_llm(state: FreeChatState) -> dict[str, Any]:
    """调用 LLM 生成回复。"""
    messages = state.get("messages", [])
    if not messages:
        return {}

    from config import settings

    if not settings.llm_api_key:
        logger.error("LLM_API_KEY 未配置")
        return {"messages": [AIMessage(content="AI 服务暂未配置，请联系管理员设置 LLM_API_KEY。")]}

    llm = ChatOpenAI(
        model=settings.llm_model,
        base_url=settings.llm_base_url,
        api_key=settings.llm_api_key,
        timeout=settings.llm_timeout_ms / 1000,
        temperature=0.7,
        max_tokens=4096,
    )

    # 构建完整消息列表：SystemMessage + 历史消息
    full_messages = [SystemMessage(content=SYSTEM_PROMPT)] + list(messages)

    try:
        # 日志：发送到 LLM 的消息
        logger.info("LLM 请求 messages=%s", [(m.type, str(m.content)[:100]) for m in full_messages])
        response = await llm.ainvoke(full_messages)
        reply_text = response.content.strip() if response.content else ""
        # 日志：LLM 返回内容
        logger.info("LLM 响应 reply=%s", reply_text[:200])
        if not reply_text:
            reply_text = "嗯…让我想想，你再问我一次好不好？"
    except Exception as e:
        logger.error("LLM 调用失败: %s", e, exc_info=True)
        reply_text = "抱歉，我暂时有点忙，请稍后再试哦～"

    # 姓名拼接
    user_name = state.get("user_name", "")
    has_assistant_history = any(
        isinstance(m, AIMessage) for m in state.get("messages", [])
    )
    if user_name and not has_assistant_history:
        reply_content = f"{user_name}你好呀！{reply_text}"
    elif user_name:
        reply_content = f"{user_name}，{reply_text}"
    else:
        reply_content = reply_text

    return {
        "messages": [AIMessage(content=reply_text)],
        "reply_content": reply_content,
    }


def route_after_llm(state: FreeChatState) -> str:
    """LLM 调用结束后直接结束。"""
    return END


def create_free_chat_graph(checkpointer: BaseCheckpointSaver = None):
    """创建自由聊天 LangGraph。

    Args:
        checkpointer: MySQLSaver / TTLMemorySaver 实例（可选）

    Returns:
        编译后的 StateGraph
    """
    nodes = {
        "llm_call": call_llm,
    }

    conditional_edges = [
        ("llm_call", route_after_llm, {END: END}),
    ]

    graph = BaseAgent.create(
        state_schema=FreeChatState,
        nodes=nodes,
        edges=[],
        conditional_edges=conditional_edges,
        checkpointer=checkpointer,
        first_node="llm_call",
    )

    return graph
