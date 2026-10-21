@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo ===================================================
echo   TRAVELINK - Restauración de Base de Datos (MySQL)
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

REM 2. Validar variables mínimas
if "%DB_USERNAME%"=="" (
    echo [ERROR] Variable DB_USERNAME no definida.
    exit /b 1
)
if "%DB_PASSWORD%"=="" (
    echo [ERROR] Variable DB_PASSWORD no definida.
    exit /b 1
)

REM 3. Extraer host, puerto y base de datos de DB_URL si no están definidos
if "%DB_HOST%"=="" set "DB_HOST=localhost"
if "%DB_PORT%"=="" set "DB_PORT=3306"
if "%DB_NAME%"=="" set "DB_NAME=DBTravelink"

if not "%DB_URL%"=="" (
    for /f "tokens=3 delims=/:?" %%a in ("%DB_URL%") do set "DB_HOST=%%a"
    for /f "tokens=4 delims=/:?" %%b in ("%DB_URL%") do set "DB_PORT=%%b"
    for /f "tokens=5 delims=/:?" %%c in ("%DB_URL%") do set "DB_NAME=%%c"
)

REM 4. Determinar archivo de respaldo a restaurar
set "BACKUP_FILE=%~1"

if "%BACKUP_FILE%"=="" (
    echo [INFO] No se especificó archivo. Buscando el más reciente en sql\backups...
    for /f "delims=" %%F in ('dir /b /a-d /o-d "%~dp0backups\*.sql" 2^>nul') do (
        set "BACKUP_FILE=%~dp0backups\%%F"
        goto :file_found
    )
)

:file_found
if "%BACKUP_FILE%"=="" (
    echo [ERROR] No se encontraron archivos de respaldo en %~dp0backups.
    echo Uso: restore.bat [ruta_al_archivo_sql]
    exit /b 1
)

if not exist "%BACKUP_FILE%" (
    echo [ERROR] El archivo de respaldo no existe: %BACKUP_FILE%
    exit /b 1
)

echo [ADVERTENCIA] Se restaurará el archivo:
echo               %BACKUP_FILE%
echo               sobre la base de datos: %DB_NAME% en %DB_HOST%:%DB_PORT%
echo [INFO] Ejecutando restauración con mysql...

set "MYSQL_PWD=%DB_PASSWORD%"
mysql --host=%DB_HOST% --port=%DB_PORT% --user=%DB_USERNAME% --default-character-set=utf8mb4 %DB_NAME% < "%BACKUP_FILE%"
set "RESTORE_STATUS=%ERRORLEVEL%"
set "MYSQL_PWD="

if %RESTORE_STATUS% equ 0 (
    echo [ÉXITO] Base de datos restaurada correctamente desde:
    echo         %BACKUP_FILE%
) else (
    echo [ERROR] Falló la restauración (Código de salida: %RESTORE_STATUS%).
    exit /b %RESTORE_STATUS%
)

endlocal
