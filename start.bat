@echo off
chcp 936 >nul
cd /d "%~dp0"

echo Starting Backend Server...
if exist "plant-adoption-server" (
cd plant-adoption-server
start cmd /k mvn spring-boot:run
cd ..
)

timeout /t 10 /nobreak >nul

echo Starting H5 Frontend...
if exist "plant-adoption-h5" (
cd plant-adoption-h5
start cmd /k pnpm dev:h5
cd ..
)

timeout /t 5 /nobreak >nul

echo Starting Admin Frontend...
if exist "plant-adoption-admin" (
cd plant-adoption-admin
start cmd /k pnpm dev
cd ..
)

echo All Services Started!
pause