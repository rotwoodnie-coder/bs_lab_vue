"""
Checkpointer 工厂 — 仅支持 MySQLSaver + TTLMemorySaver

无历史包袱：所有 PostgreSQL 相关的逻辑已移除。
"""

from __future__ import annotations

import asyncio
import logging
import time
from typing import Any, Optional

from langchain_core.runnables import RunnableConfig

from langgraph.checkpoint.base import ChannelVersions, Checkpoint, CheckpointMetadata
from langgraph.checkpoint.memory import MemorySaver

from agents_framework.mysql_checkpointer import MySQLSaver

logger = logging.getLogger("agents_framework.checkpointer")

_DEFAULT_TTL_SECONDS = 3600  # 1 小时
_MYSQL_PREFIX = "mysql+aiomysql://"


# ─── TTL MemorySaver ──────────────────────────────────────


class TTLMemorySaver(MemorySaver):
    """自愈型带 TTL 的内存 Checkpointer。"""

    def __init__(self, ttl_seconds: int = _DEFAULT_TTL_SECONDS, *args: Any, **kwargs: Any) -> None:
        super().__init__(*args, **kwargs)
        self.ttl_seconds = ttl_seconds
        self.last_accessed: dict[str, float] = {}

    def put(
        self,
        config: RunnableConfig,
        checkpoint: Checkpoint,
        metadata: CheckpointMetadata,
        new_versions: ChannelVersions,
    ) -> RunnableConfig:
        configurable = config.get("configurable", {})
        thread_id = configurable.get("thread_id")
        if thread_id:
            self.last_accessed[thread_id] = time.time()
        self._collect_garbage()
        return super().put(config, checkpoint, metadata, new_versions)

    def _collect_garbage(self) -> None:
        now = time.time()
        expired = [
            tid for tid, ts in self.last_accessed.items()
            if now - ts > self.ttl_seconds
        ]
        for thread_id in expired:
            try:
                keys = [
                    k for k in self.storage.keys()
                    if k == thread_id or (isinstance(k, tuple) and k[0] == thread_id)
                ]
                for k in keys:
                    del self.storage[k]
                if thread_id in self.last_accessed:
                    del self.last_accessed[thread_id]
                logger.info("[GC] 会话 %s 已过期，已淘汰", thread_id)
            except Exception as e:
                logger.error("[GC] 清理会话 %s 出错: %s", thread_id, e)


# ─── 辅助函数 ──────────────────────────────────────────────


def _run_async(coro) -> None:
    """在同步上下文中运行异步协程。"""
    try:
        loop = asyncio.get_running_loop()
        if loop.is_running():
            import threading
            def _run():
                nl = asyncio.new_event_loop()
                asyncio.set_event_loop(nl)
                try:
                    nl.run_until_complete(coro)
                finally:
                    nl.close()
            t = threading.Thread(target=_run, daemon=True)
            t.start()
            t.join()
            return
    except RuntimeError:
        pass
    loop = asyncio.new_event_loop()
    asyncio.set_event_loop(loop)
    try:
        loop.run_until_complete(coro)
    finally:
        loop.close()


# ─── 工厂函数 ──────────────────────────────────────────────


def create_checkpointer(
    connection_string: Optional[str] = None,
    memory_ttl_seconds: int = _DEFAULT_TTL_SECONDS,
) -> Any:
    """创建并初始化 checkpointer。

    自动选择策略：
    - MySQL 连接 → MySQLSaver（持久化）
    - 未配置或失败 → TTLMemorySaver（内存+TTL）

    Args:
        connection_string: MySQL 连接字符串（None 则使用 TTLMemorySaver）
        memory_ttl_seconds: 内存模式 TTL（秒）

    Returns:
        MySQLSaver / TTLMemorySaver 实例
    """
    if not connection_string:
        logger.info("未配置连接，使用 TTLMemorySaver（ttl=%ss）", memory_ttl_seconds)
        return TTLMemorySaver(ttl_seconds=memory_ttl_seconds)

    if connection_string.startswith(("mysql://", "mysql+aiomysql://")):
        # 标准化连接字符串
        if connection_string.startswith("mysql://"):
            connection_string = "mysql+aiomysql://" + connection_string[len("mysql://"):]
        try:
            saver = MySQLSaver(connection_string)
            _run_async(saver.setup())
            logger.info("MySQL checkpointer 初始化完成")
            return saver
        except Exception as e:
            logger.warning("MySQL checkpointer 初始化失败，降级为 TTLMemorySaver: %s", e)
            return TTLMemorySaver(ttl_seconds=memory_ttl_seconds)

    logger.warning("无法识别的连接字符串，降级为 TTLMemorySaver: %s...", connection_string[:30])
    return TTLMemorySaver(ttl_seconds=memory_ttl_seconds)
