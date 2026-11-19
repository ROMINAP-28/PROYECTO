# Generar certificado autofirmado para pruebas locales TLS en Nginx (PowerShell)
$ErrorActionPreference = "Stop"
$dir = Split-Path -Parent $MyInvocation.MyCommand.Path
$sslDir = Join-Path $dir "ssl"

if (-not (Test-Path $sslDir)) {
    New-Item -ItemType Directory -Path $sslDir | Out-Null
}

$keyPath = Join-Path $sslDir "travelink.key"
$crtPath = Join-Path $sslDir "travelink.crt"

$opensslCmd = "openssl"
if (-not (Get-Command "openssl" -ErrorAction SilentlyContinue)) {
    if (Test-Path "C:\Program Files\Git\usr\bin\openssl.exe") {
        $opensslCmd = "C:\Program Files\Git\usr\bin\openssl.exe"
    }
}

& $opensslCmd req -x509 -nodes -days 365 -newkey rsa:2048 `
  -keyout "$keyPath" `
  -out "$crtPath" `
  -subj "/C=PE/ST=Lima/L=Lima/O=TRAVELINK/OU=DevOps/CN=localhost" `
  -addext "subjectAltName=DNS:localhost,IP:127.0.0.1"

Write-Host "Certificados TLS generados exitosamente en $sslDir"
