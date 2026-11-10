<#
.SYNOPSIS
    Ejecuta pruebas de inyección SQL controladas y no destructivas con sqlmap.
.DESCRIPTION
    Ejecuta sqlmap con --level=1 --risk=1 --batch únicamente sobre la app local en 127.0.0.1:8080.
    Evalúa endpoints con parámetros sobre recursos reales del seed (tours 100 y 101, destinos 8 y 15, reservas 500 y 501).
    Guarda los reportes en docs/seguridad/evidencias/sqlmap/.
#>

[CmdletBinding()]
param (
    [string]$TargetHost = "127.0.0.1",
    [int]$Port = 8080
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
Set-Location $ProjectRoot

$OutDir = "$ProjectRoot\docs\seguridad\evidencias\sqlmap"
if (-not (Test-Path $OutDir)) {
    New-Item -ItemType Directory -Path $OutDir -Force | Out-Null
}

$LogFile = "$OutDir\sqlmap-results.txt"
"==========================================================" | Out-File -FilePath $LogFile -Encoding utf8
" PRUEBAS CONTROLADAS DE INYECCIÓN SQL (SQLMAP) - TRAVELINK" | Out-File -FilePath $LogFile -Append -Encoding utf8
" Fecha: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" | Out-File -FilePath $LogFile -Append -Encoding utf8
" Parámetros de Seguridad: --level=1 --risk=1 --batch (No destructivo)" | Out-File -FilePath $LogFile -Append -Encoding utf8
" Recursos reales del seed evaluados: tours 100/101, destinos 8/15, reservas 500/501 (usuarios 100/101)" | Out-File -FilePath $LogFile -Append -Encoding utf8
"==========================================================" | Out-File -FilePath $LogFile -Append -Encoding utf8

$TestEndpoints = @(
    "http://${TargetHost}:${Port}/api/tours?idTour=100",
    "http://${TargetHost}:${Port}/api/tours?idTour=101",
    "http://${TargetHost}:${Port}/api/destinos?idDestino=8",
    "http://${TargetHost}:${Port}/api/destinos?idDestino=15",
    "http://${TargetHost}:${Port}/api/obtener_reservas?idUsuario=100",
    "http://${TargetHost}:${Port}/api/obtener_reservas?idUsuario=101"
)

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host " [SQLi] Pruebas Controladas con sqlmap - TRAVELINK" -ForegroundColor Cyan
Write-Host " Objetivo base: http://${TargetHost}:${Port}" -ForegroundColor Cyan
Write-Host " Restricción de seguridad: --level=1 --risk=1 --batch (Local estricto)" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

$SqlmapCmd = Get-Command sqlmap -ErrorAction SilentlyContinue

if (-not $SqlmapCmd) {
    $PythonCmd = Get-Command python -ErrorAction SilentlyContinue
    $WslCmd = Get-Command wsl -ErrorAction SilentlyContinue
    
    if (-not $PythonCmd -and -not $WslCmd) {
        Write-Host "[AVISO] sqlmap no se encuentra en el PATH de Windows." -ForegroundColor Yellow
        Write-Host "Instrucciones para ejecutar sqlmap:" -ForegroundColor Cyan
        Write-Host " - Con Python: pip install sqlmap" -ForegroundColor Gray
        Write-Host " - Con WSL:    sudo apt update && sudo apt install -y sqlmap" -ForegroundColor Gray
        "[PENDIENTE] sqlmap no disponible en PATH. Ejecutar: sqlmap -u 'http://127.0.0.1:8080/api/tours?idTour=100' --level=1 --risk=1 --batch" | Out-File -FilePath $LogFile -Append -Encoding utf8
        exit 0
    }
}

foreach ($Endpoint in $TestEndpoints) {
    Write-Host ""
    Write-Host "Analizando endpoint con recurso real: $Endpoint ..." -ForegroundColor Yellow
    "----------------------------------------------------------" | Out-File -FilePath $LogFile -Append -Encoding utf8
    "ENDPOINT: $Endpoint" | Out-File -FilePath $LogFile -Append -Encoding utf8
    "----------------------------------------------------------" | Out-File -FilePath $LogFile -Append -Encoding utf8

    if ($SqlmapCmd) {
        & sqlmap -u $Endpoint --level=1 --risk=1 --batch --banner --output-dir="$OutDir\output" 2>&1 | Tee-Object -FilePath $LogFile -Append
    } elseif ($WslCmd) {
        $WslSqlmap = wsl which sqlmap 2>$null
        if ($WslSqlmap) {
            Write-Host "[INFO] Ejecutando sqlmap a través de WSL..." -ForegroundColor Green
            wsl sqlmap -u $Endpoint --level=1 --risk=1 --batch --banner 2>&1 | Tee-Object -FilePath $LogFile -Append
        }
    }
}

Write-Host ""
Write-Host "[COMPLETADO] Registro guardado en $LogFile" -ForegroundColor Green
