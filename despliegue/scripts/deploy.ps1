<#
.SYNOPSIS
    Script de despliegue automatizado para el entorno de producción de TRAVELINK.
.DESCRIPTION
    Reconstruye la imagen, levanta los contenedores en segundo plano, verifica el healthcheck y realiza smoke test HTTP.
#>
param (
    [string]$ComposeFile = "$PSScriptRoot\..\docker\docker-compose.prod.yml"
)

$ErrorActionPreference = "Continue"
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$ProjectRoot = (Get-Item "$ScriptDir\..\..").FullName
$EnvFile = "$ProjectRoot\.env"

Write-Host "=== [TRAVELINK] Iniciando Despliegue de Produccion ===" -ForegroundColor Cyan

if (-not (Test-Path $ComposeFile)) {
    Write-Error "[ERROR] No se encuentra el archivo $ComposeFile"
}

$envArgs = @()
if (Test-Path $EnvFile) {
    $envArgs = @("--env-file", "$EnvFile")
}

Write-Host "--> Verificando puertos disponibles..." -ForegroundColor Yellow
if (docker ps -q -f name=travelink-db) {
    Write-Host "--> [INFO] Deteniendo entorno de desarrollo local para evitar conflicto de puerto 3307..." -ForegroundColor Gray
    docker compose down 2>$null
}

Write-Host "--> Reconstruyendo contenedores de produccion..." -ForegroundColor Yellow
try {
    docker compose @envArgs -f $ComposeFile build app
} catch {
    Write-Host "--> [INFO] Reutilizando imagen local construida previamente..." -ForegroundColor Gray
}

Write-Host "--> Iniciando servicios (DB, App, Nginx)..." -ForegroundColor Yellow
docker compose @envArgs -f $ComposeFile up -d

Write-Host "--> Esperando que travelink-app-prod alcance estado saludable..." -ForegroundColor Yellow
$healthy = $false
for ($i = 1; $i -le 24; $i++) {
    $status = docker inspect --format='{{if .State.Health}}{{.State.Health.Status}}{{else}}starting{{end}}' travelink-app-prod 2>$null
    if ($status -eq 'healthy') {
        $healthy = $true
        break
    }
    Write-Host "    Estado actual: $status... esperando 5s ($i/24)"
    Start-Sleep -Seconds 5
}

if (-not $healthy) {
    Write-Error "[ERROR] Timeout esperando que travelink-app-prod este saludable."
}

Write-Host "[OK] travelink-app-prod esta saludable." -ForegroundColor Green

Write-Host "--> Ejecutando Smoke Test sobre Nginx..." -ForegroundColor Yellow
try {
    [System.Net.ServicePointManager]::ServerCertificateValidationCallback = {$true}
    $response = Invoke-WebRequest -Uri "https://localhost/api/destinos" -UseBasicParsing -TimeoutSec 10
    if ($response.StatusCode -eq 200) {
        Write-Host "=== [OK] Despliegue de Produccion Completado Exitosamente (HTTP 200) ===" -ForegroundColor Green
        exit 0
    }
}
catch {
    $code = & curl.exe -k -s -o NUL -w "%{http_code}" https://localhost/api/destinos
    if ($code -eq "200") {
        Write-Host "=== [OK] Despliegue de Produccion Completado Exitosamente (HTTP 200) ===" -ForegroundColor Green
        exit 0
    }
    Write-Error "[ERROR] Fallo en Smoke Test: $_"
}
