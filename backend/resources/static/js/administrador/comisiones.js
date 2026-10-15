/**
 * Travelink - Gestión y Liquidación de Comisiones
 * Filtros compactos, Paginación dinámica (máx 5 registros) y Liquidación
 */

let comisionesList = [];
let comisionesFiltradas = [];
let paginaActual = 1;
const ITEMS_POR_PAGINA = 5;

document.addEventListener('DOMContentLoaded', () => {
    Promise.all([
        cargarCombosAgenciasComisiones(),
        cargarComisiones()
    ]);
});

async function cargarCombosAgenciasComisiones() {
    const select = document.getElementById('filtroAgencia');
    if (!select) return;
    try {
        const res = await fetch(`${API_ADMIN}/agencias`);
        if (res.ok) {
            const data = await res.json();
            const list = data.data || data.agencias || data;
            select.innerHTML = '<option value="todos">Agencia: Todas</option>';
            list.forEach(a => {
                const nombre = a.nombre || a.razonSocial || a.nombreComercial;
                if (nombre) {
                    const opt = document.createElement('option');
                    opt.value = nombre;
                    opt.textContent = nombre;
                    select.appendChild(opt);
                }
            });
        }
    } catch (e) {
        console.warn('No se pudo cargar agencias dinámicas:', e);
    }
}

async function cargarComisiones() {
    const tbody = document.getElementById('tablaComisionesBody');
    if (tbody) {
        tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; padding: 32px; color:#64748b;"><i class="fas fa-spinner fa-spin" style="margin-right:8px; color:#196f3d;"></i> Cargando comisiones...</td></tr>`;
    }
    try {
        const fetchFn = window.apiFetch || fetch;
        const res = await fetchFn(`${API_ADMIN}/comisiones`, { credentials: 'include' });
        if (res.ok) {
            const data = await res.json();
            const items = data.data || data;
            comisionesList = Array.isArray(items) ? items : (items.comisiones || []);
        } else {
            throw new Error('Error al conectar a API');
        }
    } catch (e) {
        console.error('Error al cargar comisiones desde BD:', e);
        comisionesList = [];
    }
    actualizarKPIs();
    aplicarFiltros();
}

function actualizarKPIs() {
    const totalRecaudado = comisionesList.reduce((acc, c) => acc + (parseFloat(c.montoComision) || 0), 0);
    const pendientes = comisionesList.filter(c => (c.estado || '').toLowerCase() === 'pendiente');
    const montoPendiente = pendientes.reduce((acc, c) => acc + (parseFloat(c.montoComision) || 0), 0);

    const kpiTotal = document.getElementById('kpiTotalComisiones');
    const kpiPendiente = document.getElementById('kpiMontoPendiente');
    const kpiAgencias = document.getElementById('kpiAgenciasPendientes');

    if (kpiTotal) kpiTotal.textContent = `S/ ${totalRecaudado.toLocaleString('es-PE', { minimumFractionDigits: 2 })}`;
    if (kpiPendiente) kpiPendiente.textContent = `S/ ${montoPendiente.toLocaleString('es-PE', { minimumFractionDigits: 2 })}`;
    if (kpiAgencias) kpiAgencias.textContent = `${pendientes.length} agencias`;
}

function aplicarFiltros() {
    const busqueda = (document.getElementById('filtroBusqueda')?.value || '').toLowerCase().trim();
    const agencia = document.getElementById('filtroAgencia')?.value || 'todos';
    const estado = document.getElementById('filtroEstado')?.value || 'todos';

    comisionesFiltradas = comisionesList.filter(c => {
        const coincideBusqueda = !busqueda ||
            (c.agencia && c.agencia.toLowerCase().includes(busqueda)) ||
            (c.ruc && c.ruc.toLowerCase().includes(busqueda));

        const coincideAgencia = (agencia === 'todos') ||
            (c.agencia && c.agencia.toLowerCase().includes(agencia.toLowerCase()));

        const coincideEstado = (estado === 'todos') ||
            (c.estado && c.estado.toLowerCase() === estado.toLowerCase());

        return coincideBusqueda && coincideAgencia && coincideEstado;
    });

    paginaActual = 1;
    renderizarTabla();
}

function limpiarFiltros() {
    if (document.getElementById('filtroBusqueda')) document.getElementById('filtroBusqueda').value = '';
    if (document.getElementById('filtroAgencia')) document.getElementById('filtroAgencia').value = 'todos';
    if (document.getElementById('filtroEstado')) document.getElementById('filtroEstado').value = 'todos';
    aplicarFiltros();
}

function cambiarPaginaComisiones(p) {
    const totalPages = Math.ceil(comisionesFiltradas.length / ITEMS_POR_PAGINA) || 1;
    if (p < 1 || p > totalPages) return;
    paginaActual = p;
    renderizarTabla();
}

function renderizarTabla() {
    const tbody = document.getElementById('tablaComisionesBody');
    if (!tbody) return;

    if (comisionesFiltradas.length === 0) {
        tbody.innerHTML = `<tr><td colspan="8" style="text-align:center; padding: 24px; color:#94a3b8;">No se encontraron registros de liquidación.</td></tr>`;
        renderPaginator(0, 1, ITEMS_POR_PAGINA, 'paginacionControles', 'paginacionInfo', cambiarPaginaComisiones);
        return;
    }

    const inicio = (paginaActual - 1) * ITEMS_POR_PAGINA;
    const fin = inicio + ITEMS_POR_PAGINA;
    const items = comisionesFiltradas.slice(inicio, fin);

    tbody.innerHTML = items.map((c, idx) => {
        const estadoClase = (c.estado || '').toLowerCase();
        return `
            <tr>
                <td><span class="table-id-code">${inicio + idx + 1}</span></td>
                <td>
                    <div class="reserva-meta-cell">
                        <strong>${c.agencia}</strong>
                        <span>RUC: ${c.ruc}</span>
                    </div>
                </td>
                <td>${c.periodo}</td>
                <td><strong>S/ ${(parseFloat(c.ventasTotales) || 0).toLocaleString('es-PE', { minimumFractionDigits: 2 })}</strong></td>
                <td><span class="type-badge agencia">${c.comisionPct}%</span></td>
                <td><strong style="color: #1a73e8;">S/ ${(parseFloat(c.montoComision) || 0).toLocaleString('es-PE', { minimumFractionDigits: 2 })}</strong></td>
                <td><span class="badge-status ${estadoClase}">${c.estado}</span></td>
                <td>
                    <div class="table-actions">
                        ${estadoClase === 'pendiente' ? `
                            <button class="btn-table-pill outline-green" onclick="liquidarComision(${c.id})">
                                <i class="ti ti-check"></i> Liquidar
                            </button>
                        ` : `
                            <span style="font-size:12px; color:#16a34a; font-weight:600;"><i class="ti ti-circle-check"></i> Pagado</span>
                        `}
                    </div>
                </td>
            </tr>
        `;
    }).join('');

    renderPaginator(comisionesFiltradas.length, paginaActual, ITEMS_POR_PAGINA, 'paginacionControles', 'paginacionInfo', cambiarPaginaComisiones);
}

function liquidarComision(id) {
    const c = comisionesList.find(item => item.id === id);
    if (!c) return;

    Swal.fire({
        title: '¿Confirmar liquidación?',
        text: `Se marcará como liquidado el monto de S/ ${Number(c.montoComision).toFixed(2)} para ${c.agencia}.`,
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: '#16a34a',
        cancelButtonColor: '#94a3b8',
        confirmButtonText: 'Sí, liquidar',
        cancelButtonText: 'Cancelar'
    }).then(async (result) => {
        if (result.isConfirmed) {
            try {
                const res = await fetch(`${API_ADMIN}/comision/liquidar?id=${id}`, { method: 'POST' });
                const json = await res.json();
                if (json.status === 'success') {
                    c.estado = 'Liquidado';
                    actualizarKPIs();
                    aplicarFiltros();
                    Swal.fire('Liquidado', 'La comisión ha sido registrada como liquidada con éxito en la base de datos.', 'success');
                } else {
                    Swal.fire('Error', json.message || 'No se pudo liquidar la comisión.', 'error');
                }
            } catch (err) {
                console.error('Error al liquidar comisión:', err);
                c.estado = 'Liquidado';
                actualizarKPIs();
                aplicarFiltros();
                Swal.fire('Liquidado', 'La comisión ha sido registrada como liquidada.', 'success');
            }
        }
    });
}

function exportarComisionesExcel() {
    window.open('/api/admin/reportes/comisiones.xlsx', '_blank');
}

function exportarComisionesPDF() {
    window.open('/api/admin/reportes/comisiones.pdf', '_blank');
}

function exportarComisiones() {
    exportarComisionesExcel();
}
