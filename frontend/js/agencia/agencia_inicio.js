/**
 * Travelink - Panel Inicio Agencia Dashboard
 */
(function () {
    let sesion = null;
    try {
        const raw = localStorage.getItem("agenciaSesion") || localStorage.getItem("agenciasesión") || localStorage.getItem("travelink_user");
        sesion = JSON.parse(raw || "{}");
    } catch (e) { }

    const idAgenciaActual = (sesion && sesion.idAgencia) ? Number(sesion.idAgencia) : 2;
    const nombreAgenciaActual = (sesion && (sesion.nombreComercial || sesion.nombreAgencia || sesion.nombre || sesion.nombreUsuario)) ? (sesion.nombreComercial || sesion.nombreAgencia || sesion.nombre || sesion.nombreUsuario) : "INKA TRAVEL";

    // DOM Elements
    const bienvenidaAgenciaEl = document.getElementById("bienvenidaAgencia");
    const totalServiciosEl = document.getElementById("totalServicios");
    const totalReservasEl = document.getElementById("totalReservas");
    const totalViajerosEl = document.getElementById("totalViajeros");
    const totalIngresosEl = document.getElementById("totalIngresos");

    const tablaReservas = document.getElementById("tablaReservas");
    const listaDisponibilidad = document.getElementById("listaDisponibilidad");

    const pagosRecibidosEl = document.getElementById("pagosRecibidos");
    const comisionesEl = document.getElementById("comisiones");
    const liquidacionesPendientesEl = document.getElementById("liquidacionesPendientes");
    const montoNetoEl = document.getElementById("montoNeto");

    const init = () => {
        setupTopBar();
        cargarDashboard();
    };

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", init);
    } else {
        init();
    }

    function setupTopBar() {
        try {
            const raw = localStorage.getItem("agenciaSesion") || localStorage.getItem("agenciasesión");
            const s = JSON.parse(raw || "{}");
            const nombre = (s && (s.nombreComercial || s.nombreUsuario)) ? (s.nombreComercial || s.nombreUsuario) : nombreAgenciaActual;

            const topNombre = document.getElementById("nombreAgenciaTop");
            const dropNombre = document.getElementById("dropdownNombreAgencia");
            const avatarLetter = document.getElementById("agencyAvatarLetter");

            if (topNombre) topNombre.textContent = nombre;
            if (dropNombre) dropNombre.textContent = nombre;
            if (avatarLetter) avatarLetter.textContent = nombre.charAt(0).toUpperCase();
            if (bienvenidaAgenciaEl) bienvenidaAgenciaEl.textContent = nombre;
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
                localStorage.removeItem("agenciaSesion");
                localStorage.removeItem("agenciasesión");
                window.location.href = "R_agencia_login.html";
            };
        }
    }

    async function cargarDashboard() {
        try {
            const res = await fetch(`http://localhost:8080/api/agencia/dashboard?idAgencia=${idAgenciaActual}`);
            const data = await res.json();
            if (data.status === "success") {
                renderizarDashboard(data);
                return;
            }
        } catch (e) {
            console.warn("Fallo al conectar con API de dashboard:", e);
        }

        const localData = {
            totalServicios: 1,
            totalReservas: 2,
            totalViajeros: 6,
            totalIngresos: 1500,
            reservasRecientes: [],
            proximasSalidas: []
        };
        renderizarDashboard(localData);
    }

    function renderizarDashboard(d) {
        const ind = d.indicadores || {};
        const fin = d.finanzas || {};

        const numServicios = d.totalServicios !== undefined ? d.totalServicios : (ind.serviciosActivos !== undefined ? ind.serviciosActivos : 0);
        const numReservas = d.totalReservas !== undefined ? d.totalReservas : (ind.reservasMes !== undefined ? ind.reservasMes : 0);
        const numViajeros = d.totalViajeros !== undefined ? d.totalViajeros : (ind.viajeros !== undefined ? ind.viajeros : 0);
        const totalIng = Number(d.totalIngresos !== undefined ? d.totalIngresos : (ind.ingresos !== undefined ? ind.ingresos : (fin.pagosRecibidos !== undefined ? fin.pagosRecibidos : 0)));

        if (totalServiciosEl) totalServiciosEl.textContent = numServicios;
        if (totalReservasEl) totalReservasEl.textContent = numReservas;
        if (totalViajerosEl) totalViajerosEl.textContent = numViajeros;

        const ingFormatted = totalIng.toLocaleString("es-PE", { minimumFractionDigits: 2, maximumFractionDigits: 2 });
        if (totalIngresosEl) totalIngresosEl.textContent = "S/ " + ingFormatted;

        const pagosRecibidos = fin.pagosRecibidos !== undefined ? Number(fin.pagosRecibidos) : totalIng;
        const comisiones = fin.comisiones !== undefined ? Number(fin.comisiones) : (totalIng * 0.15);
        const montoNeto = fin.montoNeto !== undefined ? Number(fin.montoNeto) : (totalIng * 0.85);

        if (pagosRecibidosEl) pagosRecibidosEl.textContent = "S/ " + pagosRecibidos.toLocaleString("es-PE", { minimumFractionDigits: 2, maximumFractionDigits: 2 });
        if (comisionesEl) comisionesEl.textContent = "S/ " + comisiones.toLocaleString("es-PE", { minimumFractionDigits: 2, maximumFractionDigits: 2 });
        if (liquidacionesPendientesEl) liquidacionesPendientesEl.textContent = numReservas > 0 ? "1 pendiente" : "0 pendientes";
        if (montoNetoEl) montoNetoEl.textContent = "S/ " + montoNeto.toLocaleString("es-PE", { minimumFractionDigits: 2, maximumFractionDigits: 2 });

        // Tabla Reservas Recientes
        if (tablaReservas) {
            const list = d.reservasRecientes || [];
            if (list.length === 0) {
                tablaReservas.innerHTML = `<tr><td colspan="5" style="text-align:center; padding:20px; color:#94a3b8;">No hay reservas recientes.</td></tr>`;
            } else {
                tablaReservas.innerHTML = list.map(r => {
                    const est = String(r.estado || "CONFIRMADA").toUpperCase();
                    let badgeStyle = "background:#dcfce7; color:#15803d; border:1px solid #86efac;";
                    if (est === "PENDIENTE") badgeStyle = "background:#eff6ff; color:#2563eb; border:1px solid #bfdbfe;";
                    else if (est === "CANCELADA") badgeStyle = "background:#fee2e2; color:#b91c1c; border:1px solid #fca5a5;";

                    return `
                        <tr>
                            <td style="font-weight:600; color:#0f172a;">${r.cliente || 'Turista'}</td>
                            <td style="color:#2563eb; font-weight:500;">${r.servicio || r.tourNombre || 'Valle Sagrado de los Incas'}</td>
                            <td>${r.fecha || r.fechaTour || '-'}</td>
                            <td><strong>${r.personas || 2}</strong> pers.</td>
                            <td><span style="padding:2px 8px; border-radius:999px; font-size:11px; font-weight:700; ${badgeStyle}">${est}</span></td>
                        </tr>
                    `;
                }).join("");
            }
        }

        // Próximas Salidas
        if (listaDisponibilidad) {
            const listS = d.proximasSalidas || [];
            if (listS.length === 0) {
                listaDisponibilidad.innerHTML = `<div style="text-align:center; padding:20px; color:#94a3b8;">No hay salidas programadas.</div>`;
            } else {
                listaDisponibilidad.innerHTML = listS.map(s => {
                    const disp = s.cupoDisponible !== undefined ? s.cupoDisponible : (s.cupoLibre !== undefined ? s.cupoLibre : 18);
                    const total = s.cupoTotal || 20;
                    return `
                        <div style="background:#f8fafc; border:1px solid #e2e8f0; border-radius:10px; padding:12px 16px; margin-bottom:10px; display:flex; justify-content:space-between; align-items:center;">
                            <div>
                                <strong style="color:#0f172a; display:block; font-size:14px;">${s.servicio}</strong>
                                <small style="color:#64748b;"><i class="ti ti-calendar"></i> ${s.fecha}</small>
                            </div>
                            <div style="text-align:right;">
                                <span style="font-weight:700; color:#16a34a; font-size:13px;">${disp} libres</span>
                                <small style="color:#64748b; display:block; font-size:11px;">de ${total} total</small>
                            </div>
                        </div>
                    `;
                }).join("");
            }
        }
    }

})();
