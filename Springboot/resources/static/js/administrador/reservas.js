/**
 * Travelink - Gestión de Reservas
 * Sub-tabs, Filtros compactos, Paginación dinámica (máx 5 registros), Detalle y Eliminación
 */

let reservasList = [];
let reservasFiltradas = [];
let subTabActual = 'todas';
let paginaActual = 1;
const ITEMS_POR_PAGINA = 5;

document.addEventListener('DOMContentLoaded', () => {
    cargarReservas();
});

async function cargarReservas() {
    const tbody = document.getElementById('tablaReservasBody');
    if (tbody) {
        tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; padding: 32px; color:#64748b;"><i class="fas fa-spinner fa-spin" style="margin-right:8px; color:#196f3d;"></i> Cargando reservas desde la base de datos...</td></tr>`;
    }
    try {
        const fetchFn = window.apiFetch || fetch;
        const res = await fetchFn(`${API_ADMIN}/reservas`, { credentials: 'include' });
        if (res.ok) {
            const data = await res.json();
            reservasList = Array.isArray(data) ? data : (data.data || data.reservas || []);
        } else {
            throw new Error('Error al conectar a API');
        }
    } catch (e) {
        console.error('No se pudieron cargar las reservas reales desde la API:', e);
        reservasList = [];
        const tbody = document.getElementById('tablaReservasBody');
        if (tbody) tbody.innerHTML = '<tr><td colspan="7" style="text-align:center;padding:24px;color:#b91c1c">No se pudieron cargar las reservas de la base de datos. Comprueba que el servidor Java esté activo.</td></tr>';
    }
    actualizarComboAgencias();
    aplicarFiltros();
}

function actualizarComboAgencias() {
    const sel = document.getElementById('filtroAgencia');
    if (!sel) return;
    const valorPrevio = sel.value;
    const agencias = [...new Set(reservasList.map(r => r.agencia).filter(Boolean))].sort();
    sel.innerHTML = '<option value="todos">Agencia: Todas</option>' +
        agencias.map(a => `<option value="${a}">${a}</option>`).join('');
    if (agencias.includes(valorPrevio)) {
        sel.value = valorPrevio;
    }
}

function cambiarSubTab(tab, btn) {
    subTabActual = tab;
    document.querySelectorAll('.sub-tab-btn').forEach(b => b.classList.remove('active'));
    if (btn) btn.classList.add('active');
    aplicarFiltros();
}

function aplicarFiltros() {
    const busqueda = (document.getElementById('filtroBusqueda')?.value || '').toLowerCase().trim();
    const agencia = document.getElementById('filtroAgencia')?.value || 'todos';

    reservasFiltradas = reservasList.filter(r => {
        const coincideBusqueda = !busqueda ||
            (r.codigo && r.codigo.toLowerCase().includes(busqueda)) ||
            (r.turista && r.turista.toLowerCase().includes(busqueda)) ||
            (r.email && r.email.toLowerCase().includes(busqueda)) ||
            (r.tour && r.tour.toLowerCase().includes(busqueda));

        const coincideAgencia = (agencia === 'todos') || 
            (r.agencia && r.agencia.toLowerCase() === agencia.toLowerCase());

        const coincideSubTab = (subTabActual === 'todas') || 
            (r.estado && r.estado.toLowerCase() === subTabActual.toLowerCase());

        return coincideBusqueda && coincideAgencia && coincideSubTab;
    });

    paginaActual = 1;
    renderizarTabla();
}

function limpiarFiltros() {
    if (document.getElementById('filtroBusqueda')) document.getElementById('filtroBusqueda').value = '';
    if (document.getElementById('filtroAgencia')) document.getElementById('filtroAgencia').value = 'todos';
    cambiarSubTab('todas', document.querySelector('.sub-tab-btn'));
}

function cambiarPaginaReservas(p) {
    const totalPages = Math.ceil(reservasFiltradas.length / ITEMS_POR_PAGINA) || 1;
    if (p < 1 || p > totalPages) return;
    paginaActual = p;
    renderizarTabla();
}

function renderizarTabla() {
    const tbody = document.getElementById('tablaReservasBody');
    if (!tbody) return;

    if (reservasFiltradas.length === 0) {
        tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; padding: 24px; color:#94a3b8;">No se encontraron reservas con los criterios aplicados.</td></tr>`;
        renderPaginator(0, 1, ITEMS_POR_PAGINA, 'paginacionControles', 'paginacionInfo', cambiarPaginaReservas);
        return;
    }

    const inicio = (paginaActual - 1) * ITEMS_POR_PAGINA;
    const fin = inicio + ITEMS_POR_PAGINA;
    const items = reservasFiltradas.slice(inicio, fin);

    tbody.innerHTML = items.map(r => {
        const estadoClase = (r.estado || '').toLowerCase();
        const inicial = (r.turista || 'T').charAt(0).toUpperCase();

        return `
            <tr>
                <td><span class="table-id-code">#${r.codigo || r.id}</span></td>
                <td>
                    <div class="user-cell-meta">
                        <div class="user-avatar-circle">${inicial}</div>
                        <div class="user-text-wrap">
                            <h6>${r.turista}</h6>
                            <span>${r.email}</span>
                        </div>
                    </div>
                </td>
                <td>
                    <div class="reserva-meta-cell">
                        <strong>${r.tour}</strong>
                        <span><i class="ti ti-building-store"></i> ${r.agencia}</span>
                    </div>
                </td>
                <td>
                    <div class="reserva-meta-cell">
                        <span><strong>${r.fechaHoraSalida || r.fechaTour}</strong></span>
                        <span style="font-size:0.82rem; color:#475569; font-weight:500;">
                            ${r.desglose ? r.desglose : `<i class="ti ti-users"></i> ${r.cupos} ${r.cupos === 1 ? 'persona' : 'personas'}`}
                        </span>
                    </div>
                </td>
                <td>
                    <strong>S/ ${(parseFloat(r.total) || 0).toFixed(2)}</strong>
                    <div style="font-size:0.75rem; color:#16a34a; font-weight:600;">
                        Comisión (15%): S/ ${(parseFloat(r.comision) || (parseFloat(r.total) * 0.15) || 0).toFixed(2)}
                    </div>
                </td>
                <td><span class="badge-status ${estadoClase}">${r.estado}</span></td>
                <td>
                    <div class="table-actions">
                        <button class="action-icon-btn" title="Ver Detalle / Voucher" onclick="verVoucher(${r.id})">
                            <i class="ti ti-receipt"></i>
                        </button>
                        ${estadoClase === 'pendiente' ? `
                            <button class="action-icon-btn" title="Aprobar / Confirmar" style="color:#16a34a;" onclick="cambiarEstadoReserva(${r.id}, 'Confirmada')">
                                <i class="ti ti-check"></i>
                            </button>
                        ` : ''}
                        ${estadoClase !== 'cancelada' ? `
                            <button class="action-icon-btn delete" title="Cancelar Reserva" onclick="cambiarEstadoReserva(${r.id}, 'Cancelada')">
                                <i class="ti ti-x"></i>
                            </button>
                        ` : ''}
                        <button class="action-icon-btn delete" title="Eliminar Reserva" onclick="eliminarReserva(${r.id}, '${r.codigo || ''}')">
                            <i class="ti ti-trash"></i>
                        </button>
                    </div>
                </td>
            </tr>
        `;
    }).join('');

    renderPaginator(reservasFiltradas.length, paginaActual, ITEMS_POR_PAGINA, 'paginacionControles', 'paginacionInfo', cambiarPaginaReservas);
}

function verVoucher(id) {
    const r = reservasList.find(item => item.id === id);
    if (!r) return;

    document.getElementById('vCodigo').textContent = r.codigo || `#${r.id}`;
    document.getElementById('vTurista').textContent = r.turista;
    document.getElementById('vEmail').textContent = r.email;
    document.getElementById('vTour').textContent = r.tour;
    document.getElementById('vAgencia').textContent = r.agencia;
    document.getElementById('vFechaTour').textContent = r.fechaHoraSalida || r.fechaTour;
    document.getElementById('vCupos').textContent = r.desglose || `${r.cupos} personas`;
    document.getElementById('vTotal').textContent = `S/ ${(parseFloat(r.total) || 0).toFixed(2)}`;
    const vComisionEl = document.getElementById('vComision');
    if (vComisionEl) {
        const com = parseFloat(r.comision) || (parseFloat(r.total) * 0.15) || 0;
        vComisionEl.textContent = `S/ ${com.toFixed(2)}`;
    }
    document.getElementById('vEstado').textContent = r.estado;

    document.getElementById('modalVoucher').classList.add('show');
}

function cambiarEstadoReserva(id, nuevoEstado) {
    const r = reservasList.find(item => item.id === id);
    if (!r) return;

    Swal.fire({
        title: `¿Cambiar estado a ${nuevoEstado}?`,
        text: `La reserva ${r.codigo || r.id} pasará a estar ${nuevoEstado}.`,
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: nuevoEstado === 'Confirmada' ? '#16a34a' : '#ef4444',
        cancelButtonColor: '#94a3b8',
        confirmButtonText: 'Sí, cambiar',
        cancelButtonText: 'Cancelar'
    }).then(async (result) => {
        if (result.isConfirmed) {
            try {
                const fetchFn = window.apiFetch || fetch;
                const res = await fetchFn(`${API_ADMIN}/reservas`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({
                        id: r.id,
                        codigo: r.codigo,
                        estado: nuevoEstado
                    })
                });
                const resData = await res.json();
                if (res.ok && resData.status === 'success') {
                    r.estado = nuevoEstado;
                    aplicarFiltros();
                    Swal.fire('Actualizado', `La reserva ahora está ${nuevoEstado} en la base de datos.`, 'success');
                } else {
                    throw new Error(resData.message || 'Error al actualizar');
                }
            } catch (e) {
                console.error('Error al actualizar estado:', e);
                Swal.fire('Error', 'No se pudo actualizar el estado en la base de datos: ' + e.message, 'error');
            }
        }
    });
}

function eliminarReserva(id, codigo) {
    const r = reservasList.find(item => item.id === id);
    const cod = codigo || (r ? r.codigo : `#${id}`);

    Swal.fire({
        title: '¿Eliminar reserva permanentemente?',
        text: `La reserva ${cod} será eliminada y sus cupos devueltos al paquete en la base de datos.`,
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
                const res = await fetchFn(`${API_ADMIN}/reservas`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({
                        action: 'eliminar',
                        id: id,
                        codigo: cod
                    })
                });
                const resData = await res.json();
                if (res.ok && resData.status === 'success') {
                    reservasList = reservasList.filter(item => item.id !== id);
                    actualizarComboAgencias();
                    aplicarFiltros();
                    Swal.fire('Eliminada', 'La reserva ha sido eliminada y sus cupos reintegrados con éxito.', 'success');
                } else {
                    throw new Error(resData.message || 'Error al eliminar reserva');
                }
            } catch (e) {
                console.error('Error al eliminar reserva:', e);
                Swal.fire('Error', 'No se pudo eliminar la reserva: ' + e.message, 'error');
            }
        }
    });
}

function exportarReservasExcel() {
    window.open('/api/admin/reportes/reservas.xlsx', '_blank');
}

function exportarReservas() {
    exportarReservasExcel();
}
