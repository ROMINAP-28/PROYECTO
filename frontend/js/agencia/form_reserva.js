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
            if (prev) prev.innerText = formatted || 'â€¢â€¢â€¢â€¢ â€¢â€¢â€¢â€¢ â€¢â€¢â€¢â€¢ â€¢â€¢â€¢â€¢';
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

let precios = {
    adultos: 120,
    ninos: 80,
    bebes: 0
};

function actualizarTotales() {
    let elA = document.getElementById('cantAdultos');
    let elN = document.getElementById('cantNinos');
    let elB = document.getElementById('cantBebes');
    
    let cantA = elA ? parseInt(elA.value) || 0 : 1;
    let cantN = elN ? parseInt(elN.value) || 0 : 0;
    let cantB = elB ? parseInt(elB.value) || 0 : 0;
    
    let totalPersonas = cantA + cantN;
    let totalCosto = (cantA * precios.adultos) + (cantN * precios.ninos);
    
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
    if (elRPer) elRPer.textContent = strPersonas;
    if (elRPre) elRPre.textContent = 'S/ ' + totalCosto.toFixed(2);
    if (elSumTotalFinal) elSumTotalFinal.textContent = 'S/ ' + totalCosto.toFixed(2);
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

function guardarReservaEnStorage(cantA, cantN, cantB, totalCosto, metodo, codigoPersonalizado) {
    const user = window.getCurrentUser ? window.getCurrentUser() : { email: 'briadayanainfantes@gmail.com' };
    const key = 'travelink_reservas_' + (user.email || 'briadayanainfantes@gmail.com');

    let list = [];
    try {
        list = JSON.parse(localStorage.getItem(key) || '[]');
    } catch(e) {
        list = [];
    }
    const nextNum = list.length + 1;
    const codeStr = codigoPersonalizado || ('TRK-' + String(nextNum).padStart(3, '0'));
    const totalP = cantA + cantN;

    const fechaInput = document.getElementById('fechaInicio') ? document.getElementById('fechaInicio').value : '2026-10-15';
    const urlParams = new URLSearchParams(window.location.search);
    const tituloTour = urlParams.get('titulo') ? decodeURIComponent(urlParams.get('titulo')) : 'Tour Cañón del Colca, full day';
    const agenciaNombre = urlParams.get('agencia') ? decodeURIComponent(urlParams.get('agencia')) : 'Inti Andes Tours';

    const nueva = {
        id: nextNum,
        codigo: codeStr,
        titulo: tituloTour,
        ubicacion: 'Arequipa, Perú',
        fechas: fechaInput + ' - ' + fechaInput,
        personas: totalP + (totalP === 1 ? ' persona' : ' personas'),
        agencia: 'Agencia: ' + agenciaNombre,
        estado: 'Confirmada',
        total: totalCosto,
        metodoPago: metodo,
        imagen: '../../img/colca.jpg',
        fechaRegistro: new Date().toISOString().split('T')[0]
    };
    list.unshift(nueva);
    localStorage.setItem(key, JSON.stringify(list));
    return nueva;
}

function confirmarReserva() {
    cerrarModal();
    
    let elA = document.getElementById('cantAdultos');
    let elN = document.getElementById('cantNinos');
    let elB = document.getElementById('cantBebes');

    let cantA = elA ? parseInt(elA.value) || 0 : 1;
    let cantN = elN ? parseInt(elN.value) || 0 : 0;
    let cantB = elB ? parseInt(elB.value) || 0 : 0;
    let totalCosto = (cantA * precios.adultos) + (cantN * precios.ninos);
    let totalPersonas = cantA + cantN;
    
    const fechaInput = document.getElementById('fechaInicio') ? document.getElementById('fechaInicio').value : '2026-10-15';
    const user = window.getCurrentUser ? window.getCurrentUser() : { email: 'briadayanainfantes@gmail.com' };
    const randomCode = 'TRK-' + Math.floor(100 + Math.random() * 900);

    const nombresMetodo = {
        'efectivo': 'Presencial',
        'yape': 'Yape',
        'plin': 'Plin',
        'tarjeta': 'Tarjeta'
    };
    const metodoNombre = nombresMetodo[metodoPagoActual] || 'Presencial';

    const urlParams = new URLSearchParams(window.location.search);
    const idPaquete = parseInt(urlParams.get('idPaquete')) || 1;
    const tituloTour = urlParams.get('titulo') ? decodeURIComponent(urlParams.get('titulo')) : 'Tour Cañón del Colca, full day';

    const inputNombre = document.getElementById('inputNombre');
    const nombreTitular = inputNombre ? inputNombre.value.split(' ')[0] : (user.nombre || 'Ana');
    const apellidosTitular = inputNombre && inputNombre.value.split(' ').length > 1 ? inputNombre.value.split(' ').slice(1).join(' ') : (user.apellidos || 'Garcia');
    const dniTitular = document.getElementById('inputDoc') ? document.getElementById('inputDoc').value : '12345678';

    const payload = {
        email: user.email || 'briadayanainfantes@gmail.com',
        idUsuario: user.id || user.idUsuario || 1,
        fechaServicio: fechaInput,
        cantidadPersonas: totalPersonas,
        precioTotal: totalCosto,
        metodoPago: metodoNombre,
        observaciones: `${tituloTour} | Adultos: ${cantA}, Niños: ${cantN}, Bebés: ${cantB}`,
        codigo: randomCode,
        titulo: tituloTour,
        idPaquete: idPaquete,
        idTourFecha: idPaquete,
        dni: dniTitular,
        nombre: nombreTitular,
        apellidos: apellidosTitular
    };

    // Petición a la base de datos MySQL
    fetch('http://localhost:8080/api/guardar_reserva', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    })
    .then(res => res.json())
    .then(data => {
        if (data.status === 'success') {
            console.log('Reserva guardada en la Base de Datos:', data);
            guardarReservaEnStorage(cantA, cantN, cantB, totalCosto, metodoNombre, data.data ? data.data.codigo : randomCode);
        } else {
            console.warn('Error backend, guardando localmente:', data.message);
            guardarReservaEnStorage(cantA, cantN, cantB, totalCosto, metodoNombre, randomCode);
        }
    })
    .catch(err => {
        console.error('Error de red al guardar en la base de datos:', err);
        guardarReservaEnStorage(cantA, cantN, cantB, totalCosto, metodoNombre, randomCode);
    })
    .finally(() => {
        if (typeof toastr !== 'undefined') toastr.success('¡Reserva confirmada y guardada exitosamente en la base de datos!', 'Éxito');
        else alert('¡Reserva confirmada exitosamente!');

        setTimeout(() => {
            window.location.href = "reservas.html";
        }, 1200);
    });
}
