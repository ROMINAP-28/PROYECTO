// Travelink - Lógica de Experiencias y Filtros

function normalizarTexto(str) {
    if (!str) return '';
    return str
        .toLowerCase()
        .normalize("NFD")
        .replace(/[\u0300-\u036f]/g, "")
        .trim();
}

// Función principal para aplicar filtros
window.aplicarFiltrosExperiencias = function(e) {
    if (e && e.preventDefault) e.preventDefault();

    const destinoRaw = document.getElementById('destino')?.value || document.getElementById('filtro-destino')?.value || '';
    const categoriaRaw = document.getElementById('categoria')?.value || document.getElementById('filtro-categoria')?.value || '';
    const calificacionRaw = document.getElementById('calificacion')?.value || document.getElementById('filtro-calificacion')?.value || '';
    const busquedaRaw = document.getElementById('busqueda-input')?.value || document.querySelector('.resultados-content input[type="text"]')?.value || '';

    const destino = normalizarTexto(destinoRaw);
    const categoria = normalizarTexto(categoriaRaw);
    const calificacionMin = parseFloat(calificacionRaw || '0');
    const busqueda = normalizarTexto(busquedaRaw);

    const cards = document.querySelectorAll('.card-exp');
    const noResultados = document.getElementById('no-resultados');
    let visibleCount = 0;

    cards.forEach(card => {
        const cardDestino = normalizarTexto(card.getAttribute('data-destino') || '');
        const cardCategoria = normalizarTexto(card.getAttribute('data-categoria') || '');
        const cardRating = parseFloat(card.getAttribute('data-rating') || '0');
        const cardNombre = normalizarTexto(card.getAttribute('data-nombre') || '');
        const cardText = normalizarTexto(card.innerText || '');

        let matchesDestino = !destino || cardDestino.includes(destino) || destino.includes(cardDestino);
        let matchesCategoria = !categoria || cardCategoria.includes(categoria);
        let matchesCalificacion = isNaN(calificacionMin) || calificacionMin === 0 || cardRating >= calificacionMin;
        
        let matchesBusqueda = true;
        if (busqueda) {
            matchesBusqueda = cardText.includes(busqueda) || 
                              cardDestino.includes(busqueda) || 
                              cardCategoria.includes(busqueda) ||
                              cardNombre.includes(busqueda);
        }

        if (matchesDestino && matchesCategoria && matchesCalificacion && matchesBusqueda) {
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
        if (visibleCount === 0) {
            noResultados.style.display = 'block';
        } else {
            noResultados.style.display = 'none';
        }
    }
};

// Función para limpiar filtros
window.limpiarFiltros = function(e) {
    if (e && e.preventDefault) e.preventDefault();

    const destino = document.getElementById('destino') || document.getElementById('filtro-destino');
    if (destino) destino.value = '';

    const categoria = document.getElementById('categoria') || document.getElementById('filtro-categoria');
    if (categoria) categoria.value = '';

    const calificacion = document.getElementById('calificacion') || document.getElementById('filtro-calificacion');
    if (calificacion) calificacion.value = '';

    const busqueda = document.getElementById('busqueda-input') || document.querySelector('.resultados-content input[type="text"]');
    if (busqueda) busqueda.value = '';

    aplicarFiltrosExperiencias();
};

// Cargar parámetros de URL al iniciar (ej. ?destino=Cusco o ?categoria=Aventura)
function procesarParametrosURL() {
    const urlParams = new URLSearchParams(window.location.search);
    const destinoParam = urlParams.get('destino');
    const categoriaParam = urlParams.get('categoria');
    const busquedaParam = urlParams.get('busqueda') || urlParams.get('q');

    if (destinoParam) {
        const destinoSelect = document.getElementById('destino') || document.getElementById('filtro-destino');
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
        const categoriaSelect = document.getElementById('categoria') || document.getElementById('filtro-categoria');
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
        const busquedaInput = document.getElementById('busqueda-input') || document.querySelector('.resultados-content input[type="text"]');
        if (busquedaInput) {
            busquedaInput.value = busquedaParam;
        }
    }

    aplicarFiltrosExperiencias();
}

document.addEventListener('DOMContentLoaded', () => {
    // Event listeners para filtros directos en selects de la barra lateral
    document.querySelectorAll('.filtros-sidebar select').forEach(sel => {
        sel.addEventListener('change', () => aplicarFiltrosExperiencias());
    });

    const busquedaInput = document.getElementById('busqueda-input') || document.querySelector('.resultados-content input[type="text"]');
    if (busquedaInput) {
        busquedaInput.addEventListener('keyup', (e) => {
            if (e.key === 'Enter') aplicarFiltrosExperiencias();
        });
        busquedaInput.addEventListener('input', () => aplicarFiltrosExperiencias());
    }

    procesarParametrosURL();
});
