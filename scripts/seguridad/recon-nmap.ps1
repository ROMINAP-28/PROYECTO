<#
.SYNOPSIS
    Ejecuta reconocimiento de puertos y servicios con nmap sobre la app local.
.DESCRIPTION
    Inspecciona 127.0.0.1 en puertos 8080 (app) y 3307 (MySQL Docker).
    Guarda la salida en docs/seguridad/evidencias/nmap/nmap-recon.txt.
#>

[CmdletBinding()]
param (
    [string]$TargetHost = "127.0.0.1",
    [string]$Ports = "8080,3307"
)

# ==========================================
# GUARD DE SEGURIDAD ESTRICTA (SOLO LOCAL)
# ==========================================
$ALLOWED_HOSTS = @("localhost", "127.0.0.1", "host.docker.internal", "app", "db")
if ($TargetHost -match "aivencloud" -or ($ALLOWED_HOSTS -notcontains $TargetHost)) {
    Write-Error "[ALERTA DE SEGURIDAD] Operación abortada: el objetivo '$TargetHost' no está autorizado o apunta a un servicio remoto."
    exit 1
}

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$ProjectRoot = Resolve-Path "$ScriptDir\..\.."
$OutDir = "$ProjectRoot\docs\seguridad\evidencias\nmap"
if (-not (Test-Path $OutDir)) {
    New-Item -ItemType Directory -Path $OutDir -Force | Out-Null
}
$OutputFile = "$OutDir\nmap-recon.txt"

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host " [RECON] Escaneo de Puertos Nmap - TRAVELINK ($TargetHost)" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

$NmapCmd = Get-Command nmap -ErrorAction SilentlyContinue

if ($NmapCmd) {
    Write-Host "[INFO] Ejecutando: nmap -sV -p $Ports $TargetHost -oN `"$OutputFile`"" -ForegroundColor Yellow
    & nmap -sV -p $Ports $TargetHost -oN $OutputFile
    Write-Host "[ÉXITO] Resultados guardados en: $OutputFile" -ForegroundColor Green
    Get-Content $OutputFile | Select-Object -First 25
} else {
    Write-Host "[AVISO] Nmap no se encuentra instalado en el PATH del sistema host." -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Instrucciones para instalarlo o ejecutarlo:" -ForegroundColor Cyan
    Write-Host "1. En Windows (con Chocolatey): choco install nmap" -ForegroundColor Gray
    Write-Host "2. En Windows (con Winget):     winget install Insecure.Nmap" -ForegroundColor Gray
    Write-Host "3. O desde WSL / Kali Linux:   wsl nmap -sV -p $Ports 127.0.0.1" -ForegroundColor Gray
    Write-Host ""
    
    # Intentar ejecutar mediante WSL si está presente
    $WslCmd = Get-Command wsl -ErrorAction SilentlyContinue
    if ($WslCmd) {
        Write-Host "[INTENTO] Verificando nmap dentro de WSL..." -ForegroundColor DarkYellow
        $WslNmap = wsl which nmap 2>$null
        if ($WslNmap) {
            Write-Host "[INFO] Ejecutando nmap a través de WSL..." -ForegroundColor Green
            wsl nmap -sV -p $Ports 127.0.0.1 | Tee-Object -FilePath $OutputFile
            Write-Host "[ÉXITO] Resultados guardados en: $OutputFile" -ForegroundColor Green
            exit 0
        }
    }
    
    # Generar marcador de estado pendiente
    "[PENDIENTE] Nmap no está instalado en el sistema. Ejecutar manualmente con: nmap -sV -p $Ports $TargetHost" | Out-File -FilePath $OutputFile -Encoding utf8
    Write-Host "Se creó el registro de estado pendiente en $OutputFile" -ForegroundColor Gray
}

