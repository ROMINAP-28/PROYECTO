<#
.SYNOPSIS
    Ejecuta escaneo web con Nikto contra la app local en 127.0.0.1:8080.
.DESCRIPTION
    Inspecciona vulnerabilidades web conocidas y cabeceras inseguras.
    Almacena el resultado en docs/seguridad/evidencias/nikto/nikto-output.txt.
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
$OutDir = "$ProjectRoot\docs\seguridad\evidencias\nikto"
if (-not (Test-Path $OutDir)) {
    New-Item -ItemType Directory -Path $OutDir -Force | Out-Null
}
$OutputFile = "$OutDir\nikto-output.txt"
$TargetUrl = "http://${TargetHost}:${Port}"

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host " [RECON] Escaneo Web Nikto - TRAVELINK ($TargetUrl)" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

$NiktoCmd = Get-Command nikto -ErrorAction SilentlyContinue

if ($NiktoCmd) {
    Write-Host "[INFO] Ejecutando: nikto -h $TargetUrl -o $OutputFile" -ForegroundColor Yellow
    & nikto -h $TargetUrl -o $OutputFile
    Write-Host "[ÉXITO] Resultados guardados en: $OutputFile" -ForegroundColor Green
} else {
    Write-Host "[AVISO] Nikto no se encuentra instalado en el PATH del host." -ForegroundColor Yellow
    
    # Comprobar si se puede ejecutar mediante contenedor Docker sullo/nikto
    $DockerCmd = Get-Command docker -ErrorAction SilentlyContinue
    if ($DockerCmd) {
        Write-Host "[INFO] Intentando ejecutar Nikto mediante imagen oficial Docker (sullo/nikto)..." -ForegroundColor Yellow
        $DockerTarget = "http://host.docker.internal:$Port"
        docker run --rm sullo/nikto -h $DockerTarget -o /tmp/nikto.txt 2>$null | Tee-Object -FilePath $OutputFile
        if (Test-Path $OutputFile) {
            Write-Host "[ÉXITO] Escaneo ejecutado con Docker sullo/nikto en $OutputFile" -ForegroundColor Green
            exit 0
        }
    }

    # Comprobar WSL
    $WslCmd = Get-Command wsl -ErrorAction SilentlyContinue
    if ($WslCmd) {
        $WslNikto = wsl which nikto 2>$null
        if ($WslNikto) {
            Write-Host "[INFO] Ejecutando Nikto a través de WSL..." -ForegroundColor Green
            wsl nikto -h $TargetUrl | Tee-Object -FilePath $OutputFile
            Write-Host "[ÉXITO] Resultados guardados en: $OutputFile" -ForegroundColor Green
            exit 0
        }
    }

    Write-Host ""
    Write-Host "Instrucciones para instalar Nikto:" -ForegroundColor Cyan
    Write-Host "1. Con Docker: docker run --rm sullo/nikto -h http://host.docker.internal:$Port" -ForegroundColor Gray
    Write-Host "2. Con WSL:    sudo apt update && sudo apt install -y nikto" -ForegroundColor Gray
    Write-Host "               wsl nikto -h http://127.0.0.1:$Port" -ForegroundColor Gray
    Write-Host ""
    "[PENDIENTE] Nikto no disponible localmente. Ejecutar con: docker run --rm sullo/nikto -h http://host.docker.internal:$Port" | Out-File -FilePath $OutputFile -Encoding utf8
    Write-Host "Se creó el registro de estado pendiente en $OutputFile" -ForegroundColor Gray
}
