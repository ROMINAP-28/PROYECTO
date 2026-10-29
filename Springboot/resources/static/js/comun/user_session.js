// Travelink - Session & Dynamic Navigation Manager
// Requerimiento 6: La identidad la da exclusivamente /api/auth/me
// Prohibido almacenar identidad en localStorage o inventar nombres por defecto

(function () {
    // 1. Eliminar inmediatamente todas las claves de sesión obsoletas de localStorage
    const obsoleteKeys = [
        'travelink_user',
        'travelink_current_user',
        'admin_user',
        'agencia_user',
        'agenciasesión',
        'agenciaSesion',
        'travelink_session',
        'datosUsuario'
    ];
    obsoleteKeys.forEach(key => {
        try { localStorage.removeItem(key); } catch (e) {}
    });

    const SESSION_CACHE_KEY = 'travelink_auth_cache';
    let currentUser = null;
    let sessionInitialized = false;

    function getCurrentUser() {
        // Si ya está cargado en memoria
        if (currentUser) {
            return currentUser;
        }

        // Si todavía no, recuperar del sessionStorage
        try {
            const cache = sessionStorage.getItem('travelink_auth_cache');

            if (cache && cache !== 'guest') {
                currentUser = JSON.parse(cache);
                return currentUser;
            }
        } catch (error) {
            console.error('Error recuperando usuario:', error);
        }

        return null;
    }

    window.getCurrentUser = getCurrentUser;

// No usar el usuario guardado en sessionStorage como identidad inicial.
// Primero se debe validar la sesión real con /api/auth/me.
    currentUser = null;

    window.sessionReadyPromise = new Promise(resolve => {
        sessionReadyResolve = resolve;
    });

    window.onSessionReady = function (callback) {
        if (sessionInitialized && currentUser !== undefined) {
            callback(currentUser);
        } else if (currentUser) {
            callback(currentUser);
        } else {
            window.sessionReadyPromise.then(callback);
        }
    };

    window.getCurrentUser = function () {
        return currentUser;
    };

    window.isUserLoggedIn = function () {
        return currentUser !== null;
    };

    // 2. Cerrar sesión
    window.cerrarSesion = async function (e) {
        if (e && e.preventDefault) e.preventDefault();
        try {
            if (window.apiFetch) {
                await window.apiFetch('/api/auth/logout', { method: 'POST' });
            } else {
                await fetch('/api/auth/logout', { method: 'POST', credentials: 'include' });
            }
        } catch (err) {
            console.warn('Error cerrando sesión:', err);
        }
        currentUser = null;
        try {
            sessionStorage.removeItem(SESSION_CACHE_KEY);
            sessionStorage.clear();
        } catch (e) {}
        obsoleteKeys.forEach(key => {
            try { localStorage.removeItem(key); } catch (ex) {}
        });
        window.location.href = '/index.html';
    };

    // 3. Validación de rutas protegidas
    window.validarRutaProtegida = function () {
        const path = window.location.pathname.toLowerCase();

        // Panel de Administrador
        if (path.includes('/html/administrador/')) {
            if (!path.endsWith('login.html')) {
                if (!currentUser || currentUser.rol !== 'ROLE_ADMIN') {
                    window.location.replace('/html/administrador/login.html');
                    return false;
                }
            } else if (currentUser && currentUser.rol === 'ROLE_ADMIN') {
                window.location.replace('/html/administrador/dashboard.html');
                return false;
            }
        }

        // Panel de Agencia
        if (path.includes('/html/agencia/')) {
            const esFlujoAuth = path.endsWith('r_agencia_login.html') ||
                                path.endsWith('r_agencia_registro.html') ||
                                path.endsWith('r_agencia_turismo.html') ||
                                path.endsWith('r_agencia_empresa.html') ||
                                path.endsWith('r_agencia_responsable.html') ||
                                path.endsWith('r_agencia_documentos.html') ||
                                path.endsWith('r_agencia_revision.html');
            if (!esFlujoAuth) {
                if (!currentUser || currentUser.rol !== 'ROLE_AGENCIA') {
                    window.location.replace('/html/agencia/R_agencia_login.html');
                    return false;
                }
            } else if (currentUser && currentUser.rol === 'ROLE_AGENCIA' && path.endsWith('r_agencia_login.html')) {
                window.location.replace('/html/agencia/inicio.html');
                return false;
            }
        }

        // Vistas protegidas de Turista (Mis Reservas y Formulario de Reserva)
        if (path.includes('/html/turista/reservas.html') || path.includes('/html/turista/form_reserva.html')) {
            if (!currentUser) {
                const fullRedirect = window.location.pathname + window.location.search;
                window.location.replace('/html/turista/login.html?redirect=' + encodeURIComponent(fullRedirect));
                return false;
            }
        }

        return true;
    };

    // 4. Renderizar interfaz según estado de sesión
    window.renderUserProfile = function () {
        const user = window.getCurrentUser();
        const isTuristaFolder = window.location.pathname.includes('/turista/');

        const pathLogin = isTuristaFolder ? 'login.html' : 'html/turista/login.html';
        const pathRegistro = isTuristaFolder ? 'registro.html' : 'html/turista/registro.html';
        const pathAgencia = isTuristaFolder ? '../agencia/R_agencia_login.html' : 'html/agencia/R_agencia_login.html';
        const pathReservas = isTuristaFolder ? 'reservas.html' : 'html/turista/reservas.html';
        const pathNosotros = isTuristaFolder ? 'nosotros.html' : 'html/turista/nosotros.html';

        // 1. Enlaces del Nav principal (Nosotros vs Mis Reservas)
        document.querySelectorAll('nav.nav-links').forEach(nav => {
            let links = nav.querySelectorAll('a');
            let dynamicLink = null;
            links.forEach(a => {
                if (a.href.includes('nosotros.html') || a.href.includes('reservas.html')) {
                    dynamicLink = a;
                }
            });

            if (dynamicLink) {
                if (user) {
                    dynamicLink.href = pathReservas;
                    dynamicLink.innerHTML = '<i class="fa-regular fa-calendar-check hide-on-desktop"></i> Mis Reservas';
                } else {
                    dynamicLink.href = pathNosotros;
                    dynamicLink.innerHTML = '<i class="fa-solid fa-users hide-on-desktop"></i> Nosotros';
                }
            }
        });

        // 2. MODO INVITADO (Sin sesión)
        if (!user) {
            // Botones escritorio
            document.querySelectorAll('.nav-actions').forEach(actions => {
                actions.innerHTML = `
                    <a href="${pathAgencia}" class="btn-unirse-agencia">Únete como agencia</a>
                    <a href="${pathLogin}" class="btn-auth-login">Iniciar sesión</a>
                    <a href="${pathRegistro}" class="btn-auth-register">Registrarse</a>
                    ${!isTuristaFolder ? `
                        <a href="html/administrador/login.html" class="btn-admin-nav" title="Acceso de Administrador"
                           style="background:#0b1f38; color:#f59e0b; padding:8px 12px; border-radius:8px; font-size:12.5px; font-weight:700; text-decoration:none; display:inline-flex; align-items:center; gap:6px;">
                           <i class="fa-solid fa-shield-halved"></i> Admin
                        </a>
                    ` : ''}
                `;
                actions.style.visibility = 'visible';
                actions.style.opacity = '1';
            });

            // Menú móvil sin datos inventados
            document.querySelectorAll('.mobile-menu-user').forEach(userDiv => {
                userDiv.innerHTML = `
                    <div style="display:flex; gap:10px; margin-top:5px;">
                        <a href="${pathLogin}" style="color: #0b1f38; font-weight: 600; text-decoration: none; padding: 6px 14px; border-radius: 6px; border: 1px solid #cbd5e1; font-size: 13px; background:white;">Iniciar sesión</a>
                        <a href="${pathRegistro}" style="background: #196f3d; color: #ffffff; font-weight: 700; text-decoration: none; padding: 6px 14px; border-radius: 6px; font-size: 13px;">Registrarse</a>
                    </div>`;
            });
            return;
        }

        // 3. MODO AUTENTICADO (Con sesión real obtenida de /api/auth/me)
        const realName = user.nombre || user.username || 'Usuario';
        const realEmail = user.email || user.correo || '';

        // Dropdown de usuario en escritorio
        document.querySelectorAll('.nav-actions').forEach(actions => {
            if (!actions.querySelector('.user-dropdown')) {
                actions.innerHTML = `
                    <div class="user-dropdown" style="position: relative; display: inline-block; cursor: pointer;">
                        <div class="dropdown-header" style="display: flex; align-items: center; gap: 8px;">
                            <div class="avatar-placeholder" style="width:40px; height:40px; border-radius:50%; background:#0b1f38; color:#ffffff; display:flex; justify-content:center; align-items:center; font-size:18px; border:2px solid #196f3d; flex-shrink:0;">
                                <i class="fa-solid fa-user"></i>
                            </div>
                            <span style="font-size:14px; font-weight:600; color:#1e293b;">${realName}</span>
                            <i class="fa-solid fa-chevron-down" style="font-size: 12px; color: #475569;"></i>
                        </div>
                        <div class="dropdown-menu" style="display: none; position: absolute; right: 0; top: 100%; margin-top: 10px; background: white; border: 1px solid #e2e8f0; border-radius: 8px; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1); width: 220px; z-index: 1000; overflow: hidden;">
                            <div class="dropdown-info" style="padding: 16px; border-bottom: 1px solid #e2e8f0; background: #f8fafc;">
                                <h4 style="margin: 0; font-size: 14px; color: #0f172a; font-weight: 600;">${realName}</h4>
                                <p style="margin: 4px 0 0; font-size: 12px; color: #64748b; word-break: break-all;">${realEmail}</p>
                            </div>
                            <ul style="list-style: none; padding: 8px 0; margin: 0;">
                                <li><a href="${pathReservas}" style="display: block; padding: 8px 16px; color: #334155; text-decoration: none; font-size: 14px; transition: 0.2s;"><i class="fa-solid fa-calendar-check" style="width: 20px;"></i> Mis Reservas</a></li>
                                <li style="border-top: 1px solid #e2e8f0; margin-top: 8px; padding-top: 8px;"><a href="#" class="btn-cerrar-sesion" style="display: block; padding: 8px 16px; color: #ef4444; text-decoration: none; font-size: 14px; transition: 0.2s;"><i class="fa-solid fa-arrow-right-from-bracket" style="width: 20px;"></i> Cerrar sesión</a></li>
                            </ul>
                        </div>
                    </div>
                `;

                const dropdown = actions.querySelector('.user-dropdown');
                const menu = dropdown.querySelector('.dropdown-menu');
                dropdown.addEventListener('click', (e) => {
                    e.stopPropagation();
                    const isVisible = menu.style.display === 'block';
                    menu.style.display = isVisible ? 'none' : 'block';
                });

                document.addEventListener('click', (e) => {
                    if (!dropdown.contains(e.target)) {
                        menu.style.display = 'none';
                    }
                });

                dropdown.querySelector('.btn-cerrar-sesion').addEventListener('click', (e) => {
                    e.preventDefault();
                    window.cerrarSesion(e);
                });
            }
            actions.style.visibility = 'visible';
            actions.style.opacity = '1';
        });

        // Menú móvil con datos reales
        document.querySelectorAll('.mobile-menu-user').forEach(userDiv => {
            userDiv.innerHTML = `
                <div class="avatar-placeholder" style="width:44px; height:44px; border-radius:50%; background:#0b1f38; color:#ffffff; display:flex; justify-content:center; align-items:center; font-size:20px; border:2px solid #196f3d; flex-shrink:0;">
                    <i class="fa-solid fa-user"></i>
                </div>
                <div class="mobile-menu-info">
                    <h3 style="margin:0; font-size:15px; font-weight:600; color:#0b1f38;">${realName}</h3>
                    <p style="margin:2px 0 0; font-size:12px; color:#64748b; word-break:break-all;">${realEmail}</p>
                </div>
            `;
        });

        // Autocompletar datos del titular en form_reserva
        const inputNombre = document.getElementById('inputNombre');
        const inputCorreo = document.getElementById('inputCorreo');
        if (inputNombre && !inputNombre.value) inputNombre.value = realName;
        if (inputCorreo && !inputCorreo.value) inputCorreo.value = realEmail;
    };

    // 5. Consultar /api/auth/me una vez por página
    async function inicializarSesion() {
        if (sessionInitialized) return;
        sessionInitialized = true;

        try {
            const fetchFn = window.apiFetch || fetch;

            const res = await fetchFn('/api/auth/me', {
                credentials: 'include'
            });

            if (res.ok) {
                const data = await res.json();

                if (data && data.idUsuario) {

                    let soloNombre = data.nombres || '';

                    if (!soloNombre && data.nombre) {
                        let nom = data.nombre.trim();
                        let ape = (data.apellidos || '').trim();

                        if (ape && nom.toLowerCase().endsWith(ape.toLowerCase())) {
                            soloNombre = nom
                                .substring(0, nom.length - ape.length)
                                .trim();
                        } else {
                            soloNombre = nom.split(' ')[0] || nom;
                        }
                    }

                    let apePaterno =
                        data.apellidoPaterno ||
                        (data.user && data.user.apellidoPaterno) ||
                        '';

                    let apeMaterno =
                        data.apellidoMaterno ||
                        (data.user && data.user.apellidoMaterno) ||
                        '';

                    if (!apePaterno && (data.apellidos || (data.user && data.user.apellidos))) {

                        const fullApe =
                            (data.apellidos ||
                                (data.user && data.user.apellidos) ||
                                '').trim();

                        const parts = fullApe.split(' ');

                        apePaterno = parts[0] || '';

                        if (parts.length > 1) {
                            apeMaterno =
                                fullApe.substring(parts[0].length).trim();
                        }
                    }

                    currentUser = {
                        id: data.idUsuario,
                        idUsuario: data.idUsuario,

                        usuario: data.nombre,
                        nombre: data.nombre,
                        nombres: soloNombre,

                        apellidoPaterno: apePaterno,
                        apellidoMaterno: apeMaterno,

                        correo: data.correo || '',
                        email: data.correo || '',

                        rol: data.rol,

                        idAgencia: data.idAgencia || null,

                        nroDocumento:
                            data.nroDocumento ||
                            (data.user && data.user.nroDocumento) ||
                            '',

                        telefono:
                            data.telefono ||
                            (data.user && data.user.telefono) ||
                            '',

                        apellidos:
                            data.apellidos ||
                            (data.user && data.user.apellidos) ||
                            ''
                    };

                    try {
                        sessionStorage.setItem(
                            SESSION_CACHE_KEY,
                            JSON.stringify(currentUser)
                        );
                    } catch (e) {}

                } else {
                    currentUser = null;

                    try {
                        sessionStorage.setItem(
                            SESSION_CACHE_KEY,
                            'guest'
                        );
                    } catch (e) {}
                }

            } else {
                currentUser = null;

                try {
                    sessionStorage.setItem(
                        SESSION_CACHE_KEY,
                        'guest'
                    );
                } catch (e) {}
            }

        } catch (err) {
            console.error("Error inicializando sesión:", err);
        }
    }

    function renderImmediate() {
        // No mostrar ningún usuario antes de validar la sesión real.
        // La identidad se cargará mediante /api/auth/me
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', () => {
            inicializarSesion();
        });
    } else {
        inicializarSesion();window.getCurrentUser = function () {
            return currentUser;
        };
    }
})();
