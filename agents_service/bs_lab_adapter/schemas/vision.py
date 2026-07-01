"""
bs_lab_adapter.schemas.vision — 视觉分析服务请求/响应模型
"""

from __future__ import annotations

from typing import Any, Optional

from pydantic import BaseModel, Field


class VisionReviewRequest(BaseModel):
    """图片审查请求。"""

    image_base64: str = Field(
        ...,
        description="Base64 编码的图片数据（不含 data URL 前缀）",
    )
    context: Optional[dict[str, Any]] = Field(
        None,
        description="可选上下文信息，如实验名称、当前步骤等",
    )


class VisionDetails(BaseModel):
    """图片分析明细。"""

    objects_detected: list[str] = Field(
        default_factory=list,
        description="检测到的实验设备或物品列表",
    )
    phenomena: list[str] = Field(
        default_factory=list,
        description="可观察到的实验现象",
    )
    safety_issues: list[str] = Field(
        default_factory=list,
        description="安全隐患列表",
    )
    compliance_issues: list[str] = Field(
        default_factory=list,
        description="合规问题列表",
    )
    suggestions: list[str] = Field(
        default_factory=list,
        description="改进建议",
    )


class VisionReviewResponse(BaseModel):
    """图片审查结果。"""

    summary: str = Field(..., description="总体分析摘要")
    details: VisionDetails = Field(
        default_factory=VisionDetails,
        description="分析明细",
    )
    overall_rating: str = Field(
        default="未分析",
        description='总体评价：合格 / 需改进 / 不合格 / 未分析',
    )
    is_safe: Optional[bool] = Field(
        default=None,
        description="是否安全：True 安全 / False 不安全 / None 无法判断",
    )
