@echo off
echo ========================================
echo 安装所有项目依赖
echo ========================================
echo.

echo [1/3] 安装后端依赖...
cd plant-adoption-server
call mvn clean install -DskipTests
cd ..
echo.

echo [2/3] 安装H5前端依赖...
cd plant-adoption-h5
call pnpm install
cd ..
echo.

echo [3/3] 安装管理后台依赖...
cd plant-adoption-admin
call pnpm install
cd ..
echo.

echo ========================================
echo 所有依赖安装完成！
echo ========================================
pause
