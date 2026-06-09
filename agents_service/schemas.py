"""
数据模式 — 请求/响应 Schema
"""

from __future__ import annotations

from enum import Enum
from typing import Any, Optional, Literal

from pydantic import BaseModel, Field, ValidationError


# ─── 教学阶段枚举 ─────────────────────────────────────

class TeachingStage(str, Enum):
    """石头老师对话环节状态机。"""
    INIT = "INIT"
    GOAL = "GOAL"
    MATERIAL = "MATERIAL"
    STEP = "STEP"
    RECORD = "RECORD"
    CONCLUSION = "CONCLUSION"
    FINAL = "FINAL"


# ─── LLM 别名 → 标准阶段名称的模糊映射 ────────────────
# LLM 经常输出语义相同但名称不同的阶段名，此处统一映射。

_STAGE_ALIASES: dict[str, TeachingStage] = {
    # GOAL 相关
    "PURPOSE": TeachingStage.GOAL,
    "OBJECTIVE": TeachingStage.GOAL,
    "AIM": TeachingStage.GOAL,
    "TARGET": TeachingStage.GOAL,
    "GOALS": TeachingStage.GOAL,
    # MATERIAL 相关
    "PREPARE": TeachingStage.MATERIAL,
    "PREPARATION": TeachingStage.MATERIAL,
    "MATERIALS": TeachingStage.MATERIAL,
    "TOOLS": TeachingStage.MATERIAL,
    "EQUIPMENT": TeachingStage.MATERIAL,
    # STEP 相关
    "PROCEDURE": TeachingStage.STEP,
    "STEPS": TeachingStage.STEP,
    "METHOD": TeachingStage.STEP,
    "PROCESS": TeachingStage.STEP,
    # RECORD 相关
    "OBSERVATION": TeachingStage.RECORD,
    "OBSERVE": TeachingStage.RECORD,
    "RECORDS": TeachingStage.RECORD,
    "DATA": TeachingStage.RECORD,
    "NOTE": TeachingStage.RECORD,
    # CONCLUSION 相关
    "RESULT": TeachingStage.CONCLUSION,
    "RESULTS": TeachingStage.CONCLUSION,
    "FINDING": TeachingStage.CONCLUSION,
    "FINDINGS": TeachingStage.CONCLUSION,
    "DISCUSSION": TeachingStage.CONCLUSION,
    "ANALYSIS": TeachingStage.CONCLUSION,
    # FINAL 相关
    "WRAPUP": TeachingStage.FINAL,
    "WRAP_UP": TeachingStage.FINAL,
    "SUMMARY": TeachingStage.FINAL,
    "REFLECTION": TeachingStage.FINAL,
    "END": TeachingStage.FINAL,
    "COMPLETE": TeachingStage.FINAL,
    "COMPLETION": TeachingStage.FINAL,
}


# ─── 石头老师结构化输出（class 定义必须在函数之前） ──

class StoneTeacherResponse(BaseModel):
    """石头老师回复的结构化输出。"""
    reply: str = Field(..., description="石头老师对学生本轮输入的自然语言回复")
    inferred_stage: Optional[TeachingStage] = Field(
        default=None,
        description="基于本轮对话语义推断的下一环节",
    )
    extracted_title: Optional[str] = Field(
        default=None,
        description="从学生对话中提取的实验标题",
    )
    inferred_grade: Optional[str] = Field(
        default=None,
        description="从学生对话中推断的年级段：低段/中段/高段",
    )
    is_stage_ready_to_advance: bool = Field(
        default=False,
        description="当前阶段是否已充分讨论，可以推进到下一阶段",
    )


# ─── 归一化工具函数 ───────────────────────────────────


def normalize_stage_strict(stage_value: Any) -> Any:
    """严格的阶段值归一化。

    处理各种 LLM 输出异常：
    - 大小写不敏感（"purpose" → "PURPOSE" → GOAL）
    - 别名映射（"PURPOSE" → GOAL）
    - 包含关系模糊匹配
    - None/空值→返回原值
    """
    if stage_value is None:
        return stage_value

    raw = str(stage_value).strip().upper()

    # 直接命中枚举值
    for stage in TeachingStage:
        if stage.value == raw:
            return stage

    # 别名映射
    if raw in _STAGE_ALIASES:
        return _STAGE_ALIASES[raw]

    # 包含关系模糊匹配
    for alias, canonical in _STAGE_ALIASES.items():
        if alias in raw or raw in alias:
            return canonical
    for stage in TeachingStage:
        if stage.value in raw or raw in stage.value:
            return stage

    # 完全无法映射，返回原值
    return stage_value


def robust_parse_stone_teacher_response(raw_dict: dict[str, Any]) -> StoneTeacherResponse:
    """鲁棒解析石头老师 LLM 输出。

    在 model_validate 之前预处理：
      1. inferred_stage → 别名映射 + 大小写归一化
      2. 无法映射的 stage 置为 null
    """
    cleaned = dict(raw_dict)

    raw_stage = cleaned.get("inferred_stage")
    if raw_stage is not None:
        normalized = normalize_stage_strict(raw_stage)
        if isinstance(normalized, TeachingStage):
            cleaned["inferred_stage"] = normalized.value
        else:
            import logging
            logging.getLogger("schemas").warning(
                "inferred_stage 无法映射: %s，已置为 null", raw_stage
            )
            cleaned["inferred_stage"] = None

    return StoneTeacherResponse.model_validate(cleaned)


# ─── 统一聊天协议 ─────────────────────────────────────

class ChatRequest(BaseModel):
    """统一聊天请求体。"""
    message: str = Field(..., min_length=1, max_length=8000, description="用户消息")
    role: str = Field(
        default="student",
        description="智能体角色: student/teacher/parent/researcher",
    )
    thread_id: Optional[str] = Field(
        default=None,
        description="会话 ID，用于多轮对话。新会话留空",
    )
    user_id: Optional[str] = Field(
        default=None,
        description="用户 ID。新会话若未传则自动生成",
    )
    user_name: str = Field(
        default="",
        max_length=64,
        description="用户姓名（个性化称呼）",
    )


class ChatResponse(BaseModel):
    """统一聊天响应体。"""
    message: str = Field(..., description="智能体回复")
    thread_id: str = Field(..., description="会话 ID")
    agent_type: str = Field(..., description="当前智能体类型")
