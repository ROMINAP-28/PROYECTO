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
    cargarDatosCalidad();
});

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
            { id: 1, usuario: 'María López', email: 'maria@gmail.com', agencia: 'Andes Tours', tour: 'Machu Picchu Clásico', estrellas: 1.0, comentario: 'La experiencia fue muy mala, el guía no estaba preparado y nos dejó esperando.', fecha: '28 abr. 2025 - 14:32', estado: 'Pendiente' },
            { id: 2, usuario: 'Carlos Pérez', email: 'carlos@gmail.com', agencia: 'Inka Travel', tour: 'Montaña de 7 Colores', estrellas: 2.0, comentario: 'El transporte llegó tarde y el tour no estuvo bien organizado.', fecha: '27 abr. 2025 - 11:20', estado: 'Pendiente' },
            { id: 3, usuario: 'Ana Torres', email: 'ana@gmail.com', agencia: 'Selva Viva', tour: 'Lago Titicaca', estrellas: 2.0, comentario: 'El lugar es bonito pero la comida fue muy básica y el transporte incómodo.', fecha: '26 abr. 2025 - 16:45', estado: 'Pendiente' },
            { id: 4, usuario: 'Jorge Ramírez', email: 'jorge@gmail.com', agencia: 'Aventura Perú', tour: 'Valle Sagrado', estrellas: 3.0, comentario: 'El guía fue amable, pero hubo retrasos molestos en el transporte.', fecha: '25 abr. 2025 - 09:12', estado: 'Pendiente' },
            { id: 5, usuario: 'Lucía Fernández', email: 'lucia@gmail.com', agencia: 'Machupicchu Tours', tour: 'Cusco Arqueológico', estrellas: 3.0, comentario: 'La organización fue regular, faltó más información del guía local.', fecha: '24 abr. 2025 - 13:27', estado: 'Pendiente' },
            { id: 6, usuario: 'Diego Huamán', email: 'diego@gmail.com', agencia: 'Ica Travel', tour: 'Huacachina Sandboard', estrellas: 1.0, comentario: 'Carros en mal estado, no me sentí seguro.', fecha: '23 abr. 2025 - 10:15', estado: 'Eliminado' }
        ];

        toursRevisionList = [
            { id: 1, tour: 'Machu Picchu Clásico', duracion: '1 día', agencia: 'Andes Tours', ruc: '20456789012', destino: 'Cusco', califTour: 2.8, califAgencia: 3.2, precio: 350.00, estado: 'Revisión', motivo: 'Comentarios negativos sobre el guía y puntualidad.', imagen: 'https://images.unsplash.com/photo-1526392060635-9d6019884377?w=150' },
            { id: 2, tour: 'Laguna Humantay', duracion: '1 día', agencia: 'Montaña 7 Colores', ruc: '20678901234', destino: 'Cusco', califTour: 3.5, califAgencia: 4.1, precio: 280.00, estado: 'Activo', motivo: 'Sin observaciones graves.', imagen: 'https://images.unsplash.com/photo-1589802829985-817e51171b92?w=150' },
            { id: 3, tour: 'City Tour Cusco', duracion: '1 día', agencia: 'Selva Viva', ruc: '20123456789', destino: 'Cusco', califTour: 2.1, califAgencia: 2.8, precio: 120.00, estado: 'Revisión', motivo: 'Poca información en la descripción e itinerario.', imagen: 'https://images.unsplash.com/photo-1509299349698-dd22323b5963?w=150' },
            { id: 4, tour: 'Montaña de 7 Colores', duracion: '1 día', agencia: 'Inka Travel', ruc: '20198765432', destino: 'Cusco', califTour: 4.2, califAgencia: 4.5, precio: 280.00, estado: 'Activo', motivo: 'Sin observaciones.', imagen: 'https://images.unsplash.com/photo-1509316975850-ff9c5deb0cd9?w=150' },
            { id: 5, tour: 'Amazonía Peruana', duracion: '3 días', agencia: 'Aventura Perú', ruc: '20654321098', destino: 'Puerto Maldonado', califTour: 3.0, califAgencia: 3.6, precio: 450.00, estado: 'Revisión', motivo: 'Comentarios sobre la logística del alojamiento.', imagen: 'https://images.unsplash.com/photo-1516426122078-c23e76319801?w=150' },
            { id: 6, tour: 'Valle Sagrado', duracion: '1 día', agencia: 'Lago Titicaca', ruc: '20567890123', destino: 'Cusco', califTour: 1.8, califAgencia: 2.4, precio: 220.00, estado: 'Suspendido', motivo: 'Múltiples quejas de usuarios por cancelaciones.', imagen: 'https://images.unsplash.com/photo-1469854523086-cc02fe5d8800?w=150' },
            { id: 7, tour: 'Lago Titicaca', duracion: '1 día', agencia: 'Machupicchu Tours', ruc: '20678901235', destino: 'Puno', califTour: 3.6, califAgencia: 3.9, precio: 320.00, estado: 'Activo', motivo: 'Sin observaciones.', imagen: 'https://images.unsplash.com/photo-1544620347-c4fd4a3d5957?w=150' }
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
                (r.agencia && r.agencia.toLowerCase() === agencia.toLowerCase());

            return coincideBusqueda && coincideAgencia;
        });
    } else {
        toursRevisionFiltrados = toursRevisionList.filter(t => {
            const coincideBusqueda = !busqueda ||
                (t.tour && t.tour.toLowerCase().includes(busqueda)) ||
                (t.destino && t.destino.toLowerCase().includes(busqueda)) ||
                (t.motivo && t.motivo.toLowerCase().includes(busqueda));

            const coincideAgencia = (agencia === 'todos') ||
                (t.agencia && t.agencia.toLowerCase() === agencia.toLowerCase());

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
            tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; padding: 24px; color:#94a3b8;">No hay calificaciones reportadas.</td></tr>`;
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
                    <td>
                        <div class="reserva-meta-cell">
                            <strong>${r.agencia}</strong>
                            <span>${r.tour}</span>
                        </div>
                    </td>
                    <td>
                        <span class="star-rating-badge" style="color: ${r.estrellas <= 2 ? '#ef4444' : '#d97706'};">
                            <i class="ti ti-star-filled"></i> ${r.estrellas.toFixed(1)}
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
                            <img src="${t.imagen || 'https://images.unsplash.com/photo-1526392060635-9d6019884377?w=150'}" class="tour-thumb-img" alt="${t.tour}">
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
        text: 'La calificación ya no afectará el puntaje del tour.',
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#ef4444',
        cancelButtonColor: '#94a3b8',
        confirmButtonText: 'Sí, eliminar',
        cancelButtonText: 'Cancelar'
    }).then(result => {
        if (result.isConfirmed) {
            reviewsList = reviewsList.filter(r => r.id !== id);
            aplicarFiltros();
            Swal.fire('Eliminada', 'La reseña ha sido retirada del sistema.', 'success');
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
