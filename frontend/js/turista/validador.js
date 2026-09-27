// Travelink - Validador Universal de Formularios y Reglas de Negocio

function isDNIValido(dni) {
    return /^\d{8}$/.test(String(dni).trim());
}

function isTelefonoPeruano(tel) {
    return /^9\d{8}$/.test(String(tel).trim());
}

function isNombreValido(nombre) {
    return /^[a-zA-ZáéíóúÁÉÍÓÚñÑ\s]{2,40}$/.test(String(nombre).trim());
}

function isEmailValido(email) {
    const regExp = /^(([^<>()[\]\\.,;:\s@"]+(\.[^<>()[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
    return regExp.test(String(email).trim()) && String(email).length <= 50;
}

function isUsuarioValido(u) {
    return /^[a-zA-Z0-9._-]{3,25}$/.test(String(u).trim());
}

// Formateador y filtro de entrada en tiempo real
function aplicarRestriccionesEnTiempoReal(context) {
    const root = context || document;

    // 1. DNI: Solo números, máx 8 dígitos
    root.querySelectorAll('input[id*="Documento"], input[id*="DNI"], input[id*="dni"], .input-p-dni').forEach(input => {
        input.setAttribute('maxlength', '8');
        input.addEventListener('input', function () {
            this.value = this.value.replace(/\D/g, '').slice(0, 8);
        });
    });

    // 2. Nombres y Apellidos: Solo letras y espacios, máx 40 caracteres
    root.querySelectorAll('input[id*="Nombre"], input[id*="nombre"], input[id*="Apellido"], input[id*="Paterno"], input[id*="Materno"], input[id*="paterno"], input[id*="materno"], .input-p-nombre, .input-p-paterno, .input-p-materno').forEach(input => {
        input.setAttribute('maxlength', '40');
        input.addEventListener('input', function () {
            this.value = this.value.replace(/[^a-zA-ZáéíóúÁÉÍÓÚñÑ\s]/g, '').slice(0, 40);
        });
    });

    // 3. Teléfonos: Solo números, máx 9 dígitos
    root.querySelectorAll('input[type="tel"], input[id*="telefono"], input[id*="Telefono"], .input-p-telefono').forEach(input => {
        input.setAttribute('maxlength', '9');
        input.addEventListener('input', function () {
            this.value = this.value.replace(/\D/g, '').slice(0, 9);
        });
    });

    // 4. Correos: Máximo 50 caracteres
    root.querySelectorAll('input[type="email"], input[id*="email"], input[id*="correo"]').forEach(input => {
        input.setAttribute('maxlength', '50');
    });

    // 5. Nombres de usuario: Letras, números, guiones y puntos (sin espacios)
    root.querySelectorAll('input[id*="username"], input[id*="usuario"], input[id*="Usuario"]').forEach(input => {
        input.setAttribute('maxlength', '25');
        input.addEventListener('input', function () {
            this.value = this.value.replace(/[^a-zA-Z0-9._-]/g, '').slice(0, 25);
        });
    });
}

document.addEventListener('DOMContentLoaded', () => {
    aplicarRestriccionesEnTiempoReal();
});
