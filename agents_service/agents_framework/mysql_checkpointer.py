"""
MySQL Checkpointer — LangGraph 持久化存储后端

基于 SQLAlchemy (aiomysql) 实现 BaseCheckpointSaver 接口，
用 MySQL 完全替代 PostgreSQL 作为 LangGraph 检查点存储。

使用方法:
    from agents_framework.mysql_checkpointer import MySQLSaver

    saver = MySQLSaver("mysql+aiomysql://user:pass@host:port/db")
    await saver.setup()  # 建表
    graph = builder.compile(checkpointer=saver)
"""

from __future__ import annotations

import json
import logging
from typing import Any, AsyncIterator, Optional, Sequence

from sqlalchemy import text
from sqlalchemy.ext.asyncio import AsyncEngine, create_async_engine

from langchain_core.runnables import RunnableConfig

from langgraph.checkpoint.base import (
    BaseCheckpointSaver,
    ChannelVersions,
    Checkpoint,
    CheckpointMetadata,
    CheckpointTuple,
    get_checkpoint_id,
    WRITES_IDX_MAP,
)
from langgraph.checkpoint.serde.base import SerializerProtocol
from langgraph.checkpoint.serde.types import ERROR, INTERRUPT, RESUME, SCHEDULED

logger = logging.getLogger("agents_framework.mysql_checkpointer")

# ─── 工具函数 ─────────────────────────────────────────────


def _normalize_mysql_url(url: str) -> str:
    """确保 MySQL URL 使用 aiomysql 异步驱动前缀。"""
    if url.startswith("mysql://"):
        return url.replace("mysql://", "mysql+aiomysql://", 1)
    return url


def _make_config(
    thread_id: str,
    checkpoint_ns: str = "",
    checkpoint_id: str = "",
) -> RunnableConfig:
    """构建标准 RunnableConfig。"""
    result: RunnableConfig = {
        "configurable": {
            "thread_id": thread_id,
            "checkpoint_ns": checkpoint_ns,
        },
    }
    if checkpoint_id:
        result["configurable"]["checkpoint_id"] = checkpoint_id  # type: ignore[assignment]
    return result


# ─── MySQLSaver ──────────────────────────────────────────


class MySQLSaver(BaseCheckpointSaver):
    """基于 MySQL + aiomysql 的 LangGraph Checkpointer 实现。

    完全替代 PostgresSaver，实现单一 MySQL 数据库部署。
    数据表（自动创建）:
        langgraph_checkpoints         — 检查点主表
        langgraph_checkpoint_writes   — 检查点中间写入表

    注意：在当前 LangGraph 版本下，serde (JsonPlusSerializer)
    使用 dumps_typed() / loads_typed() 方法进行序列化，
    返回 (type_str, blob) 元组。表中用 serde_type 列存储类型名。
    """

    def __init__(
        self,
        engine_or_url: str | AsyncEngine,
        *,
        serde: SerializerProtocol | None = None,
    ) -> None:
        super().__init__(serde=serde)
        if isinstance(engine_or_url, str):
            url = _normalize_mysql_url(engine_or_url)
            self.engine: AsyncEngine = create_async_engine(
                url,
                pool_size=5,
                max_overflow=10,
                pool_recycle=300,
                pool_pre_ping=True,
                echo=False,
            )
            logger.info("MySQL checkpointer 引擎已创建 (pool_size=5, max_overflow=10)")
        else:
            self.engine = engine_or_url

    # ─── 建表 ──────────────────────────────────────────

    async def setup(self) -> None:
        """创建 langgraph_checkpoints 和 langgraph_checkpoint_writes 表。"""
        async with self.engine.begin() as conn:
            await conn.execute(text("""
                CREATE TABLE IF NOT EXISTS langgraph_checkpoints (
                    thread_id          VARCHAR(128)   NOT NULL,
                    checkpoint_ns      VARCHAR(255)   NOT NULL DEFAULT '',
                    checkpoint_id      VARCHAR(64)    NOT NULL,
                    parent_checkpoint_id VARCHAR(64)  DEFAULT NULL,
                    checkpoint         LONGBLOB       DEFAULT NULL,
                    serde_type         VARCHAR(32)    DEFAULT NULL  COMMENT '序列化类型（如 msgpack/json）',
                    metadata           JSON           DEFAULT NULL,
                    created_at         TIMESTAMP(6)   DEFAULT CURRENT_TIMESTAMP(6),
                    PRIMARY KEY (thread_id, checkpoint_ns, checkpoint_id),
                    INDEX idx_thread_created (thread_id, checkpoint_ns, created_at DESC)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
            """))

            await conn.execute(text("""
                CREATE TABLE IF NOT EXISTS langgraph_checkpoint_writes (
                    thread_id      VARCHAR(128)  NOT NULL,
                    checkpoint_ns  VARCHAR(255)  NOT NULL DEFAULT '',
                    checkpoint_id  VARCHAR(64)   NOT NULL,
                    task_id        VARCHAR(64)   NOT NULL,
                    idx            INT           NOT NULL,
                    channel        VARCHAR(128)  NOT NULL,
                    write_type     VARCHAR(32)   DEFAULT NULL  COMMENT '写入类型（error/interrupt/resume）',
                    value          LONGBLOB      DEFAULT NULL,
                    serde_type     VARCHAR(32)   DEFAULT NULL  COMMENT '序列化类型（如 msgpack/json）',
                    PRIMARY KEY (thread_id, checkpoint_ns, checkpoint_id, task_id, idx)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
            """))

        logger.info("MySQL checkpointer 数据表已就绪 (langgraph_checkpoints, langgraph_checkpoint_writes)")

    # ─── 核心查询 ──────────────────────────────────────

    async def aget_tuple(self, config: RunnableConfig) -> CheckpointTuple | None:
        """异步获取指定 config 的检查点元组。"""
        thread_id = config["configurable"]["thread_id"]
        checkpoint_ns = config["configurable"].get("checkpoint_ns", "")
        checkpoint_id = get_checkpoint_id(config)

        async with self.engine.connect() as conn:
            if checkpoint_id:
                query = text("""
                    SELECT thread_id, checkpoint_ns, checkpoint_id,
                           parent_checkpoint_id, checkpoint, serde_type, metadata
                    FROM langgraph_checkpoints
                    WHERE thread_id = :thread_id
                      AND checkpoint_ns = :checkpoint_ns
                      AND checkpoint_id = :checkpoint_id
                """)
                result = await conn.execute(query, {
                    "thread_id": thread_id,
                    "checkpoint_ns": checkpoint_ns,
                    "checkpoint_id": checkpoint_id,
                })
            else:
                # 查最新检查点
                query = text("""
                    SELECT thread_id, checkpoint_ns, checkpoint_id,
                           parent_checkpoint_id, checkpoint, serde_type, metadata
                    FROM langgraph_checkpoints
                    WHERE thread_id = :thread_id
                      AND checkpoint_ns = :checkpoint_ns
                    ORDER BY created_at DESC
                    LIMIT 1
                """)
                result = await conn.execute(query, {
                    "thread_id": thread_id,
                    "checkpoint_ns": checkpoint_ns,
                })

            row = result.fetchone()
            if row is None:
                return None

            t_id, ns, ckpt_id, parent_id, ckpt_data, serde_type, meta_data = row

            # 反序列化（JsonPlusSerializer.loads_typed 接收 (type_str, blob) 元组）
            checkpoint: Checkpoint = self.serde.loads_typed((serde_type, ckpt_data))
            metadata: CheckpointMetadata = (
                json.loads(meta_data) if meta_data else {}
            )

            # 父 config
            parent_config = None
            if parent_id:
                parent_config = _make_config(t_id, ns, parent_id)

            # 获取待处理写入
            writes_result = await conn.execute(
                text("""
                    SELECT task_id, idx, channel, write_type, value, serde_type
                    FROM langgraph_checkpoint_writes
                    WHERE thread_id = :thread_id
                      AND checkpoint_ns = :checkpoint_ns
                      AND checkpoint_id = :checkpoint_id
                    ORDER BY idx
                """),
                {
                    "thread_id": t_id,
                    "checkpoint_ns": ns,
                    "checkpoint_id": ckpt_id,
                },
            )

            pending_writes = []
            for wrow in writes_result:
                val = None
                if wrow.value is not None:
                    val = self.serde.loads_typed((wrow.serde_type, wrow.value))
                pending_writes.append((
                    wrow.task_id,
                    wrow.channel,
                    val,
                ))

            return CheckpointTuple(
                config=_make_config(t_id, ns, ckpt_id),
                checkpoint=checkpoint,
                metadata=metadata,
                parent_config=parent_config,
                pending_writes=pending_writes if pending_writes else None,
            )

    # ─── 核心写入 ──────────────────────────────────────

    async def aput(
        self,
        config: RunnableConfig,
        checkpoint: Checkpoint,
        metadata: CheckpointMetadata,
        new_versions: ChannelVersions,
    ) -> RunnableConfig:
        """异步存储一个检查点。"""
        thread_id = config["configurable"]["thread_id"]
        checkpoint_ns = config["configurable"].get("checkpoint_ns", "")
        checkpoint_id = checkpoint["id"]
        parent_checkpoint_id = get_checkpoint_id(config)

        serde_type, serialized_ckpt = self.serde.dumps_typed(checkpoint)
        serialized_meta = json.dumps(metadata)

        async with self.engine.begin() as conn:
            await conn.execute(
                text("""
                    INSERT INTO langgraph_checkpoints
                        (thread_id, checkpoint_ns, checkpoint_id,
                         parent_checkpoint_id, checkpoint, serde_type, metadata)
                    VALUES
                        (:thread_id, :checkpoint_ns, :checkpoint_id,
                         :parent_checkpoint_id, :checkpoint, :serde_type, :metadata)
                    ON DUPLICATE KEY UPDATE
                        checkpoint  = VALUES(checkpoint),
                        serde_type  = VALUES(serde_type),
                        metadata    = VALUES(metadata)
                """),
                {
                    "thread_id": thread_id,
                    "checkpoint_ns": checkpoint_ns,
                    "checkpoint_id": checkpoint_id,
                    "parent_checkpoint_id": parent_checkpoint_id,
                    "checkpoint": serialized_ckpt,
                    "serde_type": serde_type,
                    "metadata": serialized_meta,
                },
            )

        return _make_config(thread_id, checkpoint_ns, checkpoint_id)

    async def aput_writes(
        self,
        config: RunnableConfig,
        writes: Sequence[tuple[str, Any]],
        task_id: str,
        task_path: str = "",
    ) -> None:
        """异步存储检查点的中间写入。"""
        thread_id = config["configurable"]["thread_id"]
        checkpoint_ns = config["configurable"].get("checkpoint_ns", "")
        checkpoint_id = get_checkpoint_id(config)

        if not checkpoint_id:
            raise ValueError("aput_writes 需要 checkpoint_id")

        async with self.engine.begin() as conn:
            for idx, (channel, value) in enumerate(writes):
                # 判断是否为特殊写入类型（错误、中断等）
                write_type = None
                actual_idx = idx
                for sentinel_str, sentinel_idx in WRITES_IDX_MAP.items():
                    if channel == sentinel_str:
                        actual_idx = sentinel_idx
                        write_type = {
                            ERROR: "error",
                            INTERRUPT: "interrupt",
                            RESUME: "resume",
                            SCHEDULED: "scheduled",
                        }.get(channel, channel)
                        break

                serialized_value = None
                serde_type = None
                if value is not None:
                    serde_type, serialized_value = self.serde.dumps_typed(value)

                await conn.execute(
                    text("""
                        INSERT INTO langgraph_checkpoint_writes
                            (thread_id, checkpoint_ns, checkpoint_id,
                             task_id, idx, channel, write_type, value, serde_type)
                        VALUES
                            (:thread_id, :checkpoint_ns, :checkpoint_id,
                             :task_id, :idx, :channel, :write_type, :value, :serde_type)
                        ON DUPLICATE KEY UPDATE
                            value      = VALUES(value),
                            serde_type = VALUES(serde_type),
                            write_type = VALUES(write_type)
                    """),
                    {
                        "thread_id": thread_id,
                        "checkpoint_ns": checkpoint_ns,
                        "checkpoint_id": checkpoint_id,
                        "task_id": task_id,
                        "idx": actual_idx,
                        "channel": channel,
                        "write_type": write_type,
                        "value": serialized_value,
                        "serde_type": serde_type,
                    },
                )

    # ─── 列表迭代 ──────────────────────────────────────

    async def alist(
        self,
        config: RunnableConfig | None,
        *,
        filter: dict[str, Any] | None = None,
        before: RunnableConfig | None = None,
        limit: int | None = None,
    ) -> AsyncIterator[CheckpointTuple]:
        """异步列出匹配条件的检查点。"""
        conditions: list[str] = []
        params: dict[str, Any] = {}

        if config is not None:
            thread_id = config["configurable"]["thread_id"]
            checkpoint_ns = config["configurable"].get("checkpoint_ns", "")
            conditions.append("thread_id = :thread_id")
            params["thread_id"] = thread_id
            conditions.append("checkpoint_ns = :checkpoint_ns")
            params["checkpoint_ns"] = checkpoint_ns

        if before is not None:
            before_id = before["configurable"].get("checkpoint_id")
            if before_id:
                conditions.append("checkpoint_id < :before_id")
                params["before_id"] = before_id

        where_clause = " WHERE " + " AND ".join(conditions) if conditions else ""

        query_str = f"""
            SELECT thread_id, checkpoint_ns, checkpoint_id,
                   parent_checkpoint_id, checkpoint, serde_type, metadata
            FROM langgraph_checkpoints
            {where_clause}
            ORDER BY created_at DESC
        """
        if limit is not None:
            query_str += " LIMIT :limit"
            params["limit"] = limit

        async with self.engine.connect() as conn:
            result = await conn.execute(text(query_str), params)
            for row in result:
                t_id, ns, ckpt_id, parent_id, ckpt_data, serde_type, meta_data = row

                checkpoint = self.serde.loads_typed((serde_type, ckpt_data))
                metadata: CheckpointMetadata = (
                    json.loads(meta_data) if meta_data else {}
                )

                parent_config = None
                if parent_id:
                    parent_config = _make_config(t_id, ns, parent_id)

                yield CheckpointTuple(
                    config=_make_config(t_id, ns, ckpt_id),
                    checkpoint=checkpoint,
                    metadata=metadata,
                    parent_config=parent_config,
                )

    # ─── 线程管理 ──────────────────────────────────────

    async def adelete_thread(self, thread_id: str) -> None:
        """删除指定线程的所有检查点和写入。"""
        async with self.engine.begin() as conn:
            await conn.execute(
                text("DELETE FROM langgraph_checkpoint_writes WHERE thread_id = :thread_id"),
                {"thread_id": thread_id},
            )
            await conn.execute(
                text("DELETE FROM langgraph_checkpoints WHERE thread_id = :thread_id"),
                {"thread_id": thread_id},
            )
        logger.info("已删除线程 %s 的检查点数据", thread_id)

    async def acopy_thread(
        self,
        source_thread_id: str,
        target_thread_id: str,
    ) -> None:
        """复制一个线程的所有检查点和写入到新线程。"""
        async with self.engine.begin() as conn:
            await conn.execute(
                text("""
                    INSERT INTO langgraph_checkpoints
                        (thread_id, checkpoint_ns, checkpoint_id,
                         parent_checkpoint_id, checkpoint, serde_type, metadata, created_at)
                    SELECT
                        :target_id, checkpoint_ns, checkpoint_id,
                        parent_checkpoint_id, checkpoint, serde_type, metadata, created_at
                    FROM langgraph_checkpoints
                    WHERE thread_id = :source_id
                """),
                {"target_id": target_thread_id, "source_id": source_thread_id},
            )
            await conn.execute(
                text("""
                    INSERT INTO langgraph_checkpoint_writes
                        (thread_id, checkpoint_ns, checkpoint_id,
                         task_id, idx, channel, write_type, value, serde_type)
                    SELECT
                        :target_id, checkpoint_ns, checkpoint_id,
                        task_id, idx, channel, write_type, value, serde_type
                    FROM langgraph_checkpoint_writes
                    WHERE thread_id = :source_id
                """),
                {"target_id": target_thread_id, "source_id": source_thread_id},
            )
        logger.info("已复制线程 %s → %s", source_thread_id, target_thread_id)

    async def adelete_for_runs(self, run_ids: Sequence[str]) -> None:
        """按 run_id 删除检查点（暂不实现）。"""
        logger.warning("MySQLSaver 未实现 adelete_for_runs")
