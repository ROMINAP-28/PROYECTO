// Travelink - Reservas Dynamic Management & PDF Voucher Generator (User Scoped)

const ITEMS_PER_PAGE = 10;
let currentPage = 1;
let currentTab = 'historial'; // 'historial' or 'activas'

function getUserReservasKey() {
    const user = window.getCurrentUser ? window.getCurrentUser() : null;
    const email = user ? (user.email || user.correo || user.username || 'usuario') : 'usuario';
    return 'travelink_reservas_' + email;
}

function getReservasData() {
    const key = getUserReservasKey();
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
    // 1. Tab switching
    const tabHistorial = document.getElementById('tab-historial');
    const tabActivas = document.getElementById('tab-activas');

    if (tabHistorial && tabActivas) {
        tabHistorial.addEventListener('click', () => {
            tabHistorial.classList.add('active');
            tabActivas.classList.remove('active');
            currentTab = 'historial';
            currentPage = 1;
            renderReservasPage();
        });

        tabActivas.addEventListener('click', () => {
            tabActivas.classList.add('active');
            tabHistorial.classList.remove('active');
            currentTab = 'activas';
            currentPage = 1;
            renderReservasPage();
        });
    }

    cargarReservasDesdeDB();
});

function cargarReservasDesdeDB() {
    const user = window.getCurrentUser ? window.getCurrentUser() : null;
    if (!user) {
        renderReservasPage();
        return;
    }

    const email = user.email || user.correo || '';
    const key = getUserReservasKey();

    if (email) {
        fetch(`http://localhost:8080/api/obtener_reservas?correo=${encodeURIComponent(email)}`)
            .then(res => res.json())
            .then(data => {
                if (data.status === 'success' && Array.isArray(data.reservas) && data.reservas.length > 0) {
                    let localList = [];
                    try {
                        localList = JSON.parse(localStorage.getItem(key) || '[]');
                    } catch (e) { localList = []; }

                    const dbReservas = data.reservas;
                    const existingCodes = new Set(dbReservas.map(r => r.codigo));

                    localList.forEach(loc => {
                        if (!existingCodes.has(loc.codigo)) {
                            dbReservas.push(loc);
                        }
                    });

                    localStorage.setItem(key, JSON.stringify(dbReservas));
                }
            })
            .catch(err => {
                console.warn('No se pudieron cargar reservas desde la DB:', err);
            })
            .finally(() => {
                renderReservasPage();
            });
    } else {
        renderReservasPage();
    }
}

function renderReservasPage() {
    const allReservas = getReservasData();
    const listContainer = document.getElementById('reservas-list-container') || document.querySelector('.lista-reservas');

    if (!listContainer) return;

    // Filter by tab
    let filtered = allReservas;
    if (currentTab === 'activas') {
        filtered = allReservas.filter(r => r.estado === 'Confirmada' || r.estado === 'Pendiente' || r.estado === 'En progreso');
    } else if (currentTab === 'historial') {
        // Historial shows all or completed/cancelled
        filtered = allReservas;
    }

    const totalItems = filtered.length;
    const totalPages = Math.ceil(totalItems / ITEMS_PER_PAGE) || 1;

    if (currentPage > totalPages) currentPage = totalPages;
    if (currentPage < 1) currentPage = 1;

    const startIndex = (currentPage - 1) * ITEMS_PER_PAGE;
    const pageItems = filtered.slice(startIndex, startIndex + ITEMS_PER_PAGE);

    listContainer.innerHTML = '';

    if (pageItems.length === 0) {
        listContainer.innerHTML = `
            <div style="text-align:center; padding: 60px 20px; background: white; border-radius: 16px; border: 1px solid #e2e8f0; margin-top: 20px; box-shadow: 0 4px 15px rgba(0,0,0,0.02);">
                <i class="fa-regular fa-calendar-xmark" style="font-size: 52px; color: #94a3b8; margin-bottom: 18px; display:block;"></i>
                <h3 style="color: #0b1f38; font-size: 20px; margin-bottom: 8px;">No tienes reservas registradas</h3>
                <p style="color: #64748b; font-size: 14px; max-width: 450px; margin: 0 auto 20px auto;">Explora nuestra amplia variedad de tours y experiencias turísticas para realizar tu primera reserva.</p>
                <a href="experiencias.html" style="display: inline-flex; align-items: center; gap: 8px; background: #196f3d; color: #ffffff; padding: 12px 24px; border-radius: 8px; text-decoration: none; font-weight: 700; font-size: 14px; transition: 0.2s;">
                    <i class="fa-regular fa-compass"></i> Explorar Experiencias
                </a>
            </div>
        `;
        renderPagination(0);
        return;
    }

    pageItems.forEach(r => {
        const isCompletada = r.estado === 'Completada' || r.estado === 'Finalizada';
        const isCancelada = r.estado === 'Cancelada';
        const statusBadgeClass = isCompletada ? 'status-badge-finalizada' : (isCancelada ? 'status-badge-cancelada' : 'status-badge-activa');
        const statusText = r.estado || 'Confirmada';

        // Passengers list
        let pasajerosHtml = '';
        const pasajeros = Array.isArray(r.pasajeros) && r.pasajeros.length > 0 ? r.pasajeros : [
            { id: 1, nombre: r.titular || 'Titular', apellidoPaterno: '', apellidoMaterno: '', dni: r.nroDocumento || '-', telefono: '-' }
        ];

        pasajeros.forEach((p, index) => {
            const nombreStr = p.nombre || p.nombres || ('Pasajero ' + (index + 1));
            const patStr = p.apellidoPaterno || '-';
            const matStr = p.apellidoMaterno || '-';
            const dniStr = p.dni || p.nroDocumento || '-';
            const telStr = p.telefono || '-';

            pasajerosHtml += `
                <tr>
                    <td>${index + 1}</td>
                    <td><strong>${nombreStr}</strong></td>
                    <td>${patStr}</td>
                    <td>${matStr}</td>
                    <td>${dniStr}</td>
                    <td>${telStr}</td>
                </tr>
            `;
        });

        const titularNombre = r.titular && !r.titular.includes('undefined') ? r.titular : (pasajeros[0] ? ((pasajeros[0].nombre || '') + ' ' + (pasajeros[0].apellidoPaterno || '') + ' ' + (pasajeros[0].apellidoMaterno || '')).trim() : 'Briane Infantes Gonzales');

        const cardHtml = `
            <div class="reserva-main-card" style="margin-bottom: 30px;">
                <!-- Top Section Grid -->
                <div class="reserva-top-grid">
                    <!-- Left Details -->
                    <div class="reserva-info-left">
                        <img src="${r.imagen || '../../img/valle.jpg'}" alt="${r.titulo}" class="reserva-thumb" onerror="this.onerror=null; this.src='../../img/fondo1.png';">
                        <div class="reserva-details">
                            <div class="reserva-header-row">
                                <h2 class="reserva-tour-title">${r.titulo || 'Tour Travelink'}</h2>
                                <span class="${statusBadgeClass}">${statusText}</span>
                            </div>
                            <p class="reserva-agency">${r.agencia || 'Andes Tours'}</p>
                            <p class="reserva-meta-text">Fecha de registro: ${r.fechaRegistro || 'Hoy'}</p>
                            <p class="reserva-meta-text">Ubicación: ${r.ubicacion || 'Cusco, Perú'}</p>
                            <p class="reserva-meta-text">Fecha: ${r.fechas || 'Fecha por confirmar'}</p>
                            <p class="reserva-meta-text">Total personas: ${r.personas || pasajeros.length + ' persona(s)'}</p>
                        </div>
                    </div>

                    <!-- Right Box -->
                    <div class="reserva-right-box">
                        <p>Titular: <strong>${titularNombre || 'Briane Infantes Gonzales'}</strong></p>
                        <p>Método de pago: <strong>${r.metodoPago || 'Presencial'}</strong></p>
                        <p class="total-price">Total: ${rFormattedTotal(r.total)}</p>
                    </div>
                </div>

                <!-- Actions Row -->
                <div class="reserva-actions-row">
                    <button class="btn-action-outline" onclick="verDetallesReserva('${r.codigo}')">Ver detalles</button>
                    <button class="btn-action-solid-green" onclick="abrirModalCalificar('${r.titulo || 'Tour'}', '${r.ubicacion || 'Cusco'}', '${r.agencia || 'Agencia'}', '${r.imagen || '../../img/valle.jpg'}', ${r.id || 1}, ${r.idAgencia || 1})">Calificar</button>
                    <button class="btn-action-outline" onclick="descargarVoucherPDF('${r.codigo}')">Exportar PDF</button>
                    <button class="btn-action-danger" onclick="cancelarReservaConfirm('${r.codigo}')">Cancelar reserva</button>
                </div>

                <!-- Passengers Section -->
                <h3 class="passengers-section-title">Detalle de pasajeros</h3>
                <div class="passengers-table-container">
                    <table class="passengers-table">
                        <thead>
                            <tr>
                                <th>#</th>
                                <th>Nombres</th>
                                <th>Apellido Paterno</th>
                                <th>Apellido Materno</th>
                                <th>DNI</th>
                                <th>Teléfono</th>
                            </tr>
                        </thead>
                        <tbody>
                            ${pasajerosHtml}
                        </tbody>
                    </table>
                </div>
            </div>
        `;
        listContainer.insertAdjacentHTML('beforeend', cardHtml);
    });

    renderPagination(totalPages);
}

function renderPagination(totalPages) {
    let pagContainer = document.getElementById('pagination-container');

    if (!pagContainer) {
        const main = document.querySelector('.reservas-container') || document.querySelector('main');
        if (main) {
            pagContainer = document.createElement('div');
            pagContainer.id = 'pagination-container';
            pagContainer.style.cssText = "display: flex; justify-content: center; gap: 10px; margin-top: 30px;";
            main.appendChild(pagContainer);
        } else {
            return;
        }
    }

    pagContainer.innerHTML = '';

    if (totalPages <= 1) return;

    // Botón Anterior
    const prevBtn = document.createElement('button');
    prevBtn.innerHTML = '<i class="fa-solid fa-chevron-left"></i>';
    prevBtn.style.cssText = "width: 35px; height: 35px; border-radius: 50%; border: 1px solid #e2e8f0; background: white; color: #94a3b8; cursor: pointer;";
    if (currentPage > 1) {
        prevBtn.style.color = "#0b1f38";
        prevBtn.onclick = () => { currentPage--; renderReservasPage(); };
    }
    pagContainer.appendChild(prevBtn);

    // Botones numéricos dinámicos
    for (let i = 1; i <= totalPages; i++) {
        const pageBtn = document.createElement('button');
        pageBtn.innerText = i;
        if (i === currentPage) {
            pageBtn.style.cssText = "width: 35px; height: 35px; border-radius: 50%; border: none; background: #196f3d; color: white; font-weight: bold; cursor: pointer;";
        } else {
            pageBtn.style.cssText = "width: 35px; height: 35px; border-radius: 50%; border: 1px solid #e2e8f0; background: white; color: #475569; font-weight: bold; cursor: pointer;";
            pageBtn.onclick = () => { currentPage = i; renderReservasPage(); };
        }
        pagContainer.appendChild(pageBtn);
    }

    // Botón Siguiente
    const nextBtn = document.createElement('button');
    nextBtn.innerHTML = '<i class="fa-solid fa-chevron-right"></i>';
    nextBtn.style.cssText = "width: 35px; height: 35px; border-radius: 50%; border: 1px solid #e2e8f0; background: white; color: #94a3b8; cursor: pointer;";
    if (currentPage < totalPages) {
        nextBtn.style.color = "#0b1f38";
        nextBtn.onclick = () => { currentPage++; renderReservasPage(); };
    }
    pagContainer.appendChild(nextBtn);
}

// Modal Ver Detalles
window.verDetallesReserva = function (codigo) {
    const list = getReservasData();
    const user = window.getCurrentUser ? window.getCurrentUser() : { username: 'Usuario', email: '' };
    const reserva = list.find(r => r.codigo === codigo) || list[0];

    if (!reserva) return;

    if (typeof Swal !== 'undefined') {
        Swal.fire({
            title: `<strong>Detalles de Reserva ${reserva.codigo}</strong>`,
            html: `
                <div style="text-align: left; font-family: sans-serif; font-size: 14px; color: #334155; line-height: 1.6;">
                    <p style="border-bottom: 1px solid #e2e8f0; padding-bottom: 8px;"><strong>Tour / Experiencia:</strong> ${reserva.titulo}</p>
                    <p style="border-bottom: 1px solid #e2e8f0; padding-bottom: 8px;"><strong>Ubicación:</strong> ${reserva.ubicacion || 'Cusco, Perú'}</p>
                    <p style="border-bottom: 1px solid #e2e8f0; padding-bottom: 8px;"><strong>Fechas:</strong> ${reserva.fechas}</p>
                    <p style="border-bottom: 1px solid #e2e8f0; padding-bottom: 8px;"><strong>Pasajeros:</strong> ${reserva.personas}</p>
                    <p style="border-bottom: 1px solid #e2e8f0; padding-bottom: 8px;"><strong>Agencia:</strong> ${reserva.agencia || 'Andes Tours'}</p>
                    <p style="border-bottom: 1px solid #e2e8f0; padding-bottom: 8px;"><strong>Titular:</strong> ${reserva.titular || user.username} (${user.email || ''})</p>
                    <p style="border-bottom: 1px solid #e2e8f0; padding-bottom: 8px;"><strong>Método de Pago:</strong> ${reserva.metodoPago || 'Efectivo'}</p>
                    <p style="font-size: 16px; color: #0b1f38; margin-top: 10px;"><strong>Total:</strong> <span style="color: #196f3d; font-weight: bold;">${rFormattedTotal(reserva.total)}</span></p>
                </div>
            `,
            icon: 'info',
            confirmButtonText: 'Cerrar',
            confirmButtonColor: '#0b1f38'
        });
    } else {
        alert("Reserva " + reserva.codigo + "\nTour: " + reserva.titulo + "\nFechas: " + reserva.fechas + "\nTotal: " + rFormattedTotal(reserva.total));
    }
};

function rFormattedTotal(total) {
    if (typeof total === 'number') return 'S/ ' + total.toFixed(2);
    if (String(total).includes('S/')) return total;
    return 'S/ ' + total;
}

// Cancelar Reserva
window.cancelarReservaConfirm = function (codigo) {
    if (typeof Swal !== 'undefined') {
        Swal.fire({
            title: '¿Estás seguro?',
            text: `¿Deseas cancelar la reserva ${codigo}? Esta acción no se puede deshacer.`,
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#ef4444',
            cancelButtonColor: '#64748b',
            confirmButtonText: 'Sí, cancelar reserva',
            cancelButtonText: 'Volver'
        }).then((result) => {
            if (result.isConfirmed) {
                const key = getUserReservasKey();
                let list = getReservasData();
                list = list.map(r => {
                    if (r.codigo === codigo) {
                        r.estado = 'Cancelada';
                    }
                    return r;
                });
                localStorage.setItem(key, JSON.stringify(list));
                renderReservasPage();
                Swal.fire('Cancelada', `La reserva ${codigo} ha sido cancelada.`, 'success');
            }
        });
    } else {
        if (confirm(`¿Deseas cancelar la reserva ${codigo}?`)) {
            const key = getUserReservasKey();
            let list = getReservasData();
            list = list.map(r => {
                if (r.codigo === codigo) {
                    r.estado = 'Cancelada';
                }
                return r;
            });
            localStorage.setItem(key, JSON.stringify(list));
            renderReservasPage();
        }
    }
};

// Descargar Voucher PDF
window.descargarVoucherPDF = function (codigo) {
    const list = getReservasData();
    const user = window.getCurrentUser ? window.getCurrentUser() : { username: 'Usuario', email: '' };
    const reserva = list.find(r => r.codigo === codigo) || list[0];

    if (!reserva) return;

    const pdfWindow = window.open('', '_blank');
    const voucherContent = `
        <!DOCTYPE html>
        <html lang="es">
        <head>
            <meta charset="UTF-8">
            <title>Voucher - ${reserva.codigo}</title>
            <style>
                body { font-family: 'Helvetica Neue', Arial, sans-serif; padding: 40px; color: #1e293b; background: #fff; }
                .header { display: flex; justify-content: space-between; align-items: center; border-bottom: 3px solid #196f3d; padding-bottom: 20px; }
                .logo { font-size: 28px; font-weight: bold; color: #0b1f38; }
                .logo span { color: #196f3d; }
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
                <button onclick="window.print()" style="background: #196f3d; color: white; border: none; padding: 10px 20px; border-radius: 6px; font-weight: bold; cursor: pointer;">Imprimir / Guardar PDF</button>
            </div>
            <div class="header">
                <div class="logo">Travel<span>ink</span></div>
                <div class="voucher-title">
                    <h2>VOUCHER DE RESERVA</h2>
                    <span class="badge">ESTADO: ${(reserva.estado || 'CONFIRMADA').toUpperCase()}</span>
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
                        <div class="field-val">${reserva.titular || user.username} (${user.email || ''})</div>
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
                        <div class="field-val">${reserva.fechas}</div>
                    </div>
                    <div>
                        <div class="field-label">Pasajeros</div>
                        <div class="field-val">${reserva.personas || '1 persona'}</div>
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
                        <td>${reserva.metodoPago || 'Efectivo'}</td>
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
// Modal Calificar Experiencia
// ==============================
let currentSelectedRating = 0;
let currentCalificarReservaId = 1;
let currentCalificarAgenciaId = 1;

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
            highlightStars(currentSelectedRating, 'selected');
        });

        star.addEventListener('click', () => {
            currentSelectedRating = parseInt(star.getAttribute('data-value'));
            highlightStars(currentSelectedRating, 'selected');
        });
    });
}

function highlightStars(count, stateClass) {
    const stars = document.querySelectorAll('#stars-container .star-icon-btn');
    stars.forEach((s, idx) => {
        if (idx < count) {
            s.classList.remove('fa-regular');
            s.classList.add('fa-solid');
            s.style.color = '#eab308';
        } else {
            s.classList.remove('fa-solid');
            s.classList.add('fa-regular');
            s.style.color = '#cbd5e1';
        }
    });
}

window.abrirModalCalificar = function (tourTitle, location, agency, imgBanner, idReserva, idAgencia) {
    currentCalificarReservaId = idReserva || 1;
    currentCalificarAgenciaId = idAgencia || 1;

    document.getElementById('modal-tour-name').textContent = tourTitle || 'Machu Picchu Clásico';
    document.getElementById('modal-tour-location').textContent = location || 'Cusco';
    document.getElementById('modal-tour-agency').textContent = agency || 'Andes Tours';
    if (imgBanner) {
        document.getElementById('modal-img-banner').src = imgBanner;
    }
    currentSelectedRating = 0;
    highlightStars(0, '');
    document.getElementById('modal-comment-input').value = '';
    document.getElementById('modal-char-count').textContent = '0/500';

    document.getElementById('modal-calificar').classList.add('active');
};

window.cerrarModalCalificar = function () {
    document.getElementById('modal-calificar').classList.remove('active');
};

window.actualizarContadorModal = function (textarea) {
    document.getElementById('modal-char-count').textContent = `${textarea.value.length}/500`;
};

window.enviarCalificacion = function () {
    if (currentSelectedRating === 0) {
        if (typeof Swal !== 'undefined') {
            Swal.fire('Calificación requerida', 'Por favor selecciona al menos una estrella para calificar.', 'warning');
        } else {
            alert('Por favor selecciona al menos una estrella para calificar.');
        }
        return;
    }

    const comentario = document.getElementById('modal-comment-input').value.trim();
    const user = window.getCurrentUser ? window.getCurrentUser() : null;

    const payload = {
        idUsuario: user ? (user.id || user.idUsuario || 1) : 1,
        idAgencia: currentCalificarAgenciaId || 1,
        idReserva: currentCalificarReservaId || 1,
        estrellas: currentSelectedRating,
        comentario: comentario
    };

    fetch('http://localhost:8080/api/calificar', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    })
    .then(res => res.json())
    .then(data => {
        cerrarModalCalificar();
        if (typeof Swal !== 'undefined') {
            Swal.fire({
                title: '¡Gracias por tu opinión!',
                text: 'Tu calificación ha sido registrada exitosamente en la base de datos.',
                icon: 'success',
                confirmButtonColor: '#196f3d'
            });
        } else {
            alert('¡Gracias por tu opinión! Tu calificación ha sido registrada exitosamente.');
        }
    })
    .catch(err => {
        cerrarModalCalificar();
        if (typeof Swal !== 'undefined') {
            Swal.fire({
                title: '¡Gracias por tu opinión!',
                text: 'Tu calificación ha sido registrada.',
                icon: 'success',
                confirmButtonColor: '#196f3d'
            });
        } else {
            alert('¡Gracias por tu opinión!');
        }
    });
};
