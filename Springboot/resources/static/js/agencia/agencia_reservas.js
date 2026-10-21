/**
 * Travelink - Módulo Reservas Agencia
 */
(function () {
    const API_BASE = (typeof window !== 'undefined' && window.API_BASE) ? window.API_BASE : (typeof window !== 'undefined' ? window.location.origin : '');
    const ID_AGENCIA_DEFAULT = 1;
    let sesion = null;
    try {
        const raw = localStorage.getItem("agenciaSesion") || localStorage.getItem("agenciasesión");
        sesion = JSON.parse(raw || "{}");
    } catch (e) { }

    const idAgenciaActual = (sesion && sesion.idAgencia) ? Number(sesion.idAgencia) : ID_AGENCIA_DEFAULT;
    const nombreAgenciaActual = (sesion && (sesion.nombreComercial || sesion.nombre || sesion.nombreUsuario)) ? (sesion.nombreComercial || sesion.nombre || sesion.nombreUsuario) : "Agencia";

    let todasLasReservas = [];
    let reservasFiltradas = [];

    // DOM Elements
    const tablaReservas = document.getElementById("tablaReservas");
    const totalReservasEl = document.getElementById("totalReservas");
    const reservasConfirmadasEl = document.getElementById("reservasConfirmadas");
    const reservasPendientesEl = document.getElementById("reservasPendientes");
    const reservasCanceladasEl = document.getElementById("reservasCanceladas");

    const buscarReserva = document.getElementById("buscarReserva");
    const filtroEstado = document.getElementById("filtroEstado");
    const fechaDesde = document.getElementById("fechaDesde");
    const fechaHasta = document.getElementById("fechaHasta");

    const modalReserva = document.getElementById("modalReserva");
    const btnCerrarModal = document.getElementById("btnCerrarModal");

    const init = () => {
        setupTopBar();
        cargarReservas();
        setupFiltros();
        setupModal();
    };

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", init);
    } else {
        init();
    }

    async function setupTopBar() {
        try {
            const meRes = await fetch(`${API_BASE}/api/auth/me`, { credentials: "include" });
            if (meRes.ok) {
                const user = await meRes.json();
                const nombre = user.nombreComercial || user.nombre || user.correo || "Agencia";
                const topNombre = document.getElementById("nombreAgenciaTop");
                const dropNombre = document.getElementById("dropdownNombreAgencia");
                const avatarLetter = document.getElementById("agencyAvatarLetter");

                if (topNombre) topNombre.textContent = nombre;
                if (dropNombre) dropNombre.textContent = nombre;
                if (avatarLetter) avatarLetter.textContent = nombre.charAt(0).toUpperCase();
            }
        } catch (e) {}

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
                localStorage.removeItem("travelink_session");
                localStorage.removeItem("agenciasesión");
                window.location.href = "R_agencia_login.html";
            };
        }
    }

    async function cargarReservas() {
        if (!tablaReservas) return;

        try {
            const res = await fetch(`${API_BASE}/api/agencia/reservas`, { credentials: "include" });
            if (!res.ok) throw new Error("HTTP " + res.status);
            const data = await res.json();
            if (data.status === "success" && Array.isArray(data.reservas)) {
                todasLasReservas = data.reservas;
                actualizarEstadisticas();
                aplicarFiltros();
                return;
            }
            throw new Error(data.message || "Error al leer reservas");
        } catch (e) {
            console.error("Error al cargar reservas desde backend API:", e);
            tablaReservas.innerHTML = `<tr><td colspan="8" style="text-align:center; padding:35px; color:#ef4444;"><i class="ti ti-alert-triangle"></i> Error al conectar con el servidor: ${e.message}</td></tr>`;
            todasLasReservas = [];
            actualizarEstadisticas();
        }
    }

    function actualizarEstadisticas() {
        if (totalReservasEl) totalReservasEl.textContent = todasLasReservas.length;
        if (reservasConfirmadasEl) reservasConfirmadasEl.textContent = todasLasReservas.filter(r => String(r.estado).toUpperCase() === "CONFIRMADA" || String(r.estado).toUpperCase() === "COMPLETADA").length;
        if (reservasPendientesEl) reservasPendientesEl.textContent = todasLasReservas.filter(r => String(r.estado).toUpperCase() === "PENDIENTE").length;
        if (reservasCanceladasEl) reservasCanceladasEl.textContent = todasLasReservas.filter(r => String(r.estado).toUpperCase() === "CANCELADA").length;
    }

    function setupFiltros() {
        if (buscarReserva) buscarReserva.addEventListener("input", aplicarFiltros);
        if (filtroEstado) filtroEstado.addEventListener("change", aplicarFiltros);
        if (fechaDesde) fechaDesde.addEventListener("change", aplicarFiltros);
        if (fechaHasta) fechaHasta.addEventListener("change", aplicarFiltros);
    }

    function aplicarFiltros() {
        const query = (buscarReserva ? buscarReserva.value : "").trim().toLowerCase();
        const estadoSel = (filtroEstado ? filtroEstado.value : "").trim().toUpperCase();
        const fDesde = (fechaDesde ? fechaDesde.value : "").trim();
        const fHasta = (fechaHasta ? fechaHasta.value : "").trim();

        reservasFiltradas = todasLasReservas.filter(r => {
            const matchQ = !query || 
                (r.codigoReserva && r.codigoReserva.toLowerCase().includes(query)) ||
                (r.nombreCliente && r.nombreCliente.toLowerCase().includes(query)) ||
                (r.nombreTour && r.nombreTour.toLowerCase().includes(query));
            
            const est = String(r.estado || "").toUpperCase();
            const matchE = !estadoSel || est === estadoSel || (estadoSel === "CONFIRMADA" && est === "COMPLETADA");

            const reg = r.fechaRegistro || "";
            const matchFD = !fDesde || reg >= fDesde;
            const matchFH = !fHasta || reg <= fHasta;

            return matchQ && matchE && matchFD && matchFH;
        });

        renderizarTabla();
    }

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

    window.cerrarModalReserva = function () {
        const modalRes = document.getElementById("modalReserva");
        if (modalRes) cerrarModal(modalRes);
    };

    function renderizarTabla() {
        if (!tablaReservas) return;

        if (reservasFiltradas.length === 0) {
            tablaReservas.innerHTML = `
                <tr>
                    <td colspan="8" style="text-align:center; padding:40px; color:#94a3b8;">
                        <i class="ti ti-calendar-off" style="font-size:32px; display:block; margin-bottom:8px; color:#cbd5e1;"></i>
                        No se encontraron reservas con los criterios seleccionados.
                    </td>
                </tr>`;
            return;
        }

        tablaReservas.innerHTML = reservasFiltradas.map(r => {
            const estado = String(r.estado || "PENDIENTE").toUpperCase();
            let badgeStyle = "background:#eff6ff; color:#2563eb; border:1px solid #bfdbfe;";
            if (estado === "CONFIRMADA" || estado === "COMPLETADA") {
                badgeStyle = "background:#dcfce7; color:#15803d; border:1px solid #86efac;";
            } else if (estado === "CANCELADA") {
                badgeStyle = "background:#fee2e2; color:#b91c1c; border:1px solid #fca5a5;";
            }

            const totalNum = Number(r.total || 0).toLocaleString("es-PE", { minimumFractionDigits: 2, maximumFractionDigits: 2 });

            const fechaHora = r.fechaHoraSalida || (r.fechaSalida ? `${r.fechaSalida} - ${r.horaSalida || '08:00'}` : r.fechaInicio || '-');
            const comisionMonto = r.comision ? Number(r.comision).toFixed(2) : (Number(r.total || 0) * 0.15).toFixed(2);
            const desgloseExacto = r.desglose || (r.cantAdultos ? (r.cantAdultos + (r.cantAdultos === 1 ? ' adulto' : ' adultos') + (r.cantNinos ? ' + ' + r.cantNinos + ' niños' : '') + (r.cantBebes ? ' + ' + r.cantBebes + ' bebé (gratis)' : '')) : (r.cantidadPersonas + ' personas'));

            const btnConfirmar = (estado === "PENDIENTE")
                ? `<button type="button" class="btn-act" style="background:#dcfce7; color:#15803d; border:1px solid #86efac; padding:5px 9px; border-radius:6px; font-size:12px; cursor:pointer;" onclick="window.confirmarReserva(${r.idReserva})">
                     <i class="ti ti-check"></i> Confirmar
                   </button>`
                : "";

            const btnCancelar = (estado !== "CANCELADA")
                ? `<button type="button" class="btn-act" style="background:#fffbeb; color:#d97706; border:1px solid #fde68a; padding:5px 9px; border-radius:6px; font-size:12px; cursor:pointer;" onclick="window.cancelarReserva(${r.idReserva})">
                     <i class="ti ti-x"></i> Cancelar
                   </button>`
                : "";

            const btnEliminar = `<button type="button" class="btn-act" style="background:#fef2f2; color:#dc2626; border:1px solid #fecaca; padding:5px 9px; border-radius:6px; font-size:12px; cursor:pointer;" onclick="window.eliminarReserva(${r.idReserva}, '${r.codigoReserva || ('RES-' + r.idReserva)}')">
                     <i class="ti ti-trash"></i> Eliminar
                   </button>`;

            return `
                <tr>
                    <td style="font-weight:700; color:#0f172a;">${r.codigoReserva || 'RES-' + r.idReserva}</td>
                    <td>
                        <div style="font-weight:600; color:#0f172a;">${r.nombreCliente || 'Cliente'}</div>
                        <small style="color:#0284c7; font-weight:600; display:block;">${desgloseExacto}</small>
                    </td>
                    <td style="font-weight:600; color:#2563eb;">
                        <i class="ti ti-compass" style="margin-right:4px;"></i> ${r.nombreTour || 'Tour de la Agencia'}
                    </td>
                    <td>${r.fechaRegistro || '-'}</td>
                    <td style="font-weight:600; color:#0f172a;">
                        <i class="ti ti-calendar" style="color:#16a34a;"></i> ${fechaHora}
                    </td>
                    <td style="font-weight:700; color:#0f172a;">
                        S/ ${totalNum}
                        <br><small style="color:#64748b; font-weight:normal;">Comisión 15%: S/ ${comisionMonto}</small>
                    </td>
                    <td>
                        <span style="display:inline-block; padding:3px 10px; border-radius:999px; font-size:11px; font-weight:700; ${badgeStyle}">
                            ${estado}
                        </span>
                    </td>
                    <td>
                        <div style="display:flex; gap:5px; flex-wrap:wrap;">
                            <button type="button" class="btn-act" style="background:#f8fafc; border:1px solid #cbd5e1; padding:5px 9px; border-radius:6px; font-size:12px; cursor:pointer;" onclick="window.verDetalleReserva(${r.idReserva})">
                                <i class="ti ti-eye"></i> Detalles
                            </button>
                            ${btnConfirmar}
                            ${btnCancelar}
                            ${btnEliminar}
                        </div>
                    </td>
                </tr>
            `;
        }).join("");
    }

    function setupModal() {
        if (btnCerrarModal && modalReserva) {
            btnCerrarModal.onclick = () => cerrarModal(modalReserva);
        }
        if (modalReserva) {
            modalReserva.addEventListener("click", (e) => {
                if (e.target === modalReserva) cerrarModal(modalReserva);
            });
        }
    }

    window.verDetalleReserva = function (id) {
        const r = todasLasReservas.find(x => Number(x.idReserva) === Number(id));
        if (!r || !modalReserva) return;

        const desgloseExacto = r.desglose || (r.cantAdultos ? (r.cantAdultos + (r.cantAdultos === 1 ? ' adulto' : ' adultos') + (r.cantNinos ? ' + ' + r.cantNinos + ' niños' : '') + (r.cantBebes ? ' + ' + r.cantBebes + ' bebé (gratis)' : '')) : (r.cantidadPersonas + ' personas'));
        const fechaHora = r.fechaHoraSalida || (r.fechaSalida ? `${r.fechaSalida} - ${r.horaSalida || '08:00'}` : r.fechaInicio || '-');
        const comisionMonto = r.comision ? Number(r.comision).toFixed(2) : (Number(r.total || 0) * 0.15).toFixed(2);

        document.getElementById("detalleCodigo").textContent = r.codigoReserva || ("RES-" + r.idReserva);
        document.getElementById("detalleCliente").innerHTML = `${r.nombreCliente || "Cliente Registrado"}<br><small style="color:#0284c7; font-weight:600;">Desglose: ${desgloseExacto}</small>`;
        document.getElementById("detalleTour").textContent = r.nombreTour || "Tour de la Agencia";
        document.getElementById("detalleRegistro").textContent = r.fechaRegistro || "-";
        document.getElementById("detalleInicio").textContent = fechaHora;
        document.getElementById("detalleFin").textContent = r.fechaFin || r.fechaInicio || "-";
        document.getElementById("detalleTotal").innerHTML = `S/ ${Number(r.total || 0).toFixed(2)} <span style="font-size:12px; color:#64748b; font-weight:normal;">(Comisión Travelink 15%: S/ ${comisionMonto})</span>`;
        document.getElementById("detallePagado").textContent = "S/ " + Number(r.totalPagado || 0).toFixed(2);
        document.getElementById("detalleEstado").textContent = String(r.estado || "PENDIENTE").toUpperCase();
        document.getElementById("detalleEstadoPago").textContent = String(r.estadoPago || "PENDIENTE").toUpperCase();
        document.getElementById("detalleMotivo").textContent = r.motivoCancelacion || "Sin observaciones";

        abrirModal(modalReserva);
    };

    window.confirmarReserva = async function (id) {
        if (!confirm("¿Deseas confirmar la reserva seleccionada?")) return;

        const target = todasLasReservas.find(x => Number(x.idReserva) === Number(id));
        if (target) {
            target.estado = "CONFIRMADA";
            target.estadoPago = "COMPLETADO";
            actualizarEstadisticas();
            aplicarFiltros();
        }

        try {
            const fetchFn = window.apiFetch || fetch;
            const res = await fetchFn(`${API_BASE}/api/agencia/reservas`, {
                method: "POST",
                credentials: "include",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ action: "confirmar", idReserva: id, idAgencia: idAgenciaActual })
            });
            const data = await res.json();
            if (data.status === "success") cargarReservas();
        } catch (e) {
            console.error("Error al confirmar reserva:", e);
        }
    };

    window.cancelarReserva = async function (id) {
        if (!confirm("¿Estás seguro de que deseas cancelar esta reserva? Esta acción cambiará el estado a CANCELADA.")) return;

        const target = todasLasReservas.find(x => Number(x.idReserva) === Number(id));
        if (target) {
            target.estado = "CANCELADA";
            target.estadoPago = "RECHAZADO";
            actualizarEstadisticas();
            aplicarFiltros();
        }

        try {
            const fetchFn = window.apiFetch || fetch;
            const res = await fetchFn(`${API_BASE}/api/agencia/reservas`, {
                method: "POST",
                credentials: "include",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ action: "cancelar", idReserva: id, idAgencia: idAgenciaActual })
            });
            const data = await res.json();
            if (data.status === "success") cargarReservas();
        } catch (e) {
            console.error("Error al cancelar reserva:", e);
        }
    };

    window.eliminarReserva = async function (id, codigo) {
        if (!confirm(`¿Estás seguro de que deseas eliminar la reserva ${codigo}?`)) return;

        todasLasReservas = todasLasReservas.filter(x => Number(x.idReserva) !== Number(id));
        actualizarEstadisticas();
        aplicarFiltros();

        try {
            const fetchFn = window.apiFetch || fetch;
            const res = await fetchFn(`${API_BASE}/api/agencia/reservas`, {
                method: "POST",
                credentials: "include",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ action: "eliminar", idReserva: id, idAgencia: idAgenciaActual })
            });
            const data = await res.json();
            if (data.status === "success") cargarReservas();
        } catch (e) {
            console.error("Error al eliminar reserva:", e);
        }
    };

    window.exportarReservasAgenciaExcel = function () {
        const estado = filtroEstado ? filtroEstado.value : "";
        const fDesde = fechaDesde ? fechaDesde.value : "";
        const fHasta = fechaHasta ? fechaHasta.value : "";
        const params = new URLSearchParams();
        if (estado) params.append("estado", estado);
        if (fDesde) params.append("fechaInicio", fDesde);
        if (fHasta) params.append("fechaFin", fHasta);
        window.open(`/api/agencia/reportes/reservas.xlsx?${params.toString()}`, "_blank");
    };

    window.descargarComprobanteAgencia = function (idOIdReserva) {
        if (!idOIdReserva) return;
        window.open(`/api/turista/reservas/${encodeURIComponent(idOIdReserva)}/comprobante.pdf`, "_blank");
    };

})();
