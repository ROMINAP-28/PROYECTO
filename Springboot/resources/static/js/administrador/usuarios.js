/**
 * Travelink - Gestión de Usuarios
 * Trae todos los usuarios reales de la base de datos MySQL.
 * Filtros compactos y Paginación dinámica (máx 5 registros).
 */

let usuariosList = [];
let usuariosFiltrados = [];
let paginaActual = 1;
const ITEMS_POR_PAGINA = 5;

document.addEventListener('DOMContentLoaded', () => {
    cargarUsuarios();
});

async function cargarUsuarios() {
    try {
        const res = await fetch(`${API_ADMIN}/usuarios`);
        if (res.ok) {
            const json = await res.json();
            const items = json.data || json;
            usuariosList = Array.isArray(items) ? items : (items.usuarios || []);
        } else {
            throw new Error('No se pudo conectar a la API');
        }
    } catch (err) {
        console.error('Error al cargar usuarios:', err);
        usuariosList = [];
    }
    aplicarFiltros();
}

function aplicarFiltros() {
    const busqueda = (document.getElementById('filtroBusqueda')?.value || '').toLowerCase().trim();
    const rol = document.getElementById('filtroRol')?.value || 'todos';
    const estado = document.getElementById('filtroEstado')?.value || 'todos';

    usuariosFiltrados = usuariosList.filter(u => {
        const coincideBusqueda = !busqueda || 
            (u.nombre && u.nombre.toLowerCase().includes(busqueda)) ||
            (u.email && u.email.toLowerCase().includes(busqueda)) ||
            (u.id && u.id.toString().includes(busqueda));

        const coincideRol = (rol === 'todos') || 
            (u.tipo && u.tipo.toLowerCase() === rol.toLowerCase());

        const coincideEstado = (estado === 'todos') || 
            (u.estado && u.estado.toLowerCase() === estado.toLowerCase());

        return coincideBusqueda && coincideRol && coincideEstado;
    });

    paginaActual = 1;
    renderizarTabla();
}

function limpiarFiltros() {
    if (document.getElementById('filtroBusqueda')) document.getElementById('filtroBusqueda').value = '';
    if (document.getElementById('filtroRol')) document.getElementById('filtroRol').value = 'todos';
    if (document.getElementById('filtroEstado')) document.getElementById('filtroEstado').value = 'todos';
    aplicarFiltros();
}

function cambiarPaginaUsuarios(p) {
    const totalPages = Math.ceil(usuariosFiltrados.length / ITEMS_POR_PAGINA) || 1;
    if (p < 1 || p > totalPages) return;
    paginaActual = p;
    renderizarTabla();
}

function renderizarTabla() {
    const tbody = document.getElementById('tablaUsuariosBody');
    if (!tbody) return;

    if (usuariosFiltrados.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6" style="text-align:center; padding: 24px; color:#94a3b8;">No se encontraron usuarios con los filtros aplicados.</td></tr>`;
        renderPaginator(0, 1, ITEMS_POR_PAGINA, 'paginacionControles', 'paginacionInfo', cambiarPaginaUsuarios);
        return;
    }

    const inicio = (paginaActual - 1) * ITEMS_POR_PAGINA;
    const fin = inicio + ITEMS_POR_PAGINA;
    const items = usuariosFiltrados.slice(inicio, fin);

    tbody.innerHTML = items.map(u => {
        const tipoClase = (u.tipo || '').toLowerCase();
        const estadoClase = (u.estado || '').toLowerCase();
        const inicial = (u.nombre || 'U').charAt(0).toUpperCase();

        return `
            <tr>
                <td><span class="table-id-code">#USR-${String(u.id).padStart(3, '0')}</span></td>
                <td>
                    <div class="user-cell-meta">
                        <div class="user-avatar-circle">${inicial}</div>
                        <div class="user-text-wrap">
                            <h6>${u.nombre}</h6>
                            <span>${u.email}</span>
                        </div>
                    </div>
                </td>
                <td><span class="type-badge ${tipoClase}">${u.tipo}</span></td>
                <td><span class="badge-status ${estadoClase}">${u.estado}</span></td>
                <td>${u.fechaRegistro || '2026-09-26'}</td>
                <td>
                    <div class="table-actions">
                        <button class="action-icon-btn ${estadoClase === 'activo' ? 'delete' : ''}" title="${estadoClase === 'activo' ? 'Desactivar' : 'Activar'}" onclick="toggleEstadoUsuario(${u.id})">
                            <i class="ti ti-${estadoClase === 'activo' ? 'user-x' : 'user-check'}"></i>
                        </button>
                        <button class="action-icon-btn delete" title="Eliminar" onclick="eliminarUsuario(${u.id})">
                            <i class="ti ti-trash"></i>
                        </button>
                    </div>
                </td>
            </tr>
        `;
    }).join('');

    renderPaginator(usuariosFiltrados.length, paginaActual, ITEMS_POR_PAGINA, 'paginacionControles', 'paginacionInfo', cambiarPaginaUsuarios);
}

function toggleEstadoUsuario(id) {
    const u = usuariosList.find(item => item.id === id);
    if (!u) return;
    const nuevoEstado = (u.estado.toLowerCase() === 'activo') ? 'Inactivo' : 'Activo';

    Swal.fire({
        title: `¿Cambiar estado a ${nuevoEstado}?`,
        text: `El usuario ${u.nombre} pasará a estar ${nuevoEstado}.`,
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: '#1a73e8',
        cancelButtonColor: '#94a3b8',
        confirmButtonText: 'Sí, cambiar',
        cancelButtonText: 'Cancelar'
    }).then(async (result) => {
        if (result.isConfirmed) {
            u.estado = nuevoEstado;
            try {
                const fetchFn = window.apiFetch || fetch;
                await fetchFn(`${API_ADMIN}/usuario/guardar`, {
                    method: 'POST',
                    credentials: 'include',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ idUsuario: u.id, estado: nuevoEstado })
                });
            } catch (e) {
                console.error('Error al actualizar estado de usuario:', e);
            }
            aplicarFiltros();
            Swal.fire('Actualizado', `El estado ahora es ${nuevoEstado}`, 'success');
        }
    });
}

function eliminarUsuario(id) {
    Swal.fire({
        title: '¿Eliminar usuario?',
        text: 'El usuario pasará a estar INACTIVO en el sistema.',
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#ef4444',
        cancelButtonColor: '#94a3b8',
        confirmButtonText: 'Sí, eliminar',
        cancelButtonText: 'Cancelar'
    }).then(async (result) => {
        if (result.isConfirmed) {
            try {
                const fetchFn = window.apiFetch || fetch;
                await fetchFn(`${API_ADMIN}/usuario/eliminar`, {
                    method: 'POST',
                    credentials: 'include',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ idUsuario: id })
                });
            } catch (e) {
                console.error('Error al eliminar usuario:', e);
            }
            usuariosList = usuariosList.filter(u => u.id !== id);
            aplicarFiltros();
            Swal.fire('Eliminado', 'El usuario ha sido desactivado en la base de datos.', 'success');
        }
    });
}

function exportarUsuariosExcel() {
    window.open('/api/admin/reportes/usuarios.xlsx', '_blank');
}

function exportarUsuarios() {
    exportarUsuariosExcel();
}
