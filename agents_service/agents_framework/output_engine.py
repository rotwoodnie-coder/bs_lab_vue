"""
Output Engine — 结构化输出引擎

统一管理所有 Agent 的输出 Schema，提供三级兜底：
  一级: with_structured_output(Schema)
  二级: 纯文本 llm.ainvoke()
  三级: 静态模板
"""

from __future__ import annotations

import json
import logging
from typing import Any

from langchain_openai import ChatOpenAI
from pydantic import BaseModel

logger = logging.getLogger("agents_framework.output_engine")

# ─── 静态兜底模板 ──────────────────────────────────────

FALLBACK_TEMPLATES: dict[str, str] = {
    "student": "石头老师在想怎么回答你，你能再说说你的想法吗？",
    "teacher": "教师助手正在分析，请稍后再试。",
    "parent": "家长助手正在准备建议，请稍后再试。",
    "researcher": "教研助手正在处理，请稍后再试。",
}


# ─── OutputEngine ──────────────────────────────────────


class OutputEngine:
    """结构化输出引擎。

    用法:
        # 注册（启动时一次）
        OutputEngine.register("student", StoneTeacherResponse)

        # 使用（Agent 内统一调用）
        result = await OutputEngine.generate("student", llm, messages)
        reply = result["reply"]
    """

    _schemas: dict[str, type[BaseModel]] = {}

    @classmethod
    def register(cls, agent_type: str, schema: type[BaseModel]) -> None:
        """注册 Agent 的输出 Schema。"""
        cls._schemas[agent_type] = schema
        logger.info("OutputEngine 注册: %s → %s", agent_type, schema.__name__)

    @classmethod
    async def generate(
        cls,
        agent_type: str,
        llm: ChatOpenAI,
        messages: list,
        trace_id: str = "",
    ) -> dict[str, Any]:
        """统一生成结构化输出。

        三级兜底：
          1. with_structured_output(Schema)  ← 结构化输出
          2. llm.ainvoke()                   ← 纯文本降级
          3. FALLBACK_TEMPLATES[agent_type]   ← 静态模板

        Returns:
            dict — 至少包含 "reply" 键
        """
        schema = cls._schemas.get(agent_type)

        # ── 一级：Structured Output ──
        if schema:
            try:
                structured_llm = llm.with_structured_output(schema)
                result = await structured_llm.ainvoke(messages)
                if hasattr(result, "model_dump"):
                    return result.model_dump()
                return {"reply": str(result)}
            except Exception as e:
                logger.warning(
                    "[%s] 一级结构化输出失败: %s，降级到纯文本", agent_type, e
                )

        # ── 二级：纯文本 ──
        try:
            response = await llm.ainvoke(messages)
            content = response.content.strip() if response.content else ""
            if content:
                # 尝试从纯文本提取 JSON 结构的 reply 字段
                if content.startswith("{"):
                    try:
                        parsed = json.loads(content)
                        if "reply" in parsed:
                            return parsed
                    except json.JSONDecodeError:
                        pass
                return {"reply": content}
        except Exception as e:
            logger.error("[%s] 二级纯文本也失败: %s，使用静态模板", agent_type, e)

        # ── 三级：静态模板 ──
        return {"reply": FALLBACK_TEMPLATES.get(agent_type, "请稍后再试。")}
