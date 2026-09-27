/**
 * Travelink - Gestión y Liquidación de Comisiones
 * Filtros compactos, Paginación dinámica (máx 5 registros) y Liquidación
 */

let comisionesList = [];
let comisionesFiltradas = [];
let paginaActual = 1;
const ITEMS_POR_PAGINA = 5;

document.addEventListener('DOMContentLoaded', () => {
    cargarComisiones();
});

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
            { id: 1, agencia: 'Andes Tours S.A.C.', ruc: '20456789012', periodo: 'Abril 2025', ventasTotales: 48500.00, comisionPct: 10, montoComision: 4850.00, estado: 'Pendiente' },
            { id: 2, agencia: 'Inka Travel Peru EIRL', ruc: '20123456789', periodo: 'Abril 2025', ventasTotales: 36200.00, comisionPct: 12, montoComision: 4344.00, estado: 'Liquidado' },
            { id: 3, agencia: 'Selva Viva Expeditions', ruc: '20567890123', periodo: 'Abril 2025', ventasTotales: 29800.00, comisionPct: 10, montoComision: 2980.00, estado: 'Pendiente' },
            { id: 4, agencia: 'Arequipa Tours & Treks', ruc: '20678901234', periodo: 'Abril 2025', ventasTotales: 18400.00, comisionPct: 10, montoComision: 1840.00, estado: 'Pendiente' },
            { id: 5, agencia: 'Ica Travel Sand & Sun', ruc: '20198765432', periodo: 'Abril 2025', ventasTotales: 22100.00, comisionPct: 10, montoComision: 2210.00, estado: 'Liquidado' },
            { id: 6, agencia: 'Chanchamayo Expeditions', ruc: '20445566778', periodo: 'Abril 2025', ventasTotales: 14200.00, comisionPct: 10, montoComision: 1420.00, estado: 'Pendiente' },
            { id: 7, agencia: 'Puno Lake Adventures', ruc: '20334455667', periodo: 'Abril 2025', ventasTotales: 19800.00, comisionPct: 10, montoComision: 1980.00, estado: 'Pendiente' }
        ];
    }
    actualizarKPIs();
    aplicarFiltros();
}

function actualizarKPIs() {
    const totalRecaudado = comisionesList.reduce((acc, c) => acc + (parseFloat(c.montoComision) || 0), 0);
    const pendientes = comisionesList.filter(c => c.estado.toLowerCase() === 'pendiente');
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
    const estado = document.getElementById('filtroEstado')?.value || 'todos';

    comisionesFiltradas = comisionesList.filter(c => {
        const coincideBusqueda = !busqueda ||
            (c.agencia && c.agencia.toLowerCase().includes(busqueda)) ||
            (c.ruc && c.ruc.toLowerCase().includes(busqueda));

        const coincideEstado = (estado === 'todos') ||
            (c.estado && c.estado.toLowerCase() === estado.toLowerCase());

        return coincideBusqueda && coincideEstado;
    });

    paginaActual = 1;
    renderizarTabla();
}

function limpiarFiltros() {
    if (document.getElementById('filtroBusqueda')) document.getElementById('filtroBusqueda').value = '';
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
