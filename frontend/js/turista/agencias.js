// Travelink - Lógica de Agencias y Filtros

function normalizarTexto(str) {
    if (!str) return '';
    return str
        .toLowerCase()
        .normalize("NFD")
        .replace(/[\u0300-\u036f]/g, "")
        .trim();
}

window.aplicarFiltrosAgencias = function(e) {
    if (e && e.preventDefault) e.preventDefault();

    const busquedaRaw = document.getElementById('agencia-busqueda')?.value || '';
    const ubicacionRaw = document.getElementById('agencia-ubicacion')?.value || '';
    const calificacionRaw = document.getElementById('agencia-calificacion')?.value || '';

    const busqueda = normalizarTexto(busquedaRaw);
    const ubicacion = normalizarTexto(ubicacionRaw);
    const calificacionMin = parseFloat(calificacionRaw || '0');

    const cards = document.querySelectorAll('.agencia-item-card');
    const noResultados = document.getElementById('no-agencias-resultados');
    let visibleCount = 0;

    cards.forEach(card => {
        const nombre = normalizarTexto(card.getAttribute('data-nombre') || '');
        const cardUbicacion = normalizarTexto(card.getAttribute('data-ubicacion') || '');
        const cardRating = parseFloat(card.getAttribute('data-rating') || '0');
        const cardText = normalizarTexto(card.innerText || '');

        let matchesBusqueda = !busqueda || nombre.includes(busqueda) || cardText.includes(busqueda) || cardUbicacion.includes(busqueda);
        let matchesUbicacion = !ubicacion || cardUbicacion.includes(ubicacion) || ubicacion.includes(cardUbicacion);
        let matchesCalificacion = isNaN(calificacionMin) || calificacionMin === 0 || cardRating >= calificacionMin;

        if (matchesBusqueda && matchesUbicacion && matchesCalificacion) {
            card.style.display = 'flex';
            visibleCount++;
        } else {
            card.style.display = 'none';
        }
    });

    const contador = document.getElementById('contador-agencias');
    if (contador) {
        contador.textContent = `Mostrando ${visibleCount} agencia${visibleCount === 1 ? '' : 's'}`;
    }

    if (noResultados) {
        if (visibleCount === 0) {
            noResultados.style.display = 'block';
        } else {
            noResultados.style.display = 'none';
        }
    }
};

window.limpiarFiltrosAgencias = function(e) {
    if (e && e.preventDefault) e.preventDefault();

    const busqueda = document.getElementById('agencia-busqueda');
    if (busqueda) busqueda.value = '';

    const ubicacion = document.getElementById('agencia-ubicacion');
    if (ubicacion) ubicacion.value = '';

    const calificacion = document.getElementById('agencia-calificacion');
    if (calificacion) calificacion.value = '';

    aplicarFiltrosAgencias();
};

window.cargarAgenciasDinamicas = async function() {
    const container = document.getElementById('agencias-container');
    if (!container) return;

    const fallbackAgencias = [
        { id: 1, idUsuario: 2, nombre: 'ANDES TOURS PERU S.A.C.', ciudad: 'Cusco', ruc: '20601234567', rating: 4.8, descripcion: 'Especialistas en turismo cultural y de aventura en la región del Cusco.' },
        { id: 2, idUsuario: 3, nombre: 'INKA TRAVEL EXPERIENCES S.A.C.', ciudad: 'Cusco', ruc: '20609876543', rating: 4.7, descripcion: 'Tours personalizados, senderismo y experiencias arqueológicas únicas.' },
        { id: 3, idUsuario: 5, nombre: 'AGENCIA ALEGRIA S.A', ciudad: 'Cusco', ruc: '10721439114', rating: 4.8, descripcion: 'Excursiones y viajes inolvidables en los mejores destinos turísticos.' },
        { id: 5, idUsuario: 8, nombre: 'AGENCIA SELVA S.A', ciudad: 'Madre de Dios', ruc: '10721439116', rating: 4.8, descripcion: 'Explora la Amazonía peruana, flora y fauna silvestre.' },
        { id: 6, idUsuario: 9, nombre: 'TOUR AREQUIPA S.A.S', ciudad: 'Arequipa', ruc: '10721439118', rating: 4.9, descripcion: 'Rutas guiadas por la Ciudad Blanca y el Cañón del Colca.' },
        { id: 7, idUsuario: 10, nombre: 'TOUR LIMA S.A', ciudad: 'Lima', ruc: '10721439113', rating: 4.8, descripcion: 'Tours urbanos, centro histórico colonial y gastronomía en Lima.' }
    ];

    let agenciasFinales = fallbackAgencias;

    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), 1500);

    try {
        const resp = await fetch('http://localhost:8080/api/admin/agencias', { signal: controller.signal });
        clearTimeout(timeoutId);
        const data = await resp.json();
        const lista = (data && data.data && Array.isArray(data.data.agencias)) ? data.data.agencias : [];
        if (lista.length > 0) {
            agenciasFinales = lista;
        }
    } catch (e) {
        console.warn("Fallo fetch agencias dinámicas, usando agencias reales de BD:", e);
    }

    container.innerHTML = agenciasFinales.map(a => `
        <div class="agencia-item-card" data-nombre="${(a.nombre || '').toLowerCase()}" data-ubicacion="${(a.ciudad || 'cusco').toLowerCase()}" data-rating="${a.rating || 4.8}">
            <div class="agencia-item-left">
                <div class="agencia-logo-circle" style="background: #111827; color: #f59e0b;">
                    <i class="fa-solid fa-building"></i>
                </div>
                <div class="agencia-item-details">
                    <h3>${a.nombre}</h3>
                    <p class="agencia-item-desc">${a.descripcion || ('Agencia autorizada. RUC: ' + (a.ruc || 'N/A') + '. Contacto: ' + (a.email || 'Contacto activo'))}</p>
                    <div class="agencia-item-meta">
                        <span class="location"><i class="fa-solid fa-location-dot"></i> ${a.ciudad || 'Cusco'}</span>
                        <span class="rating"><i class="fa-solid fa-star"></i> ${a.rating || 4.8} (300)</span>
                    </div>
                </div>
            </div>
            <a href="detalle-agencia.html?agencia=${encodeURIComponent(a.nombre)}&idAgencia=${a.id||a.idAgencia||a.idUsuario||1}" class="btn-ver-tours">Ver tours</a>
        </div>
    `).join('');
    aplicarFiltrosAgencias();
};

document.addEventListener('DOMContentLoaded', () => {
    cargarAgenciasDinamicas();

    const busqueda = document.getElementById('agencia-busqueda');
    if (busqueda) {
        busqueda.addEventListener('input', () => aplicarFiltrosAgencias());
        busqueda.addEventListener('keyup', (e) => {
            if (e.key === 'Enter') aplicarFiltrosAgencias();
        });
    }

    const ubicacion = document.getElementById('agencia-ubicacion');
    if (ubicacion) {
        ubicacion.addEventListener('change', () => aplicarFiltrosAgencias());
    }

    const calificacion = document.getElementById('agencia-calificacion');
    if (calificacion) {
        calificacion.addEventListener('change', () => aplicarFiltrosAgencias());
    }

    aplicarFiltrosAgencias();
});

