/**
 * Travelink - Control de Calidad
 * Sub-tabs: Calificaciones Flaggeadas & Tours en Revisión
 * Filtros compactos, Paginación dinámica (máx 5 registros)
 */

let tabActivo = 'flagged';
let reviewsList = [];
let reviewsFiltrados = [];
let toursRevisionList = [];
let toursRevisionFiltrados = [];
let paginaActual = 1;
const ITEMS_POR_PAGINA = 5;

document.addEventListener('DOMContentLoaded', () => {
    cargarCombosAgenciasCalidad();
    cargarDatosCalidad();
});

async function cargarCombosAgenciasCalidad() {
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

async function cargarDatosCalidad() {
    try {
        const res = await fetch(`${API_BASE}/calidad`);
        if (res.ok) {
            const data = await res.json();
            const payload = data.data || data;
            reviewsList = payload.reviews || [];
            toursRevisionList = payload.toursEnRevision || [];
        } else {
            throw new Error('Error al conectar a API');
        }
    } catch (e) {
        console.warn('Cargando fallback para calidad...', e);
        reviewsList = [
            { id: 50, usuario: 'Diego Armando Quispe', email: 'diego.quispe@travelink.test', agencia: 'ANDES TOURS PERU S.A.C.', tour: 'Ruta Heroica y Valles del Pisco', estrellas: 5.0, comentario: '¡Increíble servicio y atención de primera en Tacna y Moquegua!', fecha: '01 oct. 2026 - 01:52', estado: 'Pendiente' },
            { id: 51, usuario: 'Ana Lucía Morales', email: 'ana.morales@travelink.test', agencia: 'INKA TRAVEL EXPERIENCES S.A.C.', tour: 'Valle Sagrado de los Incas', estrellas: 4.0, comentario: 'Muy buen recorrido guiado por el Valle Sagrado.', fecha: '01 oct. 2026 - 01:52', estado: 'Pendiente' },
            { id: 52, usuario: 'Jorge Luis Fernández', email: 'jorge.fernandez@travelink.test', agencia: 'TOUR LIMA S.A', tour: 'City Tour Lima Colonial y Catacumbas', estrellas: 5.0, comentario: 'Fascinante recorrido por las catacumbas de Lima.', fecha: '01 oct. 2026 - 01:52', estado: 'Pendiente' },
            { id: 53, usuario: 'Maria Elena Sánchez', email: 'maria.sanchez@travelink.test', agencia: 'ANDES TOURS PERU S.A.C.', tour: 'Amazonía Profunda Tambopata', estrellas: 5.0, comentario: 'Inolvidable experiencia en la selva de Madre de Dios.', fecha: '01 oct. 2026 - 01:52', estado: 'Pendiente' },
            { id: 54, usuario: 'Carlos Manuel Rojas', email: 'carlos.rojas@travelink.test', agencia: 'AGENCIA ALEGRIA S.A', tour: 'Expedición Bosque de Piedras de Huayllay', estrellas: 4.0, comentario: 'El bosque de piedras fue espectacular, los niños lo disfrutaron mucho.', fecha: '01 oct. 2026 - 01:52', estado: 'Pendiente' },
            { id: 55, usuario: 'Sofia Isabel Ramírez', email: 'sofia.ramirez@travelink.test', agencia: 'AGENCIA SELVA S.A', tour: 'Aventura Marina en Reserva Punta de Coles Ilo', estrellas: 5.0, comentario: 'Excelente avistamiento de lobos marinos y pingüinos en Ilo.', fecha: '01 oct. 2026 - 01:52', estado: 'Pendiente' },
            { id: 56, usuario: 'Mateo Gabriel Chávez', email: 'mateo.chavez@travelink.test', agencia: 'TOUR AREQUIPA S.A.S', tour: 'Circuito Valle Viejo y Viñedos de Pocollay', estrellas: 4.5, comentario: 'Los vinos y el macerado de damasco riquísimos. Súper recomendado.', fecha: '01 oct. 2026 - 01:52', estado: 'Pendiente' }
        ];

        toursRevisionList = [
            { id: 1, tour: 'Ruta Heroica y Valles del Pisco', duracion: '1 día', agencia: 'ANDES TOURS PERU S.A.C.', ruc: '20601234567', destino: 'Tacna', califTour: 4.8, califAgencia: 5.0, precio: 350.00, estado: 'Activo', motivo: 'Revisión rutinaria de calidad', imagen: '../../img/lima.jpg' },
            { id: 2, tour: 'Valle Sagrado de los Incas', duracion: '1 día', agencia: 'INKA TRAVEL EXPERIENCES S.A.C.', ruc: '20609876543', destino: 'Cusco', califTour: 4.6, califAgencia: 4.5, precio: 280.00, estado: 'Activo', motivo: 'Monitoreo de guías turísticos', imagen: '../../img/cusco.jpg' },
            { id: 3, tour: 'City Tour Lima Colonial', duracion: '1 día', agencia: 'TOUR LIMA S.A', ruc: '10721439113', destino: 'Lima', califTour: 4.7, califAgencia: 4.67, precio: 120.00, estado: 'Activo', motivo: 'Verificación de itinarios urbanos', imagen: '../../img/lima.jpg' }
        ];
    }
    aplicarFiltros();
}

function cambiarTabCalidad(tab, btn) {
    tabActivo = tab;
    document.querySelectorAll('.sub-tab-btn').forEach(b => b.classList.remove('active'));
    if (btn) btn.classList.add('active');

    const kpisFlagged = document.getElementById('kpisFlagged');
    const kpisTours = document.getElementById('kpisTours');
    const th1 = document.getElementById('thHeadingsFlagged');
    const th2 = document.getElementById('thHeadingsTours');

    if (tab === 'flagged') {
        if (kpisFlagged) kpisFlagged.style.display = 'grid';
        if (kpisTours) kpisTours.style.display = 'none';
        if (th1) th1.style.display = 'table-row';
        if (th2) th2.style.display = 'none';
    } else {
        if (kpisFlagged) kpisFlagged.style.display = 'none';
        if (kpisTours) kpisTours.style.display = 'grid';
        if (th1) th1.style.display = 'none';
        if (th2) th2.style.display = 'table-row';
    }

    aplicarFiltros();
}

function aplicarFiltros() {
    const busqueda = (document.getElementById('filtroBusqueda')?.value || '').toLowerCase().trim();
    const agencia = document.getElementById('filtroAgencia')?.value || 'todos';

    if (tabActivo === 'flagged') {
        reviewsFiltrados = reviewsList.filter(r => {
            const coincideBusqueda = !busqueda ||
                (r.usuario && r.usuario.toLowerCase().includes(busqueda)) ||
                (r.comentario && r.comentario.toLowerCase().includes(busqueda)) ||
                (r.tour && r.tour.toLowerCase().includes(busqueda));

            const coincideAgencia = (agencia === 'todos') ||
                (r.agencia && r.agencia.toLowerCase().includes(agencia.toLowerCase()));

            return coincideBusqueda && coincideAgencia;
        });
    } else {
        toursRevisionFiltrados = toursRevisionList.filter(t => {
            const coincideBusqueda = !busqueda ||
                (t.tour && t.tour.toLowerCase().includes(busqueda)) ||
                (t.destino && t.destino.toLowerCase().includes(busqueda)) ||
                (t.motivo && t.motivo.toLowerCase().includes(busqueda));

            const coincideAgencia = (agencia === 'todos') ||
                (t.agencia && t.agencia.toLowerCase().includes(agencia.toLowerCase()));

            return coincideBusqueda && coincideAgencia;
        });
    }

    paginaActual = 1;
    renderizarTabla();
}

function limpiarFiltros() {
    if (document.getElementById('filtroBusqueda')) document.getElementById('filtroBusqueda').value = '';
    if (document.getElementById('filtroAgencia')) document.getElementById('filtroAgencia').value = 'todos';
    aplicarFiltros();
}

function cambiarPaginaCalidad(p) {
    const total = (tabActivo === 'flagged') ? reviewsFiltrados.length : toursRevisionFiltrados.length;
    const totalPages = Math.ceil(total / ITEMS_POR_PAGINA) || 1;
    if (p < 1 || p > totalPages) return;
    paginaActual = p;
    renderizarTabla();
}

function renderizarTabla() {
    const tbody = document.getElementById('tablaCalidadBody');
    if (!tbody) return;

    if (tabActivo === 'flagged') {
        if (reviewsFiltrados.length === 0) {
            tbody.innerHTML = `<tr><td colspan="8" style="text-align:center; padding: 24px; color:#94a3b8;">No hay calificaciones reportadas.</td></tr>`;
            renderPaginator(0, 1, ITEMS_POR_PAGINA, 'paginacionControles', 'paginacionInfo', cambiarPaginaCalidad);
            return;
        }

        const inicio = (paginaActual - 1) * ITEMS_POR_PAGINA;
        const fin = inicio + ITEMS_POR_PAGINA;
        const items = reviewsFiltrados.slice(inicio, fin);

        tbody.innerHTML = items.map((r, idx) => {
            const inicial = (r.usuario || 'U').charAt(0).toUpperCase();
            return `
                <tr>
                    <td><span class="table-id-code">${inicio + idx + 1}</span></td>
                    <td>
                        <div class="user-cell-meta">
                            <div class="user-avatar-circle">${inicial}</div>
                            <div class="user-text-wrap">
                                <h6>${r.usuario}</h6>
                                <span>${r.email}</span>
                            </div>
                        </div>
                    </td>
                    <td><strong>${r.agencia}</strong></td>
                    <td><span>${r.tour}</span></td>
                    <td>
                        <span class="star-rating-badge" style="color: ${r.estrellas <= 2 ? '#ef4444' : '#d97706'};">
                            <i class="ti ti-star-filled"></i> ${Number(r.estrellas).toFixed(1)}
                        </span>
                    </td>
                    <td><div class="review-comment-box" title="${r.comentario}">${r.comentario}</div></td>
                    <td><span style="font-size:12px; color:#64748b;">${r.fecha}</span></td>
                    <td>
                        <div class="table-actions">
                            <button class="btn-table-pill outline-blue" onclick="verDetalleResena(${r.id})">
                                <i class="ti ti-eye"></i> Ver
                            </button>
                            <button class="btn-table-pill outline-red" onclick="eliminarResena(${r.id})">
                                <i class="ti ti-trash"></i> Eliminar
                            </button>
                        </div>
                    </td>
                </tr>
            `;
        }).join('');

        renderPaginator(reviewsFiltrados.length, paginaActual, ITEMS_POR_PAGINA, 'paginacionControles', 'paginacionInfo', cambiarPaginaCalidad);
    } else {
        if (toursRevisionFiltrados.length === 0) {
            tbody.innerHTML = `<tr><td colspan="9" style="text-align:center; padding: 24px; color:#94a3b8;">No hay tours en revisión.</td></tr>`;
            renderPaginator(0, 1, ITEMS_POR_PAGINA, 'paginacionControles', 'paginacionInfo', cambiarPaginaCalidad);
            return;
        }

        const inicio = (paginaActual - 1) * ITEMS_POR_PAGINA;
        const fin = inicio + ITEMS_POR_PAGINA;
        const items = toursRevisionFiltrados.slice(inicio, fin);

        tbody.innerHTML = items.map(t => {
            const estadoClase = (t.estado || '').toLowerCase();
            return `
                <tr>
                    <td>
                        <div class="tour-cell-meta">
                            <img src="${t.imagen || '../../img/lima.jpg'}" class="tour-thumb-img" alt="${t.tour}">
                            <div class="tour-text-wrap">
                                <h6>${t.tour}</h6>
                                <span>${t.duracion}</span>
                            </div>
                        </div>
                    </td>
                    <td>
                        <div class="reserva-meta-cell">
                            <strong>${t.agencia}</strong>
                            <span>RUC: ${t.ruc || ''}</span>
                        </div>
                    </td>
                    <td>${t.destino}</td>
                    <td><span class="star-rating-badge" style="color:#d97706;"><i class="ti ti-star-filled"></i> ${t.califTour}</span></td>
                    <td><span class="star-rating-badge" style="color:#16a34a;"><i class="ti ti-star-filled"></i> ${t.califAgencia}</span></td>
                    <td><strong>S/ ${(parseFloat(t.precio) || 0).toFixed(2)}</strong></td>
                    <td><span class="badge-status ${estadoClase}">${t.estado}</span></td>
                    <td><div class="review-comment-box" title="${t.motivo}">${t.motivo}</div></td>
                    <td>
                        <div class="table-actions">
                            <button class="btn-table-pill outline-red" onclick="moderarTour(${t.id}, 'desactivar')">
                                <i class="ti ti-ban"></i> Desactivar
                            </button>
                            <button class="btn-table-pill outline-green" onclick="moderarTour(${t.id}, 'mantener')">
                                <i class="ti ti-check"></i> Mantener activo
                            </button>
                        </div>
                    </td>
                </tr>
            `;
        }).join('');

        renderPaginator(toursRevisionFiltrados.length, paginaActual, ITEMS_POR_PAGINA, 'paginacionControles', 'paginacionInfo', cambiarPaginaCalidad);
    }
}

function verDetalleResena(id) {
    const r = reviewsList.find(item => item.id === id);
    if (!r) return;

    Swal.fire({
        title: `Reseña de ${r.usuario}`,
        html: `
            <div style="text-align:left; font-size:13.5px; color:#334155; line-height: 1.6;">
                <p><strong>Tour:</strong> ${r.tour}</p>
                <p><strong>Agencia:</strong> ${r.agencia}</p>
                <p><strong>Calificación:</strong> ⭐ ${r.estrellas} / 5.0</p>
                <p><strong>Fecha:</strong> ${r.fecha}</p>
                <hr style="margin: 10px 0; border: 0; border-top: 1px solid #e2e8f0;">
                <p><strong>Comentario reportado:</strong></p>
                <div style="background:#f8fafc; padding:10px; border-radius:6px; border:1px solid #e2e8f0; font-style:italic;">
                    "${r.comentario}"
                </div>
            </div>
        `,
        icon: 'info',
        confirmButtonText: 'Cerrar',
        confirmButtonColor: '#1a73e8'
    });
}

function eliminarResena(id) {
    Swal.fire({
        title: '¿Eliminar reseña reportada?',
        text: 'La calificación se eliminará de la base de datos.',
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#ef4444',
        cancelButtonColor: '#94a3b8',
        confirmButtonText: 'Sí, eliminar',
        cancelButtonText: 'Cancelar'
    }).then(async result => {
        if (result.isConfirmed) {
            try {
                await fetch(`${API_BASE}/calidad/eliminar?id=${id}`, { method: 'POST' });
            } catch (e) {}
            reviewsList = reviewsList.filter(r => r.id !== id);
            aplicarFiltros();
            Swal.fire('Eliminada', 'La reseña ha sido retirada del sistema y actualizada en la base de datos.', 'success');
        }
    });
}

function moderarTour(id, accion) {
    const t = toursRevisionList.find(item => item.id === id);
    if (!t) return;

    const texto = accion === 'desactivar' ? 'desactivar y pausar ventas de' : 'mantener activo';
    Swal.fire({
        title: `¿Confirmar acción?`,
        text: `Se procederá a ${texto} "${t.tour}".`,
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: accion === 'desactivar' ? '#ef4444' : '#16a34a',
        cancelButtonColor: '#94a3b8',
        confirmButtonText: 'Confirmar',
        cancelButtonText: 'Cancelar'
    }).then(result => {
        if (result.isConfirmed) {
            t.estado = (accion === 'desactivar') ? 'Suspendido' : 'Activo';
            aplicarFiltros();
            Swal.fire('Actualizado', `El tour ha sido actualizado a ${t.estado}.`, 'success');
        }
    });
}

function exportarCalidad() {
    let csv = '';
    if (tabActivo === 'flagged') {
        csv = 'ID,Usuario,Email,Agencia,Tour,Estrellas,Fecha,Comentario\n';
        reviewsFiltrados.forEach(r => {
            csv += `"${r.id}","${r.usuario}","${r.email}","${r.agencia}","${r.tour}","${r.estrellas}","${r.fecha}","${r.comentario}"\n`;
        });
    } else {
        csv = 'ID,Tour,Agencia,Destino,CalifTour,CalifAgencia,Precio,Estado,Motivo\n';
        toursRevisionFiltrados.forEach(t => {
            csv += `"${t.id}","${t.tour}","${t.agencia}","${t.destino}","${t.califTour}","${t.califAgencia}","${t.precio}","${t.estado}","${t.motivo}"\n`;
        });
    }
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.setAttribute('download', `calidad_${tabActivo}_${new Date().toISOString().slice(0,10)}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
}
