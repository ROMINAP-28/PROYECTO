package com.travelink.controlador.admin;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.File;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import com.travelink.entidades.Usuario;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.util.Map;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import com.travelink.config.ConexionDB;
import com.travelink.server.JavaApiServer;
import java.util.Locale;
import java.util.ArrayList;
import java.util.List;
import com.travelink.controlador.UsuarioControlador;
import com.travelink.controlador.CalificacionControlador;
import com.travelink.repositorio.ReservaRepositorio;

    public class DashboardAdminHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            JavaApiServer.enableCORS(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }
            double ventasTotales = 0.0;
            int reservasRealizadas = 0;
            int agenciasActivas = 0;
            double comisionGenerada = 0.0;
            StringBuilder detalleVentasJson = new StringBuilder();

            try (Connection con = ConexionDB.getConnection()) {
                if (con != null) {
                    try (Statement stmt = con.createStatement();
                         ResultSet rs = stmt.executeQuery("SELECT COALESCE(SUM(total), 0) AS total, COUNT(*) AS cant FROM Reserva WHERE estado = 'CONFIRMADA'")) {
                        if (rs.next()) {
                            ventasTotales = rs.getDouble("total");
                            reservasRealizadas = rs.getInt("cant");
                            comisionGenerada = ventasTotales * 0.15;
                        }
                    }
                    try (Statement stmt = con.createStatement();
                         ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Agencia WHERE estado = 'ACTIVO'")) {
                        if (rs.next()) {
                            agenciasActivas = rs.getInt(1);
                        }
                    }

                    // Detalle de ventas completo
                    String sqlDetalle = """
                        SELECT r.idReserva, r.codigoReserva, DATE(r.fechaRegistro) AS fecha,
                               COALESCE(CONCAT(p.nombre, ' ', p.apellidoPaterno), u.nombreUsuario, 'Turista') AS cliente,
                               COALESCE(d.nombre, 'Cusco') AS destino,
                               COALESCE(r.nombreTour, 'Tour Turístico') AS tour,
                               COALESCE(a.razonSocial, a.nombreComercial, 'Agencia Travelink') AS agencia,
                               r.total, (r.total * 0.15) AS comision, r.estado
                        FROM Reserva r
                        JOIN Usuario u ON r.idUsuario = u.idUsuario
                        JOIN Persona p ON u.idPersona = p.idPersona
                        LEFT JOIN Agencia a ON r.idAgencia = a.idAgencia
                        LEFT JOIN DetalleReserva dr ON r.idReserva = dr.idReserva
                        LEFT JOIN TourFecha tf ON dr.idTourFecha = tf.idTourFecha
                        LEFT JOIN Tour t ON tf.idTour = t.idTour
                        LEFT JOIN Destino d ON t.idDestino = d.idDestino
                        ORDER BY r.idReserva DESC
                        """;
                    try (Statement stmt = con.createStatement();
                         ResultSet rsV = stmt.executeQuery(sqlDetalle)) {
                        boolean primero = true;
                        while (rsV.next()) {
                            if (!primero) detalleVentasJson.append(",");
                            primero = false;
                            detalleVentasJson.append("{")
                                .append("\"id\":").append(rsV.getInt("idReserva")).append(",")
                                .append("\"fecha\":\"").append(rsV.getString("fecha")).append("\",")
                                .append("\"nroReserva\":\"").append(JavaApiServer.jsonEscape(rsV.getString("codigoReserva"))).append("\",")
                                .append("\"cliente\":\"").append(JavaApiServer.jsonEscape(rsV.getString("cliente"))).append("\",")
                                .append("\"destino\":\"").append(JavaApiServer.jsonEscape(rsV.getString("destino"))).append("\",")
                                .append("\"tour\":\"").append(JavaApiServer.jsonEscape(rsV.getString("tour"))).append("\",")
                                .append("\"agencia\":\"").append(JavaApiServer.jsonEscape(rsV.getString("agencia"))).append("\",")
                                .append("\"monto\":").append(rsV.getDouble("total")).append(",")
                                .append("\"comision\":").append(rsV.getDouble("comision")).append(",")
                                .append("\"estado\":\"").append(JavaApiServer.jsonEscape(rsV.getString("estado"))).append("\"")
                                .append("}");
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            StringBuilder json = new StringBuilder();
            json.append("{")
                .append("\"status\":\"success\",")
                .append("\"data\":{")
                .append("\"ventasTotales\":").append(ventasTotales).append(",")
                .append("\"reservasRealizadas\":").append(reservasRealizadas).append(",")
                .append("\"agenciasActivas\":").append(agenciasActivas).append(",")
                .append("\"comisionGenerada\":").append(comisionGenerada).append(",")
                .append("\"chartEvolucionLabels\":[\"Ene\",\"Feb\",\"Mar\",\"Abr\",\"May\",\"Jun\",\"Jul\",\"Ago\",\"Set\",\"Oct\"],")
                .append("\"chartEvolucionValues\":[1200,1800,2400,3100,4200,5100,6300,7800,8900,").append(ventasTotales).append("],")
                .append("\"destinosLabels\":[\"Cusco\",\"Lima\",\"Ilo\",\"Tacna\",\"Moquegua\",\"Pasco\"],")
                .append("\"destinosPercentages\":[35,20,15,12,10,8],")
                .append("\"detalleVentas\":[").append(detalleVentasJson.toString()).append("]")
                .append("}}");

            JavaApiServer.sendJsonResponse(exchange, 200, json.toString());
        }
    }

