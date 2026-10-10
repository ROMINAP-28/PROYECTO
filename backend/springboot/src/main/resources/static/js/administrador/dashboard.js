?/**
 * Dashboard JavaScript
 * Analítica con Gráficos, Detalle de Ventas, 24 Departamentos y Selector de Fechas Personalizado
 */

let allVentas = [];
let filteredVentas = [];
let currentVentasPage = 1;
const VENTAS_PER_PAGE = 5;

let chartEvolucionInstance = null;
let chartDestinoInstance = null;

// Calendar Range State
let calCurrentYear = 2026;
let calCurrentMonth = 8; // 0-indexed: 8 = Septiembre
let calStartDate = new Date(2026, 8, 10);
let calEndDate = new Date(2026, 8, 25);
let calSelectingStart = true;

document.addEventListener('DOMContentLoaded', () => {
    cargarDashboard();
    initCalendar();
});

async function cargarDashboard() {
    try {
        const res = await fetch(`${API_BASE}/dashboard`);
        const json = await res.json();
        const data = json.data || {};

        // Actualizar Contadores KPI Dinámicos
        const elVentas = document.getElementById('kpiVentasTotales');
        if (elVentas && data.ventasTotales !== undefined) {
            elVentas.textContent = 'S/ ' + Number(data.ventasTotales).toLocaleString('es-PE', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
        }

        const elReservas = document.getElementById('kpiReservasRealizadas');
        if (elReservas && data.reservasRealizadas !== undefined) {
            elReservas.textContent = data.reservasRealizadas;
        }

        const elAgencias = document.getElementById('kpiAgenciasActivas');
        if (elAgencias && data.agenciasActivas !== undefined) {
            elAgencias.textContent = data.agenciasActivas;
        }

        const elComision = document.getElementById('kpiComisionGenerada');
        if (elComision && data.comisionGenerada !== undefined) {
            elComision.textContent = 'S/ ' + Number(data.comisionGenerada).toLocaleString('es-PE', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
        }

        allVentas = data.detalleVentas || [];
        filteredVentas = [...allVentas];

        renderChartEvolucion(data.chartEvolucionLabels, data.chartEvolucionValues);
        renderChartDestino(data.destinosLabels, data.destinosPercentages);

        renderVentasTable(1);
    } catch (e) {
        console.error("Error al cargar dashboard:", e);
    }
}

function renderChartEvolucion(labels, values) {
    const ctx = document.getElementById('chartEvolucionVentas');
    if (!ctx) return;
    if (chartEvolucionInstance) chartEvolucionInstance.destroy();

    const lbls = labels || ["Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"];
    const vals = values || [11500, 12800, 16200, 21500, 24800, 23500, 31000, 35200, 39800, 37500, 43500, 48230];

    chartEvolucionInstance = new Chart(ctx, {
        type: 'line',
        data: {
            labels: lbls,
            datasets: [{
                label: 'Ventas (S/)',
                data: vals,
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
                    callbacks: {
                        label: function(c) {
                            return 'S/ ' + Number(c.parsed.y).toLocaleString('es-PE', { minimumFractionDigits: 2 });
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

function renderChartDestino(labels, percentages) {
    const ctx = document.getElementById('chartVentasDestino');
    if (!ctx) return;
    if (chartDestinoInstance) chartDestinoInstance.destroy();

    const lbls = labels || ["Cusco", "Machu Picchu", "Arequipa", "Ica", "Puno", "Otros"];
    const percs = percentages || [32.5, 18.7, 12.3, 9.8, 7.6, 19.1];

    chartDestinoInstance = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: lbls,
            datasets: [{
                data: percs,
                backgroundColor: ['#1a73e8', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6', '#94a3b8'],
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

function renderVentasTable(page = 1) {
    currentVentasPage = page;
    const startIndex = (page - 1) * VENTAS_PER_PAGE;
    const pageItems = filteredVentas.slice(startIndex, startIndex + VENTAS_PER_PAGE);

    const tbody = document.getElementById('tbodyReportesDetalle');
    if (!tbody) return;

    if (pageItems.length === 0) {
        tbody.innerHTML = `<tr><td colspan="10" style="text-align:center; padding:24px; color:#94a3b8;">No se encontraron registros de ventas.</td></tr>`;
    } else {
        tbody.innerHTML = pageItems.map(v => {
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
                            <button class="action-icon-btn" title="Ver" onclick="verDetalleVenta('${v.nroReserva}', '${v.cliente}', '${v.tour}', '${Number(v.monto).toFixed(2)}')"><i class="ti ti-eye"></i></button>
                            <button class="action-icon-btn" title="Descargar Voucher" onclick="descargarVoucher('${v.nroReserva}', '${v.cliente}')"><i class="ti ti-download"></i></button>
                        </div>
                    </td>
                </tr>
            `;
        }).join('');
    }

    renderPaginator(filteredVentas.length, currentVentasPage, VENTAS_PER_PAGE, 'ventasPaginationControls', 'ventasPaginationInfo', renderVentasTable);
}

function onPeriodoChange() {
    const sel = document.getElementById('filtroPeriodo');
    if (sel && sel.value === 'personalizado') {
        abrirModalCalendario();
    } else {
        aplicarFiltrosDashboard();
    }
}

function aplicarFiltrosDashboard() {
    const q = document.getElementById('filtroBusqueda')?.value.toLowerCase().trim() || '';
    const destino = document.getElementById('filtroDestino')?.value || 'Todos';

    filteredVentas = allVentas.filter(v => {
        const matchesQ = !q || v.cliente.toLowerCase().includes(q) || v.tour.toLowerCase().includes(q) || v.nroReserva.toLowerCase().includes(q);
        const matchesDest = destino === 'Todos' || v.destino.toLowerCase().includes(destino.toLowerCase());
        return matchesQ && matchesDest;
    });

    renderVentasTable(1);
}

function limpiarFiltrosDashboard() {
    if (document.getElementById('filtroBusqueda')) document.getElementById('filtroBusqueda').value = '';
    if (document.getElementById('filtroDestino')) document.getElementById('filtroDestino').value = 'Todos';
    if (document.getElementById('filtroPeriodo')) document.getElementById('filtroPeriodo').value = '7dias';
    
    filteredVentas = [...allVentas];
    renderVentasTable(1);
}

// CALENDAR RANGE PICKER FUNCTIONS
const MONTH_NAMES = [
    'enero', 'febrero', 'marzo', 'abril', 'mayo', 'junio',
    'julio', 'agosto', 'septiembre', 'octubre', 'noviembre', 'diciembre'
];

function initCalendar() {
    renderCalendarGrid();
}

function abrirModalCalendario() {
    renderCalendarGrid();
    const modal = document.getElementById('modalCalendario');
    if (modal) modal.classList.add('show');
}

function cerrarModalCalendario() {
    const modal = document.getElementById('modalCalendario');
    if (modal) modal.classList.remove('show');
}

function cambiarMesCalendario(delta) {
    calCurrentMonth += delta;
    if (calCurrentMonth > 11) {
        calCurrentMonth = 0;
        calCurrentYear++;
    } else if (calCurrentMonth < 0) {
        calCurrentMonth = 11;
        calCurrentYear--;
    }
    renderCalendarGrid();
}

function renderCalendarGrid() {
    const titleEl = document.getElementById('calMonthTitle');
    if (titleEl) {
        titleEl.textContent = `${MONTH_NAMES[calCurrentMonth]} de ${calCurrentYear}`;
    }

    const gridEl = document.getElementById('calDaysGrid');
    if (!gridEl) return;

    gridEl.innerHTML = '';

    const firstDayIndex = new Date(calCurrentYear, calCurrentMonth, 1).getDay();
    const totalDays = new Date(calCurrentYear, calCurrentMonth + 1, 0).getDate();
    const prevMonthTotalDays = new Date(calCurrentYear, calCurrentMonth, 0).getDate();

    // Previous month filler days
    for (let i = firstDayIndex - 1; i >= 0; i--) {
        const cell = document.createElement('div');
        cell.className = 'calendar-date-cell other-month';
        cell.textContent = prevMonthTotalDays - i;
        gridEl.appendChild(cell);
    }

    const today = new Date(2026, 8, 26);

    for (let day = 1; day <= totalDays; day++) {
        const thisDate = new Date(calCurrentYear, calCurrentMonth, day);
        const cell = document.createElement('div');
        cell.className = 'calendar-date-cell';
        cell.textContent = day;

        const isStart = calStartDate && isSameDay(thisDate, calStartDate);
        const isEnd = calEndDate && isSameDay(thisDate, calEndDate);
        const inRange = calStartDate && calEndDate && thisDate > calStartDate && thisDate < calEndDate;
        const isToday = isSameDay(thisDate, today);

        if (isStart) cell.classList.add('range-start');
        if (isEnd) cell.classList.add('range-end');
        if (inRange) cell.classList.add('in-range');
        if (isToday && !isStart && !isEnd) cell.classList.add('today-circle');

        cell.onclick = () => onDateClicked(thisDate);
        gridEl.appendChild(cell);
    }

    // Next month filler days to complete grid
    const remainingSlots = (7 - ((firstDayIndex + totalDays) % 7)) % 7;
    for (let i = 1; i <= remainingSlots; i++) {
        const cell = document.createElement('div');
        cell.className = 'calendar-date-cell other-month';
        cell.textContent = i;
        gridEl.appendChild(cell);
    }
}

function isSameDay(d1, d2) {
    return d1.getFullYear() === d2.getFullYear() &&
           d1.getMonth() === d2.getMonth() &&
           d1.getDate() === d2.getDate();
}

function onDateClicked(date) {
    if (calSelectingStart) {
        calStartDate = date;
        calEndDate = null;
        calSelectingStart = false;
    } else {
        if (date < calStartDate) {
            calEndDate = calStartDate;
            calStartDate = date;
        } else {
            calEndDate = date;
        }
        calSelectingStart = true;
    }
    renderCalendarGrid();
}

function aplicarRangoCalendario() {
    cerrarModalCalendario();
    const d1 = calStartDate ? `${calStartDate.getDate()} ${MONTH_NAMES[calStartDate.getMonth()].slice(0,3)}` : '';
    const d2 = calEndDate ? `${calEndDate.getDate()} ${MONTH_NAMES[calEndDate.getMonth()].slice(0,3)}` : '';
    
    Swal.fire({
        toast: true,
        position: 'top-end',
        icon: 'success',
        title: `Rango aplicado: ${d1} - ${d2} ${calCurrentYear}`,
        showConfirmButton: false,
        timer: 2000
    });
    aplicarFiltrosDashboard();
}

function verDetalleVenta(nro, cliente, tour, monto) {
    Swal.fire({
        title: `Venta ${nro}`,
        html: `
            <div style="text-align:left; font-size:13px; line-height:1.8;">
                <p><strong>Cliente:</strong> ${cliente}</p>
                <p><strong>Tour Adquirido:</strong> ${tour}</p>
                <p><strong>Monto Pagado:</strong> S/ ${monto}</p>
                <p><strong>Comisión Travelink (20%):</strong> S/ ${(monto * 0.2).toFixed(2)}</p>
                <p><strong>Fecha de Reserva:</strong> 26 de Septiembre de 2026</p>
            </div>
        `,
        icon: 'info',
        confirmButtonColor: '#1a73e8'
    });
}

function descargarVoucher(nro, cliente) {
    Swal.fire('Voucher Generado', `Descargando comprobante oficial para ${cliente} (${nro}).`, 'success');
}

function exportarDashboardPDF() {
    Swal.fire({
        title: 'Generando Reporte PDF',
        text: 'Compilando gráficos y métricas del Dashboard...',
        timer: 1600,
        showConfirmButton: false,
        icon: 'success'
    });
}
