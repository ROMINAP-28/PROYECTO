@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo ===================================================
echo   TRAVELINK - Respaldo de Base de Datos (MySQL)
echo ===================================================

cd /d "%~dp0"

REM 1. Cargar variables desde .env si existe
if exist "%~dp0..\.env" (
    echo [INFO] Cargando credenciales desde .env...
    for /f "usebackq eol=# tokens=1,* delims==" %%i in ("%~dp0..\.env") do (
        if not "%%i"=="" (
            set "%%i=%%j"
        )
    )
) else if exist "%~dp0.env" (
    for /f "usebackq eol=# tokens=1,* delims==" %%i in ("%~dp0.env") do (
        if not "%%i"=="" (
            set "%%i=%%j"
        )
    )
)

REM 2. Validar variables minimas
if "%DB_USERNAME%"=="" (
    echo [ERROR] Variable DB_USERNAME no definida.
    exit /b 1
)
if "%DB_PASSWORD%"=="" (
    echo [ERROR] Variable DB_PASSWORD no definida.
    exit /b 1
)

REM 3. Extraer host, puerto y base de datos de DB_URL si no estan definidos
if "%DB_HOST%"=="" set "DB_HOST=localhost"
if "%DB_PORT%"=="" set "DB_PORT=3306"
if "%DB_NAME%"=="" set "DB_NAME=DBTravelink"

if not "%DB_URL%"=="" (
    for /f "tokens=3 delims=/:?" %%a in ("%DB_URL%") do set "DB_HOST=%%a"
    for /f "tokens=4 delims=/:?" %%b in ("%DB_URL%") do set "DB_PORT=%%b"
    for /f "tokens=5 delims=/:?" %%c in ("%DB_URL%") do set "DB_NAME=%%c"
)

REM 4. Crear carpeta backups si no existe
if not exist "%~dp0backups" mkdir "%~dp0backups"

REM 5. Generar timestamp y nombre de archivo
for /f "tokens=2 delims==" %%I in ('wmic os get localdatetime /value 2^>nul') do set "LDT=%%I"
if "%LDT%"=="" (
    set "TIMESTAMP=%date:~6,4%%date:~3,2%%date:~0,2%_%time:~0,2%%time:~3,2%%time:~6,2%"
    set "TIMESTAMP=!TIMESTAMP: =0!"
) else (
    set "TIMESTAMP=%LDT:~0,8%_%LDT:~8,6%"
)

set "BACKUP_FILE=%~dp0backups\travelink_backup_%TIMESTAMP%.sql"

echo [INFO] Host:     %DB_HOST%:%DB_PORT%
echo [INFO] Base:     %DB_NAME%
echo [INFO] Usuario:  %DB_USERNAME%
echo [INFO] Destino:  %BACKUP_FILE%
echo [INFO] Ejecutando mysqldump...

set "MYSQL_PWD=%DB_PASSWORD%"
mysqldump --host=%DB_HOST% --port=%DB_PORT% --user=%DB_USERNAME% --single-transaction --routines --triggers --default-character-set=utf8mb4 %DB_NAME% > "%BACKUP_FILE%"
set "DUMP_STATUS=%ERRORLEVEL%"
set "MYSQL_PWD="

if %DUMP_STATUS% equ 0 (
    echo [EXITO] Respaldo generado correctamente en:
    echo        %BACKUP_FILE%
) else (
    echo [ERROR] Fallo al ejecutar mysqldump (Codigo de salida: %DUMP_STATUS%).
    if exist "%BACKUP_FILE%" del "%BACKUP_FILE%"
    exit /b %DUMP_STATUS%
)

endlocal
