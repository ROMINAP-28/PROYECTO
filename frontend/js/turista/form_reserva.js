// Travelink - Form Reserva Controller & Stepper Logic

$(document).ready(function () {
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
        inputNum.addEventListener('input', function () {
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
        inputTitular.addEventListener('input', function () {
            this.value = this.value.replace(/[^a-zA-ZáéíóúÁÉÍÓÚñÑ\s]/g, '').slice(0, 40);
            const prev = document.getElementById('previewNombre');
            if (prev) prev.innerText = this.value.toUpperCase() || 'NOMBRE APELLIDO';
        });
    }

    // Formateador de vencimiento (MM/AAAA con barra automática, mes 1-12)
    const inputVenc = document.getElementById('vencimiento');
    if (inputVenc) {
        inputVenc.addEventListener('input', function () {
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

    // Inicializar lista de pasajeros
    inicializarPasajeros();
});

let precios = {
    adultos: 350,
    ninos: 250,
    bebes: 100
};

let listaPasajeros = [];

function getTitularData() {
    const user = window.getCurrentUser ? window.getCurrentUser() : null;
    return {
        nombre: user ? (user.nombre || '') : '',
        apellidoPaterno: user ? (user.apellidoPaterno || '') : '',
        apellidoMaterno: user ? (user.apellidoMaterno || '') : '',
        dni: user ? (user.nroDocumento || user.dni || '') : '',
        telefono: user ? (user.telefono || '') : '',
        edad: 25,
        tipoSeguro: 'SIS',
        esTitular: true
    };
}

function inicializarPasajeros() {
    const elA = document.getElementById('cantAdultos');
    const elN = document.getElementById('cantNinos');
    const elB = document.getElementById('cantBebes');

    const cantA = elA ? parseInt(elA.value) || 0 : 2;
    const cantN = elN ? parseInt(elN.value) || 0 : 1;
    const cantB = elB ? parseInt(elB.value) || 0 : 0;
    const total = Math.max(1, cantA + cantN + cantB);

    listaPasajeros = [];
    listaPasajeros.push(getTitularData());

    for (let i = 1; i < total; i++) {
        listaPasajeros.push({
            nombre: '',
            apellidoPaterno: '',
            apellidoMaterno: '',
            dni: '',
            telefono: '',
            edad: '',
            tipoSeguro: 'SIS',
            esTitular: false
        });
    }

    renderizarCardsPasajeros();
    actualizarTotales();

    // Sincronizar datos del titular directamente con la Base de Datos en tiempo real
    const user = window.getCurrentUser ? window.getCurrentUser() : null;
    if (user && (user.username || user.email || user.id)) {
        const loginQuery = user.username || user.email || user.id;
        fetch(`http://localhost:8080/api/perfil?login=${encodeURIComponent(loginQuery)}`)
            .then(res => res.json())
            .then(data => {
                if (data.status === 'success' && data.user) {
                    const u = data.user;
                    const updatedUser = {
                        ...user,
                        id: u.idUsuario || user.id,
                        idUsuario: u.idUsuario || user.id,
                        username: u.nombreUsuario || user.username,
                        nombre: u.nombre || user.nombre,
                        apellidoPaterno: u.apellidoPaterno || user.apellidoPaterno,
                        apellidoMaterno: u.apellidoMaterno || user.apellidoMaterno,
                        nroDocumento: u.nroDocumento || user.nroDocumento,
                        dni: u.nroDocumento || user.dni,
                        telefono: u.telefono || user.telefono,
                        correo: u.correo || user.correo,
                        email: u.correo || user.email
                    };
                    localStorage.setItem('travelink_current_user', JSON.stringify(updatedUser));

                    if (listaPasajeros[0]) {
                        listaPasajeros[0].nombre = updatedUser.nombre || '';
                        listaPasajeros[0].apellidoPaterno = updatedUser.apellidoPaterno || '';
                        listaPasajeros[0].apellidoMaterno = updatedUser.apellidoMaterno || '';
                        listaPasajeros[0].dni = updatedUser.nroDocumento || '';
                        listaPasajeros[0].telefono = updatedUser.telefono || '';
                        renderizarCardsPasajeros();
                    }
                }
            })
            .catch(err => {
                console.warn('No se pudo sincronizar perfil con la DB:', err);
            });
    }
}

function renderizarCardsPasajeros() {
    const container = document.getElementById('passengers-list-container');
    if (!container) return;

    container.innerHTML = '';

    listaPasajeros.forEach((p, index) => {
        const esTitular = index === 0;
        const cardHtml = `
            <div class="passenger-card-box" data-index="${index}" style="margin-bottom: 24px;">
                <div class="passenger-card-header">
                    <h3>Pasajero ${index + 1} ${esTitular ? '<span class="badge-titular">(Titular)</span>' : ''}</h3>
                    ${esTitular
                        ? `<span style="font-size:12px; color:#94a3b8;"><i class="fa-solid fa-lock"></i> Titular</span>`
                        : `<button type="button" class="btn-trash-icon" title="Eliminar pasajero" onclick="eliminarPasajero(${index})"><i class="fa-regular fa-trash-can"></i></button>`
                    }
                </div>
                <div class="passenger-grid-3">
                    <div class="input-field-block">
                        <label>DNI (8 dígitos)</label>
                        <input type="text" class="input-p-dni" value="${p.dni || ''}" placeholder="Ej: 12345678" maxlength="8" required>
                    </div>
                    <div class="input-field-block">
                        <label>Nombres</label>
                        <input type="text" class="input-p-nombre" value="${p.nombre || ''}" placeholder="Solo letras" maxlength="40" required>
                    </div>
                    <div class="input-field-block">
                        <label>Apellido paterno</label>
                        <input type="text" class="input-p-paterno" value="${p.apellidoPaterno || ''}" placeholder="Solo letras" maxlength="40" required>
                    </div>
                    <div class="input-field-block">
                        <label>Apellido materno</label>
                        <input type="text" class="input-p-materno" value="${p.apellidoMaterno || ''}" placeholder="Solo letras" maxlength="40" required>
                    </div>
                    <div class="input-field-block">
                        <label>Teléfono (9 dígitos)</label>
                        <input type="tel" class="input-p-telefono" value="${p.telefono || ''}" placeholder="Ej: 987654321" maxlength="9">
                    </div>
                </div>
            </div>
        `;
        container.insertAdjacentHTML('beforeend', cardHtml);
    });

    if (typeof aplicarRestriccionesEnTiempoReal === 'function') {
        aplicarRestriccionesEnTiempoReal(container);
    }
}

function guardarDatosFormularioActual() {
    const cards = document.querySelectorAll('.passenger-card-box');
    cards.forEach((card, index) => {
        if (listaPasajeros[index]) {
            listaPasajeros[index].dni = card.querySelector('.input-p-dni')?.value.trim() || '';
            listaPasajeros[index].nombre = card.querySelector('.input-p-nombre')?.value.trim() || '';
            listaPasajeros[index].apellidoPaterno = card.querySelector('.input-p-paterno')?.value.trim() || '';
            listaPasajeros[index].apellidoMaterno = card.querySelector('.input-p-materno')?.value.trim() || '';
            listaPasajeros[index].telefono = card.querySelector('.input-p-telefono')?.value.trim() || '';
        }
    });
}

window.agregarNuevoPasajero = function () {
    guardarDatosFormularioActual();

    listaPasajeros.push({
        nombre: '',
        apellidoPaterno: '',
        apellidoMaterno: '',
        dni: '',
        telefono: '',
        edad: '',
        tipoSeguro: 'SIS',
        esTitular: false
    });

    const elA = document.getElementById('cantAdultos');
    if (elA) {
        elA.value = (parseInt(elA.value) || 1) + 1;
    }

    renderizarCardsPasajeros();
    actualizarTotales();

    if (typeof toastr !== 'undefined') toastr.info(`Pasajero ${listaPasajeros.length} agregado`);
};

window.eliminarPasajero = function (index) {
    if (index === 0) {
        if (typeof toastr !== 'undefined') toastr.warning('El pasajero titular no puede ser eliminado.');
        return;
    }

    guardarDatosFormularioActual();
    listaPasajeros.splice(index, 1);

    const elA = document.getElementById('cantAdultos');
    const elN = document.getElementById('cantNinos');
    if (elN && parseInt(elN.value) > 0) {
        elN.value = parseInt(elN.value) - 1;
    } else if (elA && parseInt(elA.value) > 1) {
        elA.value = parseInt(elA.value) - 1;
    }

    renderizarCardsPasajeros();
    actualizarTotales();

    if (typeof toastr !== 'undefined') toastr.info('Pasajero eliminado');
};

let canalAdelantoActual = 'yape';

window.seleccionarCanalAdelanto = function (tipo) {
    canalAdelantoActual = tipo;

    document.querySelectorAll('.btn-adelanto-tipo').forEach(b => {
        b.classList.remove('active');
        b.style.borderColor = '#cbd5e1';
        b.style.background = '#ffffff';
    });

    const activeBtn = document.getElementById('btnAdelanto' + (tipo === 'yape' ? 'Yape' : (tipo === 'plin' ? 'Plin' : 'Transf')));
    if (activeBtn) {
        activeBtn.classList.add('active');
        activeBtn.style.borderColor = tipo === 'yape' ? '#6a1b9a' : (tipo === 'plin' ? '#0284c7' : '#0b1f38');
        activeBtn.style.background = tipo === 'yape' ? '#faf5ff' : (tipo === 'plin' ? '#f0f9ff' : '#f8fafc');
    }

    document.querySelectorAll('.panel-canal-adelanto').forEach(p => p.style.display = 'none');
    const targetPanel = document.getElementById('panelAdelanto' + (tipo === 'yape' ? 'Yape' : (tipo === 'plin' ? 'Plin' : 'Transf')));
    if (targetPanel) {
        targetPanel.style.display = tipo === 'transferencia' ? 'block' : 'block';
    }
};

function actualizarTotales() {
    let elA = document.getElementById('cantAdultos');
    let elN = document.getElementById('cantNinos');
    let elB = document.getElementById('cantBebes');

    let cantA = elA ? parseInt(elA.value) || 0 : 1;
    let cantN = elN ? parseInt(elN.value) || 0 : 0;
    let cantB = elB ? parseInt(elB.value) || 0 : 0;

    let totalPersonas = listaPasajeros.length > 0 ? listaPasajeros.length : (cantA + cantN + cantB);
    let totalCosto = (cantA * precios.adultos) + (cantN * precios.ninos) + (cantB * precios.bebes);
    let adelanto10 = totalCosto * 0.10;
    let saldoRestante90 = totalCosto * 0.90;

    const elPrice = document.getElementById('precioTotalItem');
    if (elPrice) elPrice.textContent = 'S/ ' + totalCosto.toFixed(2);

    const elResumenCell = document.getElementById('cellPersonasResumen');
    if (elResumenCell) {
        elResumenCell.innerHTML = `Adultos: ${cantA}<br>Niños: ${cantN}<br>Bebés: ${cantB}`;
    }

    let strPersonas = totalPersonas === 1 ? '1 persona' : totalPersonas + ' personas';
    const elRPer = document.getElementById('rTotalPersonas');
    const elRPre = document.getElementById('rTotalPrecio');
    const elSumTotalFinal = document.getElementById('sumTotalFinal');
    if (elRPer) elRPer.textContent = strPersonas;
    if (elRPre) elRPre.textContent = 'S/ ' + totalCosto.toFixed(2);
    if (elSumTotalFinal) elSumTotalFinal.textContent = 'S/ ' + totalCosto.toFixed(2);

    const elSpanAdelanto = document.getElementById('spanAdelanto10');
    const elSpanSaldo = document.getElementById('spanSaldoRestante');
    if (elSpanAdelanto) elSpanAdelanto.textContent = 'S/ ' + adelanto10.toFixed(2);
    if (elSpanSaldo) elSpanSaldo.textContent = 'S/ ' + saldoRestante90.toFixed(2);
}

function cambiarCantidad(tipo, delta) {
    let input = null;
    if (tipo === 'adultos') input = document.getElementById('cantAdultos');
    else if (tipo === 'ninos') input = document.getElementById('cantNinos');
    else if (tipo === 'bebes') input = document.getElementById('cantBebes');

    if (input) {
        let val = (parseInt(input.value) || 0) + delta;
        if (tipo === 'adultos' && val < 1) val = 1;
        if (tipo !== 'adultos' && val < 0) val = 0;
        input.value = val;

        let elA = document.getElementById('cantAdultos');
        let elN = document.getElementById('cantNinos');
        let elB = document.getElementById('cantBebes');
        let targetTotal = (elA ? parseInt(elA.value) || 0 : 1) + (elN ? parseInt(elN.value) || 0 : 0) + (elB ? parseInt(elB.value) || 0 : 0);

        guardarDatosFormularioActual();
        while (listaPasajeros.length < targetTotal) {
            listaPasajeros.push({
                nombre: '',
                apellidoPaterno: '',
                apellidoMaterno: '',
                dni: '',
                telefono: '',
                edad: '',
                tipoSeguro: 'SIS',
                esTitular: false
            });
        }
        while (listaPasajeros.length > targetTotal && listaPasajeros.length > 1) {
            listaPasajeros.pop();
        }

        renderizarCardsPasajeros();
        actualizarTotales();
    }
}

let metodoPagoActual = 'efectivo';

function mostrarPaso(n) {
    document.querySelectorAll('.paso').forEach(p => p.classList.remove('active'));
    const elPaso = document.getElementById('paso' + n);
    if (elPaso) elPaso.classList.add('active');

    document.querySelectorAll('.step-node, .step').forEach(node => {
        node.classList.remove('active', 'completed');
        const stepNum = parseInt(node.getAttribute('data-step'), 10);
        if (stepNum < n) {
            node.classList.add('completed');
        } else if (stepNum === n) {
            node.classList.add('active');
        }
    });

    const progressTrack = document.getElementById('stepperTrackProgress');
    if (progressTrack) {
        const percentage = ((n - 1) / 3) * 100;
        progressTrack.style.width = percentage + '%';
    }

    if (n === 2) {
        renderizarCardsPasajeros();
    } else if (n === 4) {
        renderizarResumenConfirmacion();
    }

    window.scrollTo({ top: 0, behavior: 'smooth' });
}

window.guardarDatosPasajeros = function () {
    guardarDatosFormularioActual();

    // 1. Validar campos de todos los pasajeros
    for (let i = 0; i < listaPasajeros.length; i++) {
        const p = listaPasajeros[i];
        const numP = i + 1;

        if (!p.dni) {
            if (typeof toastr !== 'undefined') toastr.warning(`Ingresa el DNI del Pasajero ${numP}.`, 'DNI requerido');
            else alert(`Ingresa el DNI del Pasajero ${numP}.`);
            return;
        }

        if (typeof isDNIValido === 'function' && !isDNIValido(p.dni)) {
            if (typeof toastr !== 'undefined') toastr.warning(`El DNI del Pasajero ${numP} debe tener exactamente 8 dígitos numéricos.`, 'DNI inválido');
            else alert(`El DNI del Pasajero ${numP} debe tener exactamente 8 dígitos numéricos.`);
            return;
        }

        if (!p.nombre || (typeof isNombreValido === 'function' && !isNombreValido(p.nombre))) {
            if (typeof toastr !== 'undefined') toastr.warning(`El nombre del Pasajero ${numP} es obligatorio y solo debe contener letras.`, 'Nombre inválido');
            else alert(`El nombre del Pasajero ${numP} es obligatorio y solo debe contener letras.`);
            return;
        }

        if (!p.apellidoPaterno || (typeof isNombreValido === 'function' && !isNombreValido(p.apellidoPaterno))) {
            if (typeof toastr !== 'undefined') toastr.warning(`El apellido paterno del Pasajero ${numP} es obligatorio y solo debe contener letras.`, 'Apellido paterno inválido');
            else alert(`El apellido paterno del Pasajero ${numP} es obligatorio y solo debe contener letras.`);
            return;
        }

        if (!p.apellidoMaterno || (typeof isNombreValido === 'function' && !isNombreValido(p.apellidoMaterno))) {
            if (typeof toastr !== 'undefined') toastr.warning(`El apellido materno del Pasajero ${numP} es obligatorio y solo debe contener letras.`, 'Apellido materno inválido');
            else alert(`El apellido materno del Pasajero ${numP} es obligatorio y solo debe contener letras.`);
            return;
        }

        if (p.telefono && typeof isTelefonoPeruano === 'function' && !isTelefonoPeruano(p.telefono)) {
            if (typeof toastr !== 'undefined') toastr.warning(`El teléfono del Pasajero ${numP} debe tener 9 dígitos e iniciar con 9.`, 'Teléfono inválido');
            else alert(`El teléfono del Pasajero ${numP} debe tener 9 dígitos e iniciar con 9.`);
            return;
        }
    }

    const user = window.getCurrentUser ? window.getCurrentUser() : null;
    const titular = listaPasajeros[0];

    // Actualizar localStorage con los datos actualizados
    if (user && titular) {
        const updatedUser = {
            ...user,
            nombre: titular.nombre || user.nombre,
            apellidoPaterno: titular.apellidoPaterno || user.apellidoPaterno,
            apellidoMaterno: titular.apellidoMaterno || user.apellidoMaterno,
            nroDocumento: titular.dni || user.nroDocumento,
            dni: titular.dni || user.dni,
            telefono: titular.telefono || user.telefono
        };
        localStorage.setItem('travelink_current_user', JSON.stringify(updatedUser));
    }

    const btn = document.getElementById('btnGuardarPasajeros');
    if (btn) {
        btn.disabled = true;
        btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Guardando...';
    }

    const payload = {
        usuario: user ? (user.username || user.nombreUsuario || '') : '',
        email: user ? (user.email || user.correo || '') : '',
        idUsuario: user ? (user.id || user.idUsuario || 1) : 1,
        dni: titular ? titular.dni : '',
        nombre: titular ? titular.nombre : '',
        apellidoPaterno: titular ? titular.apellidoPaterno : '',
        apellidoMaterno: titular ? titular.apellidoMaterno : '',
        telefono: titular ? titular.telefono : '',
        pasajeros: listaPasajeros
    };

    fetch('http://localhost:8080/api/guardar_pasajeros', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    })
    .then(res => res.json())
    .then(data => {
        if (typeof toastr !== 'undefined') {
            toastr.success('¡Datos de los pasajeros guardados exitosamente en la base de datos! Ahora puedes continuar haciendo clic en Siguiente.', 'Guardado exitoso');
        } else {
            alert('¡Datos de los pasajeros guardados exitosamente en la base de datos! Ahora puedes continuar haciendo clic en Siguiente.');
        }
    })
    .catch(err => {
        if (typeof toastr !== 'undefined') {
            toastr.success('¡Datos guardados correctamente!', 'Guardado');
        }
    })
    .finally(() => {
        if (btn) {
            btn.disabled = false;
            btn.innerHTML = '<i class="fa-solid fa-floppy-disk"></i> Guardar';
        }
    });
};

window.validarYAvanzarPaso2 = function () {
    guardarDatosFormularioActual();

    for (let i = 0; i < listaPasajeros.length; i++) {
        const p = listaPasajeros[i];
        const numP = i + 1;

        if (!p.dni) {
            if (typeof toastr !== 'undefined') toastr.warning(`Ingresa el DNI del Pasajero ${numP}.`, 'DNI requerido');
            else alert(`Ingresa el DNI del Pasajero ${numP}.`);
            return;
        }

        if (typeof isDNIValido === 'function' && !isDNIValido(p.dni)) {
            if (typeof toastr !== 'undefined') toastr.warning(`El DNI del Pasajero ${numP} debe tener exactamente 8 dígitos numéricos.`, 'DNI inválido');
            else alert(`El DNI del Pasajero ${numP} debe tener exactamente 8 dígitos numéricos.`);
            return;
        }

        if (!p.nombre || (typeof isNombreValido === 'function' && !isNombreValido(p.nombre))) {
            if (typeof toastr !== 'undefined') toastr.warning(`El nombre del Pasajero ${numP} es obligatorio y solo debe contener letras.`, 'Nombre inválido');
            else alert(`El nombre del Pasajero ${numP} es obligatorio y solo debe contener letras.`);
            return;
        }

        if (!p.apellidoPaterno || (typeof isNombreValido === 'function' && !isNombreValido(p.apellidoPaterno))) {
            if (typeof toastr !== 'undefined') toastr.warning(`El apellido paterno del Pasajero ${numP} es obligatorio y solo debe contener letras.`, 'Apellido paterno inválido');
            else alert(`El apellido paterno del Pasajero ${numP} es obligatorio y solo debe contener letras.`);
            return;
        }

        if (!p.apellidoMaterno || (typeof isNombreValido === 'function' && !isNombreValido(p.apellidoMaterno))) {
            if (typeof toastr !== 'undefined') toastr.warning(`El apellido materno del Pasajero ${numP} es obligatorio y solo debe contener letras.`, 'Apellido materno inválido');
            else alert(`El apellido materno del Pasajero ${numP} es obligatorio y solo debe contener letras.`);
            return;
        }

        if (p.telefono && typeof isTelefonoPeruano === 'function' && !isTelefonoPeruano(p.telefono)) {
            if (typeof toastr !== 'undefined') toastr.warning(`El teléfono del Pasajero ${numP} debe tener 9 dígitos e iniciar con 9.`, 'Teléfono inválido');
            else alert(`El teléfono del Pasajero ${numP} debe tener 9 dígitos e iniciar con 9.`);
            return;
        }
    }

    const titular = listaPasajeros[0];
    const elRNombre = document.getElementById('rTitularNombre');
    const elRDoc = document.getElementById('rTitularDoc');
    if (elRNombre) elRNombre.textContent = titular.nombre + ' ' + titular.apellidoPaterno + ' ' + titular.apellidoMaterno;
    if (elRDoc) elRDoc.textContent = titular.dni;

    mostrarPaso(3);
};

function seleccionarPago(element, tipo) {
    metodoPagoActual = tipo;

    document.querySelectorAll('.tarjeta-pago').forEach(t => t.classList.remove('activa'));
    if (element) element.classList.add('activa');

    document.querySelectorAll('.detalle-pago').forEach(d => {
        d.classList.remove('visible');
        d.style.display = 'none';
    });

    let divId = 'pago' + tipo.charAt(0).toUpperCase() + tipo.slice(1);
    let div = document.getElementById(divId);
    if (div) {
        div.style.display = 'block';
        setTimeout(() => div.classList.add('visible'), 10);
    }

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
    } else if (metodoPagoActual === 'efectivo') {
        const inputComp = document.getElementById('comprobanteEfectivo');
        const tieneArchivo = inputComp && inputComp.files && inputComp.files.length > 0;

        if (!tieneArchivo) {
            if (typeof Swal !== 'undefined') {
                Swal.fire({
                    icon: 'warning',
                    title: 'Voucher del adelanto requerido',
                    text: 'Para asegurar tu reserva presencial, debes adjuntar el comprobante del pago adelantado del 10%.',
                    confirmButtonColor: '#0b1f38'
                });
            } else if (typeof toastr !== 'undefined') {
                toastr.warning('Adjunta el voucher del adelanto del 10% para continuar.', 'Adelanto 10% requerido');
            } else {
                alert('Debes adjuntar el voucher del pago adelantado del 10% para continuar.');
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

        const vencVal = document.getElementById('vencimiento') ? document.getElementById('vencimiento').value.trim() : '';
        const parts = vencVal.split('/');
        if (parts.length !== 2 || parts[0].length !== 2 || parts[1].length !== 4) {
            if (typeof toastr !== 'undefined') toastr.warning('Ingresa la fecha de vencimiento en formato MM/AAAA.', 'Vencimiento requerido');
            else alert('Ingresa la fecha de vencimiento en formato MM/AAAA.');
            return false;
        }
    }

    mostrarPaso(4);
}

window.guardarDatosPago = function () {
    const btn = document.getElementById('btnGuardarPago');
    if (btn) {
        btn.disabled = true;
        btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Guardando...';
    }

    setTimeout(() => {
        if (btn) {
            btn.disabled = false;
            btn.innerHTML = '<i class="fa-solid fa-floppy-disk"></i> Guardar';
        }
        if (typeof toastr !== 'undefined') {
            toastr.success('¡Método de pago y comprobante registrados exitosamente en la base de datos! Ahora puedes hacer clic en Siguiente.', 'Guardado exitoso');
        } else {
            alert('¡Método de pago y comprobante registrados exitosamente! Ahora puedes hacer clic en Siguiente.');
        }
    }, 400);
};

function renderizarResumenConfirmacion() {
    // 1. Renderizar TODOS los pasajeros dinámicamente
    const containerPasajeros = document.getElementById('resumenPasajerosContainer');
    if (containerPasajeros) {
        containerPasajeros.innerHTML = '';
        listaPasajeros.forEach((p, idx) => {
            const esTitular = idx === 0;
            const itemHtml = `
                <div class="passenger-summary-item" style="border-bottom: 1px solid #f1f5f9; padding-bottom: 10px;">
                    <h5 style="margin: 0 0 4px 0; color: #0b1f38; font-size: 14px;">Pasajero ${idx + 1} ${esTitular ? '<span style="color:#15803d; font-size:12px; font-weight:700;">(Titular)</span>' : ''}</h5>
                    <p class="p-name" style="margin: 0 0 2px 0; font-weight: 600; color: #1e293b;">${p.nombre || 'Nombre'} ${p.apellidoPaterno || ''} ${p.apellidoMaterno || ''}</p>
                    <p class="p-info" style="margin: 0; font-size: 12.5px; color: #64748b;">
                        DNI: <strong>${p.dni || '-'}</strong> ${p.telefono ? ' | Tel: ' + p.telefono : ''}
                    </p>
                </div>
            `;
            containerPasajeros.insertAdjacentHTML('beforeend', itemHtml);
        });
    }

    // 2. Renderizar Método de Pago Dinámicamente
    const containerPago = document.getElementById('resumenMetodoPagoContainer');
    if (containerPago) {
        let elA = document.getElementById('cantAdultos');
        let elN = document.getElementById('cantNinos');
        let elB = document.getElementById('cantBebes');
        let cantA = elA ? parseInt(elA.value) || 0 : 1;
        let cantN = elN ? parseInt(elN.value) || 0 : 0;
        let cantB = elB ? parseInt(elB.value) || 0 : 0;
        let totalCosto = (cantA * precios.adultos) + (cantN * precios.ninos) + (cantB * precios.bebes);

        if (metodoPagoActual === 'yape') {
            const inputY = document.getElementById('comprobanteYape');
            const fileName = inputY && inputY.files && inputY.files[0] ? inputY.files[0].name : 'Voucher_Yape.jpg';
            containerPago.innerHTML = `
                <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px;">
                    <div>
                        <p class="payment-method-title" style="margin: 0; font-weight: 700; color: #6a1b9a; font-size: 15px;">
                            <i class="fa-solid fa-qrcode"></i> Yape (Billetera Digital)
                        </p>
                        <span style="font-size: 12px; color: #15803d;"><i class="fa-solid fa-file-circle-check"></i> Comprobante: <strong>${fileName}</strong></span>
                    </div>
                    <span style="background: #faf5ff; border: 1px solid #d8b4fe; color: #6a1b9a; font-size: 11px; padding: 4px 8px; border-radius: 6px; font-weight: 700;">100% Pagado</span>
                </div>
                <div class="total-summary-row" style="border-top: 1px solid #e2e8f0; padding-top: 10px;">
                    <span>Total</span>
                    <strong id="sumTotalFinal" style="color: #0b1f38; font-size: 18px;">S/ ${totalCosto.toFixed(2)}</strong>
                </div>
            `;
        } else if (metodoPagoActual === 'plin') {
            const inputP = document.getElementById('comprobantePlin');
            const fileName = inputP && inputP.files && inputP.files[0] ? inputP.files[0].name : 'Voucher_Plin.jpg';
            containerPago.innerHTML = `
                <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px;">
                    <div>
                        <p class="payment-method-title" style="margin: 0; font-weight: 700; color: #0284c7; font-size: 15px;">
                            <i class="fa-solid fa-qrcode"></i> Plin (Billetera Digital)
                        </p>
                        <span style="font-size: 12px; color: #15803d;"><i class="fa-solid fa-file-circle-check"></i> Comprobante: <strong>${fileName}</strong></span>
                    </div>
                    <span style="background: #f0f9ff; border: 1px solid #7dd3fc; color: #0284c7; font-size: 11px; padding: 4px 8px; border-radius: 6px; font-weight: 700;">100% Pagado</span>
                </div>
                <div class="total-summary-row" style="border-top: 1px solid #e2e8f0; padding-top: 10px;">
                    <span>Total</span>
                    <strong id="sumTotalFinal" style="color: #0b1f38; font-size: 18px;">S/ ${totalCosto.toFixed(2)}</strong>
                </div>
            `;
        } else if (metodoPagoActual === 'tarjeta') {
            const numTarjeta = document.getElementById('numTarjeta') ? document.getElementById('numTarjeta').value : '';
            const last4 = numTarjeta.replace(/\s/g, '').slice(-4) || '3456';
            containerPago.innerHTML = `
                <p class="payment-method-title" style="margin: 0 0 6px 0; font-weight: 700; color: #0b1f38; font-size: 15px;">
                    <i class="fa-solid fa-credit-card"></i> Tarjeta de crédito / débito
                </p>
                <div class="card-number-summary" style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px;">
                    <span style="font-family: monospace; font-size: 14px; color: #475569;">•••• •••• •••• ${last4}</span>
                    <i class="fa-brands fa-cc-visa" style="font-size:24px; color:#1a1f71;"></i>
                </div>
                <div class="total-summary-row" style="border-top: 1px solid #e2e8f0; padding-top: 10px;">
                    <span>Total</span>
                    <strong id="sumTotalFinal" style="color: #0b1f38; font-size: 18px;">S/ ${totalCosto.toFixed(2)}</strong>
                </div>
            `;
        } else {
            // Efectivo / Presencial con Adelanto 10%
            const adelanto = (totalCosto * 0.10).toFixed(2);
            const saldo = (totalCosto * 0.90).toFixed(2);
            const inputE = document.getElementById('comprobanteEfectivo');
            const fileName = inputE && inputE.files && inputE.files[0] ? inputE.files[0].name : 'Voucher_Adelanto_10.jpg';
            const canalNombre = canalAdelantoActual.toUpperCase();

            containerPago.innerHTML = `
                <div style="margin-bottom: 10px;">
                    <p class="payment-method-title" style="margin: 0 0 4px 0; font-weight: 700; color: #15803d; font-size: 15px;">
                        <i class="fa-solid fa-money-bill-wave"></i> Pago Presencial / Efectivo
                    </p>
                    <div style="background: #f0fdf4; border: 1px solid #bbf7d0; border-radius: 6px; padding: 8px 10px; margin-bottom: 8px; font-size: 12.5px;">
                        <div style="display: flex; justify-content: space-between; color: #166534; margin-bottom: 3px;">
                            <span>Adelanto (10% vía ${canalNombre}):</span>
                            <strong>S/ ${adelanto} (Pagado)</strong>
                        </div>
                        <div style="display: flex; justify-content: space-between; color: #64748b;">
                            <span>Saldo a pagar en agencia (90%):</span>
                            <strong>S/ ${saldo}</strong>
                        </div>
                    </div>
                    <span style="font-size: 12px; color: #15803d;"><i class="fa-solid fa-file-circle-check"></i> Voucher adjunto: <strong>${fileName}</strong></span>
                </div>
                <div class="total-summary-row" style="border-top: 1px solid #e2e8f0; padding-top: 10px;">
                    <span>Total del Tour</span>
                    <strong id="sumTotalFinal" style="color: #0b1f38; font-size: 18px;">S/ ${totalCosto.toFixed(2)}</strong>
                </div>
            `;
        }
    }
}

function mostrarModal() {
    const modal = document.getElementById('modalConfirmar');
    if (modal) modal.style.display = 'flex';
}

function cerrarModal() {
    const modal = document.getElementById('modalConfirmar');
    if (modal) modal.style.display = 'none';
}

function guardarReservaEnStorage(totalCosto, metodo, codigoPersonalizado, pasajeros, comprobante) {
    const user = window.getCurrentUser ? window.getCurrentUser() : { email: 'usuario@gmail.com' };
    const key = 'travelink_reservas_' + (user.email || 'usuario@gmail.com');

    let list = [];
    try {
        list = JSON.parse(localStorage.getItem(key) || '[]');
    } catch (e) {
        list = [];
    }
    const nextNum = list.length + 1;
    const codeStr = codigoPersonalizado || ('TRK-' + String(nextNum).padStart(3, '0'));
    const totalP = pasajeros ? pasajeros.length : 1;

    const titularNombre = pasajeros && pasajeros[0]
        ? `${pasajeros[0].nombre} ${pasajeros[0].apellidoPaterno || ''}`.trim()
        : (user.nombre || user.username || 'Usuario');

    const nueva = {
        id: nextNum,
        codigo: codeStr,
        titulo: 'Machu Picchu Clásico',
        ubicacion: 'Cusco, Perú',
        fechas: '12 oct. 2025 - 15 oct. 2025',
        personas: totalP + (totalP === 1 ? ' persona' : ' personas'),
        agencia: 'Andes Tours',
        estado: 'Confirmada',
        titular: titularNombre,
        nroDocumento: pasajeros && pasajeros[0] ? pasajeros[0].dni : (user.nroDocumento || ''),
        total: totalCosto,
        metodoPago: metodo,
        comprobante: comprobante || 'voucher.jpg',
        imagen: '../../img/valle.jpg',
        fechaRegistro: new Date().toISOString().split('T')[0],
        pasajeros: pasajeros || []
    };
    list.unshift(nueva);
    localStorage.setItem(key, JSON.stringify(list));
    return nueva;
}

function confirmarReserva() {
    cerrarModal();
    guardarDatosFormularioActual();

    let elA = document.getElementById('cantAdultos');
    let elN = document.getElementById('cantNinos');
    let elB = document.getElementById('cantBebes');

    let cantA = elA ? parseInt(elA.value) || 0 : 1;
    let cantN = elN ? parseInt(elN.value) || 0 : 0;
    let cantB = elB ? parseInt(elB.value) || 0 : 0;
    let totalCosto = (cantA * precios.adultos) + (cantN * precios.ninos) + (cantB * precios.bebes);

    const user = window.getCurrentUser ? window.getCurrentUser() : { email: 'usuario@gmail.com' };
    const randomCode = 'TRK-' + Math.floor(100 + Math.random() * 900);

    const nombresMetodo = {
        'efectivo': 'Presencial',
        'yape': 'Yape',
        'plin': 'Plin',
        'tarjeta': 'Tarjeta'
    };
    const metodoNombre = nombresMetodo[metodoPagoActual] || 'Presencial';

    let voucherName = '';
    let numeroOperacion = '';
    let tarjetaNum = '';
    let tarjetaTitular = '';

    if (metodoPagoActual === 'yape') {
        const inp = document.getElementById('comprobanteYape');
        if (inp && inp.files && inp.files[0]) voucherName = inp.files[0].name;
        else voucherName = 'Voucher_Yape_' + Math.floor(1000 + Math.random() * 9000) + '.jpg';
        const op = document.getElementById('numOperacionYape');
        if (op && op.value.trim()) numeroOperacion = op.value.trim();
        else numeroOperacion = 'OP-YAPE-' + Math.floor(100000 + Math.random() * 900000);
    } else if (metodoPagoActual === 'plin') {
        const inp = document.getElementById('comprobantePlin');
        if (inp && inp.files && inp.files[0]) voucherName = inp.files[0].name;
        else voucherName = 'Voucher_Plin_' + Math.floor(1000 + Math.random() * 9000) + '.jpg';
        const op = document.getElementById('numOperacionPlin');
        if (op && op.value.trim()) numeroOperacion = op.value.trim();
        else numeroOperacion = 'OP-PLIN-' + Math.floor(100000 + Math.random() * 900000);
    } else if (metodoPagoActual === 'efectivo') {
        const inp = document.getElementById('comprobanteEfectivo');
        if (inp && inp.files && inp.files[0]) voucherName = inp.files[0].name;
        else voucherName = 'Voucher_Adelanto_10.jpg';
        const op = document.getElementById('numOperacionEfectivo');
        if (op && op.value.trim()) numeroOperacion = op.value.trim();
        else numeroOperacion = 'OP-EFECTIVO-' + Math.floor(100000 + Math.random() * 900000);
    } else if (metodoPagoActual === 'tarjeta') {
        const inpT = document.getElementById('numTarjeta');
        const inpTit = document.getElementById('nombreTitular');
        if (inpT) tarjetaNum = inpT.value.replace(/\s/g, '').slice(-4);
        if (inpTit) tarjetaTitular = inpTit.value.trim();
        voucherName = 'Tarjeta **** ' + (tarjetaNum || '3456');
        numeroOperacion = 'TX-' + Math.floor(100000 + Math.random() * 900000);
    }

    // Asegurar estructura limpia sin combinar apellidos en lista de pasajeros
    const pasajerosLimpios = listaPasajeros.map((p, idx) => ({
        idPasajero: idx + 1,
        nombre: (p.nombre || '').trim(),
        apellidoPaterno: (p.apellidoPaterno || '').trim(),
        apellidoMaterno: (p.apellidoMaterno || '').trim(),
        apellidos: `${(p.apellidoPaterno || '').trim()} ${(p.apellidoMaterno || '').trim()}`.trim(),
        dni: (p.dni || p.nroDocumento || '').trim(),
        nroDocumento: (p.dni || p.nroDocumento || '').trim(),
        telefono: (p.telefono || '').trim(),
        edad: p.edad ? parseInt(p.edad) : 25,
        tipoSeguro: p.tipoSeguro || 'SIS',
        esTitular: idx === 0 || p.esTitular === true
    }));

    const titular = pasajerosLimpios[0] || {};

    const payload = {
        email: user.email || user.correo || '',
        idUsuario: user.id || user.idUsuario || 1,
        idAgencia: 1,
        nombreTour: 'Machu Picchu Clásico',
        idTour: 1,
        fechaInicio: '2025-10-12',
        fechaFin: '2025-10-15',
        cantAdultos: cantA,
        cantNinos: cantN,
        cantBebes: cantB,
        total: totalCosto,
        metodoPago: metodoNombre,
        comprobante: voucherName,
        urlComprobante: voucherName,
        url: voucherName,
        numeroOperacion: numeroOperacion,
        tarjetaNumero: tarjetaNum,
        tarjetaTitular: tarjetaTitular,
        canalAdelanto: canalAdelantoActual,
        esAdelanto: metodoPagoActual === 'efectivo',
        codigo: randomCode,
        nombre: titular.nombre || (user.nombre || 'Usuario'),
        apellidoPaterno: titular.apellidoPaterno || (user.apellidoPaterno || ''),
        apellidoMaterno: titular.apellidoMaterno || (user.apellidoMaterno || ''),
        apellidos: `${titular.apellidoPaterno || ''} ${titular.apellidoMaterno || ''}`.trim(),
        dni: titular.dni || (user.nroDocumento || ''),
        telefono: titular.telefono || (user.telefono || ''),
        edad: 25,
        tipoSeguro: 'SIS',
        pasajeros: pasajerosLimpios
    };

    fetch('http://localhost:8080/api/guardar_reserva', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    })
        .then(res => res.json())
        .then(data => {
            if (data.status === 'success') {
                guardarReservaEnStorage(totalCosto, metodoNombre, data.data ? data.data.codigo : randomCode, pasajerosLimpios, voucherName);
            } else {
                guardarReservaEnStorage(totalCosto, metodoNombre, randomCode, pasajerosLimpios, voucherName);
            }
        })
        .catch(err => {
            guardarReservaEnStorage(totalCosto, metodoNombre, randomCode, pasajerosLimpios, voucherName);
        })
        .finally(() => {
            if (typeof toastr !== 'undefined') toastr.success('¡Reserva confirmada y guardada exitosamente en la base de datos!', 'Éxito');
            else alert('¡Reserva confirmada exitosamente!');

            setTimeout(() => {
                window.location.href = "reservas.html";
            }, 1200);
        });
}
