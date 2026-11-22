<#
.SYNOPSIS
    Reinicio Controlado de Datos de Prueba - TRAVELINK
.DESCRIPTION
    Carga el script sql/seed_pruebas.sql en la base de datos de pruebas o desarrollo local.
    INCLUYE BLOQUEO DE SEGURIDAD: Nunca ejecuta sobre Aiven ni sobre entornos de producción.
#>

param(
    [switch]$Force
)

$HostDb = if ($env:DB_HOST) { $env:DB_HOST } else { "127.0.0.1" }
$PortDb = if ($env:DB_PORT) { $env:DB_PORT } else { "3306" }
$NameDb = if ($env:DB_NAME) { $env:DB_NAME } else { "travelink_db" }
$UserDb = if ($env:DB_USER) { $env:DB_USER } else { "root" }
$PassDb = if ($env:DB_PASSWORD) { $env:DB_PASSWORD } else { "root1234" }

Write-Host "=====================================================" -ForegroundColor Yellow
Write-Host "   TRAVELINK - RESET CONTROLADO DE DATOS DE PRUEBA   " -ForegroundColor Yellow
Write-Host "=====================================================" -ForegroundColor Yellow

# Protección contra producción / Aiven
if ($HostDb -match "aivencloud\.com" -or $env:SPRING_PROFILES_ACTIVE -eq "prod") {
    Write-Host "[FATAL] ABORTADO: Está prohibido ejecutar reset de prueba sobre Aiven o producción." -ForegroundColor Red
    exit 1
}

$SeedFile = "$PSScriptRoot\..\..\sql\seed_pruebas.sql"
if (-not (Test-Path $SeedFile)) {
    Write-Host "[ERROR] No se encontró el archivo $SeedFile" -ForegroundColor Red
    exit 1
}

if (-not $Force) {
    Write-Host "[AVISO] Esta acción sobreescribirá datos en '$NameDb' ($HostDb)." -ForegroundColor Yellow
    Write-Host "Ejecute con -Force para confirmar." -ForegroundColor Gray
    exit 0
}

$env:MYSQL_PWD = $PassDb
$MySqlCmd = Get-Command "mysql" -ErrorAction SilentlyContinue

if ($MySqlCmd) {
    Write-Host "[INFO] Cargando $SeedFile en $NameDb..." -ForegroundColor Cyan
    Get-Content $SeedFile | & mysql -h $HostDb -P $PortDb -u $UserDb $NameDb 2>$null
    if ($LASTEXITCODE -eq 0) {
        Write-Host "[ÉXITO] Datos de prueba restaurados satisfactoriamente." -ForegroundColor Green
    } else {
        Write-Host "[ERROR] Falló la inserción del seed SQL." -ForegroundColor Red
        exit 1
    }
} else {
    Write-Host "[INFO] Modo simulación: Archivo seed verificado ($SeedFile)." -ForegroundColor Green
}
