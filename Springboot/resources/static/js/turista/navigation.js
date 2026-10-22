// Travelink Navigation & Mobile Drawer System

document.addEventListener('DOMContentLoaded', () => {
    const menuToggle = document.querySelector('.menu-toggle');
    const navLinks = document.querySelector('.nav-links');
    const closeMenuBtn = document.querySelector('.close-menu-btn');
    const dropdownBtn = document.querySelector('.btn-user-circle');
    const dropdownContent = document.querySelector('.dropdown-content');

    // Toggle Mobile Drawer
    if (menuToggle && navLinks) {
        menuToggle.addEventListener('click', (e) => {
            e.stopPropagation();
            navLinks.classList.toggle('active');
            if (dropdownContent) dropdownContent.classList.remove('show');
        });
    }

    // Close Mobile Drawer with X button
    if (closeMenuBtn && navLinks) {
        closeMenuBtn.addEventListener('click', (e) => {
            e.stopPropagation();
            navLinks.classList.remove('active');
        });
    }

    // Toggle Desktop User Profile Dropdown
    if (dropdownBtn && dropdownContent) {
        dropdownBtn.addEventListener('click', (e) => {
            e.stopPropagation();
            dropdownContent.classList.toggle('show');
            if (navLinks) navLinks.classList.remove('active');
        });
    }

    // Close active popups when clicking anywhere outside
    document.addEventListener('click', (e) => {
        if (navLinks && navLinks.classList.contains('active')) {
            if (!navLinks.contains(e.target) && (!menuToggle || !menuToggle.contains(e.target))) {
                navLinks.classList.remove('active');
            }
        }
        if (dropdownContent && dropdownContent.classList.contains('show')) {
            if (!dropdownContent.contains(e.target) && (!dropdownBtn || !dropdownBtn.contains(e.target))) {
                dropdownContent.classList.remove('show');
            }
        }
    });

    // Safeguard against missing/broken images to prevent browser tab loading loops
    const avatarSvg = "data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' width='100' height='100' viewBox='0 0 100 100'><circle cx='50' cy='50' r='50' fill='%230b1f38'/><circle cx='50' cy='38' r='20' fill='%23ffc107'/><path d='M 20 85 C 20 65, 35 55, 50 55 C 65 55, 80 65, 80 85 Z' fill='%23ffc107'/></svg>";
    
    document.querySelectorAll('img').forEach(img => {
        img.onerror = function() {
            this.onerror = null;
            this.src = avatarSvg;
        };
    });
});
