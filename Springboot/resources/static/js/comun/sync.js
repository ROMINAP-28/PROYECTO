// Travelink - Real-Time Synchronization Module (SSE + Polling Fallback)
// Permite actualización instantánea (<10 s) entre Turista, Agencia y Administrador.

(function() {
    'use strict';

    let eventSource = null;
    let pollInterval = null;
    let lastTimestamps = {};
    let debounceTimers = {};

    function debounce(key, fn, delay = 600) {
        if (debounceTimers[key]) clearTimeout(debounceTimers[key]);
        debounceTimers[key] = setTimeout(fn, delay);
    }

    function dispararActualizaciones(tipo, data) {
        // 1. Emitir evento global en window
        window.dispatchEvent(new CustomEvent('travelinkSync', { detail: { tipo, data } }));

        const path = window.location.pathname.toLowerCase();

        // 2. Eventos de RESERVAS (Creación, cancelación, cambio de estado)
        if (tipo.includes('reserva')) {
            debounce('reserva', () => {
                if (typeof window.cargarReservasDesdeDB === 'function') {
                    window.cargarReservasDesdeDB();
                }
                if (typeof window.cargarReservas === 'function') {
                    window.cargarReservas();
                }
                if (typeof window.cargarDashboard === 'function') {
                    window.cargarDashboard();
                }
                if (typeof window.cargarDisponibilidad === 'function') {
                    window.cargarDisponibilidad();
                }
                if (typeof window.cargarPagos === 'function') {
                    window.cargarPagos();
                }
                if (typeof window.cargarComisiones === 'function') {
                    window.cargarComisiones();
                }
            });
        }

        // 3. Eventos de TOURS / PAQUETES (Crear, editar, pausar, eliminar)
        if (tipo.includes('tour') || tipo.includes('paquete')) {
            debounce('tour', () => {
                if (typeof window.cargarPaquetes === 'function') {
                    window.cargarPaquetes();
                }
                if (typeof window.cargarTours === 'function') {
                    window.cargarTours();
                }
                if (typeof window.cargarToursPopulares === 'function') {
                    window.cargarToursPopulares();
                }
                if (typeof window.cargarOfertas === 'function') {
                    window.cargarOfertas();
                }
                if (typeof window.cargarCatalogo === 'function') {
                    window.cargarCatalogo();
                }
                if (typeof window.cargarServicios === 'function') {
                    window.cargarServicios();
                }
            });
        }

        // 4. Eventos de DISPONIBILIDAD (Cupos, nuevas fechas, cierres)
        if (tipo.includes('disponibilidad') || tipo.includes('salida')) {
            debounce('disponibilidad', () => {
                if (typeof window.cargarDisponibilidad === 'function') {
                    window.cargarDisponibilidad();
                }
                if (typeof window.cargarPaquetes === 'function') {
                    window.cargarPaquetes();
                }
            });
        }

        // 5. Eventos de AGENCIAS (Aprobaciones, rechazos, solicitudes)
        if (tipo.includes('agencia')) {
            debounce('agencia', () => {
                if (typeof window.cargarAgencias === 'function') {
                    window.cargarAgencias();
                }
                if (typeof window.cargarDashboard === 'function') {
                    window.cargarDashboard();
                }
            });
        }

        // 6. Eventos de CALIFICACIONES Y RESEÑAS
        if (tipo.includes('calificacion') || tipo.includes('resena')) {
            debounce('calificacion', () => {
                if (typeof window.cargarCalidad === 'function') {
                    window.cargarCalidad();
                }
                if (typeof window.cargarCalificaciones === 'function') {
                    window.cargarCalificaciones();
                }
            });
        }
    }

    function iniciarSSE() {
        if (!window.EventSource) {
            iniciarPolling();
            return;
        }

        try {
            eventSource = new EventSource('/api/eventos');

            eventSource.addEventListener('open', () => {
                if (pollInterval) {
                    clearInterval(pollInterval);
                    pollInterval = null;
                }
            });

            const tipos = [
                'reserva_creada', 'reserva_cancelada', 'reserva_actualizada',
                'tour_creado', 'tour_actualizado', 'tour_eliminado',
                'disponibilidad_actualizada', 'agencia_actualizada',
                'calificacion_creada', 'calificacion_eliminada', 'notificacion_nueva'
            ];

            tipos.forEach(t => {
                eventSource.addEventListener(t, (e) => {
                    try {
                        const payload = JSON.parse(e.data);
                        dispararActualizaciones(t, payload.data || payload);
                    } catch (err) {
                        dispararActualizaciones(t, {});
                    }
                });
            });

            eventSource.onerror = () => {
                if (eventSource) eventSource.close();
                eventSource = null;
                iniciarPolling();
            };
        } catch (e) {
            iniciarPolling();
        }
    }

    function iniciarPolling() {
        if (pollInterval) return;
        pollInterval = setInterval(async () => {
            try {
                const res = await fetch('/api/eventos/poll');
                if (res.ok) {
                    const json = await res.json();
                    const ts = json.timestamps || {};
                    let hayCambios = false;

                    for (const [key, val] of Object.entries(ts)) {
                        if (lastTimestamps[key] && lastTimestamps[key] !== val) {
                            dispararActualizaciones(key, { polling: true, key });
                            hayCambios = true;
                        }
                        lastTimestamps[key] = val;
                    }
                }
            } catch (err) {
                // Fallback silencioso
            }
        }, 7000);
    }

    // Exponer API pública del sync
    window.TravelinkSync = {
        on: function(evento, handler) {
            window.addEventListener('travelinkSync', (e) => {
                if (!evento || evento === '*' || (e.detail && e.detail.tipo === evento)) {
                    handler(e.detail);
                }
            });
        },
        emit: async function(tipo, data) {
            dispararActualizaciones(tipo, data);
            try {
                const fetchFn = window.apiFetch || fetch;
                await fetchFn('/api/eventos/emitir', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ tipo, data })
                });
            } catch (ignored) {}
        }
    };

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', iniciarSSE);
    } else {
        iniciarSSE();
    }
})();

