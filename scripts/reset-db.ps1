# ==========================================================
# TRAVELINK - SCRIPT DE REINICIO DE BASE DE DATOS LOCAL
# Entorno: Local exclusivamente (Docker / Windows PowerShell)
# ==========================================================

[CmdletBinding()]
param(
    [switch]$Force
)

$ErrorActionPreference = "Stop"

# GUARD 1: Verificar que se ejecute desde la raíz del proyecto TRAVELINK
if (-not (Test-Path "pom.xml") -or -not (Test-Path "sql/travelink_schema.sql") -or -not (Test-Path "sql/seed_pruebas.sql")) {
    Write-Error "[BLOQUEO DE SEGURIDAD] Este script debe ejecutarse desde el directorio raíz del proyecto TRAVELINK."
    exit 1
}

# GUARD 2: Verificar que el host de DB sea estrictamente local
$permitidos = @("localhost", "127.0.0.1", "host.docker.internal", "app", "db")
$dbUrl = $env:DB_URL

if ($dbUrl) {
    if ($dbUrl -match "aivencloud") {
        Write-Error "[BLOQUEO DE SEGURIDAD] Se detectó 'aivencloud' en DB_URL. Abortando operación destructiva."
        exit 1
    }
    $esSeguro = $false
    foreach ($target in $permitidos) {
        if ($dbUrl -match $target) { $esSeguro = $true; break }
    }
    if (-not $esSeguro) {
        Write-Error "[BLOQUEO DE SEGURIDAD] El host en DB_URL no coincide con objetivos locales permitidos ($($permitidos -join ', ')). Abortando."
        exit 1
    }
}

Write-Host "==========================================================" -ForegroundColor Yellow
Write-Host "  TRAVELINK - REINICIO DEL ENTORNO LOCAL DE BASE DE DATOS" -ForegroundColor Yellow
Write-Host "==========================================================" -ForegroundColor Yellow
Write-Host "ADVERTENCIA: Esta acción eliminará el contenedor y volumen local 'travelink_mysql_data'" -ForegroundColor Red
Write-Host "y reconstruirá la base de datos con los datos sintéticos de prueba." -ForegroundColor Red

if (-not $Force) {
    $confirmacion = Read-Host "¿Desea continuar con el reinicio local? (S/N)"
    if ($confirmacion -ne "S" -and $confirmacion -ne "s") {
        Write-Host "Operación cancelada por el usuario." -ForegroundColor Cyan
        exit 0
    }
}

Write-Host "[1/3] Deteniendo y eliminando contenedores y volumen local..." -ForegroundColor Cyan
docker compose down -v

Write-Host "[2/3] Levantando servicio db con inicialización automática..." -ForegroundColor Cyan
docker compose up -d db

Write-Host "[3/3] Esperando que la base de datos esté saludable..." -ForegroundColor Cyan
$saludable = $false
for ($i = 1; $i -le 30; $i++) {
    $status = docker inspect --format="{{.State.Health.Status}}" travelink-db 2>$null
    if ($status -eq "healthy") {
        $saludable = $true
        Write-Host "Base de datos local inicializada exitosamente y lista para pruebas." -ForegroundColor Green
        break
    }
    Start-Sleep -Seconds 2
    Write-Host "Esperando inicialización... ($i/30)"
}

if (-not $saludable) {
    Write-Error "[ERROR CRÍTICO] La base de datos no alcanzó el estado 'healthy' tras 30 intentos. ABORTANDO y NO se levantará la aplicación."
    exit 1
}

Write-Host "Reiniciando la aplicación web..." -ForegroundColor Cyan
docker compose up -d app
Write-Host "Entorno local restablecido en http://127.0.0.1:8080" -ForegroundColor Green
