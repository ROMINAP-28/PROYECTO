@echo off
chcp 65001 >nul
echo ===================================================
echo   TRAVELINK - Spring Boot 3 + API REST (Puerto 8080)
echo ===================================================

cd /d "%~dp0"

if exist "%~dp0.env" (
    echo [INFO] Cargando variables de entorno desde .env...
    for /f "usebackq eol=# tokens=1,* delims==" %%i in ("%~dp0.env") do (
        if not "%%i"=="" (
            set "%%i=%%j"
        )
    )
) else (
    echo [ADVERTENCIA] No se encontro archivo .env en la raiz. Se usaran variables del sistema si existen.
)

echo Iniciando Travelink con Maven Wrapper (perfil dev)...
call mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Error durante la ejecucion de la aplicacion.
    pause
    exit /b %ERRORLEVEL%
)
