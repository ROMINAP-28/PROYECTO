<#
.SYNOPSIS
    Ejecuta pruebas de carga y concurrencia con Apache JMeter (100, 300 y 500 usuarios).
.DESCRIPTION
    Ejecuta el plan jmeter/travelink.jmx en modo non-GUI.
    Genera reportes HTML y métricas (p95, p99, error %, throughput) en docs/seguridad/evidencias/jmeter/.
#>

[CmdletBinding()]
param (
    [string]$TargetHost = "127.0.0.1",
    [int]$Port = 8080,
    [int[]]$UserTiers = @(100, 300, 500),
    [string]$DockerNetwork = "travelink-net"
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

$JmxFile = "$ProjectRoot\jmeter\travelink.jmx"
$BaseOutDir = "$ProjectRoot\docs\seguridad\evidencias\jmeter"
if (-not (Test-Path $BaseOutDir)) {
    New-Item -ItemType Directory -Path $BaseOutDir -Force | Out-Null
}

$SummaryFile = "$BaseOutDir\resumen-rendimiento.txt"
"==========================================================" | Out-File -FilePath $SummaryFile -Encoding utf8
" RESUMEN DE PRUEBAS DE CARGA JMETER - TRAVELINK" | Out-File -FilePath $SummaryFile -Append -Encoding utf8
" Fecha: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" | Out-File -FilePath $SummaryFile -Append -Encoding utf8
" Objetivo: http://${TargetHost}:${Port}" | Out-File -FilePath $SummaryFile -Append -Encoding utf8
"==========================================================" | Out-File -FilePath $SummaryFile -Append -Encoding utf8

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host " [CARGA] Pruebas de Carga JMeter - TRAVELINK" -ForegroundColor Cyan
Write-Host " Objetivo: http://${TargetHost}:${Port}" -ForegroundColor Cyan
Write-Host " Escenarios: $($UserTiers -join ', ') usuarios concurrentes" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

$JmeterCmd = Get-Command jmeter -ErrorAction SilentlyContinue

if (-not $JmeterCmd) {
    # Verificar si existe imagen docker para JMeter o en WSL
    $DockerCmd = Get-Command docker -ErrorAction SilentlyContinue
    $WslCmd = Get-Command wsl -ErrorAction SilentlyContinue
    
    if (-not $DockerCmd -and -not $WslCmd) {
        Write-Host "[AVISO] JMeter no está instalado en el PATH del sistema." -ForegroundColor Yellow
        Write-Host "Instalar JMeter con:" -ForegroundColor Cyan
        Write-Host " - Windows: choco install jmeter  o  scoop install jmeter" -ForegroundColor Gray
        Write-Host " - Linux/WSL: sudo apt update && sudo apt install -y jmeter" -ForegroundColor Gray
        "[PENDIENTE] JMeter no instalado en el host. Ejecutar: jmeter -n -t $JmxFile -Jhost=$TargetHost -Jport=$Port" | Out-File -FilePath $SummaryFile -Append -Encoding utf8
        exit 0
    }
}

foreach ($Users in $UserTiers) {
    Write-Host ""
    Write-Host "----------------------------------------------------------" -ForegroundColor Yellow
    Write-Host " Ejecutando escenario con $Users usuarios concurrentes..." -ForegroundColor Yellow
    Write-Host "----------------------------------------------------------" -ForegroundColor Yellow

    $TierOutDir = "$BaseOutDir\tier_${Users}_users"
    if (Test-Path $TierOutDir) {
        Remove-Item -Recurse -Force $TierOutDir | Out-Null
    }
    New-Item -ItemType Directory -Path $TierOutDir -Force | Out-Null
    $JtlFile = "$TierOutDir\results.jtl"
    $ReportHtmlDir = "$TierOutDir\html_report"

    $RampUp = [Math]::Max(5, [Math]::Round($Users / 20))

    if ($JmeterCmd) {
        & jmeter -n -t $JmxFile `
            "-Jhost=$TargetHost" `
            "-Jport=$Port" `
            "-Jusers=$Users" `
            "-Jrampup=$RampUp" `
            "-Jloops=2" `
            -l $JtlFile `
            -e -o $ReportHtmlDir
    } elseif ($DockerCmd) {
        Write-Host "[INFO] Ejecutando JMeter vía contenedor Docker justb4/jmeter en red $DockerNetwork..." -ForegroundColor DarkYellow
        $LinuxJmx = "/jmeter/travelink.jmx"
        $NormProjectRoot = $ProjectRoot.ToString().Replace('\', '/')
        $NormTierOutDir = $TierOutDir.ToString().Replace('\', '/')
        docker run --rm `
            --network $DockerNetwork `
            -v "${NormProjectRoot}/jmeter:/jmeter:ro" `
            -v "${NormTierOutDir}:/results:rw" `
            justb4/jmeter `
            -n -t $LinuxJmx `
            "-Jhost=app" `
            "-Jport=8080" `
            "-Jusers=$Users" `
            "-Jrampup=$RampUp" `
            "-Jloops=2" `
            -l /results/results.jtl `
            -e -o /results/html_report
    }

    # Procesar métricas si se generó el archivo .jtl
    if (Test-Path $JtlFile) {
        Write-Host "[ÉXITO] Escenario $Users usuarios finalizado. Reporte HTML en: $ReportHtmlDir\index.html" -ForegroundColor Green
        
        $lines = Get-Content $JtlFile | Select-Object -Skip 1
        $totalSamples = $lines.Count
        if ($totalSamples -gt 0) {
            $errors = 0
            $elapsedTimes = @()
            foreach ($line in $lines) {
                $cols = $line -split ','
                if ($cols.Length -ge 8) {
                    $elapsedTimes += [long]$cols[1]
                    $success = $cols[7]
                    if ($success -eq "false") { $errors++ }
                }
            }
            $elapsedSorted = $elapsedTimes | Sort-Object
            $p95Index = [Math]::Min($totalSamples - 1, [Math]::Floor($totalSamples * 0.95))
            $p99Index = [Math]::Min($totalSamples - 1, [Math]::Floor($totalSamples * 0.99))
            $p95 = $elapsedSorted[$p95Index]
            $p99 = $elapsedSorted[$p99Index]
            $avg = ($elapsedTimes | Measure-Object -Average).Average
            $errRate = [Math]::Round(($errors / $totalSamples) * 100, 2)

            $summaryTier = @"
--- Nivel: $Users Usuarios Concurrentes ---
Total de Peticiones: $totalSamples
Tasa de Error: $errRate% ($errors fallos)
Tiempo de Respuesta Promedio: $([Math]::Round($avg, 2)) ms
Percentil 95 (p95): $p95 ms
Percentil 99 (p99): $p99 ms
Reporte HTML: $ReportHtmlDir\index.html
"@
            Write-Host $summaryTier -ForegroundColor Cyan
            $summaryTier | Out-File -FilePath $SummaryFile -Append -Encoding utf8
        }
    } else {
        Write-Host "[AVISO] No se pudo generar results.jtl para el escenario de $Users usuarios." -ForegroundColor Yellow
        "--- Nivel: $Users Usuarios Concurrentes --- [PENDIENTE / NO EJECUTADO]" | Out-File -FilePath $SummaryFile -Append -Encoding utf8
    }
}

Write-Host ""
Write-Host "[COMPLETADO] Resumen consolidado disponible en: $SummaryFile" -ForegroundColor Green
