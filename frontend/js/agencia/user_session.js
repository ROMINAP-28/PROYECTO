// Travelink - Multi-User Session & Dynamic Navigation Manager

(function () {
    // Get current logged-in user or null if logged out
    window.getCurrentUser = function () {
        const stored = localStorage.getItem('travelink_current_user');
        if (!stored || stored === 'logout') {
            return null;
        }
        try {
            const u = JSON.parse(stored);
            if (!u || typeof u !== 'object') return null;
            // Normalize email
            u.email = u.email || u.correo || '';
            u.username = u.username || u.usuario || u.nombre || 'Usuario';
            return u;
        } catch (e) {
            return null;
        }
    };

    window.setCurrentUser = function (userObj) {
        if (userObj) {
            userObj.email = userObj.email || userObj.correo || '';
            userObj.username = userObj.username || userObj.usuario || userObj.nombre || 'Usuario';
            localStorage.setItem('travelink_current_user', JSON.stringify(userObj));
        } else {
            localStorage.setItem('travelink_current_user', 'logout');
        }
        renderUserProfile();
    };

    window.cerrarSesion = function (e) {
        if (e) e.preventDefault();
        localStorage.removeItem('travelink_current_user');
        localStorage.removeItem('travelink_user');
        localStorage.removeItem('admin_user');
        localStorage.removeItem('agencia_user');
        localStorage.removeItem('agenciasesión');
        localStorage.removeItem('agenciaSesion');
        sessionStorage.clear();
        window.location.reload();
    };

    // Render user details & profile picture across the page
    window.renderUserProfile = function () {
        const user = window.getCurrentUser();
        const isTuristaFolder = window.location.pathname.includes('/turista/');

        const pathLogin = isTuristaFolder ? 'login.html' : 'turista/login.html';
        const pathRegistro = isTuristaFolder ? 'registro.html' : 'turista/registro.html';
        const pathAgencia = isTuristaFolder ? '../Agencia/R_agencia_login.html' : 'Agencia/R_agencia_login.html';
        const pathReservas = isTuristaFolder ? 'reservas.html' : 'turista/reservas.html';
        const pathNosotros = isTuristaFolder ? 'nosotros.html' : 'turista/nosotros.html';

        // 1. Dynamic 5th Nav link: "Nosotros" when logged out, "Mis Reservas" when logged in
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

            // Set active state for nav links based on current URL
            const currentPath = window.location.pathname;
            links.forEach(a => {
                if (a.href && currentPath.includes(a.getAttribute('href').replace('../', ''))) {
                    a.style.color = '#196f3d';
                    a.style.fontWeight = 'bold';
                    a.style.borderBottom = '2px solid #196f3d';
                    a.style.paddingBottom = '4px';
                }
            });
        });

        // Remove shopping cart icon entirely from all views
        document.querySelectorAll('.cart-icon-btn, #cart-link').forEach(cart => cart.remove());

        // 2. GUEST MODE (Not Logged In)
        if (!user) {

            // Revert nav-actions to login and register buttons if it currently has user-dropdown
            document.querySelectorAll('.nav-actions').forEach(actions => {
                if (actions.querySelector('.user-dropdown')) {
                    actions.innerHTML = `
                        <a href="${pathAgencia}" class="btn-unirse-agencia">Únete como agencia</a>
                        <a href="${pathLogin}" class="btn-auth-login">Iniciar sesión</a>
                        <a href="${pathRegistro}" class="btn-auth-register">Registrarse</a>
                    `;
                }
            });

            // Replace any leftover .user-dropdown directly (just in case they are not in .nav-actions)
            document.querySelectorAll('.user-dropdown').forEach(dropdown => {
                if (!dropdown.closest('.nav-actions')) {
                    const btnContainer = document.createElement('div');
                    btnContainer.className = 'auth-buttons';
                    btnContainer.style.cssText = 'display: flex; align-items: center; gap: 10px;';
                    btnContainer.innerHTML = `
                        <a href="${pathLogin}" style="color: #0b1f38; font-weight: 600; text-decoration: none; padding: 6px 14px; border-radius: 6px; border: 1px solid #cbd5e1; font-size: 14px; background: white; transition: 0.2s;">Iniciar sesión</a>
                        <a href="${pathRegistro}" style="background: #196f3d; color: #ffffff; font-weight: 700; text-decoration: none; padding: 6px 16px; border-radius: 6px; font-size: 14px; transition: 0.2s;">Registrarse</a>`;
                    dropdown.parentNode.replaceChild(btnContainer, dropdown);
                }
            });

            // Mobile menu guest options
            document.querySelectorAll('.mobile-menu-user').forEach(userDiv => {
                userDiv.innerHTML = `
                    <div style="display:flex; gap:10px; margin-top:5px;">
                        <a href="${pathLogin}" style="color: white; font-weight: 600; text-decoration: none; padding: 6px 12px; border-radius: 6px; border: 1px solid #cbd5e1; font-size: 13px;">Iniciar sesión</a>
                        <a href="${pathRegistro}" style="background: #196f3d; color: #ffffff; font-weight: 700; text-decoration: none; padding: 6px 12px; border-radius: 6px; font-size: 13px;">Registrarse</a>
                    </div>`;
            });
            return;
        }

        // 3. AUTHENTICATED MODE (Logged In)
        
        // Dynamically create user dropdown in .nav-actions if it doesn't exist
        document.querySelectorAll('.nav-actions').forEach(actions => {
            if (!actions.querySelector('.user-dropdown')) {
                actions.innerHTML = `
                    <div class="user-dropdown" style="position: relative; display: inline-block; cursor: pointer;">
                        <div class="dropdown-header" style="display: flex; align-items: center; gap: 8px;">
                            <div class="avatar-placeholder" style="width:40px; height:40px; border-radius:50%; background:#0b1f38; color:#ffffff; display:flex; justify-content:center; align-items:center; font-size:18px; border:2px solid #196f3d; flex-shrink:0;">
                                <i class="fa-solid fa-user"></i>
                            </div>
                            <i class="fa-solid fa-chevron-down" style="font-size: 12px; color: #475569;"></i>
                        </div>
                        <div class="dropdown-menu" style="display: none; position: absolute; right: 0; top: 100%; margin-top: 10px; background: white; border: 1px solid #e2e8f0; border-radius: 8px; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1); width: 220px; z-index: 1000; overflow: hidden;">
                            <div class="dropdown-info" style="padding: 16px; border-bottom: 1px solid #e2e8f0; background: #f8fafc;">
                                <h4 style="margin: 0; font-size: 14px; color: #0f172a; font-weight: 600;">Usuario</h4>
                                <p style="margin: 4px 0 0; font-size: 12px; color: #64748b; word-break: break-all;">correo</p>
                            </div>
                            <ul style="list-style: none; padding: 8px 0; margin: 0;">
                                <li><a href="reservas.html" style="display: block; padding: 8px 16px; color: #334155; text-decoration: none; font-size: 14px; transition: 0.2s;"><i class="fa-solid fa-calendar-check" style="width: 20px;"></i> Mis Reservas</a></li>
                                <li><a href="#" class="btn-subir-foto" style="display: block; padding: 8px 16px; color: #334155; text-decoration: none; font-size: 14px; transition: 0.2s;"><i class="fa-solid fa-camera" style="width: 20px;"></i> Subir foto</a></li>
                                <li style="border-top: 1px solid #e2e8f0; margin-top: 8px; padding-top: 8px;"><a href="#" class="btn-cerrar-sesion" style="display: block; padding: 8px 16px; color: #ef4444; text-decoration: none; font-size: 14px; transition: 0.2s;"><i class="fa-solid fa-arrow-right-from-bracket" style="width: 20px;"></i> Cerrar sesión</a></li>
                            </ul>
                        </div>
                    </div>
                `;
                // Add click interactions (toggle menu)
                const dropdown = actions.querySelector('.user-dropdown');
                const menu = dropdown.querySelector('.dropdown-menu');
                dropdown.addEventListener('click', (e) => {
                    e.stopPropagation();
                    const isVisible = menu.style.display === 'block';
                    menu.style.display = isVisible ? 'none' : 'block';
                });
                
                // Close when clicking outside
                document.addEventListener('click', (e) => {
                    if (!dropdown.contains(e.target)) {
                        menu.style.display = 'none';
                    }
                });
                
                // Attach click handlers
                dropdown.querySelector('.btn-cerrar-sesion').addEventListener('click', (e) => { e.preventDefault(); window.cerrarSesion(e); });
                dropdown.querySelector('.btn-subir-foto').addEventListener('click', (e) => { e.preventDefault(); window.subirFotoPerfil(); });
            }
        });

        // 3. AUTHENTICATED MODE (Logged In)


        // Key for user avatar storage
        const userEmail = user.email || user.correo || '';
        const avatarKey = 'travelink_avatar_' + userEmail;
        const customAvatar = localStorage.getItem(avatarKey) || user.avatar || '';

        // Update dropdown header avatar
        document.querySelectorAll('.dropdown-header').forEach(header => {
            let imgOrIcon = header.querySelector('.avatar-placeholder, img.avatar');
            if (!imgOrIcon) {
                imgOrIcon = document.createElement('div');
                imgOrIcon.className = 'avatar-placeholder';
                header.insertBefore(imgOrIcon, header.firstChild);
            }

            if (customAvatar) {
                imgOrIcon.outerHTML = `<img src="${customAvatar}" alt="Perfil" class="avatar" style="width:40px; height:40px; border-radius:50%; object-fit:cover; border:2px solid #196f3d;">`;
            } else {
                imgOrIcon.outerHTML = `
                    <div class="avatar-placeholder" style="width:40px; height:40px; border-radius:50%; background:#0b1f38; color:#ffffff; display:flex; justify-content:center; align-items:center; font-size:18px; margin-right:12px; border:2px solid #196f3d; flex-shrink:0;">
                        <i class="fa-solid fa-user"></i>
                    </div>`;
            }
        });

        // Mobile drawer avatar
        document.querySelectorAll('.mobile-menu-user').forEach(userDiv => {
            let imgOrIcon = userDiv.querySelector('.avatar-placeholder, img.avatar');
            if (customAvatar) {
                if (imgOrIcon) imgOrIcon.outerHTML = `<img src="${customAvatar}" alt="Perfil" class="avatar" style="width:44px; height:44px; border-radius:50%; object-fit:cover; border:2px solid #196f3d;">`;
            } else {
                if (imgOrIcon) imgOrIcon.outerHTML = `
                    <div class="avatar-placeholder" style="width:44px; height:44px; border-radius:50%; background:#0b1f38; color:#ffffff; display:flex; justify-content:center; align-items:center; font-size:20px; border:2px solid #196f3d; flex-shrink:0;">
                        <i class="fa-solid fa-user"></i>
                    </div>`;
            }
        });

        // Update username & EXACT registered email in dropdown & mobile header
        document.querySelectorAll('.dropdown-info h4, .mobile-menu-info h3').forEach(el => {
            el.textContent = user.username || user.usuario || user.nombre || 'Usuario';
        });
        document.querySelectorAll('.dropdown-info p, .mobile-menu-info p').forEach(el => {
            el.textContent = userEmail;
        });

        // Sync inputs on Paso 2 of form_reserva
        const inputNombre = document.getElementById('inputNombre');
        const inputCorreo = document.getElementById('inputCorreo');
        const inputTelefono = document.getElementById('inputTelefono');

        if (inputNombre) {
            const fullName = (user.nombre || '') + ' ' + (user.apellidos || '');
            inputNombre.value = fullName.trim() || user.username;
        }
        if (inputCorreo) {
            inputCorreo.value = userEmail;
        }
        if (inputTelefono && !inputTelefono.value) {
            inputTelefono.value = user.telefono || '';
        }

        // Resumen titular paso 4
        const elRNombre = document.getElementById('rTitularNombre');
        const elRCorreo = document.getElementById('rTitularCorreo');
        if (elRNombre) elRNombre.textContent = user.username || user.nombre;
        if (elRCorreo) elRCorreo.textContent = userEmail;
    };

    // Handle "Subir foto" profile picture upload
    window.subirFotoPerfil = function () {
        const fileInput = document.createElement('input');
        fileInput.type = 'file';
        fileInput.accept = 'image/*';
        fileInput.onchange = e => {
            const file = e.target.files[0];
            if (file) {
                if (file.size > 5 * 1024 * 1024) {
                    if (typeof toastr !== 'undefined') toastr.error('La imagen no debe superar los 5MB.');
                    else alert('La imagen no debe superar los 5MB.');
                    return;
                }
                const reader = new FileReader();
                reader.onload = function (event) {
                    const base64Img = event.target.result;
                    const user = window.getCurrentUser();
                    if (user) {
                        const userEmail = user.email || user.correo || '';
                        const avatarKey = 'travelink_avatar_' + userEmail;
                        localStorage.setItem(avatarKey, base64Img);
                        renderUserProfile();
                    }

                    if (typeof toastr !== 'undefined') toastr.success('¡Foto de perfil actualizada correctamente!');
                    else if (typeof Swal !== 'undefined') Swal.fire('¡Éxito!', 'Foto de perfil actualizada correctamente', 'success');
                    else alert('Foto de perfil actualizada');
                };
                reader.readAsDataURL(file);
            }
        };
        fileInput.click();
    };

    document.addEventListener('DOMContentLoaded', () => {
        renderUserProfile();

        // Attach event handlers
        document.querySelectorAll('a').forEach(item => {
            const text = item.textContent.trim().toLowerCase();
            if (text.includes('cerrar sesión') || text.includes('cerrar sesion')) {
                item.href = 'javascript:void(0)';
                item.onclick = function (e) {
                    e.preventDefault();
                    window.cerrarSesion(e);
                };
            }
            if (text.includes('subir foto')) {
                item.onclick = function (e) {
                    e.preventDefault();
                    window.subirFotoPerfil();
                };
            }
        });
    });
})();
