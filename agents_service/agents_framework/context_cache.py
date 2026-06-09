"""
Context Cache — 共享上下文缓存

三层上下文模型 + role_access_mask 权限遮罩。
"""

from __future__ import annotations

import json
import logging
import time
from typing import Any, Optional

logger = logging.getLogger("agents_framework.context_cache")

# ─── 权限遮罩定义 ──────────────────────────────────────

# layer3_detail: true=可读完整上下文, false=仅摘要
ROLE_ACCESS_MASK: dict[str, dict[str, bool]] = {
    "student":   {"layer3_detail": False},
    "parent":    {"layer3_detail": False},
    "teacher":   {"layer3_detail": True},
    "researcher": {"layer3_detail": True},
}


# ─── 内存缓存（单进程，重启丢失） ──────────────────────

class _MemoryCache:
    """简易内存缓存，带 TTL。"""

    def __init__(self) -> None:
        self._data: dict[str, tuple[Any, float]] = {}

    def get(self, key: str) -> Any | None:
        if key in self._data:
            value, expires = self._data[key]
            if time.monotonic() < expires:
                return value
            del self._data[key]
        return None

    def set(self, key: str, value: Any, ttl_seconds: int = 3600) -> None:
        self._data[key] = (value, time.monotonic() + ttl_seconds)


_memory = _MemoryCache()


# ─── ContextCache ──────────────────────────────────────


class ContextCache:
    """共享上下文缓存。

    三层：
      Layer 1 — 用户画像（永久，存 MySQL ai_chat_session）
      Layer 2 — 会话状态（临时，存 Checkpointer）
      Layer 3 — 跨 Agent 摘要（24h TTL，内存 + MySQL 双写）

    不依赖 Redis，纯内存+MySQL。
    """

    def __init__(self, repository=None) -> None:
         self._repo = repository  # repository 模块，用于读写 MySQL

    # ─── Layer 1: 用户画像 ────────────────────────────

    async def get_user_profile(self, user_id: str) -> dict[str, Any]:
        """获取用户画像（年级等长期属性）。"""
        # 先查内存
        cached = _memory.get(f"profile:{user_id}")
        if cached:
            return cached
        # 再查 MySQL（通过 repository）
        profile: dict[str, Any] = {"grade_level": None}
        if self._repo:
            try:
                sessions = await self._repo.get_user_recent_sessions(
                    user_id, limit=5
                )
                for s in sessions:
                    if s.grade_level:
                        profile["grade_level"] = s.grade_level
                        break
            except Exception as e:
                logger.warning("查询用户画像失败: %s", e)
        _memory.set(f"profile:{user_id}", profile, ttl_seconds=300)
        return profile

    # ─── Layer 2: 会话状态 ────────────────────────────

    @staticmethod
    async def get_session_context(checkpointer, thread_id: str) -> dict[str, Any]:
        """从 checkpointer 获取当前会话上下文。"""
        try:
            saved = await checkpointer.aget_tuple(
                {"configurable": {"thread_id": thread_id}}
            )
            if saved and saved.checkpoint:
                cv = saved.checkpoint.get("channel_values", {})
                return {
                    "current_stage": cv.get("current_stage", "INIT"),
                    "experiment_title": cv.get("experiment_title"),
                    "grade_level": cv.get("grade_level"),
                }
        except Exception as e:
            logger.warning("获取会话上下文失败: %s", e)
        return {}

    # ─── Layer 3: 跨 Agent 摘要 ───────────────────────

    async def get_cross_agent_summary(
        self, user_id: str, role: str
    ) -> dict[str, Any]:
        """获取跨 Agent 摘要（受 role_access_mask 控制）。"""
        mask = ROLE_ACCESS_MASK.get(role, {})
        can_read_detail = mask.get("layer3_detail", False)

        cached = _memory.get(f"cross:{user_id}")
        if cached:
            return cached if can_read_detail else {"summary": cached.get("summary", "")}

        summary: dict[str, Any] = {"summary": "", "recent_topics": []}
        if self._repo:
            try:
                sessions = await self._repo.get_user_recent_sessions(
                    user_id, limit=3
                )
                topics = []
                for s in sessions:
                    if s.experiment_title:
                        topics.append({
                            "experiment": s.experiment_title,
                            "stage": s.current_stage,
                            "agent": s.agent_type,
                        })
                    if s.summary and not summary["summary"]:
                        summary["summary"] = s.summary
                summary["recent_topics"] = topics
            except Exception as e:
                logger.warning("查询跨 Agent 摘要失败: %s", e)

        _memory.set(f"cross:{user_id}", summary, ttl_seconds=86400)
        return summary if can_read_detail else {"summary": summary.get("summary", "")}

    # ─── 更新跨 Agent 摘要 ────────────────────────────

    async def update_cross_agent_summary(
        self, user_id: str, session_id: str, summary: str
    ) -> None:
        """更新跨 Agent 摘要（Layer 3 写入）。"""
        if not summary:
            return
        _memory.set(f"cross:{user_id}", {"summary": summary}, ttl_seconds=86400)
        logger.info(
            "已更新用户 %s 的跨 Agent 摘要: %.40s", user_id, summary
        )
