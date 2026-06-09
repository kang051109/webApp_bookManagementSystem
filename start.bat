@echo off
chcp 65001 >nul
title Book Management System

echo ========================================
echo     Book Management System - Quick Start
echo ========================================
echo.

echo [1/2] Starting backend (Spring Boot)...
cd /d "%~dp0"
start "Spring Boot Backend" cmd /k "echo Backend starting on http://localhost:8080 && mvnw spring-boot:run"
echo   Backend starting on port 8080...

timeout /t 8 >nul

echo.
echo [2/2] Starting frontend dev server...
cd /d "%~dp0frontend"
if exist "package.json" (
    start "Vue Frontend" cmd /k "echo Frontend running at http://localhost:5173 && npm run dev"
    echo   OK - Frontend started
) else (
    echo   FAIL - frontend/package.json not found
)
cd /d "%~dp0"

echo.
echo ========================================
echo     Frontend: http://localhost:5173
echo     Backend:  http://localhost:8080/api
echo.
echo     Make sure:
echo     1. MySQL is running
echo     2. Database has been initialized
echo ========================================
echo.
pause
