/**
 * Travelink - Módulo Pagos y Liquidaciones Agencia
 */
(function () {
    const ID_AGENCIA_DEFAULT = 1;
    let sesion = null;
    try {
        const raw = localStorage.getItem("agenciaSesion") || localStorage.getItem("agenciasesión");
        sesion = JSON.parse(raw || "{}");
    } catch (e) { }

    const idAgenciaActual = (sesion && sesion.idAgencia) ? Number(sesion.idAgencia) : ID_AGENCIA_DEFAULT;
    const nombreAgenciaActual = (sesion && (sesion.nombreComercial || sesion.nombreUsuario)) ? (sesion.nombreComercial || sesion.nombreUsuario) : "ANDES TOURS";

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

    async function cargarPagosYLiquidaciones() {
        if (!tablaPagos) return;

        const controller = new AbortController();
        const timeoutId = setTimeout(() => controller.abort(), 1500);

        try {
            const res = await fetch(`http://localhost:8080/api/agencia/pagos?idAgencia=${idAgenciaActual}`, {
                signal: controller.signal
            });
            clearTimeout(timeoutId);
            const data = await res.json();
            if (data.status === "success") {
                todosLosPagos = Array.isArray(data.pagos) ? data.pagos : [];
                todasLasLiquidaciones = Array.isArray(data.liquidaciones) ? data.liquidaciones : [];
                if (todosLosPagos.length > 0 || todasLasLiquidaciones.length > 0) {
                    actualizarResumen();
                    aplicarFiltros();
                    return;
                }
            }
        } catch (e) {
            console.warn("Fallo o timeout al conectar con API de pagos, usando datos locales:", e);
        }

        // Fallback datos locales ultrarrápidos
        todosLosPagos = [
            { idPago: 1, numeroOperacion: "OP-982341", idReserva: 101, codigoReserva: "RES-2026-001", nombreTour: "City Tour Lima Colonial", fechaPago: "2026-09-28 14:30:00", metodoPago: "Yape / Plin", monto: 160.00, estado: "COMPLETADO" },
            { idPago: 2, numeroOperacion: "OP-982342", idReserva: 102, codigoReserva: "RES-2026-002", nombreTour: "Expedición Andina Huayllay", fechaPago: "2026-09-29 11:15:00", metodoPago: "Tarjeta de Crédito", monto: 2880.00, estado: "COMPLETADO" },
            { idPago: 3, numeroOperacion: "OP-982343", idReserva: 103, codigoReserva: "RES-2026-003", nombreTour: "Amazonía Profunda Tambopata", fechaPago: "2026-09-30 09:00:00", metodoPago: "Transferencia BCP", monto: 2900.00, estado: "PENDIENTE" }
        ];

        todasLasLiquidaciones = [
            { idLiquidacion: 1, montoBruto: 3040.00, montoComision: 456.00, montoNeto: 2584.00, fechaGeneracion: "2026-09-30", fechaLiquidacion: "2026-10-02", estado: "PENDIENTE", idAgencia: idAgenciaActual },
            { idLiquidacion: 2, montoBruto: 4500.00, montoComision: 675.00, montoNeto: 3825.00, fechaGeneracion: "2026-09-15", fechaLiquidacion: "2026-09-17", estado: "LIQUIDADO", idAgencia: idAgenciaActual }
        ];

        actualizarResumen();
        aplicarFiltros();
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

})();
