"""
实验诊断报告格式化：将学生描述 + 图片分析压缩为 A4 报告三节 JSON。
"""

from __future__ import annotations

import json
import logging
import re
from typing import Any, Optional

from langchain_core.messages import HumanMessage, SystemMessage
from langchain_openai import ChatOpenAI

logger = logging.getLogger("bs_lab_adapter.diagnosis_report_formatter")

_REPORT_JSON_PROMPT = """你是小学科学实验诊断报告撰写员。根据学生描述与图片分析摘要，填写简明诊断报告。

# 学生描述
{student_description}

# 图片分析摘要（无则忽略）
{vision_summary}

# 输出要求
仅输出一个 JSON 对象，不要 markdown 代码块，不要其它文字：
{{"findings":"...","diagnosis":"...","recommendations":"..."}}

字段说明：
- findings：观察所见，2-3 条要点，用 "• " 开头、换行分隔，每条不超过 22 字
- diagnosis：诊断意见，2-3 条要点，格式同上
- recommendations：改进建议，2-3 条要点，格式同上

总字数不超过 200 字。语言适合 {grade_level} 小学生。
不要评分、不要寒暄、不要长段落、不要用 1.2.3. 编号列表。
"""


def sections_to_markdown(sections: dict[str, str]) -> str:
    """将三节 JSON 转为聊天气泡用的 Markdown。"""
    findings = (sections.get("findings") or "").strip()
    diagnosis = (sections.get("diagnosis") or "").strip()
    recommendations = (sections.get("recommendations") or "").strip()
    return (
        f"### 现象观察\n{findings}\n\n"
        f"### 诊断意见\n{diagnosis}\n\n"
        f"### 改进建议\n{recommendations}"
    )


def _normalize_bullets(text: str, max_items: int = 3, max_len: int = 22) -> str:
    if not text:
        return ""
    t = re.sub(r"\*\*(.+?)\*\*", r"\1", text.strip())
    lines = [ln.strip() for ln in re.split(r"[\n；;]+", t) if ln.strip()]
    bullets: list[str] = []
    for ln in lines:
        ln = re.sub(r"^[•\-*]\s*", "", ln)
        ln = re.sub(r"^\d+[.、．)]\s*", "", ln)
        if not ln:
            continue
        if len(ln) > max_len:
            ln = ln[: max_len - 1] + "…"
        bullets.append(f"• {ln}")
        if len(bullets) >= max_items:
            break
    return "\n".join(bullets)


def _parse_report_json(raw: str) -> Optional[dict[str, str]]:
    if not raw:
        return None
    text = raw.strip()
    # 去掉 ```json 包裹
    fence = re.search(r"```(?:json)?\s*([\s\S]*?)```", text, re.I)
    if fence:
        text = fence.group(1).strip()
    else:
        start = text.find("{")
        end = text.rfind("}")
        if start >= 0 and end > start:
            text = text[start : end + 1]

    try:
        data = json.loads(text)
    except json.JSONDecodeError:
        logger.warning("诊断报告 JSON 解析失败: %.120s", raw)
        return None

    if not isinstance(data, dict):
        return None

    return {
        "findings": _normalize_bullets(str(data.get("findings", ""))),
        "diagnosis": _normalize_bullets(str(data.get("diagnosis", ""))),
        "recommendations": _normalize_bullets(str(data.get("recommendations", ""))),
    }


def _fallback_sections(student_description: str, vision_summary: str) -> dict[str, str]:
    """LLM 失败时的极简兜底。"""
    desc = (student_description or "学生描述了实验现象").strip()[:40]
    vision = (vision_summary or "").strip()[:60]
    findings = _normalize_bullets(desc)
    if vision:
        findings = _normalize_bullets(f"{desc}；{vision}", max_items=3, max_len=30)
    return {
        "findings": findings or "• 学生描述了实验中的主要现象",
        "diagnosis": "• 需结合操作细节进一步排查原因",
        "recommendations": "• 重复实验并记录关键步骤与现象",
    }


async def format_diagnosis_report(
    llm: Optional[ChatOpenAI],
    *,
    student_description: str,
    vision_summary: str = "",
    grade_level: str = "中段",
) -> dict[str, str]:
    """调用 LLM 生成 A4 报告三节；失败则返回兜底内容。"""
    desc = (student_description or "（学生未补充文字描述）").strip()
    vision = (vision_summary or "（无图片）").strip()

    if not llm:
        return _fallback_sections(desc, vision if vision != "（无图片）" else "")

    prompt = _REPORT_JSON_PROMPT.format(
        student_description=desc,
        vision_summary=vision,
        grade_level=grade_level or "中段",
    )

    try:
        response = await llm.ainvoke(
            [
                SystemMessage(content=prompt),
                HumanMessage(content="请输出 JSON。"),
            ]
        )
        raw = (response.content or "").strip()
        parsed = _parse_report_json(raw)
        if parsed and any(parsed.values()):
            return parsed
        logger.warning("诊断报告 JSON 字段为空，使用兜底: %.120s", raw)
    except Exception as e:
        logger.error("诊断报告格式化失败: %s", e, exc_info=True)

    return _fallback_sections(desc, vision if vision != "（无图片）" else "")
