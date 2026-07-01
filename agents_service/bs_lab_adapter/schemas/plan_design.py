"""
bs_lab_adapter.schemas.plan_design — 实验方案设计结构化输出模型
"""

from __future__ import annotations

from typing import Optional

from pydantic import BaseModel, Field


class Material(BaseModel):
    """实验材料。"""
    name: str = Field(..., description="材料名称")
    quantity: str = Field(..., description="数量")
    tip: Optional[str] = Field(None, description="替代物提醒")


class Plan(BaseModel):
    """单个实验方案。"""
    plan_name: str = Field(..., description="方案名称，如「方案一：…」")
    description: str = Field(..., description="一句话介绍")
    materials: list[Material] = Field(..., description="所需材料清单")
    steps: list[str] = Field(..., description="实验步骤，每一步一条")
    what_you_see: str = Field(..., description="你会看到的现象")
    safety_tips: list[str] = Field(..., description="安全提示列表")


class PlanDesignResponse(BaseModel):
    """方案设计回复。"""
    reply: str = Field(..., description="对用户问题的自然语言回复")
    plans: list[Plan] = Field(..., description="3 个实验方案")
