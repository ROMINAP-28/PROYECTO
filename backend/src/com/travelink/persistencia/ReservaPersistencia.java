package com.travelink.persistencia;

import com.travelink.config.ConexionDB;
import com.travelink.entidades.DetalleReserva;
import com.travelink.entidades.MetodoPago;
import com.travelink.entidades.Pasajero;
import com.travelink.entidades.Reserva;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class ReservaPersistencia {

    public boolean perteneceAUsuario(int idReserva, int idUsuario) {
        String sql = "SELECT COUNT(*) FROM Reserva WHERE idReserva = ? AND idUsuario = ?";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idReserva);
            ps.setInt(2, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String formatDesglose(int cantAdultos, BigDecimal precioAdulto, int cantNinos, BigDecimal precioNino, int cantBebes, BigDecimal precioBebe) {
        List<String> partes = new ArrayList<>();
        if (cantAdultos > 0) {
            double pA = precioAdulto != null ? precioAdulto.doubleValue() : 0.0;
            partes.add(cantAdultos + (cantAdultos == 1 ? " adulto" : " adultos") + " × S/ " + String.format(Locale.US, "%.2f", pA));
        }
        if (cantNinos > 0) {
            double pN = precioNino != null ? precioNino.doubleValue() : 0.0;
            partes.add(cantNinos + (cantNinos == 1 ? " niño" : " niños") + " × S/ " + String.format(Locale.US, "%.2f", pN));
        }
        if (cantBebes > 0) {
            if (precioBebe == null || precioBebe.compareTo(BigDecimal.ZERO) == 0) {
                partes.add(cantBebes + (cantBebes == 1 ? " bebé (gratis)" : " bebés (gratis)"));
            } else {
                partes.add(cantBebes + (cantBebes == 1 ? " bebé" : " bebés") + " × S/ " + String.format(Locale.US, "%.2f", precioBebe.doubleValue()));
            }
        }
        return partes.isEmpty() ? "1 adulto" : String.join(" + ", partes);
    }

    public Map<String, Object> guardarReservaCompleta(Map<String, Object> reqData) {
        Map<String, Object> response = new HashMap<>();
        Connection con = null;

        try {
            con = ConexionDB.getConnection();
            con.setAutoCommit(false);

            try (Statement st = con.createStatement()) {
                st.execute("SET FOREIGN_KEY_CHECKS = 0;");
            }

            // 1. Identificar Titular de la sesión
            int idUsuario = 0;
            try {
                if (reqData.containsKey("sessionUserId")) {
                    idUsuario = Integer.parseInt(String.valueOf(reqData.get("sessionUserId")));
                } else if (reqData.containsKey("idUsuario")) {
                    idUsuario = Integer.parseInt(String.valueOf(reqData.get("idUsuario")));
                }
            } catch (Exception ignored) {}

            String correo = String.valueOf(reqData.getOrDefault("email", reqData.getOrDefault("correo", "")));
            if (idUsuario <= 0 && !correo.trim().isEmpty()) {
                String sqlU = "SELECT u.idUsuario FROM Usuario u INNER JOIN Persona p ON u.idPersona = p.idPersona WHERE p.email = ? OR u.nombreUsuario = ?";
                try (PreparedStatement psU = con.prepareStatement(sqlU)) {
                    psU.setString(1, correo);
                    psU.setString(2, correo);
                    try (ResultSet rsU = psU.executeQuery()) {
                        if (rsU.next()) idUsuario = rsU.getInt(1);
                    }
                }
            }

            if (idUsuario <= 0) {
                con.rollback();
                response.put("status", "error");
                response.put("code", "UNAUTHORIZED");
                response.put("message", "Debe iniciar sesión para realizar una reserva.");
                return response;
            }

            // Obtener datos del titular desde la base de datos (inmutable frente a spoofing del navegador)
            String titularNombre = "Turista";
            String titularApellidos = "Travelink";
            String titularDoc = "12345678";
            String titularTel = "987654321";
            String titularEmail = correo;

            String sqlTitular = "SELECT p.nombre, p.apellidoPaterno, p.apellidoMaterno, p.nroDocumento, p.telefono, p.email " +
                                "FROM Usuario u JOIN Persona p ON u.idPersona = p.idPersona WHERE u.idUsuario = ?";
            try (PreparedStatement psTit = con.prepareStatement(sqlTitular)) {
                psTit.setInt(1, idUsuario);
                try (ResultSet rsTit = psTit.executeQuery()) {
                    if (rsTit.next()) {
                        titularNombre = rsTit.getString("nombre");
                        String pat = rsTit.getString("apellidoPaterno");
                        String mat = rsTit.getString("apellidoMaterno");
                        titularApellidos = (pat != null ? pat : "") + (mat != null && !mat.trim().isEmpty() ? " " + mat : "");
                        if (titularApellidos.trim().isEmpty()) titularApellidos = "Turista";
                        titularDoc = rsTit.getString("nroDocumento");
                        titularTel = rsTit.getString("telefono");
                        titularEmail = rsTit.getString("email");
                    }
                }
            }

            // 2. Resolver TourFecha e idTour
            int idTourFecha = 0;
            try { idTourFecha = Integer.parseInt(String.valueOf(reqData.getOrDefault("idTourFecha", reqData.getOrDefault("tourFechaId", "0")))); } catch (Exception ignored) {}
            if (idTourFecha <= 0) {
                try { idTourFecha = Integer.parseInt(String.valueOf(reqData.getOrDefault("idPaquete", reqData.getOrDefault("tour", "0")))); } catch (Exception ignored) {}
            }

            if (idTourFecha <= 0) {
                con.rollback();
                response.put("status", "error");
                response.put("code", "SALIDA_INEXISTENTE");
                response.put("message", "Debe seleccionar una salida válida para este tour.");
                return response;
            }

            // Consultar datos reales de TourFecha y Tour en la BD
            int idTour = 0;
            int idAgencia = 1;
            String nombreTour = "Tour Turístico";
            java.sql.Date fechaSalida = null;
            java.sql.Time horaInicio = null;
            int cupoDisponible = 0;
            String estadoFecha = "DISPONIBLE";

            BigDecimal precioAdulto = BigDecimal.ZERO;
            BigDecimal precioNino = BigDecimal.ZERO;
            BigDecimal precioBebe = BigDecimal.ZERO;
            boolean aceptaNinos = true;
            int edadMaxNino = 11;
            boolean aceptaBebes = false;
            int edadMaxBebe = 2;
            BigDecimal descuentoPct = BigDecimal.ZERO;
            String estadoTour = "ACTIVO";

            String sqlCheckTF = "SELECT tf.idTourFecha, tf.idTour, tf.fecha, tf.horaInicio, tf.cupoTotal, tf.cupoDisponible, tf.estado AS estadoFecha, " +
                                "t.idAgencia, t.nombre AS tourNombre, t.precioAdulto, t.precioNino, t.precioBebe, " +
                                "t.aceptaNinos, t.edadMaxNino, t.aceptaBebes, t.edadMaxBebe, t.descuento, t.estado AS estadoTour " +
                                "FROM TourFecha tf " +
                                "JOIN Tour t ON tf.idTour = t.idTour " +
                                "WHERE tf.idTourFecha = ?";
            try (PreparedStatement psTF = con.prepareStatement(sqlCheckTF)) {
                psTF.setInt(1, idTourFecha);
                try (ResultSet rsTF = psTF.executeQuery()) {
                    if (rsTF.next()) {
                        idTour = rsTF.getInt("idTour");
                        idAgencia = rsTF.getInt("idAgencia");
                        nombreTour = rsTF.getString("tourNombre");
                        fechaSalida = rsTF.getDate("fecha");
                        horaInicio = rsTF.getTime("horaInicio");
                        cupoDisponible = rsTF.getInt("cupoDisponible");
                        estadoFecha = rsTF.getString("estadoFecha");

                        precioAdulto = rsTF.getBigDecimal("precioAdulto");
                        if (precioAdulto == null) precioAdulto = BigDecimal.ZERO;
                        precioNino = rsTF.getBigDecimal("precioNino");
                        if (precioNino == null) precioNino = BigDecimal.ZERO;
                        precioBebe = rsTF.getBigDecimal("precioBebe");
                        if (precioBebe == null) precioBebe = BigDecimal.ZERO;

                        aceptaNinos = rsTF.getBoolean("aceptaNinos");
                        edadMaxNino = rsTF.getInt("edadMaxNino");
                        aceptaBebes = rsTF.getBoolean("aceptaBebes");
                        edadMaxBebe = rsTF.getInt("edadMaxBebe");
                        descuentoPct = rsTF.getBigDecimal("descuento");
                        if (descuentoPct == null) descuentoPct = BigDecimal.ZERO;
                        estadoTour = rsTF.getString("estadoTour");
                    } else {
                        con.rollback();
                        response.put("status", "error");
                        response.put("code", "SALIDA_INEXISTENTE");
                        response.put("message", "La salida seleccionada no existe en la base de datos.");
                        return response;
                    }
                }
            }

            // Validar estado del tour
            if ("PAUSADO".equalsIgnoreCase(estadoTour) || "INACTIVO".equalsIgnoreCase(estadoTour)) {
                con.rollback();
                response.put("status", "error");
                response.put("code", "TOUR_NO_DISPONIBLE");
                response.put("message", "Paquete no disponible actualmente.");
                return response;
            }

            // Validar que la salida no sea pasada
            java.sql.Date hoy = new java.sql.Date(System.currentTimeMillis());
            if (fechaSalida != null && fechaSalida.before(hoy)) {
                con.rollback();
                response.put("status", "error");
                response.put("code", "SALIDA_PASADA");
                response.put("message", "No se pueden reservar salidas con fecha pasada (" + fechaSalida + ").");
                return response;
            }

            // 3. Cantidad de pasajeros y reglas de tipos permitidos
            int cantAdultos = 1;
            int cantNinos = 0;
            int cantBebes = 0;
            try { cantAdultos = Integer.parseInt(String.valueOf(reqData.getOrDefault("cantAdultos", reqData.getOrDefault("adultos", "1")))); } catch (Exception ignored) {}
            try { cantNinos = Integer.parseInt(String.valueOf(reqData.getOrDefault("cantNinos", reqData.getOrDefault("ninos", "0")))); } catch (Exception ignored) {}
            try { cantBebes = Integer.parseInt(String.valueOf(reqData.getOrDefault("cantBebes", reqData.getOrDefault("bebes", "0")))); } catch (Exception ignored) {}

            // Mínimo 1 adulto
            if (cantAdultos < 1) {
                con.rollback();
                response.put("status", "error");
                response.put("code", "MINIMO_ADULTOS");
                response.put("message", "Se requiere como mínimo 1 adulto para realizar la reserva.");
                return response;
            }

            // Rechazar si no admite niños
            if (cantNinos > 0 && !aceptaNinos) {
                con.rollback();
                response.put("status", "error");
                response.put("code", "TIPO_NO_PERMITIDO");
                response.put("message", "Este tour no admite niños.");
                return response;
            }

            // Rechazar si no admite bebés
            if (cantBebes > 0 && !aceptaBebes) {
                con.rollback();
                response.put("status", "error");
                response.put("code", "TIPO_NO_PERMITIDO");
                response.put("message", "Este tour no admite bebés.");
                return response;
            }

            // 4. Validar edades de lista de pasajeros si viene en el payload
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> pasajerosReq = (List<Map<String, Object>>) reqData.get("pasajeros");
            if (pasajerosReq != null) {
                for (Map<String, Object> pMap : pasajerosReq) {
                    String tipoP = String.valueOf(pMap.getOrDefault("tipo", "ADULTO")).toUpperCase();
                    int edadP = 0;
                    try { edadP = Integer.parseInt(String.valueOf(pMap.getOrDefault("edad", "0"))); } catch (Exception ignored) {}

                    if ("NINO".equals(tipoP) || "NIÑO".equals(tipoP)) {
                        if (edadP > edadMaxNino) {
                            con.rollback();
                            response.put("status", "error");
                            response.put("code", "EDAD_INVALIDA");
                            response.put("message", "La edad del niño (" + edadP + " años) excede el máximo permitido para este tour (" + edadMaxNino + " años).");
                            return response;
                        }
                    } else if ("BEBE".equals(tipoP) || "BEBÉ".equals(tipoP)) {
                        if (edadP > edadMaxBebe) {
                            con.rollback();
                            response.put("status", "error");
                            response.put("code", "EDAD_INVALIDA");
                            response.put("message", "La edad del bebé (" + edadP + " años) excede el máximo permitido para este tour (" + edadMaxBebe + " años).");
                            return response;
                        }
                    }
                }
            }

            // 5. Cupo atómico en TourFecha
            // "Adultos y niños ocupan cupo; el bebé solo si aceptaBebes=1 y precioBebe>0."
            boolean bebeOcupaCupo = aceptaBebes && (precioBebe.compareTo(BigDecimal.ZERO) > 0);
            int cuposRequeridos = cantAdultos + cantNinos + (bebeOcupaCupo ? cantBebes : 0);

            String sqlUpdateCupo = "UPDATE TourFecha " +
                                  "SET cupoDisponible = cupoDisponible - ?, " +
                                  "    estado = CASE WHEN (cupoDisponible - ?) <= 0 THEN 'AGOTADO' ELSE 'DISPONIBLE' END " +
                                  "WHERE idTourFecha = ? AND cupoDisponible >= ?";
            int filasActualizadas = 0;
            try (PreparedStatement psUC = con.prepareStatement(sqlUpdateCupo)) {
                psUC.setInt(1, cuposRequeridos);
                psUC.setInt(2, cuposRequeridos);
                psUC.setInt(3, idTourFecha);
                psUC.setInt(4, cuposRequeridos);
                filasActualizadas = psUC.executeUpdate();
            }

            if (filasActualizadas == 0) {
                con.rollback();
                response.put("status", "error");
                response.put("code", "SIN_CUPO");
                response.put("message", "Sin cupo disponible para esta salida.");
                return response;
            }

            // 6. RECALCULAR TOTAL EN EL SERVIDOR (Nunca confiar en el precio del navegador)
            BigDecimal subtotalAdultos = precioAdulto.multiply(BigDecimal.valueOf(cantAdultos));
            BigDecimal subtotalNinos = precioNino.multiply(BigDecimal.valueOf(cantNinos));
            BigDecimal subtotalBebes = precioBebe.multiply(BigDecimal.valueOf(cantBebes));
            BigDecimal subtotalReal = subtotalAdultos.add(subtotalNinos).add(subtotalBebes);

            BigDecimal descuentoMonto = subtotalReal.multiply(descuentoPct).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            BigDecimal totalReal = subtotalReal.subtract(descuentoMonto).setScale(2, RoundingMode.HALF_UP);

            // 7. Insertar Reserva
            String codigoReserva = String.valueOf(reqData.getOrDefault("codigoReserva", reqData.getOrDefault("codigo", "TRK-" + (1000 + (int)(Math.random() * 9000)))));
            String tipoPago = String.valueOf(reqData.getOrDefault("tipoPago", reqData.getOrDefault("metodoPago", "Tarjeta")));
            boolean esEfectivo = "Efectivo".equalsIgnoreCase(tipoPago) || "Presencial".equalsIgnoreCase(tipoPago);
            String estadoReserva = esEfectivo ? "Pendiente" : "Confirmada";

            String fechaInicioStr = fechaSalida != null ? fechaSalida.toString() : new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
            String fechaFinStr = fechaInicioStr;

            int idReservaGenerada = 0;
            String sqlReserva = "INSERT INTO Reserva (idUsuario, idAgencia, idTour, idTourFecha, nombreTour, codigoReserva, fechaRegistro, fechaInicio, fechaFin, estado, total) " +
                                "VALUES (?, ?, ?, ?, ?, ?, NOW(), ?, ?, ?, ?)";
            try (PreparedStatement psR = con.prepareStatement(sqlReserva, Statement.RETURN_GENERATED_KEYS)) {
                psR.setInt(1, idUsuario);
                psR.setInt(2, idAgencia);
                psR.setInt(3, idTour);
                psR.setInt(4, idTourFecha);
                psR.setString(5, nombreTour);
                psR.setString(6, codigoReserva);
                psR.setString(7, fechaInicioStr);
                psR.setString(8, fechaFinStr);
                psR.setString(9, estadoReserva);
                psR.setBigDecimal(10, totalReal);
                psR.executeUpdate();

                try (ResultSet rsR = psR.getGeneratedKeys()) {
                    if (rsR.next()) idReservaGenerada = rsR.getInt(1);
                }
            }

            // 8. Insertar DetalleReserva (Desglose exacto: tipo, cantidad, precio unitario, subtotal)
            // Fila resumen compatible con frontend antiguo
            String sqlDetalleBase = "INSERT INTO DetalleReserva (idReserva, idTourFecha, cantAdultos, cantNinos, cantBebes, tipo, cantidad, precioUnitario, subtotal) " +
                                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

            // 8.1 Registro para Adultos
            try (PreparedStatement psDA = con.prepareStatement(sqlDetalleBase)) {
                psDA.setInt(1, idReservaGenerada);
                psDA.setInt(2, idTourFecha);
                psDA.setInt(3, cantAdultos);
                psDA.setInt(4, cantNinos);
                psDA.setInt(5, cantBebes);
                psDA.setString(6, "ADULTO");
                psDA.setInt(7, cantAdultos);
                psDA.setBigDecimal(8, precioAdulto);
                psDA.setBigDecimal(9, subtotalAdultos);
                psDA.executeUpdate();
            }

            // 8.2 Registro para Niños si aplica
            if (cantNinos > 0) {
                try (PreparedStatement psDN = con.prepareStatement(sqlDetalleBase)) {
                    psDN.setInt(1, idReservaGenerada);
                    psDN.setInt(2, idTourFecha);
                    psDN.setInt(3, 0);
                    psDN.setInt(4, cantNinos);
                    psDN.setInt(5, 0);
                    psDN.setString(6, "NINO");
                    psDN.setInt(7, cantNinos);
                    psDN.setBigDecimal(8, precioNino);
                    psDN.setBigDecimal(9, subtotalNinos);
                    psDN.executeUpdate();
                }
            }

            // 8.3 Registro para Bebés si aplica
            if (cantBebes > 0) {
                try (PreparedStatement psDB = con.prepareStatement(sqlDetalleBase)) {
                    psDB.setInt(1, idReservaGenerada);
                    psDB.setInt(2, idTourFecha);
                    psDB.setInt(3, 0);
                    psDB.setInt(4, 0);
                    psDB.setInt(5, cantBebes);
                    psDB.setString(6, "BEBE");
                    psDB.setInt(7, cantBebes);
                    psDB.setBigDecimal(8, precioBebe);
                    psDB.setBigDecimal(9, subtotalBebes);
                    psDB.executeUpdate();
                }
            }

            // 9. Insertar Pasajeros (Titular y Acompañantes con tipo y edad)
            String sqlPasajero = "INSERT INTO Pasajero (idReserva, nroDocumento, telefono, nombre, apellidos, esTitular, tipo, edad) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement psP = con.prepareStatement(sqlPasajero)) {
                // Pasajero Titular
                psP.setInt(1, idReservaGenerada);
                psP.setString(2, titularDoc);
                psP.setString(3, titularTel);
                psP.setString(4, titularNombre);
                psP.setString(5, titularApellidos);
                psP.setBoolean(6, true);
                psP.setString(7, "ADULTO");
                psP.setInt(8, 30);
                psP.executeUpdate();

                // Pasajeros adicionales especificados en payload o automáticos
                int index = 2;
                if (pasajerosReq != null && !pasajerosReq.isEmpty()) {
                    for (Map<String, Object> pMap : pasajerosReq) {
                        boolean titularCheck = Boolean.parseBoolean(String.valueOf(pMap.getOrDefault("esTitular", "false")));
                        if (titularCheck) continue; // ya insertado

                        String pNom = String.valueOf(pMap.getOrDefault("nombre", "Pasajero " + index));
                        String pApe = String.valueOf(pMap.getOrDefault("apellidos", "Acompañante"));
                        String pDoc = String.valueOf(pMap.getOrDefault("nroDocumento", String.valueOf(pMap.getOrDefault("dni", "8765432" + index))));
                        String pTel = String.valueOf(pMap.getOrDefault("telefono", "91234567" + index));
                        String pTipo = String.valueOf(pMap.getOrDefault("tipo", "ADULTO")).toUpperCase();
                        int pEdad = 0;
                        try { pEdad = Integer.parseInt(String.valueOf(pMap.getOrDefault("edad", "25"))); } catch (Exception ignored) {}

                        psP.setInt(1, idReservaGenerada);
                        psP.setString(2, pDoc);
                        psP.setString(3, pTel);
                        psP.setString(4, pNom);
                        psP.setString(5, pApe);
                        psP.setBoolean(6, false);
                        psP.setString(7, pTipo);
                        psP.setInt(8, pEdad);
                        psP.executeUpdate();
                        index++;
                    }
                } else {
                    // Generar acompañantes según conteos restantes
                    for (int i = 1; i < cantAdultos; i++) {
                        psP.setInt(1, idReservaGenerada);
                        psP.setString(2, "8765432" + index);
                        psP.setString(3, "91234567" + index);
                        psP.setString(4, "Acompañante Adulto " + i);
                        psP.setString(5, titularApellidos);
                        psP.setBoolean(6, false);
                        psP.setString(7, "ADULTO");
                        psP.setInt(8, 28);
                        psP.executeUpdate();
                        index++;
                    }
                    for (int i = 0; i < cantNinos; i++) {
                        psP.setInt(1, idReservaGenerada);
                        psP.setString(2, "7165432" + index);
                        psP.setString(3, "91234567" + index);
                        psP.setString(4, "Niño " + (i + 1));
                        psP.setString(5, titularApellidos);
                        psP.setBoolean(6, false);
                        psP.setString(7, "NINO");
                        psP.setInt(8, Math.min(8, edadMaxNino));
                        psP.executeUpdate();
                        index++;
                    }
                    for (int i = 0; i < cantBebes; i++) {
                        psP.setInt(1, idReservaGenerada);
                        psP.setString(2, "6165432" + index);
                        psP.setString(3, "91234567" + index);
                        psP.setString(4, "Bebé " + (i + 1));
                        psP.setString(5, titularApellidos);
                        psP.setBoolean(6, false);
                        psP.setString(7, "BEBE");
                        psP.setInt(8, Math.min(1, edadMaxBebe));
                        psP.executeUpdate();
                        index++;
                    }
                }
            }

            // 10. Método de pago y Pago
            // REGLA DE SEGURIDAD: NUNCA GUARDES NÚMERO COMPLETO DE TARJETA NI CVV. Solo marca y últimos 4 dígitos.
            String marcaTarjeta = null;
            String ultimosDigitos = null;
            String numTarjetaRaw = String.valueOf(reqData.getOrDefault("numTarjeta", reqData.getOrDefault("numeroTarjeta", "")));
            if (!numTarjetaRaw.trim().isEmpty()) {
                String cleanDigits = numTarjetaRaw.replaceAll("\\D", "");
                if (cleanDigits.length() >= 4) {
                    ultimosDigitos = cleanDigits.substring(cleanDigits.length() - 4);
                }
            }
            if (reqData.containsKey("ultimosDigitos")) {
                ultimosDigitos = String.valueOf(reqData.get("ultimosDigitos")).trim();
            }

            String marcaRaw = String.valueOf(reqData.getOrDefault("marcaTarjeta", reqData.getOrDefault("marca", "")));
            if (!marcaRaw.trim().isEmpty()) {
                marcaTarjeta = marcaRaw;
            } else if ("Tarjeta".equalsIgnoreCase(tipoPago)) {
                marcaTarjeta = "Visa";
                if (ultimosDigitos == null) ultimosDigitos = "4587";
            }

            BigDecimal montoPago = totalReal;
            boolean esAdelanto = esEfectivo;
            if (esAdelanto) {
                // Efectivo: 15% de adelanto
                montoPago = totalReal.multiply(new BigDecimal("0.15")).setScale(2, RoundingMode.HALF_UP);
            }
            String estadoPago = esAdelanto ? "Pendiente" : "Completado";

            String numOperacion = String.valueOf(reqData.getOrDefault("numeroOperacion", codigoReserva));
            String urlComprobante = String.valueOf(reqData.getOrDefault("urlComprobante", reqData.getOrDefault("comprobante", "")));

            int idPagoGenerado = 0;
            String sqlMetodo = "INSERT INTO MetodoPago (idReserva, tipoPago, monto, esAdelanto, estadoPago, numeroOperacion, marcaTarjeta, ultimosDigitos, urlComprobante, fechaPago) " +
                               "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())";
            try (PreparedStatement psM = con.prepareStatement(sqlMetodo, Statement.RETURN_GENERATED_KEYS)) {
                psM.setInt(1, idReservaGenerada);
                psM.setString(2, tipoPago);
                psM.setBigDecimal(3, montoPago);
                psM.setBoolean(4, esAdelanto);
                psM.setString(5, estadoPago);
                psM.setString(6, numOperacion);
                psM.setString(7, marcaTarjeta);
                psM.setString(8, ultimosDigitos);
                psM.setString(9, urlComprobante);
                psM.executeUpdate();

                try (ResultSet rsM = psM.getGeneratedKeys()) {
                    if (rsM.next()) idPagoGenerado = rsM.getInt(1);
                }
            }

            // Insertar Pago oficial
            try {
                String sqlPago = "INSERT INTO Pago (monto, fechaPago, metodoPago, estado, numeroOperacion, idReserva) VALUES (?, NOW(), ?, ?, ?, ?)";
                try (PreparedStatement psPO = con.prepareStatement(sqlPago)) {
                    psPO.setBigDecimal(1, montoPago);
                    psPO.setString(2, tipoPago);
                    psPO.setString(3, "Completado".equalsIgnoreCase(estadoPago) ? "COMPLETADO" : "PENDIENTE");
                    psPO.setString(4, numOperacion);
                    psPO.setInt(5, idReservaGenerada);
                    psPO.executeUpdate();
                }
            } catch (Exception exP) {
                System.out.println("Aviso Pago: " + exP.getMessage());
            }

            // 11. Comisión del 15% sobre el total real
            try {
                String periodo = new SimpleDateFormat("yyyy-MM").format(new java.util.Date());
                BigDecimal montoComision = totalReal.multiply(new BigDecimal("0.15")).setScale(2, RoundingMode.HALF_UP);
                BigDecimal montoAgencia = totalReal.subtract(montoComision).setScale(2, RoundingMode.HALF_UP);

                String sqlComision = "INSERT INTO Comision (porcentaje, montoComision, montoNeto, fechaCalculo, estado, idReserva, idAgencia, subtotal, porcentajeComision, montoAgencia, estadoLiquidacion, periodo, ventasTotales, montoTravelink) " +
                                     "VALUES (15.00, ?, ?, NOW(), 'Pendiente', ?, ?, ?, 15.00, ?, 'Pendiente', ?, ?, ?)";
                try (PreparedStatement psC = con.prepareStatement(sqlComision)) {
                    psC.setBigDecimal(1, montoComision);
                    psC.setBigDecimal(2, montoAgencia);
                    psC.setInt(3, idReservaGenerada);
                    psC.setInt(4, idAgencia);
                    psC.setBigDecimal(5, totalReal);
                    psC.setBigDecimal(6, montoAgencia);
                    psC.setString(7, periodo);
                    psC.setBigDecimal(8, totalReal);
                    psC.setBigDecimal(9, montoComision);
                    psC.executeUpdate();
                }
            } catch (Exception exCom) {
                System.out.println("Aviso Comision: " + exCom.getMessage());
            }

            // 12. Notificaciones
            try {
                String sqlNotif = "INSERT INTO Notificacion (idUsuario, titulo, mensaje, tipo) VALUES (?, ?, ?, ?)";
                try (PreparedStatement psN = con.prepareStatement(sqlNotif)) {
                    psN.setInt(1, idUsuario);
                    psN.setString(2, "Reserva Realizada");
                    psN.setString(3, "Tu reserva " + codigoReserva + " ha sido procesada correctamente.");
                    psN.setString(4, "EXITO");
                    psN.executeUpdate();

                    psN.setInt(1, 2); // usuario agencia
                    psN.setString(2, "Nueva Reserva");
                    psN.setString(3, "Reserva " + codigoReserva + " recibida por S/ " + totalReal + " (" + cuposRequeridos + " cupos).");
                    psN.setString(4, "INFO");
                    psN.executeUpdate();
                }
            } catch (Exception ignored) {}

            con.commit();
            try (Statement st = con.createStatement()) {
                st.execute("SET FOREIGN_KEY_CHECKS = 1;");
            }

            String desgloseTexto = formatDesglose(cantAdultos, precioAdulto, cantNinos, precioNino, cantBebes, precioBebe);

            Map<String, Object> data = new HashMap<>();
            data.put("idReserva", idReservaGenerada);
            data.put("idPago", idPagoGenerado);
            data.put("codigo", codigoReserva);
            data.put("estado", estadoReserva);
            data.put("total", totalReal.doubleValue());
            data.put("precioTotal", totalReal.doubleValue());
            data.put("metodoPago", tipoPago);
            data.put("desglose", desgloseTexto);
            data.put("fechaSalida", fechaSalida != null ? fechaSalida.toString() : fechaInicioStr);
            data.put("horaSalida", horaInicio != null ? horaInicio.toString().substring(0, 5) : "08:00");
            data.put("cuposRestantes", cupoDisponible - cuposRequeridos);

            response.put("status", "success");
            response.put("message", "Reserva guardada exitosamente");
            response.put("data", data);
            return response;

        } catch (Exception e) {
            if (con != null) {
                try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", "Error al guardar reserva: " + e.getMessage());
            return response;
        } finally {
            if (con != null) {
                try { con.setAutoCommit(true); con.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    public Map<String, Object> obtenerReservasPorUsuario(String correo, int idUsuario) {
        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> reservas = new ArrayList<>();

        try (Connection con = ConexionDB.getConnection()) {
            if (idUsuario <= 0 && correo != null && !correo.trim().isEmpty()) {
                String sqlU = "SELECT u.idUsuario FROM Usuario u INNER JOIN Persona p ON u.idPersona = p.idPersona WHERE p.email = ? OR u.nombreUsuario = ?";
                try (PreparedStatement psU = con.prepareStatement(sqlU)) {
                    psU.setString(1, correo);
                    psU.setString(2, correo);
                    try (ResultSet rsU = psU.executeQuery()) {
                        if (rsU.next()) idUsuario = rsU.getInt(1);
                    }
                }
            }

            String query = "SELECT r.idReserva, r.codigoReserva, r.fechaRegistro, r.fechaInicio, r.fechaFin, r.estado, r.total, " +
                           "r.idTour, r.idAgencia, " +
                           "(SELECT COUNT(*) FROM Calificacion c WHERE c.idReserva = r.idReserva AND (c.eliminadoPorAdmin IS NULL OR c.eliminadoPorAdmin = 0)) AS calificadaCount, " +
                           "t.nombre AS tourNombre, t.ubicacion AS tourUbicacion, a.nombreComercial AS agenciaNombre, " +
                           "t.precioAdulto, t.precioNino, t.precioBebe, " +
                           "tf.fecha AS fechaSalida, tf.horaInicio AS horaSalida, " +
                           "mp.tipoPago, mp.estadoPago, mp.marcaTarjeta, mp.ultimosDigitos, " +
                           "d.cantAdultos, d.cantNinos, d.cantBebes, " +
                           "COALESCE(ti.url, '../../img/valle.jpg') AS tourImagen " +
                           "FROM Reserva r " +
                           "LEFT JOIN DetalleReserva d ON r.idReserva = d.idReserva AND (d.tipo IS NULL OR d.tipo = 'ADULTO') " +
                           "LEFT JOIN TourFecha tf ON r.idTourFecha = tf.idTourFecha " +
                           "LEFT JOIN Tour t ON (r.idTour = t.idTour OR tf.idTour = t.idTour) " +
                           "LEFT JOIN Agencia a ON (r.idAgencia = a.idAgencia OR t.idAgencia = a.idAgencia) " +
                           "LEFT JOIN MetodoPago mp ON r.idReserva = mp.idReserva " +
                           "LEFT JOIN TourImagen ti ON (t.idTour = ti.idTour AND ti.esPrincipal = 1) ";

            if (idUsuario > 0) {
                query += "WHERE r.idUsuario = ? ";
            }
            query += "ORDER BY r.idReserva DESC";

            try (PreparedStatement ps = con.prepareStatement(query)) {
                if (idUsuario > 0) ps.setInt(1, idUsuario);
                try (ResultSet rs = ps.executeQuery()) {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd MMM. yyyy");
                    while (rs.next()) {
                        Map<String, Object> res = new HashMap<>();
                        int idRes = rs.getInt("idReserva");
                        String code = rs.getString("codigoReserva");

                        java.sql.Date fSalida = rs.getDate("fechaSalida");
                        if (fSalida == null) fSalida = rs.getDate("fechaInicio");
                        Time hSalida = rs.getTime("horaSalida");
                        String horaStr = hSalida != null ? hSalida.toString().substring(0, 5) : "08:00";
                        String fechaFmt = fSalida != null ? sdf.format(fSalida) + " - " + horaStr : "Próximamente";

                        int adultos = rs.getInt("cantAdultos");
                        if (adultos < 1) adultos = 1;
                        int ninos = rs.getInt("cantNinos");
                        int bebes = rs.getInt("cantBebes");
                        int personasTotales = adultos + ninos + bebes;

                        BigDecimal pAdulto = rs.getBigDecimal("precioAdulto");
                        BigDecimal pNino = rs.getBigDecimal("precioNino");
                        BigDecimal pBebe = rs.getBigDecimal("precioBebe");
                        String desglose = formatDesglose(adultos, pAdulto, ninos, pNino, bebes, pBebe);

                        res.put("id", idRes);
                        res.put("codigo", code != null ? code : String.format("TRK-%03d", idRes));
                        res.put("idTour", rs.getInt("idTour"));
                        res.put("idAgencia", rs.getInt("idAgencia"));
                        res.put("calificada", rs.getInt("calificadaCount") > 0);
                        res.put("titulo", rs.getString("tourNombre") != null ? rs.getString("tourNombre") : "Machu Picchu Clásico");
                        res.put("ubicacion", rs.getString("tourUbicacion") != null ? rs.getString("tourUbicacion") : "Cusco, Perú");
                        res.put("fechas", fechaFmt);
                        res.put("fechaSalida", fSalida != null ? fSalida.toString() : "");
                        res.put("horaSalida", horaStr);
                        res.put("personas", personasTotales + (personasTotales == 1 ? " persona" : " personas"));
                        res.put("cantAdultos", adultos);
                        res.put("cantNinos", ninos);
                        res.put("cantBebes", bebes);
                        res.put("desglose", desglose);
                        res.put("agencia", (rs.getString("agenciaNombre") != null ? rs.getString("agenciaNombre") : "Andes Tours"));
                        res.put("estado", rs.getString("estado") != null ? rs.getString("estado") : "Confirmada");
                        res.put("total", rs.getDouble("total"));
                        res.put("metodoPago", rs.getString("tipoPago") != null ? rs.getString("tipoPago") : "Tarjeta");
                        res.put("marcaTarjeta", rs.getString("marcaTarjeta"));
                        res.put("ultimosDigitos", rs.getString("ultimosDigitos"));
                        res.put("imagen", rs.getString("tourImagen") != null ? rs.getString("tourImagen") : "../../img/valle.jpg");

                        Timestamp fReg = rs.getTimestamp("fechaRegistro");
                        res.put("fechaRegistro", fReg != null ? fReg.toString().substring(0, 10) : "");

                        reservas.add(res);
                    }
                }
            }

            response.put("status", "success");
            response.put("reservas", reservas);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", "Error al obtener reservas: " + e.getMessage());
            return response;
        }
    }

    public boolean cancelarReserva(int idReserva, String motivo) {
        Connection con = null;
        try {
            con = ConexionDB.getConnection();
            con.setAutoCommit(false);

            // 1. Obtener datos de la reserva para devolver el cupo atómicamente
            int idTourFecha = 0;
            int cantAdultos = 1;
            int cantNinos = 0;
            int cantBebes = 0;
            boolean aceptaBebes = false;
            BigDecimal precioBebe = BigDecimal.ZERO;
            String estadoActual = "";

            String sqlRes = "SELECT r.estado, r.idTourFecha, d.cantAdultos, d.cantNinos, d.cantBebes, t.aceptaBebes, t.precioBebe " +
                            "FROM Reserva r " +
                            "LEFT JOIN DetalleReserva d ON r.idReserva = d.idReserva AND (d.tipo IS NULL OR d.tipo = 'ADULTO') " +
                            "LEFT JOIN TourFecha tf ON r.idTourFecha = tf.idTourFecha " +
                            "LEFT JOIN Tour t ON (r.idTour = t.idTour OR tf.idTour = t.idTour) " +
                            "WHERE r.idReserva = ?";
            try (PreparedStatement psR = con.prepareStatement(sqlRes)) {
                psR.setInt(1, idReserva);
                try (ResultSet rsR = psR.executeQuery()) {
                    if (rsR.next()) {
                        estadoActual = rsR.getString("estado");
                        idTourFecha = rsR.getInt("idTourFecha");
                        cantAdultos = rsR.getInt("cantAdultos");
                        if (cantAdultos < 1) cantAdultos = 1;
                        cantNinos = rsR.getInt("cantNinos");
                        cantBebes = rsR.getInt("cantBebes");
                        aceptaBebes = rsR.getBoolean("aceptaBebes");
                        precioBebe = rsR.getBigDecimal("precioBebe");
                        if (precioBebe == null) precioBebe = BigDecimal.ZERO;
                    } else {
                        con.rollback();
                        return false;
                    }
                }
            }

            if ("Cancelada".equalsIgnoreCase(estadoActual)) {
                con.commit();
                return true; // Ya cancelada
            }

            // 2. Devolver cupo atómicamente en TourFecha
            // "Cancelar la reserva devuelve el cupo"
            boolean bebeOcupaCupo = aceptaBebes && (precioBebe.compareTo(BigDecimal.ZERO) > 0);
            int cuposADevolver = cantAdultos + cantNinos + (bebeOcupaCupo ? cantBebes : 0);

            if (idTourFecha > 0 && cuposADevolver > 0) {
                String sqlRestore = "UPDATE TourFecha SET cupoDisponible = cupoDisponible + ?, estado = 'DISPONIBLE' WHERE idTourFecha = ?";
                try (PreparedStatement psUp = con.prepareStatement(sqlRestore)) {
                    psUp.setInt(1, cuposADevolver);
                    psUp.setInt(2, idTourFecha);
                    psUp.executeUpdate();
                }
            }

            // 3. Marcar reserva como Cancelada
            String sqlCancel = "UPDATE Reserva SET estado = 'Cancelada', motivoCancelacion = ? WHERE idReserva = ?";
            try (PreparedStatement psC = con.prepareStatement(sqlCancel)) {
                psC.setString(1, motivo != null ? motivo : "Cancelado por el usuario");
                psC.setInt(2, idReserva);
                psC.executeUpdate();
            }

            // 4. Actualizar pagos
            try (PreparedStatement psM = con.prepareStatement("UPDATE MetodoPago SET estadoPago = 'Cancelado' WHERE idReserva = ?")) {
                psM.setInt(1, idReserva);
                psM.executeUpdate();
            } catch (Exception ignored) {}
            try (PreparedStatement psP = con.prepareStatement("UPDATE Pago SET estado = 'RECHAZADO' WHERE idReserva = ?")) {
                psP.setInt(1, idReserva);
                psP.executeUpdate();
            } catch (Exception ignored) {}

            con.commit();
            return true;
        } catch (SQLException e) {
            if (con != null) {
                try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (con != null) {
                try { con.setAutoCommit(true); con.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }
}
