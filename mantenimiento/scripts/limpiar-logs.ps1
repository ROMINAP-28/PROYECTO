<#
.SYNOPSIS
    Limpieza y Rotación de Logs - TRAVELINK
.DESCRIPTION
    Elimina archivos de logs comprimidos (*.log.gz) y archivos temporales
    antiguos en el directorio logs/ que superen los días de retención (por defecto 14 días).
    Nunca elimina los logs activos en uso (travelink.log, travelink-json.log).
#>

param(
    [string]$LogsDir = "$PSScriptRoot\..\..\logs",
    [int]$RetentionDays = 14
)

Write-Host "=====================================================" -ForegroundColor Cyan
Write-Host "   TRAVELINK - PURGA Y LIMPIEZA DE LOGS HISTÓRICOS   " -ForegroundColor Cyan
Write-Host "=====================================================" -ForegroundColor Cyan
Write-Host "Directorio: $LogsDir | Retención: $RetentionDays días" -ForegroundColor Gray

if (-not (Test-Path $LogsDir)) {
    Write-Host "[INFO] No se encontró directorio de logs en $LogsDir (nada que purgar)." -ForegroundColor Yellow
    exit 0
}

$LimitDate = (Get-Date).AddDays(-$RetentionDays)
$PurgedCount = 0
$BytesFreed = 0

# Buscar logs rotados y comprimidos antiguos
$OldLogs = Get-ChildItem -Path $LogsDir -Recurse -File | Where-Object {
    ($_.Name -like "*.log.gz" -or $_.Name -like "*.log.*" -or $_.Extension -eq ".tmp") -and
    $_.Name -ne "travelink.log" -and
    $_.Name -ne "travelink-json.log" -and
    $_.LastWriteTime -lt $LimitDate
}

foreach ($file in $OldLogs) {
    try {
        $size = $file.Length
        Remove-Item -Path $file.FullName -Force
        $PurgedCount++
        $BytesFreed += $size
        Write-Host "  -> Eliminado: $($file.Name) ($([math]::Round($size / 1KB, 2)) KB)" -ForegroundColor DarkGray
    } catch {
        Write-Host "  -> [ERROR] No se pudo eliminar $($file.Name): $_" -ForegroundColor Red
    }
}

$MbFreed = [math]::Round($BytesFreed / 1MB, 2)
Write-Host "[OK] Purga finalizada: $PurgedCount archivos eliminados ($MbFreed MB liberados)." -ForegroundColor Green
Write-Host "=====================================================" -ForegroundColor Cyan
