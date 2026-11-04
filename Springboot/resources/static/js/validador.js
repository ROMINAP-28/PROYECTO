// ==========================================================
// TRAVELINK - VALIDADOR GLOBAL UNIVERSAL (validador.js)
// Aplica en todos los roles: Turista, Agencia y Administrador.
// Funciona en formularios estáticos y modales / campos dinámicos.
// ==========================================================

(function (window, document) {
    'use strict';

    // Algoritmo de Luhn para validación de tarjetas de crédito/débito
    function validarLuhn(numero) {
        const digitos = String(numero).replace(/\D/g, '');
        if (digitos.length < 13 || digitos.length > 19) return false;
        let suma = 0;
        let par = false;
        for (let i = digitos.length - 1; i >= 0; i--) {
            let n = parseInt(digitos.charAt(i), 10);
            if (par) {
                n *= 2;
                if (n > 9) n -= 9;
            }
            suma += n;
            par = !par;
        }
        return suma % 10 === 0;
    }

    // Reglas de validación estándar en español
    const Reglas = {
        requerido: val => val !== null && val !== undefined && String(val).trim() !== '',
        email: val => !val || /^(([^<>()[\]\\.,;:\s@"]+(\.[^<>()[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/.test(String(val).trim()),
        dni: val => !val || /^\d{8}$/.test(String(val).trim()),
        telefono: val => !val || /^9\d{8}$/.test(String(val).trim()),
        ruc: val => !val || /^(10|20)\d{9}$/.test(String(val).trim()),
        texto: val => !val || /^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\s]{2,80}$/.test(String(val).trim()),
        usuario: val => !val || /^[a-zA-Z0-9._-]{3,40}$/.test(String(val).trim()),
        usuarioCorreo: val => {
            if (!val) return true;
            const s = String(val).trim();
            if (s.includes('@')) {
                return Reglas.email(s);
            }
            return Reglas.usuario(s);
        },
        password: val => !val || String(val).length >= 6,
        numero: val => !val || (!isNaN(parseFloat(val)) && isFinite(val) && parseFloat(val) > 0),
        entero: val => !val || (/^\d+$/.test(String(val).trim()) && parseInt(val, 10) >= 0),
        decimal: val => !val || (!isNaN(parseFloat(val)) && parseFloat(val) >= 0),
        precio: val => !val || (!isNaN(parseFloat(val)) && parseFloat(val) > 0),
        url: val => {
            if (!val) return true;
            try {
                const u = new URL(val);
                return u.protocol === 'http:' || u.protocol === 'https:';
            } catch (_) {
                return false;
            }
        },
        fecha: (val, noPasada = true) => {
            if (!val) return true;
            const d = new Date(val + 'T00:00:00');
            if (isNaN(d.getTime())) return false;
            if (noPasada) {
                const hoy = new Date();
                hoy.setHours(0, 0, 0, 0);
                return d >= hoy;
            }
            return true;
        },
        select: val => {
            if (!val) return false;
            const s = String(val).trim().toLowerCase();
            return s !== '' && s !== '0' && !s.startsWith('seleccione') && s !== 'placeholder' && s !== 'null';
        },
        tarjeta: val => !val || validarLuhn(val),
        cvv: val => !val || /^\d{3,4}$/.test(String(val).trim()),
        vencimiento: val => {
            if (!val) return true;
            const match = String(val).trim().match(/^(\d{2})\/?(\d{2}|\d{4})$/);
            if (!match) return false;
            const mes = parseInt(match[1], 10);
            let anio = parseInt(match[2], 10);
            if (mes < 1 || mes > 12) return false;
            if (anio < 100) anio += 2000;
            const ahora = new Date();
            const finMes = new Date(anio, mes, 0, 23, 59, 59);
            return finMes >= ahora;
        }
    };

    function deducirTipoValidacion(campo) {
        const custom = campo.getAttribute('data-validar');
        if (custom) return custom.toLowerCase();

        const tag = campo.tagName.toLowerCase();
        const type = (campo.getAttribute('type') || '').toLowerCase();
        const idName = ((campo.id || '') + ' ' + (campo.name || '') + ' ' + (campo.placeholder || '')).toLowerCase();

        let labelText = '';
        if (campo.labels && campo.labels.length > 0) {
            labelText = Array.from(campo.labels).map(l => l.textContent).join(' ').toLowerCase();
        } else {
            const group = campo.closest('.form-group, .auth-field, .mb-3, .field, .input-box');
            if (group) {
                const lbl = group.querySelector('label');
                if (lbl) labelText = lbl.textContent.toLowerCase();
            }
        }
        const fullContext = (idName + ' ' + labelText).toLowerCase();

        if (tag === 'select') return 'select';
        if (tag === 'textarea') return 'textarea';

        // 1. Campo para Usuario O Correo (login general, admin, agencia, turista)
        const esFormLogin = (campo.form && (campo.form.id || '').toLowerCase().includes('login')) ||
                            window.location.pathname.toLowerCase().includes('login');

        if (
            fullContext.includes('usuariocorreo') ||
            fullContext.includes('usuario_correo') ||
            fullContext.includes('loginusuario') ||
            fullContext.includes('adminusuario') ||
            (fullContext.includes('usuario') && (fullContext.includes('correo') || fullContext.includes('email'))) ||
            (fullContext.includes('user') && (fullContext.includes('email') || fullContext.includes('mail'))) ||
            (esFormLogin && (fullContext.includes('usuario') || fullContext.includes('login') || fullContext.includes('user') || fullContext.includes('correo') || fullContext.includes('email')))
        ) {
            return 'usuario-correo';
        }

        if (type === 'email' || idName.includes('correo') || idName.includes('email')) return 'email';
        if (idName.includes('dni') || idName.includes('documento')) return 'dni';
        if (idName.includes('ruc')) return 'ruc';
        if (type === 'tel' || idName.includes('telefono') || idName.includes('celular')) return 'telefono';
        if (type === 'password' || idName.includes('clave') || idName.includes('contrasena') || idName.includes('password')) return 'password';
        if (idName.includes('usuario') || idName.includes('username') || idName.includes('login')) return 'usuario';
        if (idName.includes('precio') || idName.includes('monto') || idName.includes('tarifa') || idName.includes('costo')) return 'precio';
        if (type === 'date' || idName.includes('fecha')) return 'fecha';
        if (idName.includes('tarjeta') || idName.includes('cardnumber')) return 'tarjeta';
        if (idName.includes('cvv') || idName.includes('cvc')) return 'cvv';
        if (idName.includes('vencimiento') || idName.includes('expira')) return 'vencimiento';
        if (type === 'file') return 'archivo';
        if (type === 'number') return 'numero';
        if (idName.includes('nombre') || idName.includes('apellido') || idName.includes('ciudad') || idName.includes('pais')) return 'texto';

        return 'texto';
    }

    function esRequerido(campo) {
        if (campo.hasAttribute('required') || campo.getAttribute('data-requerido') === 'true') return true;
        const dv = campo.getAttribute('data-validar') || '';
        return dv.includes('requerido');
    }

    function limpiarErrorCampo(campo) {
        if (!campo) return;
        campo.classList.remove('validador-invalido', 'campo-vacio', 'novalido', 'valido');
        const formGroup = campo.closest('.form-group, .auth-field, .mb-3, .field, .input-box, .col, .col-12, .col-md-6, form');
        if (formGroup) {
            formGroup.querySelectorAll('.validador-error-msg, .error-msg').forEach(el => el.remove());
        }
        if (campo.parentElement) {
            campo.parentElement.querySelectorAll('.validador-error-msg, .error-msg').forEach(el => el.remove());
        }
        const next = campo.nextElementSibling;
        if (next && (next.classList.contains('validador-error-msg') || next.classList.contains('error-msg'))) {
            next.remove();
        }
    }

    function mostrarErrorCampo(campo, mensaje, esVacio = false) {
        if (!campo) return;
        limpiarErrorCampo(campo);

        if (esVacio) {
            campo.classList.add('campo-vacio');
        } else {
            campo.classList.add('validador-invalido', 'novalido');
        }

        const msgDiv = document.createElement('div');
        msgDiv.className = 'validador-error-msg' + (esVacio ? ' msg-vacio' : '');
        msgDiv.innerHTML = `<i class="fa-solid fa-${esVacio ? 'triangle-exclamation' : 'circle-exclamation'}"></i> <span>${mensaje}</span>`;

        // Ubicar SIEMPRE DEBAJO del campo (nunca al costado dentro del flex del input)
        const wrapper = campo.closest('.input-wrapper, .input-password-wrap, .password-field, .input-group');
        const formGroup = campo.closest('.form-group, .auth-field, .mb-3, .field, .input-box');

        if (wrapper) {
            wrapper.insertAdjacentElement('afterend', msgDiv);
        } else if (formGroup) {
            formGroup.appendChild(msgDiv);
        } else {
            campo.insertAdjacentElement('afterend', msgDiv);
        }
    }

    function marcarCampoValido(campo) {
        if (!campo) return;
        limpiarErrorCampo(campo);
        campo.classList.add('validador-valido', 'valido');
    }

    // Validador de un campo individual
    function validarCampo(campo) {
        if (!campo || campo.disabled || campo.type === 'hidden') return true;

        // Si es un campo readonly (p.ej. datos del titular precargados), se considera válido
        if (campo.readOnly && campo.value && campo.value.trim() !== '') {
            marcarCampoValido(campo);
            return true;
        }

        const tag = campo.tagName.toLowerCase();
        const type = (campo.getAttribute('type') || '').toLowerCase();
        const requerido = esRequerido(campo);
        const tipoRegla = deducirTipoValidacion(campo);
        const mensajePersonalizado = campo.getAttribute('data-mensaje');

        // 1. Checkbox obligatorio (p.ej. términos)
        if (type === 'checkbox') {
            if (requerido && !campo.checked) {
                mostrarErrorCampo(campo, mensajePersonalizado || 'Debe marcar esta casilla para continuar.', true);
                return false;
            }
            marcarCampoValido(campo);
            return true;
        }

        // 2. Radio button group
        if (type === 'radio') {
            if (requerido && campo.name) {
                const checked = document.querySelector(`input[name="${campo.name}"]:checked`);
                if (!checked) {
                    mostrarErrorCampo(campo, mensajePersonalizado || 'Seleccione una opción.', true);
                    return false;
                }
            }
            marcarCampoValido(campo);
            return true;
        }

        // 3. File upload
        if (type === 'file') {
            const files = campo.files;
            if (requerido && (!files || files.length === 0)) {
                mostrarErrorCampo(campo, mensajePersonalizado || 'Debe seleccionar un archivo.', true);
                return false;
            }
            if (files && files.length > 0) {
                const f = files[0];
                const maxBytes = 10 * 1024 * 1024; // 10 MB
                if (f.size > maxBytes) {
                    mostrarErrorCampo(campo, 'El archivo no debe superar 10 MB.');
                    return false;
                }
                const ext = (f.name.split('.').pop() || '').toLowerCase();
                const mime = (f.type || '').toLowerCase();
                const acceptAttr = (campo.getAttribute('accept') || '').toLowerCase().trim();

                if (acceptAttr) {
                    const tokens = acceptAttr.split(',').map(t => t.trim().toLowerCase());
                    let aceptado = false;
                    for (const token of tokens) {
                        if (token === '*/*' || token === '' || token === '*') {
                            aceptado = true;
                            break;
                        }
                        if (token.startsWith('image/')) {
                            if (['jpg', 'jpeg', 'png', 'webp', 'gif', 'svg', 'avif', 'bmp', 'jfif'].includes(ext) || mime.startsWith('image/')) {
                                aceptado = true;
                                break;
                            }
                        } else if (token.startsWith('.')) {
                            const extToken = token.slice(1);
                            if (extToken === ext || (extToken === 'jpg' && ext === 'jpeg') || (extToken === 'jpeg' && ext === 'jpg')) {
                                aceptado = true;
                                break;
                            }
                        } else if (token.includes('/')) {
                            if (mime === token) {
                                aceptado = true;
                                break;
                            }
                            if (token === 'application/pdf' && ext === 'pdf') {
                                aceptado = true;
                                break;
                            }
                            if (token.endsWith('/*')) {
                                const prefix = token.split('/')[0];
                                if (mime.startsWith(prefix + '/')) {
                                    aceptado = true;
                                    break;
                                }
                            }
                        } else {
                            if (token === ext || (token === 'jpg' && ext === 'jpeg') || (token === 'jpeg' && ext === 'jpg')) {
                                aceptado = true;
                                break;
                            }
                        }
                    }
                    if (!aceptado) {
                        mostrarErrorCampo(campo, `Formato no permitido (.${ext}). Permitidos: JPG, PNG, WEBP.`);
                        return false;
                    }
                } else {
                    const extensionesPermitidas = ['jpg', 'jpeg', 'png', 'webp', 'gif', 'svg', 'pdf', 'csv', 'xlsx', 'xls', 'docx', 'doc'];
                    if (!extensionesPermitidas.includes(ext) && !mime.startsWith('image/')) {
                        mostrarErrorCampo(campo, `Formato no permitido (.${ext}). Permitidos: JPG, PNG, WEBP, PDF.`);
                        return false;
                    }
                }
            }
            marcarCampoValido(campo);
            return true;
        }

        const valor = campo.value !== undefined ? String(campo.value).trim() : '';

        // 4. Verificación de campo vacío
        if (requerido && valor === '') {
            mostrarErrorCampo(campo, mensajePersonalizado || 'Este campo es obligatorio.', true);
            return false;
        }

        // Si no es obligatorio y está vacío, es válido
        if (!requerido && valor === '') {
            limpiarErrorCampo(campo);
            return true;
        }

        // 5. Validación por tipo de regla
        switch (tipoRegla) {
            case 'select':
                if (!Reglas.select(campo.value)) {
                    mostrarErrorCampo(campo, mensajePersonalizado || 'Seleccione una opción válida.', true);
                    return false;
                }
                break;

            case 'email':
                {
                    const esFormLogin = (campo.form && (campo.form.id || '').toLowerCase().includes('login')) ||
                                        window.location.pathname.toLowerCase().includes('login') ||
                                        (campo.id || '').toLowerCase().includes('usuario');
                    if (valor && !valor.includes('@') && esFormLogin) {
                        if (Reglas.usuario(valor)) {
                            break; // Es un nombre de usuario válido en login, admitirlo
                        }
                    }
                    if (!Reglas.email(valor)) {
                        mostrarErrorCampo(campo, mensajePersonalizado || 'Ingrese un correo electrónico válido (ej: usuario@correo.com).');
                        return false;
                    }
                }
                break;

            case 'dni':
                if (!Reglas.dni(valor)) {
                    mostrarErrorCampo(campo, mensajePersonalizado || 'El DNI debe contener exactamente 8 dígitos numéricos.');
                    return false;
                }
                break;

            case 'telefono':
                if (!Reglas.telefono(valor)) {
                    mostrarErrorCampo(campo, mensajePersonalizado || 'El teléfono debe iniciar con 9 y contener 9 dígitos.');
                    return false;
                }
                break;

            case 'ruc':
                if (!Reglas.ruc(valor)) {
                    mostrarErrorCampo(campo, mensajePersonalizado || 'El RUC debe iniciar con 10 o 20 y contener 11 dígitos.');
                    return false;
                }
                break;

            case 'texto':
                if (!Reglas.texto(valor)) {
                    mostrarErrorCampo(campo, mensajePersonalizado || 'Solo se permiten letras, tildes y espacios (sin números).');
                    return false;
                }
                break;

            case 'usuario':
                if (!Reglas.usuario(valor)) {
                    mostrarErrorCampo(campo, mensajePersonalizado || 'Usuario debe tener entre 3 y 40 caracteres sin espacios.');
                    return false;
                }
                break;

            case 'usuario-correo':
                if (!Reglas.usuarioCorreo(valor)) {
                    mostrarErrorCampo(campo, mensajePersonalizado || 'Ingrese un usuario válido o correo electrónico.');
                    return false;
                }
                break;

            case 'password':
                if (!Reglas.password(campo.value)) {
                    mostrarErrorCampo(campo, mensajePersonalizado || 'La contraseña debe contener al menos 6 caracteres.');
                    return false;
                }
                break;

            case 'numero':
            case 'precio':
                if (!Reglas.numero(valor)) {
                    mostrarErrorCampo(campo, mensajePersonalizado || 'Debe ser un número mayor a 0.');
                    return false;
                }
                break;

            case 'entero':
                if (!Reglas.entero(valor)) {
                    mostrarErrorCampo(campo, mensajePersonalizado || 'Debe ser un número entero válido (≥ 0).');
                    return false;
                }
                break;

            case 'fecha':
                const permitirPasada = campo.getAttribute('data-permitir-pasadas') === 'true' || campo.id.includes('nacimiento');
                if (!Reglas.fecha(valor, !permitirPasada)) {
                    mostrarErrorCampo(campo, mensajePersonalizado || 'Fecha no válida o anterior a la fecha actual.');
                    return false;
                }
                break;

            case 'tarjeta':
                if (!Reglas.tarjeta(valor)) {
                    mostrarErrorCampo(campo, mensajePersonalizado || 'Número de tarjeta inválido (algoritmo Luhn).');
                    return false;
                }
                break;

            case 'cvv':
                if (!Reglas.cvv(valor)) {
                    mostrarErrorCampo(campo, mensajePersonalizado || 'CVV inválido (3 o 4 dígitos).');
                    return false;
                }
                break;

            case 'vencimiento':
                if (!Reglas.vencimiento(valor)) {
                    mostrarErrorCampo(campo, mensajePersonalizado || 'Fecha de vencimiento inválida o expirada (MM/AA).');
                    return false;
                }
                break;

            case 'textarea':
                const minLen = parseInt(campo.getAttribute('minlength') || '5', 10);
                if (valor.length < minLen) {
                    mostrarErrorCampo(campo, mensajePersonalizado || `Debe ingresar al menos ${minLen} caracteres.`);
                    return false;
                }
                break;

            default:
                break;
        }

        // 6. Confirmación de campo (data-confirmar="#idOtroCampo")
        const targetSelector = campo.getAttribute('data-confirmar');
        if (targetSelector) {
            const otro = document.querySelector(targetSelector);
            if (otro && otro.value !== campo.value) {
                mostrarErrorCampo(campo, mensajePersonalizado || 'Los valores no coinciden.');
                return false;
            }
        }

        // 7. Límites de longitud
        const maxLen = campo.getAttribute('maxlength');
        if (maxLen && valor.length > parseInt(maxLen, 10)) {
            mostrarErrorCampo(campo, `Máximo ${maxLen} caracteres permitidos.`);
            return false;
        }

        marcarCampoValido(campo);
        return true;
    }

    // Validador maestro de un formulario completo
    function validarFormulario(formulario) {
        if (!formulario) return true;

        // Soporte retrocompatible si se pasa un objeto de configuración { noempty: [], ... }
        if (typeof formulario === 'object' && !(formulario instanceof Element)) {
            return validarFormularioPorConfig(formulario);
        }

        const formEl = typeof formulario === 'string' ? document.querySelector(formulario) : formulario;
        if (!formEl) return true;

        const campos = formEl.querySelectorAll('input:not([type="hidden"]):not([type="submit"]):not([type="button"]), select, textarea');
        let esValido = true;
        let primerInvalido = null;

        campos.forEach(campo => {
            const ok = validarCampo(campo);
            if (!ok) {
                esValido = false;
                if (!primerInvalido) primerInvalido = campo;
            }
        });

        if (!esValido && primerInvalido) {
            primerInvalido.scrollIntoView({ behavior: 'smooth', block: 'center' });
            setTimeout(() => {
                try { primerInvalido.focus(); } catch (_) {}
            }, 250);
        }

        return esValido;
    }

    // Retrocompatibilidad con el validador antiguo por config
    function validarFormularioPorConfig(config) {
        let esValido = true;
        let primerInvalido = null;

        function checkLista(lista, tipo) {
            if (!lista) return;
            lista.forEach(ref => {
                const el = typeof ref === 'string' ? (document.getElementById(ref) || document.querySelector(ref)) : ref;
                if (!el) return;
                if (tipo && !el.getAttribute('data-validar')) el.setAttribute('data-validar', tipo);
                if (!el.hasAttribute('required')) el.setAttribute('required', 'required');
                const ok = validarCampo(el);
                if (!ok) {
                    esValido = false;
                    if (!primerInvalido) primerInvalido = el;
                }
            });
        }

        checkLista(config.noempty || config.vacio, 'texto');
        checkLista(config.select, 'select');
        checkLista(config.dni, 'dni');
        checkLista(config.ruc, 'ruc');
        checkLista(config.email || config.correo, 'email');
        checkLista(config.telefono || config.tel, 'telefono');
        checkLista(config.nombre || config.nombres, 'texto');
        checkLista(config.password, 'password');
        checkLista(config.numero || config.precio || config.cupo, 'numero');
        checkLista(config.fecha, 'fecha');
        checkLista(config.textarea, 'textarea');
        checkLista(config.archivo, 'archivo');

        if (config.passwordConfirm) {
            const elPass = typeof config.passwordConfirm.original === 'string' ? document.getElementById(config.passwordConfirm.original) : config.passwordConfirm.original;
            const elConf = typeof config.passwordConfirm.confirm === 'string' ? document.getElementById(config.passwordConfirm.confirm) : config.passwordConfirm.confirm;
            if (elConf && elPass) {
                if (elConf.value !== elPass.value) {
                    mostrarErrorCampo(elConf, 'Las contraseñas no coinciden.');
                    esValido = false;
                    if (!primerInvalido) primerInvalido = elConf;
                }
            }
        }

        if (!esValido && primerInvalido) {
            try { primerInvalido.focus(); } catch (_) {}
        }

        return esValido;
    }

    function limpiarFormulario(formulario) {
        const formEl = typeof formulario === 'string' ? document.querySelector(formulario) : formulario;
        if (!formEl) return;
        formEl.querySelectorAll('input, select, textarea').forEach(limpiarErrorCampo);
    }

    // Inicialización automática de todos los <form> en la página
    function inicializarFormularios() {
        document.querySelectorAll('form').forEach(form => {
            // Anular tooltips nativos del navegador
            form.setAttribute('novalidate', 'true');

            // Interceptar submit
            if (!form.dataset.validadorEnlazado) {
                form.dataset.validadorEnlazado = 'true';

                form.addEventListener('submit', function (e) {
                    const valido = validarFormulario(form);
                    if (!valido) {
                        e.preventDefault();
                        e.stopImmediatePropagation();
                        return false;
                    }

                    // Si es válido, deshabilitar botón y mostrar 'Procesando...' o 'Ingresando...'
                    const submitBtn = form.querySelector('button[type="submit"], input[type="submit"]');
                    if (submitBtn && !submitBtn.disabled) {
                        const esLogin = form.id.toLowerCase().includes('login') || window.location.pathname.toLowerCase().includes('login');
                        const textoOriginal = submitBtn.innerHTML || submitBtn.value;
                        submitBtn.dataset.textoOriginal = textoOriginal;
                        submitBtn.disabled = true;
                        submitBtn.classList.add('btn-procesando');
                        if (submitBtn.tagName.toLowerCase() === 'input') {
                            submitBtn.value = esLogin ? 'Ingresando...' : 'Procesando...';
                        } else {
                            submitBtn.innerHTML = `<i class="fa-solid fa-spinner fa-spin"></i> ${esLogin ? 'Ingresando...' : 'Procesando...'}`;
                        }

                        // Restaurar botón a los 10 segundos en caso de timeout
                        setTimeout(() => {
                            if (submitBtn && submitBtn.classList.contains('btn-procesando')) {
                                submitBtn.disabled = false;
                                submitBtn.classList.remove('btn-procesando');
                                if (submitBtn.dataset.textoOriginal) {
                                    if (submitBtn.tagName.toLowerCase() === 'input') {
                                        submitBtn.value = submitBtn.dataset.textoOriginal;
                                    } else {
                                        submitBtn.innerHTML = submitBtn.dataset.textoOriginal;
                                    }
                                }
                            }
                        }, 10000);
                    }
                }, true); // Use capture phase to validate before custom handlers
            }
        });
    }

    // Delegación de eventos para validación en tiempo real (blur e input)
    document.addEventListener('blur', function (e) {
        const target = e.target;
        if (target && target.form && (target.tagName === 'INPUT' || target.tagName === 'SELECT' || target.tagName === 'TEXTAREA')) {
            validarCampo(target);
        }
    }, true);

    document.addEventListener('input', function (e) {
        const target = e.target;
        if (target && (target.classList.contains('validador-invalido') || target.classList.contains('campo-vacio') || target.classList.contains('novalido'))) {
            validarCampo(target);
        }
    }, true);

    document.addEventListener('change', function (e) {
        const target = e.target;
        if (target && (target.tagName === 'SELECT' || target.type === 'file' || target.type === 'checkbox' || target.type === 'radio')) {
            validarCampo(target);
        }
    }, true);

    // MutationObserver para detectar formularios o campos agregados dinámicamente
    const observer = new MutationObserver(mutations => {
        let hayNuevosForms = false;
        mutations.forEach(m => {
            if (m.addedNodes.length > 0) hayNuevosForms = true;
        });
        if (hayNuevosForms) inicializarFormularios();
    });

    if (document.body) {
        observer.observe(document.body, { childList: true, subtree: true });
        inicializarFormularios();
    } else {
        document.addEventListener('DOMContentLoaded', () => {
            observer.observe(document.body, { childList: true, subtree: true });
            inicializarFormularios();
        });
    }

    // Exponer API Pública unificada
    const Validador = {
        validarFormulario: validarFormulario,
        validarCampo: validarCampo,
        limpiar: limpiarFormulario,
        mostrarError: mostrarErrorCampo,
        reglas: Reglas
    };

    window.Validador = Validador;

    // Métodos globales de retrocompatibilidad
    window.validarFormulario = validarFormulario;
    window.limpiarEstiloValidacion = limpiarErrorCampo;
    window.resaltarValido = marcarCampoValido;
    window.resaltarVacio = (el, msg) => mostrarErrorCampo(el, msg, true);
    window.resaltarNoValido = mostrarErrorCampo;
    window.resaltarFormatoIncorrecto = mostrarErrorCampo;

    window.isEmail = Reglas.email;
    window.isUsuario = Reglas.usuario;
    window.isUsuarioOCorreo = Reglas.usuarioCorreo;
    window.isTelefonoPeruano = Reglas.telefono;
    window.isDni = Reglas.dni;
    window.isRuc = Reglas.ruc;
    window.isSoloLetras = Reglas.texto;
    window.isSelectValido = Reglas.select;
    window.isNumeroPositivo = Reglas.numero;
    window.isFechaValida = Reglas.fecha;
    window.isUrlValida = Reglas.url;

})(window, document);
