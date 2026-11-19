<#
.SYNOPSIS
    Ejecuta pruebas de inyeccion SQL controladas y no destructivas con sqlmap.
.DESCRIPTION
    Ejecuta sqlmap con --level=1 --risk=1 --batch unicamente sobre la app local.
    Evalua endpoints con parametros sobre recursos reales del seed (tours 100 y 101, destinos 8 y 15, reservas 500 y 501).
    Guarda los reportes en pruebas/evidencias/sqlmap/.

    NOTA DE RED Y HOSTS (app vs localhost):
    - http://app:8080 es el nombre de servicio dentro de la red Docker interna travelink-net;
      solo lo resuelven los contenedores (ZAP, SQLMap, JMeter, CI). En el navegador anfitrión
      genera DNS_PROBE_FINISHED_NXDOMAIN.
    - Desde el navegador web o PowerShell del equipo anfitrión se usa http://localhost:8080 (o 127.0.0.1:8080).
    - La aplicación sirve HTTP plano en local (https:// no funciona hasta configurar proxy inverso Nginx con TLS).
#>

[CmdletBinding()]
param (
    [string]$TargetHost = "127.0.0.1",
    [int]$Port = 8080,
    [string]$DockerNetwork = "travelink-net"
)

# ==========================================
# GUARD DE SEGURIDAD ESTRICTA (SOLO LOCAL)
# ==========================================
$ALLOWED_HOSTS = @("localhost", "127.0.0.1", "host.docker.internal", "app")
if ($TargetHost -match "aivencloud" -or ($ALLOWED_HOSTS -notcontains $TargetHost)) {
    Write-Error "[ALERTA DE SEGURIDAD] Operacion abortada: el objetivo '$TargetHost' no esta autorizado o apunta a un servicio remoto."
    exit 1
}

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$ProjectRoot = Resolve-Path "$ScriptDir\..\..\.."
Set-Location $ProjectRoot

$OutDir = "$ProjectRoot\pruebas\evidencias\sqlmap"
if (-not (Test-Path $OutDir)) {
    New-Item -ItemType Directory -Path $OutDir -Force | Out-Null
}

$LogFile = "$OutDir\sqlmap-results.txt"
"==========================================================" | Out-File -FilePath $LogFile -Encoding utf8
" PRUEBAS CONTROLADAS DE INYECCION SQL (SQLMAP) - TRAVELINK" | Out-File -FilePath $LogFile -Append -Encoding utf8
" Fecha: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" | Out-File -FilePath $LogFile -Append -Encoding utf8
" Parametros de Seguridad: --level=1 --risk=1 --batch (No destructivo)" | Out-File -FilePath $LogFile -Append -Encoding utf8
" Recursos reales del seed evaluados: tours 100/101, destinos 8/15, reservas 500/501 (usuarios 100/101)" | Out-File -FilePath $LogFile -Append -Encoding utf8
"==========================================================" | Out-File -FilePath $LogFile -Append -Encoding utf8

$SqlmapCmd = Get-Command sqlmap -ErrorAction SilentlyContinue
$DockerCmd = Get-Command docker -ErrorAction SilentlyContinue

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host " [SQLi] Pruebas Controladas con sqlmap - TRAVELINK" -ForegroundColor Cyan
Write-Host " Restriccion de seguridad: --level=1 --risk=1 --batch (Local estricto)" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

$Endpoints = @(
    "/api/tours?idTour=100",
    "/api/tours?idTour=101",
    "/api/destinos?idDestino=8",
    "/api/destinos?idDestino=15",
    "/api/obtener_reservas?idUsuario=100",
    "/api/obtener_reservas?idUsuario=101"
)

if ($SqlmapCmd) {
    foreach ($Ep in $Endpoints) {
        $FullUrl = "http://${TargetHost}:${Port}${Ep}"
        Write-Host ""
        Write-Host "Analizando endpoint: $FullUrl ..." -ForegroundColor Yellow
        "----------------------------------------------------------" | Out-File -FilePath $LogFile -Append -Encoding utf8
        "ENDPOINT: $FullUrl" | Out-File -FilePath $LogFile -Append -Encoding utf8
        "----------------------------------------------------------" | Out-File -FilePath $LogFile -Append -Encoding utf8
        $res = & sqlmap -u $FullUrl --level=1 --risk=1 --batch --banner 2>&1
        $res | ForEach-Object { Write-Host $_ }
        $res | Out-File -FilePath $LogFile -Append -Encoding utf8
    }
} elseif ($DockerCmd) {
    Write-Host "[INFO] Ejecutando sqlmap via contenedor Docker travelink-sqlmap en red $DockerNetwork..." -ForegroundColor DarkYellow
    foreach ($Ep in $Endpoints) {
        $DockerUrl = "http://app:8080${Ep}"
        Write-Host ""
        Write-Host "Analizando endpoint via Docker: $DockerUrl ..." -ForegroundColor Yellow
        "----------------------------------------------------------" | Out-File -FilePath $LogFile -Append -Encoding utf8
        "ENDPOINT: $DockerUrl" | Out-File -FilePath $LogFile -Append -Encoding utf8
        "----------------------------------------------------------" | Out-File -FilePath $LogFile -Append -Encoding utf8
        $res = docker run --rm --network $DockerNetwork travelink-sqlmap -u $DockerUrl --level=1 --risk=1 --batch --banner 2>&1
        $res | ForEach-Object { Write-Host $_ }
        $res | Out-File -FilePath $LogFile -Append -Encoding utf8
    }
} else {
    Write-Host "[AVISO] Ni sqlmap ni Docker estan disponibles en PATH." -ForegroundColor Yellow
    "[PENDIENTE] sqlmap no ejecutado." | Out-File -FilePath $LogFile -Append -Encoding utf8
}

Write-Host ""
Write-Host "[COMPLETADO] Registro guardado en $LogFile" -ForegroundColor Green
