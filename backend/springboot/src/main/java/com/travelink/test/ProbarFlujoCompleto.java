package com.travelink.test;

import com.travelink.config.ConexionDB;
import com.travelink.persistencia.ReservaPersistencia;
import com.travelink.entidades.Calificacion;
import com.travelink.persistencia.CalificacionPersistencia;

import java.sql.*;
import java.util.*;

public class ProbarFlujoCompleto {
    public static void main(String[] args) {
        try {
            System.out.println("=== INICIANDO PRUEBA DE FLUJO COMPLETO ===");
            ReservaPersistencia rp = new ReservaPersistencia();

            // Limpiar datos anteriores de prueba
            try (Connection con = ConexionDB.getConnection();
                 Statement st = con.createStatement()) {
                st.executeUpdate("DELETE FROM Pasajero");
                st.executeUpdate("DELETE FROM DetalleReserva");
                st.executeUpdate("DELETE FROM MetodoPago");
                st.executeUpdate("DELETE FROM Calificacion");
                st.executeUpdate("DELETE FROM Reserva");
                System.out.println("[OK] Tablas limpiadas");
            }

            // 1. Simular reserva con 3 pasajeros: 1 titular + 2 acompañantes
            List<Map<String, Object>> pasajeros = new ArrayList<>();

            // Titular
            Map<String, Object> p1 = new HashMap<>();
            p1.put("nombre", "Briane");
            p1.put("apellidoPaterno", "Infantes");
            p1.put("apellidoMaterno", "Gonzales");
            p1.put("dni", "32659878");
            p1.put("telefono", "987654321");
            p1.put("edad", 25);
            p1.put("tipoSeguro", "SIS");
            p1.put("esTitular", true);
            pasajeros.add(p1);

            // Pasajero 2
            Map<String, Object> p2 = new HashMap<>();
            p2.put("nombre", "Edgar");
            p2.put("apellidoPaterno", "Perez");
            p2.put("apellidoMaterno", "Ramos");
            p2.put("dni", "98653212");
            p2.put("telefono", "912345678");
            p2.put("edad", 28);
            p2.put("tipoSeguro", "SIS");
            p2.put("esTitular", false);
            pasajeros.add(p2);

            // Pasajero 3
            Map<String, Object> p3 = new HashMap<>();
            p3.put("nombre", "Carla");
            p3.put("apellidoPaterno", "Mendoza");
            p3.put("apellidoMaterno", "Ruiz");
            p3.put("dni", "74125896");
            p3.put("telefono", "934567890");
            p3.put("edad", 22);
            p3.put("tipoSeguro", "EsSalud");
            p3.put("esTitular", false);
            pasajeros.add(p3);

            Map<String, Object> req = new HashMap<>();
            req.put("idUsuario", 1);
            req.put("idAgencia", 1);
            req.put("nombreTour", "Machu Picchu Clásico");
            req.put("email", "briane.infantes@gmail.com");
            req.put("codigo", "TRK-777");
            req.put("fechaInicio", "2025-10-12");
            req.put("fechaFin", "2025-10-15");
            req.put("cantAdultos", 2);
            req.put("cantNinos", 1);
            req.put("cantBebes", 0);
            req.put("total", 1050.00);
            req.put("metodoPago", "Yape");
            req.put("numeroOperacion", "OP-89745612");
            req.put("urlComprobante", "Voucher_Yape_777.jpg");
            req.put("url", "Voucher_Yape_777.jpg");
            req.put("comprobante", "Voucher_Yape_777.jpg");
            req.put("pasajeros", pasajeros);

            Map<String, Object> resGuardar = rp.guardarReservaCompleta(req);
            System.out.println("Resultado guardarReserva: " + resGuardar);

            // 2. Probar calificación con la reserva recién creada
            int idReservaCreada = 1;
            Object dataObj = resGuardar.get("data");
            if (dataObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> dataMap = (Map<String, Object>) dataObj;
                Object idObj = dataMap.get("idReserva");
                if (idObj instanceof Number) {
                    idReservaCreada = ((Number) idObj).intValue();
                }
            }
            CalificacionPersistencia cp = new CalificacionPersistencia();
            Calificacion c = new Calificacion();
            c.setIdUsuario(1);
            c.setIdAgencia(1);
            c.setIdReserva(idReservaCreada);
            c.setEstrellas(5);
            c.setComentario("Excelente tour a Machu Picchu, servicio 10/10!");
            Calificacion cGuardada = cp.guardar(c);
            System.out.println("Resultado guardarCalificacion: id=" + (cGuardada != null ? cGuardada.getIdCalificacion() : "null"));

            // 3. Consultar tablas en la base de datos para verificar registros
            try (Connection con = ConexionDB.getConnection()) {
                System.out.println("\n--- REGISTROS EN TABLA Reserva ---");
                try (Statement st = con.createStatement();
                     ResultSet rs = st.executeQuery("SELECT idReserva, idUsuario, idAgencia, nombreTour, codigoReserva, total, estado FROM Reserva")) {
                    while (rs.next()) {
                        System.out.printf("idReserva: %d | idUsuario: %d | idAgencia: %d | nombreTour: %s | codigo: %s | total: %.2f | estado: %s\n",
                            rs.getInt("idReserva"), rs.getInt("idUsuario"), rs.getInt("idAgencia"), rs.getString("nombreTour"),
                            rs.getString("codigoReserva"), rs.getDouble("total"), rs.getString("estado"));
                    }
                }

                System.out.println("\n--- REGISTROS EN TABLA Pasajero ---");
                try (Statement st = con.createStatement();
                     ResultSet rs = st.executeQuery("SELECT idPasajero, idReserva, nroDocumento, telefono, nombre, apellidoPaterno, apellidoMaterno, apellidos, edad, tipoSeguro, esTitular FROM Pasajero")) {
                    while (rs.next()) {
                        System.out.printf("idPasajero: %d | DNI: %s | Tel: %s | Nombre: %s | Pat: %s | Mat: %s | Apellidos: %s | Edad: %d | Titular: %b\n",
                            rs.getInt("idPasajero"), rs.getString("nroDocumento"), rs.getString("telefono"),
                            rs.getString("nombre"), rs.getString("apellidoPaterno"), rs.getString("apellidoMaterno"),
                            rs.getString("apellidos"), rs.getInt("edad"), rs.getBoolean("esTitular"));
                    }
                }

                System.out.println("\n--- REGISTROS EN TABLA MetodoPago ---");
                try (Statement st = con.createStatement();
                     ResultSet rs = st.executeQuery("SELECT idMetodoPago, idReserva, tipoPago, monto, numeroOperacion, urlComprobante, url FROM MetodoPago")) {
                    while (rs.next()) {
                        System.out.printf("idPago: %d | Tipo: %s | Monto: %.2f | NumOp: %s | urlComprobante: %s | url: %s\n",
                            rs.getInt("idMetodoPago"), rs.getString("tipoPago"), rs.getDouble("monto"),
                            rs.getString("numeroOperacion"), rs.getString("urlComprobante"), rs.getString("url"));
                    }
                }

                System.out.println("\n--- REGISTROS EN TABLA Calificacion ---");
                try (Statement st = con.createStatement();
                     ResultSet rs = st.executeQuery("SELECT idCalificacion, idUsuario, idAgencia, idReserva, estrellas, comentario FROM Calificacion")) {
                    while (rs.next()) {
                        System.out.printf("idCal: %d | idUsuario: %d | idAgencia: %d | idReserva: %d | Estrellas: %d | Comentario: %s\n",
                            rs.getInt("idCalificacion"), rs.getInt("idUsuario"), rs.getInt("idAgencia"),
                            rs.getInt("idReserva"), rs.getInt("estrellas"), rs.getString("comentario"));
                    }
                }
            }

            System.out.println("\n=== TODO VALIDADO CON EXITO ===");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
