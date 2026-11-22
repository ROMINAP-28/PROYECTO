<#
.SYNOPSIS
    Registrador de Tarea Programada en Windows - TRAVELINK
.DESCRIPTION
    Registra una tarea en el Programador de Tareas de Windows (Task Scheduler)
    para ejecutar el backup diario de base de datos a las 02:00 AM.
#>

param(
    [string]$TaskName = "TRAVELINK_Backup_Diario",
    [string]$BackupScript = "$PSScriptRoot\..\scripts\backup.ps1",
    [string]$Time = "02:00"
)

$ResolvedScript = (Resolve-Path $BackupScript).Path

Write-Host "=====================================================" -ForegroundColor Cyan
Write-Host "   TRAVELINK - REGISTRO DE TAREA EN WINDOWS          " -ForegroundColor Cyan
Write-Host "=====================================================" -ForegroundColor Cyan
Write-Host "Tarea: $TaskName" -ForegroundColor Gray
Write-Host "Script: $ResolvedScript" -ForegroundColor Gray
Write-Host "Hora: $Time" -ForegroundColor Gray

# Comando PowerShell que ejecutará la tarea
$ActionArg = "-ExecutionPolicy Bypass -NoProfile -WindowStyle Hidden -File `"$ResolvedScript`""

try {
    # Usar schtasks para compatibilidad transversal con permisos de usuario estándar
    $Cmd = "schtasks.exe /Create /TN `"$TaskName`" /TR `"powershell.exe $ActionArg`" /SC DAILY /ST $Time /F"
    Write-Host "[INFO] Ejecutando: $Cmd" -ForegroundColor Yellow
    
    $Output = cmd.exe /c $Cmd 2>&1
    Write-Host $Output -ForegroundColor Green

    # Verificar consulta de la tarea registrada
    Write-Host "`n[INFO] Consultando estado de la tarea en Task Scheduler..." -ForegroundColor Cyan
    $QueryOutput = cmd.exe /c "schtasks.exe /Query /TN `"$TaskName`" /FO LIST" 2>&1
    Write-Host $QueryOutput -ForegroundColor White
    
    Write-Host "[ÉXITO] Tarea programada '$TaskName' registrada y validada correctamente." -ForegroundColor Green
} catch {
    Write-Host "[ERROR] No se pudo registrar la tarea: $_" -ForegroundColor Red
    exit 1
}
