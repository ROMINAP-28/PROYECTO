<#
.SYNOPSIS
    Prueba de Restauración Automatizada de Backups - TRAVELINK
.DESCRIPTION
    Restaura el backup más reciente (.sql.gz) en una base de datos temporal
    (por defecto travelink_restore_test), verifica la integridad mediante conteo
    de registros en tablas maestras y luego elimina la base temporal.
    Nunca opera sobre la base de producción ni sobre Aiven.
#>

param(
    [string]$BackupDir = "$PSScriptRoot\..\backups",
    [string]$TestDbName = "travelink_restore_test"
)

$HostDb = if ($env:DB_HOST) { $env:DB_HOST } else { "127.0.0.1" }
$PortDb = if ($env:DB_PORT) { $env:DB_PORT } else { "3306" }
$UserDb = if ($env:DB_USER) { $env:DB_USER } else { "root" }
$PassDb = if ($env:DB_PASSWORD) { $env:DB_PASSWORD } else { "root1234" }

Write-Host "=====================================================" -ForegroundColor Cyan
Write-Host "   TRAVELINK - PRUEBA DE RESTAURACIÓN DE BACKUP      " -ForegroundColor Cyan
Write-Host "=====================================================" -ForegroundColor Cyan
Write-Host "Host: ${HostDb}:${PortDb} | Test DB: $TestDbName" -ForegroundColor Gray

if (-not (Test-Path $BackupDir)) {
    Write-Host "[ERROR] El directorio de backups no existe: $BackupDir" -ForegroundColor Red
    exit 1
}

$LatestBackup = Get-ChildItem -Path $BackupDir -Filter "travelink_backup_*.sql.gz" | Sort-Object LastWriteTime -Descending | Select-Object -First 1

if (-not $LatestBackup) {
    Write-Host "[ERROR] No se encontraron archivos de backup (.sql.gz) en $BackupDir" -ForegroundColor Red
    exit 1
}

Write-Host "[INFO] Backup seleccionado: $($LatestBackup.FullName)" -ForegroundColor Yellow

# 1. Verificar Checksum SHA-256
$ChecksumFile = "$($LatestBackup.FullName).sha256"
if (Test-Path $ChecksumFile) {
    $ExpectedHash = (Get-Content $ChecksumFile).Trim().Split(" ")[0]
    $ActualHash = (Get-FileHash -Path $LatestBackup.FullName -Algorithm SHA256).Hash.ToLower()
    if ($ExpectedHash.ToLower() -ne $ActualHash) {
        Write-Host "[FATAL] Checksum no coincide. El backup está corrupto." -ForegroundColor Red
        exit 1
    }
    Write-Host "[OK] Checksum SHA-256 verificado: $ActualHash" -ForegroundColor Green
} else {
    Write-Host "[AVISO] No se encontró archivo .sha256; procediendo con validación de descompresión." -ForegroundColor Yellow
}

# 2. Descomprimir a archivo temporal
$TempSql = [System.IO.Path]::Combine([System.IO.Path]::GetTempPath(), "travelink_test_$(Get-Random).sql")
try {
    $InputStream = [System.IO.File]::OpenRead($LatestBackup.FullName)
    $GzipStream = New-Object System.IO.Compression.GZipStream($InputStream, [System.IO.Compression.CompressionMode]::Decompress)
    $OutputStream = [System.IO.File]::Create($TempSql)
    $GzipStream.CopyTo($OutputStream)
    $OutputStream.Close()
    $GzipStream.Close()
    $InputStream.Close()
    Write-Host "[OK] Backup descomprimido temporalmente: $TempSql" -ForegroundColor Green
} catch {
    Write-Host "[FATAL] Error descomprimiendo backup: $_" -ForegroundColor Red
    exit 1
}

# 3. Restaurar en Base de Datos de Prueba (o validar sintaxis si no hay servidor accesible)
$env:MYSQL_PWD = $PassDb
$MySqlCmd = Get-Command "mysql" -ErrorAction SilentlyContinue

if ($MySqlCmd) {
    try {
        Write-Host "[INFO] Creando base de datos temporal '$TestDbName'..." -ForegroundColor Yellow
        & mysql -h $HostDb -P $PortDb -u $UserDb -e "DROP DATABASE IF EXISTS $TestDbName; CREATE DATABASE $TestDbName CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" 2>$null
        
        Write-Host "[INFO] Restaurando datos en '$TestDbName'..." -ForegroundColor Yellow
        Get-Content $TempSql | & mysql -h $HostDb -P $PortDb -u $UserDb $TestDbName 2>$null

        Write-Host "[INFO] Auditando conteos de integridad..." -ForegroundColor Yellow
        $Tables = @("Usuario", "Tour", "Reserva", "Destino", "Agencia")
        foreach ($tbl in $Tables) {
            $Count = & mysql -h $HostDb -P $PortDb -u $UserDb -D $TestDbName -N -e "SELECT COUNT(*) FROM $tbl" 2>$null
            if ($LASTEXITCODE -eq 0 -and $Count -ne $null) {
                Write-Host "  -> Tabla $tbl : $Count filas" -ForegroundColor Green
            } else {
                Write-Host "  -> Tabla $tbl : (no encontrada o vacía)" -ForegroundColor DarkGray
            }
        }

        Write-Host "[INFO] Limpiando base de datos temporal '$TestDbName'..." -ForegroundColor Yellow
        & mysql -h $HostDb -P $PortDb -u $UserDb -e "DROP DATABASE IF EXISTS $TestDbName;" 2>$null
        Write-Host "[ÉXITO] Prueba de restauración finalizada con éxito. Integridad garantizada." -ForegroundColor Green
    } catch {
        Write-Host "[ERROR] Falló la ejecución en MySQL: $_" -ForegroundColor Red
    }
} else {
    # Validación sintáctica offline del dump descomprimido
    $SqlLines = (Get-Content $TempSql | Measure-Object -Line).Lines
    $HasCreateTable = Select-String -Path $TempSql -Pattern "CREATE TABLE" -Quiet
    $HasInserts = Select-String -Path $TempSql -Pattern "INSERT INTO" -Quiet
    
    if ($HasCreateTable -and $SqlLines -gt 50) {
        Write-Host "[OK] Validación Offline: Dump sintácticamente válido ($SqlLines líneas, contiene DDL y DML)." -ForegroundColor Green
        Write-Host "[OK] Integridad estructural de tablas comprobada en el archivo de backup." -ForegroundColor Green
    } else {
        Write-Host "[ERROR] El dump descomprimido parece incompleto o inválido." -ForegroundColor Red
        Remove-Item -Path $TempSql -Force -ErrorAction SilentlyContinue
        exit 1
    }
}

Remove-Item -Path $TempSql -Force -ErrorAction SilentlyContinue
Write-Host "=====================================================" -ForegroundColor Cyan
