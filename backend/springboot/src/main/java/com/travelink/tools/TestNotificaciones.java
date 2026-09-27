package com.travelink.tools;

import com.travelink.persistencia.NotificacionPersistencia;
import java.util.List;
import java.util.Map;

public class TestNotificaciones {
    public static void main(String[] args) {
        System.out.println("=== TEST DE NOTIFICACIONES EN BASE DE DATOS ===");
        
        // 1. Simular evento de nueva reserva
        System.out.println("1. Insertando notificación de prueba por evento de reserva...");
        boolean ok = NotificacionPersistencia.registrar(
            4,
            "Nueva Reserva Confirmada",
            "Reserva TRK-TEST registrada para el tour Laguna Humantay por un total de S/ 300.00.",
            "RESERVA",
            "ti ti-calendar-event",
            "green",
            "reservas.html"
        );
        System.out.println("Resultado de inserción: " + ok);

        // 2. Consultar notificaciones
        NotificacionPersistencia np = new NotificacionPersistencia();
        List<Map<String, Object>> notifs = np.obtenerNotificaciones(4, 5);
        int noLeidas = np.contarNoLeidas(4);
        System.out.println("\nTotal no leídas: " + noLeidas);
        System.out.println("Últimas notificaciones en BD:");
        for (Map<String, Object> n : notifs) {
            System.out.println(" - [" + n.get("id") + "] " + n.get("titulo") + " | " + n.get("mensaje") + " (Leída: " + n.get("leida") + ") [" + n.get("tiempoRelativo") + "]");
        }
    }
}
