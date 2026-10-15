// Travelink - Form Reserva Controller & Stepper Logic (FASE 7)

let tourInfo = {
    idTour: 1,
    idTourFecha: 1,
    titulo: 'Machu Picchu Clásico',
    agencia: 'Andes Tours',
    fecha: '2026-10-25',
    horario: '08:00 AM',
    aceptaNinos: true,
    edadMaxNino: 11,
    aceptaBebes: false,
    edadMaxBebe: 2,
    descuento: 0,
    cupoDisponible: 20
};

let precios = {
    adultos: 350.00,
    ninos: 250.00,
    bebes: 0.00
};

let metodoPagoActual = 'efectivo';

$(document).ready(function() {
    if (typeof toastr !== 'undefined') {
        toastr.options = {
            positionClass: 'toast-top-right',
            timeOut: 3000,
            closeButton: true,
            progressBar: true
        };
    }

    inicializarDatosReserva();

    // Formateador de número de tarjeta
    const inputNum = document.getElementById('numTarjeta');
    if (inputNum) {
        inputNum.addEventListener('input', function() {
            let val = this.value.replace(/\D/g, '').slice(0, 16);
            let formatted = val.match(/.{1,4}/g) ? val.match(/.{1,4}/g).join(' ') : '';
            this.value = formatted;
            
            const prev = document.getElementById('previewNum');
            if (prev) prev.innerText = formatted || '•••• •••• •••• ••••';

            // Detectar marca de tarjeta
            detectarMarcaTarjeta(val);
        });
    }

    // Formateador de nombre del titular
    const inputTitular = document.getElementById('nombreTitular');
    if (inputTitular) {
        inputTitular.addEventListener('input', function() {
            this.value = this.value.replace(/[^a-zA-ZáéíóúÁÉÍÓÚñÑ\s]/g, '').slice(0, 40);
            const prev = document.getElementById('previewNombre');
            if (prev) prev.innerText = this.value.toUpperCase() || 'NOMBRE APELLIDO';
        });
    }

    // Formateador de vencimiento (MM/AAAA)
    const inputVenc = document.getElementById('vencimiento');
    if (inputVenc) {
        inputVenc.addEventListener('input', function() {
            let val = this.value.replace(/\D/g, '').slice(0, 6);
            if (val.length >= 2) {
                let mes = parseInt(val.slice(0, 2), 10);
                if (mes > 12) mes = 12;
                if (mes === 0) mes = 1;
                let mesStr = String(mes).padStart(2, '0');
                val = mesStr + val.slice(2);
            }
            let formatted = val.length > 2 ? val.slice(0, 2) + '/' + val.slice(2) : val;
            this.value = formatted;
            
            const prev = document.getElementById('previewVence');
            if (prev) prev.innerText = formatted || 'MM/AAAA';
        });
    }
});

function detectarMarcaTarjeta(digits) {
    let brand = 'Visa';
    let iconClass = 'fa-brands fa-cc-visa';
    if (digits.startsWith('5') || digits.startsWith('2')) {
        brand = 'Mastercard';
        iconClass = 'fa-brands fa-cc-mastercard';
    } else if (digits.startsWith('3')) {
        brand = 'Amex';
        iconClass = 'fa-brands fa-cc-amex';
    }

    const prevBrand = document.getElementById('previewBrand');
    if (prevBrand) prevBrand.innerHTML = `<i class="${iconClass}" style="font-size:28px;"></i>`;

    const rCardIcon = document.getElementById('rCardIcon');
    if (rCardIcon) rCardIcon.className = iconClass;
}

function inicializarDatosReserva() {
    const urlParams = new URLSearchParams(window.location.search);
    const tourParam = urlParams.get('tour') || urlParams.get('idTour') || urlParams.get('idPaquete') || '1';
    const idTourFechaParam = urlParams.get('idTourFecha') || urlParams.get('salida') || tourParam;

    tourInfo.idTour = parseInt(tourParam) || 1;
    tourInfo.idTourFecha = parseInt(idTourFechaParam) || 1;

    if (urlParams.get('titulo')) tourInfo.titulo = decodeURIComponent(urlParams.get('titulo'));
    if (urlParams.get('agencia')) tourInfo.agencia = decodeURIComponent(urlParams.get('agencia'));
    if (urlParams.get('fecha')) tourInfo.fecha = decodeURIComponent(urlParams.get('fecha'));
    if (urlParams.get('horario')) tourInfo.horario = decodeURIComponent(urlParams.get('horario'));

    // Consultar información real del tour desde la API para asegurar precios y restricciones
    fetch(`/api/tours?id=${tourInfo.idTour}`)
        .then(res => res.json())
        .then(data => {
            const tour = data.tour || (data.tours && data.tours[0]) || data;
            if (tour && (tour.idTour || tour.id)) {
                if (tour.nombre) tourInfo.titulo = tour.nombre;
                if (tour.agenciaNombre) tourInfo.agencia = tour.agenciaNombre;
                if (tour.precioAdulto) precios.adultos = parseFloat(tour.precioAdulto);
                if (tour.precioNino != null) precios.ninos = parseFloat(tour.precioNino);
                if (tour.precioBebe != null) precios.bebes = parseFloat(tour.precioBebe);

                tourInfo.aceptaNinos = tour.aceptaNinos !== false && tour.aceptaNinos !== 0;
                tourInfo.edadMaxNino = tour.edadMaxNino != null ? parseInt(tour.edadMaxNino) : 11;
                tourInfo.aceptaBebes = tour.aceptaBebes === true || tour.aceptaBebes === 1;
                tourInfo.edadMaxBebe = tour.edadMaxBebe != null ? parseInt(tour.edadMaxBebe) : 2;
                tourInfo.descuento = tour.descuento != null ? parseFloat(tour.descuento) : 0;
            }
        })
        .catch(err => {
            console.warn('Usando valores iniciales:', err);
        })
        .finally(() => {
            aplicarConfiguracionTour();
            actualizarTotales();
        });
}

function aplicarConfiguracionTour() {
    // 1. Título y Agencia en Paso 1
    const elTituloP1 = document.querySelector('.tour-cell-text h4');
    if (elTituloP1) elTituloP1.textContent = tourInfo.titulo;

    const elAgenciaP1 = document.querySelector('.tour-cell-text p');
    if (elAgenciaP1) elAgenciaP1.textContent = tourInfo.agencia;

    const elFechaP1 = document.querySelector('.tour-table-body .cell-val');
    if (elFechaP1) elFechaP1.textContent = `${tourInfo.fecha} - ${tourInfo.horario}`;

    // 2. Control de Niños
    const labelNinos = document.getElementById('labelNinos');
    const inputNinos = document.getElementById('cantNinos');
    const avisoNinos = document.getElementById('avisoNinos');
    const btnNinoUp = document.getElementById('btnNinoUp');
    const btnNinoDown = document.getElementById('btnNinoDown');

    if (labelNinos) labelNinos.textContent = `Niños (hasta ${tourInfo.edadMaxNino} años) - S/ ${precios.ninos.toFixed(2)}`;

    if (!tourInfo.aceptaNinos) {
        if (inputNinos) { inputNinos.value = 0; inputNinos.disabled = true; }
        if (btnNinoUp) btnNinoUp.disabled = true;
        if (btnNinoDown) btnNinoDown.disabled = true;
        if (avisoNinos) avisoNinos.style.display = 'block';
    } else {
        if (inputNinos) inputNinos.disabled = false;
        if (btnNinoUp) btnNinoUp.disabled = false;
        if (btnNinoDown) btnNinoDown.disabled = false;
        if (avisoNinos) avisoNinos.style.display = 'none';
    }

    // 3. Control de Bebés
    const labelBebes = document.getElementById('labelBebes');
    const inputBebes = document.getElementById('cantBebes');
    const avisoBebes = document.getElementById('avisoBebes');
    const btnBebeUp = document.getElementById('btnBebeUp');
    const btnBebeDown = document.getElementById('btnBebeDown');

    const textoBebePrecio = precios.bebes > 0 ? `S/ ${precios.bebes.toFixed(2)}` : 'Gratis';
    if (labelBebes) labelBebes.textContent = `Bebés (hasta ${tourInfo.edadMaxBebe} años) - ${textoBebePrecio}`;

    if (!tourInfo.aceptaBebes) {
        if (inputBebes) { inputBebes.value = 0; inputBebes.disabled = true; }
        if (btnBebeUp) btnBebeUp.disabled = true;
        if (btnBebeDown) btnBebeDown.disabled = true;
        if (avisoBebes) avisoBebes.style.display = 'block';
    } else {
        if (inputBebes) inputBebes.disabled = false;
        if (btnBebeUp) btnBebeUp.disabled = false;
        if (btnBebeDown) btnBebeDown.disabled = false;
        if (avisoBebes) avisoBebes.style.display = 'none';
    }
}

function cambiarCantidad(tipo, delta) {
    let input = null;
    if (tipo === 'adultos') input = document.getElementById('cantAdultos');
    else if (tipo === 'ninos') {
        if (!tourInfo.aceptaNinos) return;
        input = document.getElementById('cantNinos');
    } else if (tipo === 'bebes') {
        if (!tourInfo.aceptaBebes) return;
        input = document.getElementById('cantBebes');
    }
    
    if (input) {
        let val = (parseInt(input.value) || 0) + delta;
        // Adultos mínimo 1
        if (tipo === 'adultos' && val < 1) val = 1;
        // Niños y bebés mínimo 0
        if (tipo !== 'adultos' && val < 0) val = 0;
        
        input.value = val;
        actualizarTotales();
    }
}

function generarTextoDesglose(cantA, cantN, cantB) {
    const partes = [];
    if (cantA > 0) {
        partes.push(`${cantA} ${cantA === 1 ? 'adulto' : 'adultos'} × S/ ${precios.adultos.toFixed(2)}`);
    }
    if (cantN > 0) {
        partes.push(`${cantN} ${cantN === 1 ? 'niño' : 'niños'} × S/ ${precios.ninos.toFixed(2)}`);
    }
    if (cantB > 0) {
        if (precios.bebes === 0) {
            partes.push(`${cantB} ${cantB === 1 ? 'bebé (gratis)' : 'bebés (gratis)'}`);
        } else {
            partes.push(`${cantB} ${cantB === 1 ? 'bebé' : 'bebés'} × S/ ${precios.bebes.toFixed(2)}`);
        }
    }
    return partes.join(' + ') || '1 adulto';
}

function actualizarTotales() {
    const elA = document.getElementById('cantAdultos');
    const elN = document.getElementById('cantNinos');
    const elB = document.getElementById('cantBebes');
    
    const cantA = elA ? Math.max(1, parseInt(elA.value) || 1) : 1;
    const cantN = elN ? (tourInfo.aceptaNinos ? parseInt(elN.value) || 0 : 0) : 0;
    const cantB = elB ? (tourInfo.aceptaBebes ? parseInt(elB.value) || 0 : 0) : 0;
    
    const totalPersonas = cantA + cantN + cantB;
    const subtotal = (cantA * precios.adultos) + (cantN * precios.ninos) + (cantB * precios.bebes);
    const descuentoMonto = subtotal * (tourInfo.descuento / 100.0);
    const totalCosto = Math.max(0, subtotal - descuentoMonto);
    
    const elPrice = document.getElementById('precioTotalItem');
    if (elPrice) elPrice.textContent = 'S/ ' + totalCosto.toFixed(2);
    
    const elResumenCell = document.getElementById('cellPersonasResumen');
    if (elResumenCell) {
        elResumenCell.innerHTML = `Adultos: ${cantA}<br>Niños: ${cantN}<br>Bebés: ${cantB}`;
    }

    // Actualizar paso 4 resumen
    const strPersonas = totalPersonas === 1 ? '1 persona' : totalPersonas + ' personas';
    const elRPer = document.getElementById('rTotalPersonas');
    const elRPre = document.getElementById('rTotalPrecio');
    const elSumTotalFinal = document.getElementById('sumTotalFinal');
    const elDesgTexto = document.getElementById('rDesgloseTexto');

    if (elRPer) elRPer.textContent = strPersonas;
    if (elRPre) elRPre.textContent = 'S/ ' + totalCosto.toFixed(2);
    if (elSumTotalFinal) elSumTotalFinal.textContent = 'S/ ' + totalCosto.toFixed(2);
    if (elDesgTexto) elDesgTexto.textContent = generarTextoDesglose(cantA, cantN, cantB);

    // Descuento en Paso 4
    const elDescBox = document.getElementById('rDescuentoAplicado');
    const elDescPct = document.getElementById('rDescuentoPct');
    if (tourInfo.descuento > 0 && elDescBox && elDescPct) {
        elDescBox.style.display = 'block';
        elDescPct.textContent = `${tourInfo.descuento}% (Ahorro de S/ ${descuentoMonto.toFixed(2)})`;
    } else if (elDescBox) {
        elDescBox.style.display = 'none';
    }

    // Actualizar adelanto 15% para efectivo
    const monto15 = (totalCosto * 0.15).toFixed(2);
    const saldo85 = (totalCosto * 0.85).toFixed(2);
    const elAdelanto = document.getElementById('rMontoAdelanto');
    const elSaldo = document.getElementById('rSaldoRestante');
    if (elAdelanto) elAdelanto.textContent = 'S/ ' + monto15;
    if (elSaldo) elSaldo.textContent = 'S/ ' + saldo85;

    const elAdelantoPresencial = document.getElementById('montoAdelanto15');
    const elSaldoPresencial = document.getElementById('saldoRestante85');
    if (elAdelantoPresencial) elAdelantoPresencial.textContent = 'S/ ' + monto15;
    if (elSaldoPresencial) elSaldoPresencial.textContent = 'S/ ' + saldo85;
}

function renderizarFormularioPasajeros() {
    const cont = document.getElementById('contenedorPasajeros');
    if (!cont) return;

    const cantA = Math.max(1, parseInt(document.getElementById('cantAdultos')?.value || '1'));
    const cantN = tourInfo.aceptaNinos ? parseInt(document.getElementById('cantNinos')?.value || '0') : 0;
    const cantB = tourInfo.aceptaBebes ? parseInt(document.getElementById('cantBebes')?.value || '0') : 0;

    const user = window.getCurrentUser ? window.getCurrentUser() : null;
    let html = '';

    // Pasajero 1: Titular
    const nombreTitular = user && user.nombre ? user.nombre : '';
    const apellidosTitular = user && user.apellidos ? user.apellidos : '';
    const dniTitular = user && user.nroDocumento ? user.nroDocumento : '';
    const telTitular = user && user.telefono ? user.telefono : '';

    html += `
        <div class="passenger-card-box" data-tipo="ADULTO" data-index="1">
            <div class="passenger-card-header">
                <h3>Pasajero 1 <span class="badge-titular">(Titular de la sesión)</span></h3>
                <span style="font-size:12px; color:#196f3d; font-weight:600;"><i class="fa-solid fa-user-check"></i> Adulto</span>
            </div>
            <div class="passenger-grid-3">
                <div class="input-field-block">
                    <label>Nombres *</label>
                    <input type="text" id="pNombre_1" value="${nombreTitular}" placeholder="Nombres del titular">
                </div>
                <div class="input-field-block">
                    <label>Apellidos *</label>
                    <input type="text" id="pApellidos_1" value="${apellidosTitular}" placeholder="Apellidos completos">
                </div>
                <div class="input-field-block">
                    <label>DNI / Documento *</label>
                    <input type="text" id="pDoc_1" value="${dniTitular}" maxlength="12" placeholder="Nro de documento">
                </div>
                <div class="input-field-block">
                    <label>Teléfono *</label>
                    <input type="tel" id="pTel_1" value="${telTitular}" maxlength="12" placeholder="Nro de teléfono">
                </div>
                <div class="input-field-block">
                    <label>Edad (Años)</label>
                    <input type="number" id="pEdad_1" value="30" min="18" max="100">
                </div>
            </div>
        </div>
    `;

    let pIndex = 2;

    // Acompañantes adultos
    for (let i = 2; i <= cantA; i++) {
        html += `
            <div class="passenger-card-box" data-tipo="ADULTO" data-index="${pIndex}">
                <div class="passenger-card-header">
                    <h3>Pasajero ${pIndex} <span style="font-size:12px; color:#475569; font-weight:600;">(Adulto)</span></h3>
                </div>
                <div class="passenger-grid-3">
                    <div class="input-field-block">
                        <label>Nombres *</label>
                        <input type="text" id="pNombre_${pIndex}" placeholder="Nombres del adulto">
                    </div>
                    <div class="input-field-block">
                        <label>Apellidos *</label>
                        <input type="text" id="pApellidos_${pIndex}" placeholder="Apellidos del adulto">
                    </div>
                    <div class="input-field-block">
                        <label>DNI / Documento *</label>
                        <input type="text" id="pDoc_${pIndex}" maxlength="12" placeholder="Nro de documento">
                    </div>
                    <div class="input-field-block">
                        <label>Edad (Años)</label>
                        <input type="number" id="pEdad_${pIndex}" value="28" min="18" max="100">
                    </div>
                </div>
            </div>
        `;
        pIndex++;
    }

    // Niños
    for (let i = 1; i <= cantN; i++) {
        html += `
            <div class="passenger-card-box" data-tipo="NINO" data-index="${pIndex}" style="border-left: 4px solid #0284c7;">
                <div class="passenger-card-header">
                    <h3>Pasajero ${pIndex} <span style="font-size:12px; color:#0284c7; font-weight:600;">(Niño - Máx. ${tourInfo.edadMaxNino} años)</span></h3>
                </div>
                <div class="passenger-grid-3">
                    <div class="input-field-block">
                        <label>Nombres *</label>
                        <input type="text" id="pNombre_${pIndex}" placeholder="Nombres del niño(a)">
                    </div>
                    <div class="input-field-block">
                        <label>Apellidos *</label>
                        <input type="text" id="pApellidos_${pIndex}" placeholder="Apellidos del niño(a)">
                    </div>
                    <div class="input-field-block">
                        <label>DNI / Documento</label>
                        <input type="text" id="pDoc_${pIndex}" maxlength="12" placeholder="DNI">
                    </div>
                    <div class="input-field-block">
                        <label>Edad (Máx. ${tourInfo.edadMaxNino} años) *</label>
                        <input type="number" id="pEdad_${pIndex}" value="${Math.min(8, tourInfo.edadMaxNino)}" min="3" max="${tourInfo.edadMaxNino}">
                    </div>
                </div>
            </div>
        `;
        pIndex++;
    }

    // Bebés
    for (let i = 1; i <= cantB; i++) {
        html += `
            <div class="passenger-card-box" data-tipo="BEBE" data-index="${pIndex}" style="border-left: 4px solid #f59e0b;">
                <div class="passenger-card-header">
                    <h3>Pasajero ${pIndex} <span style="font-size:12px; color:#d97706; font-weight:600;">(Bebé - Máx. ${tourInfo.edadMaxBebe} años)</span></h3>
                </div>
                <div class="passenger-grid-3">
                    <div class="input-field-block">
                        <label>Nombres *</label>
                        <input type="text" id="pNombre_${pIndex}" placeholder="Nombres del bebé">
                    </div>
                    <div class="input-field-block">
                        <label>Apellidos *</label>
                        <input type="text" id="pApellidos_${pIndex}" placeholder="Apellidos del bebé">
                    </div>
                    <div class="input-field-block">
                        <label>DNI / Documento</label>
                        <input type="text" id="pDoc_${pIndex}" maxlength="12" placeholder="DNI">
                    </div>
                    <div class="input-field-block">
                        <label>Edad (Máx. ${tourInfo.edadMaxBebe} años) *</label>
                        <input type="number" id="pEdad_${pIndex}" value="${Math.min(1, tourInfo.edadMaxBebe)}" min="0" max="${tourInfo.edadMaxBebe}">
                    </div>
                </div>
            </div>
        `;
        pIndex++;
    }

    cont.innerHTML = html;
}

function validarPasoDatos() {
    const pBoxes = document.querySelectorAll('#contenedorPasajeros .passenger-card-box');
    for (const box of pBoxes) {
        const idx = box.getAttribute('data-index');
        const tipo = box.getAttribute('data-tipo');

        const nom = document.getElementById(`pNombre_${idx}`)?.value.trim();
        const ape = document.getElementById(`pApellidos_${idx}`)?.value.trim();
        const doc = document.getElementById(`pDoc_${idx}`)?.value.trim();
        const edad = parseInt(document.getElementById(`pEdad_${idx}`)?.value || '0');

        if (!nom) {
            alert(`Por favor ingresa los nombres del Pasajero ${idx}.`);
            return false;
        }
        if (!ape) {
            alert(`Por favor ingresa los apellidos del Pasajero ${idx}.`);
            return false;
        }
        if (idx === '1' && !doc) {
            alert(`El documento del titular es obligatorio.`);
            return false;
        }

        // Validación de edad según tipo
        if (tipo === 'NINO' && edad > tourInfo.edadMaxNino) {
            alert(`La edad del niño (Pasajero ${idx}) no puede ser mayor a ${tourInfo.edadMaxNino} años.`);
            return false;
        }
        if (tipo === 'BEBE' && edad > tourInfo.edadMaxBebe) {
            alert(`La edad del bebé (Pasajero ${idx}) no puede ser mayor a ${tourInfo.edadMaxBebe} años.`);
            return false;
        }
    }

    mostrarPaso(3);
    return true;
}

function mostrarPaso(n) {
    if (n === 2) {
        renderizarFormularioPasajeros();
    } else if (n === 4) {
        // Actualizar Resumen en Paso 4
        actualizarTotales();

        const p1Nom = document.getElementById('pNombre_1')?.value.trim() || 'Titular';
        const p1Ape = document.getElementById('pApellidos_1')?.value.trim() || '';
        const p1Doc = document.getElementById('pDoc_1')?.value.trim() || '12345678';

        const elTit = document.getElementById('rTitularNombre');
        if (elTit) elTit.textContent = `${p1Nom} ${p1Ape}`.trim();

        const elDni = document.getElementById('rTitularDNI');
        if (elDni) elDni.textContent = p1Doc;

        // Renderizar lista en resumen de pasajeros
        const resListCont = document.getElementById('rResumenPasajerosLista');
        if (resListCont) {
            let resHtml = `
                <div class="passenger-summary-item">
                    <h5>Pasajero 1 <span class="badge-titular">(Titular)</span></h5>
                    <p class="p-name">${p1Nom} ${p1Ape}</p>
                    <p class="p-info">DNI: ${p1Doc}</p>
                </div>
            `;
            const pBoxes = document.querySelectorAll('#contenedorPasajeros .passenger-card-box');
            pBoxes.forEach(box => {
                const idx = box.getAttribute('data-index');
                if (idx === '1') return;
                const tipo = box.getAttribute('data-tipo');
                const nom = document.getElementById(`pNombre_${idx}`)?.value.trim() || `Pasajero ${idx}`;
                const ape = document.getElementById(`pApellidos_${idx}`)?.value.trim() || '';
                const doc = document.getElementById(`pDoc_${idx}`)?.value.trim() || '-';
                const edad = document.getElementById(`pEdad_${idx}`)?.value.trim() || '';

                resHtml += `
                    <div class="passenger-summary-item" style="margin-top:10px; padding-top:8px; border-top:1px dashed #e2e8f0;">
                        <h5>Pasajero ${idx} <span style="font-size:11px; color:#64748b;">(${tipo})</span></h5>
                        <p class="p-name">${nom} ${ape}</p>
                        <p class="p-info">${doc !== '-' ? 'Doc: ' + doc + ' | ' : ''}${edad ? edad + ' años' : ''}</p>
                    </div>
                `;
            });
            resListCont.innerHTML = resHtml;
        }

        // Actualizar datos de tarjeta en resumen
        if (metodoPagoActual === 'tarjeta') {
            const numT = document.getElementById('numTarjeta')?.value.replace(/\s/g, '') || '4587';
            const ult4 = numT.length >= 4 ? numT.slice(-4) : '4587';
            const maskedEl = document.getElementById('rCardMasked');
            if (maskedEl) maskedEl.textContent = `**** **** **** ${ult4}`;
            const cardDisplay = document.getElementById('rCardDisplay');
            if (cardDisplay) cardDisplay.style.display = 'flex';
            const pagoEfInfo = document.getElementById('rPagoEfectivoInfo');
            if (pagoEfInfo) pagoEfInfo.style.display = 'none';
        } else if (metodoPagoActual === 'efectivo') {
            const cardDisplay = document.getElementById('rCardDisplay');
            if (cardDisplay) cardDisplay.style.display = 'none';
            const pagoEfInfo = document.getElementById('rPagoEfectivoInfo');
            if (pagoEfInfo) pagoEfInfo.style.display = 'block';
        } else {
            const cardDisplay = document.getElementById('rCardDisplay');
            if (cardDisplay) cardDisplay.style.display = 'none';
            const pagoEfInfo = document.getElementById('rPagoEfectivoInfo');
            if (pagoEfInfo) pagoEfInfo.style.display = 'none';
        }
    }

    document.querySelectorAll('.paso').forEach(p => p.classList.remove('active'));
    const elPaso = document.getElementById('paso' + n);
    if (elPaso) elPaso.classList.add('active');

    // Stepper nodes
    document.querySelectorAll('.step-node, .step').forEach(node => {
        node.classList.remove('active', 'completed');
        const stepNum = parseInt(node.getAttribute('data-step'), 10);
        if (stepNum < n) node.classList.add('completed');
        else if (stepNum === n) node.classList.add('active');
    });

    const progressTrack = document.getElementById('stepperTrackProgress');
    if (progressTrack) {
        progressTrack.style.width = (((n - 1) / 3) * 100) + '%';
    }

    window.scrollTo({ top: 0, behavior: 'smooth' });
}

function seleccionarPago(element, tipo) {
    metodoPagoActual = tipo;

    document.querySelectorAll('.tarjeta-pago').forEach(t => t.classList.remove('activa'));
    if (element) element.classList.add('activa');

    document.querySelectorAll('.detalle-pago').forEach(d => {
        d.classList.remove('visible');
        d.style.display = 'none';
    });

    const divId = 'pago' + tipo.charAt(0).toUpperCase() + tipo.slice(1);
    const div = document.getElementById(divId);
    if (div) {
        div.style.display = 'block';
        setTimeout(() => div.classList.add('visible'), 10);
    }

    const nombresMetodo = {
        'efectivo': 'Presencial (Abono 15%)',
        'yape': 'Yape',
        'plin': 'Plin',
        'tarjeta': 'Tarjeta de crédito / débito'
    };
    const elRMet = document.getElementById('rMetodoPago');
    if (elRMet) elRMet.textContent = nombresMetodo[tipo] || 'Presencial';

    actualizarTotales();
}

function validarPasoPago() {
    if (metodoPagoActual === 'tarjeta') {
        const numTarjeta = document.getElementById('numTarjeta')?.value.replace(/\s/g, '') || '';
        if (numTarjeta.length < 16) {
            alert('Por favor ingresa un número de tarjeta válido de 16 dígitos.');
            return false;
        }

        const titular = document.getElementById('nombreTitular')?.value.trim() || '';
        if (!titular) {
            alert('Por favor ingresa el nombre del titular de la tarjeta.');
            return false;
        }

        const vencVal = document.getElementById('vencimiento')?.value.trim() || '';
        const parts = vencVal.split('/');
        if (parts.length !== 2 || parts[0].length !== 2 || parts[1].length !== 4) {
            alert('Ingresa la fecha de vencimiento en formato MM/AAAA.');
            return false;
        }

        const mes = parseInt(parts[0], 10);
        const anio = parseInt(parts[1], 10);
        if (isNaN(mes) || mes < 1 || mes > 12) {
            alert('El mes de vencimiento debe estar entre 01 y 12.');
            return false;
        }
        if (isNaN(anio) || anio < 2026) {
            alert('La tarjeta se encuentra vencida (año debe ser >= 2026).');
            return false;
        }
    }

    mostrarPaso(4);
    return true;
}

function mostrarModal() {
    const modal = document.getElementById('modalConfirmar');
    if (modal) modal.style.display = 'flex';
}

function cerrarModal() {
    const modal = document.getElementById('modalConfirmar');
    if (modal) modal.style.display = 'none';
}

function confirmarReserva() {
    cerrarModal();

    const cantA = Math.max(1, parseInt(document.getElementById('cantAdultos')?.value || '1'));
    const cantN = tourInfo.aceptaNinos ? parseInt(document.getElementById('cantNinos')?.value || '0') : 0;
    const cantB = tourInfo.aceptaBebes ? parseInt(document.getElementById('cantBebes')?.value || '0') : 0;

    const user = window.getCurrentUser ? window.getCurrentUser() : null;

    // Métodos de pago
    const nombresMetodo = {
        'efectivo': 'Efectivo',
        'yape': 'Yape',
        'plin': 'Plin',
        'tarjeta': 'Tarjeta'
    };
    const tipoPagoNombre = nombresMetodo[metodoPagoActual] || 'Tarjeta';

    // Recopilar lista de pasajeros
    const listaPasajeros = [];
    const pBoxes = document.querySelectorAll('#contenedorPasajeros .passenger-card-box');
    pBoxes.forEach(box => {
        const idx = box.getAttribute('data-index');
        const tipo = box.getAttribute('data-tipo');
        const esTitular = idx === '1';
        const nom = document.getElementById(`pNombre_${idx}`)?.value.trim() || (esTitular ? (user?.nombre || 'Titular') : `Pasajero ${idx}`);
        const ape = document.getElementById(`pApellidos_${idx}`)?.value.trim() || (user?.apellidos || 'Travelink');
        const doc = document.getElementById(`pDoc_${idx}`)?.value.trim() || (user?.nroDocumento || '12345678');
        const tel = document.getElementById(`pTel_${idx}`)?.value.trim() || (user?.telefono || '');
        const edad = parseInt(document.getElementById(`pEdad_${idx}`)?.value || (tipo === 'ADULTO' ? '30' : (tipo === 'NINO' ? '8' : '1')));

        listaPasajeros.push({
            esTitular: esTitular,
            tipo: tipo,
            nombre: nom,
            apellidos: ape,
            nroDocumento: doc,
            telefono: tel,
            edad: edad
        });
    });

    // SEGURIDAD: Nunca enviar CVV ni número completo de tarjeta
    let ult4 = '4587';
    let marca = 'Visa';
    if (metodoPagoActual === 'tarjeta') {
        const rawNum = document.getElementById('numTarjeta')?.value.replace(/\D/g, '') || '';
        if (rawNum.length >= 4) {
            ult4 = rawNum.slice(-4);
        }
        if (rawNum.startsWith('5') || rawNum.startsWith('2')) marca = 'Mastercard';
        else if (rawNum.startsWith('3')) marca = 'Amex';
    }

    // Limpiar input de CVV por seguridad
    const cvvEl = document.getElementById('cvv');
    if (cvvEl) cvvEl.value = '';

    const payload = {
        idTour: tourInfo.idTour,
        idTourFecha: tourInfo.idTourFecha,
        cantAdultos: cantA,
        cantNinos: cantN,
        cantBebes: cantB,
        metodoPago: tipoPagoNombre,
        marcaTarjeta: marca,
        ultimosDigitos: ult4,
        pasajeros: listaPasajeros
    };

    if (user && user.idUsuario) {
        payload.idUsuario = user.idUsuario;
        payload.email = user.email;
    }

    // Mostrar loader o toast
    if (typeof toastr !== 'undefined') toastr.info('Procesando reserva con disponibilidad en tiempo real...');

    fetch(`${API_BASE}/api/guardar_reserva`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    })
    .then(async res => {
        const data = await res.json();
        if (!res.ok || data.status !== 'success') {
            throw new Error(data.message || data.error || 'Error al procesar reserva');
        }
        return data;
    })
    .then(data => {
        const d = data.data || {};
        const cod = d.codigo || ('TRK-' + Math.floor(100 + Math.random() * 900));

        // Guardar en caché local para visualización offline
        guardarReservaEnStorage(cantA, cantN, cantB, d.total || 700, tipoPagoNombre, cod, d.desglose);

        if (typeof Swal !== 'undefined') {
            Swal.fire({
                title: '¡Reserva Confirmada!',
                html: `<p>Tu reserva <strong>${cod}</strong> ha sido procesada con éxito.</p>
                       <p style="font-size:13px; color:#475569; margin-top:8px;">${d.desglose || ''}</p>
                       <p style="font-size:14px; color:#196f3d; font-weight:bold; margin-top:10px;">Total: S/ ${Number(d.total || 0).toFixed(2)}</p>`,
                icon: 'success',
                confirmButtonColor: '#196f3d',
                confirmButtonText: 'Ver mis reservas'
            }).then(() => {
                window.location.href = "reservas.html";
            });
        } else {
            alert('¡Reserva confirmada exitosamente! Código: ' + cod);
            window.location.href = "reservas.html";
        }
    })
    .catch(err => {
        console.error('Error al guardar reserva:', err);
        if (typeof Swal !== 'undefined') {
            Swal.fire({
                title: 'No se pudo completar la reserva',
                text: err.message,
                icon: 'error',
                confirmButtonColor: '#0b1f38'
            });
        } else {
            alert('Error al reservar: ' + err.message);
        }
    });
}

function guardarReservaEnStorage(cantA, cantN, cantB, totalCosto, metodo, codigoPersonalizado, desglose) {
    const user = window.getCurrentUser ? window.getCurrentUser() : { email: 'turista@travelink.pe' };
    const key = 'travelink_reservas_' + (user.email || 'turista@travelink.pe');

    let list = [];
    try {
        list = JSON.parse(localStorage.getItem(key) || '[]');
    } catch(e) { list = []; }

    const totalP = cantA + cantN + cantB;
    const desgloseFinal = desglose || generarTextoDesglose(cantA, cantN, cantB);

    const nueva = {
        id: list.length + 1,
        codigo: codigoPersonalizado,
        titulo: tourInfo.titulo,
        ubicacion: 'Cusco, Perú',
        fechas: `${tourInfo.fecha} - ${tourInfo.horario}`,
        fechaSalida: tourInfo.fecha,
        horaSalida: tourInfo.horario,
        personas: totalP + (totalP === 1 ? ' persona' : ' personas'),
        cantAdultos: cantA,
        cantNinos: cantN,
        cantBebes: cantB,
        desglose: desgloseFinal,
        agencia: tourInfo.agencia,
        estado: metodo === 'Efectivo' ? 'Pendiente' : 'Confirmada',
        total: totalCosto,
        metodoPago: metodo,
        imagen: '../../img/valle.jpg',
        fechaRegistro: new Date().toISOString().split('T')[0]
    };
    list.unshift(nueva);
    localStorage.setItem(key, JSON.stringify(list));
    return nueva;
}
