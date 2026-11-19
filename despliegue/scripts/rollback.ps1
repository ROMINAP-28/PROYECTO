<#
.SYNOPSIS
    Script de rollback para el entorno de producción de TRAVELINK por Tag / Versión.
.DESCRIPTION
    Restaura una imagen etiquetada previa o tag semántico específico ante fallos críticos.
.PARAMETER TargetTag
    Tag semántico o nombre de imagen previa (por defecto: previous).
.EXAMPLE
    pwsh ./rollback.ps1 -TargetTag "v1.0.0"
#>
param (
    [string]$TargetTag = "previous"
)

$ErrorActionPreference = "Stop"
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$ProjectRoot = (Get-Item "$ScriptDir\..\..").FullName
$ComposeFile = "$ProjectRoot\despliegue\docker\docker-compose.prod.yml"
$EnvFile = "$ProjectRoot\.env"

Write-Host "=== [TRAVELINK] Iniciando Procedimiento de Rollback hacia tag: $TargetTag ===" -ForegroundColor Yellow

if (-not (Test-Path $ComposeFile)) {
    Write-Error "No se encuentra el archivo $ComposeFile"
}

Write-Host "--> 1. Deteniendo contenedor de aplicacion actual..." -ForegroundColor Cyan
docker compose --env-file "$EnvFile" -f "$ComposeFile" stop app

$hasTag = docker images -q "travelink-app:$TargetTag"
if ($hasTag) {
    Write-Host "--> 2. Restaurando travelink-app:$TargetTag como travelink-app:latest..." -ForegroundColor Green
    docker tag "travelink-app:$TargetTag" travelink-app:latest
} else {
    $hasPrev = docker images -q "travelink-app:previous"
    if ($hasPrev) {
        Write-Host "--> 2. Tag '$TargetTag' no encontrado; usando fallback travelink-app:previous..." -ForegroundColor Yellow
        docker tag travelink-app:previous travelink-app:latest
    } else {
        Write-Host "--> [AVISO] Reutilizando última imagen estable existente..." -ForegroundColor Gray
    }
}

Write-Host "--> 3. Reiniciando servicio de aplicacion con la imagen restaurada..." -ForegroundColor Cyan
docker compose --env-file "$EnvFile" -f "$ComposeFile" up -d app

Write-Host "--> 4. Verificando estado de salud del contenedor..." -ForegroundColor Cyan
for ($i = 1; $i -le 20; $i++) {
    $health = docker compose --env-file "$EnvFile" -f "$ComposeFile" ps app --format "{{.Health}}"
    if ($health -match "healthy") {
        Write-Host "[OK] Aplicación restaurada y en estado saludable." -ForegroundColor Green
        break
    }
    Write-Host "Esperando estado saludable ($i/20)..." -ForegroundColor Gray
    Start-Sleep -Seconds 3
}

docker compose --env-file "$EnvFile" -f "$ComposeFile" ps app
Write-Host "=== [OK] Procedimiento de Rollback finalizado exitosamente ===" -ForegroundColor Green
