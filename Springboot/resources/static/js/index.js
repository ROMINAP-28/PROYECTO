const toggle = document.getElementById('personas-toggle');
const popover = document.getElementById('personas-popover');

if (toggle && popover) {
    toggle.addEventListener('click', function (e) {
        popover.style.display = popover.style.display === 'none' ? 'block' : 'none';
        e.stopPropagation();
    });

    document.addEventListener('click', function (e) {
        if (!toggle.contains(e.target) && !popover.contains(e.target)) {
            popover.style.display = 'none';
        }
    });
}

let counts = { adultos: 1, ninos: 0, bebes: 0 };
window.updatePasajeros = function(type, delta) {
    let newVal = counts[type] + delta;
    if (type === 'adultos' && newVal < 1) return;
    if (newVal < 0) return;
    counts[type] = newVal;
    document.getElementById('count-' + type).innerText = newVal;

    let total = counts.adultos + counts.ninos + counts.bebes;
    if (toggle) {
        toggle.innerText = total + (total === 1 ? ' Pasajero' : ' Pasajeros');
    }
}

window.toggleHeart = function(element) {
    if (element.classList.contains('far')) {
        element.classList.remove('far');
        element.classList.add('fas');
        element.style.color = 'red';
    } else {
        element.classList.remove('fas');
        element.classList.add('far');
        element.style.color = '';
    }
}

document.addEventListener('DOMContentLoaded', () => {
    // Inicializar el calendario de rango de fechas
    if (typeof flatpickr !== 'undefined') {
        flatpickr("#fecha-rango", {
            mode: "range",
            dateFormat: "Y-m-d",
            showMonths: 2,
            minDate: "today"
        });
    }

    // Lógica del botón buscar
    const btnSearch = document.querySelector('.btn-search');
    const destinoSelect = document.getElementById('destino');
    
    if(btnSearch && destinoSelect) {
        btnSearch.addEventListener('click', () => {
            const destino = destinoSelect.value.toLowerCase();
            const cards = document.querySelectorAll('.card-exp, .card-destino');
            
            let foundAny = false;
            
            cards.forEach(card => {
                const titleElement = card.querySelector('h3');
                const subtitleElement = card.querySelector('.subtitle, .location, .card-icons span, .card-overlay p');
                
                let matches = false;
                
                if(!destino) {
                    matches = true;
                } else {
                    if(titleElement && titleElement.innerText.toLowerCase().includes(destino)) matches = true;
                    if(subtitleElement && subtitleElement.innerText.toLowerCase().includes(destino)) matches = true;
                }
                
                if(matches) {
                    card.style.display = 'block';
                    foundAny = true;
                } else {
                    card.style.display = 'none';
                }
            });
            
            if(!foundAny && destino) {
                Swal.fire({title: 'Sin resultados', text: 'No se encontraron experiencias para el destino seleccionado en esta página.', icon: 'warning', confirmButtonText: 'Entendido', confirmButtonColor: '#ffc107'});
            }
        });
    }

    // Cargar tours recomendados dinámicos desde la BD (publicados con salidas futuras)
    cargarToursRecomendados();
});

async function cargarToursRecomendados() {
    const container = document.getElementById('tours-recomendados-container');
    if (!container) return;

    try {
        const resp = await fetch('/api/tours?soloPublicados=true');
        if (!resp.ok) throw new Error('Error al consultar tours');
        const data = await resp.json();
        const tours = (data && data.status === 'success' && Array.isArray(data.data)) ? data.data : [];

        if (tours.length === 0) {
            container.innerHTML = `
                <div style="padding: 24px; text-align: center; color: #64748b; font-size: 13.5px;">
                    No hay tours recomendados disponibles por el momento.
                </div>`;
            return;
        }

        // Mostrar hasta 3 tours recomendados
        const destacados = tours.slice(0, 3);
        container.innerHTML = destacados.map(t => {
            const img = t.imagenPrincipal || t.imagen || 'img/cusco.jpg';
            const precio = parseFloat(t.precioAdulto || t.precio || 0).toFixed(2);
            const calif = parseFloat(t.calificacionPromedio || t.calificacion || 0);
            const totalC = parseInt(t.totalCalificaciones || 0);

            const ratingHtml = (totalC > 0 && calif > 0)
                ? `<div class="tour-rating">
                       <i class="fas fa-star" style="color: #f59e0b;"></i>
                       <span>${calif.toFixed(1)}</span>
                       <span style="color: #94a3b8; font-weight: normal;">(${totalC})</span>
                   </div>`
                : `<div class="tour-rating">
                       <span style="color: #94a3b8; font-size: 12px;">Aún sin calificaciones</span>
                   </div>`;

            return `
                <a href="html/turista/detalle.html?tour=${t.idTour || t.id}&from=index" class="tour-item-horizontal">
                    <img loading="lazy" src="${img}" alt="${t.nombre}" onerror="this.onerror=null; this.src='img/cusco.jpg';">
                    <div class="tour-info">
                        <h3 class="tour-title">${t.nombre}</h3>
                        <div class="tour-agency">${t.agencia || 'Agencia Travelink'}</div>
                        ${ratingHtml}
                    </div>
                    <div class="tour-price">
                        <span style="font-size: 11px; color: #64748b; display: block; font-weight: normal;">Desde</span>
                        S/ ${precio}
                    </div>
                </a>
            `;
        }).join('');
    } catch (err) {
        console.warn('Error al cargar tours recomendados:', err);
        container.innerHTML = `
            <div style="padding: 24px; text-align: center; color: #64748b; font-size: 13.5px;">
                No se pudieron cargar los tours en este momento.
            </div>`;
    }
}



