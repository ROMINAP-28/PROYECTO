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
        localStorage.setItem('travelink_current_user', 'logout');
        sessionStorage.clear();
        window.location.href = 'index.html';
    };

    // Render user details & profile picture across the page
    window.renderUserProfile = function () {
        const user = window.getCurrentUser();

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
                    dynamicLink.href = 'reservas.html';
                    dynamicLink.innerHTML = '<i class="fa-regular fa-calendar-check hide-on-desktop"></i> Mis Reservas';
                } else {
                    dynamicLink.href = 'nosotros.html';
                    dynamicLink.innerHTML = '<i class="fa-solid fa-users hide-on-desktop"></i> Nosotros';
                }
            }
        });

        // 2. GUEST MODE (Not Logged In)
        if (!user) {
            // Remove shopping cart icon if present
            document.querySelectorAll('.cart-icon-btn, #cart-link').forEach(cart => cart.remove());

            // Replace user dropdown with Iniciar sesión and Registrarse buttons
            document.querySelectorAll('.user-dropdown').forEach(dropdown => {
                const btnContainer = document.createElement('div');
                btnContainer.className = 'auth-buttons';
                btnContainer.style.cssText = 'display: flex; align-items: center; gap: 10px;';
                btnContainer.innerHTML = `
                    <a href="login.html" style="color: #0b1f38; font-weight: 600; text-decoration: none; padding: 6px 14px; border-radius: 6px; border: 1px solid #cbd5e1; font-size: 14px; background: white; transition: 0.2s;">Iniciar sesión</a>
                    <a href="registro.html" style="background: #196f3d; color: #ffffff; font-weight: 700; text-decoration: none; padding: 6px 16px; border-radius: 6px; font-size: 14px; transition: 0.2s;">Registrarse</a>`;
                dropdown.parentNode.replaceChild(btnContainer, dropdown);
            });

            // Mobile menu guest options
            document.querySelectorAll('.mobile-menu-user').forEach(userDiv => {
                userDiv.innerHTML = `
                    <div style="display:flex; gap:10px; margin-top:5px;">
                        <a href="login.html" style="color: white; font-weight: 600; text-decoration: none; padding: 6px 12px; border-radius: 6px; border: 1px solid #cbd5e1; font-size: 13px;">Iniciar sesión</a>
                        <a href="registro.html" style="background: #196f3d; color: #ffffff; font-weight: 700; text-decoration: none; padding: 6px 12px; border-radius: 6px; font-size: 13px;">Registrarse</a>
                    </div>`;
            });
            return;
        }

        // 3. AUTHENTICATED MODE (Logged In)
        // Ensure shopping cart icon is visible next to dropdown
        document.querySelectorAll('.nav-actions').forEach(actions => {
            let cartLink = actions.querySelector('.cart-icon-btn, #cart-link');
            if (!cartLink) {
                cartLink = document.createElement('a');
                cartLink.id = 'cart-link';
                cartLink.className = 'cart-icon-btn';
                cartLink.href = 'reservas.html';
                cartLink.style.cssText = 'display: flex; align-items: center; color: #196f3d; text-decoration: none; font-size: 24px; position: relative; margin-right: 15px;';
                cartLink.innerHTML = `
                    <i class="fas fa-shopping-cart"></i>
                    <span id="cart-badge" style="background:#196f3d; color:white; border-radius:50%; padding:2px 6px; font-size:12px; font-weight:bold; position: absolute; top: -10px; right: -15px;">0</span>
                `;
                actions.insertBefore(cartLink, actions.firstChild);
            }
        });

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
