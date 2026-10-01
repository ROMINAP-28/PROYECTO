/**
 * Travelink - Catálogo de Destinos
 * Filtros compactos, Paginación dinámica (máx 5 registros) y Modal de Tours por Destino
 */

let destinosList = [];
let destinosFiltrados = [];
let paginaActual = 1;
const ITEMS_POR_PAGINA = 5;

document.addEventListener('DOMContentLoaded', () => {
    cargarDestinos();
});

async function cargarDestinos() {
    try {
        const res = await fetch(`${API_BASE}/destinos`);
        if (res.ok) {
            const data = await res.json();
            const payload = data.data || data;
            destinosList = Array.isArray(payload) ? payload : (payload.destinos || []);
        } else {
            throw new Error('Error al conectar a API');
        }
    } catch (e) {
        console.error('Error al obtener destinos de la base de datos:', e);
        destinosList = [];
    }
    aplicarFiltros();
}

function aplicarFiltros() {
    const busqueda = (document.getElementById('filtroBusqueda')?.value || '').toLowerCase().trim();
    const estado = document.getElementById('filtroEstado')?.value || 'todos';

    destinosFiltrados = destinosList.filter(d => {
        const coincideBusqueda = !busqueda || (d.nombre && d.nombre.toLowerCase().includes(busqueda));
        const coincideEstado = (estado === 'todos') || (d.estado && d.estado.toLowerCase() === estado.toLowerCase());
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

function cambiarPaginaDestinos(p) {
    const totalPages = Math.ceil(destinosFiltrados.length / ITEMS_POR_PAGINA) || 1;
    if (p < 1 || p > totalPages) return;
    paginaActual = p;
    renderizarTabla();
}

function renderizarTabla() {
    const tbody = document.getElementById('tablaDestinosBody');
    if (!tbody) return;

    if (destinosFiltrados.length === 0) {
        tbody.innerHTML = `<tr><td colspan="5" style="text-align:center; padding: 24px; color:#94a3b8;">No se encontraron destinos.</td></tr>`;
        renderPaginator(0, 1, ITEMS_POR_PAGINA, 'paginacionControles', 'paginacionInfo', cambiarPaginaDestinos);
        return;
    }

    const inicio = (paginaActual - 1) * ITEMS_POR_PAGINA;
    const fin = inicio + ITEMS_POR_PAGINA;
    const items = destinosFiltrados.slice(inicio, fin);

    tbody.innerHTML = items.map((d, idx) => {
        const estadoClase = (d.estado || '').toLowerCase();
        return `
            <tr>
                <td><span class="table-id-code">${inicio + idx + 1}</span></td>
                <td>
                    <div class="destino-cell-meta">
                        <img src="${d.imagen || '../../img/cusco.jpg'}" class="destino-thumb-img" alt="${d.nombre}">
                        <strong>${d.nombre}</strong>
                    </div>
                </td>
                <td><span class="type-badge admin">${d.toursActivos || 0} tours activos</span></td>
                <td><span class="badge-status ${estadoClase}">${d.estado || 'Activo'}</span></td>
                <td>
                    <div class="table-actions">
                        <button class="btn-table-pill outline-blue" onclick="verToursEnDestino(${d.id}, '${d.nombre}')">
                            <i class="ti ti-eye"></i> Ver detalles
                        </button>
                    </div>
                </td>
            </tr>
        `;
    }).join('');

    renderPaginator(destinosFiltrados.length, paginaActual, ITEMS_POR_PAGINA, 'paginacionControles', 'paginacionInfo', cambiarPaginaDestinos);
}

async function verToursEnDestino(id, nombreDestino) {
    document.getElementById('modalDestinoNombre').textContent = nombreDestino;
    const tbody = document.getElementById('modalToursDestinoBody');
    tbody.innerHTML = `<tr><td colspan="6" style="text-align:center; padding: 20px;">Cargando tours...</td></tr>`;
    document.getElementById('modalToursDestino').classList.add('show');

    try {
        const res = await fetch(`${API_BASE}/destinos/tours?idDestino=${id}&destino=${encodeURIComponent(nombreDestino)}`);
        let tours = [];
        if (res.ok) {
            const json = await res.json();
            tours = json.data || json.tours || (Array.isArray(json) ? json : []);
        } else {
            throw new Error('Error al conectar con la API de tours por destino');
        }
        renderizarToursEnModal(tours, nombreDestino);
    } catch (e) {
        console.error('Error al cargar tours de la base de datos:', e);
        renderizarToursEnModal([], nombreDestino);
    }
}

function renderizarToursEnModal(tours, nombreDestino) {
    const tbody = document.getElementById('modalToursDestinoBody');
    if (!tours || tours.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6" style="text-align:center; padding: 20px; color:#94a3b8;">No hay tours registrados para ${nombreDestino}.</td></tr>`;
        return;
    }

    tbody.innerHTML = tours.map(t => {
        const estadoClase = (t.estado || 'activo').toLowerCase();
        return `
            <tr>
                <td><strong>${t.nombre}</strong></td>
                <td>${t.agencia || 'Agencia'}</td>
                <td>${t.duracion || '1 día'}</td>
                <td><strong>S/ ${(parseFloat(t.precio) || 0).toFixed(2)}</strong></td>
                <td><span class="star-rating-badge"><i class="ti ti-star-filled"></i> ${t.calificacion || '4.5'}</span></td>
                <td><span class="badge-status ${estadoClase}">${t.estado || 'Activo'}</span></td>
            </tr>
        `;
    }).join('');
}

function exportarDestinos() {
    let csv = 'ID,Destino,ToursActivos,Estado\n';
    destinosFiltrados.forEach(d => {
        csv += `"${d.id}","${d.nombre}","${d.toursActivos}","${d.estado}"\n`;
    });
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.setAttribute('download', `destinos_${new Date().toISOString().slice(0,10)}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
}
