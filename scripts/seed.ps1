# ==========================================================
# TRAVELINK - SCRIPT DE CARGA DE SEED SINTÉTICO LOCAL
# Entorno: Local exclusivamente (Docker / Windows PowerShell)
# ==========================================================

[CmdletBinding()]
param()

$ErrorActionPreference = "Stop"

# GUARD 1: Verificar raíz del proyecto
if (-not (Test-Path "pom.xml") -or -not (Test-Path "sql/seed_pruebas.sql")) {
    Write-Error "[BLOQUEO DE SEGURIDAD] Este script debe ejecutarse desde el directorio raíz del proyecto TRAVELINK."
    exit 1
}

# GUARD 2: Verificar que el contenedor de DB esté corriendo localmente
$running = docker ps --filter "name=travelink-db" --filter "status=running" -q
if (-not $running) {
    Write-Error "[ERROR] El contenedor 'travelink-db' no está en ejecución. Inicie con 'docker compose up -d db'."
    exit 1
}

Write-Host "Cargando datos sintéticos desde sql/seed_pruebas.sql..." -ForegroundColor Cyan
Get-Content -Path "sql/seed_pruebas.sql" -Raw | docker exec -i travelink-db sh -c 'mysql -uroot -p"$MYSQL_ROOT_PASSWORD" DBTravelink' 2>&1

Write-Host "Seed sintético cargado exitosamente en travelink-db." -ForegroundColor Green
