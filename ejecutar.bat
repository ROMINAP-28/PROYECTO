@echo off
chcp 65001 >nul
echo ===================================================
echo   TRAVELINK - Spring Boot 3 + API REST (Puerto 8080)
echo ===================================================

cd /d "%~dp0"

echo Iniciando Travelink con Maven Wrapper (perfil dev)...
call mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Error durante la ejecucion de la aplicacion.
    pause
    exit /b %ERRORLEVEL%
)
