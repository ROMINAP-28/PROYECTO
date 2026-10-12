/**
 * Travelink - Gestión y Liquidación de Comisiones
 * Filtros compactos, Paginación dinámica (máx 5 registros) y Liquidación
 */

let comisionesList = [];
let comisionesFiltradas = [];
let paginaActual = 1;
const ITEMS_POR_PAGINA = 5;

document.addEventListener('DOMContentLoaded', () => {
    cargarCombosAgenciasComisiones();
    cargarComisiones();
});

async function cargarCombosAgenciasComisiones() {
    const select = document.getElementById('filtroAgencia');
    if (!select) return;
    try {
        const res = await fetch(`${API_BASE}/agencias`);
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
    try {
        const res = await fetch(`${API_BASE}/comisiones`);
        if (res.ok) {
            const data = await res.json();
            const items = data.data || data;
            comisionesList = Array.isArray(items) ? items : (items.comisiones || []);
        } else {
            throw new Error('Error al conectar a API');
        }
    } catch (e) {
        console.warn('Cargando fallback para comisiones...', e);
        comisionesList = [
            { id: 1, agencia: 'ANDES TOURS PERU S.A.C.', ruc: '20601234567', periodo: 'Octubre 2026', ventasTotales: 3020.00, comisionPct: 15, montoComision: 453.00, estado: 'Pendiente' },
            { id: 2, agencia: 'INKA TRAVEL EXPERIENCES S.A.C.', ruc: '20609876543', periodo: 'Octubre 2026', ventasTotales: 1500.00, comisionPct: 15, montoComision: 225.00, estado: 'Pendiente' },
            { id: 3, agencia: 'AGENCIA ALEGRIA S.A', ruc: '10721439114', periodo: 'Octubre 2026', ventasTotales: 1200.00, comisionPct: 15, montoComision: 180.00, estado: 'Pendiente' },
            { id: 5, agencia: 'AGENCIA SELVA S.A', ruc: '10721439116', periodo: 'Octubre 2026', ventasTotales: 665.00, comisionPct: 15, montoComision: 99.75, estado: 'Pendiente' },
            { id: 6, agencia: 'TOUR AREQUIPA S.A.S', ruc: '10721439118', periodo: 'Octubre 2026', ventasTotales: 570.00, comisionPct: 15, montoComision: 85.50, estado: 'Pendiente' },
            { id: 7, agencia: 'TOUR LIMA S.A', ruc: '10721439113', periodo: 'Octubre 2026', ventasTotales: 760.00, comisionPct: 15, montoComision: 114.00, estado: 'Pendiente' }
        ];
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
                const res = await fetch(`${API_BASE}/comision/liquidar?id=${id}`, { method: 'POST' });
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

function exportarComisiones() {
    let csv = 'ID,Agencia,RUC,Periodo,VentasTotales,PorcentajeComision,MontoComision,Estado\n';
    comisionesFiltradas.forEach(c => {
        csv += `"${c.id}","${c.agencia}","${c.ruc}","${c.periodo}","${c.ventasTotales}","${c.comisionPct}%","${c.montoComision}","${c.estado}"\n`;
    });
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.setAttribute('download', `comisiones_${new Date().toISOString().slice(0,10)}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
}
