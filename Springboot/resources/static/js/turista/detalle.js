// Travelink - Detalle del Tour Dinámico (Fase 6)
// Muestra exactamente lo cargado por la agencia, salidas reales, galería, calificaciones reales y redirección a reserva.

let currentTour = null;

document.addEventListener('DOMContentLoaded', () => {
    cargarDetalleTour();
});

function getTourIdFromUrl() {
    const params = new URLSearchParams(window.location.search);
    return params.get('tour') || params.get('id') || params.get('idTour') || params.get('idPaquete');
}

function mostrarPaqueteNoDisponible() {
    const wrapper = document.getElementById('detail-main-wrapper');
    const noDisp = document.getElementById('paquete-no-disponible');
    if (wrapper) wrapper.style.display = 'none';
    if (noDisp) noDisp.style.display = 'block';
    document.title = 'Paquete no disponible - Travelink';
}

function formatearFecha(fechaStr) {
    if (!fechaStr) return '';
    try {
        const parts = fechaStr.split('-');
        if (parts.length === 3) {
            return `${parts[2]}/${parts[1]}/${parts[0]}`;
        }
    } catch (e) {}
    return fechaStr;
}

async function cargarDetalleTour() {
    const tourId = getTourIdFromUrl();
    if (!tourId) {
        mostrarPaqueteNoDisponible();
        return;
    }

    try {
        const resp = await fetch(`/api/tours?id=${encodeURIComponent(tourId)}`);
        if (!resp.ok) {
            mostrarPaqueteNoDisponible();
            return;
        }

        const data = await resp.json();
        if (!data || data.status !== 'success' || !data.tour) {
            mostrarPaqueteNoDisponible();
            return;
        }

        currentTour = data.tour;
        renderizarDetalle(currentTour);
        cargarCalificacionesYComentarios(currentTour.idTour, currentTour.idAgencia);
    } catch (err) {
        console.error('Error al cargar detalle del tour:', err);
        mostrarPaqueteNoDisponible();
    }
}

function renderizarDetalle(tour) {
    document.title = `${tour.nombre || 'Tour'} - Travelink`;

    // 1. Galería de imágenes (en detalle se muestra la galería completa)
    const mainImg = document.getElementById('mainDetailImg');
    const thumbsCont = document.getElementById('thumbsContainer');
    const imagenes = Array.isArray(tour.imagenes) && tour.imagenes.length > 0 
        ? tour.imagenes 
        : [tour.imagenPrincipal || tour.imagen || '../../img/machupicchu.png'];

    if (mainImg) {
        mainImg.src = imagenes[0];
        mainImg.alt = tour.nombre || 'Tour';
    }

    if (thumbsCont) {
        thumbsCont.innerHTML = '';
        if (imagenes.length > 1) {
            imagenes.forEach((imgUrl, idx) => {
                const thumb = document.createElement('div');
                thumb.className = 'thumb-item-wrap';
                thumb.style.cssText = `width: 70px; height: 50px; border-radius: 8px; overflow: hidden; cursor: pointer; border: 2px solid ${idx === 0 ? '#196f3d' : 'transparent'}; flex-shrink: 0;`;
                thumb.innerHTML = `<img loading="lazy" src="${imgUrl}" alt="Miniatura ${idx + 1}" style="width:100%; height:100%; object-fit:cover;" onerror="this.onerror=null; this.src='../../img/machupicchu.png';">`;
                thumb.onclick = () => {
                    if (mainImg) mainImg.src = imgUrl;
                    document.querySelectorAll('.thumb-item-wrap').forEach(t => t.style.borderColor = 'transparent');
                    thumb.style.borderColor = '#196f3d';
                };
                thumbsCont.appendChild(thumb);
            });
        }
    }

    // 2. Título, descripción corta, agencia, categoría, duración, destino
    const elTitle = document.getElementById('tour-title');
    if (elTitle) elTitle.textContent = tour.nombre || 'Tour sin nombre';

    const elShortDesc = document.getElementById('tour-short-desc');
    if (elShortDesc) {
        if (tour.descripcionCorta && tour.descripcionCorta.trim().length > 0) {
            elShortDesc.textContent = tour.descripcionCorta.trim();
            elShortDesc.style.display = 'block';
        } else {
            elShortDesc.style.display = 'none';
        }
    }

    // Precio por adulto principal
    const pAdulto = parseFloat(tour.precioAdulto || tour.precio || 0);
    const elPriceMain = document.getElementById('tour-price-main');
    if (elPriceMain) elPriceMain.textContent = `S/ ${pAdulto.toFixed(2)}`;

    // Descuento
    const desc = parseFloat(tour.descuento || 0);
    const wrapDesc = document.getElementById('tour-discount-wrap');
    const badgeDesc = document.getElementById('tour-discount-badge');
    if (desc > 0 && wrapDesc && badgeDesc) {
        badgeDesc.textContent = `${desc}% dcto.`;
        wrapDesc.style.display = 'block';
    } else if (wrapDesc) {
        wrapDesc.style.display = 'none';
    }

    // Agencia
    const elAgency = document.getElementById('tour-agency-name');
    if (elAgency) elAgency.textContent = tour.agencia || 'Agencia Travelink';

    // Pills
    const pillCat = document.getElementById('tour-pill-cat');
    if (pillCat) pillCat.innerHTML = `<i class="fa-solid fa-layer-group"></i> ${tour.categoria || 'Cultural'}`;

    const pillDur = document.getElementById('tour-pill-duration');
    if (pillDur) pillDur.innerHTML = `<i class="fa-regular fa-clock"></i> ${tour.duracion || '1 día'}`;

    const pillLoc = document.getElementById('tour-pill-location');
    if (pillLoc) pillLoc.innerHTML = `<i class="fa-solid fa-location-dot"></i> ${tour.ubicacion || tour.destino || 'Perú'}`;

    // Descripción completa
    const elDesc = document.getElementById('tour-desc');
    const blockDesc = document.querySelector('.detail-desc-block');
    if (elDesc) {
        if (tour.descripcion && tour.descripcion.trim().length > 0) {
            elDesc.textContent = tour.descripcion.trim();
            if (blockDesc) blockDesc.style.display = 'block';
        } else {
            if (blockDesc) blockDesc.style.display = 'none';
        }
    }

    // 3. Qué incluye, Qué no incluye, Condiciones
    const cardIncluye = document.getElementById('card-incluye');
    const listIncluye = document.getElementById('list-incluye');
    const textoIncluye = tour.queIncluye || tour.incluye || '';
    if (textoIncluye && textoIncluye.trim().length > 0 && listIncluye && cardIncluye) {
        const items = textoIncluye.split(/\r?\n|,/).map(s => s.trim()).filter(Boolean);
        listIncluye.innerHTML = items.map(it => `<li><i class="fa-solid fa-check" style="color:#15803d; margin-right:6px;"></i> ${it}</li>`).join('');
        cardIncluye.style.display = 'block';
    } else if (cardIncluye) {
        cardIncluye.style.display = 'none';
    }

    const cardNoIncluye = document.getElementById('card-no-incluye');
    const listNoIncluye = document.getElementById('list-no-incluye');
    const textoNoIncluye = tour.queNoIncluye || tour.noIncluye || '';
    if (textoNoIncluye && textoNoIncluye.trim().length > 0 && listNoIncluye && cardNoIncluye) {
        const items = textoNoIncluye.split(/\r?\n|,/).map(s => s.trim()).filter(Boolean);
        listNoIncluye.innerHTML = items.map(it => `<li><i class="fa-solid fa-xmark" style="color:#b91c1c; margin-right:6px;"></i> ${it}</li>`).join('');
        cardNoIncluye.style.display = 'block';
    } else if (cardNoIncluye) {
        cardNoIncluye.style.display = 'none';
    }

    const cardCond = document.getElementById('card-condiciones');
    const textCond = document.getElementById('text-condiciones');
    if (tour.condiciones && tour.condiciones.trim().length > 0 && cardCond && textCond) {
        textCond.textContent = tour.condiciones.trim();
        cardCond.style.display = 'block';
    } else if (cardCond) {
        cardCond.style.display = 'none';
    }

    // Si los tres están vacíos, ocultar card-inclusiones-container
    const cardInclusionesWrap = document.getElementById('card-inclusiones-container');
    if (cardInclusionesWrap) {
        const vacioIncluye = !textoIncluye || textoIncluye.trim().length === 0;
        const vacioNoIncluye = !textoNoIncluye || textoNoIncluye.trim().length === 0;
        const vacioCond = !tour.condiciones || tour.condiciones.trim().length === 0;
        if (vacioIncluye && vacioNoIncluye && vacioCond) {
            cardInclusionesWrap.style.display = 'none';
        } else {
            cardInclusionesWrap.style.display = 'block';
        }
    }

    // 4. Precios por tipo
    const elPAdulto = document.getElementById('price-adulto');
    if (elPAdulto) elPAdulto.textContent = `S/ ${pAdulto.toFixed(2)}`;

    // Niño
    const rowNino = document.getElementById('row-price-nino');
    const labelNino = document.getElementById('label-price-nino');
    const priceNino = document.getElementById('price-nino');
    if (tour.aceptaNinos && rowNino) {
        const edadMax = tour.edadMaxNino || 11;
        if (labelNino) labelNino.textContent = `Niño (hasta ${edadMax} años)`;
        if (priceNino) priceNino.textContent = `S/ ${parseFloat(tour.precioNino || 0).toFixed(2)}`;
        rowNino.style.display = 'flex';
    } else if (rowNino) {
        rowNino.style.display = 'none';
    }

    // Bebé
    const rowBebe = document.getElementById('row-price-bebe');
    const labelBebe = document.getElementById('label-price-bebe');
    const priceBebe = document.getElementById('price-bebe');
    const rowNoBebes = document.getElementById('row-no-bebes');

    if (tour.aceptaBebes) {
        if (rowBebe) {
            const edadMaxB = tour.edadMaxBebe || 2;
            if (labelBebe) labelBebe.textContent = `Bebé (hasta ${edadMaxB} años)`;
            const pBebe = parseFloat(tour.precioBebe || 0);
            if (priceBebe) {
                priceBebe.textContent = pBebe > 0 ? `S/ ${pBebe.toFixed(2)}` : 'Gratis';
            }
            rowBebe.style.display = 'flex';
        }
        if (rowNoBebes) rowNoBebes.style.display = 'none';
    } else {
        if (rowBebe) rowBebe.style.display = 'none';
        if (rowNoBebes) rowNoBebes.style.display = 'flex';
    }

    // 5. Itinerario
    const wrapItinerario = document.getElementById('card-itinerario-wrap');
    const contItinerario = document.getElementById('itinerario-container');
    if (tour.itinerario && tour.itinerario.trim().length > 0 && wrapItinerario && contItinerario) {
        contItinerario.textContent = tour.itinerario.trim();
        wrapItinerario.style.display = 'block';
    } else if (wrapItinerario) {
        wrapItinerario.style.display = 'none';
    }

    // 6. Salidas y cupos disponibles
    renderizarSalidas(tour.fechas || []);
}

function renderizarSalidas(fechas) {
    const cont = document.getElementById('schedules-container');
    const btnReservar = document.getElementById('btn-reservar-main');
    if (!cont) return;

    const salidasValidas = (fechas || []).filter(f => f.cupoDisponible > 0);

    if (salidasValidas.length === 0) {
        cont.innerHTML = `
            <div style="padding:14px; color:#ef4444; font-size:13.5px; background:#fef2f2; border-radius:8px; border:1px solid #fee2e2; text-align:center;">
                <i class="fa-solid fa-calendar-xmark" style="font-size:20px; display:block; margin-bottom:6px;"></i>
                No hay salidas disponibles próximamente para este paquete.
            </div>`;
        if (btnReservar) {
            btnReservar.disabled = true;
            btnReservar.style.opacity = '0.5';
            btnReservar.style.cursor = 'not-allowed';
            btnReservar.title = 'No hay salidas disponibles';
        }
        return;
    }

    if (btnReservar) {
        btnReservar.disabled = false;
        btnReservar.style.opacity = '1';
        btnReservar.style.cursor = 'pointer';
    }

    cont.innerHTML = salidasValidas.map((f, idx) => {
        const fechaFormateada = formatearFecha(f.fecha);
        const horaStr = f.horaInicio || '08:00';
        const cupos = f.cupoDisponible;
        const total = f.cupoTotal;
        const esPocosCupos = cupos <= (currentTour.minAsientosOferta || 5);
        const colorCupos = esPocosCupos ? '#dc2626' : '#15803d';

        return `
            <label class="schedule-radio-label">
                <input type="radio" name="tourSchedule" value="${f.idTourFecha}" data-fecha="${f.fecha}" data-hora="${horaStr}" data-cupo="${cupos}" ${idx === 0 ? 'checked' : ''}>
                <div class="schedule-box-inner">
                    <div>
                        <strong><i class="fa-regular fa-calendar"></i> ${fechaFormateada}</strong>
                        <span style="margin-left: 8px; color:#64748b;"><i class="fa-regular fa-clock"></i> ${horaStr}</span>
                    </div>
                    <span class="schedule-seats-badge" style="font-weight:700; color:${colorCupos};">
                        ${cupos} cupos disponibles
                    </span>
                </div>
            </label>
        `;
    }).join('');
}

async function cargarCalificacionesYComentarios(idTour, idAgencia) {
    const starHeader = document.getElementById('tour-star-icon');
    const valHeader = document.getElementById('tour-rating-val');
    const cntHeader = document.getElementById('tour-reviews-count');

    const starSumm = document.getElementById('summary-star-icon');
    const valSumm = document.getElementById('reviews-summary-score');
    const cntSumm = document.getElementById('reviews-summary-count');
    const contComentarios = document.getElementById('comments-list-container');

    try {
        const resp = await fetch(`/api/calificar?idTour=${idTour}&idAgencia=${idAgencia}`);
        if (!resp.ok) throw new Error('Error al consultar calificaciones');
        const data = await resp.json();

        const total = (data && data.total !== undefined) ? data.total : (currentTour.totalCalificaciones || 0);
        const prom = (data && data.promedio !== undefined) ? parseFloat(data.promedio) : parseFloat(currentTour.calificacionPromedio || 0);
        const lista = (data && Array.isArray(data.calificaciones)) ? data.calificaciones : [];

        if (total > 0 && prom > 0) {
            const promFormatted = prom.toFixed(1);
            if (starHeader) starHeader.style.display = 'inline-block';
            if (valHeader) valHeader.textContent = promFormatted;
            if (cntHeader) cntHeader.textContent = `(${total} opiniones)`;

            if (starSumm) starSumm.style.display = 'inline-block';
            if (valSumm) valSumm.textContent = `${promFormatted} / 5.0`;
            if (cntSumm) cntSumm.textContent = `(${total} opiniones verificadas)`;
        } else {
            if (starHeader) starHeader.style.display = 'none';
            if (valHeader) valHeader.textContent = 'Aún sin calificaciones';
            if (cntHeader) cntHeader.textContent = '';

            if (starSumm) starSumm.style.display = 'none';
            if (valSumm) valSumm.textContent = 'Aún sin calificaciones';
            if (cntSumm) cntSumm.textContent = '';
        }

        if (contComentarios) {
            if (lista.length > 0) {
                contComentarios.innerHTML = lista.map(c => {
                    const stars = '★'.repeat(c.estrellas) + '☆'.repeat(Math.max(0, 5 - c.estrellas));
                    return `
                        <div style="border-bottom: 1px solid #f1f5f9; padding: 12px 0;">
                            <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:4px;">
                                <strong style="color:#0b1f38; font-size:14px;"><i class="fa-solid fa-user-circle" style="color:#64748b;"></i> ${c.turista || 'Viajero Travelink'}</strong>
                                <span style="font-size:12px; color:#94a3b8;">${c.fecha || ''}</span>
                            </div>
                            <div style="color:#f59e0b; font-size:13px; margin-bottom:4px;">${stars}</div>
                            <p style="margin:0; color:#475569; font-size:13.5px; line-height:1.5;">${c.comentario || 'Excelente servicio y puntualidad.'}</p>
                        </div>
                    `;
                }).join('');
            } else {
                contComentarios.innerHTML = `<p style="color:#64748b; font-style:italic; margin:0;">Aún sin comentarios registrados para esta agencia.</p>`;
            }
        }
    } catch (e) {
        console.warn('No se pudieron cargar comentarios:', e);
        if (contComentarios) {
            contComentarios.innerHTML = `<p style="color:#64748b; font-style:italic; margin:0;">Aún sin calificaciones</p>`;
        }
    }
}

// Requerimiento 3: "Reservar ahora" pasa idTour e idTourFecha de la salida elegida a form_reserva (si no eligió salida, se pide elegirla). Corrige el botón.
window.irAReserva = function() {
    if (!currentTour) {
        if (typeof Swal !== 'undefined') {
            Swal.fire({ icon: 'error', title: 'Error', text: 'No se ha cargado la información del paquete.' });
        } else {
            alert('No se ha cargado la información del paquete.');
        }
        return;
    }

    const selectedSchedule = document.querySelector('input[name="tourSchedule"]:checked');
    if (!selectedSchedule) {
        if (typeof Swal !== 'undefined') {
            Swal.fire({
                icon: 'warning',
                title: 'Selecciona una salida',
                text: 'Por favor, selecciona una fecha y horario de salida para continuar con la reserva.',
                confirmButtonColor: '#196f3d'
            });
        } else {
            alert('Por favor, selecciona una fecha y horario de salida para continuar.');
        }
        return;
    }

    const idTourFecha = selectedSchedule.value;
    const fecha = selectedSchedule.getAttribute('data-fecha') || '';
    const hora = selectedSchedule.getAttribute('data-hora') || '';

    const pAdulto = currentTour.precioAdulto || currentTour.precio || 0;
    const pNino = currentTour.precioNino || 0;
    const pBebe = currentTour.precioBebe || 0;

    const url = `form_reserva.html?tour=${currentTour.idTour}&idTourFecha=${idTourFecha}&fecha=${encodeURIComponent(fecha)}&horario=${encodeURIComponent(hora)}&titulo=${encodeURIComponent(currentTour.nombre)}&agencia=${encodeURIComponent(currentTour.agencia || '')}&precioAdulto=${pAdulto}&precioNino=${pNino}&precioBebe=${pBebe}`;
    window.location.href = url;
};

