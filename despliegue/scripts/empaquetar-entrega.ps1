<#
.SYNOPSIS
    Script de empaquetado limpio de entrega final para TRAVELINK.
.DESCRIPTION
    Genera un archivo ZIP consolidado con todo el código fuente, configuración,
    documentación y scripts de despliegue, excluyendo estrictamente:
    - .git/ (historial local de Git)
    - .env (variables de entorno con credenciales secretas)
    - target/, bin/, out/ (binarios compilados)
    - logs/ (archivos de log en ejecución)
    - .idea/, .vscode/, .settings/ (configuraciones de IDEs)
#>

[CmdletBinding()]
param(
    [string]$OutputFile = "dist\travelink-entrega-final.zip"
)

$ErrorActionPreference = "Stop"

$ProjectRoot = Resolve-Path (Join-Path $PSScriptRoot "..\..")
Set-Location $ProjectRoot

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host " TRAVELINK - Generación de Paquete Limpio de Entrega" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "Directorio raíz: $ProjectRoot"

# Asegurar directorio de salida
$OutputFileInfo = New-Item -ItemType File -Path $OutputFile -Force
$OutputDir = Split-Path -Parent $OutputFileInfo.FullName
if (-not (Test-Path $OutputDir)) {
    New-Item -ItemType Directory -Path $OutputDir -Force | Out-Null
}
Remove-Item -Path $OutputFileInfo.FullName -Force -ErrorAction SilentlyContinue

# Directorio temporal de staging
$TempStaging = Join-Path ([System.IO.Path]::GetTempPath()) ("travelink-staging-" + [System.Guid]::NewGuid().ToString())
New-Item -ItemType Directory -Path $TempStaging -Force | Out-Null

Write-Host "Copiando archivos filtrados a directorio de preparación..." -ForegroundColor Yellow

# Patrones excluidos
$ExcludedDirs = @('.git', 'target', 'bin', 'out', 'logs', '.idea', '.vscode', '.settings', '.maven', 'dist')

# Copiar recursivamente omitiendo carpetas y archivos restringidos
Get-ChildItem -Path $ProjectRoot -Recurse | ForEach-Object {
    $item = $_
    $relPath = $item.FullName.Substring($ProjectRoot.Path.Length + 1)
    
    # Comprobar si pertenece a alguna carpeta excluida
    $parts = $relPath.Split([System.IO.Path]::DirectorySeparatorChar)
    $skip = $false
    foreach ($p in $parts) {
        if ($ExcludedDirs -contains $p) {
            $skip = $true
            break
        }
    }

    # Exclusiones de archivos específicos
    if ($item.Name -eq ".env" -or ($item.Name -like ".env.*" -and $item.Name -ne ".env.example")) {
        $skip = $true
    }
    if ($item.Extension -in @('.class', '.tmp', '.log', '.bak')) {
        $skip = $true
    }
    if ($item.FullName -eq $OutputFileInfo.FullName) {
        $skip = $true
    }

    if (-not $skip) {
        $destPath = Join-Path $TempStaging $relPath
        if ($item.PSIsContainer) {
            if (-not (Test-Path $destPath)) {
                New-Item -ItemType Directory -Path $destPath -Force | Out-Null
            }
        } else {
            $parentDir = Split-Path -Parent $destPath
            if (-not (Test-Path $parentDir)) {
                New-Item -ItemType Directory -Path $parentDir -Force | Out-Null
            }
            Copy-Item -Path $item.FullName -Destination $destPath -Force
        }
    }
}

Write-Host "Comprimiendo archivo ZIP de entrega..." -ForegroundColor Yellow
Compress-Archive -Path "$TempStaging\*" -DestinationPath $OutputFileInfo.FullName -Force

# Limpieza de staging
Remove-Item -Path $TempStaging -Recurse -Force -ErrorAction SilentlyContinue

$ZipInfo = Get-Item $OutputFileInfo.FullName
$SizeMB = [math]::Round($ZipInfo.Length / 1MB, 2)

Write-Host "==========================================================" -ForegroundColor Green
Write-Host " PAQUETE GENERADO EXITOSAMENTE" -ForegroundColor Green
Write-Host " Archivo: $($ZipInfo.FullName)" -ForegroundColor White
Write-Host " Tamaño:  $SizeMB MB" -ForegroundColor White
Write-Host "==========================================================" -ForegroundColor Green
