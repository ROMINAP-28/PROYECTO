$file = "frontend/html/turista/agencias.html"
$content = Get-Content $file -Raw

$pattern = '(?s)<div class="agencias-list" id="agencias-container">.*?</div>\s*<!-- Mensaje sin resultados -->'

$replacement = '<div class="agencias-list" id="agencias-container">
                <div style="grid-column: 1 / -1; text-align: center; padding: 50px 20px; color: #64748b; font-size: 15px;">
                    <i class="fas fa-spinner fa-spin" style="font-size: 32px; color: #196f3d; margin-bottom: 15px;"></i><br>
                    Cargando agencias de Travelink...
                </div>
            </div>

            <!-- Mensaje sin resultados -->'

$newContent = $content -replace $pattern, $replacement
Set-Content $file $newContent -NoNewline
Write-Host "agencias.html flicker fix applied!"
