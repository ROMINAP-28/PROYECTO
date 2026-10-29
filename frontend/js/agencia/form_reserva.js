// Travelink - Form Reserva Controller & Stepper Logic

$(document).ready(function() {
    if (typeof toastr !== 'undefined') {
        toastr.options = {
            positionClass: 'toast-top-right',
            timeOut: 3000,
            closeButton: true,
            progressBar: true
        };
    }

    // Formateador de número de tarjeta (Espacios cada 4 dígitos: 1234 5678 9012 3456)
    const inputNum = document.getElementById('numTarjeta');
    if (inputNum) {
        inputNum.addEventListener('input', function() {
            let val = this.value.replace(/\D/g, '').slice(0, 16);
            let formatted = val.match(/.{1,4}/g) ? val.match(/.{1,4}/g).join(' ') : '';
            this.value = formatted;
            
            const prev = document.getElementById('previewNum');
            if (prev) prev.innerText = formatted || '•••• •••• •••• ••••';
        });
    }

    // Formateador de nombre del titular (Máx 40 caracteres con espacios, solo letras)
    const inputTitular = document.getElementById('nombreTitular');
    if (inputTitular) {
        inputTitular.addEventListener('input', function() {
            this.value = this.value.replace(/[^a-zA-ZáéíóúÁÉÍÓÚñÑ\s]/g, '').slice(0, 40);
            const prev = document.getElementById('previewNombre');
            if (prev) prev.innerText = this.value.toUpperCase() || 'NOMBRE APELLIDO';
        });
    }

    // Formateador de vencimiento (MM/AAAA con barra automática, mes 1-12)
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

let precios = { adultos: 120, ninos: 80, bebes: 0 };
let travelinkSelectedTour = null;
let travelinkSelectedDate = null;

function actualizarTotales() {
    let elA = document.getElementById('cantAdultos');
    let elN = document.getElementById('cantNinos');
    let elB = document.getElementById('cantBebes');
    
    let cantA = elA ? parseInt(elA.value) || 0 : 1;
    let cantN = elN ? parseInt(elN.value) || 0 : 0;
    let cantB = elB ? parseInt(elB.value) || 0 : 0;
    
    let totalPersonas = cantA + cantN + cantB;
    let totalCosto = (cantA * precios.adultos) + (cantN * precios.ninos) + (cantB * precios.bebes);
    
    const elPrice = document.getElementById('precioTotalItem');
    if (elPrice) elPrice.textContent = 'S/ ' + totalCosto.toFixed(2);
    
    const elResumenCell = document.getElementById('cellPersonasResumen');
    if (elResumenCell) {
        elResumenCell.innerHTML = `Adultos: ${cantA}<br>Niños: ${cantN}<br>Bebés: ${cantB}`;
    }

    // Actualizar paso 4 resumen
    let strPersonas = totalPersonas === 1 ? '1 persona' : totalPersonas + ' personas';
    const elRPer = document.getElementById('rTotalPersonas');
    const elRPre = document.getElementById('rTotalPrecio');
    const elSumTotalFinal = document.getElementById('sumTotalFinal');
    if (elRPer) elRPer.textContent = (cantA + cantN + cantB) + ((cantA + cantN + cantB) === 1 ? ' persona' : ' personas');
    if (elRPre) elRPre.textContent = 'S/ ' + totalCosto.toFixed(2);
    if (elSumTotalFinal) elSumTotalFinal.textContent = 'S/ ' + totalCosto.toFixed(2);
    renderPassengerForms();
    actualizarResumenPasajeros();
}

function cambiarCantidad(tipo, delta) {
    let input = null;
    if (tipo === 'adultos') input = document.getElementById('cantAdultos');
    else if (tipo === 'ninos') input = document.getElementById('cantNinos');
    else if (tipo === 'bebes') input = document.getElementById('cantBebes');
    
    if (input) {
        let val = (parseInt(input.value) || 0) + delta;
        
        // Adultos minimo 1
        if (tipo === 'adultos' && val < 1) val = 1;
        // Niños/bebes minimo 0
        if (tipo !== 'adultos' && val < 0) val = 0;
        
        input.value = val;
        actualizarTotales();
    }
}

// Variable para guardar el método de pago seleccionado
let metodoPagoActual = 'efectivo';

function mostrarPaso(n) {
    document.querySelectorAll('.paso').forEach(p => p.classList.remove('active'));
    const elPaso = document.getElementById('paso' + n);
    if (elPaso) elPaso.classList.add('active');
    
    // Actualizar nodos del stepper
    document.querySelectorAll('.step-node, .step').forEach(node => {
        node.classList.remove('active', 'completed');
        const stepNum = parseInt(node.getAttribute('data-step'), 10);
        if (stepNum < n) {
            node.classList.add('completed');
        } else if (stepNum === n) {
            node.classList.add('active');
        }
    });

    // Actualizar barra de progreso
    const progressTrack = document.getElementById('stepperTrackProgress');
    if (progressTrack) {
        const percentage = ((n - 1) / 3) * 100;
        progressTrack.style.width = percentage + '%';
    }

    window.scrollTo({ top: 0, behavior: 'smooth' });
}

function seleccionarPago(element, tipo) {
    metodoPagoActual = tipo;

    // Cambiar color de la tarjeta seleccionada
    document.querySelectorAll('.tarjeta-pago').forEach(t => t.classList.remove('activa'));
    if (element) element.classList.add('activa');

    // Ocultar todos los detalles de pago
    document.querySelectorAll('.detalle-pago').forEach(d => {
        d.classList.remove('visible');
        d.style.display = 'none';
    });

    // Mostrar el detalle correspondiente
    let divId = 'pago' + tipo.charAt(0).toUpperCase() + tipo.slice(1);
    let div = document.getElementById(divId);
    if(div) {
        div.style.display = 'block';
        setTimeout(() => div.classList.add('visible'), 10);
    }

    // Actualizar el resumen en el Paso 4
    const nombresMetodo = {
        'efectivo': 'Presencial',
        'yape': 'Yape',
        'plin': 'Plin',
        'tarjeta': 'Tarjeta'
    };
    const elRMet = document.getElementById('rMetodoPago');
    if (elRMet) elRMet.textContent = nombresMetodo[tipo] || 'Presencial';
}

function validarPasoPago() {
    if (metodoPagoActual === 'yape' || metodoPagoActual === 'plin') {
        const inputId = metodoPagoActual === 'yape' ? 'comprobanteYape' : 'comprobantePlin';
        const inputComp = document.getElementById(inputId);
        const tieneArchivo = inputComp && inputComp.files && inputComp.files.length > 0;
        
        if (!tieneArchivo) {
            const nombreMetodo = metodoPagoActual.toUpperCase();
            if (typeof Swal !== 'undefined') {
                Swal.fire({
                    icon: 'warning',
                    title: 'Voucher requerido',
                    text: `Por favor, debes subir tu comprobante de pago de ${nombreMetodo} para poder continuar con la reserva.`,
                    confirmButtonColor: '#0b1f38'
                });
            } else if (typeof toastr !== 'undefined') {
                toastr.warning(`Debes subir tu comprobante de pago de ${nombreMetodo} para continuar.`, 'Voucher requerido');
            } else {
                alert(`Debes subir tu voucher de ${nombreMetodo} para continuar.`);
            }
            return false;
        }
    } else if (metodoPagoActual === 'tarjeta') {
        const numTarjeta = document.getElementById('numTarjeta') ? document.getElementById('numTarjeta').value : '';
        const cleanNum = numTarjeta.replace(/\s/g, '');
        if (cleanNum.length < 16) {
            if (typeof Swal !== 'undefined') {
                Swal.fire({
                    icon: 'warning',
                    title: 'Tarjeta inválida',
                    text: 'Por favor, ingresa un número de tarjeta válido (16 dígitos).',
                    confirmButtonColor: '#0b1f38'
                });
            } else if (typeof toastr !== 'undefined') {
                toastr.warning('Por favor, ingresa un número de tarjeta válido (16 dígitos).', 'Datos de tarjeta');
            } else {
                alert('Ingresa un número de tarjeta válido.');
            }
            return false;
        }

        // Validar vencimiento MM/AAAA
        const vencVal = document.getElementById('vencimiento') ? document.getElementById('vencimiento').value.trim() : '';
        const parts = vencVal.split('/');
        if (parts.length !== 2 || parts[0].length !== 2 || parts[1].length !== 4) {
            if (typeof toastr !== 'undefined') toastr.warning('Ingresa la fecha de vencimiento en formato MM/AAAA.', 'Vencimiento requerido');
            else alert('Ingresa la fecha de vencimiento en formato MM/AAAA.');
            return false;
        }

        const mes = parseInt(parts[0], 10);
        const anio = parseInt(parts[1], 10);

        if (isNaN(mes) || mes < 1 || mes > 12) {
            if (typeof toastr !== 'undefined') toastr.warning('El mes de vencimiento debe estar entre 01 y 12.', 'Mes inválido');
            else alert('El mes debe estar entre 01 y 12.');
            return false;
        }

        if (isNaN(anio) || anio < 2026) {
            if (typeof Swal !== 'undefined') {
                Swal.fire({
                    icon: 'error',
                    title: 'Tarjeta Vencida',
                    text: 'La tarjeta se encuentra vencida. El año de vencimiento debe ser mayor o igual a 2026.',
                    confirmButtonColor: '#0b1f38'
                });
            } else if (typeof toastr !== 'undefined') {
                toastr.error('La tarjeta se encuentra vencida (año debe ser >= 2026).', 'Tarjeta Vencida');
            } else {
                alert('La tarjeta se encuentra vencida.');
            }
            return false;
        }
    }

    // Si pasa las validaciones, avanza al paso 4
    mostrarPaso(4);
}

function mostrarModal() {
    const modal = document.getElementById('modalConfirmar');
    if (modal) modal.style.display = 'flex';
}

function cerrarModal() {
    const modal = document.getElementById('modalConfirmar');
    if (modal) modal.style.display = 'none';
}

function guardarReservaEnStorage(cantA, cantN, cantB, totalCosto, metodo, codigoPersonalizado, tourData, fechaInput) {
    const user = window.getCurrentUser ? window.getCurrentUser() : null;
    const key = 'travelink_reservas_' + (user?.email || user?.correo || 'guest');
    let list = [];
    try { list = JSON.parse(localStorage.getItem(key) || '[]'); } catch(e) { list = []; }
    const nextNum = list.length + 1;
    const codeStr = codigoPersonalizado || ('TRK-' + String(nextNum).padStart(3, '0'));
    const totalP = cantA + cantN + cantB;
    const tour = tourData || {};
    const nueva = {
        id: nextNum, codigo: codeStr,
        titulo: tour.nombre || tour.title || 'Experiencia Travelink',
        ubicacion: tour.destino || tour.ubicacion || 'Perú',
        fechas: fechaInput || 'Fecha seleccionada',
        personas: totalP + (totalP === 1 ? ' persona' : ' personas'),
        agencia: tour.agencia || 'Agencia autorizada', estado: 'Pendiente',
        total: totalCosto, metodoPago: metodo,
        imagen: tour.imagen || tour.imgMain || '../../img/lima.jpg',
        fechaRegistro: new Date().toISOString().split('T')[0]
    };
    list.unshift(nueva);
    localStorage.setItem(key, JSON.stringify(list));
    return nueva;
}

function confirmarReserva() {
    cerrarModal();
    const elA = document.getElementById('cantAdultos');
    const elN = document.getElementById('cantNinos');
    const elB = document.getElementById('cantBebes');
    const cantA = elA ? parseInt(elA.value, 10) || 0 : 1;
    const cantN = elN ? parseInt(elN.value, 10) || 0 : 0;
    const cantB = elB ? parseInt(elB.value, 10) || 0 : 0;
    const totalPersonas = cantA + cantN + cantB;
    const totalCosto = cantA * precios.adultos + cantN * precios.ninos + cantB * precios.bebes;
    const fechaInput = document.getElementById('fechaInicio')?.value || travelinkSelectedDate?.fecha || '';
    const idTourFecha = Number(new URLSearchParams(window.location.search).get('idTourFecha') || travelinkSelectedDate?.idTourFecha || 0);
    const idTour = Number(new URLSearchParams(window.location.search).get('idTour') || travelinkSelectedTour?.idTour || travelinkSelectedTour?.id || 0);
    const user = window.getCurrentUser ? window.getCurrentUser() : null;
    if (!user || !(user.idUsuario || user.id)) {
        alert('No se pudo identificar tu sesión. Cierra sesión e inicia nuevamente antes de reservar.');
        return;
    }
    if (!idTourFecha || !idTour || !fechaInput) {
        alert('No hay una fecha válida seleccionada. Regresa al detalle de la experiencia y selecciona una fecha disponible.');
        return;
    }
    const nombre = document.getElementById('inputNombre')?.value.trim() || '';
    const paterno = document.getElementById('inputPaterno')?.value.trim() || '';
    const materno = document.getElementById('inputMaterno')?.value.trim() || '';
    const dni = document.getElementById('inputDNI')?.value.trim() || '';
    if (!nombre || !paterno || !dni) {
        mostrarPaso(2);
        alert('Completa nombres, apellido y documento del titular antes de confirmar.');
        return;
    }
    for (let i = 2; i <= totalPersonas; i++) {
        const n = document.getElementById('pasajero' + i + 'Nombre')?.value.trim() || '';
        const a = document.getElementById('pasajero' + i + 'Apellidos')?.value.trim() || '';
        const d = document.getElementById('pasajero' + i + 'Dni')?.value.trim() || '';
        if (!n || !a || !d) {
            mostrarPaso(2);
            alert('Completa nombres, apellidos y documento de cada pasajero seleccionado.');
            return;
        }
    }
    const payload = {
        email: user.email || user.correo || '', idUsuario: user.idUsuario || user.id,
        idTour: idTour, idTourFecha: idTourFecha, idPaquete: idTour,
        fechaInicio: fechaInput, fechaServicio: fechaInput, fechaFin: fechaInput,
        cantidadPersonas: totalPersonas, cantAdultos: cantA, cantNinos: cantN, cantBebes: cantB,
        precioTotal: totalCosto, total: totalCosto,
        metodoPago: ({ efectivo: 'Presencial', yape: 'Yape', plin: 'Plin', tarjeta: 'Tarjeta' })[metodoPagoActual] || 'Presencial',
        codigo: 'TRK-' + Date.now().toString().slice(-6),
        titulo: travelinkSelectedTour?.nombre || travelinkSelectedTour?.title || document.getElementById('reservaTourTitulo')?.textContent || 'Experiencia Travelink',
        nombre: nombre, apellidos: [paterno, materno].filter(Boolean).join(' '), dni: dni,
        telefono: document.getElementById('inputTelefono')?.value.trim() || ''
    };
    for (let i = 2; i <= totalPersonas; i++) {
        payload['pasajero' + i + 'Nombre'] = document.getElementById('pasajero' + i + 'Nombre')?.value.trim() || '';
        payload['pasajero' + i + 'Apellidos'] = document.getElementById('pasajero' + i + 'Apellidos')?.value.trim() || '';
        payload['pasajero' + i + 'Dni'] = document.getElementById('pasajero' + i + 'Dni')?.value.trim() || '';
    }
    fetch('http://localhost:8080/api/guardar_reserva', {
        method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload)
    })
    .then(async res => { const data = await res.json(); if (!res.ok) throw new Error(data.message || 'Error al guardar'); return data; })
    .then(data => {
        if (data.status !== 'success') throw new Error(data.message || 'El servidor rechazó la reserva.');
        guardarReservaEnStorage(cantA, cantN, cantB, totalCosto, payload.metodoPago, data.data?.codigo || payload.codigo, travelinkSelectedTour, fechaInput);
        if (typeof Swal !== 'undefined') Swal.fire({ icon: 'success', title: 'Reserva registrada', text: 'La reserva se guardó en la base de datos.', confirmButtonColor: '#196f3d' }).then(() => { window.location.href = 'reservas.html'; });
        else { alert('La reserva se guardó en la base de datos.'); window.location.href = 'reservas.html'; }
    })
    .catch(err => {
        console.error('Error al guardar la reserva:', err);
        if (typeof Swal !== 'undefined') Swal.fire({ icon: 'error', title: 'No se pudo guardar la reserva', text: err.message, confirmButtonColor: '#0b1f38' });
        else alert('No se pudo guardar la reserva: ' + err.message);
    });
}

function renderPassengerForms() {
    const container = document.getElementById('passengerFormsContainer');
    if (!container) return;
    const counts = {
        adultos: Math.max(1, parseInt(document.getElementById('cantAdultos')?.value, 10) || 1),
        ninos: Math.max(0, parseInt(document.getElementById('cantNinos')?.value, 10) || 0),
        bebes: Math.max(0, parseInt(document.getElementById('cantBebes')?.value, 10) || 0)
    };
    const total = counts.adultos + counts.ninos + counts.bebes;
    const old = {};
    container.querySelectorAll('input').forEach(el => { old[el.id] = el.value; });
    const user = window.getCurrentUser ? window.getCurrentUser() : null;
    const userName = user?.nombres || user?.nombre || user?.username || '';
    const userSurname = user?.apellidos || '';
    const specs = [];
    for (let i = 1; i <= total; i++) {
        const tipo = i <= counts.adultos ? 'Adulto' : (i <= counts.adultos + counts.ninos ? 'Niño' : 'Bebé');
        const titular = i === 1;
        const prefix = titular ? 'input' : 'pasajero' + i;
        const nombreId = titular ? 'inputNombre' : prefix + 'Nombre';
        const paternoId = titular ? 'inputPaterno' : prefix + 'Apellidos';
        const dniId = titular ? 'inputDNI' : prefix + 'Dni';
        const telefonoId = titular ? 'inputTelefono' : prefix + 'Telefono';
        const maternoId = titular ? 'inputMaterno' : null;
        specs.push(`<div class="passenger-card-box" style="margin-bottom:14px"><div class="passenger-card-header"><h3>Pasajero ${i}${titular ? ' <span class="badge-titular">(Titular)</span>' : ''}</h3><span style="font-size:12px;color:#64748b">${tipo}</span></div><div class="passenger-grid-3"><div class="input-field-block"><label>Nombres</label><input type="text" id="${nombreId}" value="${escapeReserva(old[nombreId] || (titular ? userName : ''))}" ${titular ? '' : ''}></div><div class="input-field-block"><label>${titular ? 'Apellido paterno' : 'Apellidos'}</label><input type="text" id="${paternoId}" value="${escapeReserva(old[paternoId] || (titular ? userSurname : ''))}"></div>${titular ? `<div class="input-field-block"><label>Apellido materno</label><input type="text" id="${maternoId}" value="${escapeReserva(old[maternoId] || '')}"></div>` : ''}<div class="input-field-block"><label>DNI / documento</label><input type="text" id="${dniId}" value="${escapeReserva(old[dniId] || (titular ? (user?.dni || user?.documento || '') : ''))}" maxlength="12"></div>${titular ? `<div class="input-field-block"><label>Teléfono</label><input type="tel" id="${telefonoId}" value="${escapeReserva(old[telefonoId] || user?.telefono || '')}" maxlength="15"></div>` : ''}</div></div>`);
    }
    container.innerHTML = specs.join('');
    container.querySelectorAll('input').forEach(el => el.addEventListener('input', actualizarResumenPasajeros));
    actualizarResumenPasajeros();
}

function escapeReserva(value) {
    return String(value || '').replace(/[&<>"']/g, ch => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[ch]));
}

function actualizarResumenPasajeros() {
    const container = document.getElementById('summaryPassengers');
    if (!container) return;
    const cards = [];
    const total = (parseInt(document.getElementById('cantAdultos')?.value, 10) || 1) + (parseInt(document.getElementById('cantNinos')?.value, 10) || 0) + (parseInt(document.getElementById('cantBebes')?.value, 10) || 0);
    for (let i = 1; i <= total; i++) {
        const nombre = document.getElementById(i === 1 ? 'inputNombre' : 'pasajero' + i + 'Nombre')?.value.trim() || 'Datos por completar';
        const apellidos = document.getElementById(i === 1 ? 'inputPaterno' : 'pasajero' + i + 'Apellidos')?.value.trim() || '';
        const dni = document.getElementById(i === 1 ? 'inputDNI' : 'pasajero' + i + 'Dni')?.value.trim() || 'Pendiente';
        cards.push(`<div class="passenger-summary-item"><h5>Pasajero ${i}${i === 1 ? ' (Titular)' : ''}</h5><p class="p-name">${escapeReserva((nombre + ' ' + apellidos).trim())}</p><p class="p-info">Documento: ${escapeReserva(dni)}</p></div>`);
    }
    container.innerHTML = cards.join('');
}

function inicializarReservaSeleccionada() {
    const params = new URLSearchParams(window.location.search);
    try { travelinkSelectedTour = JSON.parse(sessionStorage.getItem('travelink_selected_tour') || 'null'); } catch (e) { travelinkSelectedTour = null; }
    try { travelinkSelectedDate = JSON.parse(sessionStorage.getItem('travelink_selected_date') || 'null'); } catch (e) { travelinkSelectedDate = null; }
    const tourId = params.get('idTour') || params.get('idPaquete');
    if (!travelinkSelectedTour || String(travelinkSelectedTour.idTour || travelinkSelectedTour.idPaquete || travelinkSelectedTour.id) !== String(tourId)) {
        travelinkSelectedTour = { idTour: Number(tourId || 0), nombre: params.get('titulo') || 'Experiencia Travelink', precio: Number(params.get('precio') || 0), precioAdulto: Number(params.get('precio') || 0), agencia: params.get('agencia') || 'Agencia autorizada', imagen: '../../img/lima.jpg', destino: 'Perú' };
    }
    const fecha = params.get('fecha') || travelinkSelectedDate?.fecha || '';
    const fechaInput = document.getElementById('fechaInicio');
    if (fechaInput) fechaInput.value = fecha;
    const title = travelinkSelectedTour.nombre || travelinkSelectedTour.title || params.get('titulo') || 'Experiencia Travelink';
    const agency = travelinkSelectedTour.agencia || params.get('agencia') || 'Agencia autorizada';
    const image = travelinkSelectedTour.imagen || travelinkSelectedTour.imgMain || '../../img/lima.jpg';
    ['reservaTourTitulo','resumenTourTitulo'].forEach(id => { const el = document.getElementById(id); if (el) el.textContent = title; });
    ['reservaTourAgencia','resumenTourAgencia'].forEach(id => { const el = document.getElementById(id); if (el) el.textContent = agency; });
    ['reservaTourImagen','resumenTourImagen'].forEach(id => { const el = document.getElementById(id); if (el) el.src = image; });
    const fechaTexto = document.getElementById('reservaFechaTexto'); if (fechaTexto) fechaTexto.textContent = fecha || 'Fecha no seleccionada';
    const resumenFecha = document.getElementById('resumenFechaTour'); if (resumenFecha) resumenFecha.textContent = fecha || 'Fecha no seleccionada';
    precios = {
        adultos: Number(travelinkSelectedTour.precioAdulto ?? travelinkSelectedTour.precio ?? params.get('precio') ?? 120),
        ninos: Number(travelinkSelectedTour.precioNino ?? Math.round(Number(travelinkSelectedTour.precio ?? params.get('precio') ?? 120) * 0.7)),
        bebes: Number(travelinkSelectedTour.precioBebe ?? Math.round(Number(travelinkSelectedTour.precio ?? params.get('precio') ?? 120) * 0.3))
    };
    const a = document.getElementById('cantAdultos'); if (a) a.value = '1';
    const n = document.getElementById('cantNinos'); if (n) n.value = '0';
    const b = document.getElementById('cantBebes'); if (b) b.value = '0';
    const rMet = document.getElementById('rMetodoPago'); if (rMet) rMet.textContent = 'Presencial';
    actualizarTotales();
}

inicializarReservaSeleccionada();
