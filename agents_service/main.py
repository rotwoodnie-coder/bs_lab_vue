"""
BS Lab LangGraph Agent 服务 — 主入口（新架构）

启动方式:
    uvicorn main:app --host 0.0.0.0 --port 5001 --reload
"""

from __future__ import annotations

import logging

from config import settings
from agents_framework.checkpointer import create_checkpointer
from agents_framework.router import AgentRouter
from agents_framework.server import create_agent_app

from bs_lab_adapter.graphs.student_graph import create_student_graph

# ─── 日志配置 ─────────────────────────────────────────

logging.basicConfig(
    level=getattr(logging, settings.log_level.upper(), logging.INFO),
    format="%(asctime)s [%(name)s] %(levelname)s %(message)s",
)
logger = logging.getLogger("agents-service")


# ─── 初始化 Checkpointer ──────────────────────────────
# CHECKPOINTER_URL → 独立检查点数据库（推荐）
# 未配置时才 fallback 到 DATABASE_URL
_checkpointer_url = settings.checkpointer_url or settings.database_url
checkpointer = create_checkpointer(_checkpointer_url)


# ─── 注册智能体路由 ───────────────────────────────────

router = AgentRouter()

# 石头老师（学生端）
student_graph = create_student_graph(checkpointer=checkpointer)
router.register("student", student_graph)

# TODO: 后续 PR 补充
# from bs_lab_adapter.graphs.teacher_graph import create_teacher_graph
# from bs_lab_adapter.graphs.researcher_graph import create_researcher_graph
# from bs_lab_adapter.graphs.parent_graph import create_parent_graph
# router.register("teacher", create_teacher_graph(checkpointer=checkpointer))
# router.register("researcher", create_researcher_graph(checkpointer=checkpointer))
# router.register("parent", create_parent_graph(checkpointer=checkpointer))


# ─── 创建 FastAPI 应用 ────────────────────────────────

app = create_agent_app(
    agent_router=router,
    checkpointer=checkpointer,
    title="BS Lab Agent Service (新架构)",
    version="2.0.0",
)

logger.info(f"Agent 服务启动完成，可用角色: {router.available_roles}")
logger.info(f"端口: {settings.port}, LLM: {settings.llm_model} @ {settings.llm_base_url}")


# ─── 主入口 ───────────────────────────────────────────

if __name__ == "__main__":
    import uvicorn

    uvicorn.run(
        "main:app",
        host=settings.host,
        port=settings.port,
        reload=True,
        log_level=settings.log_level,
    )
