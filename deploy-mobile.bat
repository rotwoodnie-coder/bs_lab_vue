@echo off
REM ============================================================
REM 移动端打包脚本 - Deploy Mobile Package
REM 打包范围：frontend/mobile + backend/.../mobile + agents_service
REM 使用：在项目根目录执行 deploy-mobile.bat
REM ============================================================
chcp 65001 >nul

set OUTPUT_DIR=deploy-mobile-pkg
set GIT_BRANCH=master

if "%1"=="" (
  echo 用法: deploy-mobile [分支名]
  echo 默认分支: %GIT_BRANCH%
  echo.
  echo 示例:
  echo   deploy-mobile          ^(默认当前分支^)
  echo   deploy-mobile mobile-dev ^(指定 mobile-dev 分支^)
) else (
  set GIT_BRANCH=%1
)

echo [1/4] 检查 git 分支: %GIT_BRANCH%
git rev-parse --verify %GIT_BRANCH% >nul 2>&1
if %ERRORLEVEL% neq 0 (
  echo 错误: 分支 %GIT_BRANCH% 不存在
  exit /b 1
)

echo [2/4] 创建输出目录: %OUTPUT_DIR%\
if exist %OUTPUT_DIR% (
  rmdir /s /q %OUTPUT_DIR%
)
mkdir %OUTPUT_DIR%\mobile-backend
mkdir %OUTPUT_DIR%\mobile-frontend
mkdir %OUTPUT_DIR%\agents-service
mkdir %OUTPUT_DIR%\shared-deps

echo [3/4] 导出移动端文件
REM --- 3a. 后端 mobile 包 ---
git archive %GIT_BRANCH% --format=zip -o %OUTPUT_DIR%\mobile-backend.zip ^
  backend/src/main/java/com/xuanyue/exp/mobile/
REM --- 3b. 前端 mobile ---
git archive %GIT_BRANCH% --format=zip -o %OUTPUT_DIR%\mobile-frontend.zip ^
  frontend/mobile/
REM --- 3c. agents_service ---
git archive %GIT_BRANCH% --format=zip -o %OUTPUT_DIR%\agents-service.zip ^
  agents_service/
REM --- 3d. 共享依赖（供部署参考） ---
git archive %GIT_BRANCH% --format=zip -o %OUTPUT_DIR%\shared-deps.zip ^
  backend/src/main/java/com/xuanyue/exp/common/storage/minio/ ^
  backend/src/main/java/com/xuanyue/exp/exp/service/impl/ExpSimulatorServiceImpl.java ^
  backend/src/main/java/com/xuanyue/exp/exp/repository/ExpSimulatorRepository.java ^
  backend/src/main/java/com/xuanyue/exp/exp/repository/ExpGradeRepository.java ^
  backend/src/main/java/com/xuanyue/exp/exp/dto/ExpSimulatorPageQuery.java ^
  backend/src/main/resources/application.yml ^
  backend/src/main/resources/application-dev.yml

REM 解压到子目录（方便查看）
echo [4/4] 解压到子目录 ^(可选参考^)
REM （手工解压）

echo.
echo ========================================
echo  打包完成！
echo.
echo  输出: %OUTPUT_DIR%\
echo   ├── mobile-backend\        ^(后端 mobile 包^)
echo   ├── mobile-frontend\       ^(前端 mobile^)
echo   ├── agents-service\        ^(智能体服务^)
echo   ├── shared-deps\           ^(共享依赖参考^)
echo.
echo  上传到服务器: 将上述目录覆盖对应路径即可
echo ========================================
echo.
echo 警告: shared-deps 为后端共享依赖修改，
echo 部署者需手动合并到现有后端代码。
echo.
pause
