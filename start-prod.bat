@echo off
chcp 65001 > nul
title BS Lab - 生产模式 (Docker)

echo ============================================
echo  宝山小实验社区 - 一键 Docker 部署
echo ============================================
echo.
echo 启动所有容器服务 (后端 + PC 前端 + 移动端 + AI)...
docker compose up -d

echo.
echo ============================================
echo  ✅ 所有服务已启动！
echo.
echo  PC 管理端:   http://localhost:8080
echo  移动端:      http://localhost:8081
echo  AI 服务:     http://localhost:5001/health
echo.
echo  查看日志: docker compose logs -f
echo  停止服务: docker compose down
echo.
pause
