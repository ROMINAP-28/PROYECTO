<#
.SYNOPSIS
    Healthcheck Actuator - TRAVELINK
.DESCRIPTION
    Consulta el endpoint /actuator/health de la aplicación.
    Verifica que el estado sea {"status":"UP"} y código HTTP 200.
    Retorna Exit Code 0 si el servicio está saludable, o Exit Code 1 si está caído o degradado.
#>

param(
    [string]$TargetUrl = $(if ($env:APP_HEALTH_URL) { $env:APP_HEALTH_URL } else { "http://127.0.0.1:8080/actuator/health" }),
    [int]$TimeoutSec = 5
)

try {
    $Response = Invoke-WebRequest -Uri $TargetUrl -UseBasicParsing -TimeoutSec $TimeoutSec -ErrorAction Stop
    $StatusCode = $Response.StatusCode
    $Content = $Response.Content

    if ($StatusCode -eq 200 -and $Content -match '"status"\s*:\s*"UP"') {
        Write-Host "[OK] Healthcheck exitoso: $TargetUrl -> HTTP $StatusCode (UP)" -ForegroundColor Green
        exit 0
    } else {
        Write-Host "[ALERTA] Healthcheck degradado: $TargetUrl -> HTTP $StatusCode (Respuesta: $Content)" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "[CRÍTICO] Falló conexión al healthcheck: $TargetUrl -> $_" -ForegroundColor Red
    exit 1
}
