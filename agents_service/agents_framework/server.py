"""
agents_framework.server — 统一 FastAPI 路由

仅暴露 POST /v1/chat（SSE 流式），通过 AgentRouter 分发。
"""

from __future__ import annotations

import hashlib
import logging
import time
import uuid
from typing import Any, Optional

from contextlib import asynccontextmanager
from fastapi import FastAPI, HTTPException, Request
from fastapi.responses import StreamingResponse

from langchain_core.messages import HumanMessage

from agents_framework.router import AgentRouter
from agents_framework.sse import (
    serialize_done,
    serialize_error,
    serialize_meta,
    serialize_token,
    serialize_stage,
    serialize_heartbeat,
    serialize_diagnosis_report,
)
from agents_framework.errors import AgentError, classify_error

logger = logging.getLogger("agents_framework.server")

_IDEMPOTENCY_WINDOW_SEC = 30
_idempotency_cache: dict[tuple[str, str], float] = {}


def _check_idempotency(thread_id: str, message: str) -> bool:
    """检查并记录幂等键。返回 True 表示已处理过（应跳过）。"""
    now = time.monotonic()
    key = (thread_id, hashlib.md5(message.encode()).hexdigest()[:16])
    if len(_idempotency_cache) > 0 and len(_idempotency_cache) % 100 == 0:
        cutoff = now - _IDEMPOTENCY_WINDOW_SEC
        stale = [k for k, ts in _idempotency_cache.items() if ts < cutoff]
        for k in stale:
            _idempotency_cache.pop(k, None)
    if key in _idempotency_cache:
        elapsed = now - _idempotency_cache[key]
        if elapsed < _IDEMPOTENCY_WINDOW_SEC:
            logger.warning("幂等拦截: thread=%s msg=%s... (%.1fs)", thread_id, message[:20], elapsed)
            return True
    _idempotency_cache[key] = now
    return False


def create_agent_app(
    agent_router: AgentRouter,
    checkpointer: Optional[Any] = None,
    title: str = "Agent Service",
    version: str = "1.0.0",
) -> FastAPI:
    """创建 Agent 服务的 FastAPI 应用。

    自动注册端点：
      POST /v1/chat   (SSE 流式)       — 统一聊天入口
      GET  /health                      — 健康检查

    Args:
        agent_router: AgentRouter 实例（已注册好所有智能体）
        checkpointer: MySQLSaver / TTLMemorySaver 实例（可选）
        title: 应用标题
        version: 版本号

    Returns:
        FastAPI 应用实例
    """

    @asynccontextmanager
    async def lifespan(app: FastAPI):
        # 启动：无需特殊处理
        yield
        # 关闭：优雅释放所有数据库连接池，避免 Event loop is closed 警告
        # 1) 业务数据库引擎
        try:
            from database import dispose_engine as _dispose_biz_engine
            await _dispose_biz_engine()
        except ImportError:
            pass
        except Exception:
            pass  # 关闭阶段忽略错误
        # 2) Checkpointer 连接池
        if checkpointer and hasattr(checkpointer, "engine"):
            try:
                await checkpointer.engine.dispose()
                logger.info("MySQL checkpointer 连接池已释放")
            except Exception:
                pass  # 关闭阶段忽略错误

    app = FastAPI(title=title, version=version, lifespan=lifespan)

    # ─── 健康检查 ────────────────────────────────────
    @app.get("/health")
    async def health():
        from config import settings as _settings
        checks = {
            "agent_count": len(agent_router.available_roles),
            "agents": agent_router.available_roles,
            "llm_configured": bool(_settings.llm_api_key),
            "llm_model": _settings.llm_model or "未配置",
            "checkpointer_type": checkpointer.__class__.__name__ if checkpointer else "none",
            "db_configured": bool(_settings.database_url),
        }
        ready = checks["agent_count"] > 0 and checks["llm_configured"]
        return {
            "status": "ok" if ready else "degraded",
            "service": title,
            "version": version,
            "checks": checks,
        }

    # ─── 统一聊天入口 ────────────────────────────────
    @app.post("/v1/chat")
    async def unified_chat(request: Request):
        body = await request.json()
        message = (body.get("message") or "").strip()
        role = (body.get("role") or "student").strip().lower()
        thread_id = body.get("thread_id")
        user_name = (body.get("user_name") or "").strip()
        user_id = body.get("user_id", thread_id or uuid.uuid4().hex[:16])
        grade_level = body.get("grade_level") or None
        image_base64 = _normalize_image_base64(body.get("image_base64"))

        if not message and not image_base64:
            raise HTTPException(status_code=422, detail="message 或 image_base64 至少提供一个")
        if not message:
            message = "请结合上传的实验照片进行分析。"

        trace_id = request.headers.get("x-trace-id", uuid.uuid4().hex[:16])
        logger.info(
            "[%s] /v1/chat role=%s grade=%s msg=%.40s image=%s tid=%s",
            trace_id, role, grade_level, message, bool(image_base64), thread_id,
        )

        # 通过 Router 解析目标智能体
        graph, resolved_thread_id = await agent_router.resolve(
            role=role,
            thread_id=thread_id,
            checkpointer=checkpointer,
        )

        # 幂等去重
        if resolved_thread_id and _check_idempotency(resolved_thread_id, message):
            logger.info("[%s] 幂等命中，继续正常处理", trace_id)

        # 构建 config
        config: dict[str, Any] = {
            "configurable": {
                "thread_id": resolved_thread_id or uuid.uuid4().hex[:32],
                "trace_id": trace_id,
            }
        }
        resolved_thread_id = config["configurable"]["thread_id"]

        # 构建输入状态（透传 grade_level 和 role）
        input_state = await _build_input_state(
            graph, config, message, user_id, user_name, resolved_thread_id,
            grade_level=grade_level, role=role, image_base64=image_base64,
        )

        return StreamingResponse(
            _stream_agent_response(graph, input_state, config, role=role),
            media_type="text/event-stream",
            headers={
                "cache-control": "no-cache",
                "connection": "keep-alive",
                "x-trace-id": trace_id,
                "x-agent-type": role,
            },
        )

    # ─── 同步 JSON 接口（供 Java proxy 等后端服务调用）────────
    @app.post("/v1/chat/sync")
    async def unified_chat_sync(request: Request):
        body = await request.json()
        message = (body.get("message") or "").strip()
        role = (body.get("role") or "student").strip().lower()
        thread_id = body.get("thread_id")
        user_name = (body.get("user_name") or "").strip()
        user_id = body.get("user_id", thread_id or uuid.uuid4().hex[:16])
        grade_level = body.get("grade_level") or None
        image_base64 = _normalize_image_base64(body.get("image_base64"))
        experiment_title = (body.get("experiment_title") or "").strip()

        if not message and not image_base64:
            if role == "plan_design" and experiment_title:
                message = f"请为「{experiment_title}」设计 3 个实验方案"
            else:
                raise HTTPException(status_code=422, detail="message 或 image_base64 至少提供一个")
        if not message:
            message = "请结合上传的实验照片进行分析。"

        trace_id = request.headers.get("x-trace-id", uuid.uuid4().hex[:16])
        logger.info("[%s] /v1/chat/sync role=%s grade=%s msg=%.40s image=%s tid=%s",
                     trace_id, role, grade_level, message, bool(image_base64), thread_id)

        graph, resolved_thread_id = await agent_router.resolve(
            role=role, thread_id=thread_id, checkpointer=checkpointer)

        if resolved_thread_id and _check_idempotency(resolved_thread_id, message):
            logger.info("[%s] 幂等命中", trace_id)

        config = {
            "configurable": {
                "thread_id": resolved_thread_id or uuid.uuid4().hex[:32],
                "trace_id": trace_id,
            }
        }
        resolved_thread_id = config["configurable"]["thread_id"]

        input_state = await _build_input_state(
            graph, config, message, user_id, user_name, resolved_thread_id,
            grade_level=grade_level, role=role, image_base64=image_base64,
            experiment_title=experiment_title,
        )

        try:
            final_state = await graph.ainvoke(input_state, config=config)
        except Exception as e:
            logger.error(f"[{trace_id}] Agent 调用失败: {e}", exc_info=True)
            return {"reply": "抱歉，石头老师现在有点忙，请稍后再试。",
                    "thread_id": resolved_thread_id, "plans": []}

        reply = ""
        plans: list[Any] = []
        diagnosis_report: dict[str, Any] = {}
        if isinstance(final_state, dict):
            reply = final_state.get("reply_content", "") or ""
            plans = final_state.get("plans", []) or []
            diagnosis_report = final_state.get("diagnosis_report", {}) or {}
            if not reply and final_state.get("messages"):
                for m in reversed(final_state["messages"]):
                    if hasattr(m, "content") and isinstance(m.content, str) and m.content:
                        reply = m.content
                        break

        # 序列化 Pydantic 方案对象
        serialized_plans = []
        for p in plans:
            if hasattr(p, "model_dump"):
                serialized_plans.append(p.model_dump())
            elif isinstance(p, dict):
                serialized_plans.append(p)
            else:
                serialized_plans.append(dict(p))

        return {
            "reply": reply or "收到你的问题，让我想想…",
            "thread_id": resolved_thread_id,
            "plans": serialized_plans,
            "diagnosis_report": diagnosis_report,
        }

    # ─── 聊天历史(读取/清除) ──────────────────────────────
    @app.get("/v1/chat/history/{thread_id}")
    async def get_chat_history(thread_id: str):
        """从 checkpointer 读取指定线程的聊天历史。"""
        if not checkpointer:
            return {"messages": [], "thread_id": thread_id}
        try:
            state = await checkpointer.aget_tuple(
                {"configurable": {"thread_id": thread_id}}
            )
            if not state:
                return {"messages": [], "thread_id": thread_id}

            messages = []
            if state.checkpoint and "channel_values" in state.checkpoint:
                channel_msgs = state.checkpoint["channel_values"].get("messages", [])
                for msg in channel_msgs:
                    if hasattr(msg, "type") and hasattr(msg, "content"):
                        messages.append({
                            "role": "user" if msg.type == "human" else "assistant",
                            "content": str(msg.content) if msg.content else "",
                        })
            return {"messages": messages, "thread_id": thread_id}
        except Exception as e:
            logger.error("获取聊天历史失败: %s", e, exc_info=True)
            return {"messages": [], "thread_id": thread_id}

    @app.delete("/v1/chat/clear/{thread_id}")
    async def clear_chat_history(thread_id: str):
        """清除指定线程的所有检查点数据。"""
        if not checkpointer:
            return {"status": "ok", "message": "未配置 checkpointer，无操作"}
        try:
            if hasattr(checkpointer, "adelete_thread"):
                await checkpointer.adelete_thread(thread_id)
                logger.info("已清除线程 %s 的检查点", thread_id)
            return {"status": "ok", "message": f"已清除会话 {thread_id}"}
        except Exception as e:
            logger.error("清除聊天历史失败: %s", e, exc_info=True)
            return {"status": "error", "message": str(e)}

    # ─── 视觉分析（独立接口，供 Java proxy 转发）────────────
    @app.post("/v1/vision/review")
    async def vision_review(request: Request):
        from bs_lab_adapter.tools.vision_review import VisionProvider
        from bs_lab_adapter.schemas.vision import VisionReviewRequest

        body = await request.json()
        req = VisionReviewRequest(**body)
        image_base64 = _normalize_image_base64(req.image_base64)
        if not image_base64:
            raise HTTPException(status_code=422, detail="image_base64 不能为空")

        provider = VisionProvider()
        result = await provider.review_image(image_base64, context=req.context)
        return result

    return app


# ─── 辅助函数 ──────────────────────────────────────────


def _build_new_session_state(message: str, user_id: str, user_name: str,
                              session_id: str = "",
                              grade_level: str | None = None,
                              role: str = "student",
                              image_base64: str = "",
                              experiment_title: str = "") -> dict:
    """构建全新会话的初始状态（角色感知）。"""
    if role == "free_chat":
        return {
            "messages": [HumanMessage(content=message)],
            "user_name": user_name or "同学",
            "user_id": user_id,
            "session_id": session_id,
            "reply_content": "",
        }

    if role == "plan_design":
        return {
            "messages": [HumanMessage(content=message)],
            "user_name": user_name or "同学",
            "user_id": user_id,
            "session_id": session_id,
            "experiment_title": experiment_title or message,
            "grade_level": grade_level or "中段",
            "plans": [],
            "reply_content": "",
        }

    if role == "post_experiment":
        return {
            "messages": [HumanMessage(content=message)],
            "user_name": user_name or "同学",
            "user_id": user_id,
            "session_id": session_id,
            "current_stage": "DATA_ANALYZE",
            "grade_level": grade_level or "中段",
            "image_base64": image_base64 or "",
            "reply_content": "",
            "stage_advanced": False,
        }

    # 默认：pre_experiment / student 格式
    return {
        "messages": [HumanMessage(content=message)],
        "user_name": user_name or "同学",
        "user_id": user_id,
        "current_stage": "INIT",
        "session_id": session_id,
        "grade_level": grade_level,
        "experiment_title": None,
        "safety_hit": None,
        "safety_tip": "",
        "stage_advanced": False,
        "reply_content": "",
        "trace_id": "",
        "system_prompt": "",
        "is_stage_ready_to_advance": False,
        "stage_summary": {},
        "user_intent": "NORMAL_CONTENT",
    }


def _normalize_image_base64(raw: Any) -> str:
    """Strip data-URL prefix from base64 image payload."""
    if not raw or not isinstance(raw, str):
        return ""
    value = raw.strip()
    if value.startswith("data:"):
        comma = value.find(",")
        if comma >= 0:
            value = value[comma + 1 :]
    return value


async def _build_input_state(
    graph,
    config: dict,
    message: str,
    user_id: str,
    user_name: str,
    session_id: str = "",
    grade_level: str | None = None,
    role: str = "student",
    image_base64: str = "",
    experiment_title: str = "",
) -> dict:
    """构建本轮输入状态。"""
    try:
        saved = await graph.aget_state(config)
        has_saved = bool(saved and saved.values)
    except Exception:
        has_saved = False
    if has_saved:
        state_update: dict[str, Any] = {"messages": [HumanMessage(content=message)]}
        if image_base64:
            state_update["image_base64"] = image_base64
        if role == "plan_design":
            if experiment_title:
                state_update["experiment_title"] = experiment_title
            if grade_level:
                state_update["grade_level"] = grade_level
        return state_update
    return _build_new_session_state(
        message, user_id, user_name, session_id,
        grade_level=grade_level, role=role, image_base64=image_base64,
        experiment_title=experiment_title,
    )


# 这些角色不直接流式转发 LLM token，而是在图节点结束后输出整理好的 reply_content
_JSON_REPLY_ROLES = frozenset({"student", "pre_experiment", "post_experiment"})


async def _stream_agent_response(graph, input_state: dict, config: dict, role: str = "student"):
    """流式输出 Agent 响应，带 heartbeat 防网关超时。"""
    session_id = config.get("configurable", {}).get("thread_id", "")
    yield serialize_meta(session_id=session_id)

    structured_json = role in _JSON_REPLY_ROLES
    reply_sent = False
    final_output = None
    diagnosis_report: dict[str, Any] = {}
    HEARTBEAT_INTERVAL = 15.0
    _last_heartbeat = time.monotonic()

    try:
        async for event in graph.astream_events(input_state, config=config, version="v2"):
            now = time.monotonic()
            if now - _last_heartbeat >= HEARTBEAT_INTERVAL:
                yield serialize_heartbeat()
                _last_heartbeat = now

            event_type = event.get("event", "")
            data = event.get("data", {})

            if event_type == "on_chat_model_stream":
                if structured_json:
                    continue
                chunk = data.get("chunk")
                if chunk and hasattr(chunk, "content") and chunk.content:
                    yield serialize_token(chunk.content)
                    reply_sent = True

            elif event_type == "on_chat_model_end":
                if structured_json or reply_sent:
                    continue
                output = data.get("output")
                if output and hasattr(output, "content") and output.content:
                    yield serialize_token(str(output.content))
                    reply_sent = True

            elif event_type == "on_chain_end":
                output = data.get("output", {})
                if isinstance(output, dict):
                    if output.get("diagnosis_report"):
                        diagnosis_report = output["diagnosis_report"]
                    if not reply_sent:
                        final_output = output
                        reply = output.get("reply_content")
                        if not reply and output.get("messages"):
                            for m in reversed(output["messages"]):
                                if hasattr(m, "content") and isinstance(m.content, str) and m.content:
                                    reply = m.content
                                    break
                        if reply:
                            yield serialize_token(reply)
                            reply_sent = True

        if not reply_sent:
            if final_output and final_output.get("reply_content"):
                yield serialize_token(final_output["reply_content"])
                reply_sent = True

        if final_output and final_output.get("stage_advanced"):
            yield serialize_stage(
                stage=final_output.get("current_stage", ""),
                grade_level=final_output.get("grade_level", ""),
            )

        if diagnosis_report:
            yield serialize_diagnosis_report(diagnosis_report)

        yield serialize_done()

    except Exception as e:
        logger.error(f"流式处理失败: {e}", exc_info=True)
        yield serialize_error(str(e))
        yield serialize_done()
