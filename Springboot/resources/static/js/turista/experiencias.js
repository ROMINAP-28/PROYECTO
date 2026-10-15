// Travelink - Lógica de Experiencias y Filtros Dinámicos (Fase 6)
// Carga paquetes publicados con salidas futuras desde el backend (sin mock ni fallbacks).

let toursCargados = [];

function normalizarTexto(str) {
    if (!str) return '';
    return str
        .toLowerCase()
        .normalize("NFD")
        .replace(/[\u0300-\u036f]/g, "")
        .replace(/-/g, ' ')
        .trim();
}

// Cargar destinos dinámicos desde la BD en el select de filtros
async function cargarDestinosSelect() {
    const destinoSelect = document.getElementById('destino');
    if (!destinoSelect) return;

    try {
        const resp = await fetch('/api/destinos');
        if (!resp.ok) return;
        const data = await resp.json();
        const destinos = (data && data.status === 'success' && Array.isArray(data.data)) ? data.data : [];

        if (destinos.length > 0) {
            // Guardar valor actual si viene de la URL
            const urlParams = new URLSearchParams(window.location.search);
            const destinoUrl = urlParams.get('destino') || '';

            const optionsHtml = ['<option value="">Todos los destinos</option>'];
            destinos.forEach(d => {
                const sel = (destinoUrl && normalizarTexto(d.nombre) === normalizarTexto(destinoUrl)) ? 'selected' : '';
                optionsHtml.push(`<option value="${d.nombre}" ${sel}>${d.nombre}</option>`);
            });
            destinoSelect.innerHTML = optionsHtml.join('');
        }
    } catch (e) {
        console.warn('No se pudieron cargar destinos dinámicos:', e);
    }
}

// Función principal para aplicar filtros
window.aplicarFiltrosExperiencias = function(e) {
    if (e && e.preventDefault) e.preventDefault();

    const destinoRaw = document.getElementById('destino')?.value || '';
    const categoriaRaw = document.getElementById('categoria')?.value || '';
    const calificacionRaw = document.getElementById('calificacion')?.value || '';
    const precioMaxRaw = document.getElementById('precio')?.value || '';
    const duracionRaw = document.getElementById('duracion')?.value || '';
    const busquedaRaw = document.getElementById('busqueda-input')?.value || '';

    const destino = normalizarTexto(destinoRaw);
    const categoria = normalizarTexto(categoriaRaw);
    const calificacionMin = parseFloat(calificacionRaw || '0');
    const precioMax = parseFloat(precioMaxRaw || '0');
    const duracionFiltro = duracionRaw ? parseInt(duracionRaw) : 0;
    const busqueda = normalizarTexto(busquedaRaw);

    const cards = document.querySelectorAll('.card-exp');
    const noResultados = document.getElementById('no-resultados');
    let visibleCount = 0;

    cards.forEach(card => {
        const cardDestino = normalizarTexto(card.getAttribute('data-destino') || '');
        const cardCategoria = normalizarTexto(card.getAttribute('data-categoria') || '');
        const cardRating = parseFloat(card.getAttribute('data-rating') || '0');
        const cardPrecio = parseFloat(card.getAttribute('data-precio') || '0');
        const cardDuracionDias = parseInt(card.getAttribute('data-dias') || '1');
        const cardNombre = normalizarTexto(card.getAttribute('data-nombre') || '');
        const cardText = normalizarTexto(card.innerText || '');

        let matchesDestino = !destino || cardDestino.includes(destino) || destino.includes(cardDestino) || cardNombre.includes(destino);
        let matchesCategoria = !categoria || cardCategoria.includes(categoria) || categoria.includes(cardCategoria) || cardNombre.includes(categoria);
        let matchesCalificacion = isNaN(calificacionMin) || calificacionMin === 0 || cardRating >= calificacionMin;
        let matchesPrecio = isNaN(precioMax) || precioMax === 0 || cardPrecio <= precioMax;
        
        let matchesDuracion = true;
        if (duracionFiltro > 0) {
            if (duracionFiltro === 1) matchesDuracion = cardDuracionDias === 1;
            else if (duracionFiltro === 2) matchesDuracion = cardDuracionDias === 2;
            else if (duracionFiltro >= 3) matchesDuracion = cardDuracionDias >= 3;
        }

        let matchesBusqueda = true;
        if (busqueda) {
            matchesBusqueda = cardText.includes(busqueda) || 
                              cardDestino.includes(busqueda) || 
                              cardCategoria.includes(busqueda) ||
                              cardNombre.includes(busqueda);
        }

        if (matchesDestino && matchesCategoria && matchesCalificacion && matchesPrecio && matchesDuracion && matchesBusqueda) {
            card.style.display = 'block';
            visibleCount++;
        } else {
            card.style.display = 'none';
        }
    });

    // Actualizar contador de resultados
    const contador = document.getElementById('contador-resultados');
    if (contador) {
        contador.textContent = `Mostrando ${visibleCount} experiencia${visibleCount === 1 ? '' : 's'}`;
    }

    if (noResultados) {
        noResultados.style.display = (visibleCount === 0) ? 'block' : 'none';
    }
};

// Función para limpiar filtros
window.limpiarFiltros = function(e) {
    if (e && e.preventDefault) e.preventDefault();

    const destino = document.getElementById('destino');
    if (destino) destino.value = '';

    const categoria = document.getElementById('categoria');
    if (categoria) categoria.value = '';

    const calificacion = document.getElementById('calificacion');
    if (calificacion) calificacion.value = '';

    const precio = document.getElementById('precio');
    if (precio) precio.value = '';

    const duracion = document.getElementById('duracion');
    if (duracion) duracion.value = '';

    const busqueda = document.getElementById('busqueda-input');
    if (busqueda) busqueda.value = '';

    aplicarFiltrosExperiencias();
};

function extraerDiasDeDuracion(duracionStr) {
    if (!duracionStr) return 1;
    const match = duracionStr.match(/(\d+)\s*(?:día|dia|días|dias)/i);
    if (match) return parseInt(match[1]);
    if (/full\s*day|medio\s*día/i.test(duracionStr)) return 1;
    return 1;
}

window.cargarPaquetesDinamicos = async function() {
    const grid = document.getElementById('experiencias-grid');
    if (!grid) return;

    grid.innerHTML = `
        <div style="grid-column: 1 / -1; text-align: center; padding: 60px 20px; color: #64748b;">
            <i class="fas fa-spinner fa-spin fa-3x" style="color: #196f3d; margin-bottom: 16px;"></i>
            <h3 style="color: #0b1f38; font-size: 16px; margin: 0 0 6px 0;">Cargando experiencias turísticas...</h3>
            <p style="margin: 0; font-size: 13px; color: #64748b;">Obteniendo las mejores opciones en tiempo real</p>
        </div>`;

    const urlParams = new URLSearchParams(window.location.search);
    const idAgenciaParam = parseInt(urlParams.get('idAgencia') || '0');
    const agenciaNombreParam = urlParams.get('agencia') || '';

    try {
        let apiUrl = '/api/tours?soloPublicados=true';
        if (idAgenciaParam > 0) apiUrl += `&idAgencia=${idAgenciaParam}`;

        const resp = await fetch(apiUrl);
        if (!resp.ok) throw new Error('Error en el servidor al consultar tours');
        const data = await resp.json();
        
        let tours = (data && data.status === 'success' && Array.isArray(data.data)) ? data.data : [];

        // Filtro por nombre de agencia si venía como texto en query param
        if (agenciaNombreParam) {
            const normAg = normalizarTexto(agenciaNombreParam);
            tours = tours.filter(t => t.agencia && normalizarTexto(t.agencia).includes(normAg));
        }

        toursCargados = tours;

        if (tours.length === 0) {
            grid.innerHTML = `
                <div style="grid-column: 1 / -1; text-align: center; padding: 60px 20px; color: #64748b; background: white; border-radius: 12px; border: 1px solid #e2e8f0;">
                    <i class="fa-solid fa-compass" style="font-size: 40px; color: #cbd5e1; margin-bottom: 16px;"></i>
                    <h3 style="color: #0b1f38; font-size: 18px; margin: 0 0 8px 0;">No hay paquetes turísticos disponibles</h3>
                    <p style="margin: 0; font-size: 14px;">Actualmente no hay tours con salidas programadas vigentes.</p>
                </div>`;
            const contador = document.getElementById('contador-resultados');
            if (contador) contador.textContent = 'Mostrando 0 experiencias';
            return;
        }

        grid.innerHTML = tours.map(p => {
            const tourId = p.idTour || p.id;
            // En listados: SOLO la imagen principal
            const img = p.imagenPrincipal || p.imagen || '../../img/colca.jpg';
            const cupos = p.cuposDisponibles !== undefined ? p.cuposDisponibles : 20;
            const minOferta = p.minAsientosOferta || 5;
            const esOferta = cupos <= minOferta || (p.descuento && p.descuento > 0);
            const badge = esOferta 
                ? '<span style="position:absolute; top:12px; left:12px; background:#f59e0b; color:white; font-size:11px; font-weight:bold; padding:4px 10px; border-radius:20px; box-shadow:0 2px 4px rgba(0,0,0,0.2);">¡ÚLTIMOS ESPACIOS!</span>'
                : '<span style="position:absolute; top:12px; left:12px; background:#196f3d; color:white; font-size:11px; font-weight:bold; padding:4px 10px; border-radius:20px; box-shadow:0 2px 4px rgba(0,0,0,0.2);">Disponible</span>';

            const ratingVal = parseFloat(p.calificacionPromedio || p.calificacion || 0);
            const totalCalif = parseInt(p.totalCalificaciones || 0);
            const ratingHtml = (totalCalif > 0 && ratingVal > 0)
                ? `<i class="fas fa-star"></i> ${ratingVal.toFixed(1)} <span style="color:#64748b; font-weight:normal;">(${totalCalif})</span>`
                : '<span style="color:#64748b; font-size:12px; font-weight:normal;">Aún sin calificaciones</span>';

            const pAdulto = parseFloat(p.precioAdulto || p.precio || 0);
            const diasDuracion = extraerDiasDeDuracion(p.duracion);
            const destinoStr = p.destino || p.ubicacion || 'Perú';

            return `
            <div class="card card-exp"
                data-destino="${destinoStr.toLowerCase()}"
                data-categoria="${(p.categoria || 'cultural').toLowerCase()}"
                data-rating="${ratingVal}"
                data-precio="${pAdulto}"
                data-dias="${diasDuracion}"
                data-nombre="${(p.nombre || '').toLowerCase()}"
                onclick="verDetallePaquete(${tourId})"
                style="background:white; border-radius:12px; border:1px solid #e2e8f0; overflow:hidden; cursor:pointer; transition:transform 0.2s; box-shadow:0 4px 6px rgba(0,0,0,0.02);">
                <div style="position:relative;">
                    <img loading="lazy" src="${img}" alt="${p.nombre}" style="width:100%; height:180px; object-fit:cover;" onerror="this.onerror=null; this.src='../../img/colca.jpg';">
                    ${badge}
                </div>
                <div style="padding:16px;">
                    <h3 style="color:#0b1f38; font-size:16px; margin:0 0 4px 0; font-weight:700;">${p.nombre}</h3>
                    <div style="color:#64748b; font-size:13px; margin-bottom:8px;"><i class="fas fa-building" style="width:16px;"></i> ${p.agencia || 'Agencia Travelink'}</div>
                    <div style="color:#f59e0b; font-size:13px; font-weight:bold; margin-bottom:15px;">${ratingHtml}</div>

                    <div style="display:flex; justify-content:space-between; align-items:flex-end; border-top:1px solid #f1f5f9; padding-top:12px;">
                        <div style="display:flex; flex-direction:column; gap:4px; color:#64748b; font-size:12px;">
                            <span><i class="fa-solid fa-location-dot" style="width:14px; color:#196f3d;"></i> ${destinoStr}</span>
                            <span><i class="far fa-clock" style="width:14px;"></i> ${p.duracion || '1 día'}</span>
                        </div>
                        <div style="text-align:right;">
                            <span style="font-size:11px; color:#64748b; display:block;">Desde</span>
                            <strong style="display:block; color:#0b1f38; font-size:16px;">S/ ${pAdulto.toFixed(2)}</strong>
                            <span style="color:#64748b; font-size:11px;">por adulto</span>
                        </div>
                    </div>
                    <button onclick="verDetallePaquete(${tourId}); event.stopPropagation();"
                        style="width:100%; background:#196f3d; color:white; border:none; padding:10px; border-radius:6px; font-weight:bold; margin-top:16px; cursor:pointer;">
                        Ver Detalle / Reservar
                    </button>
                </div>
            </div>`;
        }).join('');

        aplicarFiltrosExperiencias();
    } catch (err) {
        console.error('Error al cargar experiencias dinámicas:', err);
        grid.innerHTML = `
            <div style="grid-column: 1 / -1; text-align: center; padding: 60px 20px; color: #ef4444; background: #fef2f2; border-radius: 12px; border: 1px solid #fee2e2;">
                <i class="fa-solid fa-triangle-exclamation fa-2x" style="margin-bottom: 12px;"></i>
                <h3 style="margin: 0 0 6px 0; font-size: 16px;">No se pudieron cargar los tours</h3>
                <p style="margin: 0; font-size: 13.5px; color: #7f1d1d;">Ocurrió un error al conectar con el servidor.</p>
            </div>`;
    }
};

window.verDetallePaquete = function(idTour) {
    window.location.href = `detalle.html?tour=${idTour}&from=experiencias`;
};

// Cargar parámetros de URL al iniciar (ej. ?destino=Cusco o ?categoria=Aventura)
function procesarParametrosURL() {
    const urlParams = new URLSearchParams(window.location.search);
    const destinoParam = urlParams.get('destino');
    const categoriaParam = urlParams.get('categoria');
    const busquedaParam = urlParams.get('busqueda') || urlParams.get('q');

    if (destinoParam) {
        const destinoSelect = document.getElementById('destino');
        if (destinoSelect) {
            const normParam = normalizarTexto(destinoParam);
            for (let i = 0; i < destinoSelect.options.length; i++) {
                if (normalizarTexto(destinoSelect.options[i].value) === normParam || normalizarTexto(destinoSelect.options[i].text).includes(normParam)) {
                    destinoSelect.selectedIndex = i;
                    break;
                }
            }
        }
    }

    if (categoriaParam) {
        const categoriaSelect = document.getElementById('categoria');
        if (categoriaSelect) {
            const normParam = normalizarTexto(categoriaParam);
            for (let i = 0; i < categoriaSelect.options.length; i++) {
                if (normalizarTexto(categoriaSelect.options[i].value) === normParam || normalizarTexto(categoriaSelect.options[i].text).includes(normParam)) {
                    categoriaSelect.selectedIndex = i;
                    break;
                }
            }
        }
    }

    if (busquedaParam) {
        const busquedaInput = document.getElementById('busqueda-input');
        if (busquedaInput) {
            busquedaInput.value = busquedaParam;
        }
    }
}

document.addEventListener('DOMContentLoaded', async () => {
    await Promise.all([cargarDestinosSelect(), cargarPaquetesDinamicos()]);
    procesarParametrosURL();

    document.querySelectorAll('.filtros-sidebar select').forEach(sel => {
        sel.addEventListener('change', () => aplicarFiltrosExperiencias());
    });

    const busquedaInput = document.getElementById('busqueda-input');
    if (busquedaInput) {
        busquedaInput.addEventListener('keyup', (e) => {
            if (e.key === 'Enter') aplicarFiltrosExperiencias();
        });
        busquedaInput.addEventListener('input', () => aplicarFiltrosExperiencias());
    }
});

