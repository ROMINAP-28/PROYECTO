<#
.SYNOPSIS
    Ejecuta el escaneo DAST OWASP ZAP Baseline sobre el contenedor de la aplicación.
.DESCRIPTION
    Utiliza la imagen oficial actual zaproxy/zap-stable.
    Se conecta a la red travelink-net y ataca http://app:8080.
    Genera reportes HTML y JSON en docs/seguridad/evidencias/zap/.
#>

[CmdletBinding()]
param (
    [string]$TargetHost = "app",
    [int]$Port = 8080,
    [string]$Network = "travelink-net"
)

# ==========================================
# GUARD DE SEGURIDAD ESTRICTA (SOLO LOCAL)
# ==========================================
$ALLOWED_HOSTS = @("localhost", "127.0.0.1", "host.docker.internal", "app")
if ($TargetHost -match "aivencloud" -or ($ALLOWED_HOSTS -notcontains $TargetHost)) {
    Write-Error "[ALERTA DE SEGURIDAD] Operación abortada: el objetivo '$TargetHost' no está autorizado o apunta a un servicio remoto."
    exit 1
}

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$ProjectRoot = Resolve-Path "$ScriptDir\..\.."
$OutDir = "$ProjectRoot\docs\seguridad\evidencias\zap"
if (-not (Test-Path $OutDir)) {
    New-Item -ItemType Directory -Path $OutDir -Force | Out-Null
}

$TargetUrl = "http://${TargetHost}:${Port}"
$ZapImage = "zaproxy/zap-stable"

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host " [DAST] OWASP ZAP Baseline Scan - TRAVELINK ($TargetUrl)" -ForegroundColor Cyan
Write-Host " Imagen oficial: $ZapImage" -ForegroundColor Cyan
Write-Host " Red Docker: $Network" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

# 1. Comprobar que Docker está activo
if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    Write-Error "Docker no está disponible en la terminal."
    exit 1
}

# 2. Comprobar que el contenedor travelink-app está corriendo
$AppRunning = docker ps --filter "name=travelink-app" --filter "status=running" -q
if (-not $AppRunning) {
    Write-Error "El contenedor 'travelink-app' no está corriendo. Levántalo antes con: docker compose up -d"
    exit 1
}

Write-Host "[1/2] Iniciando contenedor ZAP en red $Network..." -ForegroundColor Yellow

# Montar volumen para exportar el reporte
# Nota: ZAP corre con UID 1000 dentro del contenedor
$DockerRunArgs = @(
    "run", "--rm",
    "--network", $Network,
    "-v", "${OutDir}:/zap/wrk:rw",
    "-t", $ZapImage,
    "zap-baseline.py",
    "-t", $TargetUrl,
    "-r", "zap-report.html",
    "-J", "zap-report.json",
    "-I"  # No falla el build por alertas informativas/bajas
)

Write-Host "docker $($DockerRunArgs -join ' ')" -ForegroundColor Gray
& docker @DockerRunArgs

Write-Host "[2/2] Verificando generación de reportes..." -ForegroundColor Yellow
if (Test-Path "$OutDir\zap-report.html") {
    Write-Host "[ÉXITO] Reporte HTML generado: $OutDir\zap-report.html" -ForegroundColor Green
    Write-Host "[ÉXITO] Reporte JSON generado: $OutDir\zap-report.json" -ForegroundColor Green
} else {
    Write-Host "[AVISO] Revisa los logs de ZAP para verificar la finalización del reporte." -ForegroundColor Yellow
}

