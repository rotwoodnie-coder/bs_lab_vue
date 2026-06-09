"""
SemanticSafetyFilter — 语义安全拦截层

两级过滤：
  L1: 关键词快速过滤（<1ms）
  L2: LLM Structured Output 语义判断（仅 L1 命中时触发，确定性输出）

使用方式：
    filter = SemanticSafetyFilter(llm)
    result = await filter.check("我想用酒精灯做实验")
    # result.pass_ → True, result.hint → "请在教师陪同下使用酒精灯"
"""

from __future__ import annotations

import logging
from typing import Literal

from langchain_openai import ChatOpenAI
from pydantic import BaseModel, Field

logger = logging.getLogger("bs_lab_adapter.safety")

# ─── L1 关键词 ─────────────────────────────────────────

# 需要 L2 语义判断的词（教学场景可能合法）
_SEMANTIC_KEYWORDS: list[str] = [
    "火", "明火", "点燃", "燃烧", "酒精灯", "蜡烛",
    "盐酸", "硫酸", "腐蚀",
    "刀", "注射器",
    "高压电", "220V", "插座", "电线",
]

# 直接拦截的词（即使教学语境也不应出现，不走 L2）
_BLOCK_KEYWORDS: list[str] = [
    "毒品", "吸毒", "注射毒品",
    "炸弹", "鞭炮", "火药",
]


# ─── L2 Structured Output Schema ───────────────────────


class SafetyJudgement(BaseModel):
    """安全语义判断的输出契约（确定性的）。"""
    is_educational: bool = Field(
        ...,
        description="消息中的危险词汇是否属于合法教学/实验语境。true=教学语境, false=非教学",
    )
    reason: str = Field(
        ...,
        max_length=200,
        description="判断依据的简短说明",
    )
    recommended_action: Literal["pass_with_tip", "block"] = Field(
        ...,
        description="pass_with_tip=附带安全提示放行, block=拦截",
    )


# ─── 安全提示模板 ──────────────────────────────────────

_SAFETY_TIPS: dict[str, str] = {
    "火": "请在老师或家长的陪同下进行与火相关的实验，注意用火安全。",
    "明火": "使用明火时请确保周围没有易燃物，并在通风处进行。",
    "酒精灯": "酒精灯需在教师指导下使用，注意酒精量不超过三分之一。",
    "刀": "使用刀具时请注意安全，建议在家长陪同下操作。",
    "盐酸": "使用化学试剂需在专业教师指导下进行，佩戴防护手套。",
}


# ─── 语义安全系统提示词（确定性约束） ──────────────────

_SAFETY_JUDGE_SYSTEM_PROMPT = """\
你是一个教育安全审核员。判断以下消息中涉及的实验或操作，
是否属于K-12科学实验教学的安全语境。

判断标准（必须严格遵守）：
- is_educational=true：消息在讨论科学实验、观察自然现象、或学习知识，
  即使包含"火""酸""刀"等词，也是正常教学的一部分。
- is_educational=false：消息明确涉及危险行为、暴力、自残、或与教学无关的危险操作。

注意：小学科学实验中常见的以下内容应判断为教育语境：
  - "酒精灯"在实验课中使用 → true
  - "蜡烛"观察燃烧现象 → true
  - "盐酸"在教师指导下做酸碱实验 → true
  - "小刀"切割实验材料 → true
"""


# ─── 安全过滤器 ────────────────────────────────────────


class SafetyResult:
    """安全检查结果。"""
    def __init__(
        self,
        passed: bool,
        hint: str = "",
        hit_keywords: list[str] | None = None,
    ):
        self.passed = passed
        self.hint = hint
        self.hit_keywords = hit_keywords or []

    def __bool__(self) -> bool:
        return self.passed


class SemanticSafetyFilter:
    """语义安全过滤器（L1 + L2）。"""

    def __init__(self, llm: ChatOpenAI | None = None) -> None:
        self._llm = llm

    async def check(self, message: str) -> SafetyResult:
        """执行安全检查。

        Args:
            message: 用户消息

        Returns:
            SafetyResult:
                passed=True  → 放行（可能有安全提示 hint）
                passed=False → 拦截
        """
        if not message:
            return SafetyResult(passed=True)

        # ── L1: 直接拦截关键词 ──
        for kw in _BLOCK_KEYWORDS:
            if kw in message:
                logger.warning("L1 直接拦截: 命中关键词「%s」", kw)
                return SafetyResult(
                    passed=False,
                    hint=f"你的消息涉及【{kw}】，这个内容不适合在这里讨论哦。",
                    hit_keywords=[kw],
                )

        # ── L1: 语义关键词匹配 ──
        semantic_hits = [kw for kw in _SEMANTIC_KEYWORDS if kw in message]
        if not semantic_hits:
            return SafetyResult(passed=True)  # L1 未命中，直接放行

        logger.info("L1 命中语义关键词: %s，进入 L2 判断", semantic_hits)

        # ── L2: LLM Structured Output 判断 ──
        # 仅在 L1 命中时调用 LLM
        if self._llm:
            try:
                judge_llm = self._llm.with_structured_output(SafetyJudgement)
                result: SafetyJudgement = await judge_llm.ainvoke([
                    {"role": "system", "content": _SAFETY_JUDGE_SYSTEM_PROMPT},
                    {"role": "user", "content": (
                        f"判断以下消息是否属于安全教学语境：\n"
                        f"【{message}】\n"
                        f"消息中包含以下关键词：{', '.join(semantic_hits)}"
                    )},
                ])
                logger.info(
                    "L2 判断: is_educational=%s, action=%s, reason=%s",
                    result.is_educational,
                    result.recommended_action,
                    result.reason,
                )

                if result.is_educational and result.recommended_action == "pass_with_tip":
                    # 教学语境 → 放行 + 安全提示
                    tip = _SAFETY_TIPS.get(semantic_hits[0], result.reason)
                    return SafetyResult(
                        passed=True,
                        hint=f"⚠️ {tip}",
                        hit_keywords=semantic_hits,
                    )
                else:
                    # 非教学语境 → 拦截
                    return SafetyResult(
                        passed=False,
                        hint=(
                            f"你提到的内容（涉及【{', '.join(semantic_hits)}】）"
                            "存在安全风险，请换一个更安全的实验方式吧。"
                        ),
                        hit_keywords=semantic_hits,
                    )

            except Exception as e:
                logger.warning("L2 判断失败: %s，降级为拦截（安全优先）", e)
                return SafetyResult(
                    passed=False,
                    hint=f"你提到的内容（涉及【{', '.join(semantic_hits)}】）存在安全风险。",
                    hit_keywords=semantic_hits,
                )

        # L2 LLM 不可用 → 降级：关键词模式（安全优先，直接拦截）
        logger.warning("L2 LLM 未配置，降级为关键词拦截模式")
        return SafetyResult(
            passed=False,
            hint=(
                f"石头老师检测到你的想法中涉及【{'、'.join(semantic_hits)}】，"
                "这个有一定危险性哦！为了你的安全，我们换一种更安全的实验方式来探究吧。"
            ),
            hit_keywords=semantic_hits,
        )
