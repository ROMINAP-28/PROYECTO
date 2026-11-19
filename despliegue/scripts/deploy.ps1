<#
.SYNOPSIS
    Script de despliegue automatizado para el entorno de producción de TRAVELINK.
.DESCRIPTION
    Reconstruye la imagen, levanta los contenedores en segundo plano, verifica el healthcheck y realiza smoke test HTTP.
#>
param (
    [string]$ComposeFile = "$PSScriptRoot\..\docker\docker-compose.prod.yml"
)

$ErrorActionPreference = "Stop"
Write-Host "=== [TRAVELINK] Iniciando Despliegue de Produccion ===" -ForegroundColor Cyan

if (-not (Test-Path $ComposeFile)) {
    Write-Error "[ERROR] No se encuentra el archivo $ComposeFile"
}

Write-Host "--> Reconstruyendo contenedores de produccion..." -ForegroundColor Yellow
docker compose -f $ComposeFile build app

Write-Host "--> Iniciando servicios (DB, App, Nginx)..." -ForegroundColor Yellow
docker compose -f $ComposeFile up -d

Write-Host "--> Esperando que travelink-prod-app alcance estado saludable..." -ForegroundColor Yellow
$healthy = $false
for ($i = 1; $i -le 24; $i++) {
    $status = docker inspect --format='{{json .State.Health.Status}}' travelink-prod-app 2>$null
    if ($status -eq '"healthy"') {
        $healthy = $true
        break
    }
    Write-Host "    Estado actual: $status... esperando 5s ($i/24)"
    Start-Sleep -Seconds 5
}

if (-not $healthy) {
    Write-Error "[ERROR] Timeout esperando que travelink-prod-app este saludable."
}

Write-Host "[OK] travelink-prod-app esta saludable." -ForegroundColor Green

Write-Host "--> Ejecutando Smoke Test sobre Nginx (puerto 80)..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost/api/destinos" -UseBasicParsing -TimeoutSec 10
    if ($response.StatusCode -eq 200) {
        Write-Host "=== [OK] Despliegue de Produccion Completado Exitosamente (HTTP 200) ===" -ForegroundColor Green
        exit 0
    }
}
catch {
    Write-Error "[ERROR] Fallo en Smoke Test: $_"
}
