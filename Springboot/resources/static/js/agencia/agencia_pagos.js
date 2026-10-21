/**
 * Travelink - Módulo Pagos y Liquidaciones Agencia
 */
(function () {
    const API_BASE = (typeof window !== 'undefined' && window.API_BASE) ? window.API_BASE : (typeof window !== 'undefined' ? window.location.origin : '');
    let sesion = null;
    try {
        const raw = localStorage.getItem("agenciaSesion") || localStorage.getItem("agenciasesión") || localStorage.getItem("travelink_user");
        sesion = JSON.parse(raw || "{}");
    } catch (e) { }

    const idAgenciaActual = (sesion && sesion.idAgencia) ? Number(sesion.idAgencia) : 1;
    const nombreAgenciaActual = (sesion && (sesion.nombreComercial || sesion.nombreAgencia || sesion.nombre || sesion.nombreUsuario)) ? (sesion.nombreComercial || sesion.nombreAgencia || sesion.nombre || sesion.nombreUsuario) : "Agencia";

    let todosLosPagos = [];
    let pagosFiltrados = [];
    let todasLasLiquidaciones = [];
    let liquidacionesFiltradas = [];

    // DOM Elements
    const tablaPagos = document.getElementById("tablaPagos");
    const tablaLiquidaciones = document.getElementById("tablaLiquidaciones");

    const totalPagosEl = document.getElementById("totalPagos");
    const totalComisionesEl = document.getElementById("totalComisiones");
    const netoPorRecibirEl = document.getElementById("netoPorRecibir");
    const liquidacionesPendientesEl = document.getElementById("liquidacionesPendientes");

    const buscarPago = document.getElementById("buscarPago");
    const filtroEstadoPago = document.getElementById("filtroEstadoPago");
    const filtroEstadoLiquidacion = document.getElementById("filtroEstadoLiquidacion");

    document.addEventListener("DOMContentLoaded", () => {
        setupTopBar();
        cargarPagosYLiquidaciones();
        setupFiltros();
    });

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

    async function cargarPagosYLiquidaciones() {
        if (!tablaPagos) return;

        try {
            const res = await fetch(`${API_BASE}/api/agencia/pagos`, { credentials: "include" });
            if (!res.ok) throw new Error("HTTP " + res.status);
            const data = await res.json();
            if (data.status === "success") {
                todosLosPagos = Array.isArray(data.pagos) ? data.pagos : [];
                todasLasLiquidaciones = Array.isArray(data.liquidaciones) ? data.liquidaciones : [];
                actualizarResumen();
                aplicarFiltros();
                return;
            }
            throw new Error(data.message || "Error al obtener pagos");
        } catch (e) {
            console.error("Error al conectar con API de pagos:", e);
            if (tablaPagos) {
                tablaPagos.innerHTML = `<tr><td colspan="7" style="text-align:center; padding:30px; color:#ef4444;"><i class="ti ti-alert-triangle"></i> Error al conectar con el servidor: ${e.message}</td></tr>`;
            }
            if (tablaLiquidaciones) {
                tablaLiquidaciones.innerHTML = `<tr><td colspan="6" style="text-align:center; padding:30px; color:#ef4444;"><i class="ti ti-alert-triangle"></i> Error al conectar con el servidor: ${e.message}</td></tr>`;
            }
            todosLosPagos = [];
            todasLasLiquidaciones = [];
            actualizarResumen();
        }
    }

    function actualizarResumen() {
        const sumaBruta = todosLosPagos.reduce((acc, p) => acc + Number(p.monto || 0), 0);
        const sumaComisiones = sumaBruta * 0.15;
        const sumaNeto = sumaBruta * 0.85;
        const liqPendientes = todasLasLiquidaciones.filter(l => String(l.estado).toUpperCase() === "PENDIENTE").length;

        if (totalPagosEl) totalPagosEl.textContent = "S/ " + sumaBruta.toLocaleString("es-PE", { minimumFractionDigits: 2, maximumFractionDigits: 2 });
        if (totalComisionesEl) totalComisionesEl.textContent = "S/ " + sumaComisiones.toLocaleString("es-PE", { minimumFractionDigits: 2, maximumFractionDigits: 2 });
        if (netoPorRecibirEl) netoPorRecibirEl.textContent = "S/ " + sumaNeto.toLocaleString("es-PE", { minimumFractionDigits: 2, maximumFractionDigits: 2 });
        if (liquidacionesPendientesEl) liquidacionesPendientesEl.textContent = liqPendientes;
    }

    function setupFiltros() {
        if (buscarPago) buscarPago.addEventListener("input", aplicarFiltros);
        if (filtroEstadoPago) filtroEstadoPago.addEventListener("change", aplicarFiltros);
        if (filtroEstadoLiquidacion) filtroEstadoLiquidacion.addEventListener("change", aplicarFiltros);
    }

    function aplicarFiltros() {
        const q = (buscarPago ? buscarPago.value : "").trim().toLowerCase();
        const estP = (filtroEstadoPago ? filtroEstadoPago.value : "").trim().toUpperCase();
        const estL = (filtroEstadoLiquidacion ? filtroEstadoLiquidacion.value : "").trim().toUpperCase();

        pagosFiltrados = todosLosPagos.filter(p => {
            const matchQ = !q || (p.numeroOperacion && p.numeroOperacion.toLowerCase().includes(q)) || (p.codigoReserva && p.codigoReserva.toLowerCase().includes(q));
            const matchE = !estP || estP === "TODOS" || String(p.estado || "").toUpperCase() === estP;
            return matchQ && matchE;
        });

        liquidacionesFiltradas = todasLasLiquidaciones.filter(l => {
            return !estL || estL === "TODOS" || String(l.estado || "").toUpperCase() === estL;
        });

        renderizarTablas();
    }

    function renderizarTablas() {
        if (tablaPagos) {
            if (pagosFiltrados.length === 0) {
                tablaPagos.innerHTML = `<tr><td colspan="7" style="text-align:center; padding:30px; color:#94a3b8;">No hay pagos registrados.</td></tr>`;
            } else {
                tablaPagos.innerHTML = pagosFiltrados.map((p, idx) => {
                    const est = String(p.estado || "COMPLETADO").toUpperCase();
                    let badgeStyle = "background:#dcfce7; color:#15803d; border:1px solid #86efac;";
                    if (est === "PENDIENTE") badgeStyle = "background:#eff6ff; color:#2563eb; border:1px solid #bfdbfe;";
                    else if (est === "RECHAZADO") badgeStyle = "background:#fee2e2; color:#b91c1c; border:1px solid #fca5a5;";

                    return `
                        <tr>
                            <td>${idx + 1}</td>
                            <td style="font-weight:700; color:#0f172a;">${p.numeroOperacion || 'OP-' + p.idPago}</td>
                            <td style="font-weight:600; color:#2563eb;">${p.codigoReserva || 'RES-' + p.idReserva}</td>
                            <td>${p.fechaPago || '-'}</td>
                            <td><span style="background:#f1f5f9; padding:2px 8px; border-radius:4px; font-size:12px; font-weight:600;">${p.metodoPago || 'Digital'}</span></td>
                            <td style="font-weight:700; color:#0f172a;">S/ ${Number(p.monto || 0).toFixed(2)}</td>
                            <td><span style="display:inline-block; padding:3px 10px; border-radius:999px; font-size:11px; font-weight:700; ${badgeStyle}">${est}</span></td>
                        </tr>
                    `;
                }).join("");
            }
        }

        if (tablaLiquidaciones) {
            if (liquidacionesFiltradas.length === 0) {
                tablaLiquidaciones.innerHTML = `<tr><td colspan="6" style="text-align:center; padding:30px; color:#94a3b8;">No hay liquidaciones registradas.</td></tr>`;
            } else {
                tablaLiquidaciones.innerHTML = liquidacionesFiltradas.map((l, idx) => {
                    const est = String(l.estado || "PENDIENTE").toUpperCase();
                    let badgeStyle = est === "LIQUIDADO"
                        ? "background:#dcfce7; color:#15803d; border:1px solid #86efac;"
                        : "background:#fff7ed; color:#c2410c; border:1px solid #fed7aa;";

                    return `
                        <tr>
                            <td>${idx + 1}</td>
                            <td>${l.fechaGeneracion || '-'}</td>
                            <td style="font-weight:600;">S/ ${Number(l.montoBruto || 0).toFixed(2)}</td>
                            <td style="color:#2563eb; font-weight:600;">S/ ${Number(l.montoComision || 0).toFixed(2)}</td>
                            <td style="color:#16a34a; font-weight:700;">S/ ${Number(l.montoNeto || 0).toFixed(2)}</td>
                            <td><span style="display:inline-block; padding:3px 10px; border-radius:999px; font-size:11px; font-weight:700; ${badgeStyle}">${est}</span></td>
                        </tr>
                    `;
                }).join("");
            }
        }
    }

    window.exportarPagosAgenciaExcel = function () {
        const estado = filtroEstadoPago ? filtroEstadoPago.value : "";
        const params = new URLSearchParams();
        if (estado) params.append("estado", estado);
        window.open(`/api/agencia/reportes/pagos.xlsx?${params.toString()}`, "_blank");
    };

})();
