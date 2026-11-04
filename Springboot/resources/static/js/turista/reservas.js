// Travelink - Reservas Dynamic Management & PDF Voucher Generator (User Scoped)

const ITEMS_PER_PAGE = 5;
let currentPage = 1;
let reservasMemoria = null; // Fuente de verdad en memoria proveniente de la BD

function getUserReservasKey(user) {
    user = user || (window.getCurrentUser ? window.getCurrentUser() : null);
    const identifier = user ? (user.idUsuario || user.id || user.email || user.correo || 'anon') : 'anon';
    return 'travelink_reservas_' + identifier;
}

function getReservasData(user) {
    if (reservasMemoria !== null) {
        return reservasMemoria;
    }
    const key = getUserReservasKey(user);
    const stored = localStorage.getItem(key);

    if (!stored) {
        return [];
    }
    try {
        const parsed = JSON.parse(stored);
        return Array.isArray(parsed) ? parsed : [];
    } catch (e) {
        return [];
    }
}

document.addEventListener('DOMContentLoaded', () => {
    // Escuchar cuando la sesión esté lista para cargar las reservas del usuario autenticado
    if (window.onSessionReady) {
        window.onSessionReady((user) => {
            cargarReservasDesdeDB(user);
        });
    } else if (window.sessionReadyPromise) {
        window.sessionReadyPromise.then((user) => {
            cargarReservasDesdeDB(user);
        });
    } else {
        cargarReservasDesdeDB();
    }

    // Sincronización automática en tiempo real
    window.addEventListener('travelink:reserva_cancelada', () => cargarReservasDesdeDB());
    window.addEventListener('travelink:reserva_creada', () => cargarReservasDesdeDB());
});

function cargarReservasDesdeDB(user) {
    user = user || (window.getCurrentUser ? window.getCurrentUser() : null);
    const email = user ? (user.email || user.correo || '') : '';
    const idUsuario = user ? (user.idUsuario || user.id || '') : '';
    const key = getUserReservasKey(user);

    const listContainer = document.querySelector('.lista-reservas');
    if (listContainer && (!listContainer.innerHTML || listContainer.innerHTML.trim() === '')) {
        listContainer.innerHTML = `
            <div style="text-align:center; padding: 50px 20px; background: white; border-radius: 16px; border: 1px solid #e2e8f0; margin-top: 10px;">
                <i class="fa-solid fa-spinner fa-spin" style="font-size: 32px; color: #196f3d; margin-bottom: 15px;"></i>
                <h4 style="color: #0b1f38; margin: 0 0 6px 0; font-size: 16px;">Cargando tus reservas...</h4>
                <p style="color: #64748b; font-size: 14px; margin: 0;">Estamos sincronizando tu información.</p>
            </div>
        `;
    }

    const fetchFn = window.apiFetch || fetch;
    const url = `/api/obtener_reservas?correo=${encodeURIComponent(email)}&idUsuario=${encodeURIComponent(idUsuario)}`;

    fetchFn(url, { credentials: 'include' })
        .then(res => res.json())
        .then(data => {
            if (data && data.status === 'success' && Array.isArray(data.reservas)) {
                reservasMemoria = data.reservas;
                localStorage.setItem(key, JSON.stringify(data.reservas));
            }
        })
        .catch(err => {
            console.warn('No se pudieron cargar reservas desde la DB:', err);
        })
        .finally(() => {
            renderReservasPage(user);
        });
}

function injectReservasCSS() {
    if (document.getElementById('reservas-modern-styles')) return;
    const style = document.createElement('style');
    style.id = 'reservas-modern-styles';
    style.innerHTML = `
        .reservas-container { max-width: 95% !important; margin: 0 auto; }
        .reservas-table-modern { width: 100%; min-width: 1100px; border-collapse: separate; border-spacing: 0 10px; font-family: 'Inter', sans-serif; }
        .reservas-table-modern thead th { background-color: #f1f5f9; color: #475569; font-size: 13px; font-weight: 600; text-transform: none; padding: 16px 20px; text-align: left; border: none; }
        .reservas-table-modern thead th:first-child { border-radius: 8px 0 0 8px; }
        .reservas-table-modern thead th:last-child { border-radius: 0 8px 8px 0; }
        .reservas-table-modern tbody tr { background-color: #ffffff; box-shadow: 0 2px 4px rgba(0,0,0,0.02); transition: all 0.2s ease; border-radius: 8px; }
        .reservas-table-modern tbody tr:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.05); transform: translateY(-1px); }
        .reservas-table-modern tbody td { padding: 16px 20px; vertical-align: middle; border-top: 1px solid #f1f5f9; border-bottom: 1px solid #f1f5f9; color: #1e293b; font-size: 14px; }
        .reservas-table-modern tbody td:first-child { border-left: 1px solid #f1f5f9; border-radius: 8px 0 0 8px; font-weight: bold; }
        .reservas-table-modern tbody td:last-child { border-right: 1px solid #f1f5f9; border-radius: 0 8px 8px 0; }
        .res-img-thumb { width: 120px; height: 80px; object-fit: cover; border-radius: 8px; }
        .res-title { font-weight: 700; color: #0f172a; margin: 0 0 4px 0; font-size: 15px; }
        .res-loc { font-size: 13px; color: #64748b; margin-bottom: 6px; display: flex; align-items: center; gap: 4px; }
        .res-badge-tour { display: inline-block; background: #e0f2fe; color: #0284c7; font-size: 11px; padding: 2px 8px; border-radius: 12px; font-weight: 600; }
        .res-badge-confirmada { background: #dcfce7; color: #16a34a; padding: 6px 12px; border-radius: 20px; font-size: 13px; font-weight: 600; display: inline-flex; align-items: center; gap: 6px; border: 1px solid #bbf7d0; }
        .res-badge-pendiente { background: #e0f2fe; color: #0284c7; padding: 6px 12px; border-radius: 20px; font-size: 13px; font-weight: 600; display: inline-flex; align-items: center; gap: 6px; border: 1px solid #bae6fd; }
        .res-badge-cancelada { background: #f1f5f9; color: #64748b; padding: 6px 12px; border-radius: 20px; font-size: 13px; font-weight: 600; display: inline-flex; align-items: center; gap: 6px; border: 1px solid #e2e8f0; }
        .res-actions { display: flex; flex-direction: column; gap: 6px; }
        .btn-act { padding: 8px 12px; border-radius: 6px; font-size: 13px; font-weight: 600; cursor: pointer; display: flex; align-items: center; justify-content: center; gap: 6px; border: none; transition: 0.2s; }
        .btn-act-brand { background: #196f3d; color: white; }
        .btn-act-brand:hover { background: #145a32; }
        .btn-act-outline { background: white; color: #0f172a; border: 1px solid #cbd5e1; }
        .btn-act-outline:hover { background: #f8fafc; }
        .btn-act-danger-outline { background: white; color: #ef4444; border: 1px solid #fca5a5; }
        .btn-act-danger-outline:hover { background: #fef2f2; }
        .agency-col { display: flex; align-items: center; gap: 8px; }
        .agency-icon { width: 28px; height: 28px; border-radius: 50%; background: #0284c7; color: white; display: flex; align-items: center; justify-content: center; font-size: 12px; }

        /* Estilos del Modal Nuevo */
        .modal-detalles-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(15, 23, 42, 0.6); display: flex; justify-content: center; align-items: center; z-index: 9999; opacity: 0; visibility: hidden; transition: 0.3s; backdrop-filter: blur(4px); }
        .modal-detalles-overlay.active { opacity: 1; visibility: visible; }
        .modal-detalles-card { background: white; width: 900px; max-width: 95%; max-height: 90vh; border-radius: 16px; overflow-y: auto; box-shadow: 0 20px 25px -5px rgba(0,0,0,0.1); display: flex; flex-direction: column; }
        .md-header { padding: 20px 30px; display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #f1f5f9; position: sticky; top: 0; background: white; z-index: 10; }
        .md-header h2 { margin: 0; font-size: 20px; color: #0f172a; font-weight: 700; }
        .md-close { background: none; border: none; font-size: 20px; color: #64748b; cursor: pointer; }
        .md-body { padding: 30px; }
        .md-top-grid { display: grid; grid-template-columns: 2fr 1fr; gap: 24px; margin-bottom: 30px; }
        .md-main-info { display: flex; gap: 20px; }
        .md-main-info img { width: 140px; height: 140px; border-radius: 12px; object-fit: cover; }
        .md-main-details { display: flex; flex-direction: column; gap: 10px; }
        .md-main-details h3 { margin: 0; font-size: 18px; display: flex; align-items: center; gap: 10px; }
        .md-right-card { background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 12px; padding: 20px; display: flex; flex-direction: column; gap: 15px; }
        
        .md-tabs { display: flex; border-bottom: 1px solid #e2e8f0; gap: 30px; margin-bottom: 24px; }
        .md-tab { padding: 12px 0; font-weight: 600; color: #64748b; cursor: pointer; position: relative; display: flex; align-items: center; gap: 8px; }
        .md-tab.active { color: #196f3d; }
        .md-tab.active::after { content: ''; position: absolute; bottom: -1px; left: 0; width: 100%; height: 3px; background: #196f3d; border-radius: 3px 3px 0 0; }
        
        .md-tab-content { display: none; }
        .md-tab-content.active { display: block; }

        .md-pass-table { width: 100%; border-collapse: collapse; margin-bottom: 24px; }
        .md-pass-table th { text-align: left; padding: 12px; color: #475569; font-size: 13px; font-weight: 600; border-bottom: 1px solid #e2e8f0; }
        .md-pass-table td { padding: 12px; font-size: 14px; color: #1e293b; border-bottom: 1px solid #f1f5f9; }
        
        .md-info-box { background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 12px; padding: 20px; margin-bottom: 30px; }
        .md-info-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; margin-top: 15px; }
        .md-info-item { display: flex; gap: 10px; }
        .md-info-item i { color: #475569; margin-top: 3px; }
        .md-info-item div p { margin: 0; font-size: 12px; color: #64748b; font-weight: 600; }
        .md-info-item div span { font-size: 14px; color: #0f172a; font-weight: 500; }
        
        .md-footer { display: flex; justify-content: flex-end; gap: 15px; align-items: center; padding: 20px 30px; border-top: 1px solid #f1f5f9; background: white; }
        .md-btn { padding: 12px 24px; border-radius: 8px; font-weight: 600; font-size: 14px; cursor: pointer; transition: 0.2s; display: flex; align-items: center; gap: 8px; }
        .md-btn-calificar { background: #196f3d; color: white; border: none; }
        .md-btn-calificar:hover { background: #145a32; }
        .md-btn-cerrar { background: white; color: #0f172a; border: 1px solid #cbd5e1; }
    `;
    document.head.appendChild(style);
}

function renderReservasPage(user) {
    injectReservasCSS();
    const allReservas = getReservasData(user);
    const listContainer = document.querySelector('.lista-reservas');

    if (!listContainer) return;

    const totalItems = allReservas.length;
    const totalPages = Math.ceil(totalItems / ITEMS_PER_PAGE) || 1;

    if (currentPage > totalPages) currentPage = totalPages;
    if (currentPage < 1) currentPage = 1;

    const startIndex = (currentPage - 1) * ITEMS_PER_PAGE;
    const pageItems = allReservas.slice(startIndex, startIndex + ITEMS_PER_PAGE);

    listContainer.innerHTML = '';

    if (pageItems.length === 0) {
        listContainer.innerHTML = `
            <div style="text-align:center; padding: 50px 20px; background: white; border-radius: 16px; border: 1px solid #e2e8f0;">
                <i class="fa-regular fa-calendar-xmark" style="font-size: 48px; color: #94a3b8; margin-bottom: 15px;"></i>
                <h3 style="color: #0b1f38;">No tienes reservas registradas</h3>
                <p style="color: #64748b;">Explora nuestros tours y realiza tu primera reserva hoy mismo.</p>
                <a href="experiencias.html" style="display: inline-block; margin-top: 15px; background: #ffc107; color: #0b1f38; padding: 10px 20px; border-radius: 20px; text-decoration: none; font-weight: bold;">Explorar Experiencias</a>
            </div>
        `;
    } else {
        let tableHtml = `
        <div style="overflow-x: auto; padding: 4px;">
            <table class="reservas-table-modern">
                <thead>
                    <tr>
                        <th>#</th>
                        <th>Imagen</th>
                        <th>Titulo / Destino</th>
                        <th>Agencia</th>
                        <th>Fecha de reserva</th>
                        <th>Salida (Fecha y Hora)</th>
                        <th>Personas / Desglose</th>
                        <th>Total</th>
                        <th>Estado</th>
                        <th style="text-align: center;">Acciones</th>
                    </tr>
                </thead>
                <tbody>
        `;

        pageItems.forEach((r, index) => {
            const isCompletada = r.estado === 'Completada' || r.estado === 'Finalizada' || r.estado === 'Confirmada';
            const isCancelada = r.estado === 'Cancelada';
            const isCalificada = r.calificada === true;
            
            let badgeHtml = '';
            if (isCompletada) badgeHtml = `<span class="res-badge-confirmada"><i class="fa-regular fa-circle-check"></i> ${r.estado}</span>`;
            else if (isCancelada) badgeHtml = `<span class="res-badge-cancelada"><i class="fa-solid fa-xmark"></i> Cancelada</span>`;
            else badgeHtml = `<span class="res-badge-pendiente"><i class="fa-regular fa-clock"></i> Pendiente</span>`;

            const desgloseStr = r.desglose || (r.cantAdultos ? (r.cantAdultos + (r.cantAdultos === 1 ? ' adulto' : ' adultos') + (r.cantNinos ? ' + ' + r.cantNinos + (r.cantNinos === 1 ? ' niño' : ' niños') : '') + (r.cantBebes ? ' + ' + r.cantBebes + ' bebé (gratis)' : '')) : '2 adultos');
            const fechaHoraDisplay = r.fechas || (r.fechaSalida ? `${r.fechaSalida} - ${r.horaSalida || '08:00'}` : 'Próximamente');

            tableHtml += `
                    <tr>
                        <td>${startIndex + index + 1}</td>
                        <td><img loading="lazy" src="${r.imagen || '../../img/valle.jpg'}" alt="${r.titulo}" class="res-img-thumb" onerror="this.onerror=null; this.src='../../img/fondo1.png';"></td>
                        <td>
                            <h4 class="res-title">${r.titulo}</h4>
                            <div class="res-loc"><i class="fa-solid fa-location-dot"></i> ${r.ubicacion || 'Perú'}</div>
                            <span class="res-badge-tour">Tour</span>
                        </td>
                        <td>
                            <div class="agency-col">
                                <div class="agency-icon"><i class="fa-solid fa-mountain"></i></div>
                                <span>${(r.agencia || '').replace('Agencia: ', '') || 'Andes Travel Perú'}</span>
                            </div>
                        </td>
                        <td style="color: #64748b;">${r.fechaRegistro || '10 abr. 2025'}<br><small>14:32</small></td>
                        <td style="color: #0f172a; font-weight:600;"><i class="fa-regular fa-clock" style="color:#196f3d;"></i> ${fechaHoraDisplay}</td>
                        <td style="white-space: nowrap;">
                            <div><i class="fa-solid fa-user-group" style="color: #64748b;"></i> <strong>${r.personas || '1 persona'}</strong></div>
                            <small style="color:#0284c7; font-weight:600; display:block; margin-top:2px;">${desgloseStr}</small>
                        </td>
                        <td style="font-weight: 700;">${rFormattedTotal(r.total)}</td>
                        <td>${badgeHtml}</td>
                        <td style="vertical-align: middle;">
                            <div class="res-actions">
                                <div style="display: flex; gap: 6px;">
                                    <button class="btn-act btn-act-brand" onclick="abrirDetalleNuevo('${r.codigo}')"><i class="fa-regular fa-eye"></i> Ver detalles</button>
                                    <button class="btn-act btn-act-outline" onclick="descargarVoucherPDF('${r.codigo}')"><i class="fa-solid fa-download"></i> Exportar PDF</button>
                                </div>
                                ${isCalificada ? `
                                    <span class="res-badge-confirmada" style="background:#fef3c7; color:#b45309; border-color:#fde68a; font-weight:700; width:100%; justify-content:center; display:inline-flex; align-items:center; gap:6px; padding:6px 10px;">
                                        <i class="fa-solid fa-star" style="color:#eab308;"></i> Calificada
                                    </span>
                                ` : (isCompletada ? `
                                    <button class="btn-act" style="background:#eab308; color:#0f172a; font-weight:700; width:100%; display:inline-flex; align-items:center; justify-content:center; gap:6px;" onclick="abrirModalCalificarReserva('${r.codigo}')">
                                        <i class="fa-solid fa-star"></i> Calificar experiencia
                                    </button>
                                ` : '')}
                                ${!isCancelada ? `<button class="btn-act btn-act-danger-outline" style="width: 100%;" onclick="cancelarReservaConfirm('${r.codigo}')"><i class="fa-regular fa-circle-xmark"></i> Cancelar reserva</button>` : ''}
                            </div>
                        </td>
                    </tr>
            `;
        });

        tableHtml += `
                </tbody>
            </table>
        </div>
        `;
        listContainer.innerHTML = tableHtml;
    }

    verificarReservaRecienCreada();
}

let _verificacionModalMostrada = false;
function verificarReservaRecienCreada() {
    if (_verificacionModalMostrada) return;

    let recienCreada = null;
    try {
        const stored = sessionStorage.getItem('reserva_recien_creada');
        if (stored) recienCreada = JSON.parse(stored);
    } catch (_) {}

    const urlParams = new URLSearchParams(window.location.search);
    const codigoParam = urlParams.get('nuevaReserva') || urlParams.get('reservaExitosa') || (recienCreada ? recienCreada.codigo : null);

    if (codigoParam) {
        _verificacionModalMostrada = true;

        try {
            const cleanUrl = window.location.pathname;
            window.history.replaceState({}, document.title, cleanUrl);
        } catch (_) {}
        try {
            sessionStorage.removeItem('reserva_recien_creada');
        } catch (_) {}

        const codigo = codigoParam;
        const total = recienCreada ? recienCreada.total : null;
        const desglose = recienCreada ? recienCreada.desglose : null;
        const titulo = recienCreada ? recienCreada.titulo : null;

        if (typeof Swal !== 'undefined') {
            Swal.fire({
                title: '¡Reserva Confirmada con Éxito!',
                html: `
                    <div style="text-align:center; padding: 4px;">
                        <div style="width:64px; height:64px; background:#dcfce7; color:#15803d; border-radius:50%; display:inline-flex; align-items:center; justify-content:center; font-size:30px; margin-bottom:14px; box-shadow:0 4px 12px rgba(22,163,74,0.18);">
                            <i class="fa-solid fa-check"></i>
                        </div>
                        <h3 style="font-size:19px; color:#0f2942; margin:0 0 10px 0; font-weight:800;">¡Tu reserva ha sido procesada!</h3>
                        <p style="font-size:14px; color:#475569; margin-bottom:16px;">Se ha registrado correctamente en el sistema y ya está disponible aquí en tu lista de <strong>Mis Reservas</strong>.</p>
                        
                        <div style="background:#f8fafc; border:1px solid #e2e8f0; border-radius:12px; padding:16px; margin-bottom:16px; text-align:left;">
                            <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:8px; padding-bottom:8px; border-bottom:1px solid #e2e8f0;">
                                <span style="font-size:13px; color:#64748b; font-weight:600;">Código de reserva:</span>
                                <span style="font-size:15px; color:#196f3d; font-weight:800; font-family:monospace; background:#e8f5e9; padding:2px 8px; border-radius:6px;">${codigo}</span>
                            </div>
                            ${titulo ? `
                            <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:8px;">
                                <span style="font-size:13px; color:#64748b;">Tour:</span>
                                <strong style="font-size:13px; color:#1e293b; max-width:60%; text-align:right;">${titulo}</strong>
                            </div>` : ''}
                            ${desglose ? `
                            <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:8px;">
                                <span style="font-size:13px; color:#64748b;">Pasajeros:</span>
                                <span style="font-size:13px; color:#334155;">${desglose}</span>
                            </div>` : ''}
                            ${total ? `
                            <div style="display:flex; justify-content:space-between; align-items:center; border-top:1px dashed #cbd5e1; padding-top:8px; margin-top:8px;">
                                <span style="font-size:14px; font-weight:700; color:#334155;">Total pagado:</span>
                                <strong style="font-size:17px; color:#196f3d;">S/ ${Number(total).toFixed(2)}</strong>
                            </div>` : ''}
                        </div>
                        <p style="font-size:12px; color:#64748b; margin:0;"><i class="fa-solid fa-circle-info" style="color:#196f3d;"></i> Puedes hacer clic en "Ver detalles" para consultar los pasajeros o descargar tu voucher en PDF.</p>
                    </div>
                `,
                confirmButtonColor: '#196f3d',
                confirmButtonText: '<i class="fa-solid fa-check"></i> Entendido'
            });
        }

        setTimeout(() => {
            const filas = document.querySelectorAll('.reservas-table-modern tbody tr');
            filas.forEach(fila => {
                if (fila.textContent.includes(codigo)) {
                    fila.scrollIntoView({ behavior: 'smooth', block: 'center' });
                    fila.style.boxShadow = '0 0 0 3px #16a34a, 0 8px 16px rgba(22,163,74,0.15)';
                    fila.style.backgroundColor = '#f0fdf4';
                    fila.style.transition = 'all 0.5s ease';
                    setTimeout(() => {
                        fila.style.boxShadow = '';
                        fila.style.backgroundColor = '';
                    }, 5000);
                }
            });
        }, 350);
    }
}


// Nueva función de Modal Detalle de Reserva
window.abrirDetalleNuevo = function(codigo) {
    const user = window.getCurrentUser ? window.getCurrentUser() : null;
    const userName = user ? (user.nombre || user.usuario || 'Turista') : 'Turista';
    const userDoc = user ? (user.nroDocumento || '---') : '---';
    const list = getReservasData(user);
    const reserva = list.find(r => r.codigo === codigo) || list[0];

    if (!reserva) return;

    let overlay = document.getElementById('modal-detalle-avanzado');
    if (!overlay) {
        overlay = document.createElement('div');
        overlay.id = 'modal-detalle-avanzado';
        overlay.className = 'modal-detalles-overlay';
        document.body.appendChild(overlay);
    }

    const isCancelada = reserva.estado === 'Cancelada';
    const isCompletada = reserva.estado === 'Completada' || reserva.estado === 'Finalizada' || reserva.estado === 'Confirmada';
    let badgeHtml = '';
    if (isCancelada) badgeHtml = `<span class="res-badge-cancelada" style="font-size:12px; padding: 4px 10px;"><i class="fa-solid fa-xmark"></i> Cancelada</span>`;
    else if (isCompletada) badgeHtml = `<span class="res-badge-confirmada" style="font-size:12px; padding: 4px 10px;">${reserva.estado}</span>`;
    else badgeHtml = `<span class="res-badge-pendiente" style="font-size:12px; padding: 4px 10px;">Pendiente</span>`;

    // Pasajeros reales desde la base de datos
    let passRows = '';
    if (Array.isArray(reserva.pasajeros) && reserva.pasajeros.length > 0) {
        reserva.pasajeros.forEach((p, pIdx) => {
            const fullNom = `${p.nombre || ''} ${p.apellidoPaterno || ''} ${p.apellidoMaterno || ''}`.trim() || `Pasajero ${pIdx + 1}`;
            passRows += `
                <tr>
                    <td>${pIdx + 1}</td>
                    <td><strong>${fullNom}</strong></td>
                    <td>${p.nroDocumento || '---'}</td>
                    <td>${p.esTitular ? '<span class="badge-titular" style="background:#dcfce7; color:#166534; padding:2px 8px; border-radius:10px; font-size:11px; font-weight:600;">Titular</span>' : (p.tipo || 'Acompañante')}</td>
                    <td>Peruana</td>
                </tr>
            `;
        });
    } else {
        passRows = `
            <tr>
                <td>1</td>
                <td><strong>${userName}</strong></td>
                <td>${userDoc}</td>
                <td><span class="badge-titular" style="background:#dcfce7; color:#166534; padding:2px 8px; border-radius:10px; font-size:11px; font-weight:600;">Titular</span></td>
                <td>Peruana</td>
            </tr>
        `;
    }

    const cancelacionBannerHtml = isCancelada ? `
        <div style="background:#fef2f2; border:1px solid #fecaca; border-left:4px solid #dc2626; padding:12px 16px; border-radius:8px; margin-bottom:20px; color:#991b1b;">
            <div style="font-weight:700; display:flex; align-items:center; gap:8px; font-size:15px;">
                <i class="fa-solid fa-triangle-exclamation" style="color:#dc2626;"></i> Reserva Cancelada
            </div>
            <div style="margin-top:6px; font-size:13px; color:#7f1d1d;">
                <strong>Motivo de cancelación:</strong> ${reserva.motivoCancelacion || 'Cancelado por el usuario'}
            </div>
        </div>
    ` : '';

    overlay.innerHTML = `
        <div class="modal-detalles-card">
            <div class="md-header">
                <h2>Detalle de reserva</h2>
                <button class="md-close" onclick="cerrarDetalleNuevo()"><i class="fa-solid fa-xmark"></i></button>
            </div>
            
            <div class="md-body">
                ${cancelacionBannerHtml}
                <div class="md-top-grid">
                    <div class="md-main-info">
                        <img loading="lazy" src="${reserva.imagen || '../../img/valle.jpg'}" alt="Tour">
                        <div class="md-main-details">
                            <h3>${reserva.titulo} ${badgeHtml}</h3>
                            <div style="color: #64748b; font-size: 14px; display: flex; align-items: center; gap: 8px;"><i class="fa-solid fa-location-dot"></i> ${reserva.ubicacion || 'Cusco, Perú'}</div>
                            <div style="color: #64748b; font-size: 14px; display: flex; align-items: center; gap: 8px; margin-top: 5px;"><i class="fa-regular fa-calendar"></i> <div><span style="font-size:12px; display:block;">Fecha y hora de salida</span><span style="color:#0f172a;">${reserva.fechaSalida ? `${reserva.fechaSalida} - ${reserva.horaSalida || '08:00'}` : (reserva.fechas || 'Próximamente')}</span></div></div>
                            <div style="color: #64748b; font-size: 14px; display: flex; align-items: center; gap: 8px; margin-top: 5px;"><i class="fa-solid fa-user-group"></i> <div><span style="font-size:12px; display:block;">Pasajeros / Desglose</span><span style="color:#0f172a;">${reserva.desglose || reserva.personas}</span></div></div>
                        </div>
                    </div>
                    
                    <div class="md-right-card">
                        <div>
                            <span style="font-size: 12px; color: #64748b;">Código de reserva</span>
                            <div style="font-size: 20px; font-weight: 700; color: #0f172a;">${reserva.codigo}</div>
                        </div>
                        <div>
                            <span style="font-size: 12px; color: #64748b;">Agencia</span>
                            <div class="agency-col" style="margin-top: 5px;">
                                <div class="agency-icon"><i class="fa-solid fa-mountain"></i></div>
                                <span style="font-weight: 500; color: #0f172a;">${(reserva.agencia || '').replace('Agencia: ', '') || 'Andes Travel Perú'}</span>
                            </div>
                        </div>
                        <div style="margin-top: auto; padding-top: 15px; border-top: 1px solid #e2e8f0;">
                            <span style="font-size: 12px; color: #64748b;">Total pagado</span>
                            <div style="display: flex; justify-content: space-between; align-items: center;">
                                <div style="font-size: 20px; font-weight: 700; color: #0f172a;">${rFormattedTotal(reserva.total)}</div>
                                <span class="${isCancelada ? 'res-badge-cancelada' : 'res-badge-confirmada'}" style="font-size: 11px; padding: 2px 8px;">${isCancelada ? 'Cancelada' : 'Pagado'}</span>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="md-tabs">
                    <div class="md-tab active" onclick="cambiarTabDetalle(this, 'tab-pasajeros')"><i class="fa-solid fa-user"></i> Pasajeros</div>
                    <div class="md-tab" onclick="cambiarTabDetalle(this, 'tab-itinerario')"><i class="fa-regular fa-calendar-check"></i> Itinerario</div>
                    <div class="md-tab" onclick="cambiarTabDetalle(this, 'tab-detalles')"><i class="fa-solid fa-circle-info"></i> Detalles del viaje</div>
                </div>
                
                <div id="tab-pasajeros" class="md-tab-content active">
                    <h4 style="color: #0f172a; margin: 0 0 15px 0;">Información de los pasajeros</h4>
                    <table class="md-pass-table">
                        <thead>
                            <tr>
                                <th>#</th>
                                <th>Nombre completo</th>
                                <th>Documento</th>
                                <th>Tipo</th>
                                <th>Nacionalidad</th>
                            </tr>
                        </thead>
                        <tbody>
                            ${passRows}
                        </tbody>
                    </table>
                    
                    <div class="md-info-box">
                        <div style="display:flex; align-items:center; gap:8px; font-weight:600; color:#0f172a; margin-bottom:10px;"><i class="fa-solid fa-circle-info" style="color:#0284c7;"></i> Información adicional</div>
                        <div class="md-info-grid">
                            <div class="md-info-item">
                                <i class="fa-regular fa-credit-card"></i>
                                <div><p>Método de pago</p><span>${reserva.metodoPago || 'Tarjeta de crédito (**** 4587)'}</span></div>
                            </div>
                            <div class="md-info-item">
                                <i class="fa-regular fa-calendar"></i>
                                <div><p>Fecha de viaje</p><span>${reserva.fechas}</span></div>
                            </div>
                            <div class="md-info-item">
                                <i class="fa-solid fa-location-dot"></i>
                                <div><p>Ubicación</p><span>${reserva.ubicacion || 'Cusco, Perú'}</span></div>
                            </div>
                        </div>
                    </div>
                </div>
                
                <div id="tab-itinerario" class="md-tab-content">
                    <p style="color: #64748b;">Itinerario detallado del viaje próximamente disponible...</p>
                </div>
                
                <div id="tab-detalles" class="md-tab-content">
                    <p style="color: #64748b;">Recomendaciones, políticas y detalles extra de la agencia...</p>
                </div>
            </div>
            
            <div class="md-footer">
                <button class="md-btn md-btn-cerrar" onclick="cerrarDetalleNuevo()">Cerrar</button>
                <button class="md-btn" style="background:#0b1f38; color:white; border:none; padding:8px 14px; border-radius:6px; font-weight:600; cursor:pointer; display:inline-flex; align-items:center; gap:6px;" onclick="descargarVoucherPDF('${reserva.codigo}')"><i class="fa-solid fa-download"></i> Comprobante PDF</button>
                ${(!isCancelada && reserva.calificada) ? `
                    <span style="color:#b45309; font-weight:600; font-size:14px; display:inline-flex; align-items:center; gap:6px; margin-left:auto;">
                        <i class="fa-solid fa-star" style="color:#eab308;"></i> Experiencia ya calificada
                    </span>
                ` : (!isCancelada && (reserva.estado === 'Completada' || reserva.estado === 'Finalizada' || reserva.estado === 'Confirmada') ? `
                    <button class="md-btn md-btn-calificar" onclick="cerrarDetalleNuevo(); abrirModalCalificarReserva('${reserva.codigo}')">
                        <i class="fa-solid fa-star"></i> Calificar experiencia
                    </button>
                ` : '')}
            </div>
        </div>
    `;

    setTimeout(() => {
        overlay.classList.add('active');
    }, 10);
};

window.cerrarDetalleNuevo = function() {
    const overlay = document.getElementById('modal-detalle-avanzado');
    if (overlay) {
        overlay.classList.remove('active');
        setTimeout(() => overlay.remove(), 300);
    }
};

window.cambiarTabDetalle = function(tabElement, targetId) {
    document.querySelectorAll('.md-tab').forEach(t => t.classList.remove('active'));
    document.querySelectorAll('.md-tab-content').forEach(c => c.classList.remove('active'));
    tabElement.classList.add('active');
    document.getElementById(targetId).classList.add('active');
};

window.verDetallesReserva = window.abrirDetalleNuevo;

function rFormattedTotal(total) {
    if (typeof total === 'number') return 'S/ ' + total.toFixed(2);
    if (String(total).includes('S/')) return total;
    return 'S/ ' + total;
}

// Descargar Voucher PDF generado en el servidor con OpenPDF (FASE 9)
window.descargarVoucherPDF = function(codigo) {
    const list = getReservasData();
    const reserva = list.find(r => r.codigo === codigo);
    const idOrCodigo = reserva && (reserva.id || reserva.idReserva) ? (reserva.id || reserva.idReserva) : codigo;

    if (!idOrCodigo) {
        if (typeof Swal !== 'undefined') {
            Swal.fire({ icon: 'warning', title: 'Atención', text: 'No se encontró la reserva para descargar el comprobante.' });
        } else {
            alert('No se encontró la reserva.');
        }
        return;
    }

    // Descarga directa del comprobante PDF oficial desde el servidor
    const url = `/api/turista/reservas/${encodeURIComponent(idOrCodigo)}/comprobante.pdf`;
    const link = document.createElement('a');
    link.href = url;
    link.target = '_blank';
    link.download = `Comprobante_Reserva_${codigo || idOrCodigo}.pdf`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
};

// ==============================
// Modal Calificar Experiencia (Image 4)
// ==============================
let currentSelectedRating = 0;
let calificacionContext = { idReserva: 1, idAgencia: 1 };

document.addEventListener('DOMContentLoaded', () => {
    initStarsRating();
});

function initStarsRating() {
    const stars = document.querySelectorAll('#stars-container .star-icon-btn');
    stars.forEach(star => {
        star.addEventListener('mouseenter', () => {
            const val = parseInt(star.getAttribute('data-value'));
            highlightStars(val, 'hovered');
        });

        star.addEventListener('mouseleave', () => {
            clearStarsState('hovered');
        });

        star.addEventListener('click', () => {
            currentSelectedRating = parseInt(star.getAttribute('data-value'));
            highlightStars(currentSelectedRating, 'selected');
        });
    });

    const overlay = document.getElementById('modal-calificar');
    if (overlay) {
        overlay.addEventListener('click', (e) => {
            if (e.target === overlay) {
                cerrarModalCalificar();
            }
        });
    }
}

function highlightStars(rating, className) {
    const stars = document.querySelectorAll('#stars-container .star-icon-btn');
    stars.forEach(star => {
        const val = parseInt(star.getAttribute('data-value'));
        if (val <= rating) {
            star.classList.add(className);
            if (className === 'selected') {
                star.classList.remove('fa-regular');
                star.classList.add('fa-solid');
            }
        } else {
            star.classList.remove(className);
            if (className === 'selected') {
                star.classList.remove('fa-solid');
                star.classList.add('fa-regular');
            }
        }
    });
}

function clearStarsState(className) {
    const stars = document.querySelectorAll('#stars-container .star-icon-btn');
    stars.forEach(star => {
        star.classList.remove(className);
    });
}

window.abrirModalCalificarReserva = function(codigo) {
    const list = getReservasData();
    const reserva = list.find(r => r.codigo === codigo || String(r.id) === String(codigo));
    if (!reserva) return;
    abrirModalCalificar(
        reserva.titulo,
        reserva.ubicacion,
        reserva.agencia,
        reserva.imagen,
        reserva.id || reserva.idReserva || 1,
        reserva.idAgencia || 1,
        reserva.codigo || codigo
    );
};

window.abrirModalCalificar = function(titulo, ubicacion, agencia, imagen, idReserva = 1, idAgencia = 1, codigo = '') {
    const modal = document.getElementById('modal-calificar');
    if (!modal) return;

    calificacionContext.idReserva = idReserva;
    calificacionContext.idAgencia = idAgencia;
    calificacionContext.codigo = codigo;

    if (titulo) document.getElementById('modal-tour-name').innerText = titulo;
    if (ubicacion) document.getElementById('modal-tour-location').innerText = ubicacion;
    if (agencia) document.getElementById('modal-tour-agency').innerText = (agencia || '').replace('Agencia: ', '');
    if (imagen) document.getElementById('modal-img-banner').src = imagen;

    currentSelectedRating = 0;
    highlightStars(0, 'selected');
    clearStarsState('hovered');

    const textarea = document.getElementById('modal-comment-input');
    if (textarea) textarea.value = '';
    const counter = document.getElementById('modal-char-count');
    if (counter) counter.innerText = '0/500';

    modal.classList.add('active');
};

window.cerrarModalCalificar = function() {
    const modal = document.getElementById('modal-calificar');
    if (modal) modal.classList.remove('active');
};

window.actualizarContadorModal = function(textarea) {
    const counter = document.getElementById('modal-char-count');
    if (counter && textarea) {
        counter.innerText = `${textarea.value.length}/500`;
    }
};

let enviandoCalificacionLock = false;
window.enviarCalificacion = function() {
    if (enviandoCalificacionLock) return;

    if (currentSelectedRating === 0) {
        if (typeof Swal !== 'undefined') {
            Swal.fire({
                icon: 'warning',
                title: 'Selecciona una calificación',
                text: 'Por favor asigna de 1 a 5 estrellas a tu experiencia.',
                confirmButtonColor: '#196f3d'
            });
        } else {
            alert('Por favor asigna de 1 a 5 estrellas.');
        }
        return;
    }

    const textarea = document.getElementById('modal-comment-input');
    const comentario = textarea ? textarea.value.trim() : '';
    const user = window.getCurrentUser ? window.getCurrentUser() : null;
    const idUsuario = user && user.idUsuario ? user.idUsuario : null;

    const payload = {
        idReserva: calificacionContext.idReserva,
        idAgencia: calificacionContext.idAgencia,
        codigo: calificacionContext.codigo || '',
        codigoReserva: calificacionContext.codigo || '',
        estrellas: currentSelectedRating,
        comentario: comentario
    };
    if (idUsuario) payload.idUsuario = idUsuario;

    enviandoCalificacionLock = true;
    const btnSubmit = document.querySelector('.btn-modal-submit');
    if (btnSubmit) { btnSubmit.disabled = true; btnSubmit.innerText = 'Enviando calificación...'; }

    // CSRF token helper
    const xsrfMatch = document.cookie.match(/XSRF-TOKEN=([^;]+)/);
    const xsrfToken = xsrfMatch ? decodeURIComponent(xsrfMatch[1]) : '';

    fetch('/api/calificar', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-XSRF-TOKEN': xsrfToken
        },
        credentials: 'include',
        body: JSON.stringify(payload)
    })
    .then(async (response) => {
        const data = await response.json().catch(() => ({}));
        if (!response.ok || data.status !== 'success') {
            throw new Error(data.message || 'Error al guardar calificación');
        }
        cerrarModalCalificar();

        // Marcar reserva como calificada localmente
        const key = getUserReservasKey();
        let list = getReservasData();
        list.forEach(r => {
            if (r.codigo === calificacionContext.codigo || String(r.id) === String(calificacionContext.idReserva)) {
                r.calificada = true;
            }
        });
        localStorage.setItem(key, JSON.stringify(list));
        renderReservasPage();

        if (typeof Swal !== 'undefined') {
            Swal.fire({
                icon: 'success',
                title: '¡Gracias por tu opinión!',
                text: `Tu calificación (${currentSelectedRating} estrellas) ha sido registrada exitosamente.`,
                confirmButtonColor: '#196f3d'
            }).then(() => {
                cargarReservasDesdeDB();
            });
        } else {
            alert(`¡Gracias por tu opinión! Tu calificación (${currentSelectedRating} estrellas) ha sido registrada exitosamente.`);
            cargarReservasDesdeDB();
        }
    })
    .catch(error => {
        console.error('Error al enviar la calificación:', error);
        if (typeof Swal !== 'undefined') {
            Swal.fire({
                icon: 'error',
                title: 'No se pudo guardar la calificación',
                text: error.message || 'Ocurrió un error al enviar tu calificación.',
                confirmButtonColor: '#0b1f38'
            });
        } else {
            alert(error.message || 'Error al enviar calificación.');
        }
    })
    .finally(() => {
        enviandoCalificacionLock = false;
        if (btnSubmit) { btnSubmit.disabled = false; btnSubmit.innerText = 'Enviar calificación'; }
    });
};

window.cancelarReservaConfirm = function(codigo) {
    let overlay = document.getElementById('modal-cancelar-reserva');
    if (!overlay) {
        overlay = document.createElement('div');
        overlay.id = 'modal-cancelar-reserva';
        overlay.className = 'modal-detalles-overlay';
        document.body.appendChild(overlay);
    }

    overlay.innerHTML = `
        <div class="modal-detalles-card" style="width: 520px; max-width: 95%;">
            <div class="md-header" style="border-bottom: 1px solid #fee2e2; background:#fef2f2;">
                <h2 style="color:#b91c1c; font-size:18px; display:flex; align-items:center; gap:8px;">
                    <i class="fa-solid fa-triangle-exclamation"></i> Cancelar Reserva ${codigo}
                </h2>
                <button class="md-close" onclick="cerrarModalCancelar()"><i class="fa-solid fa-xmark"></i></button>
            </div>
            <div class="md-body" style="padding: 24px;">
                <p style="color:#334155; font-size:14px; margin-top:0;">
                    Estás a punto de cancelar tu reserva <strong>${codigo}</strong>. Los cupos reservados se devolverán a la disponibilidad del tour.
                </p>
                <div style="margin-top:16px;">
                    <label style="display:block; font-weight:600; font-size:13px; color:#0f172a; margin-bottom:6px;">
                        Motivo de cancelación *
                    </label>
                    <select id="selectMotivoCancelacion" style="width:100%; padding:10px 12px; border:1px solid #cbd5e1; border-radius:8px; font-size:14px; background:white;" onchange="onCambioMotivoCancelacion(this)">
                        <option value="" disabled selected>-- Selecciona un motivo --</option>
                        <option value="Cambio de planes">Cambio de planes</option>
                        <option value="Problemas de salud">Problemas de salud</option>
                        <option value="Error en la reserva">Error en la reserva</option>
                        <option value="Encontré otra opción">Encontré otra opción</option>
                        <option value="Problemas de pago">Problemas de pago</option>
                        <option value="Otro">Otro (especificar)</option>
                    </select>
                </div>
                <div id="boxOtroMotivo" style="display:none; margin-top:14px;">
                    <label style="display:block; font-weight:600; font-size:13px; color:#0f172a; margin-bottom:6px;">
                        Especifica el motivo * <span style="font-size:11px; color:#64748b; font-weight:normal;">(Entre 5 y 255 caracteres)</span>
                    </label>
                    <textarea id="txtOtroMotivo" rows="3" maxlength="255" placeholder="Escribe el motivo detallado de la cancelación..." style="width:100%; padding:10px 12px; border:1px solid #cbd5e1; border-radius:8px; font-size:14px; font-family:inherit; resize:vertical;"></textarea>
                    <small id="otroMotivoHelp" style="display:block; margin-top:4px; font-size:12px; color:#64748b;"></small>
                </div>
            </div>
            <div class="md-footer" style="padding: 16px 24px; background:#f8fafc; border-top:1px solid #e2e8f0; display:flex; justify-content:flex-end; gap:10px;">
                <button class="md-btn md-btn-cerrar" onclick="cerrarModalCancelar()" style="padding:8px 16px;">Volver</button>
                <button id="btnEjecutarCancelacion" class="md-btn" style="background:#dc2626; color:white; border:none; padding:8px 18px; border-radius:6px; font-weight:600; cursor:pointer;" onclick="ejecutarCancelacionReserva('${codigo}')">
                    <i class="fa-solid fa-xmark"></i> Confirmar cancelación
                </button>
            </div>
        </div>
    `;

    setTimeout(() => overlay.classList.add('active'), 10);
};

window.cerrarModalCancelar = function() {
    const overlay = document.getElementById('modal-cancelar-reserva');
    if (overlay) {
        overlay.classList.remove('active');
        setTimeout(() => overlay.remove(), 300);
    }
};

window.onCambioMotivoCancelacion = function(sel) {
    const boxOtro = document.getElementById('boxOtroMotivo');
    sel.classList.remove('campo-vacio', 'novalido', 'valido');
    if (sel.value === 'Otro') {
        if (boxOtro) boxOtro.style.display = 'block';
        const txt = document.getElementById('txtOtroMotivo');
        if (txt) {
            txt.focus();
            if (!txt.value.trim()) txt.classList.add('campo-vacio');
        }
    } else {
        if (boxOtro) boxOtro.style.display = 'none';
        sel.classList.add('valido');
    }
};

window.ejecutarCancelacionReserva = async function(codigo) {
    const sel = document.getElementById('selectMotivoCancelacion');
    const boxOtro = document.getElementById('boxOtroMotivo');
    const txt = document.getElementById('txtOtroMotivo');
    const btn = document.getElementById('btnEjecutarCancelacion');

    let motivo = sel ? sel.value.trim() : '';
    if (!motivo) {
        if (sel) sel.classList.add('campo-vacio');
        alert('Por favor selecciona un motivo de cancelación.');
        return;
    }

    if (motivo === 'Otro') {
        const otroTexto = txt ? txt.value.trim() : '';
        if (!otroTexto) {
            if (txt) { txt.classList.remove('novalido'); txt.classList.add('campo-vacio'); txt.focus(); }
            alert('Por favor escribe el motivo de la cancelación en el cuadro de texto.');
            return;
        }
        if (otroTexto.length < 5) {
            if (txt) { txt.classList.remove('campo-vacio'); txt.classList.add('novalido'); txt.focus(); }
            alert('El motivo debe tener al menos 5 caracteres.');
            return;
        }
        if (txt) { txt.classList.remove('campo-vacio', 'novalido'); txt.classList.add('valido'); }
        motivo = otroTexto;
    }

    if (btn) { btn.disabled = true; btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Cancelando...'; }

    try {
        const fetchFn = window.apiFetch || fetch;
        const res = await fetchFn('/api/cancelar_reserva', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ codigo: codigo, motivo: motivo })
        });
        const data = await res.json().catch(() => ({}));

        if (res.ok && data.status === 'success') {
            cerrarModalCancelar();
            if (typeof Swal !== 'undefined') {
                Swal.fire({
                    icon: 'success',
                    title: 'Reserva Cancelada',
                    text: 'La reserva ' + codigo + ' ha sido cancelada correctamente y los cupos han sido restaurados.',
                    confirmButtonColor: '#196f3d'
                }).then(() => {
                    cargarReservasDesdeDB();
                });
            } else {
                alert('Reserva ' + codigo + ' cancelada correctamente.');
                cargarReservasDesdeDB();
            }
        } else {
            const msg = data.message || data.error || 'No se pudo cancelar la reserva.';
            if (typeof Swal !== 'undefined') {
                Swal.fire({ icon: 'error', title: 'No se pudo cancelar', text: msg, confirmButtonColor: '#0b1f38' });
            } else {
                alert('Error: ' + msg);
            }
            if (btn) { btn.disabled = false; btn.innerHTML = '<i class="fa-solid fa-xmark"></i> Confirmar cancelación'; }
        }
    } catch (err) {
        console.error('Error al cancelar:', err);
        alert('Ocurrió un error al cancelar la reserva: ' + err.message);
        if (btn) { btn.disabled = false; btn.innerHTML = '<i class="fa-solid fa-xmark"></i> Confirmar cancelación'; }
    }
};


