<#
.SYNOPSIS
    Ejecuta el análisis de composición de software (SCA) sobre dependencias del proyecto.
.DESCRIPTION
    Genera el árbol de dependencias con Maven y ejecuta OWASP Dependency-Check.
    Almacena los reportes en docs/seguridad/evidencias/dependency-check/.
#>

[CmdletBinding()]
param (
    [string]$TargetHost = "127.0.0.1"
)

# ==========================================
# GUARD DE SEGURIDAD ESTRICTA (SOLO LOCAL)
# ==========================================
$ALLOWED_HOSTS = @("localhost", "127.0.0.1", "host.docker.internal", "app", "db")
if ($TargetHost -match "aivencloud" -or ($ALLOWED_HOSTS -notcontains $TargetHost)) {
    Write-Error "[ALERTA DE SEGURIDAD] Operación abortada: el host '$TargetHost' no está autorizado o apunta a un servicio remoto."
    exit 1
}

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$ProjectRoot = Resolve-Path "$ScriptDir\..\.."
Set-Location $ProjectRoot

$OutDir = "$ProjectRoot\docs\seguridad\evidencias\dependency-check"
if (-not (Test-Path $OutDir)) {
    New-Item -ItemType Directory -Path $OutDir -Force | Out-Null
}

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host " [SCA] Análisis de Dependencias - TRAVELINK" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

# 1. Generar árbol de dependencias
Write-Host "[1/2] Generando árbol de dependencias (mvn dependency:tree)..." -ForegroundColor Yellow
$TreeOut = "$OutDir\dependency-tree.txt"
if (Test-Path ".\mvnw.cmd") {
    .\mvnw.cmd dependency:tree "-DoutputFile=$TreeOut"
} elseif (Get-Command mvn -ErrorAction SilentlyContinue) {
    mvn dependency:tree "-DoutputFile=$TreeOut"
} else {
    Write-Error "No se encontró Maven ni wrapper mvnw en el directorio."
    exit 1
}

# 2. Ejecutar OWASP Dependency-Check
Write-Host "[2/2] Ejecutando OWASP Dependency-Check..." -ForegroundColor Yellow

$NvdKeyArg = ""
if ($env:NVD_API_KEY) {
    Write-Host "[INFO] Usando clave NVD_API_KEY configurada en el entorno." -ForegroundColor Green
    $NvdKeyArg = "-DnvdApiKey=$env:NVD_API_KEY"
} else {
    Write-Host "[ADVERTENCIA] Variable NVD_API_KEY no definida. La descarga de la base NVD puede ser lenta o estar sujeta a rate-limit." -ForegroundColor DarkYellow
    Write-Host "[CONSEJO] Para acelerar la ejecución, exporta tu clave NVD: `$env:NVD_API_KEY='tu_clave'" -ForegroundColor Gray
}

$MvnCmd = if (Test-Path ".\mvnw.cmd") { ".\mvnw.cmd" } else { "mvn" }
$OwaspGoal = "org.owasp:dependency-check-maven:10.0.4:check"
$FormatArg = "-Dformat=ALL"
$OutArg = "-Dout=$OutDir"

$ArgsList = @($OwaspGoal, $FormatArg, $OutArg, "-DfailBuildOnCVSS=11", "-DskipTestCompile=true")
if ($NvdKeyArg) { $ArgsList += $NvdKeyArg }

Write-Host "Ejecutando: $MvnCmd $($ArgsList -join ' ')" -ForegroundColor Gray
& $MvnCmd @ArgsList

if (Test-Path "$OutDir\dependency-check-report.html") {
    Write-Host "[ÉXITO] Reportes generados en $OutDir" -ForegroundColor Green
    Write-Host " - HTML: $OutDir\dependency-check-report.html" -ForegroundColor Green
    Write-Host " - JSON: $OutDir\dependency-check-report.json" -ForegroundColor Green
} else {
    Write-Host "[AVISO] Finalizó el comando. Revisa el log para detalles de descarga de la BD de NVD." -ForegroundColor Yellow
}

