// Travelink - Reservas Dynamic Management & PDF Voucher Generator (User Scoped)

const ITEMS_PER_PAGE = 10;
let currentPage = 1;

// Default initial reservations list for default user 'briane@gmail.com'
const defaultReservasBriane = [
    {
        id: 1,
        codigo: 'TRK-001',
        titulo: 'Tour Valle Sagrado de los Incas',
        ubicacion: 'Cusco, Perú',
        fechas: '12 abr. 2026 - 13 abr. 2026',
        personas: '2 personas',
        agencia: 'Agencia: Andes Travel Perú',
        estado: 'Confirmada',
        total: 'S/ 350.00',
        metodoPago: 'Yape / Plin',
        imagen: '../../img/valle.jpg',
        fechaRegistro: '2026-04-10'
    },
    {
        id: 2,
        codigo: 'TRK-002',
        titulo: 'Montaña de 7 Colores',
        ubicacion: 'Cusco, Perú',
        fechas: '15 mar. 2026 - 15 mar. 2026',
        personas: '1 persona',
        agencia: 'Agencia: Aventura Perú',
        estado: 'Completada',
        total: 'S/ 180.00',
        metodoPago: 'Tarjeta de Crédito',
        imagen: '../../img/7colores.jpg',
        fechaRegistro: '2026-03-14'
    }
];

function getUserReservasKey() {
    const user = window.getCurrentUser ? window.getCurrentUser() : { email: 'briane@gmail.com' };
    return 'travelink_reservas_' + (user.email || 'briane@gmail.com');
}

function getReservasData() {
    const key = getUserReservasKey();
    const stored = localStorage.getItem(key);
    const user = window.getCurrentUser ? window.getCurrentUser() : { email: 'briane@gmail.com' };

    if (!stored) {
        if (user.email === 'briane@gmail.com') {
            localStorage.setItem(key, JSON.stringify(defaultReservasBriane));
            return defaultReservasBriane;
        } else {
            // New or other user has empty reservations initially
            localStorage.setItem(key, JSON.stringify([]));
            return [];
        }
    }
    try {
        const parsed = JSON.parse(stored);
        return Array.isArray(parsed) ? parsed : [];
    } catch (e) {
        return [];
    }
}

document.addEventListener('DOMContentLoaded', () => {
    cargarReservasDesdeDB();
});

function cargarReservasDesdeDB() {
    const user = window.getCurrentUser ? window.getCurrentUser() : { email: 'briadayanainfantes@gmail.com' };
    const email = user.email || 'briadayanainfantes@gmail.com';
    const key = getUserReservasKey();

    fetch(`http://localhost:8080/api/obtener_reservas?correo=${encodeURIComponent(email)}`)
        .then(res => res.json())
        .then(data => {
            if (data.status === 'success' && Array.isArray(data.reservas) && data.reservas.length > 0) {
                let localList = [];
                try {
                    localList = JSON.parse(localStorage.getItem(key) || '[]');
                } catch(e) { localList = []; }

                // Fusionar reservas de la DB con las locales sin duplicar
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
}

function renderReservasPage() {
    const allReservas = getReservasData();
    const listContainer = document.querySelector('.lista-reservas');

    if (!listContainer) return;

    // Calcular páginas (max 10 por página)
    const totalItems = allReservas.length;
    const totalPages = Math.ceil(totalItems / ITEMS_PER_PAGE) || 1;

    if (currentPage > totalPages) currentPage = totalPages;
    if (currentPage < 1) currentPage = 1;

    const startIndex = (currentPage - 1) * ITEMS_PER_PAGE;
    const pageItems = allReservas.slice(startIndex, startIndex + ITEMS_PER_PAGE);

    // Renderizar tarjetas de reserva
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
        pageItems.forEach(r => {
            const isCompletada = r.estado === 'Completada' || r.estado === 'Finalizada';
            const badgeClass = isCompletada ? 'background: #dcfce7; color: #16a34a;' : 'background: #e0f2fe; color: #0284c7;';
            const statusLabel = isCompletada ? 'Finalizada' : 'Próxima';
            const badgeTagColor = isCompletada ? 'color: #22c55e;' : 'color: #3b82f6;';
            const badgeTagIcon = isCompletada ? 'fa-check-circle' : 'fa-calendar';

            const cardHtml = `
                <div class="reserva-card" style="display: flex; background: white; border: 1px solid #e2e8f0; border-radius: 16px; overflow: hidden; box-shadow: 0 4px 15px rgba(0,0,0,0.03); flex-wrap: wrap;">
                    <div class="reserva-img" style="width: 300px; min-width: 250px; position: relative; flex: 1;">
                        <span style="position: absolute; top: 15px; left: 15px; background: white; ${badgeTagColor} font-weight: bold; padding: 5px 12px; border-radius: 8px; font-size: 13px; box-shadow: 0 2px 5px rgba(0,0,0,0.1);"><i class="fa-regular ${badgeTagIcon}"></i> ${statusLabel}</span>
                        <img src="${r.imagen || '../../img/valle.jpg'}" alt="${r.titulo}" style="width: 100%; height: 100%; object-fit: cover; min-height: 200px;" onerror="this.onerror=null; this.src='../../img/fondo1.png';">
                    </div>
                    
                    <div class="reserva-body" style="padding: 25px; flex: 2; display: flex; justify-content: space-between; flex-wrap: wrap; gap: 20px;">
                        <div class="reserva-info" style="display: flex; flex-direction: column; justify-content: center; gap: 10px;">
                            <h2 style="margin: 0; color: #0b1f38; font-size: 20px;">${r.titulo}</h2>
                            <div style="color: #64748b; font-size: 14px; display: flex; align-items: center; gap: 8px;"><i class="fa-solid fa-location-dot" style="width:16px;"></i> ${r.ubicacion || 'Cusco, Perú'}</div>
                            <div style="color: #64748b; font-size: 14px; display: flex; align-items: center; gap: 8px;"><i class="fa-regular fa-calendar" style="width:16px;"></i> ${r.fechas}</div>
                            <div style="color: #64748b; font-size: 14px; display: flex; align-items: center; gap: 8px;"><i class="fa-solid fa-user" style="width:16px;"></i> ${r.personas}</div>
                            <div style="margin-top: 10px; display: flex; align-items: center; gap: 10px;">
                                <div style="width: 30px; height: 30px; background: #e0f2fe; border-radius: 5px; display: flex; justify-content: center; align-items: center; color: #0284c7;"><i class="fa-solid fa-mountain"></i></div>
                                <span style="color: #475569; font-size: 14px; font-weight: 500;">${r.agencia || 'Agencia: Andes Travel Perú'}</span>
                            </div>
                        </div>
                        
                        <div class="reserva-actions" style="width: 250px; min-width: 200px; display: flex; flex-direction: column; justify-content: center; border-left: 1px solid #f1f5f9; padding-left: 25px;">
                            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px;">
                                <span style="${badgeClass} padding: 6px 12px; border-radius: 20px; font-size: 13px; font-weight: bold;"><i class="fa-solid fa-check-circle"></i> ${r.estado}</span>
                            </div>
                            <p style="margin: 0 0 3px 0; font-size: 13px; color: #64748b;">Código de reserva</p>
                            <p style="margin: 0 0 15px 0; font-size: 18px; font-weight: bold; color: #0b1f38;">${r.codigo}</p>
                            
                            <button onclick="verDetallesReserva('${r.codigo}')" style="width: 100%; background: #0b1f38; color: white; border: none; padding: 10px; border-radius: 8px; font-weight: 600; cursor: pointer; margin-bottom: 8px; transition: 0.2s;">Ver detalles <i class="fa-solid fa-chevron-right" style="font-size: 12px; margin-left: 5px;"></i></button>
                            <button onclick="descargarVoucherPDF('${r.codigo}')" style="width: 100%; background: white; color: #0b1f38; border: 1px solid #cbd5e1; padding: 10px; border-radius: 8px; font-weight: 600; cursor: pointer; transition: 0.2s;"><i class="fa-solid fa-download"></i> Descargar voucher (PDF)</button>
                        </div>
                    </div>
                </div>
            `;
            listContainer.insertAdjacentHTML('beforeend', cardHtml);
        });
    }

    // Renderizar paginación dinámica (máximo 10 por página)
    renderPagination(totalPages);
}

function renderPagination(totalPages) {
    let pagContainer = document.getElementById('pagination-container');

    if (!pagContainer) {
        const main = document.querySelector('.reservas-container') || document.querySelector('main');
        if (main) {
            pagContainer = document.createElement('div');
            pagContainer.id = 'pagination-container';
            pagContainer.style.cssText = "display: flex; justify-content: center; gap: 10px; margin-top: 40px;";
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
            pageBtn.style.cssText = "width: 35px; height: 35px; border-radius: 50%; border: none; background: #0b1f38; color: white; font-weight: bold; cursor: pointer;";
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
window.verDetallesReserva = function(codigo) {
    const list = getReservasData();
    const user = window.getCurrentUser ? window.getCurrentUser() : { username: 'briane', email: 'briane@gmail.com' };
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
                    <p style="border-bottom: 1px solid #e2e8f0; padding-bottom: 8px;"><strong>Agencia:</strong> ${reserva.agencia || 'Andes Travel Perú'}</p>
                    <p style="border-bottom: 1px solid #e2e8f0; padding-bottom: 8px;"><strong>Titular:</strong> ${user.username} (${user.email})</p>
                    <p style="border-bottom: 1px solid #e2e8f0; padding-bottom: 8px;"><strong>Método de Pago:</strong> ${reserva.metodoPago || 'Yape / Plin'}</p>
                    <p style="font-size: 16px; color: #0b1f38; margin-top: 10px;"><strong>Total Pagado:</strong> <span style="color: #16a34a; font-weight: bold;">${rFormattedTotal(reserva.total)}</span></p>
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

// Descargar Voucher PDF
window.descargarVoucherPDF = function(codigo) {
    const list = getReservasData();
    const user = window.getCurrentUser ? window.getCurrentUser() : { username: 'briane', email: 'briane@gmail.com' };
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
                        <div class="field-val">${user.username} (${user.email})</div>
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

window.abrirModalCalificar = function(titulo, ubicacion, agencia, imagen) {
    const modal = document.getElementById('modal-calificar');
    if (!modal) return;

    if (titulo) document.getElementById('modal-tour-name').innerText = titulo;
    if (ubicacion) document.getElementById('modal-tour-location').innerText = ubicacion;
    if (agencia) document.getElementById('modal-tour-agency').innerText = agencia;
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

window.enviarCalificacion = function() {
    if (currentSelectedRating === 0) {
        if (typeof Swal !== 'undefined') {
            Swal.fire({
                icon: 'warning',
                title: 'Selecciona una calificación',
                text: 'Por favor asigna al menos 1 estrella a tu experiencia.',
                confirmButtonColor: '#196f3d'
            });
        } else {
            alert('Por favor asigna al menos 1 estrella.');
        }
        return;
    }

    cerrarModalCalificar();

    if (typeof Swal !== 'undefined') {
        Swal.fire({
            icon: 'success',
            title: '¡Gracias por tu opinión!',
            text: 'Tu calificación ha sido enviada exitosamente.',
            confirmButtonColor: '#196f3d'
        });
    } else {
        alert('¡Gracias por tu opinión! Tu calificación ha sido enviada exitosamente.');
    }
};

window.cancelarReservaConfirm = function(codigo) {
    if (typeof Swal !== 'undefined') {
        Swal.fire({
            title: '¿Estás seguro?',
            text: 'Esta acción cancelará tu reserva ' + codigo,
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#dc2626',
            cancelButtonColor: '#64748b',
            confirmButtonText: 'Sí, cancelar reserva',
            cancelButtonText: 'No, mantener'
        }).then((result) => {
            if (result.isConfirmed) {
                Swal.fire({
                    icon: 'success',
                    title: 'Reserva Cancelada',
                    text: 'La reserva ' + codigo + ' ha sido cancelada.',
                    confirmButtonColor: '#196f3d'
                });
            }
        });
    } else {
        if (confirm('¿Estás seguro de cancelar la reserva ' + codigo + '?')) {
            alert('La reserva ' + codigo + ' ha sido cancelada.');
        }
    }
};

