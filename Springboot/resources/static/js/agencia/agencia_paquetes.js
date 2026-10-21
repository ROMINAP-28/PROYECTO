/**
 * TRAVELINK - MÓDULO UNIFICADO DE PAQUETES TURÍSTICOS (AGENCIA)
 * Fase 5: Vista unificada de gestión de paquetes, salidas y cupos
 */
(function () {
    let todosLosPaquetes = [];
    let paquetesFiltrados = [];
    let listaDestinos = [];
    let salidasEnFormulario = [];

    // DOM Elements - Grid & Filtros
    const gridPaquetes = document.getElementById("gridPaquetes");
    const buscarPaquete = document.getElementById("buscarPaquete");
    const filtroDestinoPaquete = document.getElementById("filtroDestinoPaquete");
    const filtroEstadoPaquete = document.getElementById("filtroEstadoPaquete");
    const btnAplicarFiltros = document.getElementById("btnAplicarFiltros");
    const btnLimpiarFiltros = document.getElementById("btnLimpiarFiltros");
    const btnNuevoPaquete = document.getElementById("btnNuevoPaquete");

    // DOM Elements - Modal Formulario
    const modalPaquete = document.getElementById("modalPaquete");
    const cerrarModalPaquete = document.getElementById("cerrarModalPaquete");
    const btnCancelarPaquete = document.getElementById("btnCancelarPaquete");
    const formPaquete = document.getElementById("formPaquete");
    const tituloModalPaquete = document.getElementById("tituloModalPaquete");
    const mensajePaquete = document.getElementById("mensajePaquete");
    const btnGuardarPaqueteSubmit = document.getElementById("btnGuardarPaqueteSubmit");

    // Campos del Formulario
    const idPaqueteInput = document.getElementById("idPaqueteInput");
    const nombrePaqueteInput = document.getElementById("nombrePaqueteInput");
    const idDestinoInput = document.getElementById("idDestinoInput");
    const categoriaPaqueteSelect = document.getElementById("categoriaPaqueteSelect");

    const descCortaPaqueteInput = document.getElementById("descCortaPaqueteInput");
    const descPaqueteInput = document.getElementById("descPaqueteInput");

    const diasPaqueteInput = document.getElementById("diasPaqueteInput");
    const horasPaqueteInput = document.getElementById("horasPaqueteInput");
    const itinerarioPaqueteInput = document.getElementById("itinerarioPaqueteInput");

    const precioAdultoInput = document.getElementById("precioAdultoInput");
    const aceptaNinosCheckbox = document.getElementById("aceptaNinosCheckbox");
    const contenedorNinos = document.getElementById("contenedorNinos");
    const precioNinoInput = document.getElementById("precioNinoInput");
    const edadMaxNinoInput = document.getElementById("edadMaxNinoInput");
    const aceptaBebesCheckbox = document.getElementById("aceptaBebesCheckbox");
    const contenedorBebes = document.getElementById("contenedorBebes");
    const precioBebeInput = document.getElementById("precioBebeInput");
    const edadMaxBebeInput = document.getElementById("edadMaxBebeInput");

    const queIncluyeInput = document.getElementById("queIncluyeInput");
    const queNoIncluyeInput = document.getElementById("queNoIncluyeInput");
    const condicionesPaqueteInput = document.getElementById("condicionesPaqueteInput");
    const descuentoPaqueteInput = document.getElementById("descuentoPaqueteInput");

    const imagenPaqueteInput = document.getElementById("imagenPaqueteInput");
    const imgPreviewPortada = document.getElementById("imgPreviewPortada");
    const imgGaleria1 = document.getElementById("imgGaleria1");
    const imgPreviewGaleria1 = document.getElementById("imgPreviewGaleria1");
    const imgGaleria2 = document.getElementById("imgGaleria2");
    const imgPreviewGaleria2 = document.getElementById("imgPreviewGaleria2");

    // Salidas & Generador
    const badgeContadorSalidas = document.getElementById("badgeContadorSalidas");
    const btnToggleSalidaIndividual = document.getElementById("btnToggleSalidaIndividual");
    const btnToggleGeneradorSalidas = document.getElementById("btnToggleGeneradorSalidas");
    const boxSalidaIndividual = document.getElementById("boxSalidaIndividual");
    const boxGeneradorSalidas = document.getElementById("boxGeneradorSalidas");
    const inputFechaSalida = document.getElementById("inputFechaSalida");
    const inputHoraSalida = document.getElementById("inputHoraSalida");
    const inputCupoSalida = document.getElementById("inputCupoSalida");
    const btnAgregarSalidaLista = document.getElementById("btnAgregarSalidaLista");

    const genFechaDesde = document.getElementById("genFechaDesde");
    const genFechaHasta = document.getElementById("genFechaHasta");
    const genHora = document.getElementById("genHora");
    const genCupo = document.getElementById("genCupo");
    const btnEjecutarGenerador = document.getElementById("btnEjecutarGenerador");

    const tbodySalidasModal = document.getElementById("tbodySalidasModal");
    const estadoPaqueteSelect = document.getElementById("estadoPaqueteSelect");

    document.addEventListener("DOMContentLoaded", async () => {
        setupTopBar();
        setupPasajerosToggles();
        setupSalidasEventos();
        setupModal();
        setupFiltros();
        await cargarDestinos();
        await cargarPaquetes();
    });

    // 1. TOPBAR & PERFIL
    function setupTopBar() {
        const topNombre = document.getElementById("nombreAgenciaTop");
        const dropNombre = document.getElementById("dropdownNombreAgencia");
        const avatarLetter = document.getElementById("agencyAvatarLetter");

        // Intentar actualizar con el usuario autenticado real
        apiFetch("/api/auth/me")
            .then(res => res.json())
            .then(data => {
                if (data.status === "success" && data.usuario) {
                    const nombre = data.usuario.nombre || data.usuario.nombreComercial || data.usuario.username || "Agencia";
                    if (topNombre) topNombre.textContent = nombre;
                    if (dropNombre) dropNombre.textContent = nombre;
                    if (avatarLetter) avatarLetter.textContent = nombre.charAt(0).toUpperCase();
                }
            })
            .catch(() => {});

        const btnNotif = document.getElementById("btnNotifAgencia");
        const dropNotif = document.getElementById("notifDropdownAgencia");
        const btnPerfil = document.getElementById("btnPerfilToggle");
        const dropPerfil = document.getElementById("perfilDropdownAgencia");

        if (btnNotif && dropNotif) {
            btnNotif.onclick = (e) => {
                e.stopPropagation();
                if (dropPerfil) dropPerfil.style.display = "none";
                dropNotif.style.display = dropNotif.style.display === "block" ? "none" : "block";
            };
        }
        if (btnPerfil && dropPerfil) {
            btnPerfil.onclick = (e) => {
                e.stopPropagation();
                if (dropNotif) dropNotif.style.display = "none";
                dropPerfil.style.display = dropPerfil.style.display === "block" ? "none" : "block";
            };
        }
        document.addEventListener("click", () => {
            if (dropNotif) dropNotif.style.display = "none";
            if (dropPerfil) dropPerfil.style.display = "none";
        });

        const btnLogout = document.getElementById("btnCerrarSesionDropdown");
        if (btnLogout) {
            btnLogout.onclick = (e) => {
                e.preventDefault();
                if (window.cerrarSesion) {
                    window.cerrarSesion(e);
                } else {
                    apiFetch("/api/auth/logout", { method: "POST" }).finally(() => {
                        window.location.href = "/index.html";
                    });
                }
            };
        }
    }

    // 2. CARGAR DESTINOS DINÁMICOS
    async function cargarDestinos() {
        try {
            const res = await apiFetch("/api/destinos");
            if (!res.ok) throw new Error("HTTP " + res.status);
            const data = await res.json();
            const list = Array.isArray(data.data) ? data.data : (Array.isArray(data.destinos) ? data.destinos : []);
            listaDestinos = list;

            if (idDestinoInput) {
                idDestinoInput.innerHTML = `<option value="">Selecciona un destino...</option>` +
                    list.map(d => `<option value="${d.id}">${d.nombre}</option>`).join("");
            }
            if (filtroDestinoPaquete) {
                filtroDestinoPaquete.innerHTML = `<option value="">Todos los destinos</option>` +
                    list.map(d => `<option value="${d.nombre}">${d.nombre}</option>`).join("");
            }
        } catch (err) {
            console.error("Error al cargar destinos:", err);
        }
    }

    // 3. CARGAR PAQUETES
    async function cargarPaquetes() {
        if (!gridPaquetes) return;
        gridPaquetes.innerHTML = `
            <div style="grid-column: 1/-1; text-align:center; padding:50px; color:#64748b;">
                <i class="ti ti-loader-2" style="font-size:32px; animation: spin 1s linear infinite; display:block; margin-bottom:10px;"></i>
                Cargando paquetes turísticos...
            </div>`;

        try {
            const res = await apiFetch("/api/agencia/paquetes");
            if (!res.ok) throw new Error("HTTP " + res.status);
            const data = await res.json();
            if (data.status === "success" && Array.isArray(data.paquetes)) {
                todosLosPaquetes = data.paquetes;
                aplicarFiltros();
            } else {
                throw new Error(data.message || "Error al leer paquetes");
            }
        } catch (e) {
            console.error("Error al cargar paquetes:", e);
            gridPaquetes.innerHTML = `
                <div style="grid-column: 1/-1; text-align:center; padding:50px; color:#ef4444; background:white; border-radius:12px; border:1px solid #fee2e2;">
                    <i class="ti ti-alert-triangle" style="font-size:32px; display:block; margin-bottom:10px;"></i>
                    Error al conectar con el servidor: ${e.message}
                </div>`;
            todosLosPaquetes = [];
            aplicarFiltros();
        }
    }

    // 4. FILTROS Y BÚSQUEDA
    function setupFiltros() {
        if (buscarPaquete) {
            buscarPaquete.addEventListener("keyup", (e) => {
                if (e.key === "Enter") aplicarFiltros(e);
            });
        }
        if (btnAplicarFiltros) {
            btnAplicarFiltros.addEventListener("click", (e) => aplicarFiltros(e));
        }
        if (btnLimpiarFiltros) {
            btnLimpiarFiltros.addEventListener("click", () => {
                if (buscarPaquete) buscarPaquete.value = "";
                if (filtroDestinoPaquete) filtroDestinoPaquete.value = "";
                if (filtroEstadoPaquete) filtroEstadoPaquete.value = "";
                aplicarFiltros();
            });
        }
    }

    function aplicarFiltros() {
        const query = (buscarPaquete ? buscarPaquete.value : "").trim().toLowerCase();
        const dest = (filtroDestinoPaquete ? filtroDestinoPaquete.value : "").trim().toLowerCase();
        const est = (filtroEstadoPaquete ? filtroEstadoPaquete.value : "").trim().toUpperCase();

        paquetesFiltrados = todosLosPaquetes.filter(p => {
            const nom = String(p.nombre || "").toLowerCase();
            const desc = String(p.descripcion || "").toLowerCase();
            const descCorta = String(p.descripcionCorta || "").toLowerCase();
            const matchQuery = !query || nom.includes(query) || desc.includes(query) || descCorta.includes(query);

            const pDestino = String(p.destino || p.ubicacion || "").toLowerCase();
            const matchDest = !dest || pDestino.includes(dest);

            const pEstado = String(p.estado || "PUBLICADO").toUpperCase();
            const matchEst = !est || pEstado === est;

            return matchQuery && matchDest && matchEst;
        });

        renderizarGrid();

        if (btnAplicarFiltros && e && (e.type === 'click' || (e.key && e.key === 'Enter'))) {
            const originalHtml = btnAplicarFiltros.innerHTML;
            btnAplicarFiltros.innerHTML = '<i class="ti ti-check"></i> ¡Filtros aplicados!';
            setTimeout(() => {
                btnAplicarFiltros.innerHTML = originalHtml;
            }, 800);
        }
    }

    // 5. RENDERIZAR GRID DE TARJETAS
    function renderizarGrid() {
        if (!gridPaquetes) return;

        if (paquetesFiltrados.length === 0) {
            gridPaquetes.innerHTML = `
                <div class="paquete-card" style="text-align:center; padding:60px 20px; grid-column:1/-1; color:#94a3b8; background:white; border-radius:12px; border:1px dashed #cbd5e1;">
                    <i class="ti ti-package-off" style="font-size:44px; color:#cbd5e1; display:block; margin-bottom:12px;"></i>
                    <p style="margin:0 0 6px 0; font-size:16px; font-weight:600; color:#475569;">No se encontraron paquetes turísticos.</p>
                    <p style="margin:0; font-size:13px; color:#94a3b8;">Prueba ajustando los filtros o agrega un nuevo paquete con el botón superior.</p>
                </div>`;
            return;
        }

        gridPaquetes.innerHTML = paquetesFiltrados.map(p => {
            const id = p.idTour || p.idPaquete;
            const estado = String(p.estado || "PUBLICADO").toUpperCase();
            let badgeClass = "publicado";
            let badgeIcon = '<i class="ti ti-check"></i>';
            if (estado === "PAUSADO") {
                badgeClass = "pausado";
                badgeIcon = '<i class="ti ti-clock-pause"></i>';
            } else if (estado === "BORRADOR") {
                badgeClass = "borrador";
                badgeIcon = '<i class="ti ti-edit"></i>';
            }

            // Imagen
            let imgUrl = "../../img/machupicchu.png";
            if (p.imagenPrincipal && p.imagenPrincipal.trim() !== "") {
                imgUrl = normalizarUrlImagen(p.imagenPrincipal);
            } else if (p.imagen && p.imagen.trim() !== "") {
                imgUrl = normalizarUrlImagen(p.imagen);
            }

            // Precios niños y bebés
            let ninosTag = "";
            if (p.aceptaNinos) {
                const prNino = Number(p.precioNino || 0);
                const edMax = p.edadMaxNino || 11;
                ninosTag = `<div style="font-size:12px; color:#475569; display:flex; align-items:center; gap:4px;">
                    <i class="ti ti-mood-kid" style="color:#2563eb;"></i> Niños: S/ ${prNino.toFixed(2)} (hasta ${edMax}a)
                </div>`;
            }

            let bebesTag = "";
            if (p.aceptaBebes) {
                const prBebe = Number(p.precioBebe || 0);
                const edMax = p.edadMaxBebe || 2;
                const txtBebe = prBebe > 0 ? `S/ ${prBebe.toFixed(2)}` : "Gratis";
                bebesTag = `<div style="font-size:12px; color:#475569; display:flex; align-items:center; gap:4px;">
                    <i class="ti ti-baby-carriage" style="color:#059669;"></i> Bebés: ${txtBebe} (hasta ${edMax}a)
                </div>`;
            }

            // Botón publicar / pausar
            const btnToggleEstado = estado === "PUBLICADO"
                ? `<button type="button" class="btn-card-action btn-card-pause" onclick="window.cambiarEstadoPaquete(${id}, 'pausar')" title="Pausar publicación">
                     <i class="ti ti-player-pause"></i> Pausar
                   </button>`
                : `<button type="button" class="btn-card-action btn-card-publish" onclick="window.cambiarEstadoPaquete(${id}, 'publicar')" title="Publicar paquete">
                     <i class="ti ti-send"></i> Publicar
                   </button>`;

            // Descuento badge
            const descBadge = Number(p.descuento) > 0
                ? `<span class="paquete-discount-badge" style="background:#dcfce7; color:#15803d; font-weight:700; font-size:11px; padding:3px 8px; border-radius:6px;">-${p.descuento}% desc.</span>`
                : "";

            // Salidas contador
            const totalSalidas = Array.isArray(p.salidas) ? p.salidas.length : 0;
            const futurasSalidas = p.totalSalidasFuturas !== undefined ? p.totalSalidasFuturas : totalSalidas;
            const salidasTagColor = futurasSalidas > 0 ? "#eff6ff" : "#fef2f2";
            const salidasTextColor = futurasSalidas > 0 ? "#1d4ed8" : "#b91c1c";

            return `
                <div class="paquete-card" style="background:white; border-radius:12px; border:1px solid #e2e8f0; overflow:hidden; display:flex; flex-direction:column; box-shadow:0 4px 12px rgba(0,0,0,0.03); transition:transform 0.2s, box-shadow 0.2s;">
                    <div class="paquete-img-wrap" style="position:relative; width:100%; height:180px; overflow:hidden; background:#f1f5f9;">
                        <img loading="lazy" src="${imgUrl}" alt="${escapeHtml(p.nombre)}" class="paquete-card-img" style="width:100%; height:100%; object-fit:cover;" onerror="this.onerror=null; this.src='../../img/machupicchu.png';">
                        <span class="paquete-status-badge ${badgeClass}" style="position:absolute; top:12px; left:12px;">
                            ${badgeIcon} ${estado}
                        </span>
                        <span style="position:absolute; bottom:10px; right:12px; background:rgba(15,23,42,0.75); color:white; font-size:11px; font-weight:600; padding:3px 8px; border-radius:6px; backdrop-filter:blur(2px);">
                            <i class="ti ti-map-pin"></i> ${escapeHtml(p.destino || p.ubicacion || 'Perú')}
                        </span>
                    </div>

                    <div class="paquete-card-body" style="padding:16px; flex:1; display:flex; flex-direction:column;">
                        <div style="display:flex; justify-content:space-between; align-items:flex-start; margin-bottom:6px; gap:8px;">
                            <span style="background:#f1f5f9; color:#475569; font-size:11px; font-weight:700; text-transform:uppercase; padding:3px 8px; border-radius:6px;">
                                ${escapeHtml(p.categoria || 'Cultural')}
                            </span>
                            ${descBadge}
                        </div>

                        <h3 class="paquete-title" style="margin:0 0 6px 0; font-size:16px; font-weight:700; color:#0f172a; line-height:1.3;">
                            ${escapeHtml(p.nombre)}
                        </h3>

                        <p class="paquete-desc" style="margin:0 0 12px 0; font-size:13px; color:#64748b; line-height:1.4; display:-webkit-box; -webkit-line-clamp:2; -webkit-box-orient:vertical; overflow:hidden;">
                            ${escapeHtml(p.descripcionCorta || p.descripcion || 'Sin descripción disponible.')}
                        </p>

                        <!-- Indicador de Salidas -->
                        <div style="background:${salidasTagColor}; color:${salidasTextColor}; border-radius:6px; padding:6px 10px; font-size:12px; font-weight:600; margin-bottom:12px; display:flex; align-items:center; justify-content:space-between;">
                            <span><i class="ti ti-calendar-event"></i> ${futurasSalidas} salidas activas</span>
                            <span style="font-size:11px; opacity:0.85;">${totalSalidas} total</span>
                        </div>

                        <!-- Precios y Duración -->
                        <div style="background:#f8fafc; border:1px solid #f1f5f9; border-radius:8px; padding:10px; margin-bottom:14px; margin-top:auto;">
                            <div style="display:flex; justify-content:space-between; align-items:baseline; margin-bottom:4px;">
                                <span style="font-size:11px; color:#64748b; text-transform:uppercase; font-weight:600;">Precio Adulto</span>
                                <span style="font-size:17px; font-weight:800; color:#0f172a;">S/ ${Number(p.precioAdulto || p.precio || 0).toFixed(2)}</span>
                            </div>
                            <div style="display:flex; flex-direction:column; gap:2px;">
                                ${ninosTag}
                                ${bebesTag}
                            </div>
                            <div style="font-size:11px; color:#64748b; margin-top:6px; border-top:1px dashed #e2e8f0; padding-top:4px; display:flex; justify-content:space-between;">
                                <span><i class="ti ti-clock"></i> ${escapeHtml(p.duracion || 'Personalizado')}</span>
                                <span>Cupo: ${p.cupoTotal || 0}</span>
                            </div>
                        </div>

                        <!-- Botones de Acción -->
                        <div class="paquete-actions-row" style="display:grid; grid-template-columns:1fr 1fr; gap:8px;">
                            <button type="button" class="btn-card-action" onclick="window.abrirDetallesPaquete(${id})" style="padding:7px; font-size:12px; background:#f1f5f9; color:#334155; border:1px solid #cbd5e1; border-radius:6px; cursor:pointer; font-weight:600; display:flex; align-items:center; justify-content:center; gap:4px;">
                                <i class="ti ti-eye"></i> Detalles
                            </button>
                            <button type="button" class="btn-card-action btn-card-edit" onclick="window.abrirEditarPaquete(${id})" style="padding:7px; font-size:12px; background:#eff6ff; color:#1d4ed8; border:1px solid #bfdbfe; border-radius:6px; cursor:pointer; font-weight:600; display:flex; align-items:center; justify-content:center; gap:4px;">
                                <i class="ti ti-pencil"></i> Editar
                            </button>
                            ${btnToggleEstado}
                            <button type="button" class="btn-card-action btn-card-delete" onclick="window.eliminarPaquete(${id}, '${escapeHtml(p.nombre).replace(/'/g, "\\'")}')" style="padding:7px; font-size:12px; background:#fef2f2; color:#dc2626; border:1px solid #fecaca; border-radius:6px; cursor:pointer; font-weight:600; display:flex; align-items:center; justify-content:center; gap:4px;">
                                <i class="ti ti-trash"></i> Eliminar
                            </button>
                        </div>
                    </div>
                </div>
            `;
        }).join("");
    }

    // 6. TOGGLES PASAJEROS (NIÑOS Y BEBÉS)
    function setupPasajerosToggles() {
        if (aceptaNinosCheckbox && contenedorNinos) {
            aceptaNinosCheckbox.addEventListener("change", () => {
                contenedorNinos.style.display = aceptaNinosCheckbox.checked ? "flex" : "none";
                if (!aceptaNinosCheckbox.checked) {
                    if (precioNinoInput) precioNinoInput.value = "";
                }
            });
        }
        if (aceptaBebesCheckbox && contenedorBebes) {
            aceptaBebesCheckbox.addEventListener("change", () => {
                contenedorBebes.style.display = aceptaBebesCheckbox.checked ? "flex" : "none";
                if (!aceptaBebesCheckbox.checked) {
                    if (precioBebeInput) precioBebeInput.value = "";
                }
            });
        }
    }

    // 7. PROGRAMADOR DE SALIDAS (TourFecha)
    function setupSalidasEventos() {
        // Toggle subform individual
        if (btnToggleSalidaIndividual && boxSalidaIndividual) {
            btnToggleSalidaIndividual.addEventListener("click", () => {
                if (boxGeneradorSalidas) boxGeneradorSalidas.style.display = "none";
                const isVis = boxSalidaIndividual.style.display === "block";
                boxSalidaIndividual.style.display = isVis ? "none" : "block";
                if (!isVis && inputFechaSalida) {
                    // Default fecha mínima hoy
                    const hoyStr = new Date().toISOString().split("T")[0];
                    inputFechaSalida.min = hoyStr;
                    if (!inputFechaSalida.value) inputFechaSalida.value = hoyStr;
                }
            });
        }

        // Toggle subform generador
        if (btnToggleGeneradorSalidas && boxGeneradorSalidas) {
            btnToggleGeneradorSalidas.addEventListener("click", () => {
                if (boxSalidaIndividual) boxSalidaIndividual.style.display = "none";
                const isVis = boxGeneradorSalidas.style.display === "block";
                boxGeneradorSalidas.style.display = isVis ? "none" : "block";
                if (!isVis && genFechaDesde && genFechaHasta) {
                    const hoy = new Date();
                    const en7d = new Date();
                    en7d.setDate(hoy.getDate() + 14);
                    const hoyStr = hoy.toISOString().split("T")[0];
                    genFechaDesde.min = hoyStr;
                    genFechaHasta.min = hoyStr;
                    if (!genFechaDesde.value) genFechaDesde.value = hoyStr;
                    if (!genFechaHasta.value) genFechaHasta.value = en7d.toISOString().split("T")[0];
                }
            });
        }

        // Añadir salida individual
        if (btnAgregarSalidaLista) {
            btnAgregarSalidaLista.addEventListener("click", () => {
                const fecha = inputFechaSalida.value;
                const hora = inputHoraSalida.value || "08:00";
                const cupo = parseInt(inputCupoSalida.value || "0", 10);

                if (!fecha) {
                    alert("Selecciona una fecha válida.");
                    return;
                }
                const hoyStr = new Date().toISOString().split("T")[0];
                if (fecha < hoyStr) {
                    alert("No puedes agregar salidas en fechas pasadas.");
                    return;
                }
                if (isNaN(cupo) || cupo <= 0) {
                    alert("El cupo total debe ser mayor a 0.");
                    return;
                }

                // Evitar duplicado exacto
                const yaExiste = salidasEnFormulario.some(s => s.fecha === fecha && s.horaInicio === hora && s.estado !== "CANCELADO");
                if (yaExiste) {
                    alert("Ya existe una salida programada para la misma fecha y hora.");
                    return;
                }

                salidasEnFormulario.push({
                    idTourFecha: null,
                    fecha: fecha,
                    horaInicio: hora,
                    cupoTotal: cupo,
                    cupoDisponible: cupo,
                    estado: "DISPONIBLE"
                });

                boxSalidaIndividual.style.display = "none";
                renderizarTablaSalidas();
            });
        }

        // Atajo: Generador de salidas múltiples
        if (btnEjecutarGenerador) {
            btnEjecutarGenerador.addEventListener("click", () => {
                const fDesde = genFechaDesde.value;
                const fHasta = genFechaHasta.value;
                const hora = genHora.value || "08:00";
                const cupo = parseInt(genCupo.value || "0", 10);

                if (!fDesde || !fHasta) {
                    alert("Ingresa un rango de fechas completo (desde y hasta).");
                    return;
                }
                if (fDesde > fHasta) {
                    alert("La fecha de inicio no puede ser posterior a la fecha final.");
                    return;
                }
                const hoyStr = new Date().toISOString().split("T")[0];
                if (fDesde < hoyStr) {
                    alert("El rango de fechas no puede comenzar en el pasado.");
                    return;
                }
                if (isNaN(cupo) || cupo <= 0) {
                    alert("El cupo total debe ser mayor a 0.");
                    return;
                }

                // Días seleccionados
                const checkBoxes = document.querySelectorAll(".gen-dia:checked");
                if (checkBoxes.length === 0) {
                    alert("Selecciona al menos un día de la semana.");
                    return;
                }
                const diasSeleccionados = Array.from(checkBoxes).map(cb => parseInt(cb.value, 10));

                let agregadas = 0;
                let iter = new Date(fDesde + "T00:00:00");
                const limite = new Date(fHasta + "T00:00:00");

                while (iter <= limite) {
                    const diaSemana = iter.getDay(); // 0 = Domingo, 1 = Lunes, etc.
                    if (diasSeleccionados.includes(diaSemana)) {
                        const y = iter.getFullYear();
                        const m = String(iter.getMonth() + 1).padStart(2, "0");
                        const d = String(iter.getDate()).padStart(2, "0");
                        const fechaStr = `${y}-${m}-${d}`;

                        const yaExiste = salidasEnFormulario.some(s => s.fecha === fechaStr && s.horaInicio === hora && s.estado !== "CANCELADO");
                        if (!yaExiste) {
                            salidasEnFormulario.push({
                                idTourFecha: null,
                                fecha: fechaStr,
                                horaInicio: hora,
                                cupoTotal: cupo,
                                cupoDisponible: cupo,
                                estado: "DISPONIBLE"
                            });
                            agregadas++;
                        }
                    }
                    iter.setDate(iter.getDate() + 1);
                }

                boxGeneradorSalidas.style.display = "none";
                renderizarTablaSalidas();

                if (window.Swal) {
                    Swal.fire({
                        icon: "success",
                        title: "Salidas generadas",
                        text: `Se agregaron ${agregadas} salidas programadas a la lista.`,
                        timer: 2000,
                        showConfirmButton: false
                    });
                } else {
                    alert(`Se agregaron ${agregadas} salidas programadas a la lista.`);
                }
            });
        }
    }

    // Renderizar tabla de salidas en modal
    function renderizarTablaSalidas() {
        if (!tbodySalidasModal) return;

        // Ordenar cronológicamente
        salidasEnFormulario.sort((a, b) => {
            if (a.fecha !== b.fecha) return a.fecha.localeCompare(b.fecha);
            return (a.horaInicio || "").localeCompare(b.horaInicio || "");
        });

        const hoyStr = new Date().toISOString().split("T")[0];
        const salidasFuturasActivas = salidasEnFormulario.filter(s => s.fecha >= hoyStr && s.estado !== "PAUSADO" && s.estado !== "CANCELADO").length;

        if (badgeContadorSalidas) {
            badgeContadorSalidas.textContent = `${salidasEnFormulario.length} salidas (${salidasFuturasActivas} futuras activas)`;
            badgeContadorSalidas.style.background = salidasFuturasActivas > 0 ? "#dbeafe" : "#fee2e2";
            badgeContadorSalidas.style.color = salidasFuturasActivas > 0 ? "#1e40af" : "#b91c1c";
        }

        if (salidasEnFormulario.length === 0) {
            tbodySalidasModal.innerHTML = `
                <tr>
                    <td colspan="5" style="text-align:center; color:#94a3b8; padding:16px;">
                        No hay salidas registradas. Agrega al menos una salida futura para poder publicar el paquete.
                    </td>
                </tr>`;
            return;
        }

        tbodySalidasModal.innerHTML = salidasEnFormulario.map((s, idx) => {
            const isPausada = s.estado === "PAUSADO";
            const badgeEstado = isPausada
                ? `<span style="background:#fef3c7; color:#92400e; font-size:11px; font-weight:700; padding:2px 8px; border-radius:4px;">PAUSADA</span>`
                : `<span style="background:#dcfce7; color:#15803d; font-size:11px; font-weight:700; padding:2px 8px; border-radius:4px;">DISPONIBLE</span>`;

            const btnPauseText = isPausada ? `<i class="ti ti-player-play"></i> Reanudar` : `<i class="ti ti-player-pause"></i> Pausar`;
            const btnPauseColor = isPausada ? "#059669" : "#d97706";

            return `
                <tr style="${s.fecha < hoyStr ? 'opacity:0.6;' : ''}">
                    <td><strong>${formatearFecha(s.fecha)}</strong> ${s.fecha < hoyStr ? '<small style="color:#ef4444;">(Pasada)</small>' : ''}</td>
                    <td>${s.horaInicio || '08:00'}</td>
                    <td>${s.cupoTotal}</td>
                    <td>${badgeEstado}</td>
                    <td style="text-align:right; white-space:nowrap;">
                        <button type="button" onclick="window.toggleSalidaEstado(${idx})" style="background:none; border:none; color:${btnPauseColor}; font-size:12px; font-weight:600; cursor:pointer; padding:4px 8px;">
                            ${btnPauseText}
                        </button>
                        <button type="button" onclick="window.eliminarSalidaFormulario(${idx})" style="background:none; border:none; color:#ef4444; font-size:12px; font-weight:600; cursor:pointer; padding:4px 8px;">
                            <i class="ti ti-trash"></i> Eliminar
                        </button>
                    </td>
                </tr>
            `;
        }).join("");
    }

    // Toggle estado salida (Pausar / Reanudar)
    window.toggleSalidaEstado = async function (idx) {
        const s = salidasEnFormulario[idx];
        if (!s) return;
        const nuevoEstado = s.estado === "PAUSADO" ? "DISPONIBLE" : "PAUSADO";

        if (s.idTourFecha) {
            try {
                const res = await apiFetch("/api/agencia/paquetes", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({
                        action: "cambiar_estado_salida",
                        idTourFecha: s.idTourFecha,
                        estado: nuevoEstado
                    })
                });
                const data = await res.json();
                if (data.status === "success") {
                    s.estado = nuevoEstado;
                    renderizarTablaSalidas();
                } else {
                    alert(data.message || "Error al actualizar salida");
                }
            } catch (e) {
                console.error("Error toggle salida:", e);
                alert("Error de red al actualizar estado de salida");
            }
        } else {
            // Local en formulario
            s.estado = nuevoEstado;
            renderizarTablaSalidas();
        }
    };

    // Eliminar o pausar salida
    window.eliminarSalidaFormulario = async function (idx) {
        const s = salidasEnFormulario[idx];
        if (!s) return;

        if (!confirm(`¿Deseas eliminar la salida del ${s.fecha}?`)) return;

        if (s.idTourFecha) {
            try {
                const res = await apiFetch("/api/agencia/paquetes", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({
                        action: "eliminar_salida",
                        idTourFecha: s.idTourFecha
                    })
                });
                const data = await res.json();
                if (data.status === "success") {
                    if (data.message && data.message.includes("pausado")) {
                        // Regla (g): La salida tiene reservas, se pausó en lugar de eliminarse
                        alert(data.message);
                        s.estado = "PAUSADO";
                    } else {
                        salidasEnFormulario.splice(idx, 1);
                    }
                    renderizarTablaSalidas();
                } else {
                    alert(data.message || "Error al eliminar salida");
                }
            } catch (e) {
                console.error("Error al eliminar salida:", e);
                alert("Error de red al eliminar salida");
            }
        } else {
            salidasEnFormulario.splice(idx, 1);
            renderizarTablaSalidas();
        }
    };

    // 8. MODAL & FORMULARIO
    function setupModal() {
        if (btnNuevoPaquete && modalPaquete) {
            btnNuevoPaquete.addEventListener("click", () => {
                resetearForm();
                if (tituloModalPaquete) tituloModalPaquete.textContent = "Nuevo paquete turístico";
                abrirModal(modalPaquete);
            });
        }
        if (cerrarModalPaquete && modalPaquete) {
            cerrarModalPaquete.addEventListener("click", () => cerrarModal(modalPaquete));
        }
        if (btnCancelarPaquete && modalPaquete) {
            btnCancelarPaquete.addEventListener("click", () => cerrarModal(modalPaquete));
        }

        // Cierre al hacer click en el backdrop
        [modalPaquete, document.getElementById("modalDetallesPaquete")].forEach(m => {
            if (m) {
                m.addEventListener("click", (e) => {
                    if (e.target === m) cerrarModal(m);
                });
            }
        });

        // SUBMIT DEL FORMULARIO
        if (formPaquete) {
            formPaquete.addEventListener("submit", async (e) => {
                e.preventDefault();

                const idPaq = idPaqueteInput.value;
                const nombre = nombrePaqueteInput.value.trim();
                const idDestino = parseInt(idDestinoInput.value || "0", 10);
                const categoria = categoriaPaqueteSelect.value;

                const descCorta = descCortaPaqueteInput.value.trim();
                const descCompleta = descPaqueteInput.value.trim();

                const dias = parseInt(diasPaqueteInput.value || "0", 10);
                const horas = parseInt(horasPaqueteInput.value || "0", 10);
                const itinerario = itinerarioPaqueteInput.value.trim();

                const precioAdulto = parseFloat(precioAdultoInput.value);
                const aceptaNinos = aceptaNinosCheckbox.checked;
                const precioNino = aceptaNinos ? parseFloat(precioNinoInput.value || "0") : 0;
                const edadMaxNino = aceptaNinos ? parseInt(edadMaxNinoInput.value || "11", 10) : 11;

                const aceptaBebes = aceptaBebesCheckbox.checked;
                const precioBebe = aceptaBebes ? parseFloat(precioBebeInput.value || "0") : 0;
                const edadMaxBebe = aceptaBebes ? parseInt(edadMaxBebeInput.value || "2", 10) : 2;

                const queIncluye = queIncluyeInput.value.trim();
                const queNoIncluye = queNoIncluyeInput.value.trim();
                const condiciones = condicionesPaqueteInput.value.trim();
                const descuento = parseFloat(descuentoPaqueteInput.value || "0");

                const imagenPortada = imagenPaqueteInput.value.trim() || "img/machupicchu.png";
                const gal1 = imgGaleria1 ? imgGaleria1.value.trim() : "";
                const gal2 = imgGaleria2 ? imgGaleria2.value.trim() : "";
                const estado = estadoPaqueteSelect.value || "PUBLICADO";

                // VALIDACIONES CLIENTE
                if (!nombre) {
                    mostrarMensaje("El nombre del paquete es obligatorio.", "error");
                    return;
                }
                if (!idDestino || idDestino <= 0) {
                    mostrarMensaje("Debes seleccionar un destino de la lista.", "error");
                    return;
                }
                if (!descCorta || !descCompleta) {
                    mostrarMensaje("Debes completar tanto la descripción corta como la descripción completa.", "error");
                    return;
                }
                if (isNaN(precioAdulto) || precioAdulto <= 0) {
                    mostrarMensaje("El precio para adultos debe ser mayor a 0.", "error");
                    return;
                }
                if (aceptaNinos) {
                    if (isNaN(precioNino) || precioNino < 0) {
                        mostrarMensaje("El precio para niños no puede ser negativo.", "error");
                        return;
                    }
                    if (edadMaxNino <= 0) {
                        mostrarMensaje("La edad máxima para niños debe ser mayor a 0.", "error");
                        return;
                    }
                }
                if (aceptaBebes) {
                    if (isNaN(precioBebe) || precioBebe < 0) {
                        mostrarMensaje("El precio para bebés no puede ser negativo (0 para gratis).", "error");
                        return;
                    }
                    if (edadMaxBebe < 0) {
                        mostrarMensaje("La edad máxima para bebés no puede ser negativa.", "error");
                        return;
                    }
                }
                if (aceptaNinos && aceptaBebes && edadMaxNino <= edadMaxBebe) {
                    mostrarMensaje(`La edad máxima de niños (${edadMaxNino}) debe ser mayor a la edad máxima de bebés (${edadMaxBebe}).`, "error");
                    return;
                }
                if (descuento < 0 || descuento > 90) {
                    mostrarMensaje("El descuento debe estar entre 0% y 90%.", "error");
                    return;
                }

                // REGLA (h): Un tour sin al menos una salida futura no puede publicarse
                const hoyStr = new Date().toISOString().split("T")[0];
                const salidasFuturasActivas = salidasEnFormulario.filter(s => s.fecha >= hoyStr && s.estado !== "PAUSADO" && s.estado !== "CANCELADO").length;

                if (estado === "PUBLICADO" && salidasFuturasActivas === 0) {
                    mostrarMensaje("Regla de publicación: Un paquete turístico no puede publicarse sin al menos una salida futura activa. Agrega una salida o guárdalo como Pausado.", "error");
                    return;
                }

                // Payload
                const payload = {
                    action: idPaq ? "editar" : "guardar",
                    idTour: idPaq ? Number(idPaq) : 0,
                    idPaquete: idPaq ? Number(idPaq) : 0,
                    nombre: nombre,
                    idDestino: idDestino,
                    categoria: categoria,
                    descripcionCorta: descCorta,
                    descripcion: descCompleta,
                    dias: dias,
                    horas: horas,
                    itinerario: itinerario,
                    precioAdulto: precioAdulto,
                    aceptaNinos: aceptaNinos,
                    precioNino: precioNino,
                    edadMaxNino: edadMaxNino,
                    aceptaBebes: aceptaBebes,
                    precioBebe: precioBebe,
                    edadMaxBebe: edadMaxBebe,
                    queIncluye: queIncluye,
                    queNoIncluye: queNoIncluye,
                    condiciones: condiciones,
                    descuento: descuento,
                    imagen: imagenPortada,
                    galeria: [gal1, gal2].filter(x => x && x.length > 0),
                    salidas: salidasEnFormulario,
                    estado: estado
                };

                btnGuardarPaqueteSubmit.disabled = true;
                btnGuardarPaqueteSubmit.textContent = "Guardando...";

                try {
                    const res = await apiFetch("/api/agencia/paquetes", {
                        method: "POST",
                        headers: { "Content-Type": "application/json" },
                        body: JSON.stringify(payload)
                    });
                    const data = await res.json();

                    if (data.status === "success") {
                        cerrarModal(modalPaquete);
                        if (window.Swal) {
                            Swal.fire({
                                icon: "success",
                                title: idPaq ? "Paquete actualizado" : "Paquete guardado",
                                text: data.message || "Los cambios se guardaron exitosamente.",
                                timer: 2000,
                                showConfirmButton: false
                            });
                        }
                        await cargarPaquetes();
                    } else {
                        mostrarMensaje(data.message || "Error al procesar la solicitud", "error");
                    }
                } catch (err) {
                    console.error("Error al guardar paquete:", err);
                    mostrarMensaje("Error al conectar con el servidor: " + err.message, "error");
                } finally {
                    btnGuardarPaqueteSubmit.disabled = false;
                    btnGuardarPaqueteSubmit.textContent = "Guardar paquete";
                }
            });
        }
    }

    // Slot de imagen upload helper
    window.cargarImagenSlot = function (fileInput, targetInputId, imgPreviewId) {
        if (!fileInput || !fileInput.files || fileInput.files.length === 0) return;
        const file = fileInput.files[0];
        const reader = new FileReader();
        reader.onload = (e) => {
            const dataUrl = e.target.result;
            const targetInput = document.getElementById(targetInputId);
            const imgPreview = document.getElementById(imgPreviewId);
            if (targetInput) targetInput.value = dataUrl;
            if (imgPreview) imgPreview.src = dataUrl;
        };
        reader.readAsDataURL(file);
    };

    function resetearForm() {
        salidasEnFormulario = [];
        if (idPaqueteInput) idPaqueteInput.value = "";
        if (nombrePaqueteInput) nombrePaqueteInput.value = "";
        if (idDestinoInput) idDestinoInput.value = "";
        if (categoriaPaqueteSelect) categoriaPaqueteSelect.value = "Cultural";

        if (descCortaPaqueteInput) descCortaPaqueteInput.value = "";
        if (descPaqueteInput) descPaqueteInput.value = "";

        if (diasPaqueteInput) diasPaqueteInput.value = "1";
        if (horasPaqueteInput) horasPaqueteInput.value = "4";
        if (itinerarioPaqueteInput) itinerarioPaqueteInput.value = "";

        if (precioAdultoInput) precioAdultoInput.value = "";
        if (aceptaNinosCheckbox) {
            aceptaNinosCheckbox.checked = true;
            if (contenedorNinos) contenedorNinos.style.display = "flex";
        }
        if (precioNinoInput) precioNinoInput.value = "";
        if (edadMaxNinoInput) edadMaxNinoInput.value = "11";

        if (aceptaBebesCheckbox) {
            aceptaBebesCheckbox.checked = true;
            if (contenedorBebes) contenedorBebes.style.display = "flex";
        }
        if (precioBebeInput) precioBebeInput.value = "0.00";
        if (edadMaxBebeInput) edadMaxBebeInput.value = "2";

        if (queIncluyeInput) queIncluyeInput.value = "";
        if (queNoIncluyeInput) queNoIncluyeInput.value = "";
        if (condicionesPaqueteInput) condicionesPaqueteInput.value = "";
        if (descuentoPaqueteInput) descuentoPaqueteInput.value = "0";

        if (imagenPaqueteInput) imagenPaqueteInput.value = "img/machupicchu.png";
        if (imgPreviewPortada) imgPreviewPortada.src = "../../img/machupicchu.png";
        if (imgGaleria1) imgGaleria1.value = "img/cusco.jpg";
        if (imgPreviewGaleria1) imgPreviewGaleria1.src = "../../img/cusco.jpg";
        if (imgGaleria2) imgGaleria2.value = "img/arequipa.jpg";
        if (imgPreviewGaleria2) imgPreviewGaleria2.src = "../../img/arequipa.jpg";

        if (estadoPaqueteSelect) estadoPaqueteSelect.value = "PUBLICADO";
        if (mensajePaquete) mensajePaquete.style.display = "none";
        if (boxSalidaIndividual) boxSalidaIndividual.style.display = "none";
        if (boxGeneradorSalidas) boxGeneradorSalidas.style.display = "none";

        renderizarTablaSalidas();
    }

    function mostrarMensaje(texto, tipo) {
        if (!mensajePaquete) {
            alert(texto);
            return;
        }
        mensajePaquete.textContent = texto;
        mensajePaquete.style.display = "block";
        mensajePaquete.style.background = tipo === "error" ? "#fee2e2" : "#dcfce7";
        mensajePaquete.style.color = tipo === "error" ? "#b91c1c" : "#15803d";
        mensajePaquete.style.border = tipo === "error" ? "1px solid #fecaca" : "1px solid #bbf7d0";
        mensajePaquete.scrollIntoView({ behavior: "smooth", block: "nearest" });
    }

    // 9. ABRIR EDITAR PAQUETE
    window.abrirEditarPaquete = function (id) {
        const p = todosLosPaquetes.find(x => String(x.idTour || x.idPaquete) === String(id));
        if (!p) {
            alert("No se encontró el paquete seleccionado.");
            return;
        }

        resetearForm();
        if (tituloModalPaquete) tituloModalPaquete.textContent = "Editar paquete turístico";

        idPaqueteInput.value = p.idTour || p.idPaquete;
        nombrePaqueteInput.value = p.nombre || "";
        if (idDestinoInput) idDestinoInput.value = p.idDestino || "";
        if (categoriaPaqueteSelect) categoriaPaqueteSelect.value = p.categoria || "Cultural";

        descCortaPaqueteInput.value = p.descripcionCorta || "";
        descPaqueteInput.value = p.descripcion || "";

        diasPaqueteInput.value = p.dias !== undefined ? p.dias : 1;
        horasPaqueteInput.value = p.horas !== undefined ? p.horas : 4;
        itinerarioPaqueteInput.value = p.itinerario || "";

        precioAdultoInput.value = p.precioAdulto || p.precio || "";

        aceptaNinosCheckbox.checked = Boolean(p.aceptaNinos);
        contenedorNinos.style.display = p.aceptaNinos ? "flex" : "none";
        precioNinoInput.value = p.precioNino || "";
        edadMaxNinoInput.value = p.edadMaxNino || 11;

        aceptaBebesCheckbox.checked = Boolean(p.aceptaBebes);
        contenedorBebes.style.display = p.aceptaBebes ? "flex" : "none";
        precioBebeInput.value = p.precioBebe !== undefined ? p.precioBebe : "0.00";
        edadMaxBebeInput.value = p.edadMaxBebe || 2;

        queIncluyeInput.value = p.queIncluye || "";
        queNoIncluyeInput.value = p.queNoIncluye || "";
        condicionesPaqueteInput.value = p.condiciones || "";
        descuentoPaqueteInput.value = p.descuento || 0;

        const imgPort = p.imagenPrincipal || p.imagen || "img/machupicchu.png";
        imagenPaqueteInput.value = imgPort;
        if (imgPreviewPortada) imgPreviewPortada.src = normalizarUrlImagen(imgPort);

        const gal = Array.isArray(p.galeria) ? p.galeria : [];
        if (imgGaleria1) {
            imgGaleria1.value = gal[0] || "";
            if (imgPreviewGaleria1) imgPreviewGaleria1.src = gal[0] ? normalizarUrlImagen(gal[0]) : "../../img/cusco.jpg";
        }
        if (imgGaleria2) {
            imgGaleria2.value = gal[1] || "";
            if (imgPreviewGaleria2) imgPreviewGaleria2.src = gal[1] ? normalizarUrlImagen(gal[1]) : "../../img/arequipa.jpg";
        }

        estadoPaqueteSelect.value = String(p.estado || "PUBLICADO").toUpperCase();

        // Cargar salidas existentes
        salidasEnFormulario = Array.isArray(p.salidas) ? JSON.parse(JSON.stringify(p.salidas)) : [];
        renderizarTablaSalidas();

        abrirModal(modalPaquete);
    };

    // 10. MODAL DETALLES DEL PAQUETE
    window.abrirDetallesPaquete = function (id) {
        const p = todosLosPaquetes.find(x => String(x.idTour || x.idPaquete) === String(id));
        if (!p) {
            alert("No se encontró el paquete.");
            return;
        }

        const modalDet = document.getElementById("modalDetallesPaquete");
        if (!modalDet) return;

        document.getElementById("detNombrePaquete").textContent = p.nombre || "-";
        const detEst = document.getElementById("detEstadoPaquete");
        const estStr = String(p.estado || "PUBLICADO").toUpperCase();
        detEst.textContent = estStr;
        detEst.className = `paquete-status-badge ${estStr.toLowerCase()}`;

        document.getElementById("detDestinoPaquete").textContent = `${p.destino || p.ubicacion || 'Perú'} • ${p.categoria || 'Cultural'}`;

        // Galería
        const galWrap = document.getElementById("detGaleriaWrap");
        if (galWrap) {
            const fotos = [];
            if (p.imagenPrincipal || p.imagen) fotos.push(p.imagenPrincipal || p.imagen);
            if (Array.isArray(p.galeria)) {
                p.galeria.forEach(g => { if (g && !fotos.includes(g)) fotos.push(g); });
            }
            galWrap.innerHTML = fotos.map(f => `
                <img loading="lazy" src="${normalizarUrlImagen(f)}" style="width:120px; height:80px; object-fit:cover; border-radius:8px; border:1px solid #cbd5e1; flex-shrink:0;" onerror="this.onerror=null; this.src='../../img/machupicchu.png';">
            `).join("");
        }

        document.getElementById("detDescCorta").textContent = p.descripcionCorta || "Sin frase destacada";
        document.getElementById("detDescPaquete").textContent = p.descripcion || "Sin descripción detallada.";

        document.getElementById("detPrecioAdulto").textContent = "S/ " + Number(p.precioAdulto || p.precio || 0).toFixed(2);
        document.getElementById("detPrecioNino").textContent = p.aceptaNinos
            ? `S/ ${Number(p.precioNino || 0).toFixed(2)} (hasta ${p.edadMaxNino || 11}a)`
            : "No acepta niños";
        document.getElementById("detPrecioBebe").textContent = p.aceptaBebes
            ? `${Number(p.precioBebe || 0) > 0 ? 'S/ ' + Number(p.precioBebe).toFixed(2) : 'Gratis'} (hasta ${p.edadMaxBebe || 2}a)`
            : "No acepta bebés";

        document.getElementById("detDuracionPaquete").textContent = p.duracion || "Personalizado";
        document.getElementById("detDescuentoPaquete").textContent = Number(p.descuento) > 0 ? `${p.descuento}% de descuento` : "Sin descuento";

        // Itinerario
        const boxItin = document.getElementById("detBoxItinerario");
        const detItin = document.getElementById("detItinerario");
        if (p.itinerario && p.itinerario.trim() !== "") {
            boxItin.style.display = "block";
            detItin.textContent = p.itinerario;
        } else {
            boxItin.style.display = "none";
        }

        // Qué incluye
        const boxInc = document.getElementById("detBoxIncluye");
        const listInc = document.getElementById("detListaIncluye");
        if (p.queIncluye && p.queIncluye.trim() !== "") {
            boxInc.style.display = "block";
            const items = p.queIncluye.split("\n").filter(x => x.trim() !== "");
            listInc.innerHTML = items.map(it => `<li>${escapeHtml(it.trim())}</li>`).join("");
        } else {
            boxInc.style.display = "none";
        }

        // Qué no incluye
        const boxNoInc = document.getElementById("detBoxNoIncluye");
        const listNoInc = document.getElementById("detListaNoIncluye");
        if (p.queNoIncluye && p.queNoIncluye.trim() !== "") {
            boxNoInc.style.display = "block";
            const items = p.queNoIncluye.split("\n").filter(x => x.trim() !== "");
            listNoInc.innerHTML = items.map(it => `<li>${escapeHtml(it.trim())}</li>`).join("");
        } else {
            boxNoInc.style.display = "none";
        }

        // Tabla Salidas en Detalles
        const tbodyDet = document.getElementById("detTbodySalidas");
        if (tbodyDet) {
            const salidas = Array.isArray(p.salidas) ? p.salidas : [];
            if (salidas.length === 0) {
                tbodyDet.innerHTML = `<tr><td colspan="5" style="text-align:center; color:#94a3b8; padding:12px;">Sin salidas programadas</td></tr>`;
            } else {
                tbodyDet.innerHTML = salidas.map(s => `
                    <tr>
                        <td><strong>${formatearFecha(s.fecha)}</strong></td>
                        <td>${s.horaInicio || '08:00'}</td>
                        <td>${s.cupoTotal}</td>
                        <td style="color:#059669; font-weight:700;">${s.cupoDisponible !== undefined ? s.cupoDisponible : s.cupoTotal}</td>
                        <td><span style="font-size:11px; font-weight:700; color:${s.estado === 'PAUSADO' ? '#d97706' : '#15803d'};">${s.estado || 'DISPONIBLE'}</span></td>
                    </tr>
                `).join("");
            }
        }

        abrirModal(modalDet);
    };

    window.cerrarModalDetalles = function () {
        const modalDet = document.getElementById("modalDetallesPaquete");
        if (modalDet) cerrarModal(modalDet);
    };

    // 11. CAMBIAR ESTADO (PAUSAR / PUBLICAR)
    window.cambiarEstadoPaquete = async function (id, accion) {
        const nuevoEstado = accion === "pausar" ? "PAUSADO" : "PUBLICADO";

        try {
            const res = await apiFetch("/api/agencia/paquetes", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    action: accion,
                    idTour: id,
                    idPaquete: id
                })
            });
            const data = await res.json();
            if (data.status === "success") {
                if (window.Swal) {
                    Swal.fire({
                        icon: "success",
                        title: "Estado actualizado",
                        text: data.message || `Paquete ${nuevoEstado.toLowerCase()} correctamente.`,
                        timer: 1600,
                        showConfirmButton: false
                    });
                }
                await cargarPaquetes();
            } else {
                if (window.Swal) {
                    Swal.fire({
                        icon: "warning",
                        title: "No se puede publicar",
                        text: data.message || "Error al actualizar estado.",
                        confirmButtonColor: "#2563eb"
                    });
                } else {
                    alert(data.message || "Error al cambiar estado");
                }
            }
        } catch (e) {
            console.error("Error al cambiar estado:", e);
            alert("Error de red al actualizar estado del paquete.");
        }
    };

    // 12. ELIMINAR PAQUETE
    window.eliminarPaquete = async function (id, nombre) {
        if (!confirm(`¿Estás seguro de que deseas eliminar el paquete "${nombre}"?`)) return;

        try {
            const res = await apiFetch("/api/agencia/paquetes", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    action: "eliminar",
                    idTour: id,
                    idPaquete: id
                })
            });
            const data = await res.json();
            if (data.status === "success") {
                if (window.Swal) {
                    Swal.fire({
                        icon: "success",
                        title: "Eliminado",
                        text: data.message || "Paquete eliminado exitosamente.",
                        timer: 2000,
                        showConfirmButton: false
                    });
                } else {
                    alert(data.message || "Paquete eliminado exitosamente.");
                }
                await cargarPaquetes();
            } else {
                alert(data.message || "Error al eliminar paquete");
            }
        } catch (e) {
            console.error("Error al eliminar paquete:", e);
            alert("Error de red al eliminar paquete.");
        }
    };

    // Helpers
    function abrirModal(modalEl) {
        if (!modalEl) return;
        modalEl.style.display = "flex";
        modalEl.style.opacity = "1";
        modalEl.style.pointerEvents = "auto";
    }

    function cerrarModal(modalEl) {
        if (!modalEl) return;
        modalEl.style.display = "none";
        modalEl.style.opacity = "0";
        modalEl.style.pointerEvents = "none";
    }

    function normalizarUrlImagen(url) {
        if (!url || typeof url !== "string") return "../../img/machupicchu.png";
        const t = url.trim();
        if (t.startsWith("http://") || t.startsWith("https://") || t.startsWith("data:") || t.startsWith("/")) return t;
        if (t.startsWith("../")) return t;
        return `../../${t}`;
    }

    function formatearFecha(fStr) {
        if (!fStr) return "-";
        try {
            const [y, m, d] = fStr.split("-");
            return `${d}/${m}/${y}`;
        } catch {
            return fStr;
        }
    }

    function escapeHtml(str) {
        if (!str) return "";
        return String(str)
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
    }

})();

