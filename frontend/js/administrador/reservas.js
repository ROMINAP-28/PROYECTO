?/**
 * Travelink - Gestión de Reservas
 * Sub-tabs, Filtros compactos, Paginación dinámica (máx 5 registros) y Detalle
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
    try {
        const res = await fetch(`${API_BASE}/reservas`);
        if (res.ok) {
            const data = await res.json();
            reservasList = Array.isArray(data) ? data : (data.data || data.reservas || []);
        } else {
            throw new Error('Error al conectar a API');
        }
    } catch (e) {
        console.warn('Cargando datos locales de fallback para reservas...', e);
        reservasList = [
            { id: 101, codigo: 'R-001245', turista: 'María López', email: 'maria.l@gmail.com', tour: 'Machu Picchu Clásico Full Day', agencia: 'Andes Tours', fechaTour: '2025-04-28', cupos: 2, total: 700.00, estado: 'Confirmada', fechaReserva: '2025-04-10' },
            { id: 102, codigo: 'R-001246', turista: 'Carlos Pérez', email: 'carlos.p@gmail.com', tour: 'Montaña de 7 Colores (Vinicunca)', agencia: 'Inka Travel', fechaTour: '2025-04-27', cupos: 1, total: 260.00, estado: 'Confirmada', fechaReserva: '2025-04-11' },
            { id: 103, codigo: 'R-001247', turista: 'Ana Torres', email: 'ana.t@gmail.com', tour: 'Valle Sagrado & Ollantaytambo', agencia: 'Selva Viva', fechaTour: '2025-04-29', cupos: 3, total: 960.00, estado: 'Pendiente', fechaReserva: '2025-04-12' },
            { id: 104, codigo: 'R-001248', turista: 'Jorge Ramírez', email: 'jorge.r@gmail.com', tour: 'Tour Islas Ballestas & Huacachina', agencia: 'Ica Travel', fechaTour: '2025-05-01', cupos: 2, total: 580.00, estado: 'Confirmada', fechaReserva: '2025-04-12' },
            { id: 105, codigo: 'R-001249', turista: 'Lucía Fernández', email: 'lucia.f@hotmail.com', tour: 'Cañón del Colca 2D/1N', agencia: 'Arequipa Tours', fechaTour: '2025-05-03', cupos: 2, total: 760.00, estado: 'Cancelada', fechaReserva: '2025-04-13' },
            { id: 106, codigo: 'R-001250', turista: 'Roberto Sánchez', email: 'roberto.s@gmail.com', tour: 'Lago Titicaca & Islas Uros', agencia: 'Machupicchu Tours', fechaTour: '2025-05-05', cupos: 4, total: 880.00, estado: 'Pendiente', fechaReserva: '2025-04-14' },
            { id: 107, codigo: 'R-001251', turista: 'Elena Castillo', email: 'elena.c@gmail.com', tour: 'Amazonía & Reserva Tambopata', agencia: 'Aventura Perú', fechaTour: '2025-05-10', cupos: 2, total: 1360.00, estado: 'Confirmada', fechaReserva: '2025-04-15' }
        ];
    }
    aplicarFiltros();
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
                        <span><strong>${r.fechaTour}</strong></span>
                        <span><i class="ti ti-users"></i> ${r.cupos} ${r.cupos === 1 ? 'persona' : 'personas'}</span>
                    </div>
                </td>
                <td><strong>S/ ${(parseFloat(r.total) || 0).toFixed(2)}</strong></td>
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
    document.getElementById('vFechaTour').textContent = r.fechaTour;
    document.getElementById('vCupos').textContent = `${r.cupos} personas`;
    document.getElementById('vTotal').textContent = `S/ ${(parseFloat(r.total) || 0).toFixed(2)}`;
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
            r.estado = nuevoEstado;
            try {
                await fetch(`${API_BASE}/reservas`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(r)
                });
            } catch (e) {}
            aplicarFiltros();
            Swal.fire('Actualizado', `La reserva ahora está ${nuevoEstado}`, 'success');
        }
    });
}

function exportarReservas() {
    let csv = 'Codigo,Turista,Email,Tour,Agencia,FechaTour,Cupos,Total,Estado\n';
    reservasFiltradas.forEach(r => {
        csv += `"${r.codigo || r.id}","${r.turista}","${r.email}","${r.tour}","${r.agencia}","${r.fechaTour}","${r.cupos}","${r.total}","${r.estado}"\n`;
    });
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.setAttribute('download', `reservas_${new Date().toISOString().slice(0,10)}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
}
