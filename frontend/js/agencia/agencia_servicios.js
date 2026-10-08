/**
 * Travelink - Módulo Servicios Turísticos (Fase 3)
 */

(function () {
    let sesion = null;
    try {
        const raw = localStorage.getItem("agenciaSesion") || localStorage.getItem("agenciasesión") || localStorage.getItem("travelink_user");
        sesion = JSON.parse(raw || "{}");
    } catch (e) { }

    const idAgenciaActual = (sesion && sesion.idAgencia) ? Number(sesion.idAgencia) : 2;
    const nombreAgenciaActual = (sesion && (sesion.nombreComercial || sesion.nombreAgencia || sesion.nombre || sesion.nombreUsuario)) ? (sesion.nombreComercial || sesion.nombreAgencia || sesion.nombre || sesion.nombreUsuario) : "INKA TRAVEL";

    let todosLosServicios = [];
    let serviciosFiltrados = [];
    let paginaActual = 1;
    const ITEMS_POR_PAGINA = 5;

    let listaImagenesModal = []; // Array de { url, esPrincipal }
    let destinosCargados = [];

    // Elementos DOM
    const tablaServicios = document.getElementById("tablaServicios");
    const cantidadServicios = document.getElementById("cantidadServicios");
    const paginacionInfo = document.getElementById("paginacionInfo");
    const paginacionControles = document.getElementById("paginacionControles");

    const buscarServicio = document.getElementById("buscarServicio");
    const filtroTipo = document.getElementById("filtroTipo");
    const filtroEstado = document.getElementById("filtroEstado");

    const modalServicio = document.getElementById("modalServicio");
    const formServicio = document.getElementById("formServicio");
    const btnNuevoServicio = document.getElementById("btnNuevoServicio");
    const cerrarModalServicio = document.getElementById("cerrarModalServicio");
    const btnCancelarModal = document.getElementById("btnCancelarModal");
    const tituloModalServicio = document.getElementById("tituloModalServicio");

    const idServicioInput = document.getElementById("idServicio");
    const nombreServicioInput = document.getElementById("nombreServicio");
    const tipoServicioSelect = document.getElementById("tipoServicio");
    const destinoServicioSelect = document.getElementById("destinoServicio");
    const duracionValorInput = document.getElementById("duracionValor");
    const duracionUnidadSelect = document.getElementById("duracionUnidad");
    const estadoServicioSelect = document.getElementById("estadoServicio");
    const ubicacionServicioInput = document.getElementById("ubicacionServicio");
    const precioAdultoInput = document.getElementById("precioAdulto");
    const precioNinoInput = document.getElementById("precioNino");
    const precioBebeInput = document.getElementById("precioBebe");
    const previewComision = document.getElementById("previewComision");
    const previewNeto = document.getElementById("previewNeto");
    const descripcionServicioInput = document.getElementById("descripcionServicio");

    const duracionDiasInput = document.getElementById("duracionDias");
    const duracionHorasInput = document.getElementById("duracionHoras");
    const aceptaBebesCheckbox = document.getElementById("aceptaBebes");
    const queIncluyeInput = document.getElementById("queIncluye");
    const queNoIncluyeInput = document.getElementById("queNoIncluye");

    const dropzoneFotografia = document.getElementById("dropzoneFotografia");
    const inputFileImagen = document.getElementById("inputFileImagen");
    const gridPreviewImagenes = document.getElementById("gridPreviewImagenes");
    const contadorImagenes = document.getElementById("contadorImagenes");

    const mensajeServicio = document.getElementById("mensajeServicio");
    const errorNombreServicio = document.getElementById("errorNombreServicio");
    const errorPrecios = document.getElementById("errorPrecios");

    // ==========================================
    // INICIALIZACIÓN
    // ==========================================
    document.addEventListener("DOMContentLoaded", () => {
        setupHeaderYPerfil();
        cargarDestinos();
        cargarServicios();
        setupFiltros();
        setupModal();
        setupPreciosPreview();
        setupImagenesManager();
    });

    // ==========================================
    // CABECERA Y DROPDOWNS
    // ==========================================
    function setupHeaderYPerfil() {
        const topNombre = document.getElementById("nombreAgenciaTop");
        const dropNombre = document.getElementById("dropdownNombreAgencia");
        const avatarLetter = document.getElementById("agencyAvatarLetter");

        if (topNombre) topNombre.textContent = nombreAgenciaActual;
        if (dropNombre) dropNombre.textContent = nombreAgenciaActual;
        if (avatarLetter) avatarLetter.textContent = nombreAgenciaActual.charAt(0).toUpperCase();

        const btnNotif = document.getElementById("btnNotifAgencia");
        const dropNotif = document.getElementById("notifDropdownAgencia");
        const btnPerfil = document.getElementById("btnPerfilToggle");
        const dropPerfil = document.getElementById("perfilDropdownAgencia");

        if (btnNotif && dropNotif) {
            btnNotif.addEventListener("click", (e) => {
                e.stopPropagation();
                if (dropPerfil) dropPerfil.style.display = "none";
                dropNotif.style.display = dropNotif.style.display === "block" ? "none" : "block";
            });
        }

        if (btnPerfil && dropPerfil) {
            btnPerfil.addEventListener("click", (e) => {
                e.stopPropagation();
                if (dropNotif) dropNotif.style.display = "none";
                dropPerfil.style.display = dropPerfil.style.display === "block" ? "none" : "block";
            });
        }

        document.addEventListener("click", () => {
            if (dropNotif) dropNotif.style.display = "none";
            if (dropPerfil) dropPerfil.style.display = "none";
        });

        const btnLogout = document.getElementById("btnCerrarSesionDropdown");
        if (btnLogout) {
            btnLogout.addEventListener("click", (e) => {
                e.preventDefault();
                localStorage.removeItem("agenciaSesion");
                window.location.href = "http://localhost:8080/html/Agencia/R_agencia_login.html";
            });
        }
    }

    // ==========================================
    // CARGAR DESTINOS
    // ==========================================
    async function cargarDestinos() {
        try {
            const res = await fetch("http://localhost:8080/api/agencia/destinos");
            const data = await res.json();
            if (data.status === "success" && data.destinos) {
                destinosCargados = data.destinos;
                destinoServicioSelect.innerHTML = '<option value="">Seleccionar destino</option>';
                data.destinos.forEach(d => {
                    const opt = document.createElement("option");
                    opt.value = d.idDestino;
                    opt.textContent = d.nombre;
                    destinoServicioSelect.appendChild(opt);
                });
            }
        } catch (e) {
            console.error("Error al cargar destinos:", e);
        }
    }

    // ==========================================
    // CARGAR SERVICIOS DESDE EL BACKEND
    // ==========================================
    async function cargarServicios() {
        if (!tablaServicios) return;

        try {
            const res = await fetch(`http://localhost:8080/api/agencia/servicios?idAgencia=${idAgenciaActual}`);
            const data = await res.json();

            if (data.status === "success" && Array.isArray(data.servicios)) {
                todosLosServicios = data.servicios;
                aplicarFiltros();
                return;
            }
        } catch (e) {
            console.warn("Fallo al conectar con API de servicios:", e);
        }

        // Fallback optimizado
        todosLosServicios = [
            {
                idTour: 1,
                idServicio: 1,
                nombre: "City Tour Lima Colonial y Catacumbas",
                descripcion: "Recorrido guiado por la Plaza Mayor, Catedral de Lima, Convento de San Francisco y sus famosas Catacumbas subterráneas.",
                tipoServicio: "Tour",
                categoria: "Tour",
                idDestino: 1,
                destino: "Lima",
                precioAdulto: 80.00,
                precioNino: 50.00,
                precioBebe: 0.00,
                duracion: "5 horas",
                estado: "ACTIVO",
                imagenPrincipal: "../../img/lima-package.jpg",
                imagenes: [{ url: "../../img/lima-package.jpg", esPrincipal: true }]
            },
            {
                idTour: 2,
                idServicio: 2,
                nombre: "Expedición Bosque de Piedras de Huayllay",
                descripcion: "Excursión fascinante al santuario nacional de Huayllay en Pasco con caminata entre formaciones rocosas únicas.",
                tipoServicio: "Experiencia",
                categoria: "Experiencia",
                idDestino: 2,
                destino: "Pasco",
                precioAdulto: 150.00,
                precioNino: 100.00,
                precioBebe: 0.00,
                duracion: "1 día",
                estado: "ACTIVO",
                imagenPrincipal: "../../img/huayllay-package.jpg",
                imagenes: [{ url: "../../img/huayllay-package.jpg", esPrincipal: true }]
            },
            {
                idTour: 3,
                idServicio: 3,
                nombre: "Inmersión Selva Tambopata Madre de Dios",
                descripcion: "Aventura ecológica en la Reserva Nacional Tambopata con observación de fauna silvestre y paseo en canoa por el Lago Sandoval.",
                tipoServicio: "Tour",
                categoria: "Tour",
                idDestino: 6,
                destino: "Madre de Dios",
                precioAdulto: 450.00,
                precioNino: 350.00,
                precioBebe: 0.00,
                duracion: "3 días",
                estado: "ACTIVO",
                imagenPrincipal: "../../img/tambopata-package.jpg",
                imagenes: [{ url: "../../img/tambopata-package.jpg", esPrincipal: true }]
            },
            {
                idTour: 4,
                idServicio: 4,
                nombre: "Ruta Histórica y Fuentes Termales Tacna",
                descripcion: "Circuito histórico cultural por el Paseo Cívico, Arco Parabólico y relajación en los baños termales de Calientes.",
                tipoServicio: "Tour",
                categoria: "Tour",
                idDestino: 3,
                destino: "Tacna",
                precioAdulto: 90.00,
                precioNino: 60.00,
                precioBebe: 0.00,
                duracion: "6 horas",
                estado: "PAUSADO",
                imagenPrincipal: "../../img/tacna-package.jpg",
                imagenes: [{ url: "../../img/tacna-package.jpg", esPrincipal: true }]
            }
        ];
        aplicarFiltros();
    }

    // ==========================================
    // FILTROS Y BÚSQUEDA
    // ==========================================
    function setupFiltros() {
        buscarServicio.addEventListener("input", () => {
            paginaActual = 1;
            aplicarFiltros();
        });
        filtroTipo.addEventListener("change", () => {
            paginaActual = 1;
            aplicarFiltros();
        });
        filtroEstado.addEventListener("change", () => {
            paginaActual = 1;
            aplicarFiltros();
        });
    }

    function aplicarFiltros() {
        const query = buscarServicio.value.trim().toLowerCase();
        const tipoSel = filtroTipo.value;
        const estadoSel = filtroEstado.value;

        serviciosFiltrados = todosLosServicios.filter(s => {
            const coincideNombre = s.nombre.toLowerCase().includes(query);
            const coincideTipo = !tipoSel || s.tipoServicio === tipoSel || s.categoria === tipoSel;
            const coincideEstado = !estadoSel || s.estado === estadoSel;
            return coincideNombre && coincideTipo && coincideEstado;
        });

        cantidadServicios.textContent = `${serviciosFiltrados.length} servicio${serviciosFiltrados.length !== 1 ? "s" : ""}`;
        renderizarTabla();
    }

    // ==========================================
    // RENDERIZADO DE TABLA CON PAGINACIÓN (MÁX 5)
    // ==========================================
    function renderizarTabla() {
        tablaServicios.innerHTML = "";

        if (serviciosFiltrados.length === 0) {
            tablaServicios.innerHTML = `
                <tr>
                    <td colspan="8" style="text-align:center; padding:45px 15px; color:#94a3b8;">
                        <i class="ti ti-compass-off" style="font-size:32px; display:block; margin-bottom:8px; color:#cbd5e1;"></i>
                        No se encontraron servicios turísticos con los filtros seleccionados.
                    </td>
                </tr>
            `;
            paginacionInfo.textContent = "Mostrando 0 de 0 servicios";
            paginacionControles.innerHTML = "";
            return;
        }

        const totalPaginas = Math.ceil(serviciosFiltrados.length / ITEMS_POR_PAGINA);
        if (paginaActual > totalPaginas) paginaActual = totalPaginas;

        const inicio = (paginaActual - 1) * ITEMS_POR_PAGINA;
        const fin = Math.min(inicio + ITEMS_POR_PAGINA, serviciosFiltrados.length);
        const paginaItems = serviciosFiltrados.slice(inicio, fin);

        paginaItems.forEach(s => {
            const tr = document.createElement("tr");

            // Imagen principal
            const imgUrl = s.imagenPrincipal || (s.imagenes && s.imagenes.length > 0 ? s.imagenes[0].url : "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=200");

            // Badge de estado
            let badgeClass = "activo";
            let badgeText = "Activo";
            if (s.estado === "PAUSADO") {
                badgeClass = "pausado";
                badgeText = "Pausado";
            } else if (s.estado === "DESACTIVADO_POR_ADMIN") {
                badgeClass = "desactivado-admin";
                badgeText = "Desactivado por administrador";
            }

            // Botón Pausar / Activar
            let btnPausaHtml = "";
            if (s.estado === "DESACTIVADO_POR_ADMIN") {
                btnPausaHtml = `<button type="button" class="btn-act" disabled title="Desactivado por administración" style="opacity:0.4; cursor:not-allowed;"><i class="ti ti-lock"></i> Bloqueado</button>`;
            } else if (s.estado === "PAUSADO") {
                btnPausaHtml = `<button type="button" class="btn-act btn-act-activate" onclick="window.cambiarEstadoServicio(${s.idTour}, 'ACTIVO')"><i class="ti ti-player-play"></i> Activar</button>`;
            } else {
                btnPausaHtml = `<button type="button" class="btn-act btn-act-pause" onclick="window.cambiarEstadoServicio(${s.idTour}, 'PAUSADO')"><i class="ti ti-player-pause"></i> Pausar</button>`;
            }

            // Botón Editar
            const btnEditHtml = s.estado === "DESACTIVADO_POR_ADMIN"
                ? `<button type="button" class="btn-act" disabled style="opacity:0.4; cursor:not-allowed;"><i class="ti ti-pencil"></i> Editar</button>`
                : `<button type="button" class="btn-act btn-act-edit" onclick="window.abrirEditarServicio(${s.idTour})"><i class="ti ti-pencil"></i> Editar</button>`;

            // Botón Eliminar
            const btnDelHtml = `<button type="button" class="btn-act btn-act-del" onclick="window.eliminarServicio(${s.idTour})"><i class="ti ti-trash"></i> Eliminar</button>`;

            // Comisión 15%
            const comision15 = (Number(s.precioAdulto) * 0.15).toFixed(2);

            tr.innerHTML = `
                <td>
                    <div class="tour-cell">
                        <img src="${imgUrl}" alt="${s.nombre}" class="tour-thumb" onerror="this.onerror=null; this.src='../../img/lima.jpg';">
                        <div>
                            <div class="tour-info-name">${s.nombre}</div>
                            <div class="tour-info-sub">Código #${s.idTour}</div>
                        </div>
                    </div>
                </td>
                <td>
                    <span style="background:#f1f5f9; color:#334155; padding:3px 8px; border-radius:6px; font-size:12px; font-weight:600;">
                        ${s.tipoServicio || s.categoria}
                    </span>
                </td>
                <td>
                    <span style="display:flex; align-items:center; gap:4px; font-weight:500; color:#1e293b;">
                        <i class="ti ti-map-pin" style="color:#2563eb; font-size:15px;"></i> ${s.destino}
                    </span>
                </td>
                <td>
                    <div class="prices-container">
                        <div class="price-row"><span class="price-label">Adulto:</span><span class="price-val">S/ ${Number(s.precioAdulto).toFixed(2)}</span></div>
                        <div class="price-row"><span class="price-label">Niño:</span><span class="price-val">S/ ${Number(s.precioNino).toFixed(2)}</span></div>
                        <div class="price-row"><span class="price-label">Bebé:</span><span class="price-val">S/ ${Number(s.precioBebe).toFixed(2)}</span></div>
                    </div>
                </td>
                <td>
                    <span style="color:#475569; font-weight:500;">
                        <i class="ti ti-clock" style="font-size:14px; color:#64748b;"></i> ${s.duracion}
                    </span>
                </td>
                <td>
                    <div style="font-size:12px;">
                        <strong style="color:#0f172a;">S/ ${comision15}</strong><br>
                        <span style="color:#64748b; font-size:11px;">15% (Adulto)</span>
                    </div>
                </td>
                <td>
                    <span class="badge-status ${badgeClass}">${badgeText}</span>
                </td>
                <td>
                    <div class="action-btns">
                        ${btnEditHtml}
                        ${btnPausaHtml}
                        ${btnDelHtml}
                    </div>
                </td>
            `;

            tablaServicios.appendChild(tr);
        });

        // Actualizar paginador
        paginacionInfo.textContent = `Mostrando ${inicio + 1} a ${fin} de ${serviciosFiltrados.length} servicios`;
        renderizarPaginador(totalPaginas);
    }

    function renderizarPaginador(totalPaginas) {
        paginacionControles.innerHTML = "";

        const btnAnt = document.createElement("button");
        btnAnt.className = "page-btn";
        btnAnt.innerHTML = '<i class="ti ti-chevron-left"></i>';
        btnAnt.disabled = paginaActual === 1;
        btnAnt.onclick = () => {
            if (paginaActual > 1) {
                paginaActual--;
                renderizarTabla();
            }
        };
        paginacionControles.appendChild(btnAnt);

        for (let p = 1; p <= totalPaginas; p++) {
            const btnP = document.createElement("button");
            btnP.className = `page-btn ${p === paginaActual ? "active" : ""}`;
            btnP.textContent = p;
            btnP.onclick = () => {
                paginaActual = p;
                renderizarTabla();
            };
            paginacionControles.appendChild(btnP);
        }

        const btnSig = document.createElement("button");
        btnSig.className = "page-btn";
        btnSig.innerHTML = '<i class="ti ti-chevron-right"></i>';
        btnSig.disabled = paginaActual === totalPaginas;
        btnSig.onclick = () => {
            if (paginaActual < totalPaginas) {
                paginaActual++;
                renderizarTabla();
            }
        };
        paginacionControles.appendChild(btnSig);
    }

    function abrirModalHelper(modal) {
        if (!modal) return;
        modal.classList.add("active", "show");
        modal.style.display = "flex";
        modal.style.opacity = "1";
        modal.style.pointerEvents = "auto";
    }

    function cerrarModalHelper(modal) {
        if (!modal) return;
        modal.classList.remove("active", "show");
        modal.style.display = "none";
        modal.style.opacity = "0";
        modal.style.pointerEvents = "none";
    }

    function setupModal() {
        btnNuevoServicio.addEventListener("click", () => {
            abrirModalNuevo();
        });

        cerrarModalServicio.addEventListener("click", cerrarModal);
        btnCancelarModal.addEventListener("click", cerrarModal);

        window.addEventListener("click", (e) => {
            if (e.target === modalServicio) cerrarModal();
        });

        formServicio.addEventListener("submit", async (e) => {
            e.preventDefault();
            guardarServicio();
        });

        // Validación en tiempo real de nombre único
        nombreServicioInput.addEventListener("input", () => {
            const nombre = nombreServicioInput.value.trim().toLowerCase();
            const idActual = idServicioInput.value ? Number(idServicioInput.value) : 0;
            const existe = todosLosServicios.some(s => s.idTour !== idActual && s.nombre.toLowerCase() === nombre);
            if (existe) {
                errorNombreServicio.textContent = `Ya tienes un servicio llamado "${nombreServicioInput.value.trim()}". Debe ser único.`;
                errorNombreServicio.style.display = "block";
            } else {
                errorNombreServicio.style.display = "none";
            }
        });
    }

    function abrirModalNuevo() {
        formServicio.reset();
        idServicioInput.value = "";
        tituloModalServicio.textContent = "Nuevo servicio";
        errorNombreServicio.style.display = "none";
        errorPrecios.style.display = "none";
        mensajeServicio.style.display = "none";
        listaImagenesModal = [];
        actualizarPreviewImagenes();
        actualizarPreviewComision();
        abrirModalHelper(modalServicio);
    }

    window.abrirEditarServicio = function (idTour) {
        const s = todosLosServicios.find(x => x.idTour === idTour);
        if (!s) return;

        formServicio.reset();
        idServicioInput.value = s.idTour;
        tituloModalServicio.textContent = "Editar servicio";
        nombreServicioInput.value = s.nombre;
        tipoServicioSelect.value = s.tipoServicio || s.categoria;
        destinoServicioSelect.value = s.idDestino;
        estadoServicioSelect.value = s.estado;
        if (ubicacionServicioInput) ubicacionServicioInput.value = s.ubicacion || "";

        if (duracionDiasInput) duracionDiasInput.value = s.dias || "1";
        if (duracionHorasInput) duracionHorasInput.value = s.horas || "6";
        if (aceptaBebesCheckbox) aceptaBebesCheckbox.checked = s.aceptaBebes || false;
        if (queIncluyeInput) queIncluyeInput.value = s.queIncluye || "";
        if (queNoIncluyeInput) queNoIncluyeInput.value = s.queNoIncluye || "";

        // Parsear duración (e.g. "6 horas", "1 día")
        const durParts = (s.duracion || "").split(" ");
        if (durParts.length >= 2) {
            duracionValorInput.value = durParts[0];
            duracionUnidadSelect.value = durParts[1].toLowerCase().includes("día") ? "días" : "horas";
        } else {
            duracionValorInput.value = "1";
            duracionUnidadSelect.value = "horas";
        }

        precioAdultoInput.value = s.precioAdulto;
        precioNinoInput.value = s.precioNino;
        precioBebeInput.value = s.precioBebe;
        descripcionServicioInput.value = s.descripcion || "";

        // Imágenes
        listaImagenesModal = [];
        if (s.imagenes && s.imagenes.length > 0) {
            s.imagenes.forEach(img => {
                listaImagenesModal.push({
                    url: img.url,
                    esPrincipal: img.esPrincipal
                });
            });
        } else if (s.imagenPrincipal) {
            listaImagenesModal.push({ url: s.imagenPrincipal, esPrincipal: true });
        }

        actualizarPreviewImagenes();
        actualizarPreviewComision();
        errorNombreServicio.style.display = "none";
        errorPrecios.style.display = "none";
        mensajeServicio.style.display = "none";
        abrirModalHelper(modalServicio);
    };

    function cerrarModal() {
        cerrarModalHelper(modalServicio);
    }

    // ==========================================
    // GESTIÓN DE PRECIOS Y COMISIÓN 15%
    // ==========================================
    function setupPreciosPreview() {
        precioAdultoInput.addEventListener("input", () => {
            validarJerarquiaPrecios();
            actualizarPreviewComision();
        });
        precioNinoInput.addEventListener("input", validarJerarquiaPrecios);
        precioBebeInput.addEventListener("input", validarJerarquiaPrecios);
    }

    function validarJerarquiaPrecios() {
        const ad = parseFloat(precioAdultoInput.value) || 0;
        const ni = parseFloat(precioNinoInput.value) || 0;
        const be = parseFloat(precioBebeInput.value) || 0;

        if (ad <= 0 && precioAdultoInput.value !== "") {
            errorPrecios.textContent = "El precio de adulto debe ser mayor a 0.";
            errorPrecios.style.display = "block";
            return false;
        }

        if (ni > ad) {
            errorPrecios.textContent = "El precio de niño no puede ser mayor al precio de adulto.";
            errorPrecios.style.display = "block";
            return false;
        }

        if (be > ni) {
            errorPrecios.textContent = "El precio de bebé no puede ser mayor al precio de niño.";
            errorPrecios.style.display = "block";
            return false;
        }

        errorPrecios.style.display = "none";
        return true;
    }

    function actualizarPreviewComision() {
        const ad = parseFloat(precioAdultoInput.value) || 0;
        const comision = ad * 0.15;
        const neto = ad * 0.85;

        previewComision.textContent = `S/ ${comision.toFixed(2)}`;
        previewNeto.textContent = `S/ ${neto.toFixed(2)}`;
    }

    // ==========================================
    // GESTIÓN DE IMÁGENES (HASTA 5 CON DROPZONE)
    // ==========================================
    function setupImagenesManager() {
        const dropzone = document.getElementById("dropzoneFotografia");

        if (dropzone) {
            ['dragenter', 'dragover'].forEach(evt => {
                dropzone.addEventListener(evt, (e) => {
                    e.preventDefault();
                    e.stopPropagation();
                    dropzone.classList.add("dragover");
                });
            });
            ['dragleave', 'drop'].forEach(evt => {
                dropzone.addEventListener(evt, (e) => {
                    e.preventDefault();
                    e.stopPropagation();
                    dropzone.classList.remove("dragover");
                });
            });
            dropzone.addEventListener("drop", (e) => {
                const files = e.dataTransfer.files;
                procesarArchivosImagen(files);
            });
        }

        if (inputFileImagen) {
            inputFileImagen.addEventListener("change", (e) => {
                procesarArchivosImagen(e.target.files);
            });
        }
    }

    function procesarArchivosImagen(files) {
        if (!files || files.length === 0) return;

        Array.from(files).forEach(file => {
            if (listaImagenesModal.length >= 5) {
                alert("Solo puedes subir un máximo de 5 imágenes.");
                return;
            }
            if (file.size > 5 * 1024 * 1024) {
                alert(`El archivo ${file.name} excede el tamaño máximo permitido de 5 MB.`);
                return;
            }

            const reader = new FileReader();
            reader.onload = (event) => {
                const base64Url = event.target.result;
                const esPrincipal = listaImagenesModal.length === 0;
                listaImagenesModal.push({ url: base64Url, esPrincipal: esPrincipal });
                actualizarPreviewImagenes();
            };
            reader.readAsDataURL(file);
        });

        if (inputFileImagen) inputFileImagen.value = "";
    }

    function actualizarPreviewImagenes() {
        if (contadorImagenes) contadorImagenes.textContent = `${listaImagenesModal.length} / 5 imágenes`;
        if (!gridPreviewImagenes) return;
        gridPreviewImagenes.innerHTML = "";

        listaImagenesModal.forEach((item, index) => {
            const div = document.createElement("div");
            div.style.cssText = `position:relative; width:95px; height:95px; border-radius:10px; overflow:hidden; border:2px solid ${item.esPrincipal ? '#2563eb' : '#cbd5e1'}; background:#f8fafc; box-shadow:0 2px 6px rgba(0,0,0,0.06);`;

            div.innerHTML = `
                <img src="${item.url}" alt="Foto ${index + 1}" style="width:100%; height:100%; object-fit:cover;">
                <span style="position:absolute; top:4px; left:4px; font-size:10px; font-weight:700; background:${item.esPrincipal ? '#2563eb' : 'rgba(15,23,42,0.75)'}; color:white; padding:2px 6px; border-radius:4px;">
                    ${item.esPrincipal ? 'Principal' : '#' + (index + 1)}
                </span>
                <div style="position:absolute; bottom:4px; right:4px; display:flex; gap:4px; background:rgba(255,255,255,0.92); padding:3px; border-radius:6px; box-shadow:0 1px 3px rgba(0,0,0,0.2);">
                    ${!item.esPrincipal ? `<button type="button" title="Establecer como principal" onclick="window.hacerPrincipalImagen(${index})" style="background:none; border:none; cursor:pointer; color:#d97706; padding:1px;"><i class="ti ti-star-filled" style="font-size:14px;"></i></button>` : ''}
                    <button type="button" title="Eliminar foto" onclick="window.quitarImagen(${index})" style="background:none; border:none; cursor:pointer; color:#ef4444; padding:1px;"><i class="ti ti-trash" style="font-size:14px;"></i></button>
                </div>
            `;
            gridPreviewImagenes.appendChild(div);
        });
    }

    window.hacerPrincipalImagen = function (index) {
        listaImagenesModal.forEach((img, i) => {
            img.esPrincipal = (i === index);
        });
        actualizarPreviewImagenes();
    };

    window.quitarImagen = function (index) {
        const eraPrincipal = listaImagenesModal[index].esPrincipal;
        listaImagenesModal.splice(index, 1);
        if (eraPrincipal && listaImagenesModal.length > 0) {
            listaImagenesModal[0].esPrincipal = true;
        }
        actualizarPreviewImagenes();
    };

    // ==========================================
    // GUARDAR SERVICIO (POST)
    // ==========================================
    async function guardarServicio() {
        if (!validarJerarquiaPrecios()) return;

        const nombre = nombreServicioInput.value.trim();
        const idActual = idServicioInput.value ? Number(idServicioInput.value) : 0;

        // Validar nombre único
        const existe = todosLosServicios.some(s => s.idTour !== idActual && s.nombre.toLowerCase() === nombre.toLowerCase());
        if (existe) {
            errorNombreServicio.textContent = `Ya tienes un servicio llamado "${nombre}". Debe ser único.`;
            errorNombreServicio.style.display = "block";
            return;
        }

        const duracion = `${duracionValorInput.value.trim()} ${duracionUnidadSelect.value}`;

        // Preparar URLs de imágenes
        const principalObj = listaImagenesModal.find(x => x.esPrincipal) || (listaImagenesModal.length > 0 ? listaImagenesModal[0] : null);
        const imagenPrincipal = principalObj ? principalObj.url : "";
        const urlsRestantes = listaImagenesModal.map(x => x.url).join(",");

        const payload = {
            idAgencia: idAgenciaActual,
            idServicio: idActual,
            idTour: idActual,
            nombre: nombre,
            tipoServicio: tipoServicioSelect.value,
            idDestino: destinoServicioSelect.value,
            duracion: duracion,
            estado: estadoServicioSelect.value,
            ubicacion: ubicacionServicioInput ? ubicacionServicioInput.value.trim() : "",
            precioAdulto: precioAdultoInput.value,
            precioNino: precioNinoInput.value || "0",
            precioBebe: precioBebeInput.value || "0",
            descripcion: descripcionServicioInput.value.trim(),
            dias: duracionDiasInput ? duracionDiasInput.value : "1",
            horas: duracionHorasInput ? duracionHorasInput.value : "6",
            aceptaBebes: aceptaBebesCheckbox ? aceptaBebesCheckbox.checked : false,
            queIncluye: queIncluyeInput ? queIncluyeInput.value.trim() : "",
            queNoIncluye: queNoIncluyeInput ? queNoIncluyeInput.value.trim() : "",
            imagenPrincipal: imagenPrincipal,
            imagenes: urlsRestantes
        };

        mensajeServicio.style.display = "block";
        mensajeServicio.style.background = "#eff6ff";
        mensajeServicio.style.color = "#1d4ed8";
        mensajeServicio.textContent = "Guardando servicio turístico...";

        try {
            const res = await fetch("http://localhost:8080/api/agencia/servicios", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(payload)
            });

            const data = await res.json();
            if (data.status === "success") {
                mensajeServicio.style.background = "#dcfce7";
                mensajeServicio.style.color = "#166534";
                mensajeServicio.textContent = "¡Servicio guardado exitosamente!";
                setTimeout(() => {
                    cerrarModal();
                    cargarServicios();
                }, 900);
            } else {
                mensajeServicio.style.background = "#fee2e2";
                mensajeServicio.style.color = "#991b1b";
                mensajeServicio.textContent = data.message || "No se pudo guardar el servicio.";
            }
        } catch (e) {
            console.error("Error al guardar servicio:", e);
            mensajeServicio.style.background = "#fee2e2";
            mensajeServicio.style.color = "#991b1b";
            mensajeServicio.textContent = "Error de conexión con el servidor.";
        }
    }

    // ==========================================
    // CAMBIAR ESTADO (PAUSAR / ACTIVAR)
    // ==========================================
    window.cambiarEstadoServicio = async function (idTour, nuevoEstado) {
        const accionTexto = nuevoEstado === "PAUSADO" ? "pausar" : "activar";
        if (!confirm(`¿Estás seguro de que deseas ${accionTexto} este servicio?`)) return;

        try {
            const res = await fetch("http://localhost:8080/api/agencia/servicios", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    action: "cambiar_estado",
                    idTour: idTour,
                    idAgencia: idAgenciaActual,
                    nuevoEstado: nuevoEstado
                })
            });

            const data = await res.json();
            if (data.status === "success") {
                alert(`Servicio ${nuevoEstado === "PAUSADO" ? "pausado" : "activado"} correctamente.`);
                cargarServicios();
            } else {
                alert(data.message || `No se pudo ${accionTexto} el servicio.`);
            }
        } catch (e) {
            console.error("Error al cambiar estado:", e);
            alert("Error al conectar con el servidor.");
        }
    };

    // ==========================================
    // ELIMINAR SERVICIO (VALIDACIÓN DE HISTORIAL)
    // ==========================================
    window.eliminarServicio = async function (idTour) {
        const s = todosLosServicios.find(x => x.idTour === idTour);
        const tourName = s ? s.nombre : "este servicio";

        if (!confirm(`¿Estás seguro de que deseas eliminar "${tourName}"?\n\nNota: Si el servicio ya tuvo reservas registradas, por integridad histórica del sistema solo será pausado.`)) {
            return;
        }

        try {
            const res = await fetch("http://localhost:8080/api/agencia/servicios", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    action: "eliminar",
                    idTour: idTour,
                    idAgencia: idAgenciaActual
                })
            });

            const data = await res.json();
            if (data.status === "success") {
                alert("Servicio eliminado exitosamente.");
                cargarServicios();
            } else if (data.canPause) {
                alert(`Aviso: ${data.message}`);
                cargarServicios();
            } else {
                alert(data.message || "No se pudo eliminar el servicio.");
            }
        } catch (e) {
            console.error("Error al eliminar servicio:", e);
            alert("Error de conexión al eliminar el servicio.");
        }
    };

    // ==========================================
    // GESTIÓN DE TIPOS (INSERTAR, EDITAR, ELIMINAR)
    // ==========================================
    const modalTipos = document.getElementById("modalTipos");
    const btnAdminTipos = document.getElementById("btnAdminTipos");
    const cerrarModalTipos = document.getElementById("cerrarModalTipos");
    const formTipo = document.getElementById("formTipo");
    const idTipoInput = document.getElementById("idTipoInput");
    const nombreTipoInput = document.getElementById("nombreTipoInput");
    const descTipoInput = document.getElementById("descTipoInput");
    const btnGuardarTipo = document.getElementById("btnGuardarTipo");
    const btnCancelarEditarTipo = document.getElementById("btnCancelarEditarTipo");
    const tablaTiposBody = document.getElementById("tablaTiposBody");
    const mensajeTipo = document.getElementById("mensajeTipo");

    let tiposDisponibles = [];

    function setupGestionTipos() {
        if (btnAdminTipos && modalTipos) {
            btnAdminTipos.addEventListener("click", () => {
                modalTipos.style.display = "flex";
                resetearFormTipo();
                cargarListaTipos();
            });
        }

        if (cerrarModalTipos && modalTipos) {
            cerrarModalTipos.addEventListener("click", () => {
                modalTipos.style.display = "none";
            });
        }

        if (btnCancelarEditarTipo) {
            btnCancelarEditarTipo.addEventListener("click", resetearFormTipo);
        }

        if (formTipo) {
            formTipo.addEventListener("submit", async (e) => {
                e.preventDefault();
                const idTipo = idTipoInput.value;
                const nombre = nombreTipoInput.value.trim();
                const desc = descTipoInput.value.trim();

                if (!nombre) return;

                const payload = {
                    action: idTipo ? "editar" : "crear",
                    idTipo: idTipo,
                    nombre: nombre,
                    descripcion: desc
                };

                try {
                    const res = await fetch("http://localhost:8080/api/agencia/tipos", {
                        method: "POST",
                        headers: { "Content-Type": "application/json" },
                        body: JSON.stringify(payload)
                    });
                    const data = await res.json();
                    if (data.status === "success") {
                        mostrarMensajeTipo("Tipo guardado exitosamente.", "exito");
                        resetearFormTipo();
                        cargarListaTipos();
                        cargarTiposDropdown();
                    } else {
                        mostrarMensajeTipo(data.message || "Error al guardar tipo.", "error");
                    }
                } catch (err) {
                    mostrarMensajeTipo("Error de conexión al guardar tipo.", "error");
                }
            });
        }

        cargarTiposDropdown();
    }

    function resetearFormTipo() {
        if (idTipoInput) idTipoInput.value = "";
        if (nombreTipoInput) nombreTipoInput.value = "";
        if (descTipoInput) descTipoInput.value = "";
        if (btnGuardarTipo) btnGuardarTipo.textContent = "Guardar";
        if (btnCancelarEditarTipo) btnCancelarEditarTipo.style.display = "none";
    }

    function mostrarMensajeTipo(texto, tipo) {
        if (!mensajeTipo) return;
        mensajeTipo.textContent = texto;
        mensajeTipo.style.display = "block";
        if (tipo === "exito") {
            mensajeTipo.style.background = "#dcfce7";
            mensajeTipo.style.color = "#15803d";
            mensajeTipo.style.border = "1px solid #86efac";
        } else {
            mensajeTipo.style.background = "#fee2e2";
            mensajeTipo.style.color = "#b91c1c";
            mensajeTipo.style.border = "1px solid #fca5a5";
        }
        setTimeout(() => { if (mensajeTipo) mensajeTipo.style.display = "none"; }, 3500);
    }

    async function cargarListaTipos() {
        if (!tablaTiposBody) return;
        tablaTiposBody.innerHTML = `<tr><td colspan="5" style="text-align:center; padding:20px; color:#94a3b8;">Cargando tipos...</td></tr>`;

        try {
            const res = await fetch("http://localhost:8080/api/agencia/tipos");
            const data = await res.json();
            if (data.status === "success" && data.tipos) {
                tiposDisponibles = data.tipos;
                renderizarTablaTipos();
            }
        } catch (e) {
            tablaTiposBody.innerHTML = `<tr><td colspan="5" style="text-align:center; padding:20px; color:#ef4444;">Error al cargar tipos.</td></tr>`;
        }
    }

    function renderizarTablaTipos() {
        if (!tablaTiposBody) return;
        if (tiposDisponibles.length === 0) {
            tablaTiposBody.innerHTML = `<tr><td colspan="5" style="text-align:center; padding:20px; color:#94a3b8;">No hay tipos registrados.</td></tr>`;
            return;
        }

        tablaTiposBody.innerHTML = tiposDisponibles.map((t, i) => `
            <tr>
                <td style="padding:10px 12px; border-bottom:1px solid #f1f5f9; color:#64748b;">${i + 1}</td>
                <td style="padding:10px 12px; border-bottom:1px solid #f1f5f9; font-weight:600; color:#0f172a;">${t.nombre}</td>
                <td style="padding:10px 12px; border-bottom:1px solid #f1f5f9; color:#475569; font-size:12px;">${t.descripcion || 'â€”'}</td>
                <td style="padding:10px 12px; border-bottom:1px solid #f1f5f9;">
                    <span style="background:#eff6ff; color:#2563eb; font-size:11px; font-weight:600; padding:2px 8px; border-radius:999px;">${t.estado}</span>
                </td>
                <td style="padding:10px 12px; border-bottom:1px solid #f1f5f9; text-align:right;">
                    <button type="button" onclick="window.editarTipo(${t.idTipo}, '${t.nombre}', '${(t.descripcion || '').replace(/'/g, "\\'")}')" style="background:#f1f5f9; border:none; padding:4px 8px; border-radius:4px; font-size:12px; cursor:pointer; margin-right:4px;">
                        <i class="ti ti-pencil"></i> Editar
                    </button>
                    <button type="button" onclick="window.eliminarTipo(${t.idTipo}, '${t.nombre}')" style="background:#fef2f2; color:#ef4444; border:none; padding:4px 8px; border-radius:4px; font-size:12px; cursor:pointer;">
                        <i class="ti ti-trash"></i> Eliminar
                    </button>
                </td>
            </tr>
        `).join("");
    }

    window.editarTipo = function (id, nombre, desc) {
        if (idTipoInput) idTipoInput.value = id;
        if (nombreTipoInput) nombreTipoInput.value = nombre;
        if (descTipoInput) descTipoInput.value = desc;
        if (btnGuardarTipo) btnGuardarTipo.textContent = "Actualizar";
        if (btnCancelarEditarTipo) btnCancelarEditarTipo.style.display = "inline-block";
        nombreTipoInput.focus();
    };

    window.eliminarTipo = async function (id, nombre) {
        if (!confirm(`¿Eliminar el tipo "${nombre}"?`)) return;
        try {
            const res = await fetch("http://localhost:8080/api/agencia/tipos", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ action: "eliminar", idTipo: id })
            });
            const data = await res.json();
            if (data.status === "success") {
                mostrarMensajeTipo("Tipo eliminado.", "exito");
                cargarListaTipos();
                cargarTiposDropdown();
            } else {
                alert(data.message || "No se pudo eliminar.");
            }
        } catch (e) {
            alert("Error al conectar con el servidor.");
        }
    };

    async function cargarTiposDropdown() {
        try {
            const res = await fetch("http://localhost:8080/api/agencia/tipos");
            const data = await res.json();
            if (data.status === "success" && data.tipos) {
                tiposDisponibles = data.tipos;

                if (tipoServicioSelect) {
                    const val = tipoServicioSelect.value;
                    tipoServicioSelect.innerHTML = `<option value="">Seleccionar tipo</option>` +
                        data.tipos.map(t => `<option value="${t.nombre}">${t.nombre}</option>`).join("");
                    if (val) tipoServicioSelect.value = val;
                }

                if (filtroTipo) {
                    const valF = filtroTipo.value;
                    filtroTipo.innerHTML = `<option value="">Todos los tipos</option>` +
                        data.tipos.map(t => `<option value="${t.nombre}">${t.nombre}</option>`).join("");
                    if (valF) filtroTipo.value = valF;
                }
            }
        } catch (e) {
            console.error("Error cargando tipos dropdown:", e);
        }
    }

    // Registrar en inicialización
    document.addEventListener("DOMContentLoaded", () => {
        setupGestionTipos();
    });

})();
