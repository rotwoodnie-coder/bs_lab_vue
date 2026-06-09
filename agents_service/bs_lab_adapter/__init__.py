"""
bs_lab_adapter — 科学教育领域的 Agent 适配器

提供：
- 四个智能体的 LangGraph 实现（student/teacher/researcher/parent）
- 领域工具函数（实验DB、课标、报告）
- 语义安全过滤器（SemanticSafetyFilter）
- 工具注册器（将领域工具注册到通用框架）
"""

from bs_lab_adapter.safety import SemanticSafetyFilter, SafetyJudgement, SafetyResult
from bs_lab_adapter.graphs.student_graph import create_student_graph

__all__ = [
    "SemanticSafetyFilter",
    "SafetyJudgement",
    "SafetyResult",
    "create_student_graph",
]
