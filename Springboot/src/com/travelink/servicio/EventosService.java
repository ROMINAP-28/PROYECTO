package com.travelink.servicio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Servicio centralizado para emisión y sincronización de eventos en tiempo real entre los 3 roles
 * (Turista, Agencia de Viajes y Administrador).
 * Soporta SSE (Server-Sent Events) y sondeo (polling) liviano como respaldo.
 */
@Service
public class EventosService {

    private static final Logger log = LoggerFactory.getLogger(EventosService.class);
    private static volatile EventosService instance;

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final Map<String, Long> ultimasModificaciones = new ConcurrentHashMap<>();
    private final Map<String, Long> contadores = new ConcurrentHashMap<>();

    public EventosService() {
        instance = this;
        long now = System.currentTimeMillis();
        ultimasModificaciones.put("reserva", now);
        ultimasModificaciones.put("tour", now);
        ultimasModificaciones.put("disponibilidad", now);
        ultimasModificaciones.put("agencia", now);
        ultimasModificaciones.put("calificacion", now);
        ultimasModificaciones.put("comision", now);
        ultimasModificaciones.put("usuario", now);
        ultimasModificaciones.put("notificacion", now);
    }

    public static EventosService getInstance() {
        return instance;
    }

    public static void emitir(String tipo, Object data) {
        if (instance != null) {
            instance.emitirEvento(tipo, data);
        }
    }

    public SseEmitter crearSubscripcion() {
        SseEmitter emitter = new SseEmitter(180_000L); // 3 minutos, el navegador reconecta automáticamente
        emitters.add(emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> {
            emitter.complete();
            emitters.remove(emitter);
        });
        emitter.onError(e -> {
            emitter.complete();
            emitters.remove(emitter);
        });

        // Enviar evento inicial de conexión
        try {
            emitter.send(SseEmitter.event()
                    .name("conectado")
                    .data(Map.of("status", "conectado", "timestamp", System.currentTimeMillis())));
        } catch (IOException e) {
            emitters.remove(emitter);
        }

        return emitter;
    }

    public void emitirEvento(String tipo, Object data) {
        long now = System.currentTimeMillis();
        ultimasModificaciones.put(tipo.toLowerCase(), now);
        contadores.compute(tipo.toLowerCase(), (k, v) -> v == null ? 1L : v + 1);

        log.info("Sincronización: emitiendo evento [{}] a {} clientes conectados.", tipo, emitters.size());

        Map<String, Object> payload = Map.of(
                "tipo", tipo,
                "data", data != null ? data : Map.of(),
                "timestamp", now
        );

        List<SseEmitter> fallidos = new CopyOnWriteArrayList<>();
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name(tipo)
                        .data(payload));
            } catch (Exception ex) {
                fallidos.add(emitter);
            }
        }
        emitters.removeAll(fallidos);
    }

    public Map<String, Object> obtenerEstado() {
        return Map.of(
                "status", "success",
                "timestamps", ultimasModificaciones,
                "contadores", contadores,
                "clientesActivos", emitters.size()
        );
    }
}

