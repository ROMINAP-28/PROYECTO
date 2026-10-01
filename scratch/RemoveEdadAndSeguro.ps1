$file = "frontend/html/turista/form_reserva.html"
$content = Get-Content $file -Raw
$pattern = '(?s)<div class="input-field-block">\s*<label>Edad</label>.*?</div>\s*<div class="input-field-block full-col">\s*<label>Tipo de seguro</label>.*?</div>'
$newContent = $content -replace $pattern, ''
Set-Content $file $newContent -NoNewline
Write-Host "form_reserva.html updated!"

$schemaFile = "sql/travelink_schema.sql"
$sContent = Get-Content $schemaFile -Raw
$sContent = $sContent -replace '(?m)^\s*edad INT NOT NULL,\r?\n', ''
$sContent = $sContent -replace '(?m)^\s*tipoSeguro VARCHAR\(20\) NOT NULL DEFAULT ''Particular'',\r?\n', ''
Set-Content $schemaFile $sContent -NoNewline
Write-Host "travelink_schema.sql updated!"

$cleanupFile = "sql/cleanup_and_seeds.sql"
$cContent = Get-Content $cleanupFile -Raw
$cContent = $cContent -replace '(?m)^\s*edad INT NOT NULL,\r?\n', ''
$cContent = $cContent -replace '(?m)^\s*tipoSeguro VARCHAR\(20\) NOT NULL DEFAULT ''Particular'',\r?\n', ''
Set-Content $cleanupFile $cContent -NoNewline
Write-Host "cleanup_and_seeds.sql updated!"
