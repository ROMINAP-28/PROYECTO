?// Travelink - Lógica de Agencias y Filtros

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

document.addEventListener('DOMContentLoaded', () => {
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
