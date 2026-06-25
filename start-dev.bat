@echo off
chcp 65001 > nul
title BS Lab - 开发模式

echo ============================================
echo  宝山小实验社区 - 开发环境一键启动
echo  启动后端 + PC 前端
echo ============================================
echo.
echo 启动后端服务 (Spring Boot)...
start "Backend" cmd /c "cd /d %~dp0backend && mvn spring-boot:run -Dspring.profiles.active=dev"
echo 后端启动中... (等待 30 秒让后端先就绪)
timeout /t 30 /nobreak > nul

echo.
echo 启动 PC 前端 (Vite 8080)...
start "Frontend PC" cmd /c "cd /d %~dp0frontend && pnpm dev"

echo.
echo 启动移动端构建监听 (如需开发移动端请取消注释)
rem start "Frontend Mobile" cmd /c "cd /d %~dp0frontend/mobile && pnpm dev:lan"

echo.
echo ============================================
echo  ✅ 所有服务已启动！
echo.
echo  PC 管理端:   http://localhost:8080
echo  移动端:      http://localhost:8010/m/
echo  AI 服务:     http://localhost:5001/health
echo.
echo  按任意键关闭所有服务...
pause > nul

taskkill /f /fi "WINDOWTITLE eq Backend" > nul 2>&1
taskkill /f /fi "WINDOWTITLE eq Frontend PC" > nul 2>&1
taskkill /f /fi "WINDOWTITLE eq Frontend Mobile" > nul 2>&1

echo 服务已关闭。
timeout /t 2 /nobreak > nul
