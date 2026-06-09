"""
Agent Router — 智能体路由层

统一入口 POST /v1/chat，根据 thread_id/role 分发到对应智能体。
"""

from __future__ import annotations

import logging
from typing import Any, Optional

from fastapi import HTTPException
from langgraph.graph.state import CompiledStateGraph

logger = logging.getLogger("agents_framework.router")


class AgentRouter:
    """智能体路由器。

    用法:
        router = AgentRouter()
        router.register("student", student_graph)
        router.register("teacher", teacher_graph)
        graph, resolved_thread_id = await router.route(message, role, thread_id)
    """

    def __init__(self) -> None:
        self._agents: dict[str, CompiledStateGraph] = {}

    def register(self, role: str, graph: CompiledStateGraph) -> None:
        """注册一个智能体。"""
        self._agents[role] = graph
        logger.info("Router 注册 agent: %s", role)

    @property
    def available_roles(self) -> list[str]:
        return list(self._agents.keys())

    async def resolve(
        self,
        role: str,
        thread_id: Optional[str] = None,
        checkpointer: Any = None,
    ) -> tuple[CompiledStateGraph, str]:
        """解析目标智能体。

        Args:
            role: 请求的智能体角色
            thread_id: 会话 ID（可能为空）
            checkpointer: 用于查询历史会话的 agent_type

        Returns:
            (compiled_graph, resolved_thread_id)

        Raises:
            HTTPException 404: role 未注册
            HTTPException 409: thread_id 的 agent_type 与 role 不匹配
        """
        # 校验 role 是否注册
        if role not in self._agents:
            raise HTTPException(
                status_code=404,
                detail=f"未知智能体: {role}，可用: {', '.join(self.available_roles)}",
            )

        # AgentTypeValidator：thread_id 存在时校验一致性
        if thread_id and checkpointer:
            try:
                saved_state = await checkpointer.aget_tuple(
                    {"configurable": {"thread_id": thread_id}}
                )
                if saved_state and saved_state.metadata:
                    saved_agent = saved_state.metadata.get("agent_type", "")
                    if saved_agent and saved_agent != role:
                        raise HTTPException(
                            status_code=409,
                            detail=(
                                f"thread_id {thread_id} 属于「{saved_agent}」智能体，"
                                f"无法路由到「{role}」。如需切换请使用新的 thread_id。"
                            ),
                        )
            except HTTPException:
                raise
            except Exception:
                logger.warning(
                    "AgentTypeValidator 查询失败，跳过校验: thread_id=%s", thread_id
                )

        return self._agents[role], thread_id
