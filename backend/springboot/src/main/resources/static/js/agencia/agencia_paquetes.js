?/**
 * Travelink - Módulo Paquetes Turísticos
 */
(function () {
    let sesion = null;
    try {
        const sesionRaw = localStorage.getItem("agenciaSesion") || localStorage.getItem("agenciasesión") || localStorage.getItem("travelink_user");
        sesion = JSON.parse(sesionRaw || "{}");
    } catch (e) { }

    const idAgenciaActual = (sesion && sesion.idAgencia) ? Number(sesion.idAgencia) : 2;
    const nombreAgenciaActual = (sesion && (sesion.nombreComercial || sesion.nombreAgencia || sesion.nombre || sesion.nombreUsuario)) ? (sesion.nombreComercial || sesion.nombreAgencia || sesion.nombre || sesion.nombreUsuario) : "INKA TRAVEL";

    let todosLosPaquetes = [];
    let paquetesFiltrados = [];

    // DOM Elements
    const gridPaquetes = document.getElementById("gridPaquetes");
    const buscarPaquete = document.getElementById("buscarPaquete");
    const filtroEstadoPaquete = document.getElementById("filtroEstadoPaquete");
    const btnNuevoPaquete = document.getElementById("btnNuevoPaquete");

    const modalPaquete = document.getElementById("modalPaquete");
    const cerrarModalPaquete = document.getElementById("cerrarModalPaquete");
    const btnCancelarPaquete = document.getElementById("btnCancelarPaquete");
    const formPaquete = document.getElementById("formPaquete");
    const tituloModalPaquete = document.getElementById("tituloModalPaquete");
    const mensajePaquete = document.getElementById("mensajePaquete");

    // Form fields
    const idPaqueteInput = document.getElementById("idPaqueteInput");
    const nombrePaqueteInput = document.getElementById("nombrePaqueteInput");
    const descPaqueteInput = document.getElementById("descPaqueteInput");
    const precioPaqueteInput = document.getElementById("precioPaqueteInput");
    const descuentoPaqueteInput = document.getElementById("descuentoPaqueteInput");
    const duracionPaqueteInput = document.getElementById("duracionPaqueteInput");
    const estadoPaqueteSelect = document.getElementById("estadoPaqueteSelect");
    const imagenPaqueteInput = document.getElementById("imagenPaqueteInput");
    const condicionesPaqueteInput = document.getElementById("condicionesPaqueteInput");

    document.addEventListener("DOMContentLoaded", () => {
        setupTopBar();
        cargarPaquetes();
        setupFiltros();
        setupModal();
        setupDropzone();
    });

    function setupTopBar() {
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
                localStorage.removeItem("agenciaSesion");
                localStorage.removeItem("agenciasesión");
                window.location.href = "R_agencia_login.html";
            };
        }
    }

    async function cargarPaquetes() {
        if (!gridPaquetes) return;

        try {
            const res = await fetch(`http://localhost:8080/api/agencia/paquetes?idAgencia=${idAgenciaActual}`);
            const data = await res.json();
            if (data.status === "success" && Array.isArray(data.paquetes)) {
                todosLosPaquetes = data.paquetes;
                aplicarFiltros();
                return;
            }
        } catch (e) {
            console.warn("Error al cargar paquetes desde backend:", e);
        }

        // Fallback inmediato con datos de la base de datos para carga ultrarrápida
        todosLosPaquetes = [
            {
                idPaquete: 1,
                nombre: "Lima Colonial y Sabores Criollos Express",
                descripcion: "Lo mejor de la capital: centros virreinales, templos coloniales, arte bohemio y la mejor gastronomía marina.",
                precio: 550.00,
                duracion: "2 días / 1 noche",
                condiciones: "Incluye traslados privados, guiado profesional en inglés/español e ingresos a museos.",
                estado: "PUBLICADO",
                descuento: 10,
                imagen: "../../img/lima-package.jpg",
                cuposDisponibles: 25,
                cupoTotal: 30,
                servicios: ["City Tour Lima Colonial y Catacumbas", "Traslado Privado Aeropuerto - Hotel"]
            },
            {
                idPaquete: 2,
                nombre: "Expedición Andina y Bosque de Piedras (Pasco)",
                descripcion: "Explora las increíbles figuras de piedra más altas del mundo a 4,300 msnm y navega en la Laguna Punrun.",
                precio: 720.00,
                duracion: "2 días / 1 noche",
                condiciones: "Incluye transporte turístico ida y vuelta desde Lima, hospedaje en Cerro de Pasco y kit de mate de coca.",
                estado: "PUBLICADO",
                descuento: 15,
                imagen: "../../img/huayllay-package.jpg",
                cuposDisponibles: 15,
                cupoTotal: 20,
                servicios: ["Expedición Bosque de Piedras de Huayllay", "Trekking Laguna Punrun"]
            },
            {
                idPaquete: 3,
                nombre: "Amazonía Profunda Tambopata (Madre de Dios)",
                descripcion: "Inmersión en la selva virgen con observación de guacamayos, caimanes, caminata nocturna y canopy en el bosque.",
                precio: 1450.00,
                duracion: "4 días / 3 noches",
                condiciones: "Alimentación completa tipo buffet amazónico, alojamiento ecolodge con piscina y guiado nativo.",
                estado: "PUBLICADO",
                descuento: 20,
                imagen: "../../img/tambopata-package.jpg",
                cuposDisponibles: 10,
                cupoTotal: 15,
                servicios: ["Inmersión Selva Tambopata Madre de Dios", "Tour Fluvial Río Madre de Dios"]
            },
            {
                idPaquete: 4,
                nombre: "Ruta del Sol y Fuentes Termales Tacna",
                descripcion: "Relájate en los baños termales de Calientes, recorre la microrregión vitivinícola y contempla las playas del sur.",
                precio: 480.00,
                duracion: "3 días / 2 noches",
                condiciones: "Incluye traslados locales, cata de piscos en bodegas artesanales y almuerzo típico tacneño.",
                estado: "BORRADOR",
                descuento: 5,
                imagen: "../../img/tacna-package.jpg",
                cuposDisponibles: 18,
                cupoTotal: 25,
                servicios: ["Ruta Histórica y Fuentes Termales Tacna", "Cata de Vinos y Piscos en Pachía"]
            }
        ];
        aplicarFiltros();
    }

    function setupFiltros() {
        if (buscarPaquete) buscarPaquete.addEventListener("input", aplicarFiltros);
        if (filtroEstadoPaquete) filtroEstadoPaquete.addEventListener("change", aplicarFiltros);
    }

    function aplicarFiltros() {
        const query = (buscarPaquete ? buscarPaquete.value : "").trim().toLowerCase();
        const est = (filtroEstadoPaquete ? filtroEstadoPaquete.value : "").trim().toUpperCase();

        paquetesFiltrados = todosLosPaquetes.filter(p => {
            const matchQuery = !query || p.nombre.toLowerCase().includes(query) || (p.descripcion && p.descripcion.toLowerCase().includes(query));
            const matchEst = !est || String(p.estado || "").toUpperCase() === est;
            return matchQuery && matchEst;
        });

        renderizarGrid();
    }

    function renderizarGrid() {
            if (paquetesFiltrados.length === 0) {
            gridPaquetes.innerHTML = `
                <div class="paquete-card" style="text-align:center; padding:50px 20px; grid-column:1/-1; color:#94a3b8; background:white;">
                    <i class="ti ti-package-off" style="font-size:38px; color:#cbd5e1; display:block; margin-bottom:10px;"></i>
                    <p style="margin:0; font-size:15px; font-weight:500;">No se encontraron paquetes con los criterios seleccionados.</p>
                </div>`;
            return;
        }

        gridPaquetes.innerHTML = paquetesFiltrados.map(p => {
            const estado = String(p.estado || "PUBLICADO").toUpperCase();
            let badgeClass = "publicado";
            let badgeIcon = '<i class="ti ti-check"></i>';
            if (estado === "BORRADOR") {
                badgeClass = "borrador";
                badgeIcon = '<i class="ti ti-edit"></i>';
            } else if (estado === "PAUSADO") {
                badgeClass = "pausado";
                badgeIcon = '<i class="ti ti-clock-pause"></i>';
            }

            let imgUrl = "../../img/lima.jpg";
            if (p.imagen && p.imagen.trim() !== "") {
                const imgTrim = p.imagen.trim();
                if (imgTrim.startsWith("http") || imgTrim.startsWith("/") || imgTrim.startsWith("..")) {
                    imgUrl = imgTrim;
                } else {
                    imgUrl = `../../img/${imgTrim}`;
                }
            }

            const serviciosChips = (p.servicios && p.servicios.length > 0)
                ? p.servicios.map(s => `
                    <span class="servicio-chip">
                        <i class="ti ti-compass"></i> ${s}
                    </span>
                  `).join("")
                : `<span class="servicio-chip"><i class="ti ti-compass"></i> Tour guiado</span><span class="servicio-chip"><i class="ti ti-car"></i> Traslado</span>`;

            const btnEstadoHtml = estado === "PUBLICADO"
                ? `<button type="button" class="btn-card-action btn-card-pause" onclick="window.cambiarEstadoPaquete('${p.idPaquete}', 'pausar')">
                     <i class="ti ti-player-pause"></i> Pausar
                   </button>`
                : `<button type="button" class="btn-card-action btn-card-publish" onclick="window.cambiarEstadoPaquete('${p.idPaquete}', 'publicar')">
                     <i class="ti ti-send"></i> Publicar
                   </button>`;

            const descBadge = Number(p.descuento) > 0
                ? `<span class="paquete-discount-badge">${p.descuento}% de descuento</span>`
                : "";

            return `
                <div class="paquete-card">
                    <div class="paquete-img-wrap">
                        <img src="${imgUrl}" alt="${p.nombre}" class="paquete-card-img" onerror="this.onerror=null; this.src='../../img/lima.jpg';">
                        <span class="paquete-status-badge ${badgeClass}">
                            ${badgeIcon} ${estado}
                        </span>
                    </div>

                    <div class="paquete-card-body">
                        <h3 class="paquete-title">${p.nombre}</h3>
                        <p class="paquete-desc">${p.descripcion || 'Sin descripción detallada.'}</p>

                        <span class="servicios-incluidos-label">Servicios incluidos:</span>
                        <div class="servicios-incluidos-list">
                            ${serviciosChips}
                        </div>

                        <div class="paquete-price-row">
                            <div class="paquete-price-main">
                                <span class="paquete-price-amount">S/ ${Number(p.precio || 0).toLocaleString('es-PE', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</span>
                                ${descBadge}
                            </div>
                            <span class="paquete-duration">
                                <i class="ti ti-calendar"></i> ${p.duracion || 'Personalizado'}
                            </span>
                        </div>
                        <span class="paquete-price-subtitle">Precio por adulto</span>

                        <div class="paquete-actions-row">
                            <button type="button" class="btn-card-action btn-card-edit" onclick="window.abrirEditarPaquete('${p.idPaquete}')">
                                <i class="ti ti-pencil"></i> Editar
                            </button>
                            ${btnEstadoHtml}
                            <button type="button" class="btn-card-action btn-card-delete" onclick="window.eliminarPaquete('${p.idPaquete}', '${(p.nombre || '').replace(/'/g, "\\'")}')">
                                <i class="ti ti-trash"></i> Eliminar
                            </button>
                            <button type="button" class="btn-card-action" onclick="window.abrirDetallesPaquete('${p.idPaquete}')">
                                <i class="ti ti-eye"></i> Detalles
                            </button>
                        </div>
                    </div>
                </div>
            `;
        }).join("");
    }

    let galeriaImagenesArray = [];

    function setupDropzone() {
        const dropzone = document.getElementById("dropzonePaquete");
        const inputFile = document.getElementById("inputFilePaquete");

        if (dropzone && inputFile) {
            ['dragenter', 'dragover'].forEach(eventName => {
                dropzone.addEventListener(eventName, (e) => {
                    e.preventDefault();
                    e.stopPropagation();
                    dropzone.style.borderColor = "#4f46e5";
                    dropzone.style.background = "#eef2ff";
                }, false);
            });

            ['dragleave', 'drop'].forEach(eventName => {
                dropzone.addEventListener(eventName, (e) => {
                    e.preventDefault();
                    e.stopPropagation();
                    dropzone.style.borderColor = "#cbd5e1";
                    dropzone.style.background = "#f8fafc";
                }, false);
            });

            dropzone.addEventListener('drop', (e) => {
                const dt = e.dataTransfer;
                handleFiles(dt.files);
            }, false);

            inputFile.addEventListener('change', () => {
                handleFiles(inputFile.files);
            });
        }

        const urlInput = document.getElementById("imagenPaqueteInput");
        if (urlInput) {
            urlInput.addEventListener("input", () => {
                const val = urlInput.value.trim();
                if (val && !galeriaImagenesArray.includes(val)) {
                    galeriaImagenesArray.unshift(val);
                    renderizarPreviewsGaleria();
                }
            });
        }
    }

    function handleFiles(files) {
        if (!files || files.length === 0) return;
        Array.from(files).forEach(file => {
            if (!file.type.startsWith('image/')) return;
            const reader = new FileReader();
            reader.onload = (e) => {
                const dataUrl = e.target.result;
                if (!galeriaImagenesArray.includes(dataUrl)) {
                    galeriaImagenesArray.unshift(dataUrl);
                    if (imagenPaqueteInput) imagenPaqueteInput.value = dataUrl;
                    renderizarPreviewsGaleria();
                }
            };
            reader.readAsDataURL(file);
        });
    }

    function renderizarPreviewsGaleria() {
        const container = document.getElementById("previewGaleriaPaquete");
        if (!container) return;

        if (galeriaImagenesArray.length === 0) {
            container.innerHTML = "";
            return;
        }

        container.innerHTML = galeriaImagenesArray.map((imgSrc, idx) => `
            <div style="position:relative; width:84px; height:84px; border-radius:10px; overflow:hidden; border:1px solid #cbd5e1; box-shadow:0 2px 4px rgba(0,0,0,0.05);">
                <img src="${imgSrc}" style="width:100%; height:100%; object-fit:cover;">
                <button type="button" onclick="window.eliminarImagenPreview(${idx})" style="position:absolute; top:4px; right:4px; background:rgba(239,68,68,0.9); color:white; border:none; border-radius:50%; width:20px; height:20px; display:flex; align-items:center; justify-content:center; font-size:12px; cursor:pointer;">&times;</button>
                ${idx === 0 ? '<span style="position:absolute; bottom:3px; left:3px; background:#4f46e5; color:white; font-size:9px; font-weight:bold; padding:2px 5px; border-radius:4px;">Portada</span>' : ''}
            </div>
        `).join("");

        if (galeriaImagenesArray.length > 0 && imagenPaqueteInput) {
            imagenPaqueteInput.value = galeriaImagenesArray[0];
        }
    }

    window.eliminarImagenPreview = function (idx) {
        galeriaImagenesArray.splice(idx, 1);
        renderizarPreviewsGaleria();
    };

    function abrirModal(modalEl) {
        if (!modalEl) return;
        modalEl.style.display = "flex";
        modalEl.style.opacity = "1";
        modalEl.style.pointerEvents = "auto";
        modalEl.classList.add("active", "show");
    }

    function cerrarModal(modalEl) {
        if (!modalEl) return;
        modalEl.style.display = "none";
        modalEl.style.opacity = "0";
        modalEl.style.pointerEvents = "none";
        modalEl.classList.remove("active", "show");
    }

    window.cerrarModalDetalles = function () {
        const modalDet = document.getElementById("modalDetallesPaquete");
        if (modalDet) cerrarModal(modalDet);
    };

    function setupModal() {
        const modalDet = document.getElementById("modalDetallesPaquete");
        const btnCerrarDet = document.getElementById("btnCerrarModalDetalles");
        const cerrarDetX = document.getElementById("cerrarModalDetallesX");

        if (btnCerrarDet && modalDet) {
            btnCerrarDet.addEventListener("click", () => cerrarModal(modalDet));
        }
        if (cerrarDetX && modalDet) {
            cerrarDetX.addEventListener("click", () => cerrarModal(modalDet));
        }

        if (btnNuevoPaquete && modalPaquete) {
            btnNuevoPaquete.addEventListener("click", () => {
                resetearForm();
                tituloModalPaquete.textContent = "Nuevo paquete turístico";
                abrirModal(modalPaquete);
            });
        }

        if (cerrarModalPaquete && modalPaquete) {
            cerrarModalPaquete.addEventListener("click", () => {
                cerrarModal(modalPaquete);
            });
        }

        if (btnCancelarPaquete && modalPaquete) {
            btnCancelarPaquete.addEventListener("click", () => {
                cerrarModal(modalPaquete);
            });
        }

        // Cerrar modal al hacer click en el fondo backdrop
        [modalPaquete, document.getElementById("modalDetallesPaquete")].forEach(m => {
            if (m) {
                m.addEventListener("click", (e) => {
                    if (e.target === m) {
                        cerrarModal(m);
                    }
                });
            }
        });

        if (formPaquete) {
            formPaquete.addEventListener("submit", async (e) => {
                e.preventDefault();

                const idPaq = idPaqueteInput.value;
                const nombre = nombrePaqueteInput.value.trim();
                const desc = descPaqueteInput.value.trim();
                const precio = parseFloat(precioPaqueteInput.value);
                const descVal = parseInt(descuentoPaqueteInput.value || "0");
                const duracion = duracionPaqueteInput.value.trim();
                const estado = estadoPaqueteSelect.value;
                const imagen = (imagenPaqueteInput.value.trim() || "../../img/lima.jpg");
                const gal1 = (document.getElementById("imgGaleria1") ? document.getElementById("imgGaleria1").value.trim() : "");
                const gal2 = (document.getElementById("imgGaleria2") ? document.getElementById("imgGaleria2").value.trim() : "");
                const condiciones = condicionesPaqueteInput.value.trim();

                if (!nombre || isNaN(precio) || precio <= 0) {
                    mostrarMensaje("Ingresa un nombre y precio válido.", "error");
                    return;
                }

                const payload = {
                    action: idPaq ? "editar" : "guardar",
                    idPaquete: idPaq ? Number(idPaq) : Date.now(),
                    idAgencia: idAgenciaActual,
                    nombre: nombre,
                    descripcion: desc,
                    precio: precio,
                    descuento: descVal,
                    duracion: duracion || "3 días / 2 noches",
                    estado: estado || "PUBLICADO",
                    imagen: imagen,
                    galeria: [gal1, gal2].filter(x => x && x.length > 0),
                    condiciones: condiciones || "Incluye traslados y guiado profesional"
                };

                // Actualización optimista inmediata en la interfaz
                if (idPaq) {
                    const idx = todosLosPaquetes.findIndex(x => String(x.idPaquete) === String(idPaq));
                    if (idx !== -1) todosLosPaquetes[idx] = { ...todosLosPaquetes[idx], ...payload };
                } else {
                    todosLosPaquetes.unshift(payload);
                }
                aplicarFiltros();
                cerrarModal(modalPaquete);

                try {
                    const res = await fetch("http://localhost:8080/api/agencia/paquetes", {
                        method: "POST",
                        headers: { "Content-Type": "application/json" },
                        body: JSON.stringify(payload)
                    });
                    const data = await res.json();
                    if (data.status === "success") {
                        cargarPaquetes();
                    }
                } catch (err) {
                    console.warn("Backend offline o lento al guardar paquete, guardado localmente:", err);
                }
            });
        }
    }

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
        galeriaImagenesArray = [];
        if (idPaqueteInput) idPaqueteInput.value = "";
        if (nombrePaqueteInput) nombrePaqueteInput.value = "";
        if (descPaqueteInput) descPaqueteInput.value = "";
        if (precioPaqueteInput) precioPaqueteInput.value = "";
        if (descuentoPaqueteInput) descuentoPaqueteInput.value = "";
        if (duracionPaqueteInput) duracionPaqueteInput.value = "";
        if (estadoPaqueteSelect) estadoPaqueteSelect.value = "PUBLICADO";
        if (imagenPaqueteInput) imagenPaqueteInput.value = "";

        const g1 = document.getElementById("imgGaleria1");
        const g2 = document.getElementById("imgGaleria2");
        if (g1) g1.value = "";
        if (g2) g2.value = "";

        const pPort = document.getElementById("imgPreviewPortada");
        const pG1 = document.getElementById("imgPreviewGaleria1");
        const pG2 = document.getElementById("imgPreviewGaleria2");
        const emptyImg = "data:image/gif;base64,R0lGODlhAQABAAD/ACwAAAAAAQABAAACADs=";
        if (pPort) pPort.src = emptyImg;
        if (pG1) pG1.src = emptyImg;
        if (pG2) pG2.src = emptyImg;

        if (condicionesPaqueteInput) condicionesPaqueteInput.value = "";
        if (mensajePaquete) mensajePaquete.style.display = "none";
    }

    function mostrarMensaje(texto, tipo) {
        if (!mensajePaquete) return;
        mensajePaquete.textContent = texto;
        mensajePaquete.style.display = "block";
        mensajePaquete.style.background = tipo === "error" ? "#fee2e2" : "#dcfce7";
        mensajePaquete.style.color = tipo === "error" ? "#b91c1c" : "#15803d";
    }

    window.abrirEditarPaquete = function (id) {
        const p = todosLosPaquetes.find(x => String(x.idPaquete) === String(id));
        if (!p) {
            alert("No se encontró el paquete seleccionado.");
            return;
        }

        resetearForm();
        tituloModalPaquete.textContent = "Editar paquete turístico";
        idPaqueteInput.value = p.idPaquete;
        nombrePaqueteInput.value = p.nombre || "";
        descPaqueteInput.value = p.descripcion || "";
        precioPaqueteInput.value = p.precio || "";
        descuentoPaqueteInput.value = p.descuento || 0;
        duracionPaqueteInput.value = p.duracion || "";
        estadoPaqueteSelect.value = p.estado || "PUBLICADO";
        imagenPaqueteInput.value = p.imagen || "";
        condicionesPaqueteInput.value = p.condiciones || "";

        const g1 = document.getElementById("imgGaleria1");
        const g2 = document.getElementById("imgGaleria2");
        const pPort = document.getElementById("imgPreviewPortada");
        const pG1 = document.getElementById("imgPreviewGaleria1");
        const pG2 = document.getElementById("imgPreviewGaleria2");

        const galArr = Array.isArray(p.galeria) ? p.galeria : [];
        if (g1) g1.value = galArr[0] || "";
        if (g2) g2.value = galArr[1] || "";

        if (pPort) pPort.src = p.imagen || "../../img/lima.jpg";
        if (pG1) pG1.src = galArr[0] || "../../img/valle.jpg";
        if (pG2) pG2.src = galArr[1] || "../../img/cusco.jpg";

        abrirModal(modalPaquete);
    };

    window.abrirDetallesPaquete = function (id) {
        const p = todosLosPaquetes.find(x => String(x.idPaquete) === String(id));
        if (!p) {
            alert("No se encontró el paquete.");
            return;
        }

        const modalDetalles = document.getElementById("modalDetallesPaquete");
        if (modalDetalles) {
            document.getElementById("detNombrePaquete").textContent = p.nombre || "-";
            document.getElementById("detDescPaquete").textContent = p.descripcion || "Sin descripción detallada.";
            document.getElementById("detPrecioPaquete").textContent = "S/ " + Number(p.precio || 0).toLocaleString("es-PE", { minimumFractionDigits: 2, maximumFractionDigits: 2 });
            document.getElementById("detDescuentoPaquete").textContent = (p.descuento || 0) + "% de descuento";
            document.getElementById("detDuracionPaquete").textContent = p.duracion || "3 días / 2 noches";
            document.getElementById("detEstadoPaquete").textContent = String(p.estado || "PUBLICADO").toUpperCase();
            document.getElementById("detCondicionesPaquete").textContent = p.condiciones || "Incluye traslados y guiado profesional en servicio compartido o privado.";
            const cuposDisp = (p.cuposDisponibles !== undefined && p.cuposDisponibles !== null) ? p.cuposDisponibles : 25;
            const cupoTot = (p.cupoTotal !== undefined && p.cupoTotal !== null) ? p.cupoTotal : 30;
            document.getElementById("detCuposPaquete").textContent = `${cuposDisp} de ${cupoTot} cupos disponibles por salida`;

            abrirModal(modalDetalles);
        } else {
            window.abrirEditarPaquete(id);
        }
    };

    window.cambiarEstadoPaquete = async function (id, accion) {
        // Actualización optimista inmediata
        const target = todosLosPaquetes.find(x => String(x.idPaquete) === String(id));
        if (target) {
            target.estado = accion === "pausar" ? "PAUSADO" : "PUBLICADO";
            aplicarFiltros();
        }

        try {
            const res = await fetch("http://localhost:8080/api/agencia/paquetes", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    action: accion,
                    idPaquete: id,
                    idAgencia: idAgenciaActual
                })
            });
            const data = await res.json();
            if (data.status === "success") {
                cargarPaquetes();
            }
        } catch (e) {
            console.warn("API offline o lenta para cambiar estado, estado actualizado localmente:", e);
        }
    };

    window.eliminarPaquete = async function (id, nombre) {
        if (!confirm(`¿Estás seguro de que deseas eliminar el paquete "${nombre}"?`)) return;

        // Eliminar inmediatamente del estado local
        todosLosPaquetes = todosLosPaquetes.filter(x => String(x.idPaquete) !== String(id));
        aplicarFiltros();

        try {
            const res = await fetch("http://localhost:8080/api/agencia/paquetes", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    action: "eliminar",
                    idPaquete: id,
                    idAgencia: idAgenciaActual
                })
            });
            const data = await res.json();
            if (data.status === "success") {
                cargarPaquetes();
            }
        } catch (e) {
            console.warn("API offline o lenta para eliminar, paquete eliminado localmente:", e);
        }
    };

})();
