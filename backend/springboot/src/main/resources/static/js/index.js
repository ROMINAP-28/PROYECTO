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
});

