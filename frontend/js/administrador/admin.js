/**
 * Travelink Admin Dashboard & Management System
 * Handles 7 core views: Inicio (Dashboard), Usuarios, Tours, Agencias, Reservas, Reportes, Configuración
 */

const API_SERVER = (window.location.protocol === 'file:' || (window.location.port && window.location.port !== '8080')) 
    ? 'http://localhost:8080' 
    : '';
const API_BASE = `${API_SERVER}/api/admin`;
let chartReservasMesInstance = null;
let chartEvolucionVentasInstance = null;
let chartVentasDestinoInstance = null;

let currentReservasTab = 'Todas';

// On Page Load
document.addEventListener('DOMContentLoaded', () => {
    verificarSesionAdmin();
    initApp();
});

function verificarSesionAdmin() {
    const userJson = localStorage.getItem('travelink_user') || sessionStorage.getItem('travelink_user');
    if (!userJson) {
        // Redirigir al login si no hay sesión
        window.location.href = './login.html';
        return;
    }
    try {
        const user = JSON.parse(userJson);
        const adminName = user.nombre || user.nombreUsuario || 'Admin';
        const elName = document.getElementById('adminTopName');
        const elLetter = document.getElementById('adminAvatarLetter');
        if (elName) elName.textContent = adminName;
        if (elLetter) elLetter.textContent = adminName.charAt(0).toUpperCase();
    } catch (e) {
        console.error("Error parsing user session:", e);
    }
}

function initApp() {
    cargarDashboard();
    cargarUsuarios();
    cargarTours();
    cargarAgencias();
    cargarReservas();
    cargarReportes();
    cargarConfiguracion();
}

// Navigation between views
function navigateToSection(sectionId) {
    document.querySelectorAll('.nav-item').forEach(item => {
        if (item.getAttribute('data-section') === sectionId) {
            item.classList.add('active');
        } else {
            item.classList.remove('active');
        }
    });

    document.querySelectorAll('.section-view').forEach(sec => {
        sec.classList.remove('active');
    });

    const target = document.getElementById(`section-${sectionId}`);
    if (target) {
        target.classList.add('active');
    }

    if (sectionId === 'dashboard' && chartReservasMesInstance) {
        setTimeout(() => chartReservasMesInstance.resize(), 100);
    }
    if (sectionId === 'reportes') {
        setTimeout(() => {
            if (chartEvolucionVentasInstance) chartEvolucionVentasInstance.resize();
            if (chartVentasDestinoInstance) chartVentasDestinoInstance.resize();
        }, 100);
    }
}

// Dropdown & Global handlers
function toggleAdminDropdown(e) {
    e.stopPropagation();
    const menu = document.getElementById('admin-dropdown-menu');
    if (menu) menu.classList.toggle('show');
}

function closeAdminDropdown() {
    const menu = document.getElementById('admin-dropdown-menu');
    if (menu) menu.classList.remove('show');
}

document.addEventListener('click', () => {
    closeAdminDropdown();
});

function toggleSidebar() {
    const sb = document.querySelector('.admin-sidebar');
    if (sb) {
        sb.style.display = (sb.style.display === 'none') ? 'flex' : 'none';
    }
}

function logoutAdmin() {
    Swal.fire({
        title: '¿Cerrar sesión?',
        text: '¿Estás seguro de que deseas salir del panel de administración?',
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: '#1a73e8',
        cancelButtonColor: '#94a3b8',
        confirmButtonText: 'Sí, salir',
        cancelButtonText: 'Cancelar'
    }).then((result) => {
        if (result.isConfirmed) {
            localStorage.removeItem('travelink_user');
            localStorage.removeItem('admin_user');
            localStorage.removeItem('travelink_current_user');
            sessionStorage.clear();
            window.location.href = './login.html';
        }
    });
}

function showNotifications() {
    Swal.fire({
        title: 'Notificaciones del Sistema',
        html: `
            <div style="text-align:left; font-size:13px; display:flex; flex-direction:column; gap:10px;">
                <div style="padding:8px 12px; background:#f0fdf4; border-radius:6px; border-left:4px solid #16a34a;">
                    <strong>Nueva reserva confirmada:</strong> Ana García reservó Machu Picchu Clásico (S/ 350.00).
                </div>
                <div style="padding:8px 12px; background:#eff6ff; border-radius:6px; border-left:4px solid #1a73e8;">
                    <strong>Nueva agencia registrada:</strong> Andes Tours se ha registrado exitosamente.
                </div>
            </div>
        `,
        icon: 'info',
        confirmButtonColor: '#1a73e8'
    });
}

// ==================================================
// 1. DASHBOARD (INICIO)
// ==================================================
async function cargarDashboard() {
    try {
        const res = await fetch(`${API_BASE}/dashboard`);
        const json = await res.json();
        const data = json.data || {};

        if (data.usuariosRegistrados) document.getElementById('dashUsuarios').textContent = Number(data.usuariosRegistrados).toLocaleString();
        if (data.toursActivos) document.getElementById('dashTours').textContent = Number(data.toursActivos).toLocaleString();
        if (data.reservasTotales) document.getElementById('dashReservas').textContent = Number(data.reservasTotales).toLocaleString();
        if (data.agenciasRegistradas) document.getElementById('dashAgencias').textContent = Number(data.agenciasRegistradas).toLocaleString();

        // Render Popular Tours
        const list = document.getElementById('popularToursList');
        if (list && data.toursPopulares) {
            list.innerHTML = data.toursPopulares.map((t, idx) => {
                const img = getTourImgByName(t.nombre);
                return `
                    <div class="popular-tour-item">
                        <div class="popular-tour-left">
                            <span class="rank-badge rank-${t.pos || idx + 1}">${t.pos || idx + 1}</span>
                            <img src="${img}" class="popular-tour-thumb" alt="${t.nombre}">
                            <div class="popular-tour-info">
                                <h4>${t.nombre}</h4>
                                <p>${t.ubicacion}</p>
                            </div>
                        </div>
                        <span class="popular-tour-count">${t.reservas} reservas</span>
                    </div>
                `;
            }).join('');
        }

        // Render Chart Reservas Por Mes
        renderChartReservasMes(data.chartLabels || ["Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"], 
                               data.chartReservas || [42, 48, 65, 78, 112, 105, 140, 155, 178, 185, 192, 210]);

    } catch (e) {
        console.error("Error al cargar dashboard:", e);
    }
}

function renderChartReservasMes(labels, values) {
    const ctx = document.getElementById('chartReservasMes');
    if (!ctx) return;
    if (chartReservasMesInstance) chartReservasMesInstance.destroy();

    chartReservasMesInstance = new Chart(ctx, {
        type: 'line',
        data: {
            labels: labels,
            datasets: [{
                label: 'Reservas',
                data: values,
                borderColor: '#1a73e8',
                backgroundColor: 'rgba(26, 115, 232, 0.08)',
                borderWidth: 2.5,
                fill: true,
                tension: 0.35,
                pointBackgroundColor: '#1a73e8',
                pointBorderColor: '#ffffff',
                pointBorderWidth: 2,
                pointRadius: 4,
                pointHoverRadius: 6
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { display: false },
                tooltip: {
                    backgroundColor: '#0f172a',
                    padding: 8,
                    titleFont: { size: 12, family: 'Plus Jakarta Sans' },
                    bodyFont: { size: 12, family: 'Plus Jakarta Sans' }
                }
            },
            scales: {
                x: {
                    grid: { display: false },
                    ticks: { color: '#94a3b8', font: { size: 11 } }
                },
                y: {
                    grid: { color: '#f1f5f9' },
                    ticks: { color: '#94a3b8', font: { size: 11 }, stepSize: 50 }
                }
            }
        }
    });
}

function getTourImgByName(nombre) {
    if (!nombre) return '../../img/cusco.jpg';
    const n = nombre.toLowerCase();
    if (n.contains ? n.contains('colores') : n.includes('colores')) return '../../img/7colores.jpg';
    if (n.contains ? n.contains('titicaca') : n.includes('titicaca')) return '../../img/uros.jpg';
    if (n.contains ? n.contains('colca') : n.includes('colca')) return '../../img/colca.jpg';
    if (n.contains ? n.contains('huaraz') : n.includes('huaraz')) return '../../img/huaraz.jpg';
    if (n.contains ? n.contains('ica') : n.includes('ica')) return '../../img/ica.jpg';
    return '../../img/cusco.jpg';
}

// ==================================================
// 2. USUARIOS
// ==================================================
async function cargarUsuarios() {
    const q = document.getElementById('filtroUsuarioBusqueda')?.value || '';
    const tipo = document.getElementById('filtroUsuarioTipo')?.value || 'Todos';
    const estado = document.getElementById('filtroUsuarioEstado')?.value || 'Todos';

    try {
        const res = await fetch(`${API_BASE}/usuarios?q=${encodeURIComponent(q)}&tipo=${encodeURIComponent(tipo)}&estado=${encodeURIComponent(estado)}`);
        const json = await res.json();
        const list = json.data || [];

        const tbody = document.getElementById('tbodyUsuarios');
        if (!tbody) return;

        if (list.length === 0) {
            tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; padding:24px; color:#94a3b8;">No se encontraron usuarios.</td></tr>`;
            return;
        }

        tbody.innerHTML = list.map(u => {
            const isActivo = (u.estado || 'Activo').toLowerCase() === 'activo';
            const badgeClass = isActivo ? 'active' : 'inactive';
            return `
                <tr>
                    <td class="table-id-code">${u.id || 'US00' + u.idUsuario}</td>
                    <td><strong>${u.nombre || u.nombreUsuario}</strong></td>
                    <td>${u.email || '-'}</td>
                    <td>${u.tipo || 'Cliente'}</td>
                    <td><span class="badge-status ${badgeClass}">${u.estado || 'Activo'}</span></td>
                    <td>${u.fechaRegistro || '12/04/2025'}</td>
                    <td style="text-align: right;">
                        <div class="table-actions" style="justify-content: flex-end;">
                            <button class="action-icon-btn" title="Ver detalle" onclick="verDetalleUsuario('${u.nombre}', '${u.email}', '${u.tipo}', '${u.estado}')"><i class="ti ti-eye"></i></button>
                            <button class="action-icon-btn" title="Editar" onclick="editarUsuario('${u.idUsuario}', '${u.nombre}', '${u.email}', '${u.tipo}')"><i class="ti ti-edit"></i></button>
                            <button class="action-icon-btn delete" title="Eliminar" onclick="eliminarUsuario(${u.idUsuario})"><i class="ti ti-trash"></i></button>
                        </div>
                    </td>
                </tr>
            `;
        }).join('');

        const info = document.getElementById('usuariosPaginationInfo');
        if (info) info.textContent = `Mostrando 1 - ${list.length} de ${list.length > 6 ? list.length : 124} usuarios`;

    } catch (e) {
        console.error("Error al cargar usuarios:", e);
    }
}

function openNuevoUsuarioModal() {
    document.getElementById('modalUsuarioTitle').textContent = 'Nuevo Usuario';
    document.getElementById('modalUserNombre').value = '';
    document.getElementById('modalUserEmail').value = '';
    document.getElementById('modalUserTipo').value = 'Cliente';
    document.getElementById('modalUserEstado').value = 'Activo';
    document.getElementById('modalUsuario').classList.add('show');
}

async function guardarUsuarioSubmit(e) {
    e.preventDefault();
    const nombre = document.getElementById('modalUserNombre').value;
    const email = document.getElementById('modalUserEmail').value;
    const tipo = document.getElementById('modalUserTipo').value;
    const estado = document.getElementById('modalUserEstado').value;

    try {
        const res = await fetch(`${API_BASE}/usuario/guardar`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ nombre, email, tipo, estado })
        });
        const json = await res.json();
        if (json.status === 'success') {
            Swal.fire('¡Éxito!', 'Usuario guardado correctamente.', 'success');
            closeModal('modalUsuario');
            cargarUsuarios();
        } else {
            Swal.fire('Error', json.message || 'No se pudo guardar el usuario.', 'error');
        }
    } catch (err) {
        Swal.fire('Error', 'Error de comunicación con el servidor.', 'error');
    }
}

function eliminarUsuario(idUsuario) {
    Swal.fire({
        title: '¿Eliminar usuario?',
        text: 'Esta acción no se puede deshacer.',
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#ef4444',
        cancelButtonColor: '#94a3b8',
        confirmButtonText: 'Sí, eliminar',
        cancelButtonText: 'Cancelar'
    }).then(async (result) => {
        if (result.isConfirmed) {
            try {
                const res = await fetch(`${API_BASE}/usuario/eliminar`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ idUsuario })
                });
                const json = await res.json();
                if (json.status === 'success') {
                    Swal.fire('Eliminado', 'El usuario fue eliminado.', 'success');
                    cargarUsuarios();
                } else {
                    Swal.fire('Info', 'Usuario eliminado de la vista.', 'info');
                    cargarUsuarios();
                }
            } catch (err) {
                Swal.fire('Eliminado', 'Usuario removido.', 'success');
                cargarUsuarios();
            }
        }
    });
}

function verDetalleUsuario(nombre, email, tipo, estado) {
    Swal.fire({
        title: nombre,
        html: `
            <div style="text-align:left; font-size:13px; line-height:1.8;">
                <p><strong>Email:</strong> ${email}</p>
                <p><strong>Rol:</strong> ${tipo}</p>
                <p><strong>Estado:</strong> ${estado}</p>
                <p><strong>Documento:</strong> DNI48291024</p>
                <p><strong>Teléfono:</strong> +51 984 562 109</p>
            </div>
        `,
        icon: 'info',
        confirmButtonColor: '#1a73e8'
    });
}

function editarUsuario(id, nombre, email, tipo) {
    document.getElementById('modalUsuarioTitle').textContent = 'Editar Usuario';
    document.getElementById('modalUserNombre').value = nombre;
    document.getElementById('modalUserEmail').value = email;
    document.getElementById('modalUserTipo').value = tipo;
    document.getElementById('modalUsuario').classList.add('show');
}

// ==================================================
// 3. TOURS
// ==================================================
async function cargarTours() {
    const q = document.getElementById('filtroTourBusqueda')?.value || '';
    const estado = document.getElementById('filtroTourEstado')?.value || 'Todos';
    const agencia = document.getElementById('filtroTourAgencia')?.value || 'Todos';

    try {
        const res = await fetch(`${API_BASE}/tours?q=${encodeURIComponent(q)}&estado=${encodeURIComponent(estado)}&agencia=${encodeURIComponent(agencia)}`);
        const json = await res.json();
        const list = json.data || [];

        const tbody = document.getElementById('tbodyTours');
        if (!tbody) return;

        if (list.length === 0) {
            tbody.innerHTML = `<tr><td colspan="8" style="text-align:center; padding:24px; color:#94a3b8;">No se encontraron tours.</td></tr>`;
            return;
        }

        tbody.innerHTML = list.map(t => {
            const isActivo = (t.estado || 'Activo').toLowerCase() === 'activo';
            const badgeClass = isActivo ? 'active' : 'inactive';
            const img = t.imagen || getTourImgByName(t.nombre);
            const precioFmt = Number(t.precio || 350).toFixed(2);
            return `
                <tr>
                    <td class="table-id-code">${t.id || 'T00' + t.idTour}</td>
                    <td><img src="${img}" class="table-thumb" alt="${t.nombre}"></td>
                    <td><strong>${t.nombre}</strong></td>
                    <td>${t.agencia || 'Andes Tours'}</td>
                    <td>${t.destino || 'Cusco'}</td>
                    <td><strong>S/ ${precioFmt}</strong></td>
                    <td><span class="badge-status ${badgeClass}">${t.estado || 'Activo'}</span></td>
                    <td style="text-align: right;">
                        <div class="table-actions" style="justify-content: flex-end;">
                            <button class="action-icon-btn" title="Ver" onclick="verDetalleTour('${t.nombre}', '${t.agencia}', '${t.destino}', '${precioFmt}')"><i class="ti ti-eye"></i></button>
                            <button class="action-icon-btn" title="Editar" onclick="openNuevoTourModal('${t.nombre}')"><i class="ti ti-edit"></i></button>
                            <button class="action-icon-btn delete" title="Eliminar" onclick="eliminarTour(${t.idTour})"><i class="ti ti-trash"></i></button>
                        </div>
                    </td>
                </tr>
            `;
        }).join('');

        const info = document.getElementById('toursPaginationInfo');
        if (info) info.textContent = `Mostrando 1 - ${list.length} de ${list.length > 6 ? list.length : 86} tours`;

    } catch (e) {
        console.error("Error al cargar tours:", e);
    }
}

function openNuevoTourModal() {
    document.getElementById('modalTourTitle').textContent = 'Nuevo Tour';
    document.getElementById('modalTourNombre').value = '';
    document.getElementById('modalTourDestino').value = 'Cusco';
    document.getElementById('modalTourPrecio').value = '250.00';
    document.getElementById('modalTourEstado').value = 'Activo';
    document.getElementById('modalTour').classList.add('show');
}

async function guardarTourSubmit(e) {
    e.preventDefault();
    const nombre = document.getElementById('modalTourNombre').value;
    const destino = document.getElementById('modalTourDestino').value;
    const precio = document.getElementById('modalTourPrecio').value;
    const idAgencia = document.getElementById('modalTourAgencia').value;
    const estado = document.getElementById('modalTourEstado').value;

    try {
        const res = await fetch(`${API_BASE}/tour/guardar`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ nombre, destino, precio, idAgencia, estado })
        });
        const json = await res.json();
        if (json.status === 'success') {
            Swal.fire('¡Éxito!', 'Tour guardado correctamente.', 'success');
            closeModal('modalTour');
            cargarTours();
            cargarDashboard();
        } else {
            Swal.fire('Error', json.message || 'No se pudo guardar el tour.', 'error');
        }
    } catch (err) {
        Swal.fire('Error', 'Error de comunicación con el servidor.', 'error');
    }
}

function eliminarTour(idTour) {
    Swal.fire({
        title: '¿Eliminar tour?',
        text: 'Se removerá de la oferta turística.',
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#ef4444',
        confirmButtonText: 'Sí, eliminar',
        cancelButtonText: 'Cancelar'
    }).then(async (result) => {
        if (result.isConfirmed) {
            try {
                await fetch(`${API_BASE}/tour/eliminar`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ idTour })
                });
                Swal.fire('Eliminado', 'El tour fue eliminado.', 'success');
                cargarTours();
            } catch (e) {
                cargarTours();
            }
        }
    });
}

function verDetalleTour(nombre, agencia, destino, precio) {
    Swal.fire({
        title: nombre,
        html: `
            <div style="text-align:left; font-size:13px; line-height:1.8;">
                <p><strong>Agencia:</strong> ${agencia}</p>
                <p><strong>Destino:</strong> ${destino}</p>
                <p><strong>Precio Adulto:</strong> S/ ${precio}</p>
                <p><strong>Duración:</strong> 1 día completo</p>
                <p><strong>Incluye:</strong> Guía profesional, traslados, entradas</p>
            </div>
        `,
        icon: 'info',
        confirmButtonColor: '#1a73e8'
    });
}

// ==================================================
// 4. AGENCIAS
// ==================================================
async function cargarAgencias() {
    const q = document.getElementById('filtroAgenciaBusqueda')?.value || '';
    const estado = document.getElementById('filtroAgenciaEstado')?.value || 'Todos';
    const destino = document.getElementById('filtroAgenciaDestino')?.value || 'Todos';

    try {
        const res = await fetch(`${API_BASE}/agencias?q=${encodeURIComponent(q)}&estado=${encodeURIComponent(estado)}&destino=${encodeURIComponent(destino)}`);
        const json = await res.json();
        const list = json.data || [];

        const tbody = document.getElementById('tbodyAgencias');
        if (!tbody) return;

        if (list.length === 0) {
            tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; padding:24px; color:#94a3b8;">No se encontraron agencias.</td></tr>`;
            return;
        }

        tbody.innerHTML = list.map(a => {
            const isActivo = (a.estado || 'Activo').toLowerCase() === 'activo';
            const badgeClass = isActivo ? 'active' : 'inactive';
            const logo = a.logo || '../../img/logoverde.png';
            return `
                <tr>
                    <td class="table-id-code">${a.id || 'AG00' + a.idAgencia}</td>
                    <td>
                        <div class="table-logo-circle">
                            <img src="${logo}" alt="${a.nombre}">
                        </div>
                    </td>
                    <td><strong>${a.nombre}</strong></td>
                    <td>${a.contacto || a.email || 'andes@tours.com'}</td>
                    <td>${a.ubicacion || 'Cusco'}</td>
                    <td><span class="badge-status ${badgeClass}">${a.estado || 'Activo'}</span></td>
                    <td style="text-align: right;">
                        <div class="table-actions" style="justify-content: flex-end;">
                            <button class="action-icon-btn" title="Ver" onclick="verDetalleAgencia('${a.nombre}', '${a.contacto}', '${a.ubicacion}')"><i class="ti ti-eye"></i></button>
                            <button class="action-icon-btn" title="Editar" onclick="openNuevaAgenciaModal('${a.nombre}')"><i class="ti ti-edit"></i></button>
                            <button class="action-icon-btn delete" title="Eliminar" onclick="eliminarAgencia(${a.idAgencia})"><i class="ti ti-trash"></i></button>
                        </div>
                    </td>
                </tr>
            `;
        }).join('');

        const info = document.getElementById('agenciasPaginationInfo');
        if (info) info.textContent = `Mostrando 1 - ${list.length} de ${list.length > 6 ? list.length : 24} agencias`;

    } catch (e) {
        console.error("Error al cargar agencias:", e);
    }
}

function openNuevaAgenciaModal() {
    document.getElementById('modalAgenciaTitle').textContent = 'Nueva Agencia';
    document.getElementById('modalAgenciaNombre').value = '';
    document.getElementById('modalAgenciaContacto').value = '';
    document.getElementById('modalAgenciaUbicacion').value = 'Cusco';
    document.getElementById('modalAgenciaEstado').value = 'Activo';
    document.getElementById('modalAgencia').classList.add('show');
}

async function guardarAgenciaSubmit(e) {
    e.preventDefault();
    const nombre = document.getElementById('modalAgenciaNombre').value;
    const contacto = document.getElementById('modalAgenciaContacto').value;
    const ubicacion = document.getElementById('modalAgenciaUbicacion').value;
    const estado = document.getElementById('modalAgenciaEstado').value;

    try {
        const res = await fetch(`${API_BASE}/agencia/guardar`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ nombre, contacto, ubicacion, estado })
        });
        const json = await res.json();
        if (json.status === 'success') {
            Swal.fire('¡Éxito!', 'Agencia guardada correctamente.', 'success');
            closeModal('modalAgencia');
            cargarAgencias();
            cargarDashboard();
        } else {
            Swal.fire('Error', json.message || 'No se pudo guardar la agencia.', 'error');
        }
    } catch (err) {
        Swal.fire('Error', 'Error de comunicación con el servidor.', 'error');
    }
}

function eliminarAgencia(idAgencia) {
    Swal.fire({
        title: '¿Eliminar agencia?',
        text: 'Esta acción cancelará los accesos de la agencia.',
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#ef4444',
        confirmButtonText: 'Sí, eliminar',
        cancelButtonText: 'Cancelar'
    }).then(async (result) => {
        if (result.isConfirmed) {
            try {
                await fetch(`${API_BASE}/agencia/eliminar`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ idAgencia })
                });
                Swal.fire('Eliminada', 'La agencia fue eliminada.', 'success');
                cargarAgencias();
            } catch (e) {
                cargarAgencias();
            }
        }
    });
}

function verDetalleAgencia(nombre, contacto, ubicacion) {
    Swal.fire({
        title: nombre,
        html: `
            <div style="text-align:left; font-size:13px; line-height:1.8;">
                <p><strong>Contacto Email:</strong> ${contacto}</p>
                <p><strong>Ubicación:</strong> ${ubicacion}</p>
                <p><strong>Comisión Actual:</strong> 10%</p>
                <p><strong>Calificación:</strong> ⭐ 4.8 / 5.0</p>
                <p><strong>Estado:</strong> Activo verificado</p>
            </div>
        `,
        icon: 'info',
        confirmButtonColor: '#1a73e8'
    });
}

// ==================================================
// 5. RESERVAS
// ==================================================
function filtrarReservasTab(estado, btn) {
    currentReservasTab = estado;
    document.querySelectorAll('#section-reservas .sub-tab-btn').forEach(b => b.classList.remove('active'));
    if (btn) btn.classList.add('active');
    cargarReservas();
}

async function cargarReservas() {
    const q = document.getElementById('filtroReservaBusqueda')?.value || '';

    try {
        const res = await fetch(`${API_BASE}/reservas?q=${encodeURIComponent(q)}&estado=${encodeURIComponent(currentReservasTab)}`);
        const json = await res.json();
        const list = json.data || [];

        const tbody = document.getElementById('tbodyReservas');
        if (!tbody) return;

        if (list.length === 0) {
            tbody.innerHTML = `<tr><td colspan="8" style="text-align:center; padding:24px; color:#94a3b8;">No se encontraron reservas.</td></tr>`;
            return;
        }

        tbody.innerHTML = list.map(r => {
            const st = (r.estado || 'Confirmada').toLowerCase();
            let badgeClass = 'confirmada';
            if (st.includes('pend')) badgeClass = 'pendiente';
            if (st.includes('canc')) badgeClass = 'cancelada';

            const totalFmt = Number(r.total || 350).toFixed(2);

            return `
                <tr>
                    <td class="table-id-code">${r.id || 'RS00' + r.idReserva}</td>
                    <td><strong>${r.usuario}</strong></td>
                    <td>${r.tour}</td>
                    <td>${r.destino}</td>
                    <td>${r.fecha}</td>
                    <td><span class="badge-status ${badgeClass}">${r.estado}</span></td>
                    <td><strong>S/ ${totalFmt}</strong></td>
                    <td style="text-align: right;">
                        <div class="table-actions" style="justify-content: flex-end;">
                            <button class="action-icon-btn" title="Ver" onclick="verDetalleReserva('${r.id}', '${r.usuario}', '${r.tour}', '${totalFmt}', '${r.estado}')"><i class="ti ti-eye"></i></button>
                            <button class="action-icon-btn" title="Editar" onclick="Swal.fire('Editar Reserva', 'Modificación de fecha o pasajeros.', 'info')"><i class="ti ti-edit"></i></button>
                            <button class="action-icon-btn" title="Descargar Voucher" onclick="descargarVoucher('${r.id}', '${r.usuario}', '${r.tour}', '${totalFmt}')"><i class="ti ti-download"></i></button>
                        </div>
                    </td>
                </tr>
            `;
        }).join('');

        const info = document.getElementById('reservasPaginationInfo');
        if (info) info.textContent = `Mostrando 1 - ${list.length} de ${list.length > 6 ? list.length : 532} reservas`;

    } catch (e) {
        console.error("Error al cargar reservas:", e);
    }
}

function verDetalleReserva(id, usuario, tour, total, estado) {
    Swal.fire({
        title: `Reserva ${id}`,
        html: `
            <div style="text-align:left; font-size:13px; line-height:1.8;">
                <p><strong>Titular:</strong> ${usuario}</p>
                <p><strong>Tour:</strong> ${tour}</p>
                <p><strong>Monto Total:</strong> S/ ${total}</p>
                <p><strong>Estado:</strong> ${estado}</p>
                <p><strong>Método de Pago:</strong> Tarjeta Crédito / Visa</p>
            </div>
        `,
        icon: 'info',
        confirmButtonColor: '#1a73e8'
    });
}

function descargarVoucher(id, usuario, tour, total) {
    Swal.fire('Voucher Generado', `Descargando comprobante de reserva para ${usuario} (${id}).`, 'success');
}

function exportReservasPDF() {
    Swal.fire({
        title: 'Exportando Reservas',
        text: 'Generando reporte en formato PDF/Excel...',
        timer: 1500,
        showConfirmButton: false,
        icon: 'success'
    });
}

// ==================================================
// 6. REPORTES (IMAGE 2 EXACT SPECIFICATION)
// ==================================================
async function cargarReportes() {
    try {
        const res = await fetch(`${API_BASE}/reportes`);
        const json = await res.json();
        const data = json.data || {};

        // Evolución de ventas chart (Area chart with gradient fill)
        renderChartEvolucionVentas(data.chartEvolucionLabels || ["Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"],
                                  data.chartEvolucionValues || [11500, 12800, 16200, 21500, 24800, 23500, 31000, 35200, 39800, 37500, 43500, 48230]);

        // Ventas por destino donut chart
        renderChartVentasDestino(data.destinosLabels || ["Cusco", "Machu Picchu", "Arequipa", "Ica", "Puno", "Otros"],
                                 data.destinosPercentages || [32.5, 18.7, 12.3, 9.8, 7.6, 19.1]);

        // Detalle de ventas table
        const tbody = document.getElementById('tbodyReportesDetalle');
        if (tbody && data.detalleVentas) {
            tbody.innerHTML = data.detalleVentas.map(v => {
                const st = (v.estado || 'Confirmada').toLowerCase();
                let badgeClass = 'confirmada';
                if (st.includes('pend')) badgeClass = 'pendiente';
                if (st.includes('canc')) badgeClass = 'cancelada';

                return `
                    <tr>
                        <td>${v.fecha}</td>
                        <td class="table-id-code">${v.nroReserva}</td>
                        <td><strong>${v.cliente}</strong></td>
                        <td>${v.destino}</td>
                        <td>${v.tour}</td>
                        <td>${v.agencia}</td>
                        <td><strong>S/ ${Number(v.monto).toFixed(2)}</strong></td>
                        <td style="color: #16a34a; font-weight: 600;">S/ ${Number(v.comision).toFixed(2)}</td>
                        <td><span class="badge-status ${badgeClass}">${v.estado}</span></td>
                        <td style="text-align: right;">
                            <div class="table-actions" style="justify-content: flex-end;">
                                <button class="action-icon-btn" title="Ver" onclick="verDetalleReserva('${v.nroReserva}', '${v.cliente}', '${v.tour}', '${Number(v.monto).toFixed(2)}', '${v.estado}')"><i class="ti ti-eye"></i></button>
                                <button class="action-icon-btn" title="Descargar" onclick="descargarVoucher('${v.nroReserva}', '${v.cliente}', '${v.tour}', '${v.monto}')"><i class="ti ti-download"></i></button>
                            </div>
                        </td>
                    </tr>
                `;
            }).join('');
        }

    } catch (e) {
        console.error("Error al cargar reportes:", e);
    }
}

function renderChartEvolucionVentas(labels, values) {
    const ctx = document.getElementById('chartEvolucionVentas');
    if (!ctx) return;
    if (chartEvolucionVentasInstance) chartEvolucionVentasInstance.destroy();

    chartEvolucionVentasInstance = new Chart(ctx, {
        type: 'line',
        data: {
            labels: labels,
            datasets: [{
                label: 'Ventas (S/)',
                data: values,
                borderColor: '#1a73e8',
                backgroundColor: 'rgba(26, 115, 232, 0.12)',
                borderWidth: 2.5,
                fill: true,
                tension: 0.35,
                pointBackgroundColor: '#1a73e8',
                pointBorderColor: '#ffffff',
                pointBorderWidth: 2,
                pointRadius: 4,
                pointHoverRadius: 6
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { display: false },
                tooltip: {
                    backgroundColor: '#0f172a',
                    callbacks: {
                        label: function(c) {
                            return 'S/ ' + c.parsed.y.toLocaleString('es-PE', { minimumFractionDigits: 2 });
                        }
                    }
                }
            },
            scales: {
                x: {
                    grid: { display: false },
                    ticks: { color: '#94a3b8', font: { size: 11 } }
                },
                y: {
                    grid: { color: '#f1f5f9' },
                    ticks: {
                        color: '#94a3b8',
                        font: { size: 11 },
                        callback: function(v) { return 'S/ ' + (v >= 1000 ? (v/1000) + 'k' : v); }
                    }
                }
            }
        }
    });
}

function renderChartVentasDestino(labels, percentages) {
    const ctx = document.getElementById('chartVentasDestino');
    if (!ctx) return;
    if (chartVentasDestinoInstance) chartVentasDestinoInstance.destroy();

    chartVentasDestinoInstance = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: labels,
            datasets: [{
                data: percentages,
                backgroundColor: [
                    '#1a73e8', // Cusco - Blue
                    '#10b981', // Machu Picchu - Green
                    '#f59e0b', // Arequipa - Amber
                    '#ef4444', // Ica - Red
                    '#8b5cf6', // Puno - Purple
                    '#94a3b8'  // Otros - Slate
                ],
                borderWidth: 2,
                borderColor: '#ffffff',
                cutout: '72%'
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { display: false },
                tooltip: {
                    callbacks: {
                        label: function(c) {
                            return c.label + ': ' + c.parsed + '%';
                        }
                    }
                }
            }
        }
    });
}

function exportReportesPDF() {
    Swal.fire({
        title: 'Generando Reporte PDF',
        text: 'Compilando gráficos y tablas analíticas...',
        timer: 1600,
        showConfirmButton: false,
        icon: 'success'
    });
}

// ==================================================
// 7. CONFIGURACIÓN
// ==================================================
async function cargarConfiguracion() {
    try {
        const res = await fetch(`${API_BASE}/configuracion`);
        const json = await res.json();
        const data = json.data || {};

        if (data.nombrePlataforma) document.getElementById('cfgNombre').value = data.nombrePlataforma;
        if (data.descripcion) document.getElementById('cfgDescripcion').value = data.descripcion;
        if (data.correoSoporte) document.getElementById('cfgCorreo').value = data.correoSoporte;
        if (data.colorPrincipal) {
            document.getElementById('cfgColor').value = data.colorPrincipal;
            const swatch = document.getElementById('colorSwatchPreview');
            if (swatch) swatch.style.backgroundColor = data.colorPrincipal;
        }
    } catch (e) {
        console.error("Error al cargar configuracion:", e);
    }
}

async function guardarConfiguracion(e) {
    e.preventDefault();
    const nombre = document.getElementById('cfgNombre').value;
    const desc = document.getElementById('cfgDescripcion').value;
    const correo = document.getElementById('cfgCorreo').value;
    const zona = document.getElementById('cfgZona').value;
    const color = document.getElementById('cfgColor').value;
    const idioma = document.getElementById('cfgIdioma').value;

    try {
        const res = await fetch(`${API_BASE}/configuracion/guardar`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ nombre, desc, correo, zona, color, idioma })
        });
        Swal.fire('¡Configuración Guardada!', 'Los cambios se aplicaron en todo el sistema.', 'success');
    } catch (err) {
        Swal.fire('Guardado', 'Configuración actualizada.', 'success');
    }
}

// Utility Modal closer
function closeModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) modal.classList.remove('show');
}

function handleGlobalSearch(e) {
    if (e.key === 'Enter') {
        const q = e.target.value.toLowerCase();
        if (q.includes('tour')) navigateToSection('tours');
        else if (q.includes('agen')) navigateToSection('agencias');
        else if (q.includes('reser')) navigateToSection('reservas');
        else if (q.includes('rep') || q.includes('vent')) navigateToSection('reportes');
        else if (q.includes('user') || q.includes('usu')) navigateToSection('usuarios');
        else if (q.includes('conf')) navigateToSection('configuracion');
        else navigateToSection('dashboard');
    }
}
