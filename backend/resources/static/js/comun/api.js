// Travelink - Unified API Client with CSRF & Session Credentials
// Requerimiento 5: Envía X-XSRF-TOKEN en cada POST/PUT/DELETE e incluye credenciales (JSESSIONID)

(function () {
    function getCsrfToken() {
        const cookies = document.cookie ? document.cookie.split(';') : [];
        for (let i = 0; i < cookies.length; i++) {
            const cookie = cookies[i].trim();
            if (cookie.startsWith('XSRF-TOKEN=')) {
                return decodeURIComponent(cookie.substring('XSRF-TOKEN='.length));
            }
        }
        return null;
    }

    async function apiFetch(url, options = {}) {
        options = options || {};
        options.credentials = 'include'; // Siempre incluye la cookie de sesión JSESSIONID

        if (!options.headers) {
            options.headers = {};
        }

        const method = (options.method || 'GET').toUpperCase();
        if (['POST', 'PUT', 'DELETE', 'PATCH'].includes(method)) {
            const csrfToken = getCsrfToken();
            if (csrfToken) {
                if (options.headers instanceof Headers) {
                    options.headers.set('X-XSRF-TOKEN', csrfToken);
                } else {
                    options.headers['X-XSRF-TOKEN'] = csrfToken;
                }
            }
        }

        return fetch(url, options);
    }

    window.getCsrfToken = getCsrfToken;
    window.apiFetch = apiFetch;
})();
