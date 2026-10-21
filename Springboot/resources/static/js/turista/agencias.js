// Travelink - Lógica de Agencias y Filtros Dinámicos (Fase 6)
// Carga agencias reales y activas desde /api/agencias sin datos simulados

let agenciasCargadas = [];

function normalizarTexto(str) {
    if (!str) return '';
    return str
        .toLowerCase()
        .normalize("NFD")
        .replace(/[\u0300-\u036f]/g, "")
        .replace(/-/g, ' ')
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
        noResultados.style.display = (visibleCount === 0) ? 'block' : 'none';
    }

    const btn = document.getElementById('btn-aplicar-filtros');
    if (btn && e && (e.type === 'click' || (e.key && e.key === 'Enter'))) {
        const originalHtml = btn.innerHTML;
        btn.innerHTML = '<i class="fas fa-check"></i> ¡Filtros aplicados!';
        btn.style.background = '#15803d';
        setTimeout(() => {
            btn.innerHTML = originalHtml;
            btn.style.background = '#196f3d';
        }, 800);
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

    container.innerHTML = `
        <div style="grid-column: 1 / -1; text-align: center; padding: 50px 20px; color: #64748b;">
            <i class="fas fa-spinner fa-spin fa-2x" style="color: #196f3d; margin-bottom: 12px;"></i>
            <p style="margin: 0; font-weight: 600; color: #0b1f38;">Cargando agencias turísticas...</p>
        </div>`;

    try {
        const resp = await fetch('/api/agencias');
        if (!resp.ok) throw new Error('Error al consultar agencias');
        const data = await resp.json();
        
        const lista = (data && data.data && Array.isArray(data.data.agencias)) 
            ? data.data.agencias 
            : (data && Array.isArray(data.agencias) ? data.agencias : []);

        agenciasCargadas = lista;

        if (lista.length === 0) {
            container.innerHTML = `
                <div style="grid-column: 1 / -1; text-align: center; padding: 60px 20px; color: #64748b; background: white; border-radius: 12px; border: 1px solid #e2e8f0;">
                    <i class="fa-solid fa-building" style="font-size: 40px; color: #cbd5e1; margin-bottom: 16px;"></i>
                    <h3 style="color: #0b1f38; font-size: 18px; margin: 0 0 8px 0;">No hay agencias disponibles</h3>
                    <p style="margin: 0; font-size: 14px;">No se encontraron agencias activas en la plataforma.</p>
                </div>`;
            const contador = document.getElementById('contador-agencias');
            if (contador) contador.textContent = 'Mostrando 0 agencias';
            return;
        }

        container.innerHTML = lista.map(a => {
            const rVal = parseFloat(a.rating || a.promedioCalificacion || 0);
            const tCalif = parseInt(a.totalCalificaciones || a.total || 0);
            const ratingHtml = (tCalif > 0 && rVal > 0)
                ? `<i class="fa-solid fa-star" style="color:#f59e0b;"></i> ${rVal.toFixed(1)} (${tCalif} ${tCalif === 1 ? 'opinión' : 'opiniones'})`
                : `<span style="color:#64748b; font-size:12px;">Aún sin calificaciones</span>`;

            const nombreAgencia = a.nombre || a.razonSocial || 'Agencia Travelink';
            const ciudad = a.ciudad || 'Perú';
            const idAgencia = a.id || a.idAgencia || 1;

            return `
            <div class="agencia-item-card" data-nombre="${nombreAgencia.toLowerCase()}" data-ubicacion="${ciudad.toLowerCase()}" data-rating="${rVal}">
                <div class="agencia-item-left">
                    <div class="agencia-logo-circle" style="background: #111827; color: #f59e0b; width:52px; height:52px; border-radius:50%; display:flex; align-items:center; justify-content:center; font-size:22px; flex-shrink:0;">
                        <i class="fa-solid fa-building"></i>
                    </div>
                    <div class="agencia-item-details">
                        <h3 style="margin:0 0 4px 0; color:#0b1f38; font-size:17px; font-weight:700;">${nombreAgencia}</h3>
                        <p class="agencia-item-desc" style="margin:0 0 8px 0; color:#64748b; font-size:13.5px; line-height:1.4;">${a.descripcion || ('Agencia turística autorizada. RUC: ' + (a.ruc || 'N/A'))}</p>
                        <div class="agencia-item-meta" style="display:flex; gap:16px; font-size:12.5px; color:#475569;">
                            <span class="location"><i class="fa-solid fa-location-dot" style="color:#196f3d;"></i> ${ciudad}</span>
                            <span class="rating">${ratingHtml}</span>
                        </div>
                    </div>
                </div>
                <a href="experiencias.html?idAgencia=${idAgencia}&agencia=${encodeURIComponent(nombreAgencia)}" class="btn-ver-tours" style="text-decoration:none; display:inline-flex; align-items:center; gap:6px; background:#196f3d; color:white; padding:10px 18px; border-radius:8px; font-weight:700; font-size:13px; align-self:center;">
                    Ver experiencias <i class="fa-solid fa-arrow-right"></i>
                </a>
            </div>
            `;
        }).join('');

        aplicarFiltrosAgencias();
    } catch (e) {
        console.error("Error al cargar agencias dinámicas:", e);
        container.innerHTML = `
            <div style="grid-column: 1 / -1; text-align: center; padding: 50px 20px; color: #ef4444; background: #fef2f2; border-radius: 12px; border: 1px solid #fee2e2;">
                <i class="fa-solid fa-triangle-exclamation fa-2x" style="margin-bottom: 10px;"></i>
                <p style="margin: 0; font-weight: 600;">No se pudieron cargar las agencias en este momento.</p>
            </div>`;
    }
};

document.addEventListener('DOMContentLoaded', () => {
    cargarAgenciasDinamicas();

    const busqueda = document.getElementById('agencia-busqueda');
    if (busqueda) {
        busqueda.addEventListener('keyup', (e) => {
            if (e.key === 'Enter') aplicarFiltrosAgencias(e);
        });
    }

    const btnAplicar = document.getElementById('btn-aplicar-filtros');
    if (btnAplicar) {
        btnAplicar.addEventListener('click', (e) => aplicarFiltrosAgencias(e));
    }

    const btnLimpiar = document.getElementById('btn-limpiar-filtros');
    if (btnLimpiar) {
        btnLimpiar.addEventListener('click', (e) => limpiarFiltrosAgencias(e));
    }
});
