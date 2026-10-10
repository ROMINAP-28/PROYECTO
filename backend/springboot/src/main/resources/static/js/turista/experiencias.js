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

window.cargarPaquetesDinamicos = async function() {
    const grid = document.getElementById('experiencias-grid');
    if (!grid) return;

    const urlParams = new URLSearchParams(window.location.search);
    const idAgenciaParam = parseInt(urlParams.get('idAgencia') || '0');
    const agenciaNombreParam = urlParams.get('agencia') || '';

    const fallbackPaquetes = [
        // ANDES TOURS PERU S.A.C. (idAgencia: 1)
        { idPaquete: 101, idAgencia: 1, agencia: 'ANDES TOURS PERU S.A.C.', nombre: 'Machu Picchu Clásico y Valle Sagrado', descripcion: 'Excursión completa al santuario histórico con guía profesional y tren panorámico.', precio: 350.00, duracion: '1 día / 1 noche', estado: 'PUBLICADO', imagen: '../../img/colca.jpg', cuposDisponibles: 20, cupoTotal: 25 },
        { idPaquete: 102, idAgencia: 1, agencia: 'ANDES TOURS PERU S.A.C.', nombre: 'City Tour Cusco Imperial & Sacsayhuamán', descripcion: 'Recorrido por la capital Inca, Qorikancha, Sacsayhuamán, Qenqo y Tambomachay.', precio: 120.00, duracion: '1 día', estado: 'PUBLICADO', imagen: '../../img/cusco.jpg', cuposDisponibles: 22, cupoTotal: 30 },
        { idPaquete: 103, idAgencia: 1, agencia: 'ANDES TOURS PERU S.A.C.', nombre: 'Valle Sagrado de los Incas & Ollantaytambo', descripcion: 'Pisaq, Urubamba y fortaleza fortaleza de Ollantaytambo con almuerzo buffet criollo.', precio: 220.00, duracion: '1 día', estado: 'PUBLICADO', imagen: '../../img/uros.jpg', cuposDisponibles: 18, cupoTotal: 25 },

        // INKA TRAVEL EXPERIENCES S.A.C. (idAgencia: 2)
        { idPaquete: 201, idAgencia: 2, agencia: 'INKA TRAVEL EXPERIENCES S.A.C.', nombre: 'Montaña de 7 Colores (Vinicunca)', descripcion: 'Trek guiado a la impresionante montaña de colores con desayuno y almuerzo buffet.', precio: 280.00, duracion: '1 día', estado: 'PUBLICADO', imagen: '../../img/7colores.jpg', cuposDisponibles: 15, cupoTotal: 20 },
        { idPaquete: 202, idAgencia: 2, agencia: 'INKA TRAVEL EXPERIENCES S.A.C.', nombre: 'Super Valle Sagrado + Maras Moray Salineras', descripcion: 'Circuito arqueológico completo combinando Maras, Moray y terrazas incas.', precio: 210.00, duracion: '1 día', estado: 'PUBLICADO', imagen: '../../img/colca.jpg', cuposDisponibles: 19, cupoTotal: 25 },
        { idPaquete: 203, idAgencia: 2, agencia: 'INKA TRAVEL EXPERIENCES S.A.C.', nombre: 'Ruta Inca Jungle Trek & Aventura', descripcion: 'Combinación de caminata, bicicletas de montaña y tirolesa hacia Machu Picchu.', precio: 680.00, duracion: '3 días / 2 noches', estado: 'PUBLICADO', imagen: '../../img/huaraz.jpg', cuposDisponibles: 12, cupoTotal: 15 },
        { idPaquete: 204, idAgencia: 2, agencia: 'INKA TRAVEL EXPERIENCES S.A.C.', nombre: 'Tour Exclusivo Machu Picchu en Tren Vistadome', descripcion: 'Experiencia premium VIP con show folclórico a bordo y guía personalizado.', precio: 520.00, duracion: '1 día', estado: 'PUBLICADO', imagen: '../../img/cusco.jpg', cuposDisponibles: 8, cupoTotal: 10 },

        // AGENCIA ALEGRIA S.A (idAgencia: 3)
        { idPaquete: 301, idAgencia: 3, agencia: 'AGENCIA ALEGRIA S.A', nombre: 'Laguna Humantay Trek & Aventura', descripcion: 'Caminata paisajística hacia la turquesa Laguna Humantay con paramédico y equipo de oxígeno.', precio: 150.00, duracion: '1 día', estado: 'PUBLICADO', imagen: '../../img/puno.jpg', cuposDisponibles: 18, cupoTotal: 25 },
        { idPaquete: 302, idAgencia: 3, agencia: 'AGENCIA ALEGRIA S.A', nombre: 'Tour Maras, Moray & Salineras Ancestrales', descripcion: 'Visita guiada a las pozas naturales de sal y los laboratorios agrícolas incas.', precio: 130.00, duracion: '1 día', estado: 'PUBLICADO', imagen: '../../img/ica.jpg', cuposDisponibles: 25, cupoTotal: 30 },
        { idPaquete: 303, idAgencia: 3, agencia: 'AGENCIA ALEGRIA S.A', nombre: 'Excursión Valle Sur: Tipón, Pikillacta & Andahuaylillas', descripcion: 'Arquitectura prehispánica Wari e Inka con visita a la Capilla Sixtina de América.', precio: 110.00, duracion: '1 día', estado: 'PUBLICADO', imagen: '../../img/uros.jpg', cuposDisponibles: 22, cupoTotal: 25 },

        // AGENCIA SELVA S.A (idAgencia: 5)
        { idPaquete: 501, idAgencia: 5, agencia: 'AGENCIA SELVA S.A', nombre: 'Amazonía Profunda Tambopata Lodge', descripcion: 'Inmersión completa en la selva virgen con observación de fauna silvestre, canopy y navegación fluvial.', precio: 1450.00, duracion: '4 días / 3 noches', estado: 'PUBLICADO', imagen: '../../img/selva.jpg', cuposDisponibles: 10, cupoTotal: 15 },
        { idPaquete: 502, idAgencia: 5, agencia: 'AGENCIA SELVA S.A', nombre: 'Expedición Parque Nacional Manu & Biosfera', descripcion: 'Aventura ecológica avistando guacamayos, nutrias gigantes y la biodiversidad amazónica.', precio: 1850.00, duracion: '5 días / 4 noches', estado: 'PUBLICADO', imagen: '../../img/selva.jpg', cuposDisponibles: 8, cupoTotal: 12 },
        { idPaquete: 503, idAgencia: 5, agencia: 'AGENCIA SELVA S.A', nombre: 'Trek de Selva & Lago Sandoval Ecología', descripcion: 'Navegación en canoa a remo en el Lago Sandoval observando caimanes y lobos de río.', precio: 580.00, duracion: '2 días / 1 noche', estado: 'PUBLICADO', imagen: '../../img/uros.jpg', cuposDisponibles: 14, cupoTotal: 20 },

        // TOUR AREQUIPA S.A.S (idAgencia: 6)
        { idPaquete: 601, idAgencia: 6, agencia: 'TOUR AREQUIPA S.A.S', nombre: 'Cañón del Colca & Mirador del Cóndor', descripcion: 'Recorrido por el Cañón del Colca, baños termales de La Calera y avistamiento del Cóndor andino.', precio: 180.00, duracion: '2 días / 1 noche', estado: 'PUBLICADO', imagen: '../../img/arequipa.jpg', cuposDisponibles: 12, cupoTotal: 20 },
        { idPaquete: 602, idAgencia: 6, agencia: 'TOUR AREQUIPA S.A.S', nombre: 'Ruta del Sillar & Monasterio de Santa Catalina', descripcion: 'Paseo arquitectónico por las canteras de volcán sillar y el convento histórico.', precio: 120.00, duracion: '1 día', estado: 'PUBLICADO', imagen: '../../img/arequipa.jpg', cuposDisponibles: 20, cupoTotal: 25 },
        { idPaquete: 603, idAgencia: 6, agencia: 'TOUR AREQUIPA S.A.S', nombre: 'Ascenso al Volcán Misti & Ciclismo de Montaña', descripcion: 'Trek extremo de alta montaña para aventureros experimentados.', precio: 320.00, duracion: '2 días / 1 noche', estado: 'PUBLICADO', imagen: '../../img/huaraz.jpg', cuposDisponibles: 9, cupoTotal: 12 },

        // TOUR LIMA S.A (idAgencia: 7)
        { idPaquete: 701, idAgencia: 7, agencia: 'TOUR LIMA S.A', nombre: 'Oasis de Huacachina & Tubulares Ica', descripcion: 'Aventura en los tubulares del desierto de Ica, sandboarding y visita a bodegas vitivinícolas.', precio: 190.00, duracion: '1 día', estado: 'PUBLICADO', imagen: '../../img/ica.jpg', cuposDisponibles: 22, cupoTotal: 30 },
        { idPaquete: 702, idAgencia: 7, agencia: 'TOUR LIMA S.A', nombre: 'City Tour Lima Colonial y Catacumbas Virreinales', descripcion: 'Recorrido histórico por la Plaza Mayor, Basílica de San Francisco y Museo Larco.', precio: 80.00, duracion: '1 día', estado: 'PUBLICADO', imagen: '../../img/lima-package.jpg', cuposDisponibles: 28, cupoTotal: 35 },
        { idPaquete: 703, idAgencia: 7, agencia: 'TOUR LIMA S.A', nombre: 'Sobrevuelo a las Líneas de Nazca & Paracas', descripcion: 'Excursión completa observando los geoglifos de Nazca y las Islas Ballestas.', precio: 690.00, duracion: '1 día', estado: 'PUBLICADO', imagen: '../../img/ica.jpg', cuposDisponibles: 15, cupoTotal: 20 }
    ];

    let listaPaquetes = fallbackPaquetes;

    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), 1500);

    try {
        const resp = await fetch(`http://localhost:8080/api/agencia/paquetes?idAgencia=${idAgenciaParam}`, { signal: controller.signal });
        clearTimeout(timeoutId);
        const data = await resp.json();
        
        if (data && data.status === 'success' && Array.isArray(data.paquetes) && data.paquetes.length > 0) {
            listaPaquetes = data.paquetes;
        }
    } catch (e) {
        console.warn("Fallo fetch paquetes dinámicos, usando fallback de agencias reales:", e);
    }

    // Filtrar si el usuario especificó idAgencia > 0 o nombre de agencia
    if (idAgenciaParam > 0) {
        listaPaquetes = listaPaquetes.filter(p => parseInt(p.idAgencia || '0') === idAgenciaParam || (p.agencia && normalizarTexto(p.agencia).includes(normalizarTexto(agenciaNombreParam))));
    } else if (agenciaNombreParam) {
        const normAgencia = normalizarTexto(agenciaNombreParam);
        listaPaquetes = listaPaquetes.filter(p => p.agencia && normalizarTexto(p.agencia).includes(normAgencia));
    }

    if (listaPaquetes.length > 0) {
        grid.innerHTML = listaPaquetes.map(p => {
            const img = p.imagen || p.imagenUrl || '../../img/colca.jpg';
            const cupos = p.cuposDisponibles !== undefined ? p.cuposDisponibles : (p.cupoDisponible !== undefined ? p.cupoDisponible : 25);
            const totalCupos = p.cupoTotal || 30;
            const esOferta = cupos <= 5 || (totalCupos > 0 && cupos / totalCupos <= 0.20);
            const esAgotado = cupos === 0 || String(p.estado).toUpperCase() === 'AGOTADO';
            const badge = esAgotado ? '<span style="position:absolute; top:12px; left:12px; background:#ef4444; color:white; font-size:11px; font-weight:bold; padding:4px 10px; border-radius:20px;">AGOTADO</span>'
                        : esOferta ? '<span style="position:absolute; top:12px; left:12px; background:#f59e0b; color:white; font-size:11px; font-weight:bold; padding:4px 10px; border-radius:20px;">¡ÚLTIMOS ESPACIOS!</span>'
                        : '<span style="position:absolute; top:12px; left:12px; background:#196f3d; color:white; font-size:11px; font-weight:bold; padding:4px 10px; border-radius:20px;">Disponible</span>';
            
            return `
            <div class="card card-exp"
                data-destino="${(p.destino || p.ubicacion || '').toLowerCase()}" data-categoria="${(p.categoria || p.tipoServicio || 'cultura historia').toLowerCase()}" data-rating="${p.calificacionPromedio || '4.8'}" data-nombre="${(p.nombre||'').toLowerCase()}"
                onclick="verDetallePaquete(${p.idPaquete || p.id})"
                style="background:white; border-radius:12px; border:1px solid #e2e8f0; overflow:hidden; cursor:pointer; transition:transform 0.2s; box-shadow:0 4px 6px rgba(0,0,0,0.02);">
                <div style="position:relative;">
                    <img src="${img}" alt="${p.nombre}" style="width:100%; height:180px; object-fit:cover;" onerror="this.onerror=null; this.src='../../img/colca.jpg';">
                    ${badge}
                </div>
                <div style="padding:16px;">
                    <h3 style="color:#0b1f38; font-size:16px; margin:0 0 4px 0;">${p.nombre}</h3>
                    <div style="color:#64748b; font-size:13px; margin-bottom:8px;"><i class="fas fa-building" style="width:16px;"></i> ${p.agencia || 'Agencia Autorizada'}</div>
                    <div style="color:#f59e0b; font-size:13px; font-weight:bold; margin-bottom:15px;"><i class="fas fa-star"></i> 4.8 <span style="color:#64748b; font-weight:normal;">${cupos === 0 ? '(Agotado)' : '(Cupos: ' + cupos + '/' + totalCupos + ')'}</span></div>

                    <div style="display:flex; justify-content:space-between; align-items:flex-end; border-top:1px solid #f1f5f9; padding-top:12px;">
                        <div style="display:flex; gap:12px; color:#64748b; font-size:12px;">
                            <span><i class="far fa-clock" style="width:14px;"></i> ${p.duracion || '1 día'}</span>
                        </div>
                        <div style="text-align:right;">
                            <strong style="display:block; color:#0b1f38; font-size:16px;">S/ ${parseFloat(p.precio || 0).toFixed(2)}</strong>
                            <span style="color:#64748b; font-size:11px;">por persona</span>
                        </div>
                    </div>
                    <button onclick="verDetallePaquete(${p.idPaquete || p.id}); event.stopPropagation();"
                        style="width:100%; background:${esAgotado ? '#94a3b8' : '#196f3d'}; color:white; border:none; padding:10px; border-radius:6px; font-weight:bold; margin-top:16px; cursor:${esAgotado ? 'not-allowed' : 'pointer'};" ${esAgotado ? 'disabled' : ''}>
                        ${esAgotado ? 'Agotado' : 'Reservar / Ver Detalle'}
                    </button>
                </div>
            </div>`;
        }).join('');
        
        aplicarFiltrosExperiencias();
    }
};

window.verDetallePaquete = function(idPaquete) {
    const user = window.getCurrentUser ? window.getCurrentUser() : null;
    if (!user) {
        if (typeof Swal !== 'undefined') {
            Swal.fire({
                title: 'Inicio de Sesión Requerido',
                text: 'Para realizar una reserva debes iniciar sesión o registrarte.',
                icon: 'info',
                showCancelButton: true,
                confirmButtonText: 'Iniciar Sesión',
                cancelButtonText: 'Cancelar',
                confirmButtonColor: '#196f3d'
            }).then(result => {
                if (result.isConfirmed) {
                    window.location.href = 'login.html';
                }
            });
        } else {
            if (confirm('Debes iniciar sesión para realizar una reserva. ¿Deseas ir al Login?')) {
                window.location.href = 'login.html';
            }
        }
        return;
    }
    window.location.href = `detalle.html?tour=${idPaquete}&from=experiencias`;
};

document.addEventListener('DOMContentLoaded', () => {
    cargarPaquetesDinamicos();

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

