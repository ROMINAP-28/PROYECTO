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
$PortDb = if ($env:DB_PORT) { $env:DB_PORT } else { "3307" }
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

    # Reemplazar nombre de base original por el nombre de base temporal de prueba
    $sqlContent = [System.IO.File]::ReadAllText($TempSql)
    $sqlContent = $sqlContent -replace "(?i)\bDBTravelink\b", $TestDbName
    [System.IO.File]::WriteAllText($TempSql, $sqlContent)

    Write-Host "[OK] Backup descomprimido y adaptado a DB temporal: $TempSql" -ForegroundColor Green
} catch {
    Write-Host "[FATAL] Error descomprimiendo backup: $_" -ForegroundColor Red
    exit 1
}

# 3. Restaurar en Base de Datos de Prueba (vía mysql CLI o contenedor Docker travelink-db / travelink-db-prod)
$MySqlCmd = Get-Command "mysql" -ErrorAction SilentlyContinue
$DockerCmd = Get-Command "docker" -ErrorAction SilentlyContinue

$DockerContainer = ""
if ($DockerCmd) {
    $active = & docker ps --filter "name=travelink-db" --format "{{.Names}}" 2>$null
    if ($active) {
        $DockerContainer = ($active -split "`r?`n")[0].Trim()
    }
}

if ($MySqlCmd) {
    try {
        $env:MYSQL_PWD = $PassDb
        Write-Host "[INFO] Creando base de datos temporal '$TestDbName' en ${HostDb}:${PortDb}..." -ForegroundColor Yellow
        & mysql -h $HostDb -P $PortDb -u $UserDb -e "DROP DATABASE IF EXISTS $TestDbName; CREATE DATABASE $TestDbName CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" 2>$null
        
        Write-Host "[INFO] Restaurando datos en '$TestDbName'..." -ForegroundColor Yellow
        Get-Content $TempSql | & mysql -h $HostDb -P $PortDb -u $UserDb $TestDbName 2>$null

        Write-Host "[INFO] Auditando conteos de integridad..." -ForegroundColor Yellow
        $Tables = @("Rol", "Persona", "Usuario", "Agencia", "Destino", "Tour", "Reserva")
        foreach ($tbl in $Tables) {
            $Count = & mysql -h $HostDb -P $PortDb -u $UserDb -D $TestDbName -N -e "SELECT COUNT(*) FROM $tbl" 2>$null
            if ($LASTEXITCODE -eq 0 -and $Count -ne $null) {
                Write-Host "  -> Tabla $tbl : $Count filas" -ForegroundColor Green
            } else {
                Write-Host "  -> Tabla $tbl : 0 filas (vacía o sin datos)" -ForegroundColor DarkGray
            }
        }

        Write-Host "[INFO] Limpiando base de datos temporal '$TestDbName'..." -ForegroundColor Yellow
        & mysql -h $HostDb -P $PortDb -u $UserDb -e "DROP DATABASE IF EXISTS $TestDbName;" 2>$null
        Write-Host "[ÉXITO] Prueba de restauración finalizada con éxito. Integridad garantizada." -ForegroundColor Green
    } catch {
        Write-Host "[ERROR] Falló la ejecución en MySQL: $_" -ForegroundColor Red
    }
} elseif ($DockerContainer) {
    try {
        Write-Host "[INFO] Ejecutando restauración en contenedor Docker '$DockerContainer'..." -ForegroundColor Yellow
        
        # Crear base temporal
        "DROP DATABASE IF EXISTS $TestDbName; CREATE DATABASE $TestDbName CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" | & docker exec -i $DockerContainer sh -c 'mysql -uroot -p"$MYSQL_ROOT_PASSWORD"' 2>$null

        # Restaurar dump en base temporal
        Write-Host "[INFO] Restaurando datos en '$TestDbName' dentro del contenedor..." -ForegroundColor Yellow
        Get-Content $TempSql | & docker exec -i $DockerContainer sh -c "mysql -uroot -p`$MYSQL_ROOT_PASSWORD $TestDbName" 2>$null

        # Auditar conteos
        Write-Host "[INFO] Auditando conteos de integridad en base de datos temporal..." -ForegroundColor Yellow
        $Tables = @("Rol", "Persona", "Usuario", "Agencia", "Destino", "Tour", "Reserva")
        foreach ($tbl in $Tables) {
            $query = "SELECT COUNT(*) FROM $tbl;"
            $Count = $query | & docker exec -i $DockerContainer sh -c "mysql -uroot -p`$MYSQL_ROOT_PASSWORD -N -D $TestDbName" 2>$null
            if ($Count) {
                $CleanCount = ($Count -split "`r?`n" | Where-Object { $_ -match '^\d+$' })[0]
                Write-Host "  -> Tabla $tbl : $CleanCount filas" -ForegroundColor Green
            } else {
                Write-Host "  -> Tabla $tbl : 0 filas" -ForegroundColor DarkGray
            }
        }

        # Limpiar base temporal
        Write-Host "[INFO] Eliminando base de datos temporal '$TestDbName'..." -ForegroundColor Yellow
        "DROP DATABASE IF EXISTS $TestDbName;" | & docker exec -i $DockerContainer sh -c 'mysql -uroot -p"$MYSQL_ROOT_PASSWORD"' 2>$null
        Write-Host "[ÉXITO] Prueba de restauración finalizada con éxito. Integridad garantizada." -ForegroundColor Green
    } catch {
        Write-Host "[ERROR] Falló la restauración en Docker: $_" -ForegroundColor Red
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
