"""MySQL 连接串校验与诊断（供 checkpointer / database 共用）。"""

from __future__ import annotations

from urllib.parse import quote, urlparse, parse_qs, urlencode, ParseResult

# aiomysql 不支持的 JDBC 专有参数，这些参数对 SQLAlchemy/aiomysql 无意义
_JDBC_QUERY_PARAMS_TO_STRIP = frozenset({
    "useUnicode",
    "characterEncoding",
    "useSSL",
    "serverTimezone",
    "autoReconnect",
    "allowPublicKeyRetrieval",
})


def normalize_mysql_driver_url(url: str) -> str:
    """标准化 MySQL 连接串：确保异步驱动前缀，并剥离 JDBC 专有参数。"""
    if url.startswith("mysql://"):
        url = url.replace("mysql://", "mysql+aiomysql://", 1)
    return strip_jdbc_params(url)


def strip_jdbc_params(url: str) -> str:
    """从 MySQL 连接串中移除 aiomysql 不支持的 JDBC 查询参数。"""
    if "?" not in url:
        return url
    parsed: ParseResult = urlparse(url)
    params = parse_qs(parsed.query, keep_blank_values=True)
    stripped = {k: v for k, v in params.items() if k not in _JDBC_QUERY_PARAMS_TO_STRIP}
    if len(stripped) == len(params):
        return url  # 没有需要移除的参数
    new_query = urlencode(stripped, doseq=True) if stripped else ""
    return parsed._replace(query=new_query).geturl()


def diagnose_mysql_url(url: str) -> str | None:
    """若 URL 明显不合法，返回中文诊断说明；合法则返回 None。"""
    if not url:
        return "DATABASE_URL 为空"

    normalized = normalize_mysql_driver_url(url.strip())
    if not normalized.startswith("mysql+aiomysql://"):
        return f"无法识别的连接串前缀: {url[:40]}..."

    rest = normalized.split("://", 1)[1]
    if "@" not in rest:
        return (
            "连接串缺少 @，无法区分密码与主机。"
            "正确示例: mysql+aiomysql://root:密码@10.0.181.204:13306/bs_exp_vue"
        )

    try:
        from sqlalchemy.engine.url import make_url

        make_url(normalized)
        return None
    except Exception as exc:
        message = str(exc)
        if "invalid literal for int()" in message:
            return (
                "连接串中主机或端口解析失败。"
                "常见原因：① 密码与主机之间漏写 @；② 密码含 @ 等特殊字符未 URL 编码（@ 应写成 %40）。"
                f" 解析错误: {message}"
            )
        return f"连接串格式错误: {message}"


def encode_mysql_password(password: str) -> str:
    """对用户名/密码做 URL 编码（保留常见安全字符）。"""
    return quote(password, safe="")


def build_mysql_url(
    user: str,
    password: str,
    host: str,
    port: int | str,
    database: str,
    *,
    query: str = "",
) -> str:
    """从分项配置组装标准 MySQL 异步连接串。"""
    auth = f"{encode_mysql_password(user)}:{encode_mysql_password(password)}"
    base = f"mysql+aiomysql://{auth}@{host}:{port}/{database}"
    if query:
        return f"{base}?{query.lstrip('?')}"
    return base
