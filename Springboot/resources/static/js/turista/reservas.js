// Travelink - Reservas Dynamic Management & PDF Voucher Generator (User Scoped)

const ITEMS_PER_PAGE = 5;
let currentPage = 1;

// Las reservas se cargan desde la API y no se mezclan con datos de demostración.

function getUserReservasKey() {
    const user = window.getCurrentUser ? window.getCurrentUser() : null;
    const email = user && (user.email || user.correo);
    return 'travelink_reservas_' + (email || 'guest');
}

function getReservasData() {
    const user = window.getCurrentUser ? window.getCurrentUser() : null;
    if (!user || !(user.email || user.correo)) return [];
    try {
        const parsed = JSON.parse(localStorage.getItem(getUserReservasKey()) || '[]');
        return Array.isArray(parsed) ? parsed : [];
    } catch (e) {
        return [];
    }
}

let filtroReservasActual = 'activas';

document.addEventListener('DOMContentLoaded', () => {
    // Quitar la tarjeta y tabla de pasajeros de ejemplo que venían escritas en el HTML.
    document.querySelector('.reserva-main-card')?.remove();
    const tabActivas = document.getElementById('tab-activas');
    const tabHistorial = document.getElementById('tab-historial');
    if (tabActivas && tabHistorial) {
        tabActivas.classList.add('active');
        tabHistorial.classList.remove('active');
        tabActivas.style.cursor = tabHistorial.style.cursor = 'pointer';
        tabActivas.addEventListener('click', () => {
            filtroReservasActual = 'activas';
            tabActivas.classList.add('active'); tabHistorial.classList.remove('active');
            renderReservasPage();
        });
        tabHistorial.addEventListener('click', () => {
            filtroReservasActual = 'historial';
            tabHistorial.classList.add('active'); tabActivas.classList.remove('active');
            renderReservasPage();
        });
    }
    cargarReservasDesdeDB();
});

function cargarReservasDesdeDB() {
    const user = window.getCurrentUser ? window.getCurrentUser() : null;
    const key = getUserReservasKey();
    if (!user || !(user.email || user.correo)) {
        localStorage.setItem(key, JSON.stringify([]));
        renderReservasPage();
        return;
    }
    const email = user.email || user.correo;
    const idUsuario = Number(user.idUsuario || user.id || 0);
    fetch(`http://localhost:8080/api/obtener_reservas?correo=${encodeURIComponent(email)}&idUsuario=${encodeURIComponent(idUsuario)}`)
        .then(res => res.json())
        .then(data => {
            if (data.status === 'success' && Array.isArray(data.reservas)) {
                // La base de datos es la fuente de verdad; no mezclar datos demo antiguos.
                localStorage.setItem(key, JSON.stringify(data.reservas));
            } else {
                localStorage.setItem(key, JSON.stringify([]));
                console.error('La API no devolvió las reservas de esta cuenta:', data.message || data);
            }
        })
        .catch(err => {
            localStorage.setItem(key, JSON.stringify([]));
            console.error('No se pudieron cargar reservas desde la DB:', err);
        })
        .finally(() => renderReservasPage());
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

    const reservasVisibles = allReservas.filter(r => {
        const estado = String(r.estado || '').toLowerCase();
        if (filtroReservasActual === 'activas') return ['pendiente', 'confirmada', 'completada'].includes(estado);
        return ['finalizada', 'cancelada', 'cancelado'].includes(estado);
    });
    const totalItems = reservasVisibles.length;
    const totalPages = Math.ceil(totalItems / ITEMS_PER_PAGE) || 1;

    if (currentPage > totalPages) currentPage = totalPages;
    if (currentPage < 1) currentPage = 1;

    const startIndex = (currentPage - 1) * ITEMS_PER_PAGE;
    const pageItems = reservasVisibles.slice(startIndex, startIndex + ITEMS_PER_PAGE);

    listContainer.innerHTML = '';

    if (pageItems.length === 0) {
        listContainer.innerHTML = `
            <div style="text-align:center; padding: 50px 20px; background: white; border-radius: 16px; border: 1px solid #e2e8f0;">
                <i class="fa-regular fa-calendar-xmark" style="font-size: 48px; color: #94a3b8; margin-bottom: 15px;"></i>
                <h3 style="color: #0b1f38;">${filtroReservasActual === 'activas' ? 'No tienes reservas activas' : 'No tienes reservas en el historial'}</h3>
                <p style="color: #64748b;">Las reservas que correspondan a esta sección aparecerán aquí cuando se carguen desde la base de datos.</p>
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
}


// Nueva función de Modal Detalle de Reserva
window.abrirDetalleNuevo = function(codigo) {
    const list = getReservasData();
    const reserva = list.find(r => String(r.codigo) === String(codigo));

    if (!reserva) {
        alert('No se encontró esa reserva en los datos de tu cuenta. Actualiza la página e inténtalo nuevamente.');
        return;
    }

    let overlay = document.getElementById('modal-detalle-avanzado');
    if (!overlay) {
        overlay = document.createElement('div');
        overlay.id = 'modal-detalle-avanzado';
        overlay.className = 'modal-detalles-overlay';
        document.body.appendChild(overlay);
    }

    const isCompletada = reserva.estado === 'Completada' || reserva.estado === 'Finalizada' || reserva.estado === 'Confirmada';
    const badgeHtml = isCompletada 
        ? `<span class="res-badge-confirmada" style="font-size:12px; padding: 4px 10px;">Confirmada</span>` 
        : `<span class="res-badge-pendiente" style="font-size:12px; padding: 4px 10px;">Pendiente</span>`;

    overlay.innerHTML = `
        <div class="modal-detalles-card">
            <div class="md-header">
                <h2>Detalle de reserva</h2>
                <button class="md-close" onclick="cerrarDetalleNuevo()"><i class="fa-solid fa-xmark"></i></button>
            </div>
            
            <div class="md-body">
                <div class="md-top-grid">
                    <div class="md-main-info">
                        <img loading="lazy" src="${reserva.imagen || '../../img/valle.jpg'}" alt="Tour">
                        <div class="md-main-details">
                            <h3>${reserva.titulo} ${badgeHtml}</h3>
                            <div style="color: #64748b; font-size: 14px; display: flex; align-items: center; gap: 8px;"><i class="fa-solid fa-location-dot"></i> ${reserva.ubicacion || 'Cusco, Perú'}</div>
                            <div style="color: #64748b; font-size: 14px; display: flex; align-items: center; gap: 8px; margin-top: 5px;"><i class="fa-regular fa-calendar"></i> <div><span style="font-size:12px; display:block;">Fecha de reserva</span><span style="color:#0f172a;">${reserva.fechaRegistro || 'No disponible'} - ${(reserva.fechas || '').split(' - ')[1] || (reserva.fechas || 'No disponible')}</span></div></div>
                            <div style="color: #64748b; font-size: 14px; display: flex; align-items: center; gap: 8px; margin-top: 5px;"><i class="fa-solid fa-user-group"></i> <div><span style="font-size:12px; display:block;">Personas</span><span style="color:#0f172a;">${reserva.personas}</span></div></div>
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
                                <span class="res-badge-confirmada" style="font-size: 11px; padding: 2px 8px;">Pagado</span>
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
                            </tr>
                        </thead>
                        <tbody>
                            ${(Array.isArray(reserva.pasajeros) && reserva.pasajeros.length) ? reserva.pasajeros.map((p, i) => `
                            <tr>
                                <td>${i + 1}</td>
                                <td>${[p.nombre, p.apellidos].filter(Boolean).join(' ')}</td>
                                <td>${p.documento || 'No registrado'}</td>
                                <td>${p.titular ? 'Titular' : 'Acompañante'}</td>
                            </tr>`).join('') : '<tr><td colspan="4">No hay datos de pasajeros disponibles para esta reserva.</td></tr>'}
                        </tbody>
                    </table>
                    
                    <div class="md-info-box">
                        <div style="display:flex; align-items:center; gap:8px; font-weight:600; color:#0f172a; margin-bottom:10px;"><i class="fa-solid fa-circle-info" style="color:#0284c7;"></i> Información adicional</div>
                        <div class="md-info-grid">
                            <div class="md-info-item">
                                <i class="fa-regular fa-credit-card"></i>
                                <div><p>Método de pago</p><span>${reserva.metodoPago || 'No registrado'}</span></div>
                            </div>
                            <div class="md-info-item">
                                <i class="fa-regular fa-calendar"></i>
                                <div><p>Fecha de viaje</p><span>${reserva.fechas || 'No disponible'}</span></div>
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
                ${reserva.calificada ? `
                    <span style="color:#b45309; font-weight:600; font-size:14px; display:inline-flex; align-items:center; gap:6px; margin-left:auto;">
                        <i class="fa-solid fa-star" style="color:#eab308;"></i> Experiencia ya calificada
                    </span>
                ` : ((reserva.estado === 'Completada' || reserva.estado === 'Finalizada' || reserva.estado === 'Confirmada') ? `
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
    const user = window.getCurrentUser ? window.getCurrentUser() : null;
    const reserva = list.find(r => String(r.codigo) === String(codigo));

    if (!idOrCodigo) {
        if (typeof Swal !== 'undefined') {
            Swal.fire({ icon: 'warning', title: 'Atención', text: 'No se encontró la reserva para descargar el comprobante.' });
        } else {
            alert('No se encontró la reserva.');
        }
        return;
    }

    const pdfWindow = window.open('', '_blank');
    const voucherContent = `
        <!DOCTYPE html>
        <html lang="es">
        <head>
            <meta charset="UTF-8">
            <title>Voucher - ${reserva.codigo}</title>
            <style>
                body { font-family: 'Helvetica Neue', Arial, sans-serif; padding: 40px; color: #1e293b; background: #fff; }
                .header { display: flex; justify-content: space-between; align-items: center; border-bottom: 3px solid #ffc107; padding-bottom: 20px; }
                .logo { font-size: 28px; font-weight: bold; color: #0b1f38; }
                .logo span { color: #ffc107; }
                .voucher-title { text-align: right; }
                .voucher-title h2 { margin: 0; color: #0b1f38; font-size: 20px; }
                .badge { background: #dcfce7; color: #15803d; padding: 4px 12px; border-radius: 12px; font-weight: bold; font-size: 12px; }
                .section-box { background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 10px; padding: 20px; margin-top: 25px; }
                .grid { display: grid; grid-template-columns: 1fr 1fr; gap: 15px; }
                .field-label { font-size: 12px; color: #64748b; font-weight: bold; text-transform: uppercase; }
                .field-val { font-size: 15px; font-weight: 600; color: #0f172a; margin-top: 2px; }
                .table-summary { width: 100%; border-collapse: collapse; margin-top: 25px; }
                .table-summary th { background: #0b1f38; color: white; padding: 12px; text-align: left; }
                .table-summary td { padding: 12px; border-bottom: 1px solid #e2e8f0; }
                .footer-stamp { margin-top: 40px; text-align: center; border-top: 1px solid #e2e8f0; padding-top: 20px; color: #94a3b8; font-size: 12px; }
                @media print {
                    .no-print { display: none; }
                }
            </style>
        </head>
        <body>
            <div class="no-print" style="margin-bottom: 20px; text-align: right;">
                <button onclick="window.print()" style="background: #0b1f38; color: white; border: none; padding: 10px 20px; border-radius: 6px; font-weight: bold; cursor: pointer;">Imprimir / Guardar PDF</button>
            </div>
            <div class="header">
                <div class="logo">Travel<span>ink</span></div>
                <div class="voucher-title">
                    <h2>VOUCHER DE RESERVA</h2>
                    <span class="badge">ESTADO: ${reserva.estado.toUpperCase()}</span>
                </div>
            </div>

            <div class="section-box">
                <div class="grid">
                    <div>
                        <div class="field-label">Código de Reserva</div>
                        <div class="field-val" style="color:#0284c7; font-size:18px;">${reserva.codigo}</div>
                    </div>
                    <div>
                        <div class="field-label">Titular de Reserva</div>
                        <div class="field-val">${user ? (user.username || user.nombre || 'Usuario') + ' (' + (user.email || user.correo || '') + ')' : 'Usuario no identificado'}</div>
                    </div>
                    <div>
                        <div class="field-label">Tour / Experiencia</div>
                        <div class="field-val">${reserva.titulo}</div>
                    </div>
                    <div>
                        <div class="field-label">Ubicación</div>
                        <div class="field-val">${reserva.ubicacion || 'Cusco, Perú'}</div>
                    </div>
                    <div>
                        <div class="field-label">Fechas del Viaje</div>
                        <div class="field-val">${reserva.fechas || 'No disponible'}</div>
                    </div>
                    <div>
                        <div class="field-label">Pasajeros</div>
                        <div class="field-val">${reserva.personas}</div>
                    </div>
                </div>
            </div>

            <table class="table-summary">
                <thead>
                    <tr>
                        <th>Descripción</th>
                        <th>Método de Pago</th>
                        <th style="text-align:right;">Monto Total</th>
                    </tr>
                </thead>
                <tbody>
                    <tr>
                        <td>Reserva de paquete turístico (${reserva.titulo})</td>
                        <td>${reserva.metodoPago || 'Yape / Plin'}</td>
                        <td style="text-align:right; font-weight:bold; font-size:16px;">${rFormattedTotal(reserva.total)}</td>
                    </tr>
                </tbody>
            </table>

            <div class="footer-stamp">
                <p>Este voucher sirve como comprobante oficial de reserva emitido por Travelink.</p>
                <p>2026 Travelink Perú - Todos los derechos reservados.</p>
            </div>

            <script>
                window.onload = function() {
                    setTimeout(function() { window.print(); }, 500);
                }
            </script>
        </body>
        </html>
    `;

    pdfWindow.document.write(voucherContent);
    pdfWindow.document.close();
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
    const idUsuario = user ? Number(user.idUsuario || user.id || 0) : 0;
    if (!idUsuario) {
        alert('No se pudo identificar tu sesión. Cierra sesión e inicia sesión nuevamente antes de calificar.');
        return;
    }

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
    if (typeof Swal !== 'undefined') {
        Swal.fire({
            title: '¿Estás seguro?',
            text: 'Esta acción cancelará tu reserva ' + codigo + ' y devolverá los cupos a la disponibilidad del tour.',
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#dc2626',
            cancelButtonColor: '#64748b',
            confirmButtonText: 'Sí, cancelar reserva',
            cancelButtonText: 'No, mantener'
        }).then(async (result) => {
            if (result.isConfirmed) {
                try {
                    const res = await fetch('/api/cancelar_reserva', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({ codigo: codigo, motivo: 'Cancelado por el usuario desde Mis Reservas' })
                    });
                    const data = await res.json();
                    if (res.ok && data.status === 'success') {
                        // Actualizar en localStorage
                        const key = getUserReservasKey();
                        let list = getReservasData();
                        list.forEach(r => { if (r.codigo === codigo) r.estado = 'Cancelada'; });
                        localStorage.setItem(key, JSON.stringify(list));

                        Swal.fire({
                            icon: 'success',
                            title: 'Reserva Cancelada',
                            text: 'La reserva ' + codigo + ' ha sido cancelada correctamente y los cupos han sido restaurados.',
                            confirmButtonColor: '#196f3d'
                        }).then(() => {
                            cargarReservasDesdeDB();
                        });
                    } else {
                        throw new Error(data.message || 'Error al cancelar la reserva');
                    }
                } catch (e) {
                    Swal.fire({ icon: 'error', title: 'Error', text: e.message, confirmButtonColor: '#0b1f38' });
                }
            }
        });
    } else {
        if (confirm('¿Estás seguro de cancelar la reserva ' + codigo + '?')) {
            fetch('/api/cancelar_reserva', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ codigo: codigo, motivo: 'Cancelado por el usuario desde Mis Reservas' })
            }).then(() => cargarReservasDesdeDB());
        }
    }
};


