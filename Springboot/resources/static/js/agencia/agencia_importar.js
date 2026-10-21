/**
 * agencia_importar.js
 * Manejo de importación masiva de disponibilidad (Excel .xlsx y CSV) para la agencia.
 */
document.addEventListener("DOMContentLoaded", () => {
    const btnAbrirModal = document.getElementById("btnAbrirModalImportar");
    const modalImportar = document.getElementById("modalImportar");
    const btnCerrarModal = document.getElementById("btnCerrarModalImportar");
    const btnCancelar = document.getElementById("btnCancelarImportar");
    const formImportar = document.getElementById("formImportarDisponibilidad");
    const inputArchivo = document.getElementById("inputArchivoImportar");
    const contenedorResultado = document.getElementById("resultadoImportacion");
    const btnEjecutar = document.getElementById("btnEjecutarImportar");

    if (!btnAbrirModal || !modalImportar) return;

    const abrirModal = () => {
        modalImportar.style.display = "flex";
        if (formImportar) formImportar.reset();
        if (contenedorResultado) {
            contenedorResultado.style.display = "none";
            contenedorResultado.innerHTML = "";
        }
        if (btnEjecutar) {
            btnEjecutar.disabled = false;
            btnEjecutar.innerHTML = '<i class="ti ti-upload"></i> Subir e importar';
        }
    };

    const cerrarModal = () => {
        modalImportar.style.display = "none";
    };

    btnAbrirModal.addEventListener("click", abrirModal);
    if (btnCerrarModal) btnCerrarModal.addEventListener("click", cerrarModal);
    if (btnCancelar) btnCancelar.addEventListener("click", cerrarModal);

    modalImportar.addEventListener("click", (e) => {
        if (e.target === modalImportar) cerrarModal();
    });

    if (formImportar) {
        formImportar.addEventListener("submit", async (e) => {
            e.preventDefault();

            const file = inputArchivo?.files?.[0];
            if (!file) {
                alert("Por favor seleccione un archivo .xlsx o .csv");
                return;
            }

            // Validar tamaño en frontend (5 MB)
            const MAX_BYTES = 5 * 1024 * 1024;
            if (file.size > MAX_BYTES) {
                alert(`El archivo seleccionado supera el límite de 5 MB (${(file.size / (1024 * 1024)).toFixed(2)} MB).`);
                return;
            }

            // Validar extensión
            const nombre = file.name.toLowerCase();
            if (!nombre.endsWith(".xlsx") && !nombre.endsWith(".csv")) {
                alert("Formato no válido. Debe ser un archivo .xlsx o .csv");
                return;
            }

            const formData = new FormData();
            formData.append("archivo", file);

            btnEjecutar.disabled = true;
            btnEjecutar.innerHTML = '<i class="ti ti-loader ti-spin"></i> Procesando archivo...';
            contenedorResultado.style.display = "none";
            contenedorResultado.innerHTML = "";

            try {
                const response = await fetch("/api/agencia/importar/disponibilidad", {
                    method: "POST",
                    body: formData,
                    credentials: "include"
                });

                const data = await response.json();

                btnEjecutar.disabled = false;
                btnEjecutar.innerHTML = '<i class="ti ti-upload"></i> Subir e importar';
                contenedorResultado.style.display = "block";

                const isSuccess = data.status === "success";
                const isWarning = data.status === "warning";
                const bgColor = isSuccess ? "#f0fdf4" : (isWarning ? "#fffbeb" : "#fef2f2");
                const borderColor = isSuccess ? "#86efac" : (isWarning ? "#fde68a" : "#fca5a5");
                const textColor = isSuccess ? "#166534" : (isWarning ? "#92400e" : "#991b1b");
                const iconClass = isSuccess ? "ti-circle-check" : (isWarning ? "ti-alert-triangle" : "ti-alert-circle");

                let html = `
                    <div style="background:${bgColor}; border:1px solid ${borderColor}; border-radius:10px; padding:14px; color:${textColor}; font-size:13px;">
                        <div style="display:flex; align-items:center; gap:8px; font-weight:700; margin-bottom:6px; font-size:14px;">
                            <i class="ti ${iconClass}" style="font-size:18px;"></i> ${data.mensaje || 'Procesamiento concluido'}
                        </div>
                        <div style="display:flex; gap:16px; margin-top:8px; font-size:12px;">
                            <span><strong>Total leídas:</strong> ${data.totalFilas || 0}</span>
                            <span style="color:#16a34a;"><strong>Importadas:</strong> ${data.filasImportadas || 0}</span>
                            <span style="color:#dc2626;"><strong>Con error:</strong> ${data.filasError || 0}</span>
                        </div>
                `;

                if (Array.isArray(data.errores) && data.errores.length > 0) {
                    html += `
                        <div style="margin-top:12px; border-top:1px dashed ${borderColor}; padding-top:10px;">
                            <strong style="display:block; margin-bottom:6px; font-size:12px;">Detalle de filas con observaciones:</strong>
                            <div style="max-height:140px; overflow-y:auto; background:white; border:1px solid ${borderColor}; border-radius:6px; padding:6px 10px;">
                                <ul style="margin:0; padding-left:16px; font-size:11.5px; color:#475569;">
                    `;
                    data.errores.forEach(err => {
                        html += `<li><strong>Fila ${err.fila}:</strong> ${err.motivo}</li>`;
                    });
                    html += `
                                </ul>
                            </div>
                        </div>
                    `;
                }

                html += `</div>`;
                contenedorResultado.innerHTML = html;

                // Si se importó al menos una fecha con éxito, recargar la lista de paquetes para reflejar cupos
                if (data.filasImportadas > 0 && typeof window.cargarPaquetes === "function") {
                    setTimeout(() => window.cargarPaquetes(), 1200);
                }

            } catch (err) {
                btnEjecutar.disabled = false;
                btnEjecutar.innerHTML = '<i class="ti ti-upload"></i> Subir e importar';
                contenedorResultado.style.display = "block";
                contenedorResultado.innerHTML = `
                    <div style="background:#fef2f2; border:1px solid #fca5a5; border-radius:10px; padding:12px; color:#991b1b; font-size:13px;">
                        <i class="ti ti-alert-circle"></i> Error de conexión con el servidor al procesar la importación: ${err.message}
                    </div>
                `;
            }
        });
    }
});
