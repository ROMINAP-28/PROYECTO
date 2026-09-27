package com.travelink.test;

import com.travelink.repositorio.ReservaRepositorio;
import java.util.*;

public class TestGuardarReserva {
    public static void main(String[] args) {
        ReservaRepositorio repo = new ReservaRepositorio();
        Map<String, Object> req = new HashMap<>();
        req.put("email", "BrianeInfantes@gmail.com");
        req.put("idUsuario", 4);
        req.put("idTour", 1);
        req.put("fechaInicio", "2025-10-12");
        req.put("fechaFin", "2025-10-15");
        req.put("cantAdultos", 2);
        req.put("cantNinos", 1);
        req.put("cantBebes", 0);
        req.put("total", 950.00);
        req.put("metodoPago", "Presencial");
        req.put("codigo", "TRK-999");
        req.put("comprobante", "voucher_test.jpg");
        req.put("canalAdelanto", "yape");
        req.put("esAdelanto", true);

        List<Map<String, Object>> pasajeros = new ArrayList<>();
        Map<String, Object> p1 = new HashMap<>();
        p1.put("dni", "32659878");
        p1.put("nombre", "Briane");
        p1.put("apellidoPaterno", "Infantes");
        p1.put("apellidoMaterno", "Gonzales");
        p1.put("telefono", "987456321");
        p1.put("esTitular", true);
        pasajeros.add(p1);

        Map<String, Object> p2 = new HashMap<>();
        p2.put("dni", "98653212");
        p2.put("nombre", "Edgar");
        p2.put("apellidoPaterno", "Perez");
        p2.put("apellidoMaterno", "Ramos");
        p2.put("telefono", "987111222");
        p2.put("esTitular", false);
        pasajeros.add(p2);

        req.put("pasajeros", pasajeros);

        System.out.println("Intentando guardar reserva...");
        Map<String, Object> res = repo.guardarReservaCompleta(req);
        System.out.println("Resultado de guardarReservaCompleta: " + res);
    }
}
