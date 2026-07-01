"""
实验方案设计智能体 — 单次生成 3 个结构化实验方案
"""

from __future__ import annotations

import json
import logging
from typing import Any, Optional, TypedDict, Annotated

from langgraph.graph import StateGraph, START, END, add_messages
from langchain_openai import ChatOpenAI
from langchain_core.messages import SystemMessage, AIMessage
from langgraph.checkpoint.base import BaseCheckpointSaver

from agents_framework.base_agent import BaseAgent
from agents_framework.output_engine import OutputEngine
from bs_lab_adapter.schemas.plan_design import PlanDesignResponse

logger = logging.getLogger("bs_lab_adapter.plan_design_graph")


class PlanDesignState(TypedDict):
    """方案设计状态。"""
    messages: Annotated[list, add_messages]
    user_name: str
    user_id: str
    session_id: str
    experiment_title: str
    grade_level: str
    plans: list       # list[Plan] 兜底用
    reply_content: str


SYSTEM_PROMPT_TEMPLATE = """# 角色定义
你是一位经验丰富的科学实验设计专家，专为小学生提供实验方案。

# 任务
根据学生提出的实验想法，生成 3 个不同的实验方案。
每个方案必须包含：方案名称、一句话介绍、所需材料、详细步骤、会观察到的现象、安全提示。

# 约束
1. 方案必须适合 {grade_level} 学生操作
2. 材料必须是家庭常见物品或容易获得的材料
3. 所有方案必须绝对安全，不能使用明火、腐蚀性化学品、高压电等危险品
4. 每步操作描述要清晰、具体，适合小学生理解
5. 必须有安全提示
6. 输出严格遵循指定的 JSON 格式

# 输出格式要求
你必须返回一个包含以下字段的纯 JSON 对象，不要有任何 Markdown 代码块包裹：
  1. "reply"：对学生问题的自然语言回复，说明你设计了 3 个方案
  2. "plans"：包含 3 个方案对象的数组，每个对象包含：
     - "plan_name": 方案名称（如"方案一：…"）
     - "description": 一句话介绍
     - "materials": 材料数组 [{{"name": "", "quantity": "", "tip": ""}}]
     - "steps": 步骤数组 ["第一步：…", "第二步：…"]
     - "what_you_see": 你会看到什么现象
     - "safety_tips": 安全提示数组

# 实验主题
{experiment_title}
"""


async def call_llm(state: PlanDesignState) -> dict[str, Any]:
    """调用 LLM 生成实验方案。"""
    messages = state.get("messages", [])
    if not messages:
        return {}

    experiment_title = state.get("experiment_title", "科学实验")
    grade_level = state.get("grade_level", "中段")

    from config import settings

    if not settings.llm_api_key:
        logger.error("LLM_API_KEY 未配置")
        return {"reply_content": "AI 服务暂未配置，请联系管理员设置 LLM_API_KEY。"}

    llm = ChatOpenAI(
        model=settings.llm_model,
        base_url=settings.llm_base_url,
        api_key=settings.llm_api_key,
        timeout=settings.llm_timeout_ms / 1000,
        temperature=0.6,
        max_tokens=8192,
    )

    system_prompt = SYSTEM_PROMPT_TEMPLATE.format(
        experiment_title=experiment_title,
        grade_level=grade_level,
    )

    full_messages = [SystemMessage(content=system_prompt)] + list(messages)

    # 使用 OutputEngine 生成结构化输出
    try:
        # 日志：发送到 LLM 的请求
        logger.info("LLM 请求 prompt=%.200s experiment_title=%s grade_level=%s",
                     system_prompt[:200], experiment_title, grade_level)
        result_dict = await OutputEngine.generate("plan_design", llm, full_messages)
        reply = result_dict.get("reply", "")
        plans = result_dict.get("plans", [])
        # 日志：LLM 返回
        logger.info("LLM 响应 reply=%.200s plans=%d",
                     reply[:200], len(plans))

        if not reply:
            reply = "我给你设计了 3 个实验方案，快来看看吧！"

        reply_content = reply

        return {
            "reply_content": reply_content,
            "plans": plans,
            "messages": [AIMessage(content=reply)],
        }
    except Exception as e:
        logger.error("方案设计 LLM 调用失败: %s", e, exc_info=True)
        return {
            "reply_content": "抱歉，设计实验方案时遇到了小问题，请稍后再试哦～",
            "plans": [],
        }


def route_after_llm(state: PlanDesignState) -> str:
    """LLM 调用结束后直接结束。"""
    return END


def create_plan_design_graph(checkpointer: BaseCheckpointSaver = None):
    """创建方案设计 LangGraph。

    Args:
        checkpointer: MySQLSaver / TTLMemorySaver 实例（可选）

    Returns:
        编译后的 StateGraph
    """
    # 注册结构化输出 Schema
    OutputEngine.register("plan_design", PlanDesignResponse)

    nodes = {
        "llm_call": call_llm,
    }

    conditional_edges = [
        ("llm_call", route_after_llm, {END: END}),
    ]

    graph = BaseAgent.create(
        state_schema=PlanDesignState,
        nodes=nodes,
        edges=[],
        conditional_edges=conditional_edges,
        checkpointer=checkpointer,
        first_node="llm_call",
    )

    return graph
