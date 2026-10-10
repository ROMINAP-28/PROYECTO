/**
 * Travelink - Módulo Disponibilidad y Cupos
 */
(function () {
    let sesion = null;
    try {
        const sesionRaw = localStorage.getItem("agenciaSesion") || localStorage.getItem("agenciasesión") || localStorage.getItem("travelink_user");
        sesion = JSON.parse(sesionRaw || "{}");
    } catch (e) { }

    const idAgenciaActual = (sesion && sesion.idAgencia) ? Number(sesion.idAgencia) : 2;
    const nombreAgenciaActual = (sesion && (sesion.nombreComercial || sesion.nombreAgencia || sesion.nombre || sesion.nombreUsuario)) ? (sesion.nombreComercial || sesion.nombreAgencia || sesion.nombre || sesion.nombreUsuario) : "INKA TRAVEL";

    let todasDisponibilidades = [];
    let filtradas = [];
    let serviciosAgencia = [];

    // DOM Elements
    const tablaDisp = document.getElementById("tablaDisponibilidad");
    const cantidadDisp = document.getElementById("cantidadDisponibilidades");
    const buscarDisp = document.getElementById("buscarDisponibilidad");
    const filtroEstadoDisp = document.getElementById("filtroEstadoDisponibilidad");
    const filtroFechaDisp = document.getElementById("filtroFechaDisponibilidad");

    const btnNuevaDisp = document.getElementById("btnNuevaDisponibilidad");
    const modalDisp = document.getElementById("modalDisponibilidad");
    const cerrarModalDisp = document.getElementById("cerrarModalDisponibilidad");
    const btnCancelarDisp = document.getElementById("btnCancelarDisponibilidad");
    const formDisp = document.getElementById("formDisponibilidad");
    const tituloModalDisp = document.getElementById("tituloModalDisponibilidad");
    const mensajeDisp = document.getElementById("mensajeDisponibilidad");

    // Modal Form inputs
    const idDispInput = document.getElementById("idDispInput");
    const selectServicioDisp = document.getElementById("selectServicioDisp");
    const fechaDispInput = document.getElementById("fechaDispInput");
    const horaInicioDisp = document.getElementById("horaInicioDisp");
    const horaFinDisp = document.getElementById("horaFinDisp");
    const cupoTotalDisp = document.getElementById("cupoTotalDisp");

    // Modal Ver
    const modalVerDisp = document.getElementById("modalVerDisponibilidad");
    const cerrarModalVer = document.getElementById("cerrarModalVer");
    const detalleVerContent = document.getElementById("detalleVerContent");

    document.addEventListener("DOMContentLoaded", () => {
        setupTopBar();
        cargarServiciosAgencia();
        cargarDisponibilidades();
        setupFiltros();
        setupModales();
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
                window.location.href = "R_agencia_login.html";
            };
        }
    }

    async function cargarServiciosAgencia() {
        try {
            const res = await fetch(`http://localhost:8080/api/agencia/servicios?idAgencia=${idAgenciaActual}`);
            const data = await res.json();
            if (data.status === "success" && data.servicios) {
                serviciosAgencia = data.servicios;
                if (selectServicioDisp) {
                    selectServicioDisp.innerHTML = `<option value="">Seleccionar servicio...</option>` +
                        serviciosAgencia.map(s => `<option value="${s.idTour}">${s.nombre}</option>`).join("");
                }
            }
        } catch (e) {
            console.error("Error al cargar servicios para disponibilidad:", e);
        }
    }

    async function cargarDisponibilidades() {
        if (!tablaDisp) return;
        
        const controller = new AbortController();
        const timeoutId = setTimeout(() => controller.abort(), 1500);

        try {
            const res = await fetch(`http://localhost:8080/api/agencia/disponibilidad?idAgencia=${idAgenciaActual}`, {
                signal: controller.signal
            });
            clearTimeout(timeoutId);
            const data = await res.json();
            const list = Array.isArray(data) ? data : (data.disponibilidades || []);
            if (list.length > 0) {
                todasDisponibilidades = list;
                aplicarFiltros();
                return;
            }
        } catch (e) {
            console.warn("Fallo o timeout al conectar con API de disponibilidad, usando datos locales:", e);
        }

        // Fallback ultrarrápido con cupos por agencia
        todasDisponibilidades = [
            { idDisponibilidad: 1, idServicio: 1, servicio: "City Tour Lima Colonial y Catacumbas", fecha: "2026-10-15", horaInicio: "08:00:00", horaFin: "13:00:00", cupoTotal: 30, cupoDisponible: 28, estado: "DISPONIBLE" },
            { idDisponibilidad: 2, idServicio: 2, servicio: "Expedición Bosque de Piedras de Huayllay", fecha: "2026-10-18", horaInicio: "07:00:00", horaFin: "17:00:00", cupoTotal: 20, cupoDisponible: 14, estado: "DISPONIBLE" },
            { idDisponibilidad: 3, idServicio: 3, servicio: "Inmersión Selva Tambopata Madre de Dios", fecha: "2026-10-20", horaInicio: "09:00:00", horaFin: "18:00:00", cupoTotal: 15, cupoDisponible: 5, estado: "DISPONIBLE" },
            { idDisponibilidad: 4, idServicio: 4, servicio: "Ruta Histórica y Fuentes Termales Tacna", fecha: "2026-10-22", horaInicio: "08:30:00", horaFin: "14:00:00", cupoTotal: 25, cupoDisponible: 0, estado: "CERRADO" },
            { idDisponibilidad: 5, idServicio: 1, servicio: "City Tour Lima Colonial y Catacumbas", fecha: "2026-10-25", horaInicio: "14:00:00", horaFin: "18:30:00", cupoTotal: 30, cupoDisponible: 22, estado: "DISPONIBLE" }
        ];
        aplicarFiltros();
    }

    function setupFiltros() {
        if (buscarDisp) buscarDisp.addEventListener("input", aplicarFiltros);
        if (filtroEstadoDisp) filtroEstadoDisp.addEventListener("change", aplicarFiltros);
        if (filtroFechaDisp) filtroFechaDisp.addEventListener("change", aplicarFiltros);
    }

    function aplicarFiltros() {
        const query = (buscarDisp ? buscarDisp.value : "").trim().toLowerCase();
        const estado = (filtroEstadoDisp ? filtroEstadoDisp.value : "").trim().toUpperCase();
        const fecha = (filtroFechaDisp ? filtroFechaDisp.value : "").trim();

        filtradas = todasDisponibilidades.filter(d => {
            const matchQ = !query || (d.servicio && d.servicio.toLowerCase().includes(query));
            const matchE = !estado || String(d.estado).toUpperCase() === estado;
            const matchF = !fecha || String(d.fecha).startsWith(fecha);
            return matchQ && matchE && matchF;
        });

        if (cantidadDisp) {
            cantidadDisp.textContent = `${filtradas.length} disponibilidad${filtradas.length === 1 ? '' : 'es'}`;
        }

        renderizarTabla();
    }

    function renderizarTabla() {
        if (!tablaDisp) return;

        if (filtradas.length === 0) {
            tablaDisp.innerHTML = `<tr><td colspan="7" style="text-align:center; padding:35px; color:#94a3b8;">No se encontraron disponibilidades con los filtros aplicados.</td></tr>`;
            return;
        }

        tablaDisp.innerHTML = filtradas.map(d => {
            const estado = String(d.estado || 'DISPONIBLE').toUpperCase();
            let badgeClass = "activo";
            let badgeStyle = "background:#dcfce7; color:#15803d; border:1px solid #86efac;";
            if (estado === "CERRADO" || estado === "AGOTADO") {
                badgeStyle = "background:#fee2e2; color:#b91c1c; border:1px solid #fca5a5;";
            }

            const cupoTotal = Number(d.cupoTotal || 0);
            const cupoDisp = Number(d.cupoDisponible || 0);
            const cupoOcupado = Math.max(0, cupoTotal - cupoDisp);
            const pctOcupado = cupoTotal > 0 ? Math.round((cupoOcupado / cupoTotal) * 100) : 0;

            const horaIni = (d.horaInicio || '').substring(0, 5);
            const horaFn = (d.horaFin || '').substring(0, 5);
            const horario = horaIni ? (horaFn ? `${horaIni} - ${horaFn}` : `${horaIni} hrs`) : 'â€”';

            // Botón Cerrar ventas / Reabrir
            const btnCerrarReabrir = estado === "CERRADO"
                ? `<button type="button" class="btn-act" style="background:#eff6ff; color:#2563eb; border:1px solid #bfdbfe; padding:5px 9px; border-radius:6px; font-size:12px; cursor:pointer;" onclick="window.cambiarEstadoVentas(${d.idDisponibilidad}, 'reabrir')">
                     <i class="ti ti-lock-open"></i> Reabrir
                   </button>`
                : `<button type="button" class="btn-act" style="background:#fff7ed; color:#c2410c; border:1px solid #fed7aa; padding:5px 9px; border-radius:6px; font-size:12px; cursor:pointer;" onclick="window.cambiarEstadoVentas(${d.idDisponibilidad}, 'cerrar_ventas')">
                     <i class="ti ti-lock"></i> Cerrar ventas
                   </button>`;

            return `
                <tr>
                    <td style="font-weight:600; color:#0f172a;">
                        <i class="ti ti-compass" style="color:#2563eb; margin-right:4px;"></i> ${d.servicio || 'Servicio #' + d.idServicio}
                    </td>
                    <td>
                        <span style="display:inline-flex; align-items:center; gap:4px; font-weight:500;">
                            <i class="ti ti-calendar" style="color:#64748b;"></i> ${d.fecha}
                        </span>
                    </td>
                    <td>${horario}</td>
                    <td><strong>${cupoTotal}</strong> cupos</td>
                    <td>
                        <div>
                            <span style="font-weight:700; color:${cupoDisp > 0 ? '#16a34a' : '#ef4444'};">${cupoDisp} libres</span>
                            <small style="color:#64748b; margin-left:4px;">(${cupoOcupado} ocupados)</small>
                            <div style="background:#e2e8f0; border-radius:999px; height:5px; width:90px; margin-top:4px; overflow:hidden;">
                                <div style="background:#2563eb; width:${pctOcupado}%; height:100%;"></div>
                            </div>
                        </div>
                    </td>
                    <td>
                        <span style="display:inline-block; padding:3px 10px; border-radius:999px; font-size:11px; font-weight:700; ${badgeStyle}">
                            ${estado}
                        </span>
                    </td>
                    <td>
                        <div style="display:flex; gap:5px; flex-wrap:wrap;">
                            <button type="button" class="btn-act" style="background:#f8fafc; border:1px solid #cbd5e1; padding:5px 9px; border-radius:6px; font-size:12px; cursor:pointer;" onclick="window.verDisponibilidad(${d.idDisponibilidad})">
                                <i class="ti ti-eye"></i> Ver
                            </button>
                            <button type="button" class="btn-act" style="background:#f8fafc; border:1px solid #cbd5e1; padding:5px 9px; border-radius:6px; font-size:12px; cursor:pointer;" onclick="window.editarDisponibilidad(${d.idDisponibilidad})">
                                <i class="ti ti-pencil"></i> Editar
                            </button>
                            ${btnCerrarReabrir}
                            <button type="button" class="btn-act" style="background:#fef2f2; border:1px solid #fecaca; color:#ef4444; padding:5px 9px; border-radius:6px; font-size:12px; cursor:pointer;" onclick="window.eliminarDisponibilidad(${d.idDisponibilidad})">
                                <i class="ti ti-trash"></i> Eliminar
                            </button>
                        </div>
                    </td>
                </tr>
            `;
        }).join("");
    }

    function abrirModal(modal) {
        if (!modal) return;
        modal.classList.add("active", "show");
        modal.style.display = "flex";
        modal.style.opacity = "1";
        modal.style.pointerEvents = "auto";
    }

    function cerrarModal(modal) {
        if (!modal) return;
        modal.classList.remove("active", "show");
        modal.style.display = "none";
        modal.style.opacity = "0";
        modal.style.pointerEvents = "none";
    }

    function setupModales() {
        if (btnNuevaDisp && modalDisp) {
            btnNuevaDisp.onclick = () => {
                resetearFormDisp();
                tituloModalDisp.textContent = "Nueva disponibilidad y cupos";
                abrirModal(modalDisp);
            };
        }

        if (cerrarModalDisp && modalDisp) cerrarModalDisp.onclick = () => cerrarModal(modalDisp);
        if (btnCancelarDisp && modalDisp) btnCancelarDisp.onclick = () => cerrarModal(modalDisp);

        if (cerrarModalVer && modalVerDisp) {
            cerrarModalVer.onclick = () => cerrarModal(modalVerDisp);
        }

        // Close on background click
        window.addEventListener("click", (e) => {
            if (e.target === modalDisp) cerrarModal(modalDisp);
            if (e.target === modalVerDisp) cerrarModal(modalVerDisp);
        });

        if (formDisp) {
            formDisp.onsubmit = async (e) => {
                e.preventDefault();
                const idDisp = idDispInput.value;
                const idServicio = selectServicioDisp.value;
                const fecha = fechaDispInput.value;
                const horaIni = horaInicioDisp.value;
                const horaFn = horaFinDisp.value;
                const cupos = Number(cupoTotalDisp.value || 0);

                if (!idServicio || !fecha || !cupos || cupos <= 0) {
                    mostrarMensajeDisp("Completa todos los campos obligatorios correctamente.", "error");
                    return;
                }

                // Obtener nombre del servicio seleccionado
                const sObj = serviciosAgencia.find(s => String(s.idTour || s.idServicio) === String(idServicio));
                const nombreServicio = sObj ? sObj.nombre : `Servicio #${idServicio}`;

                // Actualización optimista local
                if (idDisp) {
                    const idx = todasDisponibilidades.findIndex(d => String(d.idDisponibilidad) === String(idDisp));
                    if (idx !== -1) {
                        todasDisponibilidades[idx] = {
                            ...todasDisponibilidades[idx],
                            idServicio: Number(idServicio),
                            servicio: nombreServicio,
                            fecha: fecha,
                            horaInicio: horaIni ? (horaIni.length === 5 ? horaIni + ":00" : horaIni) : "08:00:00",
                            horaFin: horaFn ? (horaFn.length === 5 ? horaFn + ":00" : horaFn) : "13:00:00",
                            cupoTotal: cupos,
                            cupoDisponible: Math.max(0, cupos - (todasDisponibilidades[idx].cupoTotal - todasDisponibilidades[idx].cupoDisponible))
                        };
                    }
                } else {
                    const newId = Date.now();
                    todasDisponibilidades.unshift({
                        idDisponibilidad: newId,
                        idServicio: Number(idServicio),
                        servicio: nombreServicio,
                        fecha: fecha,
                        horaInicio: horaIni ? (horaIni.length === 5 ? horaIni + ":00" : horaIni) : "08:00:00",
                        horaFin: horaFn ? (horaFn.length === 5 ? horaFn + ":00" : horaFn) : "13:00:00",
                        cupoTotal: cupos,
                        cupoDisponible: cupos,
                        estado: "DISPONIBLE"
                    });
                }

                cerrarModal(modalDisp);
                aplicarFiltros();

                // Sync asíncrono con Backend
                const params = new URLSearchParams();
                if (idDisp) params.append("idDisponibilidad", idDisp);
                params.append("idServicio", idServicio);
                params.append("fecha", fecha);
                params.append("horaInicio", horaIni ? (horaIni.length === 5 ? horaIni + ":00" : horaIni) : "");
                params.append("horaFin", horaFn ? (horaFn.length === 5 ? horaFn + ":00" : horaFn) : "");
                params.append("cupoTotal", cupos);

                try {
                    await fetch("http://localhost:8080/api/agencia/disponibilidad", {
                        method: "POST",
                        headers: { "Content-Type": "application/x-www-form-urlencoded" },
                        body: params.toString()
                    });
                } catch (err) {
                    console.warn("Sync backend disponibilidad diferido.");
                }
            };
        }
    }

    function resetearFormDisp() {
        if (idDispInput) idDispInput.value = "";
        if (selectServicioDisp) selectServicioDisp.value = "";
        if (fechaDispInput) fechaDispInput.value = "";
        if (horaInicioDisp) horaInicioDisp.value = "08:00";
        if (horaFinDisp) horaFinDisp.value = "13:00";
        if (cupoTotalDisp) cupoTotalDisp.value = "20";
        if (mensajeDisp) mensajeDisp.style.display = "none";
    }

    function mostrarMensajeDisp(txt, tipo) {
        if (!mensajeDisp) return;
        mensajeDisp.textContent = txt;
        mensajeDisp.style.display = "block";
        mensajeDisp.style.background = tipo === "error" ? "#fee2e2" : "#dcfce7";
        mensajeDisp.style.color = tipo === "error" ? "#b91c1c" : "#15803d";
    }

    window.verDisponibilidad = function (id) {
        const d = todasDisponibilidades.find(x => String(x.idDisponibilidad) === String(id));
        if (!d || !detalleVerContent || !modalVerDisp) return;

        const cupoTotal = Number(d.cupoTotal || 0);
        const cupoDisp = Number(d.cupoDisponible || 0);
        const cupoOcupado = Math.max(0, cupoTotal - cupoDisp);

        detalleVerContent.innerHTML = `
            <div style="display:flex; justify-content:space-between; margin-bottom:10px; border-bottom:1px solid #f1f5f9; padding-bottom:6px;">
                <span style="color:#64748b; font-size:13px;">Servicio / Tour:</span>
                <strong style="color:#0f172a;">${d.servicio || 'Servicio #' + d.idServicio}</strong>
            </div>
            <div style="display:flex; justify-content:space-between; margin-bottom:10px; border-bottom:1px solid #f1f5f9; padding-bottom:6px;">
                <span style="color:#64748b; font-size:13px;">Fecha programada:</span>
                <strong style="color:#0f172a;">${d.fecha}</strong>
            </div>
            <div style="display:flex; justify-content:space-between; margin-bottom:10px; border-bottom:1px solid #f1f5f9; padding-bottom:6px;">
                <span style="color:#64748b; font-size:13px;">Horario de salida:</span>
                <strong style="color:#0f172a;">${(d.horaInicio || '08:00').substring(0, 5)} - ${(d.horaFin || '13:00').substring(0, 5)} hrs</strong>
            </div>
            <div style="display:flex; justify-content:space-between; margin-bottom:10px; border-bottom:1px solid #f1f5f9; padding-bottom:6px;">
                <span style="color:#64748b; font-size:13px;">Cupo total de pasajeros:</span>
                <strong style="color:#0f172a;">${cupoTotal} cupos</strong>
            </div>
            <div style="display:flex; justify-content:space-between; margin-bottom:10px; border-bottom:1px solid #f1f5f9; padding-bottom:6px;">
                <span style="color:#64748b; font-size:13px;">Cupos disponibles (libres):</span>
                <strong style="color:#16a34a;">${cupoDisp} libres</strong>
            </div>
            <div style="display:flex; justify-content:space-between; margin-bottom:10px; border-bottom:1px solid #f1f5f9; padding-bottom:6px;">
                <span style="color:#64748b; font-size:13px;">Cupos ocupados (reservados):</span>
                <strong style="color:#2563eb;">${cupoOcupado} ocupados</strong>
            </div>
            <div style="display:flex; justify-content:space-between; margin-bottom:10px;">
                <span style="color:#64748b; font-size:13px;">Estado de ventas:</span>
                <span style="padding:2px 8px; border-radius:999px; font-weight:700; font-size:11px; ${d.estado === 'CERRADO' ? 'background:#fee2e2; color:#dc2626;' : 'background:#dcfce7; color:#166534;'}">${d.estado}</span>
            </div>
        `;

        abrirModal(modalVerDisp);
    };

    window.editarDisponibilidad = function (id) {
        const d = todasDisponibilidades.find(x => String(x.idDisponibilidad) === String(id));
        if (!d || !modalDisp) return;

        resetearFormDisp();
        tituloModalDisp.textContent = "Editar disponibilidad";
        idDispInput.value = d.idDisponibilidad;
        selectServicioDisp.value = d.idServicio;
        fechaDispInput.value = d.fecha;
        horaInicioDisp.value = (d.horaInicio || '08:00').substring(0, 5);
        horaFinDisp.value = (d.horaFin || '13:00').substring(0, 5);
        cupoTotalDisp.value = d.cupoTotal;

        abrirModal(modalDisp);
    };

    window.cambiarEstadoVentas = async function (id, accion) {
        const accionNom = accion === 'cerrar_ventas' ? 'cerrar ventas para' : 'reabrir ventas de';
        if (!confirm(`¿Deseas ${accionNom} esta fecha?`)) return;

        const idx = todasDisponibilidades.findIndex(x => String(x.idDisponibilidad) === String(id));
        if (idx !== -1) {
            todasDisponibilidades[idx].estado = accion === 'cerrar_ventas' ? 'CERRADO' : 'DISPONIBLE';
            aplicarFiltros();
        }

        try {
            const params = new URLSearchParams();
            params.append("action", accion);
            params.append("idDisponibilidad", id);

            await fetch("http://localhost:8080/api/agencia/disponibilidad", {
                method: "POST",
                headers: { "Content-Type": "application/x-www-form-urlencoded" },
                body: params.toString()
            });
        } catch (e) {
            console.warn("Error background sync cambiarEstadoVentas:", e);
        }
    };

    window.eliminarDisponibilidad = async function (id) {
        if (!confirm("¿Estás seguro de que deseas eliminar esta fecha de disponibilidad?")) return;

        todasDisponibilidades = todasDisponibilidades.filter(x => String(x.idDisponibilidad) !== String(id));
        aplicarFiltros();

        try {
            const params = new URLSearchParams();
            params.append("action", "eliminar");
            params.append("idDisponibilidad", id);

            await fetch("http://localhost:8080/api/agencia/disponibilidad", {
                method: "POST",
                headers: { "Content-Type": "application/x-www-form-urlencoded" },
                body: params.toString()
            });
        } catch (e) {
            console.warn("Error background sync eliminarDisponibilidad:", e);
        }
    };

})();
