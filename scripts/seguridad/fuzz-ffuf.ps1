<#
.SYNOPSIS
    Ejecuta fuzzing de endpoints con ffuf contra http://127.0.0.1:8080/api/FUZZ.
.DESCRIPTION
    Inspecciona rutas descubribles, códigos 200, 401, 403 y anomalías 500.
    Guarda los resultados en docs/seguridad/evidencias/ffuf/ffuf-result.json.
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
$OutDir = "$ProjectRoot\docs\seguridad\evidencias\ffuf"
if (-not (Test-Path $OutDir)) {
    New-Item -ItemType Directory -Path $OutDir -Force | Out-Null
}

$OutputFile = "$OutDir\ffuf-result.json"
$Wordlist = "$ScriptDir\wordlists\api-endpoints.txt"
$TargetUrl = "http://${TargetHost}:${Port}/api/FUZZ"

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host " [FUZZING] Escaneo con ffuf - TRAVELINK" -ForegroundColor Cyan
Write-Host " Objetivo: $TargetUrl" -ForegroundColor Cyan
Write-Host " Diccionario: $Wordlist" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

$FfufCmd = Get-Command ffuf -ErrorAction SilentlyContinue

if ($FfufCmd) {
    Write-Host "[INFO] Ejecutando ffuf..." -ForegroundColor Yellow
    & ffuf -u $TargetUrl -w $Wordlist -mc 200,401,403,500 -o $OutputFile -of json
    Write-Host "[ÉXITO] Resultados guardados en $OutputFile" -ForegroundColor Green
} else {
    Write-Host "[AVISO] ffuf no se encuentra instalado en el PATH del sistema." -ForegroundColor Yellow

    # Probar si está en WSL
    $WslCmd = Get-Command wsl -ErrorAction SilentlyContinue
    if ($WslCmd) {
        $WslFfuf = wsl which ffuf 2>$null
        if ($WslFfuf) {
            Write-Host "[INFO] Ejecutando ffuf a través de WSL..." -ForegroundColor Green
            $LinuxWordlist = wsl wslpath -a ($Wordlist -replace '\\', '/')
            $LinuxOut = wsl wslpath -a ($OutputFile -replace '\\', '/')
            wsl ffuf -u "http://127.0.0.1:$Port/api/FUZZ" -w $LinuxWordlist -mc 200,401,403,500 -o $LinuxOut -of json
            Write-Host "[ÉXITO] Resultados generados mediante WSL en $OutputFile" -ForegroundColor Green
            exit 0
        }
    }

    Write-Host ""
    Write-Host "Instrucciones para instalar ffuf:" -ForegroundColor Cyan
    Write-Host "1. Con Winget:     winget install ffuf" -ForegroundColor Gray
    Write-Host "2. Con Scoop:      scoop install ffuf" -ForegroundColor Gray
    Write-Host "3. Con WSL / Kali: sudo apt update && sudo apt install -y ffuf" -ForegroundColor Gray
    Write-Host ""
    "[PENDIENTE] ffuf no instalado en el host. Ejecutar: ffuf -u $TargetUrl -w $Wordlist -mc 200,401,403,500 -o $OutputFile -of json" | Out-File -FilePath $OutputFile -Encoding utf8
    Write-Host "Se creó el registro de estado pendiente en $OutputFile" -ForegroundColor Gray
}

