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

    public class ReservasAdminHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            JavaApiServer.enableCORS(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }
            StringBuilder json = new StringBuilder("{\"status\":\"success\",\"data\":[");
            boolean primero = true;
            String sql = "SELECT r.idReserva AS id, r.codigoReserva AS codigo, CONCAT(p.nombre, ' ', p.apellidoPaterno) AS turista, p.email, COALESCE(r.nombreTour, 'Tour Turístico') AS tour, a.razonSocial AS agencia, r.fechaInicio AS fechaTour, (COALESCE(dr.cantAdultos, 1) + COALESCE(dr.cantNinos, 0)) AS cupos, r.total, r.estado, DATE(r.fechaRegistro) AS fechaReserva FROM Reserva r JOIN Usuario u ON r.idUsuario = u.idUsuario JOIN Persona p ON u.idPersona = p.idPersona JOIN Agencia a ON r.idAgencia = a.idAgencia LEFT JOIN DetalleReserva dr ON r.idReserva = dr.idReserva ORDER BY r.idReserva DESC";
            try (Connection con = ConexionDB.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (!primero) json.append(",");
                    primero = false;
                    json.append("{")
                        .append("\"id\":").append(rs.getInt("id")).append(",")
                        .append("\"codigo\":\"").append(JavaApiServer.jsonEscape(rs.getString("codigo"))).append("\",")
                        .append("\"turista\":\"").append(JavaApiServer.jsonEscape(rs.getString("turista"))).append("\",")
                        .append("\"email\":\"").append(JavaApiServer.jsonEscape(rs.getString("email"))).append("\",")
                        .append("\"tour\":\"").append(JavaApiServer.jsonEscape(rs.getString("tour"))).append("\",")
                        .append("\"agencia\":\"").append(JavaApiServer.jsonEscape(rs.getString("agencia"))).append("\",")
                        .append("\"fechaTour\":\"").append(rs.getString("fechaTour")).append("\",")
                        .append("\"cupos\":").append(rs.getInt("cupos")).append(",")
                        .append("\"total\":").append(rs.getBigDecimal("total")).append(",")
                        .append("\"estado\":\"").append(JavaApiServer.jsonEscape(rs.getString("estado"))).append("\",")
                        .append("\"fechaReserva\":\"").append(rs.getString("fechaReserva")).append("\"")
                        .append("}");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            json.append("]}");
            JavaApiServer.sendJsonResponse(exchange, 200, json.toString());
        }
    }

