"""
bs_lab_adapter.tools.vision_review — 视觉分析服务

使用 Qwen-VL（阿里云百炼 / DashScope）对实验场景图片进行 AI 视觉分析，
检测设备、现象、安全隐患及合规问题，返回结构化评估结果。

使用标准 OpenAI SDK 调用（与 LangChain 共享依赖），无需额外安装。
"""

from __future__ import annotations

import asyncio
import json
import logging
import time
from enum import Enum
from typing import Any

import httpx
from openai import APIStatusError, AsyncOpenAI

from config import settings

logger = logging.getLogger("bs_lab_adapter.tools.vision_review")

# ── 阿里云百炼 DashScope OpenAI 兼容端点 ──
_DEFAULT_VISION_BASE_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1"


class CircuitBreakerState(Enum):
    """熔断器状态枚举。"""

    CLOSED = "closed"
    OPEN = "open"
    HALF_OPEN = "half_open"


class CircuitBreaker:
    """熔断器 — 防止连续失败时重复调用下游视觉服务。

    Attributes:
        threshold: 连续失败次数阈值（达到后熔断器打开）
        recovery_timeout: 熔断超时秒数（超时后进入半开状态尝试恢复）
        state: 当前状态 closed / open / half_open
        failure_count: 当前连续失败次数
        last_failure_time: 最近一次失败的时间戳（monotonic）
    """

    def __init__(
        self,
        threshold: int = 5,
        recovery_timeout: float = 60.0,
    ) -> None:
        self.threshold = threshold
        self.recovery_timeout = recovery_timeout
        self.state = CircuitBreakerState.CLOSED
        self.failure_count = 0
        self.last_failure_time: float | None = None

    @property
    def is_available(self) -> bool:
        """检查熔断器是否允许请求通过。"""
        if self.state == CircuitBreakerState.CLOSED:
            return True

        if self.state == CircuitBreakerState.OPEN:
            # 检查恢复超时是否已过
            if self.last_failure_time is not None:
                elapsed = time.monotonic() - self.last_failure_time
                if elapsed >= self.recovery_timeout:
                    logger.info("熔断器进入半开状态，允许试探请求")
                    self.state = CircuitBreakerState.HALF_OPEN
                    return True

            remaining = (
                self.recovery_timeout
                - (time.monotonic() - self.last_failure_time)
                if self.last_failure_time
                else self.recovery_timeout
            )
            logger.warning("熔断器已打开，拒绝请求（剩余 %.1fs）", max(remaining, 0.0))
            return False

        # HALF_OPEN 状态允许单个试探请求
        return True

    def record_success(self) -> None:
        """记录一次成功调用 — 重置熔断器。"""
        self.failure_count = 0
        self.last_failure_time = None
        if self.state == CircuitBreakerState.HALF_OPEN:
            logger.info("熔断器从半开状态恢复为关闭")
        self.state = CircuitBreakerState.CLOSED

    def record_failure(self) -> None:
        """记录一次失败调用 — 递增计数，达到阈值时打开熔断器。"""
        self.failure_count += 1
        self.last_failure_time = time.monotonic()
        if self.failure_count >= self.threshold:
            self.state = CircuitBreakerState.OPEN
            logger.warning(
                "熔断器已打开（连续 %d 次失败）", self.failure_count
            )

    def __repr__(self) -> str:
        return (
            f"CircuitBreaker(state={self.state.value}, "
            f"failures={self.failure_count}/{self.threshold})"
        )


class VisionProvider:
    """视觉分析提供者 — 封装 Qwen-VL API 调用及熔断/重试逻辑。"""

    def __init__(self) -> None:
        self.circuit_breaker = CircuitBreaker(
            threshold=settings.vision_circuit_breaker_threshold,
        )

    # ── 公开方法 ──────────────────────────────────────────────

    async def review_image(
        self,
        image_base64: str,
        context: dict[str, Any] | None = None,
    ) -> dict[str, Any]:
        """分析一张图片，返回结构化审查结果。

        Args:
            image_base64: Base64 编码的图片数据
            context: 可选上下文（如实验名称、当前步骤等）

        Returns:
            包含 summary / details / overall_rating / is_safe 的结果字典
        """
        # ── 服务未配置：直接返回降级结果 ──
        if not settings.vision_api_key:
            logger.warning("视觉分析服务未配置（VISION_API_KEY 为空），返回降级结果")
            return _degraded_result()

        # ── 熔断检查 ──
        if not self.circuit_breaker.is_available:
            logger.warning("熔断器已打开，返回降级结果")
            return _degraded_result("图片分析服务暂时不可用，请稍后重试")

        # ── 构造请求 ──
        system_prompt = _build_system_prompt(context)
        user_content = _build_user_content(image_base64, context)

        base_url = settings.vision_base_url or _DEFAULT_VISION_BASE_URL
        client = AsyncOpenAI(
            api_key=settings.vision_api_key,
            base_url=base_url,
        )

        try:
            response = await _call_with_retry(
                client=client,
                model=settings.vision_model,
                messages=[
                    {"role": "system", "content": system_prompt},
                    {"role": "user", "content": user_content},
                ],
                max_retries=settings.vision_retry_max,
                base_delay=settings.vision_retry_base_delay,
            )

            result = _parse_response(response)
            self.circuit_breaker.record_success()
            return result

        except APIStatusError as e:
            self.circuit_breaker.record_failure()
            logger.error(
                "视觉 API 返回错误 (status=%d): %s", e.status_code, e
            )
            return _degraded_result(f"图片分析服务返回错误 (HTTP {e.status_code})")

        except Exception as e:
            self.circuit_breaker.record_failure()
            logger.error("图片分析失败: %s", e, exc_info=True)
            return _degraded_result(f"图片分析出错: {e}")


# ── 内部辅助函数 ────────────────────────────────────────────


async def _call_with_retry(
    client: AsyncOpenAI,
    model: str,
    messages: list[dict[str, Any]],
    max_retries: int = 3,
    base_delay: float = 1.0,
) -> Any:
    """调用 OpenAI API 并实现指数退避重试。

    仅对网络级异常（超时、连接错误）重试；
    认证错误（401/403）和无效请求不重试，直接抛出。
    """
    last_exception: Exception | None = None

    for attempt in range(max_retries + 1):
        try:
            if attempt > 0:
                delay = base_delay * (2 ** (attempt - 1))
                logger.info("视觉 API 重试第 %d 次（等待 %.1fs）", attempt, delay)
                await asyncio.sleep(delay)

            return await client.chat.completions.create(
                model=model,
                messages=messages,
                timeout=settings.vision_timeout_ms / 1000,
            )

        except APIStatusError as e:
            # 认证 / 鉴权错误 — 不重试
            if e.status_code in (401, 403):
                logger.error("认证/授权错误，不重试: %s", e)
                raise
            # 请求格式错误 — 不重试
            if 400 <= e.status_code < 500 and e.status_code not in (429,):
                logger.error("请求错误 (HTTP %d)，不重试: %s", e.status_code, e)
                raise
            last_exception = e
            if attempt == max_retries:
                raise

        except httpx.TimeoutException as e:
            last_exception = e
            logger.warning("请求超时（尝试 %d/%d）", attempt + 1, max_retries + 1)
            if attempt == max_retries:
                raise

        except (TimeoutError, ConnectionError) as e:
            last_exception = e
            logger.warning("网络错误（尝试 %d/%d）: %s", attempt + 1, max_retries + 1, e)
            if attempt == max_retries:
                raise

    # 不应到达此处
    if last_exception:
        raise last_exception


def _build_system_prompt(context: dict[str, Any] | None = None) -> str:
    """构造系统提示词，要求 LLM 以 JSON 格式返回结果。"""
    scene = (context or {}).get("scene", "")
    if scene == "experiment_diagnosis":
        return """你是一个专业的小学科学实验分析助手。请分析学生上传的实验照片，并结合学生文字描述（如有），以 **JSON 格式** 返回诊断结果。

## 分析要求
1. **实验设备/物品** (objects_detected)：列出图片中可见的实验器材、试剂、工具等
2. **实验现象** (phenomena)：描述可观察到的实验现象（如冒泡、变色、水位变化等）
3. **发现的问题** (safety_issues)：操作不当、安全隐患或实验异常（合并写入此字段）
4. **合规/规范问题** (compliance_issues)：摆放不规范、步骤遗漏等
5. **改进建议** (suggestions)：针对发现的问题给出 2-4 条具体、可操作的改进建议

## summary 字段
用 2-4 句话概括：从图片看到了什么、结合学生描述的关键信息、主要诊断结论。
不要使用数字评分，不要使用"合格/不合格"等评级词汇。

## 输出格式
请严格按以下 JSON 结构返回，不要添加额外说明：
{
  "summary": "总体诊断摘要",
  "details": {
    "objects_detected": ["物品1", "物品2"],
    "phenomena": ["现象1", "现象2"],
    "safety_issues": ["问题1", "问题2"],
    "compliance_issues": ["问题1", "问题2"],
    "suggestions": ["建议1", "建议2"]
  },
  "overall_rating": "未分析",
  "is_safe": null
}"""

    return """你是一个专业的实验安全审查助手。请分析图片中的实验场景，并以 **JSON 格式** 返回结果。

## 分析要求
1. **实验设备/物品** (objects_detected)：列出图片中可见的所有实验设备、仪器、试剂等
2. **实验现象** (phenomena)：描述可观察到的实验现象（如冒泡、变色、火焰等）
3. **安全隐患** (safety_issues)：识别潜在安全风险
   - 水杯/饮料靠近实验设备或边缘
   - 明火、高温源无人看管
   - 电线/电源线杂乱或靠近水源
   - 未佩戴防护装备（护目镜、手套等）
   - 化学品容器未盖盖子
   - 通风不良
   - 其他安全风险
4. **合规问题** (compliance_issues)：识别实验操作或摆放不规范之处
   - 设备摆放不符合规范
   - 试剂标签不清晰或缺失
   - 废弃物处理不当
   - 实验区域杂乱
   - 其他合规问题
5. **改进建议** (suggestions)：针对发现的问题给出具体改进建议

## 总体评价 (overall_rating)
- "合格" — 无安全隐患，符合规范
- "需改进" — 存在轻微问题需要调整
- "不合格" — 存在严重安全隐患需要立即整改
- "未分析" — 无法分析图片内容

## 安全性判断 (is_safe)
- true — 实验场景安全
- false — 实验场景不安全
- null — 无法判断

## 输出格式
请严格按以下 JSON 结构返回，不要添加额外说明：
{
  "summary": "总体分析摘要",
  "details": {
    "objects_detected": ["物品1", "物品2"],
    "phenomena": ["现象1", "现象2"],
    "safety_issues": ["问题1", "问题2"],
    "compliance_issues": ["问题1", "问题2"],
    "suggestions": ["建议1", "建议2"]
  },
  "overall_rating": "合格/需改进/不合格/未分析",
  "is_safe": true/false/null
}"""


def _build_user_content(
    image_base64: str,
    context: dict[str, Any] | None,
) -> list[dict[str, Any]]:
    """构造多模态用户消息内容。"""
    content: list[dict[str, Any]] = []

    if context:
        ctx_parts = []
        if context.get("student_description"):
            ctx_parts.append(f"学生文字描述：{context['student_description']}")
        other = {k: v for k, v in context.items()
                 if k not in ("scene", "student_description") and v}
        if other:
            ctx_parts.append(f"其他上下文：{json.dumps(other, ensure_ascii=False)}")
        if ctx_parts:
            content.append({
                "type": "text",
                "text": "\n".join(ctx_parts),
            })

    content.append({
        "type": "image_url",
        "image_url": {
            "url": f"data:image/jpeg;base64,{image_base64}",
        },
    })

    return content


def _parse_response(response: Any) -> dict[str, Any]:
    """解析 LLM 返回的 JSON 响应，填充缺失字段并确保结构完整。"""
    try:
        content = response.choices[0].message.content.strip()

        # 去除可能的 Markdown 代码围栏
        if content.startswith("```"):
            lines = content.splitlines()
            if lines[0].startswith("```"):
                lines = lines[1:]
            if lines and lines[-1].strip().startswith("```"):
                lines = lines[:-1]
            content = "\n".join(lines)

        result: dict[str, Any] = json.loads(content)
    except (json.JSONDecodeError, AttributeError, IndexError, KeyError) as e:
        logger.error("解析 LLM 响应 JSON 失败: %s", e)
        return _degraded_result("图片分析结果解析失败")

    # 确保所有字段存在且类型正确
    details_raw = result.get("details", {}) or {}
    if not isinstance(details_raw, dict):
        details_raw = {}

    return {
        "summary": result.get("summary", "") or "",
        "details": {
            "objects_detected": _ensure_str_list(
                details_raw.get("objects_detected")
            ),
            "phenomena": _ensure_str_list(details_raw.get("phenomena")),
            "safety_issues": _ensure_str_list(
                details_raw.get("safety_issues")
            ),
            "compliance_issues": _ensure_str_list(
                details_raw.get("compliance_issues")
            ),
            "suggestions": _ensure_str_list(details_raw.get("suggestions")),
        },
        "overall_rating": result.get("overall_rating", "未分析") or "未分析",
        "is_safe": result.get("is_safe"),
    }


def _ensure_str_list(value: Any) -> list[str]:
    """确保值为字符串列表。"""
    if isinstance(value, list):
        return [str(item) for item in value]
    if isinstance(value, str):
        return [value]
    return []


def _degraded_result(summary: str | None = None) -> dict[str, Any]:
    """构造降级结果（服务不可用、未配置或出错时返回）。"""
    return {
        "summary": (
            summary
            or "当前图片分析服务未配置，无法分析图片"
        ),
        "details": {
            "objects_detected": [],
            "phenomena": [],
            "safety_issues": [],
            "compliance_issues": [],
            "suggestions": (
                ["请联系管理员配置视觉分析服务"]
                if summary is None
                else []
            ),
        },
        "overall_rating": "未分析",
        "is_safe": None,
    }
