<#
.SYNOPSIS
    Script de construccion y empaquetado Maven de produccion para TRAVELINK.
.DESCRIPTION
    Ejecuta el empaquetado del archivo JAR con el wrapper de Maven y calcula su checksum SHA-256.
#>
$ErrorActionPreference = "Stop"
$ProjectRoot = Resolve-Path "$PSScriptRoot\..\.."

Write-Host "=== [TRAVELINK] Compilacion de Artefacto de Produccion (JAR) ===" -ForegroundColor Cyan
Set-Location $ProjectRoot

if ($env:JAVA_HOME -eq $null -or $env:JAVA_HOME -eq "") {
    $env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
}

cmd.exe /c "mvnw.cmd clean package -DskipTests -B"

$jar = Get-ChildItem -Path "$ProjectRoot\target" -Filter "*.jar" | Where-Object { $_.Name -notlike "*.original" } | Select-Object -First 1

if ($jar) {
    Write-Host "[OK] Artefacto generado exitosamente: $($jar.FullName)" -ForegroundColor Green
    $hash = Get-FileHash -Path $jar.FullName -Algorithm SHA256
    Write-Host "--> SHA-256: $($hash.Hash)" -ForegroundColor Yellow
}
else {
    Write-Error "[ERROR] No se pudo localizar el archivo JAR en target\"
}
