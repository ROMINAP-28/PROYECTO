<#
.SYNOPSIS
    Script de Restauración de Base de Datos para TRAVELINK (PowerShell).
.DESCRIPTION
    Verifica la integridad del checksum SHA-256 del archivo .sql.gz y restaura
    el contenido en la base de datos destino.
#>

[CmdletBinding()]
param(
    [Parameter(Mandatory=$false)]
    [string]$BackupFile = "",
    [string]$TargetDb = ""
)

$ErrorActionPreference = "Stop"

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$ProjectRoot = Resolve-Path (Join-Path $ScriptDir "..\..")

# 1. Cargar variables de entorno
$envPath = Join-Path $ProjectRoot ".env"
if (Test-Path $envPath) {
    Get-Content $envPath | Where-Object { $_ -match '^\s*([^#=]+)\s*=\s*(.*)$' } | ForEach-Object {
        $key = $matches[1].Trim()
        $val = $matches[2].Trim().Trim('"').Trim("'")
        if (-not [System.Environment]::GetEnvironmentVariable($key)) {
            [System.Environment]::SetEnvironmentVariable($key, $val, "Process")
        }
    }
}

$DbHost = if ($env:DB_HOST) { $env:DB_HOST } else { "127.0.0.1" }
$DbPort = if ($env:DB_PORT) { $env:DB_PORT } else { "3306" }
$DbName = if ($TargetDb) { $TargetDb } elseif ($env:DB_NAME) { $env:DB_NAME } elseif ($env:MYSQL_DATABASE) { $env:MYSQL_DATABASE } else { "DBTravelink" }
$DbUser = if ($env:DB_USER) { $env:DB_USER } elseif ($env:DB_USERNAME) { $env:DB_USERNAME } else { "root" }
$DbPass = if ($env:DB_PASSWORD) { $env:DB_PASSWORD } elseif ($env:MYSQL_ROOT_PASSWORD) { $env:MYSQL_ROOT_PASSWORD } else { "" }

if (-not $BackupFile) {
    # Buscar el respaldo más reciente
    $backupDir = Join-Path $ProjectRoot "mantenimiento\backups"
    $latest = Get-ChildItem -Path $backupDir -Filter "travelink_backup_*.sql.gz" | Sort-Object LastWriteTime -Descending | Select-Object -First 1
    if (-not $latest) {
        Write-Error "[ERROR] No se encontraron archivos de respaldo en $backupDir."
        exit 1
    }
    $BackupFile = $latest.FullName
}

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host " TRAVELINK - Restauración de Base de Datos" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "[INFO] Archivo de Respaldo: $BackupFile"
Write-Host "[INFO] Base Destino:        $DbName"
Write-Host "[INFO] Host:                $DbHost`:$DbPort"

# 2. Verificar Checksum SHA-256 si existe archivo .sha256
$shaFile = "$BackupFile.sha256"
if (Test-Path $shaFile) {
    Write-Host "[INFO] Verificando integridad SHA-256..." -ForegroundColor Yellow
    $expectedHash = (Get-Content $shaFile).Split(" ")[0].Trim().ToUpper()
    $actualHash = (Get-FileHash -Path $BackupFile -Algorithm SHA256).Hash.ToUpper()
    if ($expectedHash -ne $actualHash) {
        Write-Error "[FATAL] Checksum no coincide. El archivo de respaldo está corrupto o alterado."
        exit 1
    }
    Write-Host "[OK] Integridad verificada correctamente (SHA-256: $actualHash)" -ForegroundColor Green
}

# 3. Descomprimir temporalmente
$tempSql = [System.IO.Path]::GetTempFileName() + ".sql"
Write-Host "[INFO] Descomprimiendo archivo SQL temporal..." -ForegroundColor Yellow
$srcStream = [System.IO.File]::OpenRead($BackupFile)
$gzipStream = New-Object System.IO.Compression.GZipStream($srcStream, [System.IO.Compression.CompressionMode]::Decompress)
$dstStream = [System.IO.File]::Create($tempSql)
$gzipStream.CopyTo($dstStream)
$dstStream.Close()
$gzipStream.Close()
$srcStream.Close()

# 4. Restaurar en MySQL
Write-Host "[INFO] Importando volcado SQL en $DbName..." -ForegroundColor Yellow
$env:MYSQL_PWD = $DbPass

$mysqlCmd = Get-Command "mysql" -ErrorAction SilentlyContinue
if ($mysqlCmd) {
    Get-Content $tempSql | & mysql.exe --host=$DbHost --port=$DbPort --user=$DbUser --default-character-set=utf8mb4 $DbName
} else {
    $containerName = if ($env:CONTAINER_DB_NAME) { $env:CONTAINER_DB_NAME } else { "travelink-db-prod" }
    docker exec -i $containerName mysql -u$DbUser -p"$DbPass" --default-character-set=utf8mb4 $DbName < $tempSql
}
$env:MYSQL_PWD = $null

Remove-Item -Path $tempSql -Force -ErrorAction SilentlyContinue

Write-Host "==========================================================" -ForegroundColor Green
Write-Host " RESTAURACIÓN COMPLETADA EXITOSAMENTE EN $DbName" -ForegroundColor Green
Write-Host "==========================================================" -ForegroundColor Green
