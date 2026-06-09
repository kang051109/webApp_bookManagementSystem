@echo off
chcp 65001 >nul
title Init Database

set SCHEMA_FILE="%~dp0src\main\resources\sql\schema.sql"
set MYSQL_CMD="C:\Program Files\MySQL\MySQL Workbench 8.0\mysql.exe"

echo ========================================
echo     Initialize book_management Database
echo ========================================
echo.
echo Schema file: %SCHEMA_FILE%
echo.
echo Using MySQL at: %MYSQL_CMD%
echo Password: mysql123
echo.

%MYSQL_CMD% -u root -pmysql123 < %SCHEMA_FILE%

if %ERRORLEVEL% EQU 0 (
    echo.
    echo SUCCESS - Database initialized
    echo.
    echo Now start the frontend:
    echo   cd frontend ^&^& npm run dev
    echo.
    echo Login: admin / admin123
) else (
    echo.
    echo FAILED - Try running manually in MySQL Workbench:
    echo   1. Open MySQL Workbench
    echo   2. File - Open SQL Script
    echo   3. Select: %SCHEMA_FILE%
    echo   4. Execute (Ctrl+Shift+Enter)
)

echo.
pause
