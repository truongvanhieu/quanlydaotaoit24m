@echo off
chcp 65001 >nul
cd /d "%~dp0"
if not exist "node_modules\vite" (
  echo [FRONTEND] Dang cai dat thu vien npm...
  call npm install
  if errorlevel 1 (
    echo [ERROR] Khong cai duoc frontend libraries.
    pause
    exit /b 1
  )
)
call npm run dev
pause
