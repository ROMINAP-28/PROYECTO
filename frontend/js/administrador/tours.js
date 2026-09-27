/**
 * Travelink - Catálogo General de Tours (Vista Administrador)
 * Quitado botón "+ Nuevo Tour" y edición de precio.
 * Filtros por 24 Departamentos y Estado con única acción Activar/Desactivar.
 * Paginación dinámica (máx 5 registros).
 */

let toursList = [];
let toursFiltrados = [];
let paginaActual = 1;
const ITEMS_POR_PAGINA = 5;

document.addEventListener('DOMContentLoaded', () => {
    cargarTours();
});

async function cargarTours() {
    try {
        const res = await fetch(`${API_BASE}/tours`);
        if (res.ok) {
            const data = await res.json();
            toursList = Array.isArray(data) ? data : (data.data || data.tours || []);
        } else {
            throw new Error('Error al obtener tours de API');
        }
    } catch (e) {
        console.warn('Cargando fallback para tours...', e);
        toursList = [
            { id: 1, nombre: 'Machu Picchu Clásico Full Day', agencia: 'Andes Tours', destino: 'Cusco', duracion: '1 día', precio: 350.00, calificacion: 4.8, estado: 'Activo', imagen: 'https://images.unsplash.com/photo-1526392060635-9d6019884377?w=150' },
            { id: 2, nombre: 'Montaña de 7 Colores (Vinicunca)', agencia: 'Inka Travel', destino: 'Cusco', duracion: '1 día', precio: 260.00, calificacion: 4.6, estado: 'Activo', imagen: 'https://images.unsplash.com/photo-1589802829985-817e51171b92?w=150' },
            { id: 3, nombre: 'Valle Sagrado de los Incas & Ollantaytambo', agencia: 'Selva Viva', destino: 'Cusco', duracion: '1 día', precio: 320.00, calificacion: 4.5, estado: 'Activo', imagen: 'https://images.unsplash.com/photo-1509299349698-dd22323b5963?w=150' },
            { id: 4, nombre: 'Tour Islas Ballestas & Huacachina', agencia: 'Ica Travel', destino: 'Ica', duracion: '2 días', precio: 290.00, calificacion: 4.2, estado: 'En revisión', imagen: 'https://images.unsplash.com/photo-1509316975850-ff9c5deb0cd9?w=150' },
            { id: 5, nombre: 'Cañón del Colca 2D/1N', agencia: 'Arequipa Tours', destino: 'Arequipa', duracion: '2 días', precio: 380.00, calificacion: 4.7, estado: 'Activo', imagen: 'https://images.unsplash.com/photo-1469854523086-cc02fe5d8800?w=150' },
            { id: 6, nombre: 'Ruta del Café & Selva Central', agencia: 'Chanchamayo Expeditions', destino: 'Junín', duracion: '3 días', precio: 450.00, calificacion: 4.3, estado: 'Activo', imagen: 'https://images.unsplash.com/photo-1518684079-3c830dcef090?w=150' },
            { id: 7, nombre: 'Lago Titicaca & Islas Uros', agencia: 'Machupicchu Tours', destino: 'Puno', duracion: '1 día', precio: 220.00, calificacion: 3.9, estado: 'Desactivado', imagen: 'https://images.unsplash.com/photo-1544620347-c4fd4a3d5957?w=150' },
            { id: 8, nombre: 'Amazonía & Reserva Nacional Tambopata', agencia: 'Aventura Perú', destino: 'Madre de Dios', duracion: '3 días', precio: 680.00, calificacion: 4.9, estado: 'Activo', imagen: 'https://images.unsplash.com/photo-1516426122078-c23e76319801?w=150' }
        ];
    }
    aplicarFiltros();
}

function aplicarFiltros() {
    const busqueda = (document.getElementById('filtroBusqueda')?.value || '').toLowerCase().trim();
    const destino = document.getElementById('filtroDestino')?.value || 'todos';
    const estado = document.getElementById('filtroEstado')?.value || 'todos';

    toursFiltrados = toursList.filter(t => {
        const coincideBusqueda = !busqueda ||
            (t.nombre && t.nombre.toLowerCase().includes(busqueda)) ||
            (t.agencia && t.agencia.toLowerCase().includes(busqueda)) ||
            (t.destino && t.destino.toLowerCase().includes(busqueda));

        const coincideDestino = (destino === 'todos') || 
            (t.destino && t.destino.toLowerCase() === destino.toLowerCase());

        const coincideEstado = (estado === 'todos') || 
            (t.estado && t.estado.toLowerCase() === estado.toLowerCase());

        return coincideBusqueda && coincideDestino && coincideEstado;
    });

    paginaActual = 1;
    renderizarTabla();
}

function limpiarFiltros() {
    if (document.getElementById('filtroBusqueda')) document.getElementById('filtroBusqueda').value = '';
    if (document.getElementById('filtroDestino')) document.getElementById('filtroDestino').value = 'todos';
    if (document.getElementById('filtroEstado')) document.getElementById('filtroEstado').value = 'todos';
    aplicarFiltros();
}

function cambiarPaginaTours(p) {
    const totalPages = Math.ceil(toursFiltrados.length / ITEMS_POR_PAGINA) || 1;
    if (p < 1 || p > totalPages) return;
    paginaActual = p;
    renderizarTabla();
}

function renderizarTabla() {
    const tbody = document.getElementById('tablaToursBody');
    if (!tbody) return;

    if (toursFiltrados.length === 0) {
        tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; padding: 24px; color:#94a3b8;">No se encontraron tours en el catálogo con los filtros aplicados.</td></tr>`;
        renderPaginator(0, 1, ITEMS_POR_PAGINA, 'paginacionControles', 'paginacionInfo', cambiarPaginaTours);
        return;
    }

    const inicio = (paginaActual - 1) * ITEMS_POR_PAGINA;
    const fin = inicio + ITEMS_POR_PAGINA;
    const items = toursFiltrados.slice(inicio, fin);

    tbody.innerHTML = items.map(t => {
        const estadoClase = (t.estado || '').toLowerCase().replace(' ', '');
        const esActivo = (t.estado || '').toLowerCase() === 'activo';
        const fallbackImg = 'https://images.unsplash.com/photo-1526392060635-9d6019884377?w=150';

        return `
            <tr>
                <td><span class="table-id-code">#TR-${String(t.id).padStart(3, '0')}</span></td>
                <td>
                    <div class="tour-cell-meta">
                        <img src="${t.imagen || fallbackImg}" alt="${t.nombre}" class="tour-thumb-img" onerror="this.src='${fallbackImg}'">
                        <div class="tour-text-wrap">
                            <h6>${t.nombre}</h6>
                            <span>${t.duracion || '1 día'}</span>
                        </div>
                    </div>
                </td>
                <td><strong>${t.agencia || 'Agencia Oficial'}</strong></td>
                <td><i class="ti ti-map-pin" style="color:#ef4444; font-size:13px;"></i> ${t.destino || 'Perú'}</td>
                <td><strong>S/ ${(parseFloat(t.precio) || 0).toFixed(2)}</strong></td>
                <td>
                    <span class="star-rating-badge">
                        <i class="ti ti-star-filled"></i> ${t.calificacion || '4.5'}
                    </span>
                </td>
                <td><span class="badge-status ${estadoClase}">${t.estado}</span></td>
                <td>
                    <div class="table-actions">
                        <button class="btn-table-pill ${esActivo ? 'outline-red' : 'outline-green'}" onclick="toggleEstadoTour(${t.id})">
                            <i class="ti ti-${esActivo ? 'ban' : 'check'}"></i> ${esActivo ? 'Desactivar' : 'Activar'}
                        </button>
                    </div>
                </td>
            </tr>
        `;
    }).join('');

    renderPaginator(toursFiltrados.length, paginaActual, ITEMS_POR_PAGINA, 'paginacionControles', 'paginacionInfo', cambiarPaginaTours);
}

function toggleEstadoTour(id) {
    const t = toursList.find(item => item.id === id);
    if (!t) return;
    const nuevoEstado = (t.estado.toLowerCase() === 'activo') ? 'Desactivado' : 'Activo';

    Swal.fire({
        title: `¿${nuevoEstado === 'Activo' ? 'Activar' : 'Desactivar'} tour?`,
        text: `El tour "${t.nombre}" pasará a estar ${nuevoEstado}.`,
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: nuevoEstado === 'Activo' ? '#16a34a' : '#ef4444',
        cancelButtonColor: '#94a3b8',
        confirmButtonText: `Sí, ${nuevoEstado.toLowerCase()}`,
        cancelButtonText: 'Cancelar'
    }).then(async (result) => {
        if (result.isConfirmed) {
            t.estado = nuevoEstado;
            try {
                await fetch(`${API_BASE}/tours`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(t)
                });
            } catch (e) {}
            aplicarFiltros();
            Swal.fire('Actualizado', `El tour ha sido ${nuevoEstado.toLowerCase()} con éxito.`, 'success');
        }
    });
}

function exportarTours() {
    let csv = 'ID,Nombre,Agencia,Destino,Duracion,Precio,Calificacion,Estado\n';
    toursFiltrados.forEach(t => {
        csv += `"${t.id}","${t.nombre}","${t.agencia}","${t.destino}","${t.duracion}","${t.precio}","${t.calificacion}","${t.estado}"\n`;
    });
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.setAttribute('download', `catalogo_tours_${new Date().toISOString().slice(0,10)}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
}
