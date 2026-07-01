"""
实验后分析智能体 — 4 阶段 FSM（数据描述→结果判断→结论总结→反馈拓展）

阶段流转：
  DATA_ANALYZE → RESULT_JUDGE → CONCLUSION → FEEDBACK
"""

from __future__ import annotations

import logging
from typing import Any, Optional, TypedDict, Annotated, Literal

from langgraph.graph import StateGraph, START, END, add_messages
from langchain_openai import ChatOpenAI
from langchain_core.messages import SystemMessage, HumanMessage, AIMessage
from langgraph.checkpoint.base import BaseCheckpointSaver

from agents_framework.base_agent import BaseAgent
from config import settings
from bs_lab_adapter.tools.vision_review import VisionProvider
from bs_lab_adapter.tools.diagnosis_report_formatter import (
    format_diagnosis_report,
    sections_to_markdown,
)

logger = logging.getLogger("bs_lab_adapter.post_experiment_graph")


# ─── 阶段枚举 ─────────────────────────────────────────

POST_EXPERIMENT_STAGES = [
    "DATA_ANALYZE",
    "RESULT_JUDGE",
    "CONCLUSION",
    "FEEDBACK",
]

_STAGE_INDEX = {s: i for i, s in enumerate(POST_EXPERIMENT_STAGES)}


# ─── 状态定义 ─────────────────────────────────────────

class PostExperimentState(TypedDict):
    """实验后分析状态。"""
    messages: Annotated[list, add_messages]
    user_name: str
    user_id: str
    session_id: str
    current_stage: str                # DATA_ANALYZE / RESULT_JUDGE / CONCLUSION / FEEDBACK
    grade_level: str
    image_base64: str                 # 可选：Base64 编码的图片
    reply_content: str
    diagnosis_report: dict            # A4 报告三节：findings / diagnosis / recommendations
    stage_advanced: bool


# ─── 阶段 System Prompt ───────────────────────────────

STAGE_PROMPTS = {
    "DATA_ANALYZE": """# 角色定义
你是石头老师，耐心亲切的小学科学老师。学生刚刚做完实验，正在向你描述实验过程，或上传了实验照片。

# 当前阶段：数据与现象描述分析（DATA_ANALYZE）
你的任务是：
1. 认真倾听学生描述的实验过程和观察到的现象
2. 如果学生上传了实验图片，结合图片分析结果给出观察到的器材、现象和可能的问题
3. 综合文字描述与图片信息，给出清晰的**实验现象观察**和**初步诊断**
4. 指出实验中可能存在的操作问题或安全隐患（如有），并给出改进建议
5. 用亲切、适合小学生的语言，结构清晰（可用小标题或分点）

# 输出格式（文字描述或图文分析均适用）
- **必须严格按以下三个小标题输出**（不要增减、不要合并到其他标题下）：

### 现象观察
（2-3 条要点，每条不超过 25 字）

### 诊断意见
（2-3 条要点，指出主要问题，每条不超过 25 字）

### 改进建议
（2-3 条要点，每条不超过 25 字）

- 全文总字数不超过 220 字，便于写入一页 A4 报告
- 不要给出数字评分或"合格/不合格"等评级
- 不要使用表格、长段落或大段编号列表

# 约束
- 使用亲切自然的语气
- 适合 {grade_level} 学生的理解水平
- 每轮回复不超过 220 字
- 绝对不要询问年级信息
""",
    "RESULT_JUDGE": """# 角色定义
你是石头老师，耐心亲切的小学科学老师。学生已经描述了实验过程和现象，现在需要你帮助他们判断实验是否成功。

# 当前阶段：实验结果判断（RESULT_JUDGE）
你的任务是：
1. 根据学生描述的实验过程和数据，判断实验是否成功
2. 分析实验结果是否合理，是否有异常
3. 如果实验成功，热情肯定学生的努力
4. 如果实验结果不太理想，温柔地帮助学生分析可能的原因
5. 用简单易懂的方式解释"为什么会出现这样的结果"
6. 注意保护学生的自信心，即使实验不成功也要肯定他们的尝试

# 约束
- 使用亲切自然的语气
- 适合 {grade_level} 学生的理解水平
- 每轮回复不超过 300 字
- 先肯定再分析，不要打击学生的积极性
- 绝对不要询问年级信息
""",
    "CONCLUSION": """# 角色定义
你是石头老师，耐心亲切的小学科学老师。现在要帮助学生总结整个实验的发现。

# 当前阶段：实验结论总结（CONCLUSION）
你的任务是：
1. 用孩子能听懂的语言，帮学生总结整个实验的核心发现
2. 把实验现象和科学原理用生动的比喻或故事联系起来
3. 引导学生用自己的话说出"通过这个实验，我知道了什么"
4. 结论要简洁明了，突出最核心的 1-2 个知识点
5. 鼓励学生把结论分享给家人朋友

# 约束
- 使用亲切自然的语气
- 适合 {grade_level} 学生的理解水平
- 每轮回复不超过 300 字
- 用比喻和故事帮助理解科学原理
- 绝对不要询问年级信息
""",
    "FEEDBACK": """# 角色定义
你是石头老师，耐心亲切的小学科学老师。实验已经完成，结论也有了，现在要给学生提出拓展建议。

# 当前阶段：拓展反馈（FEEDBACK）
你的任务是：
1. 肯定学生完成了整个实验探究过程
2. 提出 1-2 个拓展思考问题，激发进一步探索的兴趣
3. 建议相关的延伸实验或生活中的应用
4. 鼓励学生记录实验报告、画实验图或写实验日记
5. 以温暖鼓励的话语结束本次实验辅导

# 约束
- 使用亲切自然的语气
- 适合 {grade_level} 学生的理解水平
- 每轮回复不超过 300 字
- 拓展问题要有趣、开放，激发孩子好奇心
- 绝对不要询问年级信息
""",
}

STAGE_ORDER: list[str] = ["DATA_ANALYZE", "RESULT_JUDGE", "CONCLUSION", "FEEDBACK"]

_STAGE_COMPLETION_MARKERS: dict[str, list[str]] = {
    "DATA_ANALYZE": ["明白了", "知道了", "看到了", "听懂了", "好的", "还没", "不太清楚"],
    "RESULT_JUDGE": ["明白了", "知道了", "原来是这样", "懂了", "好的"],
    "CONCLUSION": ["明白了", "记住了", "知道了", "我来说", "我想说"],
    "FEEDBACK": ["好的", "知道了", "谢谢", "明白了", "好玩"],
}


# ─── 工具函数 ─────────────────────────────────────────

def _get_next_stage(current: str) -> Optional[str]:
    """获取线性顺序中的下一个阶段。到达 FEEDBACK 时返回 None。"""
    idx = _STAGE_INDEX.get(current)
    if idx is None or idx >= len(POST_EXPERIMENT_STAGES) - 1:
        return None
    return POST_EXPERIMENT_STAGES[idx + 1]


def _detect_stage_completion(reply: str, stage: str) -> bool:
    """检测 LLM 回复中是否包含阶段完成信号。"""
    markers = _STAGE_COMPLETION_MARKERS.get(stage, [])
    return any(marker in reply for marker in markers)


# ─── LLM 实例（共享配置） ─────────────────────────────

def _get_llm(temperature: float = 0.5, max_tokens: int = 4096) -> Optional[ChatOpenAI]:
    """创建 ChatOpenAI 实例。"""
    if not settings.llm_api_key:
        return None
    return ChatOpenAI(
        model=settings.llm_model,
        base_url=settings.llm_base_url,
        api_key=settings.llm_api_key,
        timeout=settings.llm_timeout_ms / 1000,
        temperature=temperature,
        max_tokens=max_tokens,
    )


# ─── 节点函数 ─────────────────────────────────────────

async def build_stage_prompt(state: PostExperimentState) -> dict[str, Any]:
    """根据当前阶段构建 system prompt 并存入 state。"""
    current_stage = state.get("current_stage", "DATA_ANALYZE")
    grade_level = state.get("grade_level", "中段")
    user_name = state.get("user_name", "")

    stage_prompt = STAGE_PROMPTS.get(
        current_stage,
        STAGE_PROMPTS["DATA_ANALYZE"],
    ).format(grade_level=grade_level)

    # 构建基本信息
    base_info = f"\n# 学生信息\n当前年级：{grade_level}\n当前阶段：{current_stage}\n"

    if user_name:
        base_info += f"学生姓名：{user_name}\n"

    system_prompt = stage_prompt + base_info

    return {"system_prompt": system_prompt}


async def call_llm(state: PostExperimentState) -> dict[str, Any]:
    """调用 LLM 生成当前阶段回复。"""
    messages = state.get("messages", [])
    system_prompt = state.get("system_prompt", "")
    current_stage = state.get("current_stage", "DATA_ANALYZE")
    grade_level = state.get("grade_level", "中段")
    image_base64 = state.get("image_base64", "")

    if not messages:
        return {}

    # 获取本轮用户最新消息（用于视觉分析上下文）
    last_human_msg = ""
    for m in reversed(messages):
        if isinstance(m, HumanMessage):
            last_human_msg = m.content
            break

    llm = _get_llm(temperature=0.6)
    report_llm = _get_llm(temperature=0.2, max_tokens=512)
    if not llm:
        return {"reply_content": "AI 服务暂未配置，请联系管理员设置 LLM_API_KEY。"}

    # ── DATA_ANALYZE 阶段有图片 → 调用视觉分析 ──
    vision_summary = ""
    if current_stage == "DATA_ANALYZE" and image_base64:
        try:
            provider = VisionProvider()
            vision_result = await provider.review_image(
                image_base64,
                context={
                    "scene": "experiment_diagnosis",
                    "student_description": last_human_msg,
                },
            )
            vision_summary = vision_result.get("summary", "")
            details = vision_result.get("details", {})
            if details:
                objects = ", ".join(details.get("objects_detected", []))
                phenomena = ", ".join(details.get("phenomena", []))
                issues = ", ".join(
                    details.get("safety_issues", [])
                    + details.get("compliance_issues", [])
                )
                suggestions = ", ".join(details.get("suggestions", []))

                extra = []
                if objects:
                    extra.append(f"图片中可见：{objects}")
                if phenomena:
                    extra.append(f"观察到的现象：{phenomena}")
                if issues:
                    extra.append(f"发现的问题：{issues}")
                if suggestions:
                    extra.append(f"改进建议：{suggestions}")
                if extra:
                    vision_summary = vision_summary + "\n" + "\n".join(extra)

            logger.info("视觉分析完成（实验诊断模式）")
        except Exception as e:
            logger.warning("视觉分析调用失败: %s", e, exc_info=True)
            vision_summary = "（图片分析服务暂时不可用）"

    # ── 组装完整消息 ──
    full_messages = [SystemMessage(content=system_prompt)]

    # 如果有视觉分析结果，插入到用户消息之前
    if vision_summary:
        desc_hint = f"\n学生文字描述：{last_human_msg}\n" if last_human_msg else ""
        full_messages.append(
            SystemMessage(
                content=(
                    "以下是对学生上传的实验图片的分析结果，请结合学生的文字描述一起给出诊断：\n"
                    f"{vision_summary}{desc_hint}\n"
                    "请综合图片与文字，给出现象观察、问题诊断和改进建议，不要使用评分或评级。"
                )
            )
        )

    full_messages.extend(list(messages))

    diagnosis_report: dict[str, str] = {}

    try:
        current_stage = state.get("current_stage", "DATA_ANALYZE")

        # DATA_ANALYZE：专用报告格式化，避免长段对话污染 A4 报告
        if current_stage == "DATA_ANALYZE":
            diagnosis_report = await format_diagnosis_report(
                report_llm,
                student_description=last_human_msg,
                vision_summary=vision_summary,
                grade_level=grade_level,
            )
            reply_text = sections_to_markdown(diagnosis_report)
            logger.info(
                "诊断报告格式化完成 findings=%.40s",
                diagnosis_report.get("findings", ""),
            )
        else:
            logger.info(
                "LLM 请求 stage=%s messages=%s",
                current_stage,
                [(m.type, str(m.content)[:80]) for m in full_messages],
            )
            response = await llm.ainvoke(full_messages)
            reply_text = response.content.strip() if response.content else ""
            logger.info("LLM 响应 stage=%s reply=%.200s", current_stage, reply_text[:200])
            if not reply_text:
                reply_text = "嗯，让我想想怎么回答你哦～"
    except Exception as e:
        logger.error("LLM 调用失败: %s", e, exc_info=True)
        reply_text = "石头老师暂时有点忙，请稍后再试哦～"

    # 姓名拼接
    user_name = state.get("user_name", "")
    has_assistant_history = any(
        isinstance(m, AIMessage) for m in state.get("messages", [])
    )
    if user_name and not has_assistant_history:
        reply_content = f"{user_name}你好呀！我是石头老师~{reply_text}"
    elif user_name:
        reply_content = f"{user_name}，{reply_text}"
    else:
        reply_content = reply_text

    result: dict[str, Any] = {
        "reply_content": reply_content,
        "messages": [AIMessage(content=reply_text)],
    }
    if diagnosis_report:
        result["diagnosis_report"] = diagnosis_report
    if image_base64:
        result["image_base64"] = ""
    return result


async def check_stage_transition(state: PostExperimentState) -> dict[str, Any]:
    """检查并决定是否推进到下一阶段。"""
    current_stage = state.get("current_stage", "DATA_ANALYZE")
    reply_content = state.get("reply_content", "")

    # 检测是否应该推进
    should_advance = _detect_stage_completion(reply_content, current_stage)

    if should_advance:
        next_stage = _get_next_stage(current_stage)
        if next_stage:
            logger.info(
                "实验后分析阶段推进: %s → %s",
                current_stage,
                next_stage,
            )
            return {
                "current_stage": next_stage,
                "stage_advanced": True,
            }

    return {"stage_advanced": False}


# ─── 条件边函数 ───────────────────────────────────────

def route_after_llm(state: PostExperimentState) -> str:
    """LLM 调用结束后直接结束本轮。"""
    return END


# ─── 构建图 ───────────────────────────────────────────

def create_post_experiment_graph(checkpointer: BaseCheckpointSaver = None):
    """创建实验后分析 LangGraph（4 阶段 FSM）。

    三节点线性图：
      build_stage_prompt → call_llm → check_stage_transition

    阶段流转由 check_stage_transition 更新 current_stage 完成，
    每次请求执行完整的三节点流程。

    Args:
        checkpointer: MySQLSaver / TTLMemorySaver 实例（可选）

    Returns:
        编译后的 StateGraph
    """
    nodes = {
        "build_prompt": build_stage_prompt,
        "llm_call": call_llm,
        "check_transition": check_stage_transition,
    }

    edges = [
        ("build_prompt", "llm_call"),
        ("llm_call", "check_transition"),
    ]

    conditional_edges = [
        ("check_transition", route_after_llm, {END: END}),
    ]

    graph = BaseAgent.create(
        state_schema=PostExperimentState,
        nodes=nodes,
        edges=edges,
        conditional_edges=conditional_edges,
        checkpointer=checkpointer,
        first_node="build_prompt",
    )

    return graph
