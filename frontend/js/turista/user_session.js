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

        // 1. Dynamic 5th Nav link: "Sobre nosotros" when logged out, "Mis Reservas" when logged in
        document.querySelectorAll('nav.nav-links').forEach(nav => {
            let links = nav.querySelectorAll('a');
            let dynamicLink = null;
            links.forEach(a => {
                const text = a.textContent.trim().toLowerCase();
                if (text.includes('nosotros') || text.includes('reservas') || a.href.includes('nosotros.html') || a.href.includes('reservas.html')) {
                    dynamicLink = a;
                }
            });

            if (dynamicLink) {
                if (user) {
                    dynamicLink.href = 'reservas.html';
                    dynamicLink.innerHTML = '<i class="fa-regular fa-calendar-check hide-on-desktop"></i> Mis Reservas';
                } else {
                    dynamicLink.href = 'nosotros.html';
                    dynamicLink.innerHTML = '<i class="fa-solid fa-users hide-on-desktop"></i> Sobre nosotros';
                }
            }
        });

        // 2. GUEST MODE (Not Logged In)
        if (!user) {
            // Remove shopping cart icon if present
            document.querySelectorAll('.cart-icon-btn, #cart-link').forEach(cart => cart.remove());

            // Render standard Iniciar sesión & Registrarse buttons on top right
            document.querySelectorAll('.nav-actions').forEach(actions => {
                actions.innerHTML = `
                    <a href="login.html" class="btn-auth-login" style="color: #0b1f38; font-weight: 600; text-decoration: none; padding: 7px 16px; border-radius: 6px; border: 1px solid #cbd5e1; font-size: 14px; background: white; transition: 0.2s;">Iniciar sesión</a>
                    <a href="registro.html" class="btn-auth-register" style="background: #196f3d; color: #ffffff; font-weight: 700; text-decoration: none; padding: 7px 18px; border-radius: 6px; font-size: 14px; transition: 0.2s;">Registrarse</a>
                `;
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
        const userEmail = user.email || user.correo || '';
        const userName = user.username || user.usuario || user.nombre || 'Usuario';
        const avatarKey = 'travelink_avatar_' + userEmail;
        const customAvatar = localStorage.getItem(avatarKey) || user.avatar || '';

        const avatarImgSmall = customAvatar 
            ? `<img src="${customAvatar}" alt="Perfil" style="width:36px; height:36px; border-radius:50%; object-fit:cover; border:2px solid #196f3d;">`
            : `<div style="width:36px; height:36px; border-radius:50%; background:#0b1f38; color:#ffffff; display:flex; justify-content:center; align-items:center; font-size:16px; border:2px solid #196f3d; flex-shrink:0;"><i class="fa-solid fa-user"></i></div>`;

        const avatarImgLarge = customAvatar 
            ? `<img src="${customAvatar}" alt="Perfil" style="width:46px; height:46px; border-radius:50%; object-fit:cover; border:2px solid #196f3d; margin-right:12px;">`
            : `<div style="width:46px; height:46px; border-radius:50%; background:#0b1f38; color:#ffffff; display:flex; justify-content:center; align-items:center; font-size:20px; border:2px solid #196f3d; margin-right:12px; flex-shrink:0;"><i class="fa-solid fa-user"></i></div>`;

        // Render User Profile Dropdown in Nav Actions
        document.querySelectorAll('.nav-actions').forEach(actions => {
            actions.innerHTML = `
                <div class="user-profile-wrapper" style="position: relative; display: flex; align-items: center;">
                    <button type="button" class="btn-user-circle" id="user-profile-toggle" style="background: white; border: 1px solid #e2e8f0; cursor: pointer; display: flex; align-items: center; gap: 10px; padding: 4px 12px 4px 4px; border-radius: 50px; box-shadow: 0 2px 6px rgba(0,0,0,0.04); transition: 0.2s;">
                        ${avatarImgSmall}
                        <span style="color: #0b1f38; font-weight: 700; font-size: 14px; max-width: 120px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">${userName}</span>
                        <i class="fa-solid fa-chevron-down" style="font-size: 11px; color: #64748b;"></i>
                    </button>
                    <div class="dropdown-content" id="user-dropdown-content" style="display: none; position: absolute; right: 0; top: 52px; width: 280px; background: white; border-radius: 12px; box-shadow: 0 10px 30px rgba(0,0,0,0.15); padding: 15px; z-index: 10000; border: 1px solid #e2e8f0;">
                        <div class="dropdown-header" style="display: flex; align-items: center; padding-bottom: 12px; border-bottom: 1px solid #f1f5f9; margin-bottom: 10px;">
                            ${avatarImgLarge}
                            <div class="dropdown-info" style="overflow: hidden;">
                                <h4 style="margin: 0; color: #0b1f38; font-size: 15px; font-weight: 700; text-overflow: ellipsis; overflow: hidden; white-space: nowrap;">${userName}</h4>
                                <p style="margin: 2px 0 0 0; color: #64748b; font-size: 12.5px; text-overflow: ellipsis; overflow: hidden; white-space: nowrap;">${userEmail}</p>
                            </div>
                        </div>
                        <a href="javascript:void(0)" onclick="subirFotoPerfil()" class="dropdown-item" style="display: flex; align-items: center; justify-content: space-between; padding: 10px 12px; border-radius: 8px; color: #334155; text-decoration: none; font-weight: 600; font-size: 13.5px; transition: 0.2s; background: #f8fafc; margin-bottom: 6px;">
                            <span><i class="fa-regular fa-image" style="font-size: 16px; margin-right: 8px; color: #0b1f38;"></i> Subir foto</span>
                            <i class="fa-solid fa-chevron-right" style="color: #94a3b8; font-size: 11px;"></i>
                        </a>
                        <a href="reservas.html" class="dropdown-item" style="display: flex; align-items: center; justify-content: space-between; padding: 10px 12px; border-radius: 8px; color: #334155; text-decoration: none; font-weight: 600; font-size: 13.5px; transition: 0.2s; background: #f8fafc; margin-bottom: 6px;">
                            <span><i class="fa-regular fa-calendar-check" style="font-size: 16px; margin-right: 8px; color: #196f3d;"></i> Mis Reservas</span>
                            <i class="fa-solid fa-chevron-right" style="color: #94a3b8; font-size: 11px;"></i>
                        </a>
                        <div style="height: 1px; background: #f1f5f9; margin: 6px 0;"></div>
                        <a href="javascript:void(0)" onclick="cerrarSesion(event)" class="dropdown-item" style="display: flex; align-items: center; justify-content: space-between; padding: 10px 12px; border-radius: 8px; color: #ef4444; text-decoration: none; font-weight: 600; font-size: 13.5px; transition: 0.2s; background: #fef2f2;">
                            <span><i class="fa-solid fa-arrow-right-from-bracket" style="font-size: 16px; margin-right: 8px; color: #ef4444;"></i> Cerrar sesión</span>
                            <i class="fa-solid fa-chevron-right" style="color: #ef4444; font-size: 11px;"></i>
                        </a>
                    </div>
                </div>
            `;

            // Attach toggle click event
            const btnToggle = actions.querySelector('#user-profile-toggle');
            const dropContent = actions.querySelector('#user-dropdown-content');
            if (btnToggle && dropContent) {
                btnToggle.addEventListener('click', (e) => {
                    e.stopPropagation();
                    const isVisible = dropContent.style.display === 'block';
                    dropContent.style.display = isVisible ? 'none' : 'block';
                });
            }
        });

        // Close dropdown when clicking outside
        document.addEventListener('click', (e) => {
            document.querySelectorAll('#user-dropdown-content').forEach(drop => {
                if (drop && drop.style.display === 'block') {
                    if (!drop.contains(e.target)) {
                        drop.style.display = 'none';
                    }
                }
            });
        });

        // Mobile drawer user info
        document.querySelectorAll('.mobile-menu-user').forEach(userDiv => {
            userDiv.innerHTML = `
                <div style="display:flex; align-items:center; gap:12px;">
                    ${avatarImgSmall}
                    <div class="mobile-menu-info">
                        <h3 style="margin:0; color:white; font-size:15px;">${userName}</h3>
                        <p style="margin:0; color:#cbd5e1; font-size:12px;">${userEmail}</p>
                    </div>
                </div>
            `;
        });
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
    });
})();
