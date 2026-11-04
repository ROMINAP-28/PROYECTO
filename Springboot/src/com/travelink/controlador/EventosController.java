package com.travelink.controlador;

import com.travelink.servicio.EventosService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

/**
 * Controlador REST para Server-Sent Events (SSE) y sondeo liviano de eventos.
 * Proporciona el canal de tiempo real para sincronizar vistas entre Turista, Agencia y Administrador.
 */
@RestController
@RequestMapping("/api/eventos")
public class EventosController {

    private final EventosService eventosService;

    public EventosController(EventosService eventosService) {
        this.eventosService = eventosService;
    }

    /**
     * Canal SSE para suscripción a eventos en tiempo real.
     */
    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter suscribirEventos() {
        return eventosService.crearSubscripcion();
    }

    /**
     * Endpoint de sondeo liviano (fallback de 5-10 s si SSE no estuviese disponible).
     */
    @GetMapping("/poll")
    public ResponseEntity<Map<String, Object>> sondeoEventos() {
        return ResponseEntity.ok(eventosService.obtenerEstado());
    }

    /**
     * Permite emitir eventos manualmente desde el cliente o pruebas.
     */
    @PostMapping("/emitir")
    public ResponseEntity<Map<String, Object>> emitirManual(@RequestBody Map<String, Object> body) {
        String tipo = String.valueOf(body.getOrDefault("tipo", "cambio_general"));
        Object data = body.getOrDefault("data", Map.of());
        eventosService.emitirEvento(tipo, data);
        return ResponseEntity.ok(Map.of("status", "success", "mensaje", "Evento emitido"));
    }
}

