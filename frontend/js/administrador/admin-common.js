/**
 * Travelink Admin Common Helpers
 * Session check, sidebar, notifications card, profile menu, and dynamic paginator (5 items/page)
 */

const API_SERVER = (window.location.protocol === 'file:' || (window.location.port && window.location.port !== '8080')) 
    ? 'http://localhost:8080' 
    : '';
const API_BASE = `${API_SERVER}/api/admin`;

document.addEventListener('DOMContentLoaded', () => {
    verificarSesionAdmin();
    setupDropdowns();
    cargarNotificacionesAdmin();
});

function verificarSesionAdmin() {
    const userJson = localStorage.getItem('travelink_user') || sessionStorage.getItem('travelink_user');
    if (!userJson) {
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
        console.error("Error al verificar sesión:", e);
    }
}

function setupDropdowns() {
    document.addEventListener('click', (e) => {
        const userMenu = document.getElementById('admin-dropdown-menu');
        const notifCard = document.getElementById('notificationsDropdownCard');
        
        if (userMenu && !e.target.closest('.user-dropdown-container')) {
            userMenu.classList.remove('show');
        }
        if (notifCard && !e.target.closest('.notification-btn') && !e.target.closest('.notifications-dropdown-card')) {
            notifCard.classList.remove('show');
        }
    });
}

function toggleAdminDropdown(e) {
    if (e) e.stopPropagation();
    const notifCard = document.getElementById('notificationsDropdownCard');
    if (notifCard) notifCard.classList.remove('show');

    const menu = document.getElementById('admin-dropdown-menu');
    if (menu) menu.classList.toggle('show');
}

function toggleNotifications(e) {
    if (e) e.stopPropagation();
    const userMenu = document.getElementById('admin-dropdown-menu');
    if (userMenu) userMenu.classList.remove('show');

    const notifCard = document.getElementById('notificationsDropdownCard');
    if (notifCard) {
        notifCard.classList.toggle('show');
        if (notifCard.classList.contains('show')) {
            cargarNotificacionesAdmin();
        }
    }
}

async function cargarNotificacionesAdmin() {
    try {
        const res = await fetch(`${API_BASE}/notificaciones?limite=15`);
        if (!res.ok) return;
        const json = await res.json();
        if (json.status === 'success' && json.data) {
            renderNotificacionesList(json.data.notificaciones || [], json.data.noLeidas || 0);
        }
    } catch (e) {
        console.error("Error cargando notificaciones de BD:", e);
    }
}

function renderNotificacionesList(notifs, noLeidas) {
    const badge = document.querySelector('.notification-dot');
    if (badge) {
        badge.style.display = noLeidas > 0 ? 'inline-block' : 'none';
    }

    const listEl = document.querySelector('#notificationsDropdownCard .notif-list');
    if (!listEl) return;

    if (notifs.length === 0) {
        listEl.innerHTML = `
            <div style="padding: 24px 16px; text-align: center; color: #94a3b8; font-size: 0.88rem;">
                <i class="ti ti-bell-off" style="font-size: 1.8rem; display: block; margin-bottom: 8px; color: #cbd5e1;"></i>
                No hay notificaciones registradas
            </div>`;
        return;
    }

    let html = '';
    notifs.forEach(item => {
        const iconClass = item.icono || 'ti ti-bell';
        const colorClass = item.color || 'blue';
        const unreadDot = (!item.leida) ? '<span class="notif-unread-dot"></span>' : '';
        const enlace = item.enlace ? item.enlace : '';

        html += `
            <div class="notif-item ${!item.leida ? 'notif-unread' : ''}" style="cursor: pointer;" onclick="abrirNotificacion(${item.id}, '${enlace}')">
                <div class="notif-icon-circle ${colorClass}"><i class="${iconClass}"></i></div>
                <div class="notif-content">
                    <p class="notif-text"><strong>${item.titulo}</strong> ${item.mensaje}</p>
                    <span class="notif-time">${item.tiempoRelativo || 'Reciente'}</span>
                </div>
                ${unreadDot}
            </div>
        `;
    });

    listEl.innerHTML = html;
}

async function abrirNotificacion(idNotificacion, enlace) {
    try {
        await fetch(`${API_BASE}/notificaciones/marcar-leida`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ idNotificacion })
        });
    } catch (e) {
        console.error("Error marcando notificación leída:", e);
    }
    cargarNotificacionesAdmin();
    if (enlace && enlace.trim() !== '') {
        window.location.href = enlace;
    }
}

async function marcarTodasLeidas() {
    try {
        await fetch(`${API_BASE}/notificaciones/marcar-todas`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({})
        });
        document.querySelectorAll('.notif-unread-dot').forEach(d => d.style.display = 'none');
        const badge = document.querySelector('.notification-dot');
        if (badge) badge.style.display = 'none';

        Swal.fire({
            toast: true,
            position: 'top-end',
            icon: 'success',
            title: 'Notificaciones marcadas como leídas',
            showConfirmButton: false,
            timer: 1500
        });
        cargarNotificacionesAdmin();
    } catch (e) {
        console.error("Error al marcar todas leídas en BD:", e);
    }
}

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
            sessionStorage.removeItem('travelink_user');
            window.location.href = './login.html';
        }
    });
}

function handleGlobalSearch(e) {
    if (e.key === 'Enter') {
        const q = e.target.value.toLowerCase().trim();
        if (q.includes('usu') || q.includes('user')) window.location.href = './usuarios.html';
        else if (q.includes('tour')) window.location.href = './tours.html';
        else if (q.includes('agen')) window.location.href = './agencias.html';
        else if (q.includes('rese')) window.location.href = './reservas.html';
        else if (q.includes('calid') || q.includes('revi')) window.location.href = './calidad.html';
        else if (q.includes('comis') || q.includes('liqui')) window.location.href = './comisiones.html';
        else if (q.includes('dest') || q.includes('cata')) window.location.href = './catalogo.html';
        else window.location.href = './dashboard.html';
    }
}

/**
 * Dynamic Pagination Utility for max 5 records per page
 */
function renderPaginator(totalItems, currentPage, itemsPerPage, controlsContainerId, infoContainerId, onPageChangeCallback) {
    const totalPages = Math.ceil(totalItems / itemsPerPage) || 1;
    const startItem = totalItems === 0 ? 0 : (currentPage - 1) * itemsPerPage + 1;
    const endItem = Math.min(currentPage * itemsPerPage, totalItems);

    const infoEl = document.getElementById(infoContainerId);
    if (infoEl) {
        infoEl.textContent = `Mostrando ${startItem} - ${endItem} de ${totalItems} registros`;
    }

    const controlsEl = document.getElementById(controlsContainerId);
    if (!controlsEl) return;

    let html = '';
    // Previous Button
    html += `<button class="page-btn" ${currentPage === 1 ? 'disabled' : ''} onclick="${onPageChangeCallback.name}(${currentPage - 1})"><i class="ti ti-chevron-left"></i></button>`;

    for (let p = 1; p <= totalPages; p++) {
        if (p === 1 || p === totalPages || (p >= currentPage - 1 && p <= currentPage + 1)) {
            html += `<button class="page-btn ${p === currentPage ? 'active' : ''}" onclick="${onPageChangeCallback.name}(${p})">${p}</button>`;
        } else if (p === currentPage - 2 || p === currentPage + 2) {
            html += `<span style="padding: 0 4px; color: #94a3b8;">...</span>`;
        }
    }

    // Next Button
    html += `<button class="page-btn" ${currentPage === totalPages ? 'disabled' : ''} onclick="${onPageChangeCallback.name}(${currentPage + 1})"><i class="ti ti-chevron-right"></i></button>`;

    controlsEl.innerHTML = html;
}

function closeModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) modal.classList.remove('show');
}
