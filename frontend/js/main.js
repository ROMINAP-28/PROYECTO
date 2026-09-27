// Toggle Mobile Menu
const mobileMenu = document.getElementById('mobile-menu');
const navLinks = document.getElementById('nav-links');

mobileMenu.addEventListener('click', () => {
    navLinks.classList.toggle('active');
});

// Modal Logic
const modal = document.getElementById('login-modal');

function openLoginModal() {
    modal.style.display = 'block';
}

function closeLoginModal() {
    modal.style.display = 'none';
}

// Cerrar modal si se hace clic fuera de él
window.onclick = function(event) {
    if (event.target == modal) {
        modal.style.display = 'none';
    }
}

// Simulación de Login
function simularLogin() {
    const usernameInput = document.getElementById('username').value;
    const authContainer = document.getElementById('auth-container');
    
    if(usernameInput.trim() !== '') {
        // Cambiar botones por el nombre de usuario
        authContainer.innerHTML = `
            <div id="user-profile">
                <i class="fas fa-user-circle"></i>
                <span>Hola, ${usernameInput}</span>
            </div>
        `;
        closeLoginModal();
    } else {
        alert('Por favor ingrese un nombre de usuario.');
    }
}
